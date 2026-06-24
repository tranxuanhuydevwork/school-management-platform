import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';
import '../widget/shedule_page/ClassCard.dart';
import '../widget/shedule_page/header.dart';
import '../widget/bottom_navigation_widget.dart';

class SchedulePage extends StatefulWidget {
  const SchedulePage({super.key});

  @override
  State<SchedulePage> createState() => _SchedulePageState();
}

class _SchedulePageState extends State<SchedulePage> {
  final Map<String, List<dynamic>> _cache = {};

  late DateTime _viewingMonday;
  int _selectedDayIndex = 0;

  late final PageController _pageController;
  Timer? _refreshTimer;

  final String _baseUrl = 'http://10.0.2.2:8080/api';

  int    _weeklyCount  = 0;
  int    _nextDayCount = 0;
  String _nextDayLabel = '';

  static const _dayNames = ['T2','T3','T4','T5','T6','T7','CN'];

  static DateTime _toMonday(DateTime d) {
    final c = DateTime(d.year, d.month, d.day);
    return c.subtract(Duration(days: c.weekday - 1));
  }

  static DateTime _stripTime(DateTime d) =>
      DateTime(d.year, d.month, d.day);

  String _dateKey(DateTime d) =>
      '${d.year}-${d.month.toString().padLeft(2,'0')}'
          '-${d.day.toString().padLeft(2,'0')}';

  String _weekKey(DateTime monday) => 'week_${_dateKey(monday)}';

  @override
  void initState() {
    super.initState();
    final now         = DateTime.now();
    _viewingMonday    = _toMonday(now);
    _selectedDayIndex = now.weekday - 1;
    _pageController   = PageController(initialPage: _selectedDayIndex);
    _fetchWeek(_viewingMonday);
    _refreshTimer = Timer.periodic(
      const Duration(minutes: 1),
          (_) { if (mounted) _fetchWeek(_viewingMonday, force: true); },
    );
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    _pageController.dispose();
    super.dispose();
  }

