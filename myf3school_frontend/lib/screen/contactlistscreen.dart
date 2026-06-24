import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:intl/intl.dart';

import '../controller/AuthController.dart';
import '../routes/router_names.dart';

// ─── Model ────────────────────────────────────────────────────
class ChatContact {
  final int userId;
  final String name;
  final String role;
  final String? avatarUrl;
  final String? lastMessage;
  final DateTime? lastTime;
  final int unread;

  ChatContact({
    required this.userId,
    required this.name,
    required this.role,
    this.avatarUrl,
    this.lastMessage,
    this.lastTime,
    required this.unread,
  });

  factory ChatContact.fromJson(Map<String, dynamic> j) => ChatContact(
    userId: j['userId'] ?? 0,
    name: j['name'] ?? '',
    role: j['role'] ?? 'STUDENT',
    avatarUrl: j['avatarUrl'],
    lastMessage: j['lastMessage'],
    lastTime: j['lastTime'] != null
        ? DateTime.parse(j['lastTime']).toLocal()
        : null,
    unread: j['unread'] ?? 0,
  );
}

// ─── Screen ───────────────────────────────────────────────────
class ContactListScreen extends StatefulWidget {
  const ContactListScreen({super.key});

  @override
  State<ContactListScreen> createState() => _ContactListScreenState();
}

class _ContactListScreenState extends State<ContactListScreen> {
  final String _baseUrl = 'http://10.0.2.2:8080';

  List<ChatContact> _contacts = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _fetchContacts();
  }

  Future<void> _fetchContacts() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    final userId = AuthController.userId;
    final token = AuthController.sessionToken;

    if (userId == null || token == null) {
      setState(() {
        _error = 'Vui lòng đăng nhập lại.';
        _loading = false;
      });
      return;
    }

    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/api/chat/contacts/$userId'),
        headers: {'Authorization': 'Bearer $token'},
      );

      if (res.statusCode == 200) {
        final List<dynamic> data = jsonDecode(res.body)['data'] ?? [];
        setState(() {
          _contacts = data.map((e) => ChatContact.fromJson(e)).toList();
          _loading = false;
        });
      } else {
        setState(() {
          _error = 'Lỗi server: ${res.statusCode}';
          _loading = false;
        });
      }
    } catch (e) {
      setState(() {
        _error = 'Không thể kết nối: $e';
        _loading = false;
      });
    }
  }

  void _openChat(ChatContact contact) {
    Navigator.pushNamed(
      context,
      RouteNames.chat,
      arguments: {
        'partnerId': contact.userId,
        'partnerName': contact.name,
      },
    );
  }

  String _formatTime(DateTime? dt) {
    if (dt == null) return '';
    final now = DateTime.now();
    if (now.difference(dt).inDays == 0) {
      return DateFormat('HH:mm').format(dt);
    } else if (now.difference(dt).inDays < 7) {
      return DateFormat('EEE', 'vi').format(dt);
    }
    return DateFormat('dd/MM').format(dt);
  }

  String _roleLabel(String role) {
    switch (role.toUpperCase()) {
      case 'TEACHER':
        return 'Giáo viên';
      case 'ADMIN':
        return 'Quản trị';
      default:
        return 'Sinh viên';
    }
  }

  Color _roleColor(String role) {
    switch (role.toUpperCase()) {
      case 'TEACHER':
        return Colors.orange;
      case 'ADMIN':
        return Colors.purple;
      default:
        return const Color(0xff00BCD4);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xffF5F7FA),
      appBar: AppBar(
        backgroundColor: const Color(0xff00BCD4),
        foregroundColor: Colors.white,
        title: const Text(
          'Nhắn tin',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _fetchContacts,
          ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, color: Colors.red, size: 48),
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: Colors.red)),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _fetchContacts,
              icon: const Icon(Icons.refresh),
              label: const Text('Thử lại'),
            ),
          ],
        ),
      );
    }

    if (_contacts.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.chat_bubble_outline,
                size: 64, color: Colors.grey.shade400),
            const SizedBox(height: 12),
            Text(
              'Chưa có cuộc trò chuyện nào',
              style: TextStyle(color: Colors.grey.shade500, fontSize: 15),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _fetchContacts,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(vertical: 8),
        itemCount: _contacts.length,
        separatorBuilder: (_, __) =>
            Divider(height: 1, indent: 72, color: Colors.grey.shade200),
        itemBuilder: (context, i) => _ContactTile(
          contact: _contacts[i],
          onTap: () => _openChat(_contacts[i]),
          formatTime: _formatTime,
          roleLabel: _roleLabel,
          roleColor: _roleColor,
        ),
      ),
    );
  }
}

// ─── Tile ─────────────────────────────────────────────────────
class _ContactTile extends StatelessWidget {
  final ChatContact contact;
  final VoidCallback onTap;
  final String Function(DateTime?) formatTime;
  final String Function(String) roleLabel;
  final Color Function(String) roleColor;

  const _ContactTile({
    required this.contact,
    required this.onTap,
    required this.formatTime,
    required this.roleLabel,
    required this.roleColor,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        color: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            // Avatar
            Stack(
              children: [
                CircleAvatar(
                  radius: 26,
                  backgroundColor: roleColor(contact.role).withOpacity(0.15),
                  backgroundImage: contact.avatarUrl != null
                      ? NetworkImage(contact.avatarUrl!)
                      : null,
                  child: contact.avatarUrl == null
                      ? Text(
                    contact.name.isNotEmpty
                        ? contact.name[0].toUpperCase()
                        : '?',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: roleColor(contact.role),
                    ),
                  )
                      : null,
                ),
                if (contact.unread > 0)
                  Positioned(
                    right: 0,
                    top: 0,
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(
                        color: Colors.red,
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        contact.unread > 99 ? '99+' : '${contact.unread}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(width: 12),
            // Info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          contact.name,
                          style: TextStyle(
                            fontWeight: contact.unread > 0
                                ? FontWeight.bold
                                : FontWeight.w600,
                            fontSize: 14,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Text(
                        formatTime(contact.lastTime),
                        style: TextStyle(
                          fontSize: 11,
                          color: contact.unread > 0
                              ? const Color(0xff00BCD4)
                              : Colors.grey,
                          fontWeight: contact.unread > 0
                              ? FontWeight.bold
                              : FontWeight.normal,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 2),
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 1),
                        decoration: BoxDecoration(
                          color: roleColor(contact.role).withOpacity(0.1),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          roleLabel(contact.role),
                          style: TextStyle(
                            fontSize: 10,
                            color: roleColor(contact.role),
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          contact.lastMessage ?? 'Bắt đầu trò chuyện',
                          style: TextStyle(
                            fontSize: 12,
                            color: contact.unread > 0
                                ? Colors.black87
                                : Colors.grey,
                            fontWeight: contact.unread > 0
                                ? FontWeight.w500
                                : FontWeight.normal,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}