
import 'package:flutter/material.dart';
import '../../model/ClubModel.dart';
const _orange = Color(0xFFF37021);

class MemberTile extends StatelessWidget {
  final ClubMemberDetail member;
  final bool             showApprove;
  final VoidCallback?    onApprove;
  final VoidCallback?    onReject;

  const MemberTile({
    super.key,
    required this.member,
    this.showApprove = false,
    this.onApprove,
    this.onReject,
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
            blurRadius: 6,
            offset: const Offset(0, 2))],
      ),
      child: Row(children: [
        // Avatar
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

        // Info
        Expanded(child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(m.studentName,
                style: const TextStyle(
                    fontWeight: FontWeight.w600, fontSize: 13)),
            const SizedBox(height: 2),
            Row(children: [
              Text(m.studentCode,
                  style: const TextStyle(fontSize: 11, color: Colors.grey)),
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

        // Nút duyệt / từ chối (chỉ hiện khi showApprove = true)
        if (showApprove) Row(children: [
          IconButton(
            icon: const Icon(Icons.check_circle,
                color: Color(0xFF2E7D32), size: 26),
            onPressed: onApprove,
            tooltip: 'Duyệt',
          ),
          IconButton(
            icon: const Icon(Icons.cancel, color: Colors.red, size: 26),
            onPressed: onReject,
            tooltip: 'Từ chối',
          ),
        ]),
      ]),
    );
  }
}