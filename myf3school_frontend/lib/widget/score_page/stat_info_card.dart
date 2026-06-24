import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class StatInfoCard extends StatelessWidget{
  final String title;
  final String value;

  const StatInfoCard({super.key, required this.title, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
              fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white),
        ),
        const SizedBox(height: 6),
        Text(title, style: const TextStyle(color: Colors.white70)),
      ],
    );
  }
}