// lib/screen/club/club_screen.dart
import 'dart:async';
import 'package:flutter/material.dart';

import '../../widget/bottom_navigation_widget.dart';
import '../../model/ClubModel.dart';
import 'club_detail_screen.dart';
import 'club_event_screen.dart';

// ignore_for_file: use_build_context_synchronously

const _orange = Color(0xFFF37021);
const _blue   = Color(0xFF003A8F);

const _categories = [
  {'id': 0, 'name': 'Tất cả',     'icon': Icons.apps},
  {'id': 1, 'name': 'Công nghệ',  'icon': Icons.code},
  {'id': 2, 'name': 'Âm nhạc',    'icon': Icons.music_note},
  {'id': 3, 'name': 'Thể thao',   'icon': Icons.sports_soccer},
  {'id': 4, 'name': 'Nghệ thuật', 'icon': Icons.palette},
  {'id': 5, 'name': 'Khác',       'icon': Icons.more_horiz},
];

class ClubListScreen extends StatefulWidget {
  const ClubListScreen({super.key});

  @override
  State<ClubListScreen> createState() => _ClubListScreenState();
}

class _ClubListScreenState extends State<ClubListScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tab;

  List<ClubSummary> _allClubs = [];
  List<ClubSummary> _myClubs  = [];
  bool    _loading = true;
  String? _error;

  String _search = '';
  Timer? _debounce;
  final  _searchCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 3, vsync: this);
    _fetch();
  }

  @override
  void dispose() {
    _tab.dispose();
    _searchCtrl.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  Future<void> _fetch() async {
    setState(() { _loading = true; _error = null; });
    try {
      final results = await Future.wait([
        ClubApiService.fetchClubs(search: _search.isEmpty ? null : _search),
        ClubApiService.fetchMyClubs(),
      ]);
      setState(() {
        _allClubs = results[0] as List<ClubSummary>;
        _myClubs  = results[1] as List<ClubSummary>;
        _loading  = false;
      });
    } catch (e) {
      setState(() { _error = e.toString(); _loading = false; });
    }
  }

  void _onSearch(String v) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 500), () {
      setState(() => _search = v);
      _fetch();
    });
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      // ✅ Sửa: BottomNav đặt ở đây, KHÔNG đặt bên trong _buildHeader()
      bottomNavigationBar: const BottomNav(currentIndex: 2),
      body: Column(children: [
        _buildHeader(),
        Expanded(
          child: _loading
              ? const Center(child: CircularProgressIndicator(color: _orange))
              : _error != null
              ? _buildError()
              : _buildBody(),
        ),
      ]),
    );
  }

  // ── Header ─────────────────────────────────────────────────────────────────
  // ✅ Sửa: bỏ Scaffold + BottomNav lồng bên trong — chỉ giữ Container + SafeArea

  Widget _buildHeader() {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [_orange, Color(0xFFE85D04)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.vertical(bottom: Radius.circular(28)),
      ),
      child: SafeArea(
        bottom: false,
        child: Column(children: [
          // Title
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
            child: Row(children: [
              const Icon(Icons.groups, color: Colors.white, size: 28),
              const SizedBox(width: 10),
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                const Text('Câu lạc bộ',
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.bold)),
                // ✅ Sửa: string interpolation dùng "" bên ngoài, '' bên trong
                Text(
                  "${_allClubs.where((c) => c.status == 'ACTIVE').length} CLB đang hoạt động",
                  style: const TextStyle(color: Colors.white70, fontSize: 12),
                ),
              ]),
            ]),
          ),
          const SizedBox(height: 12),

          // Search bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: TextField(
              controller: _searchCtrl,
              onChanged: _onSearch,
              decoration: InputDecoration(
                hintText: 'Tìm câu lạc bộ...',
                hintStyle: TextStyle(color: Colors.grey[400]),
                prefixIcon: const Icon(Icons.search, color: Colors.grey),
                suffixIcon: _search.isNotEmpty
                    ? IconButton(
                  icon: const Icon(Icons.clear, color: Colors.grey),
                  onPressed: () {
                    _searchCtrl.clear();
                    _onSearch('');
                  },
                )
                    : null,
                filled: true,
                fillColor: Colors.white,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: BorderSide.none,
                ),
                contentPadding: const EdgeInsets.symmetric(vertical: 0),
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Category chips — UI only
          SizedBox(
            height: 38,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _categories.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (_, i) {
                final cat        = _categories[i];
                const isSelected = false;
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  padding: const EdgeInsets.symmetric(
                      horizontal: 14, vertical: 6),
                  decoration: BoxDecoration(
                    color: isSelected ? Colors.white : Colors.white24,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(mainAxisSize: MainAxisSize.min, children: [
                    Icon(cat['icon'] as IconData,
                        size: 14,
                        color: isSelected ? _orange : Colors.white),
                    const SizedBox(width: 5),
                    Text(cat['name'] as String,
                        style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: isSelected ? _orange : Colors.white)),
                  ]),
                );
              },
            ),
          ),
          const SizedBox(height: 12),

          // Tabs
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            decoration: BoxDecoration(
              color: Colors.white24,
              borderRadius: BorderRadius.circular(14),
            ),
            child: TabBar(
              controller: _tab,
              indicator: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
              ),
              indicatorSize: TabBarIndicatorSize.tab,
              dividerColor: Colors.transparent,
              labelColor: _orange,
              unselectedLabelColor: Colors.white,
              labelStyle: const TextStyle(
                  fontWeight: FontWeight.bold, fontSize: 13),
              tabs: const [
                Tab(text: 'Tất cả CLB'),
                Tab(text: 'CLB của tôi'),
                Tab(text: 'Sự kiện'),
              ],
            ),
          ),
          const SizedBox(height: 12),
        ]),
      ),
    );
  }

  // ── Body ───────────────────────────────────────────────────────────────────

  Widget _buildBody() => TabBarView(
    controller: _tab,
    children: [
      _ClubListView(
        clubs:     _allClubs,
        onRefresh: _fetch,
        onTap:     _openDetail,
        onJoin:    _joinClub,
      ),
      _MyClubsView(
        clubs:     _myClubs,
        onRefresh: _fetch,
        onTap:     _openDetail,
      ),
      const ClubEventScreen(embedded: true),
    ],
  );

  Widget _buildError() => Center(
    child: Column(mainAxisSize: MainAxisSize.min, children: [
      const Icon(Icons.wifi_off, color: _orange, size: 48),
      const SizedBox(height: 12),
      Text(_error!, style: const TextStyle(color: Colors.grey)),
      const SizedBox(height: 16),
      ElevatedButton.icon(
        onPressed: _fetch,
        icon: const Icon(Icons.refresh),
        label: const Text('Thử lại'),
        style: ElevatedButton.styleFrom(
            backgroundColor: _orange, foregroundColor: Colors.white),
      ),
    ]),
  );

  void _openDetail(ClubSummary club) {
    Navigator.push(context,
        MaterialPageRoute(builder: (_) => ClubDetailScreen(club: club)));
  }

  Future<void> _joinClub(ClubSummary club) async {
    try {
      await ClubApiService.joinClub(club.id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('Đã gửi đơn tham gia ${club.name}'),
          backgroundColor: _orange,
        ));
        _fetch();
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
}

