import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'views/login_view.dart';
import 'views/home_view.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();
  final hasToken = prefs.getString('token') != null;

  runApp(AsistenciaApp(initialRouteIsHome: hasToken));
}

class AsistenciaApp extends StatelessWidget {
  final bool initialRouteIsHome;

  const AsistenciaApp({super.key, required this.initialRouteIsHome});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Control de Asistencia Universitario',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0B1120),
        primaryColor: const Color(0xFF3B82F6),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF3B82F6),
          secondary: Color(0xFF10B981),
          surface: Color(0xFF1E293B),
        ),
        fontFamily: 'Roboto',
      ),
      home: initialRouteIsHome ? const HomeView() : const LoginView(),
    );
  }
}
