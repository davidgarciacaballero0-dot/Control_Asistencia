import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/usuario_model.dart';
import '../models/asistencia_model.dart';

import 'package:flutter/foundation.dart';

class ApiService {
  static String _customBaseUrl = '';

  // Por defecto usa la IP local para conexion desde dispositivos fisicos en la misma red Wi-Fi
  static String get baseUrl {
    if (_customBaseUrl.isNotEmpty) return _customBaseUrl;
    return kIsWeb ? 'http://localhost:8080' : 'http://192.168.100.29:8080';
  }

  static Future<void> initBaseUrl() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString('server_url');
    if (saved != null && saved.isNotEmpty) {
      _customBaseUrl = saved;
    }
  }

  static Future<void> setBaseUrl(String url) async {
    _customBaseUrl = url.trim();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('server_url', _customBaseUrl);
  }

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token');
  }

  static Future<String?> getRegistro() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('registro');
  }

  static Future<Map<String, String>> _getHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  // Iniciar sesion
  static Future<UsuarioModel> login(String username, String password) async {
    final url = Uri.parse('$baseUrl/api/v1/auth/login');
    final response = await http.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': username, 'password': password}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final usuario = UsuarioModel.fromJson(data);

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', usuario.token);
      await prefs.setString('username', usuario.username);
      await prefs.setString('nombreCompleto', usuario.nombreCompleto);
      await prefs.setString('registro', usuario.identificadorReferencia ?? usuario.username);
      if (usuario.ci != null) {
        await prefs.setString('ci', usuario.ci!);
      }

      return usuario;
    } else {
      throw Exception('Credenciales invalidas o error en el servidor: ${response.body}');
    }
  }

  // Marcar asistencia mediante codigo QR escaneado
  static Future<AsistenciaModel> marcarAsistenciaQr(String codigoQr) async {
    final url = Uri.parse('$baseUrl/api/v1/asistencia/registros/marcar-qr');
    final headers = await _getHeaders();
    final registro = await getRegistro();

    if (registro == null) {
      throw Exception('No se encontro el numero de registro del estudiante autenticado');
    }

    final response = await http.post(
      url,
      headers: headers,
      body: jsonEncode({
        'registroEstudiante': registro,
        'codigoQr': codigoQr,
        'observacion': 'Marcado desde Flutter Mobile App',
      }),
    );

    if (response.statusCode == 201 || response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return AsistenciaModel.fromJson(data);
    } else {
      final errData = jsonDecode(response.body);
      final msg = errData['message'] ?? errData['mensaje'] ?? response.body;
      throw Exception(msg);
    }
  }

  // Listar historial de asistencias del estudiante
  static Future<List<AsistenciaModel>> getHistorialAsistencias() async {
    final registro = await getRegistro();
    if (registro == null) return [];

    final url = Uri.parse('$baseUrl/api/v1/asistencia/registros/estudiante/$registro');
    final headers = await _getHeaders();

    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => AsistenciaModel.fromJson(json)).toList();
    } else {
      throw Exception('Error al obtener historial de asistencias');
    }
  }

  // Listar materias inscritas del estudiante
  static Future<List<dynamic>> getMateriasInscritas() async {
    final registro = await getRegistro();
    if (registro == null) return [];

    final url = Uri.parse('$baseUrl/api/v1/academico/boletas/estudiante/$registro/materias');
    final headers = await _getHeaders();

    final response = await http.get(url, headers: headers);
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    }
    return [];
  }

  // Listar clases de hoy
  static Future<List<dynamic>> getClasesHoy() async {
    final registro = await getRegistro();
    if (registro == null) return [];

    final url = Uri.parse('$baseUrl/api/v1/academico/boletas/estudiante/$registro/clases-hoy');
    final headers = await _getHeaders();

    final response = await http.get(url, headers: headers);
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    }
    return [];
  }

  // Obtener perfil extendido del estudiante
  static Future<Map<String, dynamic>?> getPerfilEstudiante() async {
    final registro = await getRegistro();
    if (registro == null) return null;

    final url = Uri.parse('$baseUrl/api/v1/academico/estudiantes/$registro');
    final headers = await _getHeaders();

    final response = await http.get(url, headers: headers);
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    }
    return null;
  }

  // Listar sesiones activas en tiempo real
  static Future<List<dynamic>> getSesionesActivas() async {
    final url = Uri.parse('$baseUrl/api/v1/asistencia/sesiones/activas');
    final headers = await _getHeaders();

    final response = await http.get(url, headers: headers);
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    }
    return [];
  }

  // Cerrar sesion
  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
