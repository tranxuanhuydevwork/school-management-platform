import 'ScoreComponent.dart';

class Subject {
  final String code;
  final String name;
  final double average;
  final List<ScoreComponent> components;

  Subject({
    required this.code,
    required this.name,
    required this.average,
    required this.components,
  });

}