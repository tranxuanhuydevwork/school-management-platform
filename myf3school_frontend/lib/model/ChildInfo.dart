import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;
import '../widget/bottom_navigation_widget.dart';

// ════════════════════════════════════════════════════════════════════════════
// MODELS — ánh xạ 1-1 với UserProfileResponse từ UserProfileService.java
// ════════════════════════════════════════════════════════════════════════════

/// Tương ứng UserProfileResponse.ChildInfo
class ChildInfo {
  final int studentProfileId;
  final String fullName;
  final String studentCode;
  final String? className;
  final String relationship; // FATHER / MOTHER / GUARDIAN
  final double? gpa;

  ChildInfo({
    required this.studentProfileId,
    required this.fullName,
    required this.studentCode,
    this.className,
    required this.relationship,
    this.gpa,
  });
  factory ChildInfo.fromJson(Map<String, dynamic> j) => ChildInfo(
    studentProfileId: j['studentProfileId'] ?? 0,
    fullName:         j['fullName']         ?? '',
    studentCode:      j['studentCode']      ?? '',
    className:        j['className'],
    relationship:     j['relationship']     ?? 'GUARDIAN',
    gpa:              (j['gpa'] as num?)?.toDouble(),
  );

  String get relationshipLabel {
    switch (relationship) {
      case 'FATHER': return 'Bố của';
      case 'MOTHER': return 'Mẹ của';
      default:       return 'Giám hộ của';
    }
  }
}