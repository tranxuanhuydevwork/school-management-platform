import 'package:flutter/material.dart';
import 'package:myf3school/routes/router_names.dart';

import '../screen/score_page.dart';

const Color colorSelect = Colors.orange;

class BottomNav extends StatelessWidget {
  final int currentIndex;
  const BottomNav({super.key, this.currentIndex = 0});

  void _onItemTapped(BuildContext context, int index) {
    if (index == currentIndex) return;

    switch (index) {
      case 0:
      // Về trang chủ → xoá toàn bộ stack, không bị "page not found"
        Navigator.pushNamedAndRemoveUntil(
          context,
          RouteNames.home,
              (route) => false,
        );
        break;
      case 1:
        Navigator.pushNamedAndRemoveUntil(
          context,
          RouteNames.schedule,
              (route) => false,
        );
        break;
      // case 2:
      //   Navigator.pushNamedAndRemoveUntil(
      //     context,
      //     RouteNames.subjects,
      //         (route) => false,
      //   );
      //   break;
      case 3:
        Navigator.pushNamedAndRemoveUntil(
          context,
          RouteNames.notification,
              (route) => false,
        );
        break;
      case 4:
        Navigator.pushNamedAndRemoveUntil(
          context,
          RouteNames.userprofile,
              (route) => false,
        );
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return BottomNavigationBar(
      currentIndex: currentIndex,
      selectedItemColor: colorSelect,
      unselectedItemColor: Colors.grey,
      type: BottomNavigationBarType.fixed,
      onTap: (index) => _onItemTapped(context, index),
      items: const [
        BottomNavigationBarItem(icon: Icon(Icons.home),           label: 'Trang chủ'),
        BottomNavigationBarItem(icon: Icon(Icons.calendar_today), label: 'Lịch học'),
        BottomNavigationBarItem(icon: Icon(Icons.menu_book),      label: 'Môn học'),
        BottomNavigationBarItem(icon: Icon(Icons.notifications),  label: 'Thông báo'),
        BottomNavigationBarItem(icon: Icon(Icons.person),         label: 'Cá nhân'),
      ],
    );
  }
}