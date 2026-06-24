import 'package:flutter/material.dart';
import 'WeekCalendar.dart';

class Schedule_Header extends StatelessWidget {
  final int    selectedIndex;
  final int    weeklyCount;
  final int    nextDayCount;   // số lớp của ngày tiếp theo có lịch
  final String nextDayLabel;   // "T3", "T5"...
  final Function(int, DateTime) onChanged;

  const Schedule_Header({
    super.key,
    required this.selectedIndex,
    required this.onChanged,
    this.weeklyCount  = 0,
    this.nextDayCount = 0,
    this.nextDayLabel = '',
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.only(
          top: 50, left: 20, right: 20, bottom: 20),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFFF37021), Color(0xFFFF8A3D)],
        ),
        borderRadius: BorderRadius.only(
          bottomLeft:  Radius.circular(30),
          bottomRight: Radius.circular(30),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Thời khóa biểu',
            style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: Colors.white),
          ),

          const SizedBox(height: 16),

          Row(
            children: [
              // Stat 1: Buổi trong tuần
              _StatChip(
                icon:  Icons.calendar_month_outlined,
                label: 'Tuần này',
                value: '$weeklyCount buổi',
              ),
              const SizedBox(width: 10),
              // Stat 2: Ngày tiếp theo có lịch
              _StatChip(
                icon:  Icons.arrow_forward_rounded,
                label: nextDayLabel.isEmpty
                    ? 'Sắp tới'
                    : 'Tiếp: $nextDayLabel',
                value: nextDayCount == 0
                    ? 'Nghỉ cả tuần'
                    : '$nextDayCount lớp',
              ),
            ],
          ),

          const SizedBox(height: 20),

          WeekCalendar(
            selectedIndex: selectedIndex,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  final IconData icon;
  final String   label;
  final String   value;

  const _StatChip({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color:        Colors.white.withOpacity(0.18),
        borderRadius: BorderRadius.circular(14),
        border:       Border.all(color: Colors.white.withOpacity(0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: Colors.white, size: 18),
          const SizedBox(width: 8),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontSize: 10,
                  color: Colors.white70,
                  fontWeight: FontWeight.w500,
                  letterSpacing: 0.5,
                ),
              ),
              Text(
                value,
                style: const TextStyle(
                  fontSize: 16,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  height: 1.2,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}