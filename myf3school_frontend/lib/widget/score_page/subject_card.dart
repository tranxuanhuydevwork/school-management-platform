import 'package:flutter/material.dart';
import '../../screen/score_page.dart';

class AppColors {
  static const primary = Color(0xFF1EBBD7);
  static const green   = Color(0xFF2ECC71);
  static const red     = Color(0xFFE74C3C);
  static const orange  = Color(0xFFF37021);
}

class SubjectCard extends StatelessWidget {
  final SubjectScore subject;
  const SubjectCard({super.key, required this.subject});

  @override
  Widget build(BuildContext context) {
    final avg     = subject.average;
    final isPassed = subject.passed;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10),
        ],
      ),
      child: ExpansionTile(
        tilePadding: const EdgeInsets.all(16),
        childrenPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),

        // ── Header: mã môn + tên môn ──────────────────────
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              subject.subjectCode,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
                fontSize: 13,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              subject.subjectName,
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 2),
            Text(
              subject.teacherName,
              style: TextStyle(fontSize: 12, color: Colors.grey.shade500),
            ),
          ],
        ),

        // ── Trailing: điểm TB + badge ──────────────────────
        trailing: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              avg != null ? avg.toStringAsFixed(1) : '-',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: avg == null
                    ? Colors.grey
                    : (isPassed ? AppColors.green : AppColors.red),
              ),
            ),
            const SizedBox(height: 4),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: avg == null
                    ? Colors.grey.withOpacity(0.15)
                    : (isPassed
                    ? AppColors.green.withOpacity(0.15)
                    : AppColors.red.withOpacity(0.15)),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                subject.letterGrade,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: avg == null
                      ? Colors.grey
                      : (isPassed ? AppColors.green : AppColors.red),
                ),
              ),
            ),
          ],
        ),

        // ── Chi tiết từng đầu điểm ─────────────────────────
        children: [
          const Divider(height: 1),
          const SizedBox(height: 8),
          ...subject.components.map((c) {
            final hasScore = c.score != null;
            return Padding(
              padding: const EdgeInsets.symmetric(vertical: 6),
              child: Row(
                children: [
                  // Loại điểm badge
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      '${c.weight.toInt()}%',
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      c.name,
                      style: const TextStyle(fontSize: 13),
                    ),
                  ),
                  // Điểm
                  Text(
                    hasScore ? c.score!.toStringAsFixed(1) : 'Chưa có',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                      color: hasScore
                          ? (c.score! >= 5 ? AppColors.green : AppColors.red)
                          : Colors.grey,
                    ),
                  ),
                  Text(
                    ' / ${c.maxScore.toInt()}',
                    style: TextStyle(fontSize: 12, color: Colors.grey.shade400),
                  ),
                ],
              ),
            );
          }),
          const SizedBox(height: 8),
        ],
      ),
    );
  }
}