// lib/widget/attendance_page/attendance_progress.dart
import 'package:flutter/material.dart';
import 'circular_progress_painter.dart';

class AttendanceProgress extends StatelessWidget {
  final int    attended;
  final int    total;
  final int    present;
  final int    late;
  final int    absent;
  final bool   isWarning;

  const AttendanceProgress({
    super.key,
    required this.attended,
    required this.total,
    required this.present,
    required this.late,
    required this.absent,
    required this.isWarning,
  });

  @override
  Widget build(BuildContext context) {
    final pct = total > 0 ? (attended / total * 100).round() : 0;

    return SizedBox(
      width: 72,
      height: 72,
      child: Stack(alignment: Alignment.center, children: [
        CustomPaint(
          size: const Size(72, 72),
          painter: CircularProgressPainter(
            presentFraction: total > 0 ? present / total : 0,
            lateFraction:    total > 0 ? late    / total : 0,
            absentFraction:  total > 0 ? absent  / total : 0,
            strokeWidth: 9,
          ),
        ),
        Text(
          '$pct',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: isWarning ? AppColors.red : AppColors.green,
          ),
        ),
      ]),
    );
  }
}