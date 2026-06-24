import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';
import '../widget/bottom_navigation_widget.dart';
import '../widget/score_page/header.dart';
import '../widget/score_page/subject_card.dart';

// ── Models ───────────────────────────────────────────────────

class ScoreComponent {
  final String name;
  final String type;
  final double weight;
  final double maxScore;
  final double? score;

  ScoreComponent({
    required this.name,
    required this.type,
    required this.weight,
    required this.maxScore,
    this.score,
  });

  factory ScoreComponent.fromJson(Map<String, dynamic> json) {
    double? score;
    final raw = json['score'];
    if (raw is num) score = raw.toDouble();
    return ScoreComponent(
      name:     json['name']               ?? '',
      type:     json['type']               ?? '',
      weight:   (json['weight']   as num?)?.toDouble() ?? 0,
      maxScore: (json['maxScore'] as num?)?.toDouble() ?? 10,
      score:    score,
    );
  }
}

class SubjectScore {
  final int sectionId;
  final String subjectCode;
  final String subjectName;
  final String teacherName;
  final List<ScoreComponent> components;
  final double? average;
  final String letterGrade;
  final bool passed;

  SubjectScore({
    required this.sectionId,
    required this.subjectCode,
    required this.subjectName,
    required this.teacherName,
    required this.components,
    this.average,
    required this.letterGrade,
    required this.passed,
  });

  factory SubjectScore.fromJson(Map<String, dynamic> json) {
    double? average;
    final raw = json['average'];
    if (raw is num) average = raw.toDouble();
    return SubjectScore(
      sectionId:   json['sectionId']   ?? 0,
      subjectCode: json['subjectCode'] ?? '',
      subjectName: json['subjectName'] ?? '',
      teacherName: json['teacherName'] ?? '',
      letterGrade: json['letterGrade'] ?? '-',
      passed:      json['passed']      ?? false,
      average:     average,
      components: (json['components'] as List<dynamic>? ?? [])
          .map((c) => ScoreComponent.fromJson(c as Map<String, dynamic>))
          .toList(),
    );
  }
}

// ── Screen ───────────────────────────────────────────────────

class ScoreScreen extends StatefulWidget {
  const ScoreScreen({super.key});

  @override
  State<ScoreScreen> createState() => _ScoreScreenState();
}

class _ScoreScreenState extends State<ScoreScreen> {
  List<SubjectScore> _subjects = [];
  bool _isLoading = true;
  String? _error;

  final String _baseUrl = 'http://10.0.2.2:8080/api';

  @override
  void initState() {
    super.initState();
    _fetchScores();
  }

  Future<void> _fetchScores() async {
    setState(() { _isLoading = true; _error = null; });

    final token  = AuthController.sessionToken;
    final studentId = AuthController.studentId;

    // Debug
    print('[SCORE] userId=$studentId token=${token?.substring(0, 20)}');

    if (studentId == null || token == null) {
      setState(() {
        _error = 'Vui lòng đăng nhập lại.';
        _isLoading = false;
      });
      return;
    }

    try {
      final sectionsRes = await http.get(
        Uri.parse('$_baseUrl/grades/students/$studentId/sections'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      print('[SCORE] sections status=${sectionsRes.statusCode}');
      print('[SCORE] sections body=${sectionsRes.body}');

      if (!mounted) return;
      if (sectionsRes.statusCode != 200) {
        setState(() {
          _error = 'Lỗi tải môn học: ${sectionsRes.statusCode}';
          _isLoading = false;
        });
        return;
      }

      final List<dynamic> sections =
          jsonDecode(sectionsRes.body)['data'] ?? [];

      if (sections.isEmpty) {
        setState(() { _subjects = []; _isLoading = false; });
        return;
      }

      final futures = sections.map((s) async {
        final sectionId = (s['sectionId'] as num).toInt(); // ← cast đúng
        final res = await http.get(
          Uri.parse('$_baseUrl/grades/students/$studentId/sections/$sectionId'),
          headers: {
            'Authorization': 'Bearer $token',
            'Content-Type': 'application/json',
          },
        );
        print('[SCORE] grade[$sectionId] status=${res.statusCode}');
        print('[SCORE] grade[$sectionId] body=${res.body}');
        if (res.statusCode == 200) {
          return SubjectScore.fromJson(
              jsonDecode(res.body)['data'] as Map<String, dynamic>);
        }
        return null;
      });

      final results = await Future.wait(futures);
      if (!mounted) return;
      setState(() {
        _subjects  = results.whereType<SubjectScore>().toList();
        _isLoading = false;
      });

    } catch (e) {
      print('[SCORE] error: $e');
      if (!mounted) return;
      setState(() {
        _error     = 'Không thể kết nối: $e';
        _isLoading = false;
      });
    }
  }
  double get _overallGpa {
    final graded = _subjects.where((s) => s.average != null).toList();
    if (graded.isEmpty) return 0;
    return graded.map((s) => s.average!).reduce((a, b) => a + b) / graded.length;
  }

  int get _passedCount => _subjects.where((s) => s.passed).length;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      body: SafeArea(
        child: Column(
          children: [
            ScoreHeader(
              gpa:           _overallGpa.toStringAsFixed(2),
              totalSubjects: _subjects.length.toString(),
              passedCount:   _passedCount.toString(),
            ),
            Expanded(child: _buildBody()),
          ],
        ),
      ),
      bottomNavigationBar: const BottomNav(currentIndex: 2),
    );
  }

  Widget _buildBody() {
    if (_isLoading) return const Center(child: CircularProgressIndicator());

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, color: Colors.red, size: 48),
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: Colors.red),
                textAlign: TextAlign.center),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: _fetchScores,
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text('Thử lại'),
            ),
          ],
        ),
      );
    }

    if (_subjects.isEmpty) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.assignment_outlined, size: 64, color: Colors.grey),
            SizedBox(height: 12),
            Text('Chưa có điểm số',
                style: TextStyle(color: Colors.grey, fontSize: 15)),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _fetchScores,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _subjects.length,
        itemBuilder: (context, index) => Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: SubjectCard(subject: _subjects[index]),
        ),
      ),
    );
  }
}