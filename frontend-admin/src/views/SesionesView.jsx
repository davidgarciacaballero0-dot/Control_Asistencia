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
  Eye
} from 'lucide-react';
import { asistenciaApi, academicoApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const SesionesView = () => {
  const { user } = useAuth();
  const [sesiones, setSesiones] = useState([]);
  const [grupos, setGrupos] = useState([]);
  const [sesionSeleccionada, setSesionSeleccionada] = useState(null);
  const [asistencias, setAsistencias] = useState([]);
  const [reporte, setReporte] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showIniciarModal, setShowIniciarModal] = useState(false);

  // Form para iniciar sesion
  const [nuevoGrupoId, setNuevoGrupoId] = useState('');
  const [nuevoHorarioId, setNuevoHorarioId] = useState('');
  const [nuevoTema, setNuevoTema] = useState('');
  const [validezMinutos, setValidezMinutos] = useState(15);

  // Simulador de escaneo movil
  const [registroSimulado, setRegistroSimulado] = useState('2024001');
  const [resultadoSimulacion, setResultadoSimulacion] = useState(null);
  const [errorSimulacion, setErrorSimulacion] = useState('');

  useEffect(() => {
    cargarSesiones();
    cargarGrupos();
  }, []);

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
      const res = await asistenciaApi.getSesionesActivas();
      setSesiones(res.data);
      if (res.data.length > 0 && !sesionSeleccionada) {
        setSesionSeleccionada(res.data[0]);
      }
    } catch (e) {
      console.error('Error al cargar sesiones', e);
    }
  };

  const cargarGrupos = async () => {
    try {
      const res = await academicoApi.getGrupos();
      setGrupos(res.data);
      if (res.data.length > 0) {
        setNuevoGrupoId(res.data[0].id);
        if (res.data[0].horarios?.length > 0) {
          setNuevoHorarioId(res.data[0].horarios[0].id);
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
      cargarSesiones();
    } catch (err) {
      alert('Error al iniciar sesion: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleFinalizarSesion = async () => {
    if (!sesionSeleccionada) return;
    if (!confirm('Desea finalizar la sesion de clase? El codigo QR quedara invalidado.')) return;

    try {
      await asistenciaApi.finalizarSesion(sesionSeleccionada.id);
      cargarSesiones();
      cargarDetalleSesion(sesionSeleccionada.id);
    } catch (err) {
      alert('Error al finalizar sesion: ' + err.message);
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

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Control de Asistencia en Vivo</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Generacion de Codigo QR y Monitoreo de Asistencia en Tiempo Real
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowIniciarModal(true)}>
          <Play size={16} />
          <span>Iniciar Nueva Sesion</span>
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
        {/* Columna Izquierda: Proyector de QR */}
        <div>
          {sesionSeleccionada ? (
            <div className="qr-presenter">
              <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                <span className="badge badge-activa">
                  Sesion #{sesionSeleccionada.id} - {sesionSeleccionada.estado}
                </span>
                <span className="timer-badge">
                  <Clock size={14} />
                  <span>Expira: {sesionSeleccionada.expiracionQr?.substring(11, 16) || '15 min'}</span>
                </span>
              </div>

              <h3 style={{ marginTop: '16px', fontSize: '1.3rem' }}>{sesionSeleccionada.tema}</h3>
              <div style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem' }}>
                Grupo Referencia: #{sesionSeleccionada.idGrupoReferencia} | Fecha: {sesionSeleccionada.fecha}
              </div>

              {/* Contenedor del QR proyectable */}
              <div className="qr-box">
                {sesionSeleccionada.estado === 'ACTIVA' ? (
                  <QRCodeSVG
                    value={sesionSeleccionada.codigoQrGenerado || 'EMPTY'}
                    size={220}
                    level="H"
                    includeMargin={true}
                  />
                ) : (
                  <div style={{ width: 220, height: 220, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>
                    Sesion Finalizada
                  </div>
                )}
              </div>

              <div className="qr-code-text">
                {sesionSeleccionada.codigoQrGenerado}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginTop: '6px' }}>
                Escanear desde la aplicacion movil de asistencia
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                {sesionSeleccionada.estado === 'ACTIVA' && (
                  <>
                    <button className="btn btn-secondary btn-sm" onClick={handleRegenerarQr}>
                      <RefreshCw size={14} />
                      <span>Regenerar QR</span>
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
              Simula la lectura de este QR desde el telefono de un estudiante registrado.
            </p>

            <form onSubmit={handleSimularEscaneo} style={{ display: 'flex', gap: '12px' }}>
              <input
                type="text"
                className="form-input"
                value={registroSimulado}
                onChange={(e) => setRegistroSimulado(e.target.value)}
                placeholder="Registro (ej: 2024001, 2024002)"
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
                <div style={{ fontWeight: 600 }}>Asistencia Confirmada:</div>
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
                      <th>Metodo</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {asistencias.map((a) => (
                      <tr key={a.id}>
                        <td>{a.horaRegistro}</td>
                        <td style={{ fontWeight: 600 }}>{a.registroEstudiante}</td>
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
