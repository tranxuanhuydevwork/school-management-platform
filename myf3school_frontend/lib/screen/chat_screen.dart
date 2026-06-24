import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'package:intl/intl.dart';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';

// ─── MODEL ────────────────────────────────────────────────────
class ChatMessage {
  final String id;
  final int senderId;
  final String senderName;
  final String senderRole;
  final int receiverId;
  final String content;
  final DateTime timestamp;
  final bool isRead;

  ChatMessage({
    required this.id,
    required this.senderId,
    required this.senderName,
    required this.senderRole,
    required this.receiverId,
    required this.content,
    required this.timestamp,
    this.isRead = false,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> j) => ChatMessage(
    id: j['id']?.toString() ?? '',
    senderId: _parseInt(j['senderId']),
    senderName: j['senderName'] ?? '',
    senderRole: j['senderRole'] ?? 'STUDENT',
    receiverId: _parseInt(j['receiverId']),
    content: j['content'] ?? '',
    timestamp: _parseTime(j['timestamp']),
    isRead: j['isRead'] ?? false,
  );

  static int _parseInt(dynamic v) =>
      v is int ? v : int.tryParse(v?.toString() ?? '0') ?? 0;

  static DateTime _parseTime(dynamic v) {
    if (v == null) return DateTime.now();
    try {
      return DateTime.parse(v.toString()).toLocal();
    } catch (_) {
      return DateTime.now();
    }
  }

  Map<String, dynamic> toJson() => {
    'senderId': senderId,
    'senderName': senderName,
    'senderRole': senderRole,
    'receiverId': receiverId,
    'content': content,
    'timestamp': timestamp.toUtc().toIso8601String(),
  };
}

// ─── SERVICE (singleton) ──────────────────────────────────────
class ChatService {
  static final ChatService _i = ChatService._();
  factory ChatService() => _i;
  ChatService._();

  StompClient? _client;
  bool connected = false;

  // partnerId → list of callbacks
  final Map<int, List<Function(ChatMessage)>> _listeners = {};

  final String baseUrl = 'http://10.0.2.2:8080';

  // ── Connect WebSocket ───────────────────────────────────────
  void connect({required String token, required int userId}) {
    if (connected) return;

    _client = StompClient(
      config: StompConfig.sockJS(
        url: '$baseUrl/ws-chat',
        onConnect: (frame) {
          connected = true;

          _client!.subscribe(
            destination: '/user/$userId/queue/messages',
            callback: (frame) {
              if (frame.body == null) return;
              try {
                final msg = ChatMessage.fromJson(jsonDecode(frame.body!));
                // Deliver to whichever chat screen is open
                final cbs = _listeners[msg.senderId] ?? [];
                for (final cb in cbs) cb(msg);
              } catch (_) {}
            },
          );
        },
        onDisconnect: (_) => connected = false,
        onWebSocketError: (_) => connected = false,
        webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
        stompConnectHeaders: {'Authorization': 'Bearer $token'},
        reconnectDelay: const Duration(seconds: 5),
      ),
    );

    _client!.activate();
  }

  // ── Send via WebSocket ──────────────────────────────────────
  void send(ChatMessage msg) {
    _client?.send(
      destination: '/app/chat.send',
      body: jsonEncode(msg.toJson()),
    );
  }

  // ── REST: load history ──────────────────────────────────────
  Future<List<ChatMessage>> fetchHistory({
    required String token,
    required int userId,
    required int partnerId,
    int page = 0,
    int size = 50,
  }) async {
    final uri = Uri.parse(
      '$baseUrl/api/chat/history'
          '?userId=$userId&partnerId=$partnerId&page=$page&size=$size',
    );
    final res = await http.get(uri, headers: {'Authorization': 'Bearer $token'});

    if (res.statusCode == 200) {
      final List<dynamic> data = jsonDecode(res.body)['data'] ?? [];
      return data.map((e) => ChatMessage.fromJson(e)).toList();
    }
    throw Exception('Lỗi tải lịch sử: ${res.statusCode}');
  }

