import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
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
  late final MobileScannerController _scannerController;
  bool _loading = false;
  bool _isProcessing = false;
  bool _torchOn = false;
  CameraFacing _currentFacing = CameraFacing.back;
  AsistenciaModel? _asistenciaConfirmada;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _qrInputController = TextEditingController(text: widget.codigoQrPredefinido ?? '');
    _scannerController = MobileScannerController(
      detectionSpeed: DetectionSpeed.noDuplicates,
      facing: CameraFacing.back,
      formats: const [BarcodeFormat.qrCode],
    );
  }

  @override
  void dispose() {
    _scannerController.dispose();
    _qrInputController.dispose();
    super.dispose();
  }

  String _normalizarCodigo(String codigo) {
    String limpio = codigo.trim().toUpperCase();
    // Si el usuario pego solo el hash sin el prefijo QR-
    if (!limpio.startsWith('QR-') && !limpio.contains('-') && limpio.length >= 10) {
      limpio = 'QR-$limpio';
    }
    return limpio;
  }

  void _onDetect(BarcodeCapture capture) {
    if (_isProcessing || _loading || _asistenciaConfirmada != null) return;

    for (final barcode in capture.barcodes) {
      final rawValue = barcode.rawValue;
      if (rawValue != null && rawValue.trim().isNotEmpty) {
        _isProcessing = true;
        HapticFeedback.mediumImpact();
        final codigoNormalizado = _normalizarCodigo(rawValue);
        _qrInputController.text = codigoNormalizado;
        _enviarCodigoQr(codigoNormalizado);
        break;
      }
    }
  }

  void _enviarCodigoQr(String codigo) async {
    final codigoNormalizado = _normalizarCodigo(codigo);
    if (codigoNormalizado.isEmpty) return;

    setState(() {
      _loading = true;
      _errorMessage = null;
      _asistenciaConfirmada = null;
    });

    try {
      final res = await ApiService.marcarAsistenciaQr(codigoNormalizado);
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
        _isProcessing = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Escaneo de Asistencia QR', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        actions: [
          IconButton(
            icon: Icon(_torchOn ? Icons.flash_on : Icons.flash_off),
            tooltip: 'Alternar Linterna',
            onPressed: () async {
              await _scannerController.toggleTorch();
              setState(() {
                _torchOn = !_torchOn;
              });
            },
          ),
          IconButton(
            icon: const Icon(Icons.flip_camera_android),
            tooltip: 'Cambiar Camara',
            onPressed: () async {
              await _scannerController.switchCamera();
              setState(() {
                _currentFacing = _currentFacing == CameraFacing.back ? CameraFacing.front : CameraFacing.back;
              });
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Visor de Camara con MobileScanner
            Container(
              height: 320,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFF3B82F6), width: 2),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.08),
                    blurRadius: 14,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(18),
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    // Camara activa
                    MobileScanner(
                      controller: _scannerController,
                      onDetect: _onDetect,
                      errorBuilder: (context, error) {
                        return Container(
                          color: isDark ? const Color(0xFF1E293B) : const Color(0xFFF1F5F9),
                          padding: const EdgeInsets.all(20),
                          child: Center(
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                const Icon(Icons.videocam_off_outlined, size: 50, color: Color(0xFFF59E0B)),
                                const SizedBox(height: 12),
                                Text(
                                  'Camara no disponible en este dispositivo (${error.errorCode.name}). Puede ingresar el codigo manualmente en el campo inferior.',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                    color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                                    fontSize: 13,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),

                    // Marco guia de escaneo
                    Container(
                      width: 220,
                      height: 220,
                      decoration: BoxDecoration(
                        border: Border.all(color: const Color(0xFF3B82F6), width: 3),
                        borderRadius: BorderRadius.circular(16),
                      ),
                    ),

                    // Indicador de estado o carga
                    if (_loading)
                      Container(
                        color: Colors.black45,
                        child: const Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              CircularProgressIndicator(color: Colors.white),
                              SizedBox(height: 12),
                              Text('Validando asistencia...', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                            ],
                          ),
                        ),
                      ),

                    // Superposicion de confirmacion de escaneo exitoso sobre el visor
                    if (_asistenciaConfirmada != null)
                      Container(
                        color: const Color(0xFF10B981).withOpacity(0.35),
                        child: const Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.check_circle, color: Colors.white, size: 64),
                              SizedBox(height: 10),
                              Text(
                                'Asistencia Registrada',
                                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                              ),
                            ],
                          ),
                        ),
                      ),

                    // Etiqueta inferior del visor
                    Positioned(
                      bottom: 12,
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                        decoration: BoxDecoration(
                          color: Colors.black54,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Text(
                          _asistenciaConfirmada != null
                              ? 'Codigo QR detectado correctamente'
                              : 'Apunta la camara al codigo QR de la clase',
                          style: const TextStyle(color: Colors.white, fontSize: 12),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Formulario para ingreso / pegado de codigo QR
            Text(
              'Ingresar o Pegar Codigo QR Manualmente',
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
                      hintText: 'ej: QR-4B3B743B96FB4012',
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
            const SizedBox(height: 20),

            // Resultado de asistencia exitosa
            if (_asistenciaConfirmada != null) ...[
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
                        Expanded(
                          child: Text(
                            'Asistencia Registrada Exitosamente',
                            style: TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 15),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'Materia: ${_asistenciaConfirmada!.materiaNombre ?? _asistenciaConfirmada!.materiaSigla ?? "-"}',
                      style: TextStyle(color: isDark ? Colors.white : const Color(0xFF0F172A), fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Grupo: ${_asistenciaConfirmada!.grupoNombre ?? "-"}',
                      style: TextStyle(color: isDark ? Colors.white70 : const Color(0xFF475569), fontSize: 13),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Hora de Registro: ${_asistenciaConfirmada!.horaRegistro}',
                      style: TextStyle(color: isDark ? Colors.white70 : const Color(0xFF475569), fontSize: 13),
                    ),
                    const SizedBox(height: 10),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
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
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: () {
                  setState(() {
                    _asistenciaConfirmada = null;
                    _errorMessage = null;
                    _qrInputController.clear();
                    _isProcessing = false;
                  });
                },
                icon: const Icon(Icons.qr_code_scanner),
                label: const Text('Escanear Otro Codigo'),
              ),
            ],

            // Mensaje de Error
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

