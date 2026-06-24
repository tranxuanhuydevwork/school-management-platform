import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../../controller/AuthController.dart';
import '../../model/RequestForm.dart';
import '../../widget/bottom_navigation_widget.dart';
import '../../widget/application_page/CreateRequestSheet.dart';
import '../../widget/application_page/RequestCard.dart';
import '../../widget/application_page/RequestDetailScreen.dart';
import '../../widget/application_page/RequestStatusBar.dart';

class ApplicationScreen extends StatefulWidget {
  const ApplicationScreen({super.key});

  @override
  State<ApplicationScreen> createState() => _ApplicationScreenState();
}

class _ApplicationScreenState extends State<ApplicationScreen> {
  final String _baseUrl = 'http://10.0.2.2:8080/api';

  List<RequestForm> _all   = [];
  List<RequestForm> _shown = [];
  RequestStatus?    _filter;
  bool              _loading = true;
  String?           _error;

  final _searchCtrl = TextEditingController();

  Map<RequestStatus, int> get _counts => {
    for (final s in RequestStatus.values)
      s: _all.where((r) => r.status == s).length,
  };

  @override
  void initState() {
    super.initState();
    _fetchRequests();
  }

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  Future<void> _fetchRequests() async {
    setState(() { _loading = true; _error = null; });

    final studentId = AuthController.studentId;
    final token     = AuthController.sessionToken;

    if (studentId == null || token == null) {
      setState(() { _error = 'Vui lòng đăng nhập lại.'; _loading = false; });
      return;
    }

    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/requests/students/$studentId'),
        headers: {'Authorization': 'Bearer $token'},
      );

      print('[APP] status=${res.statusCode} body=${res.body}');

      if (res.statusCode == 200) {
        final List<dynamic> data = jsonDecode(res.body)['data'] ?? [];
        _all = data
            .map((j) => RequestForm.fromJson(j as Map<String, dynamic>))
            .toList()
          ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
        setState(() { _loading = false; });
        _applyFilter();
      } else {
        setState(() {
          _error = 'Lỗi server: ${res.statusCode}';
          _loading = false;
        });
      }
    } catch (e) {
      print('[APP] error=$e');
      setState(() { _error = 'Không thể kết nối: $e'; _loading = false; });
    }
  }

  void _applyFilter() {
    final q = _searchCtrl.text.trim().toLowerCase();
    setState(() {
      _shown = _all.where((r) {
        final matchStatus = _filter == null || r.status == _filter;
        final matchQuery  = q.isEmpty ||
            r.title.toLowerCase().contains(q) ||
            r.description.toLowerCase().contains(q) ||
            r.type.label.toLowerCase().contains(q);
        return matchStatus && matchQuery;
      }).toList();
    });
  }

  Future<bool> _createRequest(
      RequestType type, String title, String description) async {
    final studentId = AuthController.studentId;
    final token     = AuthController.sessionToken;
    if (studentId == null || token == null) return false;

    // Chặn học sinh tạo LEAVE_REQUEST
    if (type == RequestType.leaveRequest && !AuthController.isParent) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Chỉ phụ huynh mới được gửi đơn xin nghỉ học'),
            backgroundColor: Colors.red,
          ),
        );
      }
      return false;
    }

    try {
      final res = await http.post(
        Uri.parse('$_baseUrl/requests'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type':  'application/json',
        },
        body: jsonEncode({
          'studentId':         studentId,
          'requestType':       type.apiValue,
          'title':             title,
          'description':       description,
          'submittedByParent': AuthController.isParent,
        }),
      );

      print('[APP] create status=${res.statusCode} body=${res.body}');

      if (res.statusCode == 200 || res.statusCode == 201) {
        await _fetchRequests();
        return true;
      }
    } catch (e) {
      print('[APP] create error=$e');
    }
    return false;
  }

  void _openCreateSheet() async {
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => CreateRequestSheet(
        onCreate: _createRequest,
        isParent: AuthController.isParent, // ← truyền role
      ),
    );
    if (ok == true && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Đã gửi đơn thành công!'),
          backgroundColor: Color(0xFF10B981),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xffF5F7FA),
      bottomNavigationBar: const BottomNav(),
      floatingActionButton: FloatingActionButton(
        onPressed: _openCreateSheet,
        backgroundColor: Colors.orange,
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: SafeArea(
        child: Column(
          children: [
            Container(
              color: Colors.orange,
              padding: const EdgeInsets.fromLTRB(4, 12, 16, 0),
              child: Row(
                children: [
                  const BackButton(color: Colors.white),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Đăng ký đơn',
                            style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: Colors.white)),
                        Text('Quản lý đơn từ & yêu cầu',
                            style: TextStyle(
                                fontSize: 12, color: Colors.white70)),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.25),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      '${_all.length} đơn',
                      style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 13),
                    ),
                  ),
                ],
              ),
            ),

            RequestStatusBar(
              counts:   _counts,
              selected: _filter,
              onTap: (s) {
                _filter = s;
                _applyFilter();
              },
            ),

            Container(
              margin: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(14),
                boxShadow: [
                  BoxShadow(
                      color: Colors.black.withOpacity(0.06),
                      blurRadius: 8,
                      offset: const Offset(0, 2))
                ],
              ),
              child: TextField(
                controller: _searchCtrl,
                onChanged: (_) => _applyFilter(),
                decoration: InputDecoration(
                  hintText: 'Tìm kiếm đơn...',
                  hintStyle:
                  TextStyle(color: Colors.grey.shade400, fontSize: 14),
                  prefixIcon: Icon(Icons.search,
                      color: Colors.grey.shade400, size: 20),
                  suffixIcon: _searchCtrl.text.isNotEmpty
                      ? IconButton(
                      icon: Icon(Icons.close,
                          color: Colors.grey.shade400, size: 18),
                      onPressed: () {
                        _searchCtrl.clear();
                        _applyFilter();
                      })
                      : null,
                  border: InputBorder.none,
                  contentPadding:
                  const EdgeInsets.symmetric(vertical: 14),
                ),
              ),
            ),

            Expanded(child: _buildBody()),
          ],
        ),
      ),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(
          child: CircularProgressIndicator(color: Colors.orange));
    }

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: Colors.red)),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: _fetchRequests,
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text('Thử lại'),
              style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.orange,
                  foregroundColor: Colors.white),
            ),
          ],
        ),
      );
    }

    if (_shown.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.inbox_outlined,
                size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 12),
            Text(
              _filter != null || _searchCtrl.text.isNotEmpty
                  ? 'Không tìm thấy đơn phù hợp'
                  : 'Chưa có đơn nào\nBấm + để tạo đơn mới',
              textAlign: TextAlign.center,
              style:
              TextStyle(color: Colors.grey.shade500, fontSize: 14),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      color: Colors.orange,
      onRefresh: _fetchRequests,
      child: ListView.builder(
        padding: const EdgeInsets.only(bottom: 80),
        itemCount: _shown.length,
        itemBuilder: (_, i) => RequestCard(
          request: _shown[i],
          onTap: () => Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => RequestDetailScreen(request: _shown[i]),
            ),
          ),
        ),
      ),
    );
  }
}