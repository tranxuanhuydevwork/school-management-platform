// lib/model/attendance_status.dart

enum AttendanceStatus { present, late, absent, excused, notYet }

extension AttendanceStatusX on AttendanceStatus {
  static AttendanceStatus fromString(String s) {
    switch (s.toUpperCase()) {
      case 'PRESENT': return AttendanceStatus.present;
      case 'LATE':    return AttendanceStatus.late;
      case 'ABSENT':  return AttendanceStatus.absent;
      case 'EXCUSED': return AttendanceStatus.excused;
      default:        return AttendanceStatus.notYet;
    }
  }

  String get label {
    switch (this) {
      case AttendanceStatus.present: return 'Có mặt';
      case AttendanceStatus.late:    return 'Đến muộn';
      case AttendanceStatus.absent:  return 'Vắng mặt';
      case AttendanceStatus.excused: return 'Có phép';
      case AttendanceStatus.notYet:  return 'Chưa điểm danh';
    }
  }
}