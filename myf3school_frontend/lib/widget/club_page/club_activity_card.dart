import 'package:flutter/material.dart';
import '../../model/ClubModel.dart';

const _orange = Color(0xFFF37021);
class ActivityCard extends StatelessWidget {
  final ClubActivity activity;
  const ActivityCard({super.key, required this.activity});

  Color _statusColor(ClubActivityStatus s) {
    switch (s) {
      case ClubActivityStatus.scheduled:  return _orange;
      case ClubActivityStatus.inProgress: return const Color(0xFF1565C0);
      case ClubActivityStatus.completed:  return const Color(0xFF2E7D32);
      case ClubActivityStatus.cancelled:  return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    final a     = activity;
    final color = _statusColor(a.status);

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 3))],
        border: Border(left: BorderSide(color: color, width: 4)),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Expanded(
            child: Text(a.title,
                style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                    color: Color(0xFF1A1A2E))),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Text(a.status.label,
                style: TextStyle(
                    fontSize: 11,
                    color: color,
                    fontWeight: FontWeight.w600)),
          ),
        ]),
        const SizedBox(height: 6),

        Text(a.clubName,
            style: const TextStyle(
                fontSize: 12,
                color: _orange,
                fontWeight: FontWeight.w500)),
        const SizedBox(height: 6),

        Row(children: [
          const Icon(Icons.schedule, size: 13, color: Colors.grey),
          const SizedBox(width: 4),
          Text(
            a.startTime.length >= 16
                ? a.startTime.substring(0, 16).replaceAll('T', ' ')
                : a.startTime,
            style: const TextStyle(fontSize: 12, color: Colors.grey),
          ),
          if (a.location != null) ...[
            const SizedBox(width: 12),
            const Icon(Icons.location_on, size: 13, color: Colors.grey),
            const SizedBox(width: 4),
            Expanded(
              child: Text(a.location!,
                  style: const TextStyle(fontSize: 12, color: Colors.grey),
                  overflow: TextOverflow.ellipsis),
            ),
          ],
        ]),

        if (a.conductPoints > 0) ...[
          const SizedBox(height: 6),
          Row(children: [
            const Icon(Icons.stars, size: 13, color: _orange),
            const SizedBox(width: 4),
            Text('+${a.conductPoints} điểm hạnh kiểm',
                style: const TextStyle(
                    fontSize: 12,
                    color: _orange,
                    fontWeight: FontWeight.w500)),
          ]),
        ],
      ]),
    );
  }
}