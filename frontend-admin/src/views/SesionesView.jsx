import React, { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import {
  Play,
  Square,
  RefreshCw,
  Clock,
  CheckCircle2,
  AlertTriangle,
  QrCode,
  Smartphone,
  Eye,
  Copy,
  Check,
  BookOpen,
  UserCheck,
  Calendar,
  Layers
} from 'lucide-react';
import { asistenciaApi, academicoApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const SesionesView = () => {
  const { user } = useAuth();
  const [sesiones, setSesiones] = useState([]);
  const [grupos, setGrupos] = useState([]);
  const [todosLosGrupos, setTodosLosGrupos] = useState([]);
  const [sesionSeleccionada, setSesionSeleccionada] = useState(null);
  const [asistencias, setAsistencias] = useState([]);
  const [reporte, setReporte] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showIniciarModal, setShowIniciarModal] = useState(false);
  const [copiado, setCopiado] = useState(false);

  // Form para iniciar sesion
  const [nuevoGrupoId, setNuevoGrupoId] = useState('');
  const [nuevoHorarioId, setNuevoHorarioId] = useState('');
  const [nuevoTema, setNuevoTema] = useState('');
  const [validezMinutos, setValidezMinutos] = useState(15);

  // Simulador de escaneo movil
  const [registroSimulado, setRegistroSimulado] = useState('2024001');
  const [resultadoSimulacion, setResultadoSimulacion] = useState(null);
  const [errorSimulacion, setErrorSimulacion] = useState('');

  // Contador regresivo en tiempo real para el QR
  const [tiempoRestante, setTiempoRestante] = useState('');
  const [qrExpirado, setQrExpirado] = useState(false);

  const esDocente = user?.roles?.includes('ROLE_DOCENTE') || user?.rol === 'ROLE_DOCENTE';
  const codigoDocenteActual = user?.identificadorReferencia;

  useEffect(() => {
    cargarSesiones();
    cargarGrupos();
  }, [user]);

  // Contador de segundos en vivo
  useEffect(() => {
    if (!sesionSeleccionada || sesionSeleccionada.estado !== 'ACTIVA' || !sesionSeleccionada.expiracionQr) {
      setTiempoRestante('');
      setQrExpirado(false);
      return;
    }

    const actualizarContador = () => {
      const ahora = new Date().getTime();
      const expDate = new Date(sesionSeleccionada.expiracionQr.replace(' ', 'T')).getTime();
      const diff = expDate - ahora;

      if (diff <= 0) {
        setTiempoRestante('00:00');
        setQrExpirado(true);
      } else {
        setQrExpirado(false);
        const minutos = Math.floor(diff / 60000);
        const segundos = Math.floor((diff % 60000) / 1000);
        const mStr = minutos < 10 ? `0${minutos}` : `${minutos}`;
        const sStr = segundos < 10 ? `0${segundos}` : `${segundos}`;
        setTiempoRestante(`${mStr}:${sStr}`);
      }
    };

    actualizarContador();
    const intervalTimer = setInterval(actualizarContador, 1000);
    return () => clearInterval(intervalTimer);
  }, [sesionSeleccionada?.id, sesionSeleccionada?.expiracionQr, sesionSeleccionada?.estado]);

  useEffect(() => {
    let interval;
    if (sesionSeleccionada) {
      cargarDetalleSesion(sesionSeleccionada.id);
      // Polling cada 4 segundos para actualizar la lista de asistentes en tiempo real
      interval = setInterval(() => {
        cargarDetalleSesion(sesionSeleccionada.id);
      }, 4000);
    }
    return () => clearInterval(interval);
  }, [sesionSeleccionada?.id]);

  const cargarSesiones = async () => {
    try {
      let res;
      if (esDocente && codigoDocenteActual) {
        res = await asistenciaApi.getSesionesByDocente(codigoDocenteActual);
      } else {
        res = await asistenciaApi.getSesionesActivas();
      }

      const lista = res.data || [];
      // Ordenar por ID descendente (la mas reciente primero)
      lista.sort((a, b) => b.id - a.id);
      setSesiones(lista);

      const activa = lista.find(s => s.estado === 'ACTIVA');
      if (activa) {
        setSesionSeleccionada(activa);
      } else if (lista.length > 0 && !sesionSeleccionada) {
        setSesionSeleccionada(lista[0]);
      }
    } catch (e) {
      console.error('Error al cargar sesiones', e);
    }
  };

  const cargarGrupos = async () => {
    try {
      const res = await academicoApi.getGrupos();
      const todos = res.data || [];
      setTodosLosGrupos(todos);

      let listaParaIniciar = todos;
      // Si el usuario es docente, filtrar sus grupos asignados para la creacion
      if (esDocente && codigoDocenteActual) {
        listaParaIniciar = todos.filter(g => g.docenteCodigo === codigoDocenteActual);
      }

      setGrupos(listaParaIniciar);
      if (listaParaIniciar.length > 0) {
        setNuevoGrupoId(listaParaIniciar[0].id);
        if (listaParaIniciar[0].horarios?.length > 0) {
          setNuevoHorarioId(listaParaIniciar[0].horarios[0].id);
        }
      }
    } catch (e) {
      console.error('Error al cargar grupos', e);
    }
  };

  const cargarDetalleSesion = async (sesionId) => {
    try {
      const [resSesion, resAsistencias, resReporte] = await Promise.allSettled([
        asistenciaApi.getSesion(sesionId),
        asistenciaApi.getAsistenciasBySesion(sesionId),
        asistenciaApi.getReporteSesion(sesionId)
      ]);

      if (resSesion.status === 'fulfilled') {
        setSesionSeleccionada(resSesion.value.data);
      }
      if (resAsistencias.status === 'fulfilled') {
        setAsistencias(resAsistencias.value.data);
      }
      if (resReporte.status === 'fulfilled') {
        setReporte(resReporte.value.data);
      }
    } catch (e) {
      console.error('Error al actualizar detalle de sesion', e);
    }
  };

  const handleIniciarSesion = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await asistenciaApi.iniciarSesion({
        idGrupoReferencia: nuevoGrupoId,
        idHorarioReferencia: nuevoHorarioId || 1,
        codigoDocenteReferencia: user?.identificadorReferencia || 'DOC-101',
        tema: nuevoTema || 'Clase regular',
        minutosValidezQr: validezMinutos
      });

      setShowIniciarModal(false);
      setSesionSeleccionada(res.data);
      await cargarSesiones();
    } catch (err) {
      alert('Error al iniciar sesion: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleFinalizarSesion = async () => {
    if (!sesionSeleccionada) return;
    try {
      setLoading(true);
      const res = await asistenciaApi.finalizarSesion(sesionSeleccionada.id);
      setSesionSeleccionada(res.data);
      await cargarSesiones();
      await cargarDetalleSesion(sesionSeleccionada.id);
    } catch (err) {
      alert('Error al finalizar sesion: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleRegenerarQr = async () => {
    if (!sesionSeleccionada) return;
    try {
      const res = await asistenciaApi.regenerarQr(sesionSeleccionada.id, 15);
      setSesionSeleccionada(res.data);
    } catch (err) {
      alert('Error al regenerar QR: ' + err.message);
    }
  };

  const handleCopiarQr = () => {
    if (!sesionSeleccionada?.codigoQrGenerado) return;
    navigator.clipboard.writeText(sesionSeleccionada.codigoQrGenerado);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2000);
  };

  // Metodo para probar y simular la marcacion desde la misma UI
  const handleSimularEscaneo = async (e) => {
    e.preventDefault();
    setResultadoSimulacion(null);
    setErrorSimulacion('');

    if (!sesionSeleccionada) {
      setErrorSimulacion('No hay ninguna sesion activa seleccionada');
      return;
    }

    try {
      const res = await asistenciaApi.marcarAsistenciaQr({
        registroEstudiante: registroSimulado,
        codigoQr: sesionSeleccionada.codigoQrGenerado
      });
      setResultadoSimulacion(res.data);
      cargarDetalleSesion(sesionSeleccionada.id);
    } catch (err) {
      setErrorSimulacion(err.response?.data?.message || err.response?.data || err.message);
    }
  };

  // Resolucion de los datos academicos asociados a la sesion
  const grupoAsociado = todosLosGrupos.find(g => Number(g.id) === Number(sesionSeleccionada?.idGrupoReferencia));
  const horarioAsociado = grupoAsociado?.horarios?.find(h => Number(h.id) === Number(sesionSeleccionada?.idHorarioReferencia)) ||
    (grupoAsociado?.horarios?.length > 0 ? grupoAsociado.horarios[0] : null);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Control de Asistencia en Vivo</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Generacion de Codigo QR dinamico y Monitoreo de Asistencia en Tiempo Real
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowIniciarModal(true)}>
          <Play size={16} />
          <span>Iniciar Nueva Sesion</span>
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
        {/* Columna Izquierda: Proyector de QR con Informacion Academica Completa */}
        <div>
          {sesionSeleccionada ? (
            <div className="qr-presenter">
              {/* Barra superior de badges */}
              <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className={`badge ${sesionSeleccionada.estado === 'ACTIVA' ? 'badge-activa' : 'badge-finalizada'}`}>
                    Sesion #{sesionSeleccionada.id} - {sesionSeleccionada.estado}
                  </span>
                  {grupoAsociado?.materiaSigla && (
                    <span className="badge badge-presente" style={{ fontWeight: 700 }}>
                      {grupoAsociado.materiaSigla}
                    </span>
                  )}
                </div>

                <span
                  className="timer-badge"
                  style={{
                    background: qrExpirado ? 'rgba(239, 68, 68, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                    color: qrExpirado ? '#ef4444' : '#f59e0b',
                    border: qrExpirado ? '1px solid #ef4444' : '1px solid rgba(245, 158, 11, 0.3)',
                    letterSpacing: '0.5px'
                  }}
                >
                  <Clock size={14} />
                  <span>{qrExpirado ? 'QR EXPIRADO (00:00)' : `Tiempo Restante: ${tiempoRestante}`}</span>
                </span>
              </div>

              {/* Titulo del Tema */}
              <h3 style={{ marginTop: '16px', fontSize: '1.35rem', color: 'var(--color-text-primary)' }}>
                {sesionSeleccionada.tema}
              </h3>

              {/* Ficha de Detalles Academicos Completos */}
              <div className="qr-academic-card">
                <div className="qr-academic-grid">
                  <div className="qr-academic-item">
                    <span className="label">Materia</span>
                    <span className="value">
                      {grupoAsociado ? `${grupoAsociado.materiaSigla} - ${grupoAsociado.materiaNombre}` : 'Materia Universitaria'}
                    </span>
                  </div>

                  <div className="qr-academic-item">
                    <span className="label">Grupo y Cupo</span>
                    <span className="value">
                      Grupo {grupoAsociado?.nombre || sesionSeleccionada.idGrupoReferencia} {grupoAsociado?.cupo ? `(${grupoAsociado.cupo} alumnos)` : ''}
                    </span>
                  </div>

                  <div className="qr-academic-item">
                    <span className="label">Docente a Cargo</span>
                    <span className="value">
                      {grupoAsociado?.docenteNombreCompleto || sesionSeleccionada.codigoDocenteReferencia}
                    </span>
                  </div>

                  <div className="qr-academic-item">
                    <span className="label">Horario de Clase</span>
                    <span className="value">
                      {horarioAsociado ? `${horarioAsociado.dia} ${horarioAsociado.horaInicio} - ${horarioAsociado.horaFin}` : 'Clase en Curso'}
                    </span>
                  </div>

                  <div className="qr-academic-item">
                    <span className="label">Fecha y Apertura</span>
                    <span className="value">
                      {sesionSeleccionada.fecha} | {sesionSeleccionada.horaInicio || 'En curso'}
                    </span>
                  </div>

                  <div className="qr-academic-item">
                    <span className="label">Asistentes en Vivo</span>
                    <span className="value" style={{ color: '#10b981' }}>
                      {asistencias.length} {grupoAsociado?.cupo ? `/ ${grupoAsociado.cupo} registrados` : 'marcaciones'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Contenedor del Codigo QR de Alto Contraste */}
              <div
                className="qr-box"
                style={{
                  position: 'relative',
                  opacity: qrExpirado ? 0.4 : 1,
                  filter: qrExpirado ? 'grayscale(90%)' : 'none',
                  transition: 'all 0.3s ease'
                }}
              >
                {sesionSeleccionada.estado === 'ACTIVA' ? (
                  <QRCodeSVG
                    value={sesionSeleccionada.codigoQrGenerado || 'EMPTY'}
                    size={220}
                    level="H"
                    includeMargin={true}
                  />
                ) : (
                  <div style={{ width: 220, height: 220, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#64748b', gap: '8px' }}>
                    <Square size={32} />
                    <span style={{ fontWeight: 600 }}>Sesion Finalizada</span>
                  </div>
                )}
              </div>

              {/* Alerta de QR Expirado */}
              {qrExpirado && sesionSeleccionada.estado === 'ACTIVA' && (
                <div style={{
                  padding: '10px 14px',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  color: '#ef4444',
                  fontSize: '0.85rem',
                  marginTop: '8px',
                  textAlign: 'center',
                  fontWeight: 500
                }}>
                  El tiempo de validez del codigo QR ha expirado. Presione 'Regenerar QR' para habilitar nuevas marcaciones.
                </div>
              )}

              {/* Codigo QR en texto con boton de copiado */}
              <div className="qr-code-pill">
                <span>{sesionSeleccionada.codigoQrGenerado}</span>
                <button
                  onClick={handleCopiarQr}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-primary)', display: 'flex', alignItems: 'center' }}
                  title="Copiar codigo al portapapeles"
                >
                  {copiado ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
                </button>
              </div>

              <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginTop: '6px' }}>
                Enfoque la camara desde la aplicacion movil de asistencia institucional
              </div>

              {/* Botones de Control de la Sesion */}
              <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                {sesionSeleccionada.estado === 'ACTIVA' && (
                  <>
                    <button className="btn btn-secondary btn-sm" onClick={handleRegenerarQr}>
                      <RefreshCw size={14} />
                      <span>Regenerar QR (+15 min)</span>
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={handleFinalizarSesion}>
                      <Square size={14} />
                      <span>Finalizar Sesion</span>
                    </button>
                  </>
                )}
              </div>
            </div>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '60px 20px' }}>
              <QrCode size={48} style={{ color: 'var(--color-text-muted)', margin: '0 auto 16px' }} />
              <h3>No hay sesion seleccionada</h3>
              <p style={{ color: 'var(--color-text-secondary)', marginTop: '8px' }}>
                Inicie una nueva sesion de clase para generar el codigo QR interactivo.
              </p>
            </div>
          )}

          {/* Panel Simulador de Marcacion Movil */}
          <div className="card" style={{ marginTop: '24px' }}>
            <div className="card-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Smartphone size={18} color="#3b82f6" />
                <h3 className="card-title">Simulador de Marcacion Movil (Pruebas)</h3>
              </div>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', marginBottom: '14px' }}>
              Simula la lectura de este QR desde el telefono de un estudiante inscrito.
            </p>

            <form onSubmit={handleSimularEscaneo} style={{ display: 'flex', gap: '12px' }}>
              <input
                type="text"
                className="form-input"
                value={registroSimulado}
                onChange={(e) => setRegistroSimulado(e.target.value)}
                placeholder="Registro (ej: 2024001, 2024010)"
                required
                style={{ flex: 1 }}
              />
              <button type="submit" className="btn btn-primary btn-sm">
                Marcar Asistencia
              </button>
            </form>

            {resultadoSimulacion && (
              <div style={{
                marginTop: '14px',
                padding: '12px',
                background: 'var(--color-success-bg)',
                border: '1px solid rgba(16, 185, 129, 0.3)',
                borderRadius: 'var(--radius-md)',
                color: 'var(--color-success)',
                fontSize: '0.85rem'
              }}>
                <div style={{ fontWeight: 600 }}>Asistencia Confirmada Exitosamente:</div>
                <div>Estudiante: {resultadoSimulacion.nombreEstudiante} ({resultadoSimulacion.registroEstudiante})</div>
                <div>Estado: <strong>{resultadoSimulacion.estadoAsistencia}</strong> | Hora: {resultadoSimulacion.horaRegistro}</div>
              </div>
            )}

            {errorSimulacion && (
              <div style={{
                marginTop: '14px',
                padding: '12px',
                background: 'var(--color-danger-bg)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                borderRadius: 'var(--radius-md)',
                color: 'var(--color-danger)',
                fontSize: '0.85rem'
              }}>
                <strong>Rechazado:</strong> {errorSimulacion}
              </div>
            )}
          </div>
        </div>

        {/* Columna Derecha: Lista de Asistencias en Tiempo Real */}
        <div>
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Asistencias en Vivo ({asistencias.length})</h3>
              {reporte && (
                <div style={{ display: 'flex', gap: '8px', fontSize: '0.75rem' }}>
                  <span className="badge badge-presente">Presentes: {reporte.totalPresentes}</span>
                  <span className="badge badge-atraso">Atrasos: {reporte.totalAtrasos}</span>
                </div>
              )}
            </div>

            {asistencias.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px', color: 'var(--color-text-muted)' }}>
                Esperando marcaciones de estudiantes...
              </div>
            ) : (
              <div className="table-container" style={{ maxHeight: '420px', overflowY: 'auto' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Hora</th>
                      <th>Registro</th>
                      <th>Estudiante</th>
                      <th>Metodo</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {asistencias.map((a) => (
                      <tr key={a.id}>
                        <td>{a.horaRegistro}</td>
                        <td style={{ fontWeight: 600 }}>{a.registroEstudiante}</td>
                        <td style={{ fontWeight: 500 }}>{a.nombreEstudiante || 'Estudiante'}</td>
                        <td>
                          <span className="badge badge-activa">{a.metodoValidacion}</span>
                        </td>
                        <td>
                          <span className={`badge ${a.estadoAsistencia === 'PRESENTE' ? 'badge-presente' : 'badge-atraso'}`}>
                            {a.estadoAsistencia}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Historial de Sesiones Activas */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Otras Sesiones Activas</h3>
            </div>
            {sesiones.length === 0 ? (
              <div style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>No hay otras sesiones.</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {sesiones.map((s) => (
                  <div
                    key={s.id}
                    onClick={() => setSesionSeleccionada(s)}
                    style={{
                      padding: '10px 14px',
                      borderRadius: 'var(--radius-md)',
                      background: sesionSeleccionada?.id === s.id ? 'var(--color-surface-hover)' : 'rgba(255,255,255,0.02)',
                      border: '1px solid var(--color-border)',
                      cursor: 'pointer',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{s.tema}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                        Grupo #{s.idGrupoReferencia} - {s.horaInicio}
                      </div>
                    </div>
                    <Eye size={16} color="#3b82f6" />
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modal Iniciar Sesion */}
      {showIniciarModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 className="modal-title">Iniciar Sesion de Clase</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowIniciarModal(false)}>X</button>
            </div>
            <form onSubmit={handleIniciarSesion}>
              <div className="form-group">
                <label className="form-label">Grupo Academico</label>
                <select
                  className="form-select"
                  value={nuevoGrupoId}
                  onChange={(e) => setNuevoGrupoId(e.target.value)}
                  required
                >
                  {grupos.map((g) => (
                    <option key={g.id} value={g.id}>
                      {g.materiaSigla} - Grupo {g.nombre} ({g.docenteNombreCompleto})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Tema o Contenido de la Clase</label>
                <input
                  type="text"
                  className="form-input"
                  value={nuevoTema}
                  onChange={(e) => setNuevoTema(e.target.value)}
                  placeholder="ej: Patrones Arquitectonicos y Microservicios"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Tiempo de Validez del Codigo QR (Minutos)</label>
                <input
                  type="number"
                  className="form-input"
                  value={validezMinutos}
                  onChange={(e) => setValidezMinutos(Number(e.target.value))}
                  min={1}
                  max={120}
                  required
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowIniciarModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Iniciando...' : 'Generar Sesion y QR'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
