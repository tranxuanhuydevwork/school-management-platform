import 'package:flutter/material.dart';

import '../controller/AuthController.dart';
import '../routes/router_names.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => _LoginScreenState();
}

const fptOrange = Color(0xFFFF6F00);
const fptBlue = Color(0xFF0D47A1);
const lightGray = Color(0xFFF5F5F5);
const subtleGray = Color(0xFF9E9E9E);

class _LoginScreenState extends State<LoginScreen> {
  final userTextController     = TextEditingController();
  final passwordTextController = TextEditingController();
  bool _isLoading = false;

  @override
  void dispose() {
    userTextController.dispose();
    passwordTextController.dispose();
    super.dispose();
  }

  // ── Hàm login ────────────────────────────────────────
  Future<void> _login() async {
    final username = userTextController.text.trim();
    final password = passwordTextController.text.trim();

    if (username.isEmpty || password.isEmpty) {
      _showSnackBar('Vui lòng nhập đầy đủ thông tin');
      return;
    }

    setState(() => _isLoading = true);

    final result = await AuthController.login(username, password);

    setState(() => _isLoading = false);

    if (result['success'] == true) {
      Navigator.pushReplacementNamed(context, RouteNames.home);
    } else {
      _showSnackBar(result['message'] ?? 'Đăng nhập thất bại');
    }
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.redAccent,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  // ── Build ─────────────────────────────────────────────
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: lightGray,
      body: LayoutBuilder(
        builder: (context, constraints) {
          return Stack(
            children: [
              SingleChildScrollView(
                child: ConstrainedBox(
                  constraints: BoxConstraints(minHeight: constraints.maxHeight),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 24),
                    child: Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const SizedBox(height: 48),

                          Image.network(
                            'https://upload.wikimedia.org/wikipedia/commons/6/68/Logo_FPT_Education.png',
                            height: 80,
                          ),

                          const SizedBox(height: 20),

                          const Text(
                            "FPT University Academic Portal",
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              color: fptBlue,
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                              letterSpacing: 0.3,
                            ),
                          ),

                          const SizedBox(height: 36),

                          _buildInput(
                            controller: userTextController,
                            hint: "Phone number",
                            icon: Icons.person_outline,
                          ),

                          const SizedBox(height: 12),

                          _buildInput(
                            controller: passwordTextController,
                            hint: "Password",
                            icon: Icons.lock_outline,
                            obscure: true,
                          ),

                          const SizedBox(height: 24),

                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: fptOrange,
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(8),
                                ),
                              ),
                              onPressed: _isLoading ? null : _login,
                              child: _isLoading
                                  ? const SizedBox(
                                width: 22,
                                height: 22,
                                child: CircularProgressIndicator(
                                  color: Colors.white,
                                  strokeWidth: 2.5,
                                ),
                              )
                                  : const Text(
                                "LOGIN",
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                  letterSpacing: 0.8,
                                ),
                              ),
                            ),
                          ),

                          const SizedBox(height: 16),

                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              TextButton(
                                onPressed: () {
                                  userTextController.clear();
                                  passwordTextController.clear();
                                },
                                child: const Text(
                                  "Reset",
                                  style: TextStyle(color: fptBlue),
                                ),
                              ),
                              TextButton(
                                onPressed: () {
                                  Navigator.pushNamed(
                                      context, RouteNames.forgot_password);
                                },
                                child: const Text(
                                  "Forgot password?",
                                  style: TextStyle(color: fptBlue),
                                ),
                              ),
                            ],
                          ),

                          const SizedBox(height: 80),
                        ],
                      ),
                    ),
                  ),
                ),
              ),

              Positioned(
                bottom: 16,
                left: 0,
                right: 0,
                child: Center(
                  child: Text(
                    "© 2026 FPT University. All rights reserved.",
                    style: TextStyle(
                      fontSize: 12,
                      color: subtleGray,
                      letterSpacing: 0.2,
                    ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildInput({
    required TextEditingController controller,
    required String hint,
    required IconData icon,
    bool obscure = false,
  }) {
    return TextField(
      controller: controller,
      obscureText: obscure,
      decoration: InputDecoration(
        hintText: hint,
        filled: true,
        fillColor: Colors.white,
        prefixIcon: Icon(icon, color: Colors.grey[700]),
        contentPadding: const EdgeInsets.symmetric(vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
      ),
    );
  }
}