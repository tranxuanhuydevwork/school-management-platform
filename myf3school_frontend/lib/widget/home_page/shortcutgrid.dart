import 'package:flutter/material.dart';
import '../../routes/router_names.dart';

class ShortcutGrid extends StatelessWidget {
  const ShortcutGrid({super.key});

  @override
  Widget build(BuildContext context) {
    final items = [
      Icons.calendar_today,
      Icons.description,
      Icons.credit_card,
      Icons.assignment,
      Icons.bookmark,
      Icons.school,
      Icons.chat_bubble_outline, // index 6 → Nhắn tin
      Icons.people,
    ];

    final labels = [
      'Thời khóa biểu',
      'Điểm số',
      'Học phí',
      'Điểm danh',
      'Đăng ký',
      'Khóa học',
      'Nhắn tin',   // ← đổi từ "Phản hồi"
      'Câu lạc bộ',
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: items.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
      ),
      itemBuilder: (context, i) {
        return Column(
          children: [
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    blurRadius: 12,
                    color: Colors.black.withOpacity(0.05),
                  ),
                ],
              ),
              child: InkWell(
                onTap: () {
                  switch (i) {
                    case 0:
                      Navigator.pushNamed(context, RouteNames.schedule);
                      break;
                    case 1:
                      Navigator.pushNamed(context, RouteNames.score);
                      break;
                    case 3:
                      Navigator.pushNamed(context, RouteNames.attendance);
                      break;
                    case 4:
                      Navigator.pushNamed(context, RouteNames.application);
                      break;
                    case 6:
                    // ← Nhắn tin: mở danh sách liên hệ
                      Navigator.pushNamed(context, RouteNames.contactList);
                      break;
                    case 7:
                      Navigator.pushNamed(context, RouteNames.club_module);
                      break;
                    default:
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Chức năng đang phát triển'),
                        ),
                      );
                  }
                },
                child: Icon(items[i], color: const Color(0xff00BCD4)),
              ),
            ),
            const SizedBox(height: 6),
            Text(
              labels[i],
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 11),
            ),
          ],
        );
      },
    );
  }
}