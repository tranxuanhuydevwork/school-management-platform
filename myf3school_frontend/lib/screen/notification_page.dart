import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';
import '../widget/bottom_navigation_widget.dart';

class NotificationPage extends StatefulWidget {
  const NotificationPage({super.key});

  @override
  State<NotificationPage> createState() => _NotificationPageState();
}

class _NotificationPageState extends State<NotificationPage> {
  List<dynamic> _notifications = [];
  bool _isLoading = true;
  String? _error;
  int _unreadCount = 0;

  final String _baseUrl = 'http://10.0.2.2:8080/api';

  @override
  void initState() {
    super.initState();
    _fetchNotifications();
    _fetchUnreadCount();
  }

  Future<void> _fetchNotifications() async {
    final userId = AuthController.userId;
    final token = AuthController.sessionToken;

    if (userId == null || token == null) {
      setState(() {
        _error = 'Vui lòng đăng nhập lại.';
        _isLoading = false;
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/notifications/users/$userId?page=0&size=30'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (!mounted) return;

      if (res.statusCode == 200) {
        final body = jsonDecode(res.body);
        setState(() {
          _notifications = body['data']['content'] ?? [];
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = 'Lỗi server: ${res.statusCode}';
          _isLoading = false;
        });
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = 'Không thể kết nối: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchUnreadCount() async {
    final userId = AuthController.userId;
    final token = AuthController.sessionToken;
    if (userId == null || token == null) return;

    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/notifications/users/$userId/unread-count'),
        headers: {'Authorization': 'Bearer $token'},
      );
      if (!mounted) return;
      if (res.statusCode == 200) {
        final body = jsonDecode(res.body);
        setState(() {
          _unreadCount = (body['data']['count'] ?? 0) as int;
        });
      }
    } catch (_) {}
  }

  Future<void> _markRead(int index, dynamic item) async {
    final token = AuthController.sessionToken;
    if (token == null || item['isRead'] == true) return;

    final id = item['id'];
    try {
      final res = await http.put(
        Uri.parse('$_baseUrl/notifications/$id/read'),
        headers: {'Authorization': 'Bearer $token'},
      );
      if (!mounted) return;
      if (res.statusCode == 200) {
        setState(() {
          _notifications[index]['isRead'] = true;
          if (_unreadCount > 0) _unreadCount--;
        });
      }
    } catch (_) {}
  }

  Future<void> _markAllRead() async {
    final userId = AuthController.userId;
    final token = AuthController.sessionToken;
    if (userId == null || token == null) return;

    try {
      final res = await http.put(
        Uri.parse('$_baseUrl/notifications/users/$userId/read-all'),
        headers: {'Authorization': 'Bearer $token'},
      );
      if (!mounted) return;
      if (res.statusCode == 200) {
        setState(() {
          for (var n in _notifications) n['isRead'] = true;
          _unreadCount = 0;
        });
      }
    } catch (_) {}
  }

  IconData _iconFor(String? refType) {
    switch (refType) {
      case 'SCHEDULE': return Icons.calendar_today;
      case 'SCORE':    return Icons.description;
      case 'ATTENDANCE': return Icons.warning_amber_rounded;
      case 'ENROLLMENT': return Icons.check_circle;
      default:         return Icons.notifications;
    }
  }

  Color _iconBgFor(String? refType) {
    switch (refType) {
      case 'SCHEDULE':   return const Color(0xFFFFF3E0);
      case 'SCORE':      return const Color(0xFFE8F5E9);
      case 'ATTENDANCE': return const Color(0xFFFFF8E1);
      case 'ENROLLMENT': return const Color(0xFFE0F2F1);
      default:           return const Color(0xFFF0F0F0);
    }
  }

  String _timeAgo(String? createdAt) {
    if (createdAt == null) return '';
    try {
      final dt = DateTime.parse(createdAt);
      final diff = DateTime.now().difference(dt);
      if (diff.inMinutes < 1)  return 'Vừa xong';
      if (diff.inMinutes < 60) return '${diff.inMinutes} phút trước';
      if (diff.inHours < 24)   return '${diff.inHours} giờ trước';
      if (diff.inDays < 30)    return '${diff.inDays} ngày trước';
      return '${(diff.inDays / 30).floor()} tháng trước';
    } catch (_) {
      return '';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F7FB),
      appBar: AppBar(
        elevation: 0,
        backgroundColor: Colors.white,
        title: Row(
          children: [
            const Text(
              'Thông báo',
              style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold),
            ),
            if (_unreadCount > 0) ...[
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.teal,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '$_unreadCount',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ],
        ),
        actions: [
          if (_unreadCount > 0)
            TextButton.icon(
              onPressed: _markAllRead,
              icon: const Icon(Icons.check, color: Colors.teal),
              label: const Text('Đọc tất cả', style: TextStyle(color: Colors.teal)),
            ),
        ],
      ),
      body: _buildBody(),
      bottomNavigationBar: const BottomNav(currentIndex: 3),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
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
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: _fetchNotifications,
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text('Thử lại'),
            ),
          ],
        ),
      );
    }

    if (_notifications.isEmpty) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.notifications_none, size: 64, color: Colors.grey),
            SizedBox(height: 12),
            Text(
              'Không có thông báo nào',
              style: TextStyle(color: Colors.grey, fontSize: 15),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () async {
        await _fetchNotifications();
        await _fetchUnreadCount();
      },
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _notifications.length,
        itemBuilder: (context, index) {
          final item = _notifications[index];
          final isRead = item['isRead'] == true;
          return GestureDetector(
            onTap: () => _markRead(index, item),
            child: _NotificationItem(
              icon: _iconFor(item['refType']),
              iconBg: _iconBgFor(item['refType']),
              title: item['title'] ?? '',
              content: item['message'] ?? '',
              time: _timeAgo(item['createdAt']),
              unread: !isRead,
            ),
          );
        },
      ),
    );
  }
}

class _NotificationItem extends StatelessWidget {
  final IconData icon;
  final Color iconBg;
  final String title;
  final String content;
  final String time;
  final bool unread;

  const _NotificationItem({
    required this.icon,
    required this.iconBg,
    required this.title,
    required this.content,
    required this.time,
    required this.unread,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: unread ? Colors.white : Colors.grey.shade50,
        borderRadius: BorderRadius.circular(16),
        border: unread
            ? Border.all(color: Colors.teal.withOpacity(0.3))
            : Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(unread ? 0.06 : 0.03),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: iconBg,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: Colors.orange, size: 22),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        title,
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: unread ? FontWeight.bold : FontWeight.w500,
                          color: unread ? Colors.black : Colors.grey.shade700,
                        ),
                      ),
                    ),
                    if (unread)
                      Container(
                        width: 8,
                        height: 8,
                        decoration: const BoxDecoration(
                          color: Colors.teal,
                          shape: BoxShape.circle,
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  content,
                  style: TextStyle(
                    color: Colors.grey.shade700,
                    height: 1.4,
                    fontSize: 13,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  time,
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade500),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}