import 'package:flutter/material.dart';
import 'package:flutter/material.dart';

class StatGrid extends StatelessWidget {
  final double gpa;
  final int attendance;
  final int earnedCredits;
  final int totalCredits;
  final int rank;

  const StatGrid({
    super.key,
    required this.gpa,
    required this.attendance,
    required this.earnedCredits,
    required this.totalCredits,
    required this.rank,
  });

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      crossAxisSpacing: 16,
      mainAxisSpacing: 16,
      childAspectRatio: 1.5,
      children: [
        StatCard(
          title: 'Điểm GPA',
          value: gpa.toStringAsFixed(2),
          sub: '/ 4.00',
          colors: [Color(0xff00c6ff), Color(0xff0072ff)],
        ),
        StatCard(
          title: 'Điểm danh',
          value: '$attendance%',
          sub: 'Tháng này',
          colors: [Color(0xff2ecc71), Color(0xff27ae60)],
        ),
        StatCard(
          title: 'Tín chỉ',
          value: '$earnedCredits',
          sub: '/ $totalCredits tín chỉ',
          colors: [Color(0xff6a5acd), Color(0xff00bcd4)],
        ),
        StatCard(
          title: 'Xếp hạng',
          value: '#$rank',
          sub: 'Trong lớp',
          colors: [Color(0xfffbc02d), Color(0xfff57c00)],
        ),
      ],
    );
  }
}
class StatCard extends StatelessWidget {
  final String title, value, sub;
  final List<Color> colors;

  const StatCard({
    super.key,
    required this.title,
    required this.value,
    required this.sub,
    required this.colors,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(18),
        gradient: LinearGradient(colors: colors),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween, // 🔥 auto giãn
        children: [
          Text(title, style: const TextStyle(color: Colors.white70)),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                value,
                style: const TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              Text(sub, style: const TextStyle(color: Colors.white70)),
            ],
          ),

        ],
      )
      );
  }
}
