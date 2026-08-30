import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/asistencia_model.dart';

class QrScannerView extends StatefulWidget {
  final String? codigoQrPredefinido;
  const QrScannerView({super.key, this.codigoQrPredefinido});

  @override
  State<QrScannerView> createState() => _QrScannerViewState();
}

class _QrScannerViewState extends State<QrScannerView> {
  late final TextEditingController _qrInputController;
  bool _loading = false;
  AsistenciaModel? _asistenciaConfirmada;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _qrInputController = TextEditingController(text: widget.codigoQrPredefinido ?? '');
  }

  void _enviarCodigoQr(String codigo) async {
    if (codigo.trim().isEmpty) return;

    setState(() {
      _loading = true;
      _errorMessage = null;
      _asistenciaConfirmada = null;
    });

    try {
      final res = await ApiService.marcarAsistenciaQr(codigo.trim());
      setState(() {
        _asistenciaConfirmada = res;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString().replaceAll('Exception: ', '');
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Escaneo de Asistencia QR', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Area de visualizacion del visor de camara / escaneo
            Container(
              height: 240,
              decoration: BoxDecoration(
                color: isDark ? const Color(0xFF1E293B) : Colors.white,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF3B82F6), width: 2),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.04),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: Stack(
                alignment: Alignment.center,
                children: [
                  Icon(Icons.qr_code, size: 120, color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0)),
                  Container(
                    width: 180,
                    height: 180,
                    decoration: BoxDecoration(
                      border: Border.all(color: const Color(0xFF3B82F6), width: 2),
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  Positioned(
                    bottom: 16,
                    child: Text(
                      'Enfoque el codigo QR mostrado en clase',
                      style: TextStyle(color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B), fontSize: 12),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Formulario para ingreso / pegado de codigo QR (soporte para emulador)
            Text(
              'Ingresar o Pegar Codigo QR (Emulador / Pruebas)',
              style: TextStyle(
                color: isDark ? Colors.white : const Color(0xFF0F172A),
                fontWeight: FontWeight.bold,
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 10),

            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _qrInputController,
                    style: TextStyle(color: isDark ? Colors.white : const Color(0xFF0F172A)),
                    decoration: InputDecoration(
                      hintText: 'ej: QR-8374829104829182',
                      hintStyle: TextStyle(color: isDark ? const Color(0xFF64748B) : const Color(0xFF94A3B8)),
                      filled: true,
                      fillColor: isDark ? const Color(0xFF1E293B) : Colors.white,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0)),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide(color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0)),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                ElevatedButton(
                  onPressed: _loading ? null : () => _enviarCodigoQr(_qrInputController.text),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3B82F6),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                  child: _loading
                      ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                      : const Text('Validar', style: TextStyle(fontWeight: FontWeight.bold)),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Resultado de la asistencia exitosa
            if (_asistenciaConfirmada != null)
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: const Color(0xFF10B981).withOpacity(0.12),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.check_circle, color: Color(0xFF10B981), size: 24),
                        SizedBox(width: 8),
                        Text(
                          'Asistencia Registrada Exitosamente',
                          style: TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 15),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'Materia: ${_asistenciaConfirmada!.materiaNombre ?? _asistenciaConfirmada!.materiaSigla ?? "-"}',
                      style: TextStyle(color: isDark ? Colors.white : const Color(0xFF0F172A), fontSize: 14),
                    ),
                    Text(
                      'Grupo: ${_asistenciaConfirmada!.grupoNombre ?? "-"}',
                      style: TextStyle(color: isDark ? Colors.white70 : const Color(0xFF475569), fontSize: 13),
                    ),
                    Text(
                      'Hora de Registro: ${_asistenciaConfirmada!.horaRegistro}',
                      style: TextStyle(color: isDark ? Colors.white70 : const Color(0xFF475569), fontSize: 13),
                    ),
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.2),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'Estado: ${_asistenciaConfirmada!.estadoAsistencia}',
                        style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),

            // Error
            if (_errorMessage != null)
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFFEF4444).withOpacity(0.12),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFFEF4444).withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline, color: Color(0xFFEF4444), size: 24),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(color: Color(0xFFEF4444), fontSize: 13),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
