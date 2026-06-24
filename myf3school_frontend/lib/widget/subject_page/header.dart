
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:myf3school/widget/subject_page/statcard.dart';


class SubjectPageHeader extends StatelessWidget{
  @override
  Widget build(BuildContext context) {
    return  Container(
      padding: const EdgeInsets.only(top: 50, left: 20, right: 20, bottom: 20),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Color(0xff2DB4C7),
            Color(0xff3AA0E6),
          ],
        ),
        borderRadius: BorderRadius.only(
          bottomLeft: Radius.circular(25),
          bottomRight: Radius.circular(25),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [

          const Row(
            children: [
              CircleAvatar(
                backgroundColor: Colors.white24,
                child: Icon(Icons.school, color: Colors.white),
              ),
              SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "Môn học",
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  Text(
                    "Chương trình đào tạo",
                    style: TextStyle(color: Colors.white70),
                  ),
                ],
              )
            ],
          ),

          const SizedBox(height: 20),

          Row(
            children: [

              StatCard(done: '86', total: '107', title: 'TÍN CHỈ'),

              const SizedBox(width: 12),

              StatCard( done: '29', total: '35', title: 'HOÀN THÀNH',),
            ],
          )
        ],
      ),
    );
  }

}