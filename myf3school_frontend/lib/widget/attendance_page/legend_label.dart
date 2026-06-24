// lib/widget/attendance_page/legend_label.dart
import 'package:flutter/material.dart';

class LegendLabel extends StatelessWidget {
  final Color  color;
  final String label;

  const LegendLabel({super.key, required this.color, required this.label});

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      CircleAvatar(radius: 5, backgroundColor: color),
      const SizedBox(width: 5),
      Text(label,
          style: const TextStyle(color: Colors.white, fontSize: 12)),
    ],
  );
}