import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';
import '../model/Lesson.dart';
import '../widget/bottom_navigation_widget.dart';
import '../widget/home_page/header.dart';
import '../widget/home_page/shortcutgrid.dart';
import '../widget/home_page/statgrid.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  double gpa = 0;
  int attendance = 0;
  int earnedCredits = 0;
  int totalCredits = 0;
  int rank = 0;
  List<Lesson> _lessons = [];
  bool _isLoading = true;
  String? _error;
  final String _baseUrl = 'http://10.0.2.2:8080/api';

  static const _weekdayNames = ['', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];
  static const _weekdayEnums = ['', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  @override
  void initState() {
    super.initState();
    _fetchSchedule();
    _fetchStats();
  }

  Future<void> _fetchSchedule() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    final classId = AuthController.classId;
    final token = AuthController.sessionToken;

    if (classId == null || token == null) {
      setState(() {
        _error = 'Vui lòng đăng nhập lại.';
        _isLoading = false;
      });
      return;
    }

    final todayEnum = _weekdayEnums[DateTime.now().weekday];

    try {
      final url = '$_baseUrl/schedules/classes/$classId/by-day?day=$todayEnum';
      print('>>> Calling: $url');

      final response = await http.get(
        Uri.parse(url),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      print('>>> Status: ${response.statusCode}');
      print('>>> Body: ${response.body}');

      if (response.statusCode == 200) {
        final body = jsonDecode(response.body);
        final List<dynamic> data = body['data'] ?? [];

        setState(() {
          _lessons = data.map((json) => Lesson.fromJson(json)).toList();
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = 'Lỗi server: ${response.statusCode}';
          _isLoading = false;
        });
      }
    } catch (e) {
      print('>>> Error: $e');
      setState(() {
        _error = 'Không thể kết nối: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchStats() async {
    final studentId = AuthController.studentId;  // ← đổi từ userId
    final token = AuthController.sessionToken;

    if (studentId == null || token == null) return;

    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/students/$studentId/stats'),
        headers: {'Authorization': 'Bearer $token'},
      );

      print('>>> fetchStats status=${response.statusCode} body=${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body)['data'];
        setState(() {
          gpa           = (data['gpa']               as num? ?? 0).toDouble();
          attendance    = (data['attendancePercent']  as num? ?? 0).toInt();
          earnedCredits = (data['earnedCredits']      as num? ?? 0).toInt();
          totalCredits  = (data['totalCredits']       as num? ?? 0).toInt();
          rank          = (data['classRank']          as num? ?? 0).toInt();
        });
      }
    } catch (e) {
      print('>>> fetchStats error: $e');
    }
  }
  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final dateLabel = '${_weekdayNames[now.weekday]} Ngày ${now.day}/${now.month}/${now.year}';

    return Scaffold(
      backgroundColor: const Color(0xffF5F7FA),
      bottomNavigationBar: const BottomNav(),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Header(),
              const SizedBox(height: 16),
              StatGrid(
                gpa: gpa,
                attendance: attendance,
                earnedCredits: earnedCredits,
                totalCredits: totalCredits,
                rank: rank,
              ),
              const SizedBox(height: 24),
              const Text(
                'Truy cập nhanh',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              const ShortcutGrid(),
              const SizedBox(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Lịch học hôm nay',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        dateLabel,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.grey,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                  IconButton(
                    onPressed: _fetchSchedule,
                    icon: const Icon(Icons.refresh, size: 20),
                    tooltip: 'Tải lại',
                  ),
                ],
              ),
              const SizedBox(height: 12),
              _buildLessonList(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLessonList() {
    if (_isLoading) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 32),
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 24),
          child: Column(
            children: [
              const Icon(Icons.error_outline, color: Colors.red, size: 40),
              const SizedBox(height: 8),
              Text(_error!, style: const TextStyle(color: Colors.red)),
              const SizedBox(height: 12),
              ElevatedButton.icon(
                onPressed: _fetchSchedule,
                icon: const Icon(Icons.refresh, size: 16),
                label: const Text('Thử lại'),
              ),
            ],
          ),
        ),
      );
    }

    if (_lessons.isEmpty) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 32),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.grey.shade200),
        ),
        child: const Column(
          children: [
            Icon(Icons.event_available, size: 48, color: Colors.grey),
            SizedBox(height: 8),
            Text(
              'Hôm nay không có lịch học',
              style: TextStyle(color: Colors.grey, fontSize: 14),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: _lessons.length,
      itemBuilder: (context, index) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 10),
          child: _LessonCard(lesson: _lessons[index]),
        );
      },
    );
  }
}

class _LessonCard extends StatelessWidget {
  final Lesson lesson;
  const _LessonCard({required this.lesson});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [

          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Tiết: ${lesson.periodStart} - ${lesson.periodEnd}',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
              const SizedBox(height: 4),
              Text(
                lesson.time,
                style: const TextStyle(fontSize: 13, color: Colors.grey),
              ),
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: lesson.statusColor.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: lesson.statusColor),
                ),
                child: Text(
                  lesson.statusLabel,
                  style: TextStyle(
                    color: lesson.statusColor,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _infoRow(Icons.meeting_room_outlined, 'Phòng:', lesson.room),
              const SizedBox(height: 6),
              _infoRow(Icons.book_outlined, 'Môn:', lesson.subjectCode),
              const SizedBox(height: 6),
              _infoRow(Icons.group_outlined, 'Lớp:', lesson.classCode),
            ],
          ),
        ],
      ),
    );
  }

  Widget _infoRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 14, color: Colors.grey),
        const SizedBox(width: 4),
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        const SizedBox(width: 4),
        Text(
          value.isNotEmpty ? value : '—',
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}