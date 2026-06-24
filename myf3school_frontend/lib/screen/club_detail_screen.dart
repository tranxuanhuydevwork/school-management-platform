// lib/screen/club/club_detail_screen.dart
import 'package:flutter/material.dart';

import '../model/ClubModel.dart';
import '../widget/club_page/club_activity_card.dart';
import '../widget/club_page/club_header.dart';
import '../widget/club_page/club_member_tile.dart';
import 'club_manage_screen.dart';


// ignore_for_file: use_build_context_synchronously

const _orange = Color(0xFFF37021);
const _blue   = Color(0xFF003A8F);

class ClubDetailScreen extends StatefulWidget {
  final ClubSummary club;
  const ClubDetailScreen({super.key, required this.club});

  @override
  State<ClubDetailScreen> createState() => _ClubDetailScreenState();
}

class _ClubDetailScreenState extends State<ClubDetailScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tab;
  late ClubSummary   _club;

  List<ClubActivity>       _activities = [];
  List<ClubMemberDetail>   _members    = [];
  bool _loadingAct = true;
  bool _loadingMem = true;

  @override
  void initState() {
    super.initState();
    _club = widget.club;
    _tab  = TabController(length: _club.isLeader ? 3 : 2, vsync: this);
    _fetchActivities();
    if (_club.isLeader) _fetchMembers();
  }

  @override
  void dispose() { _tab.dispose(); super.dispose(); }

  Future<void> _fetchActivities() async {
    try {
      final list = await ClubApiService.fetchClubActivities(_club.id);
      setState(() { _activities = list; _loadingAct = false; });
    } catch (_) { setState(() => _loadingAct = false); }
  }

  Future<void> _fetchMembers() async {
    try {
      final list = await ClubApiService.fetchMembers(_club.id);
      setState(() { _members = list; _loadingMem = false; });
    } catch (_) { setState(() => _loadingMem = false); }
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: NestedScrollView(
        headerSliverBuilder: (_, __) => [_buildSliverHeader()],
        body: Column(children: [
          _buildTabBar(),
          Expanded(
            child: TabBarView(
              controller: _tab,
              children: [
                _buildInfoTab(),
                _buildActivitiesTab(),
                if (_club.isLeader) _buildMembersTab(),
              ],
            ),
          ),
        ]),
      ),
    );
  }

  // ── Sliver Header ──────────────────────────────────────────────────────────

  SliverAppBar _buildSliverHeader() {
    return SliverAppBar(
      expandedHeight: 200,
      pinned: true,
      backgroundColor: _orange,
      leading: IconButton(
        icon: const Icon(Icons.arrow_back, color: Colors.white),
        onPressed: () => Navigator.pop(context),
      ),
      actions: [
        if (_club.isLeader)
          IconButton(
            icon: const Icon(Icons.manage_accounts, color: Colors.white),
            tooltip: 'Quản lý CLB',
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (_) => ClubManageScreen(club: _club)),
            ),
          ),
      ],
      flexibleSpace: FlexibleSpaceBar(
        background: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [_orange, Color(0xFFE85D04)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: SafeArea(
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const SizedBox(height: 40),
                  Container(
                    width: 72, height: 72,
                    decoration: BoxDecoration(
                      color: Colors.white24,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: _club.logoUrl != null
                        ? ClipRRect(
                        borderRadius: BorderRadius.circular(20),
                        child: Image.network(_club.logoUrl!, fit: BoxFit.cover))
                        : const Icon(Icons.groups, color: Colors.white, size: 36),
                  ),
                  const SizedBox(height: 10),
                  Text(_club.name,
                      style: const TextStyle(
                          color: Colors.white,
                          fontSize: 20,
                          fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  _StatusRow(club: _club),
                ]),
          ),
        ),
      ),
    );
  }

  // ── Tab Bar ────────────────────────────────────────────────────────────────

  Widget _buildTabBar() => Container(
    color: Colors.white,
    child: TabBar(
      controller: _tab,
      indicatorColor: _orange,
      labelColor: _orange,
      unselectedLabelColor: Colors.grey,
      tabs: [
        const Tab(text: 'Giới thiệu'),
        const Tab(text: 'Sự kiện'),
        if (_club.isLeader) const Tab(text: 'Thành viên'),
      ],
    ),
  );

  // ── Tab: Giới thiệu ────────────────────────────────────────────────────────

  Widget _buildInfoTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(children: [

        // Stats row
        Row(children: [
          _StatCard(
              icon: Icons.group,
              value: '${_club.memberCount}',
              label: 'Thành viên'),
          const SizedBox(width: 10),
          _StatCard(
              icon: Icons.event,
              value: '${_club.upcomingEvents}',
              label: 'Sự kiện'),
          const SizedBox(width: 10),
          _StatCard(
              icon: Icons.star,
              value: _club.isRecruiting ? 'Mở' : 'Đóng',
              label: 'Tuyển thành viên',
              valueColor: _club.isRecruiting
                  ? const Color(0xFF2E7D32)
                  : Colors.grey),
        ]),
        const SizedBox(height: 16),

        // Mô tả
        _InfoCard(
          title: 'Giới thiệu',
          child: Text(
            _club.description ?? 'Chưa có thông tin mô tả.',
            style: const TextStyle(
                fontSize: 14, color: Color(0xFF444444), height: 1.6),
          ),
        ),
        const SizedBox(height: 12),

        // Action button
        _buildJoinButton(),
      ]),
    );
  }

  Widget _buildJoinButton() {
    // if (_club.isMember) {
    //   return OutlinedButton.icon(
    //     onPressed: _leaveClub,
    //     icon: const Icon(Icons.exit_to_app, color: Colors.red),
    //     label: const Text('Rời khỏi CLB',
    //         style: TextStyle(color: Colors.red)),
    //     style: OutlinedButton.styleFrom(
    //       side: const BorderSide(color: Colors.red),
    //       minimumSize: const Size.fromHeight(48),
    //       shape: RoundedRectangleBorder(
    //           borderRadius: BorderRadius.circular(14)),
    //     ),
    //   );
    // }
    if (_club.isPending) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: _orange.withOpacity(0.1),
          borderRadius: BorderRadius.circular(14),
        ),
        child: const Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.hourglass_top, color: _orange, size: 18),
            SizedBox(width: 8),
            Text('Đang chờ duyệt',
                style: TextStyle(
                    color: _orange, fontWeight: FontWeight.bold)),
          ],
        ),
      );
    }
    if (_club.isRecruiting) {
      return ElevatedButton.icon(
        onPressed: _joinClub,
        icon: const Icon(Icons.add_circle_outline),
        label: const Text('Đăng ký tham gia'),
        style: ElevatedButton.styleFrom(
          backgroundColor: _orange,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(48),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14)),
        ),
      );
    }
    return const SizedBox.shrink();
  }

  // ── Tab: Sự kiện ───────────────────────────────────────────────────────────

  Widget _buildActivitiesTab() {
    if (_loadingAct) {
      return const Center(
          child: CircularProgressIndicator(color: _orange));
    }
    if (_activities.isEmpty) {
      return const Center(
        child: Text('Chưa có sự kiện nào',
            style: TextStyle(color: Colors.grey)),
      );
    }
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: _activities.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (_, i) => ActivityCard(activity: _activities[i]),
    );
  }

  // ── Tab: Thành viên (chỉ leader) ───────────────────────────────────────────

  Widget _buildMembersTab() {
    if (_loadingMem) {
      return const Center(
          child: CircularProgressIndicator(color: _orange));
    }
    final pending = _members
        .where((m) => m.status == ClubMemberStatus.pending)
        .toList();
    final active = _members
        .where((m) => m.status == ClubMemberStatus.active)
        .toList();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
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
        SectionHeader(
            title: 'Thành viên (${active.length})', color: _blue),
        const SizedBox(height: 8),
        ...active.map((m) => MemberTile(member: m)),
      ],
    );
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  Future<void> _joinClub() async {
    try {
      await ClubApiService.joinClub(_club.id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
          content: Text('Đã gửi đơn tham gia, chờ duyệt.'),
          backgroundColor: _orange,
        ));

        // ✅ Sửa: dùng đúng fields của ClubSummary (không có category, isRecruiting)
        // Sau khi join → myStatus = pending, canJoin = false
        setState(() => _club = ClubSummary(
          id:              _club.id,
          code:            _club.code,
          name:            _club.name,
          description:     _club.description,
          logoUrl:         _club.logoUrl,
          meetingLocation: _club.meetingLocation,
          maxMembers:      _club.maxMembers,
          foundedDate:     _club.foundedDate,
          status:          _club.status,
          advisorName:     _club.advisorName,
          presidentName:   _club.presidentName,
          memberCount:     _club.memberCount,
          upcomingEvents:  _club.upcomingEvents,
          myStatus:        ClubMemberStatus.pending, // ← đổi sang pending
          myRole:          _club.myRole,
          canJoin:         false,                    // ← đã gửi đơn, không cho join lại
        ));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Lỗi: $e'),
          backgroundColor: Colors.red,
        ));
      }
    }
  }

  Future<void> _leaveClub() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Rời khỏi CLB'),
        content: Text('Bạn có chắc muốn rời ${_club.name}?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Huỷ')),
          TextButton(
              onPressed: () => Navigator.pop(context, true),
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('Rời CLB')),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await ClubApiService.leaveClub(_club.id);
      if (mounted) Navigator.pop(context);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Lỗi: $e'), backgroundColor: Colors.red));
    }
  }

  Future<void> _approve(ClubMemberDetail m) async {
    try {
      await ClubApiService.approveMember(m.id);
      _fetchMembers();
    } catch (_) {}
  }

  Future<void> _reject(ClubMemberDetail m) async {
    try {
      await ClubApiService.rejectMember(m.id);
      _fetchMembers();
    } catch (_) {}
  }
}