  // ── Fetch toàn tuần ──────────────────────────────────────
  Future<void> _fetchWeek(DateTime monday, {bool force = false}) async {
    final classId = AuthController.classId;
    final token   = AuthController.sessionToken;
    if (classId == null || token == null) return;

    final wk = _weekKey(monday);

    if (!force && _cache.containsKey(wk)) {
      _recalcStat(monday);
      return;
    }

    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/schedules/classes/$classId'
            '/by-week?date=${_dateKey(monday)}'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type':  'application/json',
        },
      );

      if (!mounted) return;
      if (res.statusCode != 200) return;

      final List<dynamic> all =
      List<dynamic>.from(jsonDecode(res.body)['data'] ?? []);

      // Nhóm theo ngày thực tế
      final Map<String, List<dynamic>> grouped = {};
      for (int i = 0; i < 7; i++) {
        grouped[_dateKey(monday.add(Duration(days: i)))] = [];
      }
      for (final item in all) {
        final dow = int.tryParse(item['dayOfWeek']?.toString() ?? '');
        if (dow == null || dow < 1 || dow > 7) continue;
        grouped[_dateKey(monday.add(Duration(days: dow - 1)))]?.add(item);
      }

      setState(() {
        grouped.forEach((k, v) => _cache[k] = v);
        _cache[wk] = []; // đánh dấu tuần đã fetch
      });

      _recalcStat(monday);
    } catch (e) {
      debugPrint('>>> fetchWeek error: $e');
    }
  }

  void _recalcStat(DateTime monday) {
    int weekly = 0, nextCount = 0;
    String nextLabel = '';
    final tomorrow = _stripTime(DateTime.now().add(const Duration(days: 1)));
    for (int i = 0; i < 7; i++) {
      final d   = monday.add(Duration(days: i));
      final len = _cache[_dateKey(d)]?.length ?? 0;
      weekly   += len;
      if (nextLabel.isEmpty && !d.isBefore(tomorrow) && len > 0) {
        nextCount = len;
        nextLabel = _dayNames[i];
      }
    }
    if (!mounted) return;
    setState(() {
      _weeklyCount  = weekly;
      _nextDayCount = nextCount;
      _nextDayLabel = nextLabel;
    });
  }

  // ── Được gọi khi: bấm < >, bấm ngày, reset tuần ─────────
  void _onDaySelected(int dayIndex, DateTime date) {
    final newMonday = _toMonday(_stripTime(date));

    // ✅ Cập nhật state TRƯỚC — PageView.itemBuilder dùng _viewingMonday
    // để tính pageDate, phải đảm bảo _viewingMonday đúng trước khi jump
    setState(() {
      _viewingMonday    = newMonday;
      _selectedDayIndex = dayIndex;
    });

    // ✅ jumpToPage sau khi setState rebuild xong — tránh itemBuilder
    // render với _viewingMonday cũ trong khi page đã nhảy sang index mới
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _pageController.jumpToPage(dayIndex);
    });

    if (!_cache.containsKey(_weekKey(newMonday))) {
      _fetchWeek(newMonday);
    } else {
      _recalcStat(newMonday);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xffF6F6F6),
      body: Column(
        children: [
          Schedule_Header(
            selectedIndex: _selectedDayIndex,
            weeklyCount:   _weeklyCount,
            nextDayCount:  _nextDayCount,
            nextDayLabel:  _nextDayLabel,
            onChanged:     _onDaySelected,
          ),
          Expanded(
            child: PageView.builder(
              controller: _pageController,
              onPageChanged: (i) =>
                  setState(() => _selectedDayIndex = i),
              itemCount: 7,
              itemBuilder: (context, pageIndex) {
                // ✅ Snapshot _viewingMonday tại thời điểm build để tránh
                // closure capture giá trị cũ khi tuần thay đổi mid-render
                final currentMonday = _viewingMonday;
                final pageDate      = currentMonday.add(Duration(days: pageIndex));
                final key           = _dateKey(pageDate);
                final schedules     = _cache[key];

                if (schedules == null) {
                  return const Center(
                      child: CircularProgressIndicator(
                          color: Color(0xFFF37021)));
                }

                if (schedules.isEmpty) return _buildEmpty(pageDate);

                return ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: schedules.length,
                  itemBuilder: (_, i) {
                    final item = schedules[i];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: ClassCard(
                        code:    item['subjectCode']  ?? '',
                        title:   item['subjectName']  ?? '',
                        time:    item['time']          ?? '',
                        room:    item['room']          ?? '',
                        teacher: item['teacherName']  ?? '',
                        // ✅ Tính status phía client dựa vào ngày thực tế
                        // Tránh backend trả ONGOING/DONE sai cho ngày khác hôm nay
                        status: _computeStatus(
                          item['time'] as String? ?? '',
                          pageDate,
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ),
          const BottomNav(currentIndex: 1),
        ],
      ),
    );
  }

  /// Tính status phía client — chính xác cho mọi ngày, không chỉ hôm nay
  String _computeStatus(String timeStr, DateTime date) {
    final today = _stripTime(DateTime.now());
    final day   = _stripTime(date);

    if (day.isBefore(today)) return 'DONE';
    if (day.isAfter(today))  return 'UPCOMING';

    // Hôm nay — phân tích chuỗi "HH:mm - HH:mm"
    final parts = timeStr.split(' - ');
    if (parts.length != 2) return 'UPCOMING';

    try {
      final startParts = parts[0].trim().split(':');
      final endParts   = parts[1].trim().split(':');
      final now        = TimeOfDay.now();
      final start      = TimeOfDay(
        hour:   int.parse(startParts[0]),
        minute: int.parse(startParts[1]),
      );
      final end = TimeOfDay(
        hour:   int.parse(endParts[0]),
        minute: int.parse(endParts[1]),
      );

      final nowMin   = now.hour   * 60 + now.minute;
      final startMin = start.hour * 60 + start.minute;
      final endMin   = end.hour   * 60 + end.minute;

      if (nowMin < startMin) return 'UPCOMING';
      if (nowMin > endMin)   return 'DONE';
      return 'ONGOING';
    } catch (_) {
      return 'UPCOMING';
    }
  }

  Widget _buildEmpty(DateTime date) {
    final now    = _stripTime(DateTime.now());
    final isPast = date.isBefore(now);
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(isPast ? Icons.history : Icons.event_note_outlined,
              size: 56, color: Colors.grey.shade300),
          const SizedBox(height: 12),
          Text(
            isPast ? 'Không có lịch học ngày này'
                : 'Chưa có lịch học cho ngày này',
            style: TextStyle(color: Colors.grey.shade500, fontSize: 14),
          ),
          const SizedBox(height: 4),
          Text('${date.day}/${date.month}/${date.year}',
              style: TextStyle(color: Colors.grey.shade400, fontSize: 12)),
        ],
      ),
    );
  }
}