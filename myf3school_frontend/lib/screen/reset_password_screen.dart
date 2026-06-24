import 'package:flutter/material.dart';

import '../controller/ForgotPasswordController.dart';

const _fptOrange = Color(0xFFFF6F00);
const _fptBlue   = Color(0xFF0D47A1);

/// BƯỚC 3 — Nhập mật khẩu mới
class ResetPasswordScreen extends StatefulWidget {
  final String target;
  final String resetToken;

  const ResetPasswordScreen({
    super.key,
    required this.target,
    required this.resetToken,
  });

  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  final _passController    = TextEditingController();
  final _confirmController = TextEditingController();
  bool _isLoading         = false;
  bool _obscurePass       = true;
  bool _obscureConfirm    = true;

  @override
  void dispose() {
    _passController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  Future<void> _reset() async {
    final pass    = _passController.text.trim();
    final confirm = _confirmController.text.trim();

    if (pass.isEmpty || confirm.isEmpty) {
      _showSnack('Vui lòng nhập đầy đủ thông tin');
      return;
    }
    if (pass.length < 6) {
      _showSnack('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }
    if (pass != confirm) {
      _showSnack('Mật khẩu xác nhận không khớp');
      return;
    }

    setState(() => _isLoading = true);

    final result = await ForgotPasswordController.resetPassword(
      target:      widget.target,
      resetToken:  widget.resetToken,
      newPassword: pass,
    );

    setState(() => _isLoading = false);
    if (!mounted) return;

    if (result['success'] == true) {
      _showSuccessDialog();
    } else {
      _showSnack(result['message'] ?? 'Đặt lại mật khẩu thất bại');
    }
  }

  void _showSnack(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: Colors.redAccent,
      behavior: SnackBarBehavior.floating,
    ));
  }

  void _showSuccessDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: const BoxDecoration(
                color: Color(0xFFE8F5E9),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.check_circle, color: Colors.green, size: 48),
            ),
            const SizedBox(height: 16),
            const Text(
              'Thành công!',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text(
              'Mật khẩu của bạn đã được đặt lại.\nVui lòng đăng nhập lại.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey, fontSize: 14, height: 1.5),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: _fptOrange,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8)),
                  elevation: 0,
                ),
                onPressed: () {
                  // Pop tất cả về Login
                  Navigator.of(context).popUntil((route) => route.isFirst);
                },
                child: const Text(
                  'Đăng nhập ngay',
                  style: TextStyle(fontWeight: FontWeight.w600),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Strength indicator ────────────────────────────────────────────────────

  int get _strength {
    final p = _passController.text;
    int s = 0;
    if (p.length >= 6)  s++;
    if (p.length >= 10) s++;
    if (p.contains(RegExp(r'[A-Z]'))) s++;
    if (p.contains(RegExp(r'[0-9]'))) s++;
    if (p.contains(RegExp(r'[^A-Za-z0-9]'))) s++;
    return s;
  }

  Color get _strengthColor {
    if (_strength <= 1) return Colors.red;
    if (_strength <= 3) return Colors.orange;
    return Colors.green;
  }

  String get _strengthLabel {
    if (_strength <= 1) return 'Yếu';
    if (_strength <= 3) return 'Trung bình';
    return 'Mạnh';
  }

  // ── Build ─────────────────────────────────────────────────────────────────
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: _fptBlue),
        title: const Text(
          'Đặt lại mật khẩu',
          style: TextStyle(color: _fptBlue, fontWeight: FontWeight.w600),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 16),

            Center(
              child: Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: _fptBlue.withOpacity(.08),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.lock_outline, size: 48, color: _fptBlue),
              ),
            ),

            const SizedBox(height: 24),

            const Text(
              'Tạo mật khẩu mới',
              style: TextStyle(
                fontSize: 22, fontWeight: FontWeight.bold, color: _fptBlue,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Mật khẩu mới phải có ít nhất 6 ký tự.',
              style: TextStyle(color: Colors.grey, fontSize: 14),
            ),

            const SizedBox(height: 32),

            // ── Mật khẩu mới ────────────────────────────────────────
            const Text(
              'Mật khẩu mới',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _passController,
              obscureText: _obscurePass,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'Nhập mật khẩu mới',
                filled: true,
                fillColor: Colors.white,
                prefixIcon: const Icon(Icons.lock_outline, color: Colors.grey),
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscurePass ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                    color: Colors.grey,
                  ),
                  onPressed: () => setState(() => _obscurePass = !_obscurePass),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: BorderSide.none,
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: const BorderSide(color: _fptOrange, width: 1.5),
                ),
              ),
            ),

            // Password strength bar
            if (_passController.text.isNotEmpty) ...[
              const SizedBox(height: 8),
              Row(
                children: [
                  ...List.generate(5, (i) => Expanded(
                    child: Container(
                      height: 4,
                      margin: const EdgeInsets.symmetric(horizontal: 2),
                      decoration: BoxDecoration(
                        color: i < _strength ? _strengthColor : Colors.grey.shade200,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                  )),
                  const SizedBox(width: 8),
                  Text(
                    _strengthLabel,
                    style: TextStyle(
                      fontSize: 11,
                      color: _strengthColor,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ],

            const SizedBox(height: 16),

            // ── Xác nhận mật khẩu ───────────────────────────────────
            const Text(
              'Xác nhận mật khẩu',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _confirmController,
              obscureText: _obscureConfirm,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'Nhập lại mật khẩu',
                filled: true,
                fillColor: Colors.white,
                prefixIcon: const Icon(Icons.lock_outline, color: Colors.grey),
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscureConfirm ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                    color: Colors.grey,
                  ),
                  onPressed: () => setState(() => _obscureConfirm = !_obscureConfirm),
                ),
                // Viền đỏ nếu không khớp
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: BorderSide(
                    color: _confirmController.text.isNotEmpty &&
                        _confirmController.text != _passController.text
                        ? Colors.red
                        : Colors.transparent,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: BorderSide(
                    color: _confirmController.text.isNotEmpty &&
                        _confirmController.text != _passController.text
                        ? Colors.red
                        : _fptOrange,
                    width: 1.5,
                  ),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: BorderSide.none,
                ),
              ),
            ),

            if (_confirmController.text.isNotEmpty &&
                _confirmController.text != _passController.text) ...[
              const SizedBox(height: 4),
              const Text(
                'Mật khẩu không khớp',
                style: TextStyle(color: Colors.red, fontSize: 12),
              ),
            ],

            const SizedBox(height: 32),

            // ── Nút đặt lại ──────────────────────────────────────────
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: _fptOrange,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10)),
                  elevation: 0,
                ),
                onPressed: _isLoading ? null : _reset,
                child: _isLoading
                    ? const SizedBox(
                  width: 22, height: 22,
                  child: CircularProgressIndicator(
                      color: Colors.white, strokeWidth: 2.5),
                )
                    : const Text(
                  'ĐẶT LẠI MẬT KHẨU',
                  style: TextStyle(
                    fontSize: 16, fontWeight: FontWeight.w700,
                    letterSpacing: 0.5,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}