// ════════════════════════════════════════════════════════════════════════════
// SUB VIEWS
// ════════════════════════════════════════════════════════════════════════════

class _ClubListView extends StatelessWidget {
  final List<ClubSummary>                  clubs;
  final Future<void> Function()            onRefresh;
  final void Function(ClubSummary)         onTap;
  final Future<void> Function(ClubSummary) onJoin;

  const _ClubListView({
    required this.clubs,
    required this.onRefresh,
    required this.onTap,
    required this.onJoin,
  });

  @override
  Widget build(BuildContext context) {
    if (clubs.isEmpty) {
      return const Center(
        child: Text('Không tìm thấy câu lạc bộ nào',
            style: TextStyle(color: Colors.grey)),
      );
    }
    return RefreshIndicator(
      color: _orange,
      onRefresh: onRefresh,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: clubs.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (_, i) => _ClubCard(
          club:  clubs[i],
          onTap: () => onTap(clubs[i]),
          onJoin: () => onJoin(clubs[i]),
        ),
      ),
    );
  }
}

class _MyClubsView extends StatelessWidget {
  final List<ClubSummary>          clubs;
  final Future<void> Function()    onRefresh;
  final void Function(ClubSummary) onTap;

  const _MyClubsView({
    required this.clubs,
    required this.onRefresh,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    if (clubs.isEmpty) {
      return Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.group_off, color: Colors.grey[300], size: 64),
          const SizedBox(height: 12),
          const Text('Bạn chưa tham gia câu lạc bộ nào',
              style: TextStyle(color: Colors.grey)),
        ]),
      );
    }
    return RefreshIndicator(
      color: _orange,
      onRefresh: onRefresh,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: clubs.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (_, i) => _ClubCard(
          club:       clubs[i],
          onTap:      () => onTap(clubs[i]),
          showMyRole: true,
        ),
      ),
    );
  }
}

// ════════════════════════════════════════════════════════════════════════════
// CLUB CARD
// ════════════════════════════════════════════════════════════════════════════

class _ClubCard extends StatelessWidget {
  final ClubSummary   club;
  final VoidCallback  onTap;
  final VoidCallback? onJoin;
  final bool          showMyRole;

  const _ClubCard({
    required this.club,
    required this.onTap,
    this.onJoin,
    this.showMyRole = false,
  });

