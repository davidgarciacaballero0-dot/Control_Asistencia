import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'views/login_view.dart';
import 'views/home_view.dart';

final ValueNotifier<ThemeMode> themeNotifier = ValueNotifier(ThemeMode.dark);

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();
  final hasToken = prefs.getString('token') != null;
  final isDark = prefs.getBool('isDarkMode') ?? true;
  themeNotifier.value = isDark ? ThemeMode.dark : ThemeMode.light;

  runApp(AsistenciaApp(initialRouteIsHome: hasToken));
}

class AsistenciaApp extends StatelessWidget {
  final bool initialRouteIsHome;

  const AsistenciaApp({super.key, required this.initialRouteIsHome});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: themeNotifier,
      builder: (_, currentMode, __) {
        return MaterialApp(
          title: 'Control de Asistencia Universitario',
          debugShowCheckedModeBanner: false,
          themeMode: currentMode,
          theme: ThemeData(
            brightness: Brightness.light,
            scaffoldBackgroundColor: const Color(0xFFF8FAFC),
            primaryColor: const Color(0xFF2563EB),
            cardColor: Colors.white,
            appBarTheme: const AppBarTheme(
              backgroundColor: Colors.white,
              foregroundColor: Color(0xFF0F172A),
              elevation: 0,
              iconTheme: IconThemeData(color: Color(0xFF475569)),
            ),
            colorScheme: const ColorScheme.light(
              primary: Color(0xFF2563EB),
              secondary: Color(0xFF059669),
              surface: Colors.white,
              background: Color(0xFFF8FAFC),
            ),
            fontFamily: 'Roboto',
          ),
          darkTheme: ThemeData(
            brightness: Brightness.dark,
            scaffoldBackgroundColor: const Color(0xFF0B1120),
            primaryColor: const Color(0xFF3B82F6),
            cardColor: const Color(0xFF1E293B),
            appBarTheme: const AppBarTheme(
              backgroundColor: Color(0xFF0F172A),
              foregroundColor: Colors.white,
              elevation: 0,
              iconTheme: IconThemeData(color: Color(0xFF94A3B8)),
            ),
            colorScheme: const ColorScheme.dark(
              primary: Color(0xFF3B82F6),
              secondary: Color(0xFF10B981),
              surface: Color(0xFF1E293B),
              background: Color(0xFF0B1120),
            ),
            fontFamily: 'Roboto',
          ),
          home: initialRouteIsHome ? const HomeView() : const LoginView(),
        );
      },
    );
  }
}
