import 'package:flutter/material.dart';
import '../../model/ClubModel.dart';

const _orange = Color(0xFFF37021);

// ════════════════════════════════════════════════════════════════════════════
// SectionHeader — tiêu đề section có thanh màu bên trái
// ════════════════════════════════════════════════════════════════════════════

class SectionHeader extends StatelessWidget {
  final String title;
  final Color  color;

  const SectionHeader({super.key, required this.title, required this.color});

  @override
  Widget build(BuildContext context) => Row(children: [
    Container(
      width: 4, height: 18,
      decoration: BoxDecoration(
          color: color, borderRadius: BorderRadius.circular(2)),
    ),
    const SizedBox(width: 8),
    Text(title,
        style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 14,
            color: color)),
  ]);
}