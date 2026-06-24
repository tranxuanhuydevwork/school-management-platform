import 'package:flutter/material.dart';
import 'stat_info_card.dart';

class ScoreHeader extends StatelessWidget {
  final String gpa;
  final String totalSubjects;
  final String passedCount;

  const ScoreHeader({
    super.key,
    required this.gpa,
    required this.totalSubjects,
    required this.passedCount,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 50, 20, 24),
      decoration: const BoxDecoration(
        color: Colors.amber,
        borderRadius: BorderRadius.vertical(bottom: Radius.circular(25)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Kết quả học tập',
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              StatInfoCard(title: 'Điểm TB', value: gpa),
              StatInfoCard(title: 'Tổng môn', value: totalSubjects),
              StatInfoCard(title: 'Môn đạt', value: passedCount),
            ],
          ),
        ],
      ),
    );
  }
}