import 'dart:convert';
import 'package:http/http.dart' as http;

class AuthController {
  static String? accessToken;
  static String? refreshToken;
  static int?    userId;
  static String? fullName;
  static List<String> roles = [];
  static String? avatarUrl;
  static int?    classId;
  static int?    studentId;

  static const String _baseUrl = 'http://10.0.2.2:8080/api';

  static Future<Map<String, dynamic>> login(
      String usernameOrEmail, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'phoneNumber': usernameOrEmail,
          'password': password,
        }),
      );

      print('>>> STATUS: ${response.statusCode}');
      print('>>> BODY: ${response.body}');

      if (response.body.isEmpty) {
        return {'success': false, 'message': 'Server trả về response rỗng'};
      }

      final body = jsonDecode(response.body);

      if (response.statusCode == 200 && body['success'] == true) {
        final data = body['data'] as Map<String, dynamic>;

        accessToken  = data['accessToken']  as String?;
        refreshToken = data['refreshToken'] as String?;
        userId       = data['userId'] as int?;
        fullName     = data['fullName'] as String?;
        avatarUrl    = data['avatarUrl'] as String?;
        classId      = data['classId'] as int?;
        studentId    = data['studentId'] as int?;
        roles        = (data['roles'] as List<dynamic>? ?? [])
            .map((e) => e.toString())
            .toList();

        print('>>> Login OK: userId=$userId, roles=$roles');
        return {'success': true, 'data': data};
      }

      final message = body['message'] as String? ?? 'Đăng nhập thất bại';
      return {'success': false, 'message': message};

    } catch (e) {
      print('>>> Login ERROR: $e');
      return {'success': false, 'message': 'Lỗi kết nối: $e'};
    }
  }

  static Future<void> logout() async {
    try {
      await http.post(
        Uri.parse('$_baseUrl/auth/logout'),
        headers: {
          'Content-Type': 'application/json',
          if (accessToken != null) 'Authorization': 'Bearer $accessToken',
        },
      );
    } catch (_) {}

    accessToken  = null;
    refreshToken = null;
    userId       = null;
    fullName     = null;
    roles        = [];
    avatarUrl    = null;
    classId      = null;
    studentId    = null;
  }

  static bool isLoggedIn() => accessToken != null && accessToken!.isNotEmpty;
  static String? get sessionToken => accessToken;
  static bool hasRole(String role) => roles.contains(role);
  static bool get isStudent     => hasRole('STUDENT');
  static bool get isTeacher     => hasRole('TEACHER');
  static bool get isAdmin       => hasRole('ADMIN');
  static bool get isParent => hasRole('PARENT');
  static Map<String, String> get authHeaders => {
    'Content-Type': 'application/json',
    if (accessToken != null) 'Authorization': 'Bearer $accessToken',
  };
}