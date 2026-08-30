class HorarioItemModel {
  final int? id;
  final String dia;
  final String horaInicio;
  final String horaFin;

  HorarioItemModel({
    this.id,
    required this.dia,
    required this.horaInicio,
    required this.horaFin,
  });

  factory HorarioItemModel.fromJson(Map<String, dynamic> json) {
    return HorarioItemModel(
      id: json['id'],
      dia: json['dia'] ?? '',
      horaInicio: json['horaInicio'] ?? '',
      horaFin: json['horaFin'] ?? '',
    );
  }
}

class MateriaInscritaModel {
  final int? grupoId;
  final String grupoNombre;
  final int? cupo;
  final String materiaSigla;
  final String materiaNombre;
  final String docenteCodigo;
  final String docenteNombreCompleto;
  final String? docenteCorreo;
  final List<HorarioItemModel> horarios;

  MateriaInscritaModel({
    this.grupoId,
    required this.grupoNombre,
    this.cupo,
    required this.materiaSigla,
    required this.materiaNombre,
    required this.docenteCodigo,
    required this.docenteNombreCompleto,
    this.docenteCorreo,
    required this.horarios,
  });

  factory MateriaInscritaModel.fromJson(Map<String, dynamic> json) {
    var rawHorarios = json['horarios'] as List<dynamic>? ?? [];
    List<HorarioItemModel> listaHorarios =
        rawHorarios.map((h) => HorarioItemModel.fromJson(h)).toList();

    return MateriaInscritaModel(
      grupoId: json['grupoId'],
      grupoNombre: json['grupoNombre'] ?? '',
      cupo: json['cupo'],
      materiaSigla: json['materiaSigla'] ?? '',
      materiaNombre: json['materiaNombre'] ?? '',
      docenteCodigo: json['docenteCodigo'] ?? '',
      docenteNombreCompleto: json['docenteNombreCompleto'] ?? '',
      docenteCorreo: json['docenteCorreo'],
      horarios: listaHorarios,
    );
  }
}
