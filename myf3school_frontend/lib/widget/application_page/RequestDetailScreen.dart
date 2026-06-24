import 'package:flutter/material.dart';
import '../../model/RequestForm.dart';

/// Xem chi tiết 1 đơn — push từ RequestCard
class RequestDetailScreen extends StatelessWidget {
  final RequestForm request;
  const RequestDetailScreen({super.key, required this.request});

  @override
  Widget build(BuildContext context) {
    final type   = request.type;
    final status = request.status;

    return Scaffold(
      backgroundColor: const Color(0xffF5F7FA),
      appBar: AppBar(
        backgroundColor: Colors.orange,
        foregroundColor: Colors.white,
        title: const Text('Chi tiết đơn',
            style: TextStyle(fontWeight: FontWeight.bold)),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ── Card header ──────────────────────────────
            _card(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: type.color.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Icon(type.icon, color: type.color, size: 24),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(type.label,
                                style: TextStyle(
                                    color: type.color,
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600)),
                            Text(request.title,
                                style: const TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold)),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  const Divider(),
                  const SizedBox(height: 12),
                  _row('Trạng thái', _StatusBadge(status: status)),
                  const SizedBox(height: 10),
                  _row('Ngày tạo',
                      Text(_fmt(request.createdAt),
                          style: const TextStyle(fontSize: 14))),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // ── Nội dung ─────────────────────────────────
            _section(
              title: 'Nội dung đơn',
              child: Text(request.description,
                  style: const TextStyle(fontSize: 14, height: 1.6)),
            ),

            // ── Lý do từ chối ────────────────────────────
            if (request.rejectedReason != null) ...[
              const SizedBox(height: 16),
              _section(
                title: 'Lý do từ chối',
                titleColor: const Color(0xFFEF4444),
                child: Text(request.rejectedReason!,
                    style: const TextStyle(
                        fontSize: 14,
                        height: 1.6,
                        color: Color(0xFFEF4444))),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _card({required Widget child}) => Container(
    width: double.infinity,
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(16),
      border: Border.all(color: Colors.grey.shade200),
      boxShadow: [
        BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 6,
            offset: const Offset(0, 2))
      ],
    ),
    child: child,
  );

  Widget _section(
      {required String title,
        required Widget child,
        Color? titleColor}) =>
      _card(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title,
                style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: titleColor ?? const Color(0xFF1A1A2E))),
            const SizedBox(height: 10),
            child,
          ],
        ),
      );

  Widget _row(String label, Widget value) => Row(
    mainAxisAlignment: MainAxisAlignment.spaceBetween,
    children: [
      Text(label,
          style:
          const TextStyle(fontSize: 13, color: Colors.grey)),
      value,
    ],
  );

  String _fmt(DateTime dt) =>
      '${dt.day.toString().padLeft(2, '0')}/'
          '${dt.month.toString().padLeft(2, '0')}/'
          '${dt.year}';
}

class _StatusBadge extends StatelessWidget {
  final RequestStatus status;
  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: status.color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: status.color.withOpacity(0.4)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(status.icon, size: 12, color: status.color),
          const SizedBox(width: 4),
          Text(status.label,
              style: TextStyle(
                  fontSize: 12,
                  color: status.color,
                  fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}