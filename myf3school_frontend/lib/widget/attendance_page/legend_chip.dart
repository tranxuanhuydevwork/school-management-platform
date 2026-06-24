// lib/widget/attendance_page/legend_chip.dart
import 'package:flutter/material.dart';

class LegendChip extends StatelessWidget {
  final IconData icon;
  final Color    color;
  final String   label;

  const LegendChip({
    super.key,
    required this.icon,
    required this.color,
    required this.label,
  });

  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Icon(icon, size: 13, color: color),
      const SizedBox(width: 3),
      Text(label,
          style: TextStyle(
              fontSize: 12,
              color: color,
              fontWeight: FontWeight.w500)),
    ],
  );
}