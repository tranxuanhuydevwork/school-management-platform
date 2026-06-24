import 'package:flutter/material.dart';
import 'package:myf3school/routes/router_names.dart';
import 'package:myf3school/screen/attendance_screen.dart';
import 'package:myf3school/screen/forgot_password_page.dart';
import 'package:myf3school/screen/home_page.dart';
import 'package:myf3school/screen/notification_page.dart';
import 'package:myf3school/screen/score_page.dart';
import 'package:myf3school/screen/user_profile_screen.dart';
import '../screen/chat_screen.dart';
import '../screen/application_screen.dart';
import '../screen/club_screen.dart';
import '../screen/contactlistscreen.dart';
import '../screen/forgotpassword.dart';
import '../screen/login.dart';
import '../screen/schedule_screen.dart';

class AppRouter {
  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case RouteNames.login:
        return MaterialPageRoute(
          builder: (_) => const LoginScreen(),
        );

      case RouteNames.home:
        return MaterialPageRoute(
          builder: (_) => const HomeScreen(),
        );

      case RouteNames.notification:
        return MaterialPageRoute(
          builder: (_) => const NotificationPage(),
        );

      case RouteNames.forgot_password:
        return MaterialPageRoute(
          builder: (_) => ForgotPasswordScreen(),
        );

      case RouteNames.score:
        return MaterialPageRoute(
          builder: (_) => ScoreScreen(),
        );

      case RouteNames.attendance:
        return MaterialPageRoute(
          builder: (_) => AttendanceScreen(),
        );

      case RouteNames.schedule:
        return MaterialPageRoute(
          builder: (_) => SchedulePage(),
        );

      case RouteNames.application:
        return MaterialPageRoute(
          builder: (_) => const ApplicationScreen(),
        );

      case RouteNames.contactList:
        return MaterialPageRoute(
          builder: (_) => const ContactListScreen(),
        );

      case RouteNames.chat:
        final args = settings.arguments as Map<String, dynamic>;
        return MaterialPageRoute(
          builder: (_) => ChatScreen(
            partnerId: args['partnerId'],
            partnerName: args['partnerName'],
          ),
        );

      case RouteNames.userprofile:
        return MaterialPageRoute(
          builder: (_) => const UserProfileScreen(),
        );

      case RouteNames.club_module:
        return MaterialPageRoute(
          builder: (_) => const ClubListScreen(),
        );

      default:
        return MaterialPageRoute(
          builder: (_) => const Scaffold(
            body: Center(child: Text('Page not found')),
          ),
        );
    }
  }
}