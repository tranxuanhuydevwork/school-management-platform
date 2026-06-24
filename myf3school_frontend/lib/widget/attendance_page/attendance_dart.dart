// lib/widget/attendance_page/attendance_card.dart
import 'package:flutter/material.dart';
import '../../model/AttendanceRecord.dart';
import '../AppColors.dart';

import 'attendance_progress.dart';
import 'attendance_session_table.dart';
import 'legend_chip.dart';

class AttendanceCard extends StatefulWidget {
  final AttendanceRecord record;
  const AttendanceCard({super.key, required this.record});

  @override
  State<AttendanceCard> createState() => _AttendanceCardState();
}

class _AttendanceCardState extends State<AttendanceCard>
    with SingleTickerProviderStateMixin {
  bool _expanded = false;
  late AnimationController _ctrl;
  late Animation<double>   _anim;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
        duration: const Duration(milliseconds: 280), vsync: this);
    _anim = CurvedAnimation(parent: _ctrl, curve: Curves.easeInOut);
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  void _toggle() {
    setState(() => _expanded = !_expanded);
    _expanded ? _ctrl.forward() : _ctrl.reverse();
  }

  @override
  Widget build(BuildContext context) {
    final r         = widget.record;
    final absentPct = (r.absentPercent * 100).round();
    final isWarning = absentPct >= 20;

    return GestureDetector(
      onTap: _toggle,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [BoxShadow(
              color: Colors.black.withOpacity(0.06),
              blurRadius: 12,
              offset: const Offset(0, 4))],
        ),
        child: Column(children: [

          // ── Summary ────────────────────────────────────────────────────
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(children: [

              // Vòng tròn tiến độ
              AttendanceProgress(
                attended:  r.attended,
                total:     r.total,
                present:   r.present,
                late:      r.late,
                absent:    r.absent,
                isWarning: isWarning,
              ),
              const SizedBox(width: 14),

              // Tên môn + badge vắng + chip tóm tắt
              Expanded(child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(children: [
                    Text(r.subjectCode,
                        style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: AppColors.primary,
                            fontSize: 14)),
                    const SizedBox(width: 8),
                    if (absentPct > 0)
                      _AbsentBadge(pct: absentPct, isWarning: isWarning),
                  ]),
                  const SizedBox(height: 3),
                  Text(r.subjectName,
                      style: const TextStyle(
                          fontSize: 13, color: AppColors.textDark)),
                  const SizedBox(height: 8),
                  _SummaryChips(record: r),
                ],
              )),

              // Chevron
              AnimatedRotation(
                turns: _expanded ? 0.5 : 0,
                duration: const Duration(milliseconds: 280),
                child: const Icon(Icons.keyboard_arrow_down_rounded,
                    color: AppColors.textLight),
              ),
            ]),
          ),

          // ── Expanded: bảng buổi học ────────────────────────────────────
          SizeTransition(
            sizeFactor: _anim,
            child: AttendanceSessionTable(
              sessions: r.sessions,
              attended: r.attended,
              total:    r.total,
            ),
          ),
        ]),
      ),
    );
  }
}

// ── Sub-widgets private ───────────────────────────────────────────────────────

class _AbsentBadge extends StatelessWidget {
  final int  pct;
  final bool isWarning;
  const _AbsentBadge({required this.pct, required this.isWarning});

  @override
  Widget build(BuildContext context) {
    final color = isWarning ? AppColors.red : AppColors.orange;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text('Vắng $pct%',
          style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: color)),
    );
  }
}

class _SummaryChips extends StatelessWidget {
  final AttendanceRecord record;
  const _SummaryChips({required this.record});

  @override
  Widget build(BuildContext context) {
    final r = record;
    return Wrap(spacing: 8, children: [
      LegendChip(icon: Icons.check_circle_rounded,
          color: AppColors.green,  label: '✓ ${r.present}'),
      if (r.late > 0)
        LegendChip(icon: Icons.watch_later_rounded,
            color: AppColors.orange, label: '⏱ ${r.late}'),
      if (r.absent > 0)
        LegendChip(icon: Icons.cancel_rounded,
            color: AppColors.red,    label: '✕ ${r.absent}'),
      if (r.excused > 0)
        LegendChip(icon: Icons.check_circle_outline_rounded,
            color: AppColors.grey2,  label: '✓ ${r.excused}'),
      if (r.notYet > 0)
        LegendChip(icon: Icons.radio_button_unchecked_rounded,
            color: AppColors.textLight, label: '— ${r.notYet}'),
    ]);
  }
}