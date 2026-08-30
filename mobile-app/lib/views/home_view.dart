import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';
import '../models/clase_horario_model.dart';
import '../models/materia_inscrita_model.dart';
import '../main.dart';
import 'qr_scanner_view.dart';
import 'historial_view.dart';
import 'login_view.dart';

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> with SingleTickerProviderStateMixin {
  String _nombre = '';
  String _registro = '';
  String _ci = '';
  String _carrera = 'Ingenieria Informatica';
  String _plan = '2020';

  List<ClaseHorarioModel> _clasesHoy = [];
  List<MateriaInscritaModel> _materiasInscritas = [];
  bool _loading = true;
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _cargarDatosEstudiante();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _cargarDatosEstudiante() async {
    setState(() => _loading = true);
    final prefs = await SharedPreferences.getInstance();

    setState(() {
      _nombre = prefs.getString('nombreCompleto') ?? 'Estudiante';
      _registro = prefs.getString('registro') ?? '2024001';
      _ci = prefs.getString('ci') ?? '30000003';
    });

    try {
      // Perfil extendido
      final perfil = await ApiService.getPerfilEstudiante();
      if (perfil != null) {
        setState(() {
          _carrera = perfil['carrera'] ?? _carrera;
          _plan = perfil['plan'] ?? _plan;
          if (perfil['ci'] != null) _ci = perfil['ci'];
        });
      }

      // Clases de hoy
      final rawHoy = await ApiService.getClasesHoy();
      final clases = rawHoy.map((c) => ClaseHorarioModel.fromJson(c)).toList();

      // Materias inscritas
      final rawMaterias = await ApiService.getMateriasInscritas();
      final materias = rawMaterias.map((m) => MateriaInscritaModel.fromJson(m)).toList();

      setState(() {
        _clasesHoy = clases;
        _materiasInscritas = materias;
      });
    } catch (e) {
      debugPrint('Error cargando datos del estudiante: $e');
    } finally {
      setState(() => _loading = false);
    }
  }

  void _toggleTheme() async {
    final isDark = themeNotifier.value == ThemeMode.dark;
    themeNotifier.value = isDark ? ThemeMode.light : ThemeMode.dark;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('isDarkMode', !isDark);
  }

  void _cerrarSesion() async {
    await ApiService.logout();
    if (mounted) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const LoginView()),
      );
    }
  }

  ClaseHorarioModel? _obtenerClasePrioritaria() {
    if (_clasesHoy.isEmpty) return null;

    final now = TimeOfDay.now();
    final minutosActuales = now.hour * 60 + now.minute;

    // Buscar clase en curso
    for (var c in _clasesHoy) {
      final partesIni = c.horaInicio.split(':');
      final partesFin = c.horaFin.split(':');
      if (partesIni.length >= 2 && partesFin.length >= 2) {
        final minIni = int.parse(partesIni[0]) * 60 + int.parse(partesIni[1]);
        final minFin = int.parse(partesFin[0]) * 60 + int.parse(partesFin[1]);
        if (minutosActuales >= minIni && minutosActuales <= minFin) {
          return c;
        }
      }
    }

    // Buscar proxima clase
    for (var c in _clasesHoy) {
      final partesIni = c.horaInicio.split(':');
      if (partesIni.length >= 2) {
        final minIni = int.parse(partesIni[0]) * 60 + int.parse(partesIni[1]);
        if (minIni > minutosActuales) {
          return c;
        }
      }
    }

    return _clasesHoy.first;
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final clasePrioritaria = _obtenerClasePrioritaria();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Portal Estudiante', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        actions: [
          IconButton(
            icon: Icon(isDark ? Icons.light_mode : Icons.dark_mode),
            tooltip: isDark ? 'Cambiar a Modo Claro' : 'Cambiar a Modo Oscuro',
            onPressed: _toggleTheme,
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'Actualizar datos',
            onPressed: _cargarDatosEstudiante,
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar Sesion',
            onPressed: _cerrarSesion,
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _cargarDatosEstudiante,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // 1. Tarjeta de Perfil
                    Container(
                      padding: const EdgeInsets.all(18),
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          colors: isDark
                              ? [const Color(0xFF1E293B), const Color(0xFF0F172A)]
                              : [Colors.white, const Color(0xFFF1F5F9)],
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                        ),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0),
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.04),
                            blurRadius: 10,
                            offset: const Offset(0, 4),
                          )
                        ],
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              CircleAvatar(
                                radius: 24,
                                backgroundColor: const Color(0xFF3B82F6).withOpacity(0.15),
                                child: const Icon(Icons.person, color: Color(0xFF3B82F6), size: 26),
                              ),
                              const SizedBox(width: 14),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      _nombre,
                                      style: TextStyle(
                                        color: isDark ? Colors.white : const Color(0xFF0F172A),
                                        fontSize: 18,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const SizedBox(height: 2),
                                    Text(
                                      '$_carrera - Plan $_plan',
                                      style: TextStyle(
                                        color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 14),
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                decoration: BoxDecoration(
                                  color: const Color(0xFF3B82F6).withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Text(
                                  'Registro: $_registro',
                                  style: const TextStyle(
                                    color: Color(0xFF3B82F6),
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                decoration: BoxDecoration(
                                  color: const Color(0xFF10B981).withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Text(
                                  'CI: $_ci',
                                  style: const TextStyle(
                                    color: Color(0xFF10B981),
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),

                    // 2. Tarjeta Atajo Inteligente para la Clase Actual / Siguiente
                    if (clasePrioritaria != null)
                      Container(
                        padding: const EdgeInsets.all(18),
                        decoration: BoxDecoration(
                          color: clasePrioritaria.enCurso
                              ? (isDark ? const Color(0xFF1E293B) : const Color(0xFFEFF6FF))
                              : (isDark ? const Color(0xFF1E293B) : Colors.white),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                            color: clasePrioritaria.enCurso
                                ? const Color(0xFF3B82F6)
                                : (isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0)),
                            width: clasePrioritaria.enCurso ? 2 : 1,
                          ),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: clasePrioritaria.enCurso
                                        ? const Color(0xFF10B981).withOpacity(0.15)
                                        : const Color(0xFFF59E0B).withOpacity(0.15),
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    clasePrioritaria.enCurso ? 'En Curso Ahora' : 'Proxima Clase Hoy',
                                    style: TextStyle(
                                      color: clasePrioritaria.enCurso
                                          ? const Color(0xFF10B981)
                                          : const Color(0xFFF59E0B),
                                      fontWeight: FontWeight.bold,
                                      fontSize: 11,
                                    ),
                                  ),
                                ),
                                Text(
                                  '${clasePrioritaria.horaInicio} - ${clasePrioritaria.horaFin}',
                                  style: TextStyle(
                                    color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                    fontWeight: FontWeight.w600,
                                    fontSize: 13,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 10),
                            Text(
                              '${clasePrioritaria.materiaSigla} - ${clasePrioritaria.materiaNombre}',
                              style: TextStyle(
                                color: isDark ? Colors.white : const Color(0xFF0F172A),
                                fontWeight: FontWeight.bold,
                                fontSize: 16,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              'Docente: ${clasePrioritaria.docenteNombreCompleto} (Grupo ${clasePrioritaria.grupoNombre})',
                              style: TextStyle(
                                color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                fontSize: 12,
                              ),
                            ),
                            const SizedBox(height: 14),
                            SizedBox(
                              width: double.infinity,
                              child: ElevatedButton.icon(
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: const Color(0xFF3B82F6),
                                  foregroundColor: Colors.white,
                                  padding: const EdgeInsets.symmetric(vertical: 12),
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                ),
                                icon: const Icon(Icons.qr_code_scanner, size: 20),
                                label: const Text('Escanear QR de Asistencia', style: TextStyle(fontWeight: FontWeight.bold)),
                                onPressed: () {
                                  Navigator.of(context).push(
                                    MaterialPageRoute(builder: (_) => const QrScannerView()),
                                  ).then((_) => _cargarDatosEstudiante());
                                },
                              ),
                            ),
                          ],
                        ),
                      ),
                    const SizedBox(height: 16),

                    // 3. Pestañas de Navegacion
                    Container(
                      decoration: BoxDecoration(
                        color: isDark ? const Color(0xFF1E293B) : const Color(0xFFF1F5F9),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: TabBar(
                        controller: _tabController,
                        labelColor: Colors.white,
                        unselectedLabelColor: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                        indicator: BoxDecoration(
                          color: const Color(0xFF3B82F6),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        indicatorSize: TabBarIndicatorSize.tab,
                        tabs: const [
                          Tab(text: 'Clases Hoy'),
                          Tab(text: 'Mis Materias'),
                          Tab(text: 'Historial'),
                        ],
                      ),
                    ),
                    const SizedBox(height: 14),

                    // 4. Vista de Contenido de Pestañas
                    SizedBox(
                      height: 360,
                      child: TabBarView(
                        controller: _tabController,
                        children: [
                          // Tab 1: Clases de Hoy
                          _clasesHoy.isEmpty
                              ? Center(
                                  child: Text(
                                    'No hay clases programadas para hoy',
                                    style: TextStyle(
                                      color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                    ),
                                  ),
                                )
                              : ListView.builder(
                                  itemCount: _clasesHoy.length,
                                  itemBuilder: (context, idx) {
                                    final c = _clasesHoy[idx];
                                    return Card(
                                      margin: const EdgeInsets.only(bottom: 10),
                                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                                      child: ListTile(
                                        leading: CircleAvatar(
                                          backgroundColor: c.enCurso
                                              ? const Color(0xFF10B981).withOpacity(0.2)
                                              : const Color(0xFF3B82F6).withOpacity(0.15),
                                          child: Icon(
                                            Icons.access_time,
                                            color: c.enCurso ? const Color(0xFF10B981) : const Color(0xFF3B82F6),
                                            size: 20,
                                          ),
                                        ),
                                        title: Text(
                                          '${c.materiaSigla} - ${c.materiaNombre}',
                                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                                        ),
                                        subtitle: Text(
                                          '${c.horaInicio} - ${c.horaFin} | Doc: ${c.docenteNombreCompleto}',
                                          style: const TextStyle(fontSize: 12),
                                        ),
                                        trailing: ElevatedButton(
                                          style: ElevatedButton.styleFrom(
                                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                            minimumSize: const Size(60, 30),
                                          ),
                                          onPressed: () {
                                            Navigator.of(context).push(
                                              MaterialPageRoute(builder: (_) => const QrScannerView()),
                                            ).then((_) => _cargarDatosEstudiante());
                                          },
                                          child: const Text('QR', style: TextStyle(fontSize: 11)),
                                        ),
                                      ),
                                    );
                                  },
                                ),

                          // Tab 2: Mis Materias
                          _materiasInscritas.isEmpty
                              ? Center(
                                  child: Text(
                                    'No se encontraron materias inscritas',
                                    style: TextStyle(
                                      color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                    ),
                                  ),
                                )
                              : ListView.builder(
                                  itemCount: _materiasInscritas.length,
                                  itemBuilder: (context, idx) {
                                    final m = _materiasInscritas[idx];
                                    return Card(
                                      margin: const EdgeInsets.only(bottom: 10),
                                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                                      child: ExpansionTile(
                                        leading: const Icon(Icons.book, color: Color(0xFF3B82F6)),
                                        title: Text(
                                          '${m.materiaSigla} - ${m.materiaNombre}',
                                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                                        ),
                                        subtitle: Text(
                                          'Grupo ${m.grupoNombre} | Doc: ${m.docenteNombreCompleto}',
                                          style: const TextStyle(fontSize: 12),
                                        ),
                                        children: [
                                          Padding(
                                            padding: const EdgeInsets.all(12.0),
                                            child: Column(
                                              crossAxisAlignment: CrossAxisAlignment.start,
                                              children: m.horarios
                                                  .map((h) => Padding(
                                                        padding: const EdgeInsets.symmetric(vertical: 2.0),
                                                        child: Row(
                                                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                                          children: [
                                                            Text(h.dia, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12)),
                                                            Text('${h.horaInicio} - ${h.horaFin}', style: const TextStyle(color: Color(0xFF3B82F6), fontSize: 12)),
                                                          ],
                                                        ),
                                                      ))
                                                  .toList(),
                                            ),
                                          )
                                        ],
                                      ),
                                    );
                                  },
                                ),

                          // Tab 3: Acceso a Historial
                          Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Icon(Icons.history, size: 48, color: Color(0xFF3B82F6)),
                                const SizedBox(height: 12),
                                const Text(
                                  'Historial de Asistencias',
                                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                                ),
                                const SizedBox(height: 6),
                                const Text(
                                  'Consulta todas tus asistencias registradas',
                                  style: TextStyle(fontSize: 12, color: Colors.grey),
                                ),
                                const SizedBox(height: 16),
                                ElevatedButton.icon(
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF3B82F6),
                                    foregroundColor: Colors.white,
                                  ),
                                  icon: const Icon(Icons.open_in_new, size: 16),
                                  label: const Text('Abrir Historial Completo'),
                                  onPressed: () {
                                    Navigator.of(context).push(
                                      MaterialPageRoute(builder: (_) => const HistorialView()),
                                    );
                                  },
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
    );
  }
}
