import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:myf3school/screen/reset_password_screen.dart';

import '../controller/ForgotPasswordController.dart';


const _fptOrange = Color(0xFFFF6F00);
const _fptBlue   = Color(0xFF0D47A1);

/// BƯỚC 2 — Nhập mã OTP 6 chữ số
class OtpScreen extends StatefulWidget {
  final String target;
  final String channel;

  const OtpScreen({super.key, required this.target, required this.channel});

  @override
  State<OtpScreen> createState() => _OtpScreenState();
}

class _OtpScreenState extends State<OtpScreen> {
  // 6 ô OTP, mỗi ô 1 controller + focusNode
  final List<TextEditingController> _controllers =
  List.generate(6, (_) => TextEditingController());
  final List<FocusNode> _focusNodes =
  List.generate(6, (_) => FocusNode());

  bool _isLoading   = false;
  bool _isResending = false;

  // Đếm ngược resend (60 giây)
  int _countdown = 60;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _startCountdown();
  }

  @override
  void dispose() {
    for (var c in _controllers) c.dispose();
    for (var f in _focusNodes) f.dispose();
    _timer?.cancel();
    super.dispose();
  }

  void _startCountdown() {
    _countdown = 60;
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (_countdown == 0) {
        t.cancel();
      } else {
        setState(() => _countdown--);
      }
    });
  }

  String get _otp => _controllers.map((c) => c.text).join();

  Future<void> _verify() async {
    if (_otp.length < 6) {
      _showSnack('Vui lòng nhập đủ 6 chữ số');
      return;
    }

    setState(() => _isLoading = true);

    final result = await ForgotPasswordController.verifyOtp(
      target: widget.target,
      code: _otp,
    );

    setState(() => _isLoading = false);
    if (!mounted) return;

    if (result['success'] == true) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => ResetPasswordScreen(
            target:     widget.target,
            resetToken: result['resetToken'] as String,
          ),
        ),
      );
    } else {
      _showSnack(result['message'] ?? 'Mã OTP không đúng');
      // Xoá các ô để nhập lại
      for (var c in _controllers) c.clear();
      _focusNodes[0].requestFocus();
    }
  }

  Future<void> _resend() async {
    setState(() => _isResending = true);
    final result = await ForgotPasswordController.sendOtp(
      target:  widget.target,
      channel: widget.channel,
    );
    setState(() => _isResending = false);
    if (!mounted) return;

    if (result['success'] == true) {
      _showSnack('Đã gửi lại OTP', isError: false);
      _startCountdown();
      for (var c in _controllers) c.clear();
      _focusNodes[0].requestFocus();
    } else {
      _showSnack(result['message'] ?? 'Không thể gửi lại OTP');
    }
  }

  void _showSnack(String msg, {bool isError = true}) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: isError ? Colors.redAccent : Colors.green,
      behavior: SnackBarBehavior.floating,
    ));
  }

  // ── Build ─────────────────────────────────────────────────────────────────
  @override
  Widget build(BuildContext context) {
    final isEmail = widget.channel == 'EMAIL';
    final maskedTarget = isEmail
        ? _maskEmail(widget.target)
        : _maskPhone(widget.target);

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: _fptBlue),
        title: const Text(
          'Nhập mã OTP',
          style: TextStyle(color: _fptBlue, fontWeight: FontWeight.w600),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const SizedBox(height: 24),

            // Icon
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.green.shade50,
                shape: BoxShape.circle,
              ),
              child: Icon(
                isEmail ? Icons.mark_email_unread_outlined : Icons.sms_outlined,
                size: 48, color: Colors.green.shade600,
              ),
            ),

            const SizedBox(height: 20),
            const Text(
              'Xác thực OTP',
              style: TextStyle(
                fontSize: 22, fontWeight: FontWeight.bold, color: _fptBlue,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              isEmail
                  ? 'Mã OTP đã được gửi đến email\n$maskedTarget'
                  : 'Mã OTP đã được gửi đến số\n$maskedTarget',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.grey, fontSize: 14, height: 1.5),
            ),

            const SizedBox(height: 36),

            // ── 6 ô OTP ─────────────────────────────────────────────
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: List.generate(6, (i) => _OtpBox(
                controller: _controllers[i],
                focusNode:  _focusNodes[i],
                onChanged: (val) {
                  if (val.isNotEmpty && i < 5) {
                    _focusNodes[i + 1].requestFocus();
                  }
                  if (val.isEmpty && i > 0) {
                    _focusNodes[i - 1].requestFocus();
                  }
                  setState(() {});
                },
              )),
            ),

            const SizedBox(height: 32),

            // ── Nút xác thực ────────────────────────────────────────
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: _otp.length == 6 ? _fptOrange : Colors.grey.shade300,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10)),
                  elevation: 0,
                ),
                onPressed: (_isLoading || _otp.length < 6) ? null : _verify,
                child: _isLoading
                    ? const SizedBox(
                  width: 22, height: 22,
                  child: CircularProgressIndicator(
                      color: Colors.white, strokeWidth: 2.5),
                )
                    : const Text(
                  'XÁC NHẬN',
                  style: TextStyle(
                    fontSize: 16, fontWeight: FontWeight.w700,
                    letterSpacing: 0.8,
                  ),
                ),
              ),
            ),

            const SizedBox(height: 20),

            // ── Resend ──────────────────────────────────────────────
            if (_countdown > 0)
              Text(
                'Gửi lại OTP sau $_countdown giây',
                style: const TextStyle(color: Colors.grey, fontSize: 13),
              )
            else
              _isResending
                  ? const SizedBox(
                width: 20, height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              )
                  : TextButton.icon(
                onPressed: _resend,
                icon: const Icon(Icons.refresh, size: 16),
                label: const Text('Gửi lại OTP'),
                style: TextButton.styleFrom(foregroundColor: _fptBlue),
              ),
          ],
        ),
      ),
    );
  }

  String _maskEmail(String email) {
    final at = email.indexOf('@');
    if (at <= 2) return email;
    return '${email[0]}***${email.substring(at - 1)}';
  }

  String _maskPhone(String phone) {
    if (phone.length < 4) return phone;
    return '${phone.substring(0, 3)}****${phone.substring(phone.length - 3)}';
  }
}

// ── Widget ô OTP đơn lẻ ─────────────────────────────────────────────────────

class _OtpBox extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode             focusNode;
  final ValueChanged<String>  onChanged;

  const _OtpBox({
    required this.controller,
    required this.focusNode,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 44,
      height: 52,
      child: TextField(
        controller: controller,
        focusNode:  focusNode,
        textAlign:  TextAlign.center,
        maxLength:  1,
        keyboardType: TextInputType.number,
        inputFormatters: [FilteringTextInputFormatter.digitsOnly],
        style: const TextStyle(
          fontSize: 22, fontWeight: FontWeight.bold, color: _fptBlue,
        ),
        decoration: InputDecoration(
          counterText: '',
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: Colors.grey),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: _fptOrange, width: 2),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: BorderSide(
              color: controller.text.isNotEmpty
                  ? _fptOrange
                  : Colors.grey.shade300,
            ),
          ),
        ),
        onChanged: onChanged,
      ),
    );
  }
}