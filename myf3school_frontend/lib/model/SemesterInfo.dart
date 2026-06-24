// lib/model/SemesterInfo.dart
class SemesterInfo {
  final int    id;
  final String name;
  final bool   isCurrent;

  const SemesterInfo({
    required this.id,
    required this.name,
    required this.isCurrent,
  });

  factory SemesterInfo.fromJson(Map<String, dynamic> json) => SemesterInfo(
    id:        (json['id'] as num).toInt(),
    name:      json['name'] ?? '',
    isCurrent: json['isCurrent'] ?? json['current'] ?? false,
  );
}