// lib/model/session_record.dart
// Tương ứng với AttendanceStudentResponse.SessionRecord (backend)

import 'AttendanceStatus.dart';

class SessionRecord {
  final int?    sessionId;
  final String  sessionDate;   // "yyyy-MM-dd"
  final int     periodStart;
  final int     periodEnd;
  final String? lessonTopic;
  final AttendanceStatus status;
  final String? note;

  const SessionRecord({
    this.sessionId,
    required this.sessionDate,
    required this.periodStart,
    required this.periodEnd,
    this.lessonTopic,
    required this.status,
    this.note,
  });

  factory SessionRecord.fromJson(Map<String, dynamic> json) {
    return SessionRecord(
      sessionId:   (json['sessionId'] as num?)?.toInt(),
      sessionDate: json['sessionDate'] as String? ?? '',
      periodStart: (json['periodStart'] as num? ?? 0).toInt(),
      periodEnd:   (json['periodEnd']   as num? ?? 0).toInt(),
      lessonTopic: json['lessonTopic'] as String?,
      status:      AttendanceStatusX.fromString(json['status'] as String? ?? ''),
      note:        json['note'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
    'sessionId':   sessionId,
    'sessionDate': sessionDate,
    'periodStart': periodStart,
    'periodEnd':   periodEnd,
    'lessonTopic': lessonTopic,
    'status':      status.name.toUpperCase(),
    'note':        note,
  };
}