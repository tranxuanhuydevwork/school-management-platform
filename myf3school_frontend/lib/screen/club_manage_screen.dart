// lib/screen/club/club_manage_screen.dart
// Chỉ PRESIDENT hoặc VICE_PRESIDENT mới điều hướng tới đây.
import 'package:flutter/material.dart';
import '../model/ClubModel.dart';
import '../widget/club_page/club_activity_card.dart';
import '../widget/club_page/club_header.dart';
import '../widget/club_page/club_member_tile.dart';
import 'club_detail_screen.dart';

// ignore_for_file: use_build_context_synchronously

const _orange = Color(0xFFF37021);
const _blue   = Color(0xFF003A8F);

class ClubManageScreen extends StatefulWidget {
  final ClubSummary club;
  const ClubManageScreen({super.key, required this.club});

  @override
  State<ClubManageScreen> createState() => _ClubManageScreenState();
}

class _ClubManageScreenState extends State<ClubManageScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tab;

  List<ClubMemberDetail> _members    = [];
  List<ClubActivity>     _activities = [];
  bool _loadingMem = true;
  bool _loadingAct = true;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 3, vsync: this);
    _fetchMembers();
    _fetchActivities();
  }

  @override
  void dispose() { _tab.dispose(); super.dispose(); }

  Future<void> _fetchMembers() async {
    try {
      final list = await ClubApiService.fetchMembers(widget.club.id);
      setState(() { _members = list; _loadingMem = false; });
    } catch (_) { setState(() => _loadingMem = false); }
  }

  Future<void> _fetchActivities() async {
    try {
      final list = await ClubApiService.fetchClubActivities(widget.club.id);
      setState(() { _activities = list; _loadingAct = false; });
    } catch (_) { setState(() => _loadingAct = false); }
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: const Color(0xFFF5F5F5),
    appBar: AppBar(
      backgroundColor: _blue,
      iconTheme: const IconThemeData(color: Colors.white),
      title: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Quản lý CLB',
              style: TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.bold)),
          Text(widget.club.name,
              style: const TextStyle(
                  color: Colors.white70, fontSize: 12)),
        ],
      ),
      bottom: TabBar(
        controller: _tab,
        indicatorColor: _orange,
        labelColor: Colors.white,
        unselectedLabelColor: Colors.white54,
        tabs: const [
          Tab(text: 'Tổng quan'),
          Tab(text: 'Thành viên'),
          Tab(text: 'Sự kiện'),
        ],
      ),
    ),
    body: TabBarView(
      controller: _tab,
      children: [
        _buildOverviewTab(),
        _buildMembersTab(),
        _buildActivitiesTab(),
      ],
    ),
  );

  // ── Tab: Tổng quan ─────────────────────────────────────────────────────────

  Widget _buildOverviewTab() {
    final pending = _members
        .where((m) => m.status == ClubMemberStatus.pending)
        .length;
    final active = _members
        .where((m) => m.status == ClubMemberStatus.active)
        .length;
    final upcoming = _activities
        .where((a) => a.status == ClubActivityStatus.scheduled)
        .length;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(children: [

        // Stat cards
        Row(children: [
          _ManageStatCard(
              icon: Icons.group,
              value: '$active',
              label: 'Thành viên',
              color: _blue),
          const SizedBox(width: 10),
          _ManageStatCard(
              icon: Icons.hourglass_top,
              value: '$pending',
              label: 'Chờ duyệt',
              color: _orange),
          const SizedBox(width: 10),
          _ManageStatCard(
              icon: Icons.event,
              value: '$upcoming',
              label: 'Sự kiện tới',
              color: const Color(0xFF2E7D32)),
        ]),
        const SizedBox(height: 20),

        // Quick actions
        SectionHeader(title: 'Thao tác nhanh', color: _blue),
        const SizedBox(height: 12),
        _QuickAction(
          icon: Icons.person_add,
          label: 'Duyệt thành viên mới',
          badge: pending > 0 ? '$pending' : null,
          onTap: () => _tab.animateTo(1),
        ),
        const SizedBox(height: 8),
        _QuickAction(
          icon: Icons.add_circle_outline,
          label: 'Tạo sự kiện mới',
          onTap: _showCreateActivityDialog,
        ),
        const SizedBox(height: 8),
        _QuickAction(
          icon: Icons.campaign,
          label: 'Bật / tắt tuyển thành viên',
          onTap: _toggleRecruiting,
        ),
        const SizedBox(height: 8),
        _QuickAction(
          icon: Icons.info_outline,
          label: 'Chỉnh sửa thông tin CLB',
          onTap: _showEditInfoDialog,
        ),
      ]),
    );
  }

  // ── Tab: Thành viên ────────────────────────────────────────────────────────

  Widget _buildMembersTab() {
    if (_loadingMem) {
      return const Center(child: CircularProgressIndicator(color: _orange));
    }

    final pending  = _members.where((m) => m.status == ClubMemberStatus.pending).toList();
    final active   = _members.where((m) => m.status == ClubMemberStatus.active).toList();

    return RefreshIndicator(
      color: _orange,
      onRefresh: _fetchMembers,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [

          // ── Chờ duyệt ─────────────────────────────────────────────────
          if (pending.isNotEmpty) ...[
            SectionHeader(
                title: 'Chờ duyệt (${pending.length})',
                color: _orange),
            const SizedBox(height: 8),
            ...pending.map((m) => MemberTile(
              member: m,
              showApprove: true,
              onApprove: () => _approve(m),
              onReject: () => _reject(m),
            )),
            const SizedBox(height: 16),
          ],

          // ── Thành viên active ──────────────────────────────────────────
          SectionHeader(
              title: 'Thành viên (${active.length})',
              color: _blue),
          const SizedBox(height: 8),
          ...active.map((m) => _MemberTileWithActions(
            member: m,
            onChangeRole: () => _showChangeRoleDialog(m),
            onExpel: () => _expelMember(m),
          )),
        ],
      ),
    );
  }

  // ── Tab: Sự kiện ───────────────────────────────────────────────────────────

  Widget _buildActivitiesTab() {
    if (_loadingAct) {
      return const Center(child: CircularProgressIndicator(color: _orange));
    }
    return RefreshIndicator(
      color: _orange,
      onRefresh: _fetchActivities,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Nút tạo sự kiện
          ElevatedButton.icon(
            onPressed: _showCreateActivityDialog,
            icon: const Icon(Icons.add),
            label: const Text('Tạo sự kiện mới'),
            style: ElevatedButton.styleFrom(
              backgroundColor: _orange,
              foregroundColor: Colors.white,
              minimumSize: const Size.fromHeight(46),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14)),
            ),
          ),
          const SizedBox(height: 16),

          if (_activities.isEmpty)
            const Center(
              child: Text('Chưa có sự kiện nào',
                  style: TextStyle(color: Colors.grey)),
            )
          else
            ...(_activities.map((a) => Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: ActivityCard(activity: a),
            ))),
        ],
      ),
    );
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  Future<void> _approve(ClubMemberDetail m) async {
    try {
      await ClubApiService.approveMember(m.id);
      _fetchMembers();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Đã duyệt ${m.studentName}'),
          backgroundColor: const Color(0xFF2E7D32),
        ));
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Lỗi: $e'), backgroundColor: Colors.red));
    }
  }

  Future<void> _reject(ClubMemberDetail m) async {
    try {
      await ClubApiService.rejectMember(m.id);
      _fetchMembers();
    } catch (_) {}
  }

  Future<void> _expelMember(ClubMemberDetail m) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Khai trừ thành viên'),
        content: Text('Bạn có chắc muốn khai trừ ${m.studentName}?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Huỷ')),
          TextButton(
              onPressed: () => Navigator.pop(context, true),
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('Khai trừ')),
        ],
      ),
    );
    if (ok != true) return;
    // TODO: gọi API expel
    _fetchMembers();
  }

  void _showChangeRoleDialog(ClubMemberDetail m) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: Text('Đổi vai trò — ${m.studentName}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: ClubMemberRole.values.map((r) => ListTile(
            leading: Radio<ClubMemberRole>(
              value: r,
              groupValue: m.role,
              activeColor: _orange,
              onChanged: (_) => Navigator.pop(context, r),
            ),
            title: Text(r.label),
          )).toList(),
        ),
      ),
    );
    // TODO: gọi API đổi role
  }

  void _showCreateActivityDialog() {
    final titleCtrl = TextEditingController();
    final locCtrl   = TextEditingController();
    final descCtrl  = TextEditingController();

    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Tạo sự kiện mới'),
        content: SingleChildScrollView(
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            TextField(
              controller: titleCtrl,
              decoration: const InputDecoration(
                  labelText: 'Tên sự kiện *',
                  border: OutlineInputBorder()),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: locCtrl,
              decoration: const InputDecoration(
                  labelText: 'Địa điểm',
                  border: OutlineInputBorder()),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: descCtrl,
              maxLines: 3,
              decoration: const InputDecoration(
                  labelText: 'Mô tả',
                  border: OutlineInputBorder()),
            ),
          ]),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Huỷ')),
          ElevatedButton(
            onPressed: () {
              // TODO: gọi API tạo sự kiện
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                content: Text('Đã tạo sự kiện (cần kết nối API)'),
                backgroundColor: _orange,
              ));
            },
            style: ElevatedButton.styleFrom(backgroundColor: _orange),
            child: const Text('Tạo',
                style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _showEditInfoDialog() {
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
      content: Text('Tính năng chỉnh sửa CLB — cần kết nối API'),
      backgroundColor: _blue,
    ));
  }

  void _toggleRecruiting() {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(widget.club.isRecruiting
          ? 'Đã tắt tuyển thành viên'
          : 'Đã bật tuyển thành viên'),
      backgroundColor: _orange,
    ));
    // TODO: gọi API toggle recruiting
  }
}

