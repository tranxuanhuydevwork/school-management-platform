import 'package:flutter/material.dart';
import 'package:myf3school/routes/router_names.dart';
import '../../controller/AuthController.dart';

class Header extends StatelessWidget {
  const Header({super.key});

  @override
  Widget build(BuildContext context) {
    // Lấy tên & chữ viết tắt từ AuthController (không hardcode)
    final fullName = AuthController.fullName ?? 'Người dùng';
    final initials = fullName
        .trim()
        .split(' ')
        .where((w) => w.isNotEmpty)
        .map((w) => w[0].toUpperCase())
        .take(3)
        .join();

    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: Colors.orange,
        borderRadius: BorderRadius.circular(15),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 22,
                backgroundColor: const Color(0xff00BCD4),
                child: Text(
                  initials,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    fullName,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ],
          ),
          Row(
            children: [
              const Icon(Icons.search),
              const SizedBox(width: 16),
              InkWell(
                onTap: () {
                  Navigator.pushNamed(context, RouteNames.notification);
                },
                child: Stack(
                  children: [
                    const Icon(Icons.notifications_none),
                    Positioned(
                      right: 0,
                      top: 0,
                      child: const CircleAvatar(
                        radius: 8,
                        backgroundColor: Color(0xff00BCD4),
                        child: Text(
                          '3',
                          style: TextStyle(fontSize: 10, color: Colors.white),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}