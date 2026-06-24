// lib/widget/attendance_page/attendance_session_row.dart
import 'package:flutter/material.dart';

import '../../model/AttendanceStatus.dart';
import '../../model/SessionRecord.dart';
import '../AppColors.dart';


class AttendanceSessionRow extends StatelessWidget {
  final SessionRecord session;
  final bool          isEven;

  const AttendanceSessionRow({
    super.key,
    required this.session,
    required this.isEven,
  });

  Color _color(AttendanceStatus s) {
    switch (s) {
      case AttendanceStatus.present: return AppColors.green;
      case AttendanceStatus.late:    return AppColors.orange;
      case AttendanceStatus.absent:  return AppColors.red;
      case AttendanceStatus.excused: return AppColors.grey2;
      case AttendanceStatus.notYet:  return AppColors.textLight;
    }
  }

  String _label(AttendanceStatus s) {
    switch (s) {
      case AttendanceStatus.present: return 'Có mặt';
      case AttendanceStatus.late:    return 'Trễ';
      case AttendanceStatus.absent:  return 'Vắng';
      case AttendanceStatus.excused: return 'Có phép';
      case AttendanceStatus.notYet:  return 'Chưa học';
    }
  }

  IconData _icon(AttendanceStatus s) {
    switch (s) {
      case AttendanceStatus.present: return Icons.check_circle_rounded;
      case AttendanceStatus.late:    return Icons.watch_later_rounded;
      case AttendanceStatus.absent:  return Icons.cancel_rounded;
      case AttendanceStatus.excused: return Icons.check_circle_outline_rounded;
      case AttendanceStatus.notYet:  return Icons.radio_button_unchecked_rounded;
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = session;
    return Container(
      color: isEven ? Colors.white : const Color(0xFFFAFAFA),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(children: [
        // STT
        SizedBox(
          width: 36,
          child: Text('${s.sessionId ?? '-'}',
              style: const TextStyle(fontSize: 13, color: AppColors.textLight)),
        ),
        // Ngày + ghi chú
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(s.sessionDate, style: const TextStyle(fontSize: 13)),
              if (s.lessonTopic != null)
                Text(s.lessonTopic!,
                    style: const TextStyle(
                        fontSize: 11, color: AppColors.textLight)),
              if (s.note != null)
                Text('(${s.note})',
                    style: const TextStyle(
                        fontSize: 11, color: AppColors.textLight)),
            ],
          ),
        ),
        // Trạng thái
        Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(_icon(s.status), size: 16, color: _color(s.status)),
          const SizedBox(width: 4),
          Text(_label(s.status),
              style: TextStyle(
                  fontSize: 13,
                  color: _color(s.status),
                  fontWeight: FontWeight.w500)),
        ]),
      ]),
    );
  }
}