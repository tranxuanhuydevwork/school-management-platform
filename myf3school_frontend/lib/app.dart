import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:myf3school/routes/app_router.dart';
import 'package:myf3school/routes/router_names.dart';

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Money App',
      theme: ThemeData(useMaterial3: true),
      initialRoute: RouteNames.login,
      onGenerateRoute: AppRouter.generateRoute,
    );
  }
}
