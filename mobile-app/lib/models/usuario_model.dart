class UsuarioModel {
  final String token;
  final String tipo;
  final String username;
  final String nombreCompleto;
  final String email;
  final String? identificadorReferencia;
  final List<String> roles;

  UsuarioModel({
    required this.token,
    required this.tipo,
    required this.username,
    required this.nombreCompleto,
    required this.email,
    this.identificadorReferencia,
    required this.roles,
  });

  factory UsuarioModel.fromJson(Map<String, dynamic> json) {
    return UsuarioModel(
      token: json['token'] ?? '',
      tipo: json['tipo'] ?? 'Bearer',
      username: json['username'] ?? '',
      nombreCompleto: json['nombreCompleto'] ?? '',
      email: json['email'] ?? '',
      identificadorReferencia: json['identificadorReferencia'],
      roles: (json['roles'] as List<dynamic>?)?.map((e) => e.toString()).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'token': token,
      'tipo': tipo,
      'username': username,
      'nombreCompleto': nombreCompleto,
      'email': email,
      'identificadorReferencia': identificadorReferencia,
      'roles': roles,
    };
  }
}
