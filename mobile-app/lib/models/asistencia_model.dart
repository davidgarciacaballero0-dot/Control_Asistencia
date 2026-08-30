class AsistenciaModel {
  final int? id;
  final String fechaRegistro;
  final String horaRegistro;
  final String metodoValidacion;
  final String registroEstudiante;
  final String? nombreEstudiante;
  final int? sesionId;
  final int? grupoId;
  final String? grupoNombre;
  final String? materiaSigla;
  final String? materiaNombre;
  final String estadoAsistencia;
  final String? mensaje;

  AsistenciaModel({
    this.id,
    required this.fechaRegistro,
    required this.horaRegistro,
    required this.metodoValidacion,
    required this.registroEstudiante,
    this.nombreEstudiante,
    this.sesionId,
    this.grupoId,
    this.grupoNombre,
    this.materiaSigla,
    this.materiaNombre,
    required this.estadoAsistencia,
    this.mensaje,
  });

  factory AsistenciaModel.fromJson(Map<String, dynamic> json) {
    return AsistenciaModel(
      id: json['id'],
      fechaRegistro: json['fechaRegistro'] ?? '',
      horaRegistro: json['horaRegistro'] ?? '',
      metodoValidacion: json['metodoValidacion'] ?? 'QR',
      registroEstudiante: json['registroEstudiante'] ?? '',
      nombreEstudiante: json['nombreEstudiante'],
      sesionId: json['sesionId'],
      grupoId: json['grupoId'],
      grupoNombre: json['grupoNombre'],
      materiaSigla: json['materiaSigla'],
      materiaNombre: json['materiaNombre'],
      estadoAsistencia: json['estadoAsistencia'] ?? 'PRESENTE',
      mensaje: json['mensaje'],
    );
  }
}
