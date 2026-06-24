import 'dart:math';
import 'dart:ui';

import 'package:flutter/cupertino.dart';

class CircularProgressPainter extends CustomPainter {
  final double presentFraction;
  final double lateFraction;
  final double absentFraction;
  final double strokeWidth;

  CircularProgressPainter({
    required this.presentFraction,
    required this.lateFraction,
    required this.absentFraction,
    this.strokeWidth = 8,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = (min(size.width, size.height) - strokeWidth) / 2;

    final paintBg = Paint()
      ..color = AppColors.grey
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;

    final paintPresent = Paint()
      ..color = AppColors.green
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.butt;

    final paintLate = Paint()
      ..color = AppColors.orange
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.butt;

    final paintAbsent = Paint()
      ..color = AppColors.red
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.butt;

    const startAngle = -pi / 2;
    const fullCircle = 2 * pi;
    const gap = 0.04;

    canvas.drawCircle(center, radius, paintBg);

    double current = startAngle;

    void drawArc(Paint p, double fraction) {
      if (fraction <= 0) return;
      final sweep = fraction * fullCircle - gap;
      if (sweep > 0) {
        canvas.drawArc(
          Rect.fromCircle(center: center, radius: radius),
          current,
          sweep,
          false,
          p,
        );
      }
      current += fraction * fullCircle;
    }

    drawArc(paintPresent, presentFraction);
    drawArc(paintLate, lateFraction);
    drawArc(paintAbsent, absentFraction);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

class AppColors {
  static const primary = Color(0xFF1EBBD7);
  static const green = Color(0xFF2ECC71);
  static const orange = Color(0xFFF5A623);
  static const red = Color(0xFFE74C3C);
  static const grey = Color(0xFFEAEAEA);
  static const textDark = Color(0xFF2D3142);
  static const textLight = Color(0xFF9098B1);
}
