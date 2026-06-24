import 'package:flutter/material.dart';

enum RequestStatus { draft, pending, approved, rejected }

extension RequestStatusExt on RequestStatus {
  String get label {
    switch (this) {
      case RequestStatus.draft:     return 'Nháp';
      case RequestStatus.pending:   return 'Chờ duyệt';
      case RequestStatus.approved:  return 'Đã duyệt';
      case RequestStatus.rejected:  return 'Từ chối';
    }
  }

  Color get color {
    switch (this) {
      case RequestStatus.draft:     return const Color(0xFF9E9E9E);
      case RequestStatus.pending:   return const Color(0xFFF59E0B);
      case RequestStatus.approved:  return const Color(0xFF10B981);
      case RequestStatus.rejected:  return const Color(0xFFEF4444);
    }
  }

  IconData get icon {
    switch (this) {
      case RequestStatus.draft:     return Icons.edit_outlined;
      case RequestStatus.pending:   return Icons.hourglass_empty_rounded;
      case RequestStatus.approved:  return Icons.check_circle_outline;
      case RequestStatus.rejected:  return Icons.cancel_outlined;
    }
  }

  static RequestStatus fromString(String s) {
    switch (s.toUpperCase()) {
      case 'PENDING':   return RequestStatus.pending;
      case 'APPROVED':  return RequestStatus.approved;
      case 'REJECTED':  return RequestStatus.rejected;
      default:          return RequestStatus.draft;
    }
  }
}

enum RequestType {
  leaveRequest,
  transcriptRequest,
  studentVerification,
  studentCardReissue,
}

extension RequestTypeExt on RequestType {
  String get label {
    switch (this) {
      case RequestType.leaveRequest:        return 'Đơn xin nghỉ học';
      case RequestType.transcriptRequest:   return 'Cấp bảng điểm';
      case RequestType.studentVerification: return 'Giấy xác nhận sinh viên';
      case RequestType.studentCardReissue:  return 'Cấp lại thẻ sinh viên';
    }
  }

  String get apiValue {
    switch (this) {
      case RequestType.leaveRequest:        return 'LEAVE_REQUEST';
      case RequestType.transcriptRequest:   return 'TRANSCRIPT_REQUEST';
      case RequestType.studentVerification: return 'STUDENT_VERIFICATION';
      case RequestType.studentCardReissue:  return 'STUDENT_CARD_REISSUE';
    }
  }

  IconData get icon {
    switch (this) {
      case RequestType.leaveRequest:        return Icons.event_busy_outlined;
      case RequestType.transcriptRequest:   return Icons.bar_chart_outlined;
      case RequestType.studentVerification: return Icons.badge_outlined;
      case RequestType.studentCardReissue:  return Icons.credit_card_outlined;
    }
  }

  Color get color {
    switch (this) {
      case RequestType.leaveRequest:        return const Color(0xFFFF6B35);
      case RequestType.transcriptRequest:   return const Color(0xFF3B82F6);
      case RequestType.studentVerification: return const Color(0xFF8B5CF6);
      case RequestType.studentCardReissue:  return const Color(0xFF10B981);
    }
  }

  static RequestType fromString(String s) {
    switch (s.toUpperCase()) {
      case 'LEAVE_REQUEST':         return RequestType.leaveRequest;
      case 'TRANSCRIPT_REQUEST':    return RequestType.transcriptRequest;
      case 'STUDENT_VERIFICATION':  return RequestType.studentVerification;
      case 'STUDENT_CARD_REISSUE':  return RequestType.studentCardReissue;
      default:                      return RequestType.leaveRequest;
    }
  }
}

class RequestForm {
  final int id;
  final RequestType type;
  final String title;
  final String description;
  final RequestStatus status;
  final DateTime createdAt;
  final String? rejectedReason;

  const RequestForm({
    required this.id,
    required this.type,
    required this.title,
    required this.description,
    required this.status,
    required this.createdAt,
    this.rejectedReason,
  });

  factory RequestForm.fromJson(Map<String, dynamic> json) {
    return RequestForm(
      id:             json['id'] as int,
      type:           RequestTypeExt.fromString(json['requestType'] as String),
      title:          json['title'] as String,
      description:    json['description'] as String,
      status:         RequestStatusExt.fromString(json['status'] as String),
      createdAt:      DateTime.parse(json['createdAt'] as String),
      rejectedReason: json['rejectedReason'] as String?,
    );
  }
}