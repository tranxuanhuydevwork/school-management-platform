import 'package:flutter/material.dart';
import '../../model/RequestForm.dart';

/// 4 ô đếm số đơn theo trạng thái — bấm để lọc
class RequestStatusBar extends StatelessWidget {
  final Map<RequestStatus, int> counts;
  final RequestStatus? selected;
  final ValueChanged<RequestStatus?> onTap;

  const RequestStatusBar({
    super.key,
    required this.counts,
    required this.selected,
    required this.onTap,
  });

  static const _items = [
    (RequestStatus.draft,    'Nháp'),
    (RequestStatus.pending,  'Chờ duyệt'),
    (RequestStatus.approved, 'Đã duyệt'),
    (RequestStatus.rejected, 'Từ chối'),
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.orange,
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
      child: Row(
        children: _items.map((item) {
          final (status, label) = item;
          final count  = counts[status] ?? 0;
          final active = selected == status;

          return Expanded(
            child: GestureDetector(
              onTap: () => onTap(active ? null : status),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 180),
                margin: const EdgeInsets.symmetric(horizontal: 4),
                padding: const EdgeInsets.symmetric(vertical: 10),
                decoration: BoxDecoration(
                  color: active ? Colors.white : Colors.white.withOpacity(0.25),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      '$count',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: active ? Colors.orange : Colors.white,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      label,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.w600,
                        color: active ? Colors.orange : Colors.white.withOpacity(0.9),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}