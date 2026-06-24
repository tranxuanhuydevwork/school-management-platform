import 'dart:convert';
import 'package:flutter/material.dart';
import '../controller/AuthController.dart';
import '../model/ChildInfo.dart';
import '../model/UserProfile.dart';
import '../service/api_service.dart';
import '../widget/bottom_navigation_widget.dart';





// ════════════════════════════════════════════════════════════════════════════
// SCREEN
// ════════════════════════════════════════════════════════════════════════════

class UserProfileScreen extends StatefulWidget {
  const UserProfileScreen({super.key});

  @override
  State<UserProfileScreen> createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  static const fptOrange = Color(0xFFF37021);
  static const fptBlue   = Color(0xFF003A8F);

  UserProfile? _profile;
  bool    _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  // ── Fetch với auto-refresh ─────────────────────────────────────────────────

  Future<void> _load({bool retrying = false}) async {
    setState(() { _loading = true; _error = null; });
    try {
      final data = await ApiService.fetchProfile();
      if (mounted) {
        setState(() { _profile = UserProfile.fromJson(data); _loading = false; });
      }
    } on UnauthorizedException {
      if (retrying) { _goLogin(); return; }
      try {
        await ApiService.refreshAccessToken(); // cập nhật AuthController
        await _load(retrying: true);
      } on UnauthorizedException {
        _goLogin();
      }
    } catch (e) {
      if (mounted) setState(() { _error = e.toString(); _loading = false; });
    }
  }

  // ── Logout ─────────────────────────────────────────────────────────────────

