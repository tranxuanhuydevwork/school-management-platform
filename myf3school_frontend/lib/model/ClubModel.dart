// lib/model/ClubModel.dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../controller/AuthController.dart';

// ════════════════════════════════════════════════════════════════════════════
// ENUMS
// ════════════════════════════════════════════════════════════════════════════

enum ClubStatus       { active, inactive, suspended, disbanded }
enum ClubMemberStatus { pending, active, left, expelled }
enum ClubMemberRole   { president, vicePresident, secretary, treasurer, member }
enum ClubActivityStatus { scheduled, inProgress, completed, cancelled }

extension ClubMemberRoleX on ClubMemberRole {
  static ClubMemberRole fromString(String s) {
    switch (s.toUpperCase()) {
      case 'PRESIDENT':      return ClubMemberRole.president;
      case 'VICE_PRESIDENT': return ClubMemberRole.vicePresident;
      case 'SECRETARY':      return ClubMemberRole.secretary;
      case 'TREASURER':      return ClubMemberRole.treasurer;
      default:               return ClubMemberRole.member;
    }
  }

  String get label {
    switch (this) {
      case ClubMemberRole.president:     return 'Chủ nhiệm';
      case ClubMemberRole.vicePresident: return 'Phó chủ nhiệm';
      case ClubMemberRole.secretary:     return 'Thư ký';
      case ClubMemberRole.treasurer:     return 'Thủ quỹ';
      case ClubMemberRole.member:        return 'Thành viên';
    }
  }

  bool get isLeader =>
      this == ClubMemberRole.president || this == ClubMemberRole.vicePresident;
}

extension ClubMemberStatusX on ClubMemberStatus {
  static ClubMemberStatus fromString(String s) {
    switch (s.toUpperCase()) {
      case 'ACTIVE':   return ClubMemberStatus.active;
      case 'LEFT':     return ClubMemberStatus.left;
      case 'EXPELLED': return ClubMemberStatus.expelled;
      default:         return ClubMemberStatus.pending;
    }
  }
}

extension ClubActivityStatusX on ClubActivityStatus {
  static ClubActivityStatus fromString(String s) {
    switch (s.toUpperCase()) {
      case 'IN_PROGRESS': return ClubActivityStatus.inProgress;
      case 'COMPLETED':   return ClubActivityStatus.completed;
      case 'CANCELLED':   return ClubActivityStatus.cancelled;
      default:            return ClubActivityStatus.scheduled;
    }
  }

  String get label {
    switch (this) {
      case ClubActivityStatus.scheduled:  return 'Sắp diễn ra';
      case ClubActivityStatus.inProgress: return 'Đang diễn ra';
      case ClubActivityStatus.completed:  return 'Đã kết thúc';
      case ClubActivityStatus.cancelled:  return 'Đã huỷ';
    }
  }
}

// ════════════════════════════════════════════════════════════════════════════
// MODELS
// ════════════════════════════════════════════════════════════════════════════

class ClubSummary {
  final int               id;
  final String            code;
  final String            name;
  final String?           description;
  final String?           logoUrl;
  final String?           meetingLocation;
  final int?              maxMembers;
  final String?           foundedDate;
  final String            status;
  final String?           advisorName;
  final String?           presidentName;
  final int               memberCount;
  final int               upcomingEvents;
  final ClubMemberStatus? myStatus;
  final ClubMemberRole?   myRole;
  // ✅ Sửa: đổi isRecruiting → canJoin (khớp với backend ClubResponse)
  final bool              canJoin;

  const ClubSummary({
    required this.id,
    required this.code,
    required this.name,
    this.description,
    this.logoUrl,
    this.meetingLocation,
    this.maxMembers,
    this.foundedDate,
    required this.status,
    this.advisorName,
    this.presidentName,
    required this.memberCount,
    required this.upcomingEvents,
    this.myStatus,
    this.myRole,
    required this.canJoin,
  });

  bool get isMember  => myStatus == ClubMemberStatus.active;
  bool get isPending => myStatus == ClubMemberStatus.pending;
  bool get isLeader  => myRole?.isLeader ?? false;
  // ✅ Sửa: isRecruiting → canJoin (dùng trong _ClubCard._buildActionBadge)
  bool get isRecruiting => canJoin;

