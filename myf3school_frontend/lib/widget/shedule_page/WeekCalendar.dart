import 'package:flutter/material.dart';

class WeekCalendar extends StatefulWidget {
  final int selectedIndex;
  final Function(int, DateTime) onChanged;

  const WeekCalendar({
    super.key,
    required this.selectedIndex,
    required this.onChanged,
  });

  @override
  State<WeekCalendar> createState() => _WeekCalendarState();
}

class _WeekCalendarState extends State<WeekCalendar> {
  int _weekOffset = 0;

  static const _dayLabels = ['T2','T3','T4','T5','T6','T7','CN'];

  DateTime _mondayFor(int offset) {
    final now        = DateTime.now();
    final thisMonday = DateTime(now.year, now.month, now.day - (now.weekday - 1));
    return thisMonday.add(Duration(days: offset * 7));
  }

  String _weekLabelFor(int offset) {
    if (offset == 0)  return 'Tuần này';
    if (offset == -1) return 'Tuần trước';
    if (offset == 1)  return 'Tuần sau';
    final mon = _mondayFor(offset);
    final sun = mon.add(const Duration(days: 6));
    return '${mon.day}/${mon.month} - ${sun.day}/${sun.month}';
  }

  void _changeWeek(int delta) {
    final newOffset = _weekOffset + delta;
    final newMonday = _mondayFor(newOffset);
    setState(() => _weekOffset = newOffset);
    widget.onChanged(0, newMonday);
  }

  void _resetToThisWeek() {
    final now        = DateTime.now();
    final todayIndex = now.weekday - 1;
    setState(() => _weekOffset = 0);
    widget.onChanged(todayIndex, now);
  }

  @override
  Widget build(BuildContext context) {
    final now    = DateTime.now();
    final today  = DateTime(now.year, now.month, now.day);
    final monday = _mondayFor(_weekOffset);

    return Column(
      children: [
        // ── Điều hướng tuần ──────────────────────────
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            _NavBtn(
              icon:  Icons.chevron_left,
              onTap: () => _changeWeek(-1),
            ),

            GestureDetector(
              onTap: _weekOffset != 0 ? _resetToThisWeek : null,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    _weekLabelFor(_weekOffset),
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                    ),
                  ),
                  if (_weekOffset != 0) ...[
                    const SizedBox(width: 4),
                    const Icon(Icons.replay,
                        color: Colors.white70, size: 13),
                  ],
                ],
              ),
            ),

            _NavBtn(
              icon:  Icons.chevron_right,
              onTap: () => _changeWeek(1),
            ),
          ],
        ),

        const SizedBox(height: 12),

        // ── 7 ngày ───────────────────────────────────
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: List.generate(7, (index) {
            final date     = DateTime(monday.year, monday.month,
                monday.day + index);
            final isToday  = date == today;
            final isPast   = date.isBefore(today);
            final isSelected = index == widget.selectedIndex
                && _weekOffset == 0;

            return GestureDetector(
              onTap: () => widget.onChanged(index, date),
              child: Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 8, vertical: 8),
                decoration: BoxDecoration(
                  color: isSelected
                      ? Colors.white
                      : isToday
                      ? Colors.white.withOpacity(0.25)
                      : Colors.transparent,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Column(
                  children: [
                    Text(
                      _dayLabels[index],
                      style: TextStyle(
                        color: isSelected
                            ? Colors.orange
                            : isPast ? Colors.white54 : Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 11,
                      ),
                    ),
                    const SizedBox(height: 4),
                    SizedBox(
                      width: 24, height: 24,
                      child: Center(
                        child: Text(
                          '${date.day}',
                          style: TextStyle(
                            color: isSelected
                                ? Colors.orange
                                : isPast ? Colors.white54 : Colors.white,
                            fontSize: 12,
                            fontWeight: isToday
                                ? FontWeight.bold : FontWeight.normal,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
        ),
      ],
    );
  }
}

class _NavBtn extends StatelessWidget {
  final IconData     icon;
  final VoidCallback onTap;
  const _NavBtn({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(6),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.2),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Icon(icon, color: Colors.white, size: 18),
      ),
    );
  }
}