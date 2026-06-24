// lib/widget/attendance_page/attendance_session_table.dart
import 'package:flutter/material.dart';

import '../../model/SessionRecord.dart';
import '../AppColors.dart';
import 'attendance_session_row.dart';


class AttendanceSessionTable extends StatelessWidget {
  final List<SessionRecord> sessions;
  final int                 attended;
  final int                 total;

  const AttendanceSessionTable({
    super.key,
    required this.sessions,
    required this.attended,
    required this.total,
  });

  static const _headerStyle = TextStyle(
      fontWeight: FontWeight.w600,
      fontSize: 12,
      color: AppColors.textLight);

  @override
  Widget build(BuildContext context) => Column(children: [
    const Divider(height: 1, color: Color(0xFFF0F0F0)),

    // ── Header bảng ──────────────────────────────────────────────────
    Container(
      color: const Color(0xFFF8F9FA),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      child: const Row(children: [
        SizedBox(width: 36, child: Text('STT', style: _headerStyle)),
        Expanded(child: Text('Ngày', style: _headerStyle)),
        Text('Trạng thái', style: _headerStyle),
      ]),
    ),

    // ── Các hàng buổi học ─────────────────────────────────────────────
    ...sessions.asMap().entries.map((e) => AttendanceSessionRow(
      session: e.value,
      isEven:  e.key % 2 == 0,
    )),

    // ── Footer tổng ───────────────────────────────────────────────────
    Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: const BoxDecoration(
        color: Color(0xFFF8F9FA),
        borderRadius:
        BorderRadius.vertical(bottom: Radius.circular(18)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          Text('$attended/$total buổi',
              style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 13,
                  color: AppColors.primary)),
        ],
      ),
    ),
  ]);
}