import 'package:flutter/material.dart';

class Lesson {
  final int id;
  final String subjectCode;
  final String subjectName;
  final String teacherName;
  final String room;
  final String classCode;
  final int periodStart;
  final int periodEnd;
  final String time;
  final String dayOfWeek;
  final String status;

  Lesson({
    required this.id,
    required this.subjectCode,
    required this.subjectName,
    required this.teacherName,
    required this.room,
    required this.classCode,
    required this.periodStart,
    required this.periodEnd,
    required this.time,
    required this.dayOfWeek,
    required this.status,
  });

  factory Lesson.fromJson(Map<String, dynamic> json) {
    return Lesson(
      id: json['id'] ?? 0,
      subjectCode: json['subjectCode'] ?? '',
      subjectName: json['subjectName'] ?? '',
      teacherName: json['teacherName'] ?? '',
      room: json['room'] ?? '',
      classCode: json['classCode'] ?? '',
      periodStart: json['periodStart'] ?? 0,
      periodEnd: json['periodEnd'] ?? 0,
      time: json['time'] ?? '',
      dayOfWeek: json['dayOfWeek'] ?? '',
      status: json['status'] ?? 'UPCOMING',
    );
  }

  Color get statusColor {
    switch (status) {
      case 'ONGOING':
        return Colors.green;
      case 'DONE':
        return Colors.grey;
      default:
        return Colors.blue;
    }
  }

  String get statusLabel {
    switch (status) {
      case 'ONGOING':
        return 'Đang học';
      case 'DONE':
        return 'Đã xong';
      default:
        return 'Sắp học';
    }
  }
}