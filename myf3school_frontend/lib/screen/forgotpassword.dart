import 'package:flutter/material.dart';
import 'package:myf3school/controller/ForgotPasswordController.dart';
import 'forgotpassword.dart';
import 'otp_screen.dart';

const _fptOrange = Color(0xFFFF6F00);
const _fptBlue   = Color(0xFF0D47A1);

/// BƯỚC 1 — Chọn kênh (Phone / Email) và nhập thông tin
class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  final _controller = TextEditingController();
  String _channel   = 'EMAIL'; // mặc định email
  bool   _isLoading = false;

  bool get _isEmail => _channel == 'EMAIL';

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _sendOtp() async {
    final target = _controller.text.trim();
    if (target.isEmpty) {
      _showSnack(_isEmail ? 'Vui lòng nhập email' : 'Vui lòng nhập số điện thoại');
      return;
    }

    setState(() => _isLoading = true);

    final result = await ForgotPasswordController.sendOtp(
      target: target,
      channel: _channel,
    );

    setState(() => _isLoading = false);

    if (!mounted) return;

    if (result['success'] == true) {
      _showSnack(result['message'] ?? 'Đã gửi OTP', isError: false);
      // Chuyển sang màn OTP
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => OtpScreen(target: target, channel: _channel),
        ),
      );
    } else {
      _showSnack(result['message'] ?? 'Gửi OTP thất bại');
    }
  }

  void _showSnack(String msg, {bool isError = true}) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: isError ? Colors.redAccent : Colors.green,
      behavior: SnackBarBehavior.floating,
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: _fptBlue),
        title: const Text(
          'Quên mật khẩu',
          style: TextStyle(color: _fptBlue, fontWeight: FontWeight.w600),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 16),

            // Icon
            Center(
              child: Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: _fptOrange.withOpacity(.1),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.lock_reset, size: 56, color: _fptOrange),
              ),
            ),

            const SizedBox(height: 24),

            const Text(
              'Đặt lại mật khẩu',
              style: TextStyle(
                fontSize: 22, fontWeight: FontWeight.bold, color: _fptBlue,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Chọn cách nhận mã OTP để xác nhận danh tính của bạn.',
              style: TextStyle(color: Colors.grey, fontSize: 14, height: 1.5),
            ),

            const SizedBox(height: 32),

            // ── Chọn kênh ─────────────────────────────────────────
            const Text(
              'Gửi mã OTP qua',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(child: _ChannelCard(
                  icon: Icons.email_outlined,
                  label: 'Email',
                  subtitle: 'Gmail / FPT mail',
                  selected: _channel == 'EMAIL',
                  onTap: () {
                    setState(() {
                      _channel = 'EMAIL';
                      _controller.clear();
                    });
                  },
                )),
                const SizedBox(width: 12),
                Expanded(child: _ChannelCard(
                  icon: Icons.phone_android_outlined,
                  label: 'Điện thoại',
                  subtitle: 'SMS về máy',
                  selected: _channel == 'PHONE',
                  onTap: () {
                    setState(() {
                      _channel = 'PHONE';
                      _controller.clear();
                    });
                  },
                )),
              ],
            ),

            const SizedBox(height: 24),

            // ── Input ──────────────────────────────────────────────
            Text(
              _isEmail ? 'Địa chỉ Email' : 'Số điện thoại',
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _controller,
              keyboardType: _isEmail
                  ? TextInputType.emailAddress
                  : TextInputType.phone,
              decoration: InputDecoration(
                hintText: _isEmail
                    ? 'abc@fpt.edu.vn'
                    : '0901234567',
                filled: true,
                fillColor: Colors.white,
                prefixIcon: Icon(
                  _isEmail ? Icons.email_outlined : Icons.phone_outlined,
                  color: Colors.grey[600],
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

            const SizedBox(height: 32),

            // ── Nút gửi OTP ────────────────────────────────────────
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: _fptOrange,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                  elevation: 0,
                ),
                onPressed: _isLoading ? null : _sendOtp,
                child: _isLoading
                    ? const SizedBox(
                  width: 22, height: 22,
                  child: CircularProgressIndicator(
                    color: Colors.white, strokeWidth: 2.5,
                  ),
                )
                    : Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.send_outlined, size: 18),
                    const SizedBox(width: 8),
                    Text(
                      _isEmail ? 'Gửi OTP qua Email' : 'Gửi OTP qua SMS',
                      style: const TextStyle(
                        fontSize: 16, fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Ghi chú
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue.shade200),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(Icons.info_outline, size: 16, color: Colors.blue.shade700),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _isEmail
                          ? 'Mã OTP sẽ được gửi đến email của bạn và có hiệu lực trong 5 phút.'
                          : 'Mã OTP sẽ được gửi qua SMS và có hiệu lực trong 5 phút.',
                      style: TextStyle(fontSize: 12, color: Colors.blue.shade700),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Widget chọn kênh ─────────────────────────────────────────────────────────

class _ChannelCard extends StatelessWidget {
  final IconData icon;
  final String   label;
  final String   subtitle;
  final bool     selected;
  final VoidCallback onTap;

  const _ChannelCard({
    required this.icon,
    required this.label,
    required this.subtitle,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
        decoration: BoxDecoration(
          color: selected ? _fptOrange.withOpacity(.08) : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: selected ? _fptOrange : Colors.grey.shade300,
            width: selected ? 2 : 1,
          ),
        ),
        child: Column(
          children: [
            Icon(icon, size: 28, color: selected ? _fptOrange : Colors.grey),
            const SizedBox(height: 6),
            Text(label,
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  color: selected ? _fptOrange : Colors.black87,
                )),
            Text(subtitle,
                style: const TextStyle(fontSize: 11, color: Colors.grey)),
          ],
        ),
      ),
    );
  }
}