  Future<void> _logout() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Đăng xuất'),
        content: const Text('Bạn có chắc muốn đăng xuất không?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Huỷ')),
          TextButton(
              onPressed: () => Navigator.pop(context, true),
              style: TextButton.styleFrom(foregroundColor: fptOrange),
              child: const Text('Đăng xuất')),
        ],
      ),
    );
    if (ok != true) return;

    await AuthController.logout(); // dùng logout() sẵn có của bạn
    _goLogin();
  }

  void _goLogin() {
    if (!mounted) return;
    Navigator.of(context).pushNamedAndRemoveUntil('/login', (_) => false);
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: Colors.grey[100],
    body: Column(
      children: [
        _header(),
        Expanded(
          child: _loading
              ? const Center(child: CircularProgressIndicator(color: fptOrange))
              : _error != null
              ? _errorView()
              : _body(),
        ),
      ],
    ),
    bottomNavigationBar: BottomNav(),
  );

  Widget _header() => Container(
    height: 120,
    decoration: const BoxDecoration(
      gradient: LinearGradient(colors: [fptOrange, fptBlue]),
      borderRadius: BorderRadius.only(
        bottomLeft:  Radius.circular(30),
        bottomRight: Radius.circular(30),
      ),
    ),
  );

  Widget _errorView() => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.wifi_off, color: fptOrange, size: 48),
          const SizedBox(height: 12),
          Text(_error!, textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.grey)),
          const SizedBox(height: 16),
          ElevatedButton.icon(
            onPressed: _load,
            icon: const Icon(Icons.refresh),
            label: const Text('Thử lại'),
            style: ElevatedButton.styleFrom(
                backgroundColor: fptOrange, foregroundColor: Colors.white),
          ),
        ],
      ),
    ),
  );

  Widget _body() {
    final p = _profile!;
    return RefreshIndicator(
      color: fptOrange,
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _profileCard(p),
          const SizedBox(height: 16),
          if (p.isStudent) ...[_studentCard(p), const SizedBox(height: 16)],
          if (p.isParent)  ...[_childrenCard(p.children), const SizedBox(height: 16)],
          _menuItem(Icons.notifications, 'Cài đặt thông báo'),
          _menuItem(Icons.security,      'Bảo mật tài khoản'),
          _menuItem(Icons.dark_mode,     'Giao diện'),
          _menuItem(Icons.help_outline,  'Trợ giúp & Hỗ trợ'),
          _menuItem(Icons.settings,      'Cài đặt'),
          const SizedBox(height: 16),
          _logoutBtn(),
          const SizedBox(height: 20),
          const Center(child: Text('UniApp v1.0.0',
              style: TextStyle(color: Colors.grey, fontSize: 12))),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  // ── Profile card ───────────────────────────────────────────────────────────

  Widget _profileCard(UserProfile p) => Card(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    elevation: 6,
    shadowColor: Colors.black26,
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          p.avatarUrl != null
              ? CircleAvatar(radius: 35, backgroundImage: NetworkImage(p.avatarUrl!))
              : CircleAvatar(
              radius: 35,
              backgroundColor: fptOrange,
              child: Text(_initials(p.fullName),
                  style: const TextStyle(color: Colors.white, fontSize: 20))),
          const SizedBox(height: 10),
          Text(p.fullName,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          Wrap(
            alignment: WrapAlignment.center,
            spacing: 6,
            children: p.roles.map((r) => _chip(_roleLabel(r),
                r == 'STUDENT'
                    ? fptBlue.withOpacity(0.15)
                    : fptOrange.withOpacity(0.15))).toList(),
          ),
          const Divider(height: 20),
          _infoRow(Icons.email,       'EMAIL',      p.email    ?? '—',
              Icons.phone,       'ĐIỆN THOẠI', p.phone    ?? '—'),
          const SizedBox(height: 10),
          _infoRow(Icons.location_on, 'ĐỊA CHỈ',   p.address     ?? '—',
              Icons.cake,        'NGÀY SINH',  p.dateOfBirth ?? '—'),
        ],
      ),
    ),
  );

  // ── Student card ───────────────────────────────────────────────────────────

  Widget _studentCard(UserProfile p) => Card(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    elevation: 4,
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(children: [
            const Icon(Icons.school, color: fptBlue, size: 20),
            const SizedBox(width: 8),
            const Text('Thông tin học tập',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
          ]),
          const Divider(height: 20),
          _infoRow(Icons.badge,         'MÃ HỌC SINH', p.studentCode ?? '—',
              Icons.class_,        'LỚP',         p.className   ?? '—'),
          const SizedBox(height: 10),
          _infoRow(Icons.timeline,      'HỌC KỲ',      p.semester     ?? '—',
              Icons.military_tech, 'XẾP LOẠI',    p.academicRank ?? '—'),
          const SizedBox(height: 10),
          _infoRow(Icons.bar_chart,     'GPA',
              p.gpa != null ? p.gpa!.toStringAsFixed(2) : '—',
              Icons.today,         'NHẬP HỌC',    p.enrollmentDate ?? '—'),
          if (p.emergencyContactName != null) ...[
            const Divider(height: 20),
            Row(children: [
              const Icon(Icons.contact_phone, color: Colors.red, size: 16),
              const SizedBox(width: 6),
              const Text('Liên lạc khẩn cấp',
                  style: TextStyle(fontSize: 13,
                      fontWeight: FontWeight.w600, color: Colors.red)),
            ]),
            const SizedBox(height: 8),
            _infoRow(Icons.person_outline, 'TÊN', p.emergencyContactName!,
                Icons.phone, 'SĐT', p.emergencyContactPhone ?? '—'),
          ],
        ],
      ),
    ),
  );

  // ── Children card ──────────────────────────────────────────────────────────

  Widget _childrenCard(List<ChildInfo> children) => Card(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    elevation: 4,
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(children: [
            const Icon(Icons.family_restroom, color: fptOrange, size: 20),
            const SizedBox(width: 8),
            const Text('Con em đang theo học',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
          ]),
          const SizedBox(height: 12),
          if (children.isEmpty)
            const Center(child: Text('Chưa có thông tin con em',
                style: TextStyle(color: Colors.grey)))
          else
            ...children.map(_childTile).toList(),
        ],
      ),
    ),
  );

  Widget _childTile(ChildInfo c) => Container(
    margin: const EdgeInsets.only(bottom: 12),
    padding: const EdgeInsets.all(12),
    decoration: BoxDecoration(
      color: fptBlue.withOpacity(0.05),
      borderRadius: BorderRadius.circular(14),
      border: Border.all(color: fptBlue.withOpacity(0.15)),
    ),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        CircleAvatar(
            radius: 22,
            backgroundColor: fptBlue,
            child: Text(_initials(c.fullName),
                style: const TextStyle(color: Colors.white, fontSize: 13))),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Badge quan hệ
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                margin: const EdgeInsets.only(bottom: 6),
                decoration: BoxDecoration(
                  color: fptOrange.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text('${c.relationshipLabel} ${c.fullName}',
                    style: const TextStyle(
                        fontSize: 11, color: fptOrange,
                        fontWeight: FontWeight.w600)),
              ),
              Text(c.fullName,
                  style: const TextStyle(
                      fontWeight: FontWeight.bold, fontSize: 14)),
              const SizedBox(height: 6),
              _infoRow(Icons.badge,  'MÃ HS', c.studentCode,
                  Icons.class_, 'LỚP',   c.className ?? '—'),
              if (c.gpa != null) ...[
                const SizedBox(height: 4),
                _infoItem(Icons.bar_chart, 'GPA', c.gpa!.toStringAsFixed(2)),
              ],
            ],
          ),
        ),
      ],
    ),
  );

  // ── Menu & logout ──────────────────────────────────────────────────────────

  Widget _menuItem(IconData icon, String title) => Card(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
    child: ListTile(
      leading: CircleAvatar(
          backgroundColor: Colors.grey[200],
          child: Icon(icon, color: fptOrange)),
      title: Text(title),
      trailing: const Icon(Icons.arrow_forward_ios, size: 16),
      onTap: () {},
    ),
  );

  Widget _logoutBtn() => GestureDetector(
    onTap: _logout,
    child: Container(
      decoration: BoxDecoration(
        color: fptOrange.withOpacity(0.12),
        borderRadius: BorderRadius.circular(15),
      ),
      child: const ListTile(
        leading: Icon(Icons.logout, color: fptOrange),
        title: Text('Đăng xuất',
            style: TextStyle(color: fptOrange, fontWeight: FontWeight.w600)),
      ),
    ),
  );

  // ── Helpers ────────────────────────────────────────────────────────────────

  Widget _chip(String text, Color color) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
    decoration: BoxDecoration(
        color: color, borderRadius: BorderRadius.circular(20)),
    child: Text(text, style: const TextStyle(fontSize: 12)),
  );

  Widget _infoRow(IconData i1, String l1, String v1,
      IconData i2, String l2, String v2) =>
      Row(children: [
        Expanded(child: _infoItem(i1, l1, v1)),
        Expanded(child: _infoItem(i2, l2, v2)),
      ]);

  Widget _infoItem(IconData icon, String label, String value) => Row(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Icon(icon, size: 14, color: fptBlue),
      const SizedBox(width: 5),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: const TextStyle(fontSize: 10, color: Colors.grey)),
            Text(value, style: const TextStyle(fontSize: 12),
                overflow: TextOverflow.ellipsis, maxLines: 2),
          ],
        ),
      ),
    ],
  );

  String _initials(String name) => name.trim().isEmpty
      ? '?'
      : name.trim().split(' ').map((w) => w[0]).take(2).join().toUpperCase();

  String _roleLabel(String role) {
    switch (role) {
      case 'STUDENT': return 'Học sinh';
      case 'PARENT':  return 'Phụ huynh';
      case 'TEACHER': return 'Giáo viên';
      default:        return role;
    }
  }
}