// ════════════════════════════════════════════════════════════════════════════
// LOCAL WIDGETS
// ════════════════════════════════════════════════════════════════════════════

class _ManageStatCard extends StatelessWidget {
  final IconData icon;
  final String   value;
  final String   label;
  final Color    color;
  const _ManageStatCard({
    required this.icon, required this.value,
    required this.label, required this.color});

  @override
  Widget build(BuildContext context) => Expanded(
    child: Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8, offset: const Offset(0, 3))],
      ),
      child: Column(children: [
        Icon(icon, color: color, size: 24),
        const SizedBox(height: 6),
        Text(value,
            style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 22, color: color)),
        const SizedBox(height: 2),
        Text(label,
            style: const TextStyle(
                fontSize: 11, color: Colors.grey),
            textAlign: TextAlign.center),
      ]),
    ),
  );
}

class _QuickAction extends StatelessWidget {
  final IconData icon;
  final String   label;
  final String?  badge;
  final VoidCallback onTap;

  const _QuickAction({
    required this.icon,
    required this.label,
    required this.onTap,
    this.badge,
  });

  @override
  Widget build(BuildContext context) => GestureDetector(
    onTap: onTap,
    child: Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 6, offset: const Offset(0, 2))],
      ),
      child: Row(children: [
        Container(
          width: 40, height: 40,
          decoration: BoxDecoration(
            color: _orange.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Stack(children: [
            Center(child: Icon(icon, color: _orange, size: 20)),
            if (badge != null)
              Positioned(
                top: 2, right: 2,
                child: Container(
                  width: 16, height: 16,
                  decoration: const BoxDecoration(
                      color: Colors.red, shape: BoxShape.circle),
                  child: Center(
                    child: Text(badge!,
                        style: const TextStyle(
                            color: Colors.white,
                            fontSize: 9,
                            fontWeight: FontWeight.bold)),
                  ),
                ),
              ),
          ]),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Text(label,
              style: const TextStyle(
                  fontWeight: FontWeight.w500,
                  fontSize: 14,
                  color: Color(0xFF1A1A2E))),
        ),
        const Icon(Icons.chevron_right, color: Colors.grey),
      ]),
    ),
  );
}

