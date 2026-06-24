import 'package:flutter/material.dart';

class ClassCard extends StatelessWidget {
  final String code;
  final String title;
  final String time;
  final String room;
  final String teacher;
  final String status;

  const ClassCard({
    super.key,
    required this.code,
    required this.title,
    required this.time,
    required this.room,
    required this.teacher,
    required this.status,
  });

  Color getColor() {
    switch (status) {
      case "ONGOING":
        return Colors.green;
      case "DONE":
        return Colors.grey;
      default:
        return Colors.blue;
    }
  }

  String getLabel() {
    switch (status) {
      case "ONGOING":
        return "Đang học";
      case "DONE":
        return "Đã xong";
      default:
        return "Sắp học";
    }
  }

  @override
  Widget build(BuildContext context) {
    final color = getColor();

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color.withOpacity(0.08),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding:
                const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFFFFE6D8),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  code,
                  style: const TextStyle(
                    color: Color(0xFFF37021),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(width: 10),

              Container(
                padding:
                const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(getLabel()),
              )
            ],
          ),

          const SizedBox(height: 10),

          Text(
            title,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),

          const SizedBox(height: 12),

          Row(
            children: [
              const Icon(Icons.access_time, color: Color(0xFFF37021)),
              const SizedBox(width: 8),
              Text(time),
            ],
          ),

          const SizedBox(height: 8),

          Row(
            children: [
              const Icon(Icons.location_on, color: Color(0xFFF37021)),
              const SizedBox(width: 8),
              Text(room),
              const Spacer(),
              const Icon(Icons.person, color: Color(0xFFF37021)),
              const SizedBox(width: 8),
              Text(teacher),
            ],
          )
        ],
      ),
    );
  }
}