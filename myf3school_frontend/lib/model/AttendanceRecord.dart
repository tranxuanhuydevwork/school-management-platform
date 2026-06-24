// lib/model/attendance_record.dart


import 'SessionRecord.dart';

class AttendanceRecord {
  final int     subjectId;    // ← backend: sectionId
  final String  subjectCode;
  final String  subjectName;
  final int     present;      // ← backend: presentCount
  final int     late;         // ← backend: lateCount
  final int     absent;       // ← backend: absentCount
  final int     excused;      // ← backend: excusedCount
  final int     total;        // ← backend: totalSessions
  final double  attendancePercent;
  final bool    atRisk;
  final List<SessionRecord> sessions;

  const AttendanceRecord({
    required this.subjectId,
    required this.subjectCode,
    required this.subjectName,
    required this.present,
    required this.late,
    required this.absent,
    required this.excused,
    required this.total,
    required this.attendancePercent,
    required this.atRisk,
    required this.sessions,
  });

  /// Tổng buổi được tính (có mặt + trễ + có phép)
  int get attended => present + late + excused;

  /// Tỷ lệ vắng / tổng số buổi
  double get absentPercent => total > 0 ? absent / total : 0.0;

  /// Cảnh báo nếu vắng >= 20%
  bool get isWarning => absentPercent >= 0.2;

  int get notYet {
    final value = total - (present + late + absent + excused);
    return value < 0 ? 0 : value;
  }
  factory AttendanceRecord.fromJson(Map<String, dynamic> json) {
    return AttendanceRecord(
      subjectId:          (json['sectionId']         as num? ?? 0).toInt(),
      subjectCode:        json['subjectCode']         as String? ?? '',
      subjectName:        json['subjectName']         as String? ?? '',
      present:            (json['presentCount']       as num? ?? 0).toInt(),
      late:               (json['lateCount']          as num? ?? 0).toInt(),
      absent:             (json['absentCount']        as num? ?? 0).toInt(),
      excused:            (json['excusedCount']       as num? ?? 0).toInt(),
      total:              (json['totalSessions']      as num? ?? 0).toInt(),
      attendancePercent:  (json['attendancePercent']  as num? ?? 0).toDouble(),
      atRisk:             json['atRisk']              as bool? ?? false,
      sessions: (json['sessions'] as List<dynamic>? ?? [])
          .map((s) => SessionRecord.fromJson(s as Map<String, dynamic>))
          .toList(),
    );
  }
}