  // ── REST: mark read ─────────────────────────────────────────
  Future<void> markRead({
    required String token,
    required int fromId,
    required int toId,
  }) async {
    await http.post(
      Uri.parse('$baseUrl/api/chat/read?fromId=$fromId&toId=$toId'),
      headers: {'Authorization': 'Bearer $token'},
    );
  }

  // ── Listener management ─────────────────────────────────────
  void addListener(int partnerId, Function(ChatMessage) cb) {
    _listeners.putIfAbsent(partnerId, () => []).add(cb);
  }

  void removeListener(int partnerId, Function(ChatMessage) cb) {
    _listeners[partnerId]?.remove(cb);
  }
}

// ─── SCREEN ───────────────────────────────────────────────────
class ChatScreen extends StatefulWidget {
  final int partnerId;
  final String partnerName;

  const ChatScreen({
    super.key,
    required this.partnerId,
    required this.partnerName,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final List<ChatMessage> _messages = [];
  final TextEditingController _ctrl = TextEditingController();
  final ScrollController _scroll = ScrollController();
  final FocusNode _focusNode = FocusNode();

  bool _loadingHistory = true;
  String? _historyError;

  int get _myId => AuthController.userId ?? 0;
  String get _myName => AuthController.fullName ?? '';
  String get _token => AuthController.sessionToken ?? '';

  @override
  void initState() {
    super.initState();

    // Ensure WebSocket is connected
    if (!ChatService().connected) {
      ChatService().connect(token: _token, userId: _myId);
    }

    // Register listener for incoming messages
    ChatService().addListener(widget.partnerId, _onReceive);

    // Load history from REST API
    _loadHistory();
  }

  @override
  void dispose() {
    ChatService().removeListener(widget.partnerId, _onReceive);
    _ctrl.dispose();
    _scroll.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  // ── Load history ─────────────────────────────────────────────
  Future<void> _loadHistory() async {
    setState(() {
      _loadingHistory = true;
      _historyError = null;
    });

    try {
      final history = await ChatService().fetchHistory(
        token: _token,
        userId: _myId,
        partnerId: widget.partnerId,
      );

      // Mark messages from partner as read
      await ChatService().markRead(
        token: _token,
        fromId: widget.partnerId,
        toId: _myId,
      );

      // Đảm bảo sort đúng thứ tự tăng dần theo thời gian
      history.sort((a, b) => a.timestamp.compareTo(b.timestamp));

      if (mounted) {
        setState(() {
          _messages
            ..clear()
            ..addAll(history);
          _loadingHistory = false;
        });
        _scrollToBottom(jump: true);
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _historyError = e.toString();
          _loadingHistory = false;
        });
      }
    }
  }

  // ── Receive incoming WebSocket message ────────────────────────
  void _onReceive(ChatMessage msg) {
    if (!mounted) return;
    // Avoid duplicate if server echoes back sender's own message
    final alreadyExists = _messages.any((m) =>
    m.senderId == msg.senderId &&
        m.content == msg.content &&
        msg.timestamp.difference(m.timestamp).abs().inSeconds < 3);
    if (alreadyExists && msg.senderId == _myId) return;

    setState(() => _messages.add(msg));
    _scrollToBottom();
  }

  // ── Send message ──────────────────────────────────────────────
  void _send() {
    final text = _ctrl.text.trim();
    if (text.isEmpty) return;

    final msg = ChatMessage(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      senderId: _myId,
      senderName: _myName,
      senderRole: 'STUDENT',
      receiverId: widget.partnerId,
      content: text,
      timestamp: DateTime.now(),
    );

    // Optimistic UI: add immediately
    setState(() => _messages.add(msg));
    _ctrl.clear();
    _focusNode.requestFocus();

    ChatService().send(msg);
    _scrollToBottom();
  }

  void _scrollToBottom({bool jump = false}) {
    Future.delayed(const Duration(milliseconds: 80), () {
      if (!_scroll.hasClients) return;
      if (jump) {
        _scroll.jumpTo(_scroll.position.maxScrollExtent);
      } else {
        _scroll.animateTo(
          _scroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  // ── Date separator label ──────────────────────────────────────
  String _dateSeparator(DateTime dt) {
    final now = DateTime.now();
    if (dt.year == now.year && dt.month == now.month && dt.day == now.day) {
      return 'Hôm nay';
    }
    final yesterday = now.subtract(const Duration(days: 1));
    if (dt.year == yesterday.year &&
        dt.month == yesterday.month &&
        dt.day == yesterday.day) {
      return 'Hôm qua';
    }
    return DateFormat('dd/MM/yyyy').format(dt);
  }

  bool _needDateSeparator(int index) {
    if (index == 0) return true;
    final prev = _messages[index - 1].timestamp;
    final curr = _messages[index].timestamp;
    return prev.day != curr.day ||
        prev.month != curr.month ||
        prev.year != curr.year;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xffF5F7FA),
      appBar: AppBar(
        backgroundColor: const Color(0xff00BCD4),
        foregroundColor: Colors.white,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.partnerName,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
            ),
            Text(
              ChatService().connected ? 'Đã kết nối' : 'Đang kết nối...',
              style: TextStyle(
                fontSize: 11,
                color: ChatService().connected
                    ? Colors.greenAccent
                    : Colors.white70,
              ),
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          // ── Message list ─────────────────────────────────────
          Expanded(child: _buildMessageList()),

          // ── Input bar ────────────────────────────────────────
          _buildInputBar(),
        ],
      ),
    );
  }

  Widget _buildMessageList() {
    if (_loadingHistory) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_historyError != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, color: Colors.red, size: 40),
            const SizedBox(height: 8),
            Text(_historyError!, style: const TextStyle(color: Colors.red)),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: _loadHistory,
              icon: const Icon(Icons.refresh),
              label: const Text('Thử lại'),
            ),
          ],
        ),
      );
    }

    if (_messages.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.chat_bubble_outline,
                size: 56, color: Colors.grey.shade400),
            const SizedBox(height: 10),
            Text(
              'Hãy bắt đầu cuộc trò chuyện!',
              style: TextStyle(color: Colors.grey.shade500),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      controller: _scroll,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      itemCount: _messages.length,
      itemBuilder: (_, i) {
        final m = _messages[i];
        final isMe = m.senderId == _myId;

        return Column(
          children: [
            // Date separator
            if (_needDateSeparator(i))
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10),
                child: Text(
                  _dateSeparator(m.timestamp),
                  style: TextStyle(
                    fontSize: 11,
                    color: Colors.grey.shade500,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),

            // Bubble
            Align(
              alignment:
              isMe ? Alignment.centerRight : Alignment.centerLeft,
              child: Container(
                constraints: BoxConstraints(
                  maxWidth: MediaQuery.of(context).size.width * 0.72,
                ),
                margin: const EdgeInsets.only(bottom: 4),
                padding: const EdgeInsets.symmetric(
                    horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: isMe
                      ? const Color(0xff00BCD4)
                      : Colors.white,
                  borderRadius: BorderRadius.only(
                    topLeft: const Radius.circular(18),
                    topRight: const Radius.circular(18),
                    bottomLeft: Radius.circular(isMe ? 18 : 4),
                    bottomRight: Radius.circular(isMe ? 4 : 18),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.06),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      m.content,
                      style: TextStyle(
                        color: isMe ? Colors.white : Colors.black87,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      DateFormat('HH:mm').format(m.timestamp),
                      style: TextStyle(
                        fontSize: 10,
                        color: isMe
                            ? Colors.white.withOpacity(0.75)
                            : Colors.grey,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildInputBar() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.06),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: _ctrl,
                focusNode: _focusNode,
                textInputAction: TextInputAction.send,
                onSubmitted: (_) => _send(),
                decoration: InputDecoration(
                  hintText: 'Nhập tin nhắn...',
                  hintStyle: TextStyle(color: Colors.grey.shade400),
                  filled: true,
                  fillColor: const Color(0xffF5F7FA),
                  contentPadding: const EdgeInsets.symmetric(
                      horizontal: 16, vertical: 10),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide.none,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 8),
            GestureDetector(
              onTap: _send,
              child: Container(
                padding: const EdgeInsets.all(10),
                decoration: const BoxDecoration(
                  color: Color(0xff00BCD4),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.send, color: Colors.white, size: 20),
              ),
            ),
          ],
        ),
      ),
    );
  }
}