class _MemberTileWithActions extends StatelessWidget {
  final ClubMemberDetail member;
  final VoidCallback onChangeRole;
  final VoidCallback onExpel;

  const _MemberTileWithActions({
    required this.member,
    required this.onChangeRole,
    required this.onExpel,
  });

  @override
  Widget build(BuildContext context) {
    final m = member;
    final initials = m.studentName.trim().split(' ')
        .map((w) => w[0]).take(2).join().toUpperCase();

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 6, offset: const Offset(0, 2))],
      ),
      child: Row(children: [
        m.avatarUrl != null
            ? CircleAvatar(
            radius: 22,
            backgroundImage: NetworkImage(m.avatarUrl!))
            : CircleAvatar(
            radius: 22,
            backgroundColor: _orange.withOpacity(0.15),
            child: Text(initials,
                style: const TextStyle(
                    color: _orange, fontWeight: FontWeight.bold))),
        const SizedBox(width: 12),
        Expanded(child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(m.studentName,
                style: const TextStyle(
                    fontWeight: FontWeight.w600, fontSize: 13)),
            const SizedBox(height: 2),
            Row(children: [
              Text(m.studentCode,
                  style: const TextStyle(
                      fontSize: 11, color: Colors.grey)),
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 6, vertical: 1),
                decoration: BoxDecoration(
                  color: _orange.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(m.role.label,
                    style: const TextStyle(
                        fontSize: 10,
                        color: _orange,
                        fontWeight: FontWeight.w600)),
              ),
            ]),
          ],
        )),

        // Menu actions
        PopupMenuButton<String>(
          icon: const Icon(Icons.more_vert, color: Colors.grey),
          onSelected: (v) {
            if (v == 'role')  onChangeRole();
            if (v == 'expel') onExpel();
          },
          itemBuilder: (_) => [
            const PopupMenuItem(
              value: 'role',
              child: Row(children: [
                Icon(Icons.swap_horiz, color: _orange, size: 18),
                SizedBox(width: 8),
                Text('Đổi vai trò'),
              ]),
            ),
            const PopupMenuItem(
              value: 'expel',
              child: Row(children: [
                Icon(Icons.remove_circle, color: Colors.red, size: 18),
                SizedBox(width: 8),
                Text('Khai trừ', style: TextStyle(color: Colors.red)),
              ]),
            ),
          ],
        ),
      ]),
    );
  }
}