  factory ClubSummary.fromJson(Map<String, dynamic> j) => ClubSummary(
    id:              j['id']             ?? 0,
    code:            j['code']           ?? '',
    name:            j['name']           ?? '',
    description:     j['description'],
    logoUrl:         j['logoUrl'],
    meetingLocation: j['meetingLocation'],
    maxMembers:      j['maxMembers'],
    foundedDate:     j['foundedDate'],
    status:          j['status']         ?? 'ACTIVE',
    advisorName:     j['advisorName'],
    presidentName:   j['presidentName'],
    memberCount:     j['memberCount']    ?? 0,
    upcomingEvents:  j['upcomingEvents'] ?? 0,
    myStatus: j['myStatus'] != null
        ? ClubMemberStatusX.fromString(j['myStatus']) : null,
    myRole: j['myRole'] != null
        ? ClubMemberRoleX.fromString(j['myRole']) : null,
    canJoin: j['canJoin'] ?? false,
  );
}

class ClubMemberDetail {
  final int              id;
  final int              studentId;
  final String           studentName;
  final String           studentCode;
  final String?          avatarUrl;
  final ClubMemberRole   role;
  final ClubMemberStatus status;
  final String?          joinDate;

  const ClubMemberDetail({
    required this.id,
    required this.studentId,
    required this.studentName,
    required this.studentCode,
    this.avatarUrl,
    required this.role,
    required this.status,
    this.joinDate,
  });

  factory ClubMemberDetail.fromJson(Map<String, dynamic> j) => ClubMemberDetail(
    id:          j['id']          ?? 0,
    studentId:   j['studentId']   ?? 0,
    studentName: j['studentName'] ?? '',
    studentCode: j['studentCode'] ?? '',
    avatarUrl:   j['avatarUrl'],
    role:        ClubMemberRoleX.fromString(j['role'] ?? ''),
    status:      ClubMemberStatusX.fromString(j['status'] ?? ''),
    joinDate:    j['joinDate'],
  );
}

class ClubActivity {
  final int                id;
  final int                clubId;
  final String             clubName;
  final String             clubCode;
  final String             title;
  final String?            description;
  final String             startTime;
  final String?            endTime;
  final String?            location;
  final int                conductPoints;
  final ClubActivityStatus status;

  const ClubActivity({
    required this.id,
    required this.clubId,
    required this.clubName,
    required this.clubCode,
    required this.title,
    this.description,
    required this.startTime,
    this.endTime,
    this.location,
    required this.conductPoints,
    required this.status,
  });

  factory ClubActivity.fromJson(Map<String, dynamic> j) => ClubActivity(
    id:            j['id']            ?? 0,
    clubId:        j['clubId']        ?? 0,
    clubName:      j['clubName']      ?? '',
    clubCode:      j['clubCode']      ?? '',
    title:         j['title']         ?? '',
    description:   j['description'],
    startTime:     j['startTime']     ?? '',
    endTime:       j['endTime'],
    location:      j['location'],
    conductPoints: j['conductPoints'] ?? 0,
    status:        ClubActivityStatusX.fromString(j['status'] ?? ''),
  );
}

// ════════════════════════════════════════════════════════════════════════════
// API SERVICE
// ════════════════════════════════════════════════════════════════════════════

class ClubApiService {
  static const _base = 'http://10.0.2.2:8080/api';
  static Map<String, String> get _h => AuthController.authHeaders;

  // ✅ Sửa: bỏ categoryId (không có trong backend), chỉ còn search
  static Future<List<ClubSummary>> fetchClubs({String? search}) async {
    final params = <String, String>{
      if (search != null && search.isNotEmpty) 'search': search,
      if (AuthController.studentId != null)
        'studentId': '${AuthController.studentId}',
    };
    final uri = Uri.parse('$_base/clubs').replace(queryParameters: params);
    final res = await http.get(uri, headers: _h)
        .timeout(const Duration(seconds: 15));
    _check(res);
    final list = _data(res) as List;
    return list.map((e) => ClubSummary.fromJson(e)).toList();
  }

