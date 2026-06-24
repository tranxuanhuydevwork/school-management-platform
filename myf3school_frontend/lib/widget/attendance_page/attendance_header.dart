// lib/widget/attendance_page/attendance_header.dart
import 'package:flutter/material.dart';
import '../AppColors.dart';
import 'legend_label.dart';


class AttendanceHeader extends StatelessWidget {
  const AttendanceHeader({super.key});

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(20),
    decoration: const BoxDecoration(
      gradient: LinearGradient(
          colors: [AppColors.primary, Color(0xFF1CA7C8)]),
      borderRadius:
      BorderRadius.vertical(bottom: Radius.circular(25)),
    ),
    child: const Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        LegendLabel(color: AppColors.green,     label: 'Có mặt'),
        LegendLabel(color: AppColors.orange,    label: 'Trễ'),
        LegendLabel(color: AppColors.red,       label: 'Vắng'),
        LegendLabel(color: AppColors.grey2,     label: 'Có phép'),
        LegendLabel(color: Colors.white54,      label: 'Chưa học'),
      ],
    ),
  );
}