  Color _categoryColor(ClubSummary c) {
    final code = c.code.toLowerCase();
    if (code.contains('it') || code.contains('tech')) return const Color(0xFF1565C0);
    if (code.contains('music') || code.contains('am')) return const Color(0xFF6A1B9A);
    if (code.contains('sport') || code.contains('tt')) return const Color(0xFF2E7D32);
    if (code.contains('art')) return const Color(0xFFAD1457);
    return const Color(0xFF00695C);
  }

  IconData _categoryIcon(ClubSummary c) {
    final code = c.code.toLowerCase();
    if (code.contains('it') || code.contains('tech')) return Icons.code;
    if (code.contains('music') || code.contains('am')) return Icons.music_note;
    if (code.contains('sport') || code.contains('tt')) return Icons.sports_soccer;
    if (code.contains('art')) return Icons.palette;
    return Icons.groups;
  }

  @override
  Widget build(BuildContext context) {
    final c        = club;
    final catColor = _categoryColor(c);

    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.06),
            blurRadius: 12,
            offset: const Offset(0, 4),
          )],
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Row(children: [
              // Logo
              Container(
                width: 48, height: 48,
                decoration: BoxDecoration(
                  color: catColor.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: c.logoUrl != null
                    ? ClipRRect(
                    borderRadius: BorderRadius.circular(14),
                    child: Image.network(c.logoUrl!, fit: BoxFit.cover))
                    : Icon(_categoryIcon(c), color: catColor, size: 24),
              ),
              const SizedBox(width: 12),

              // Tên + code
              Expanded(child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(c.name,
                      style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 15,
                          color: Color(0xFF1A1A2E))),
                  const SizedBox(height: 2),
                  Row(children: [
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: catColor.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Text(c.code,
                          style: TextStyle(
                              fontSize: 11,
                              color: catColor,
                              fontWeight: FontWeight.w600)),
                    ),
                    if (showMyRole && c.myRole != null) ...[
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: _orange.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(c.myRole!.label,
                            style: const TextStyle(
                                fontSize: 11,
                                color: _orange,
                                fontWeight: FontWeight.w600)),
                      ),
                    ],
                  ]),
                ],
              )),

              _buildActionBadge(c),
            ]),
          ),

          if (c.description != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
              child: Text(
                c.description!,
                style: const TextStyle(
                    fontSize: 12.5, color: Color(0xFF555555), height: 1.4),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 10, 16, 14),
            child: Row(children: [
              const Icon(Icons.group, size: 14, color: Colors.grey),
              const SizedBox(width: 4),
              Text('${c.memberCount}',
                  style: const TextStyle(fontSize: 12, color: Colors.grey)),
              const SizedBox(width: 14),
              const Icon(Icons.event, size: 14, color: Colors.grey),
              const SizedBox(width: 4),
              Text('${c.upcomingEvents} sự kiện',
                  style: const TextStyle(fontSize: 12, color: Colors.grey)),
              if (c.meetingLocation != null) ...[
                const SizedBox(width: 14),
                const Icon(Icons.location_on, size: 14, color: Colors.grey),
                const SizedBox(width: 4),
                Expanded(
                  child: Text(c.meetingLocation!,
                      style: const TextStyle(
                          fontSize: 12, color: Colors.grey),
                      overflow: TextOverflow.ellipsis),
                ),
              ],
            ]),
          ),
        ]),
      ),
    );
  }

  Widget _buildActionBadge(ClubSummary c) {
    if (c.isMember) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: const Color(0xFF2E7D32).withOpacity(0.12),
          borderRadius: BorderRadius.circular(20),
        ),
        child: const Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.check_circle, size: 13, color: Color(0xFF2E7D32)),
          SizedBox(width: 4),
          Text('Đã tham gia',
              style: TextStyle(
                  fontSize: 11,
                  color: Color(0xFF2E7D32),
                  fontWeight: FontWeight.w600)),
        ]),
      );
    }
    if (c.isPending) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: _orange.withOpacity(0.12),
          borderRadius: BorderRadius.circular(20),
        ),
        child: const Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.hourglass_top, size: 13, color: _orange),
          SizedBox(width: 4),
          Text('Chờ duyệt',
              style: TextStyle(
                  fontSize: 11,
                  color: _orange,
                  fontWeight: FontWeight.w600)),
        ]),
      );
    }
    if (c.canJoin && onJoin != null) {
      return GestureDetector(
        onTap: onJoin,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
                colors: [_orange, Color(0xFFE85D04)]),
            borderRadius: BorderRadius.circular(20),
          ),
          child: const Text('Đăng ký',
              style: TextStyle(
                  fontSize: 11,
                  color: Colors.white,
                  fontWeight: FontWeight.bold)),
        ),
      );
    }
    return const SizedBox.shrink();
  }
}