  static Future<List<ClubSummary>> fetchMyClubs() async {
    final res = await http.get(Uri.parse('$_base/clubs/my'), headers: _h)
        .timeout(const Duration(seconds: 15));
    _check(res);
    final list = _data(res) as List;
    return list.map((e) => ClubSummary.fromJson(e)).toList();
  }

  static Future<ClubMemberDetail> joinClub(int clubId) async {
    final res = await http.post(
      Uri.parse('$_base/clubs/$clubId/join'),
      headers: _h,
      body: jsonEncode({'studentId': AuthController.studentId}),
    ).timeout(const Duration(seconds: 10));
    _check(res);
    return ClubMemberDetail.fromJson(_data(res));
  }

  static Future<void> leaveClub(int clubId, {String? reason}) async {
    final res = await http.delete(
      Uri.parse('$_base/clubs/$clubId/leave'),
      headers: _h,
      body: jsonEncode({
        'studentId': AuthController.studentId,
        if (reason != null) 'leaveReason': reason,
      }),
    ).timeout(const Duration(seconds: 10));
    _check(res);
  }

  static Future<List<ClubMemberDetail>> fetchMembers(int clubId) async {
    final res = await http.get(
        Uri.parse('$_base/clubs/$clubId/members'), headers: _h)
        .timeout(const Duration(seconds: 15));
    _check(res);
    final list = _data(res) as List;
    return list.map((e) => ClubMemberDetail.fromJson(e)).toList();
  }

  static Future<void> approveMember(int memberId) async {
    final res = await http.put(
        Uri.parse('$_base/clubs/members/$memberId/approve'), headers: _h)
        .timeout(const Duration(seconds: 10));
    _check(res);
  }

  static Future<void> rejectMember(int memberId) async {
    final res = await http.put(
        Uri.parse('$_base/clubs/members/$memberId/reject'), headers: _h)
        .timeout(const Duration(seconds: 10));
    _check(res);
  }

  static Future<void> expelMember(int memberId, {String? reason}) async {
    final res = await http.put(
      Uri.parse('$_base/clubs/members/$memberId/expel'),
      headers: _h,
      body: jsonEncode({
        'studentId': 0,
        if (reason != null) 'leaveReason': reason,
      }),
    ).timeout(const Duration(seconds: 10));
    _check(res);
  }

  static Future<void> changeRole(int memberId, String role) async {
    final res = await http.put(
      Uri.parse('$_base/clubs/members/$memberId/role'),
      headers: _h,
      body: jsonEncode({'role': role}),
    ).timeout(const Duration(seconds: 10));
    _check(res);
  }

  static Future<List<ClubActivity>> fetchAllActivities() async {
    final res = await http.get(
        Uri.parse('$_base/clubs/activities'), headers: _h)
        .timeout(const Duration(seconds: 15));
    _check(res);
    final list = _data(res) as List;
    return list.map((e) => ClubActivity.fromJson(e)).toList();
  }

  static Future<List<ClubActivity>> fetchClubActivities(int clubId) async {
    final res = await http.get(
        Uri.parse('$_base/clubs/$clubId/activities'), headers: _h)
        .timeout(const Duration(seconds: 15));
    _check(res);
    final list = _data(res) as List;
    return list.map((e) => ClubActivity.fromJson(e)).toList();
  }

  static Future<ClubActivity> createActivity(
      int clubId, Map<String, dynamic> body) async {
    final res = await http.post(
      Uri.parse('$_base/clubs/$clubId/activities'),
      headers: _h,
      body: jsonEncode(body),
    ).timeout(const Duration(seconds: 10));
    _check(res);
    return ClubActivity.fromJson(_data(res));
  }

  static dynamic _data(http.Response res) {
    final body = jsonDecode(utf8.decode(res.bodyBytes)) as Map<String, dynamic>;
    return body['data'];
  }

  static void _check(http.Response res) {
    if (res.statusCode == 401) throw Exception('UNAUTHORIZED');
    if (res.statusCode == 403) throw Exception('FORBIDDEN');
    if (res.statusCode >= 400) {
      final msg = jsonDecode(utf8.decode(res.bodyBytes))['message']
          ?? res.statusCode.toString();
      throw Exception(msg);
    }
  }
}