// ════════════════════════════════════════════════════════════════════════════
// REUSABLE WIDGETS (dùng trong detail + manage)
// ════════════════════════════════════════════════════════════════════════════

class _StatusRow extends StatelessWidget {
  final ClubSummary club;
  const _StatusRow({required this.club});

  @override
  Widget build(BuildContext context) => Row(
    mainAxisAlignment: MainAxisAlignment.center,
    children: [
      if (club.isMember)
        _badge('Đã tham gia', const Color(0xFF2E7D32)),
      if (club.isPending)
        _badge('Chờ duyệt', Colors.white54),
      if (club.isRecruiting && !club.isMember && !club.isPending)
        _badge('Đang tuyển', Colors.white),
    ],
  );

  Widget _badge(String t, Color c) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
    decoration: BoxDecoration(
      color: c.withOpacity(0.25),
      borderRadius: BorderRadius.circular(12),
      border: Border.all(color: c.withOpacity(0.6)),
    ),
    child: Text(t,
        style: TextStyle(
            color: c, fontSize: 11, fontWeight: FontWeight.w600)),
  );
}

class _StatCard extends StatelessWidget {
  final IconData icon;
  final String   value;
  final String   label;
  final Color?   valueColor;

  const _StatCard({
    required this.icon,
    required this.value,
    required this.label,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) => Expanded(
    child: Container(
      padding: const EdgeInsets.symmetric(vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8, offset: const Offset(0, 3))],
      ),
      child: Column(children: [
        Icon(icon, color: _orange, size: 22),
        const SizedBox(height: 4),
        Text(value,
            style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: valueColor ?? const Color(0xFF1A1A2E))),
        const SizedBox(height: 2),
        Text(label,
            style: const TextStyle(fontSize: 10, color: Colors.grey),
            textAlign: TextAlign.center),
      ]),
    ),
  );
}

class _InfoCard extends StatelessWidget {
  final String title;
  final Widget child;
  const _InfoCard({required this.title, required this.child});

  @override
  Widget build(BuildContext context) => Container(
    width: double.infinity,
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(16),
      boxShadow: [BoxShadow(
          color: Colors.black.withOpacity(0.05),
          blurRadius: 8, offset: const Offset(0, 3))],
    ),
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text(title,
          style: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 14,
              color: Color(0xFF1A1A2E))),
      const Divider(height: 16),
      child,
    ]),
  );
}

// Shared widgets moved to club_widgets.dart