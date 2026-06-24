import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:myf3school/widget/bottom_navigation_widget.dart';

import '../controller/AuthController.dart';
import '../model/AttendanceRecord.dart';
import '../model/SemesterInfo.dart';
import '../widget/attendance_page/attendance_dart.dart';
import '../widget/attendance_page/attendance_header.dart';

class AttendanceScreen extends StatefulWidget {
  const AttendanceScreen({super.key});

  @override
  State<AttendanceScreen> createState() => _AttendanceScreenState();
}

class _AttendanceScreenState extends State<AttendanceScreen> {
  static const _base = 'http://10.0.2.2:8080/api';

  List<AttendanceRecord> _records   = [];
  List<SemesterInfo>     _semesters = [];
  SemesterInfo?          _selected;
  bool    _loading         = true;
  bool    _loadingSemesters = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _fetchSemesters();
  }

  // ── Bước 1: load danh sách học kỳ ─────────────────────────
  Future<void> _fetchSemesters() async {
    setState(() { _loadingSemesters = true; });
    try {
      final res = await http.get(
        Uri.parse('$_base/semesters'),
        headers: AuthController.authHeaders,
      ).timeout(const Duration(seconds: 10));

      if (res.statusCode == 200) {
        final List<dynamic> data =
            jsonDecode(utf8.decode(res.bodyBytes))['data'] ?? [];
        final list = data
            .map((s) => SemesterInfo.fromJson(s as Map<String, dynamic>))
            .toList();

        setState(() {
          _semesters        = list;
          // Mặc định chọn học kỳ hiện tại
          _selected         = list.firstWhere(
                (s) => s.isCurrent,
            orElse: () => list.first,
          );
          _loadingSemesters = false;
        });

        // Bước 2: load điểm danh với semester đã chọn
        await _fetchAttendance();
      } else {
        setState(() { _loadingSemesters = false; _error = 'Lỗi tải học kỳ'; });
      }
    } catch (e) {
      setState(() { _loadingSemesters = false; _error = 'Lỗi kết nối: $e'; });
    }
  }

  // ── Bước 2: load điểm danh ────────────────────────────────
  Future<void> _fetchAttendance() async {
    if (_selected == null) return;
    setState(() { _loading = true; _error = null; });

    final studentId = AuthController.studentId;
    if (studentId == null) {
      setState(() {
        _error   = 'Không tìm thấy thông tin học sinh';
        _loading = false;
      });
      return;
    }

    try {
      final res = await http.get(
        Uri.parse('$_base/attendance/students/$studentId'
            '?semesterId=${_selected!.id}'),
        headers: AuthController.authHeaders,
      ).timeout(const Duration(seconds: 15));

      if (res.statusCode == 200) {
        final body = jsonDecode(utf8.decode(res.bodyBytes))
        as Map<String, dynamic>;
        final subjects =
        (body['data']['subjects'] as List<dynamic>? ?? [])
            .map((s) => AttendanceRecord.fromJson(
            s as Map<String, dynamic>))
            .toList();
        setState(() { _records = subjects; _loading = false; });
      } else if (res.statusCode == 401) {
        setState(() { _error = 'Phiên đăng nhập hết hạn'; _loading = false; });
      } else {
        setState(() {
          _error   = 'Lỗi server: ${res.statusCode}';
          _loading = false;
        });
      }
    } catch (e) {
      setState(() { _error = 'Lỗi kết nối: $e'; _loading = false; });
    }
  }

  // ── Build ──────────────────────────────────────────────────
  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: Colors.grey[100],
    body: Scaffold(
      bottomNavigationBar: const BottomNav(),
          body:
      SafeArea(
        child: Column(children: [
          const AttendanceHeader(),
          // ── Dropdown chọn học kỳ ──────────────────────────
          if (!_loadingSemesters && _semesters.isNotEmpty)
            _SemesterDropdown(
              semesters: _semesters,
              selected:  _selected,
              onChanged: (s) {
                setState(() => _selected = s);
                _fetchAttendance();
              },
            ),
          // ── Nội dung ──────────────────────────────────────
          Expanded(
            child: _loadingSemesters
                ? const Center(child: CircularProgressIndicator(
                color: Color(0xFF1EBBD7)))
                : _loading
                ? const Center(child: CircularProgressIndicator(
                color: Color(0xFF1EBBD7)))
                : _error != null
                ? _ErrorView(error: _error!, onRetry: _fetchAttendance)
                : _ListView(records: _records, onRefresh: _fetchAttendance),
          ),
        
        ]),
      ),
    ),

  );
}

// ── Dropdown widget ───────────────────────────────────────────
class _SemesterDropdown extends StatelessWidget {
  final List<SemesterInfo> semesters;
  final SemesterInfo?      selected;
  final ValueChanged<SemesterInfo?> onChanged;

  const _SemesterDropdown({
    required this.semesters,
    required this.selected,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF1EBBD7).withOpacity(0.4)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<SemesterInfo>(
          value: selected,
          isExpanded: true,
          icon: const Icon(Icons.keyboard_arrow_down,
              color: Color(0xFF1EBBD7)),
          style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Color(0xFF2D3142)),
          items: semesters.map((s) => DropdownMenuItem(
            value: s,
            child: Row(children: [
              if (s.isCurrent) ...[
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1EBBD7).withOpacity(0.1),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: const Text('Hiện tại',
                      style: TextStyle(
                          fontSize: 10,
                          color: Color(0xFF1EBBD7),
                          fontWeight: FontWeight.bold)),
                ),
                const SizedBox(width: 8),
              ],
              Text(s.name),
            ]),
          )).toList(),
          onChanged: onChanged,
        ),
      ),
    );
  }
}

// ── Error + List views (giữ nguyên) ──────────────────────────
class _ErrorView extends StatelessWidget {
  final String       error;
  final VoidCallback onRetry;
  const _ErrorView({required this.error, required this.onRetry});

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        const Icon(Icons.wifi_off, color: Color(0xFF1EBBD7), size: 48),
        const SizedBox(height: 12),
        Text(error,
            textAlign: TextAlign.center,
            style: const TextStyle(color: Colors.grey)),
        const SizedBox(height: 16),
        ElevatedButton.icon(
          onPressed: onRetry,
          icon: const Icon(Icons.refresh),
          label: const Text('Thử lại'),
          style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF1EBBD7),
              foregroundColor: Colors.white),
        ),
      ]),
    ),
  );
}

class _ListView extends StatelessWidget {
  final List<AttendanceRecord>   records;
  final Future<void> Function()  onRefresh;
  const _ListView({required this.records, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    if (records.isEmpty) {
      return const Center(
        child: Text('Chưa có dữ liệu điểm danh',
            style: TextStyle(color: Colors.grey)),
      );
    }
    return RefreshIndicator(
      color: const Color(0xFF1EBBD7),
      onRefresh: onRefresh,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: records.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (_, i) => AttendanceCard(record: records[i]),
      ),
    );
  }
}