import 'package:flutter/material.dart';
import 'package:myf3school/widget/bottom_navigation_widget.dart';

import '../widget/subject_page/header.dart';

class SubjectPage extends StatefulWidget {
  const SubjectPage({super.key});

  @override
  State<SubjectPage> createState() => _SubjectPageState();
}

class _SubjectPageState extends State<SubjectPage> {

  final List semesters = [
    {
      "title": "Kỳ 1 - 2025",
      "subjects": [
        {"name": "Giáo dục Quốc phòng", "code": "VOV114", "credit": 0, "score": 7.0},
        {"name": "Toán học", "code": "Toán", "credit": 3, "score": 8.2},
        {"name": "Vật lý", "code": "Vật lý", "credit": 3, "score": 9.0},
        {"name": "Tiếng anh", "code": "Tiếng Anh", "credit": 3, "score": 7.5},
        {"name": "Kỹ năng giao tiếp", "code": "Kỹ năng giao tiếp", "credit": 3, "score": 8.0},
      ]
    },
    {
      "title": "Kỳ 2 - 2025",
      "subjects": [
        {"name": "Lịch sử", "code": "Lịch sử", "credit": 3, "score": 7.8},
        {"name": "Ngữ văn", "code": "Ngữ văn", "credit": 3, "score": 8.1},
      ]
    }
  ];

  Color getScoreColor(double score) {
    if (score >= 8) return Colors.green;
    if (score >= 6.5) return Colors.blue;
    if (score >= 5) return Colors.orange;
    return Colors.red;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xffF4F5F7),
      body: Column(
        children: [


          SubjectPageHeader(),

          const SizedBox(height: 10),

          Expanded(
            child: ListView.builder(
              itemCount: semesters.length,
              itemBuilder: (context, index) {

                final semester = semesters[index];

                return Container(
                  margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(18),
                  ),

                  child: ExpansionTile(

                    leading: CircleAvatar(
                      backgroundColor: Colors.green.withOpacity(.15),
                      child: Text("${index+1}",
                          style: const TextStyle(color: Colors.green)),
                    ),

                    title: Text(
                      semester["title"],
                      style: const TextStyle(
                          fontWeight: FontWeight.bold),
                    ),

                    subtitle: Text(
                      "${semester["subjects"].length} môn",
                      style: const TextStyle(fontSize: 12),
                    ),

                    children: List.generate(
                      semester["subjects"].length,
                          (i) {

                        final subject = semester["subjects"][i];

                        return ListTile(

                          leading: const CircleAvatar(
                            backgroundColor: Color(0xffE8F5E9),
                            child: Icon(Icons.check, color: Colors.green),
                          ),

                          title: Text(subject["name"]),

                          subtitle: Text(
                              "${subject["code"]}   ☆ ${subject["credit"]} TC"),

                          trailing: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [

                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 10, vertical: 5),
                                decoration: BoxDecoration(
                                  color: getScoreColor(subject["score"])
                                      .withOpacity(.15),
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                child: Text(
                                  subject["score"].toString(),
                                  style: TextStyle(
                                    color: getScoreColor(subject["score"]),
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),

                              const SizedBox(width: 6),

                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 10, vertical: 5),
                                decoration: BoxDecoration(
                                  color: Colors.green.withOpacity(.15),
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                child: const Text(
                                  "Đạt",
                                  style: TextStyle(
                                    color: Colors.green,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              )
                            ],
                          ),
                        );
                      },
                    ),
                  ),
                );
              },
            ),
          ),
          BottomNav(),
        ],
      ),
    );
  }

}