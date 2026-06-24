// lib/screen/club/club_event_screen.dart
import 'package:flutter/material.dart';
import '../../model/ClubModel.dart';
import '../widget/club_page/club_activity_card.dart';
import 'club_detail_screen.dart' show ActivityCard;

const _orange = Color(0xFFF37021);

class ClubEventScreen extends StatefulWidget {
  final bool embedded;
  const ClubEventScreen({super.key, this.embedded = false});

  @override
  State<ClubEventScreen> createState() => _ClubEventScreenState();
}

class _ClubEventScreenState extends State<ClubEventScreen> {
  List<ClubActivity>  _all      = [];
  List<ClubActivity>  _filtered = [];
  ClubActivityStatus? _filter;
  bool    _loading = true;
  String? _error;

  @override
  void initState() { super.initState(); _fetch(); }

  Future<void> _fetch() async {
    setState(() { _loading = true; _error = null; });
    try {
      final list = await ClubApiService.fetchAllActivities();
      setState(() { _all = list; _filtered = _applyFilter(list); _loading = false; });
    } catch (e) {
      setState(() { _error = e.toString(); _loading = false; });
    }
  }

  List<ClubActivity> _applyFilter(List<ClubActivity> src) =>
      _filter == null ? src : src.where((a) => a.status == _filter).toList();

  void _setFilter(ClubActivityStatus? f) =>
      setState(() { _filter = f; _filtered = _applyFilter(_all); });

  @override
  Widget build(BuildContext context) {
    if (widget.embedded) return _buildContent();
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        backgroundColor: _orange,
        title: const Text('Sự kiện',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: _buildContent(),
    );
  }

  Widget _buildContent() => Column(children: [
    Container(
      color: Colors.white,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: Row(children: [
          _FilterChip(label: 'Tất cả', selected: _filter == null,
              onTap: () => _setFilter(null)),
          const SizedBox(width: 8),
          ...ClubActivityStatus.values.map((s) => Padding(
            padding: const EdgeInsets.only(right: 8),
            child: _FilterChip(
              label: s.label, selected: _filter == s,
              color: _statusColor(s), onTap: () => _setFilter(s),
            ),
          )),
        ]),
      ),
    ),
    Expanded(
      child: _loading
          ? const Center(child: CircularProgressIndicator(color: _orange))
          : _error != null ? _buildError() : _buildList(),
    ),
  ]);

  Widget _buildList() {
    if (_filtered.isEmpty) return const Center(
        child: Text('Không có sự kiện nào', style: TextStyle(color: Colors.grey)));
    return RefreshIndicator(
      color: _orange, onRefresh: _fetch,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _filtered.length,
        separatorBuilder: (_, __) => const SizedBox(height: 10),
        itemBuilder: (_, i) => ActivityCard(activity: _filtered[i]),
      ),
    );
  }

  Widget _buildError() => Center(
    child: Column(mainAxisSize: MainAxisSize.min, children: [
      const Icon(Icons.wifi_off, color: _orange, size: 48),
      const SizedBox(height: 12),
      Text(_error!, style: const TextStyle(color: Colors.grey)),
      const SizedBox(height: 16),
      ElevatedButton.icon(onPressed: _fetch,
          icon: const Icon(Icons.refresh), label: const Text('Thử lại'),
          style: ElevatedButton.styleFrom(
              backgroundColor: _orange, foregroundColor: Colors.white)),
    ]),
  );

  Color _statusColor(ClubActivityStatus s) {
    switch (s) {
      case ClubActivityStatus.scheduled:  return _orange;
      case ClubActivityStatus.inProgress: return const Color(0xFF1565C0);
      case ClubActivityStatus.completed:  return const Color(0xFF2E7D32);
      case ClubActivityStatus.cancelled:  return Colors.grey;
    }
  }
}

class _FilterChip extends StatelessWidget {
  final String label; final bool selected;
  final Color color; final VoidCallback onTap;
  const _FilterChip({required this.label, required this.selected,
    required this.onTap, this.color = _orange});

  @override
  Widget build(BuildContext context) => GestureDetector(
    onTap: onTap,
    child: AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
      decoration: BoxDecoration(
        color: selected ? color : color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(label, style: TextStyle(
          fontSize: 12, fontWeight: FontWeight.w600,
          color: selected ? Colors.white : color)),
    ),
  );
}