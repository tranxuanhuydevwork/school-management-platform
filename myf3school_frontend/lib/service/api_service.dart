import 'dart:convert';
import 'package:http/http.dart' as http;

import '../controller/AuthController.dart';

/// Gọi tất cả API cần auth.
/// Dùng AuthController.accessToken — không cần SharedPreferences.
class ApiService {
  static const _base = 'http://10.0.2.2:8080/api'; // ← đổi nếu cần

  // ── GET /user/profile ────────────────────────────────────────────────────

  static Future<Map<String, dynamic>> fetchProfile() async {
    final res = await http.get(
      Uri.parse('$_base/user/profile'),
      headers: AuthController.authHeaders,
    ).timeout(const Duration(seconds: 15));

    if (res.statusCode == 200) {
      final body = jsonDecode(utf8.decode(res.bodyBytes)) as Map<String, dynamic>;
      return body['data'] as Map<String, dynamic>;
    }

    if (res.statusCode == 401) throw UnauthorizedException();
    throw ApiException('Lỗi server: ${res.statusCode}');
  }

  // ── POST /auth/refresh ────────────────────────────────────────────────────

  static Future<void> refreshAccessToken() async {
    final rt = AuthController.refreshToken;
    if (rt == null) throw UnauthorizedException();

    final res = await http.post(
      Uri.parse('$_base/auth/refresh'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'refreshToken': rt}),
    ).timeout(const Duration(seconds: 10));

    if (res.statusCode == 200) {
      final body = jsonDecode(utf8.decode(res.bodyBytes)) as Map<String, dynamic>;
      final data = body['data'] as Map<String, dynamic>;
      // Cập nhật thẳng vào AuthController — đồng bộ với phần còn lại của app
      AuthController.accessToken  = data['accessToken']  as String?;
      AuthController.refreshToken = data['refreshToken'] as String?;
    } else {
      throw UnauthorizedException();
    }
  }
}

class UnauthorizedException implements Exception {}
class ApiException implements Exception {
  final String message;
  ApiException(this.message);
  @override String toString() => message;
}