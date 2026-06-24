
import 'ChildInfo.dart';

class UserProfile {
  // ── Thông tin chung ──────────────────────────────────────────────────────
  final int    userId;
  final String fullName;
  final String? email;
  final String? phone;
  final String? avatarUrl;
  final String? address;
  final String? dateOfBirth;   // LocalDate → "yyyy-MM-dd"
  final List<String> roles;

  // ── STUDENT fields ───────────────────────────────────────────────────────
  final String? studentCode;
  final String? className;
  final String? semester;        // tính bởi calculateSemester()
  final double? gpa;
  final String? academicRank;
  final String? enrollmentDate;  // LocalDate → "yyyy-MM-dd"
  final String? emergencyContactName;
  final String? emergencyContactPhone;

  // ── PARENT fields ────────────────────────────────────────────────────────
  final List<ChildInfo> children;

  UserProfile({
    required this.userId,
    required this.fullName,
    this.email,
    this.phone,
    this.avatarUrl,
    this.address,
    this.dateOfBirth,
    required this.roles,
    this.studentCode,
    this.className,
    this.semester,
    this.gpa,
    this.academicRank,
    this.enrollmentDate,
    this.emergencyContactName,
    this.emergencyContactPhone,
    this.children = const [],
  });

  bool get isStudent => roles.contains('STUDENT');
  bool get isParent  => roles.contains('PARENT');

  factory UserProfile.fromJson(Map<String, dynamic> j) => UserProfile(
    userId:      j['userId']   ?? 0,
    fullName:    j['fullName'] ?? '',
    email:       j['email'],
    phone:       j['phone'],
    avatarUrl:   j['avatarUrl'],
    address:     j['address'],
    dateOfBirth: j['dateOfBirth']?.toString(),
    // roles trả về dạng Set<RoleType> được serialize thành ["STUDENT"] hoặc ["PARENT"]
    roles: List<String>.from(j['roles'] ?? []),
    studentCode:           j['studentCode'],
    className:             j['className'],
    semester:              j['semester'],
    gpa:                   (j['gpa'] as num?)?.toDouble(),
    academicRank:          j['academicRank'],
    enrollmentDate:        j['enrollmentDate']?.toString(),
    emergencyContactName:  j['emergencyContactName'],
    emergencyContactPhone: j['emergencyContactPhone'],
    children: (j['children'] as List<dynamic>? ?? [])
        .map((c) => ChildInfo.fromJson(c as Map<String, dynamic>))
        .toList(),
  );
}