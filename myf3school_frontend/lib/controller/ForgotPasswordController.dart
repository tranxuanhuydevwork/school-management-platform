import 'dart:convert';
import 'package:http/http.dart' as http;

/// Xử lý toàn bộ luồng quên mật khẩu:
///   sendOtp → verifyOtp → resetPassword
class ForgotPasswordController {
  static const String _baseUrl = 'http://10.0.2.2:8080/api/auth/forgot-password';

  // ── STEP 1: Gửi OTP ─────────────────────────────────────────────────────

  /// [channel]: "PHONE" hoặc "EMAIL"
  /// [target] : số điện thoại hoặc địa chỉ email
  static Future<Map<String, dynamic>> sendOtp({
    required String target,
    required String channel,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/send-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'target': target, 'channel': channel}),
      );

      final body = jsonDecode(response.body);
      if (response.statusCode == 200 && body['success'] == true) {
        return {'success': true, 'message': body['message']};
      }
      return {
        'success': false,
        'message': body['message'] ?? 'Không thể gửi OTP',
      };
    } catch (e) {
      return {'success': false, 'message': 'Lỗi kết nối: $e'};
    }
  }

  // ── STEP 2: Xác thực OTP ────────────────────────────────────────────────

  static Future<Map<String, dynamic>> verifyOtp({
    required String target,
    required String code,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/verify-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'target': target, 'code': code}),
      );

      final body = jsonDecode(response.body);
      if (response.statusCode == 200 && body['success'] == true) {
        final resetToken = body['data']?['resetToken'] as String?;
        return {'success': true, 'resetToken': resetToken};
      }
      return {
        'success': false,
        'message': body['message'] ?? 'Mã OTP không đúng',
      };
    } catch (e) {
      return {'success': false, 'message': 'Lỗi kết nối: $e'};
    }
  }

  // ── STEP 3: Đặt lại mật khẩu ────────────────────────────────────────────

  static Future<Map<String, dynamic>> resetPassword({
    required String target,
    required String resetToken,
    required String newPassword,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/reset'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'target': target,
          'resetToken': resetToken,
          'newPassword': newPassword,
        }),
      );

      final body = jsonDecode(response.body);
      if (response.statusCode == 200 && body['success'] == true) {
        return {'success': true};
      }
      return {
        'success': false,
        'message': body['message'] ?? 'Không thể đặt lại mật khẩu',
      };
    } catch (e) {
      return {'success': false, 'message': 'Lỗi kết nối: $e'};
    }
  }
}