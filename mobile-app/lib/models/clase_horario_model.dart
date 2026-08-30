class ClaseHorarioModel {
  final int? grupoId;
  final String grupoNombre;
  final String materiaSigla;
  final String materiaNombre;
  final String docenteCodigo;
  final String docenteNombreCompleto;
  final int? horarioId;
  final String dia;
  final String horaInicio;
  final String horaFin;
  final bool esHoy;
  final bool enCurso;
  final bool concluida;

  ClaseHorarioModel({
    this.grupoId,
    required this.grupoNombre,
    required this.materiaSigla,
    required this.materiaNombre,
    required this.docenteCodigo,
    required this.docenteNombreCompleto,
    this.horarioId,
    required this.dia,
    required this.horaInicio,
    required this.horaFin,
    required this.esHoy,
    required this.enCurso,
    required this.concluida,
  });

  factory ClaseHorarioModel.fromJson(Map<String, dynamic> json) {
    return ClaseHorarioModel(
      grupoId: json['grupoId'],
      grupoNombre: json['grupoNombre'] ?? '',
      materiaSigla: json['materiaSigla'] ?? '',
      materiaNombre: json['materiaNombre'] ?? '',
      docenteCodigo: json['docenteCodigo'] ?? '',
      docenteNombreCompleto: json['docenteNombreCompleto'] ?? '',
      horarioId: json['horarioId'],
      dia: json['dia'] ?? '',
      horaInicio: json['horaInicio'] ?? '',
      horaFin: json['horaFin'] ?? '',
      esHoy: json['esHoy'] ?? false,
      enCurso: json['enCurso'] ?? false,
      concluida: json['concluida'] ?? false,
    );
  }
}
