class StudentResponse {
  final int id;
  final String studentCode;
  final String fullName;
  final String email;
  final String phone;
  final String? avatarUrl;
  final String dateOfBirth;
  final String gender;
  final String className;
  final int classId;
  final String gradeName;
  final String academicYear;
  final String enrollmentDate;
  final double gpa;
  final String rank;
  final String conduct;

  StudentResponse({
    required this.id,
    required this.studentCode,
    required this.fullName,
    required this.email,
    required this.phone,
    this.avatarUrl,
    required this.dateOfBirth,
    required this.gender,
    required this.className,
    required this.classId,
    required this.gradeName,
    required this.academicYear,
    required this.enrollmentDate,
    required this.gpa,
    required this.rank,
    required this.conduct,
  });

  factory StudentResponse.fromJson(Map<String, dynamic> json) {
    return StudentResponse(
      id: json['id'],
      studentCode: json['studentCode'],
      fullName: json['fullName'],
      email: json['email'],
      phone: json['phone'],
      avatarUrl: json['avatarUrl'],
      dateOfBirth: json['dateOfBirth'],
      gender: json['gender'],
      className: json['className'],
      classId: json['classId'],
      gradeName: json['gradeName'],
      academicYear: json['academicYear'],
      enrollmentDate: json['enrollmentDate'],
      gpa: (json['gpa'] as num).toDouble(),
      rank: json['rank'],
      conduct: json['conduct'],
    );
  }
}