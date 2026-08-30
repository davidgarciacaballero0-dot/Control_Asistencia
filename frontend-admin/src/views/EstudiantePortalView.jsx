import React, { useState, useEffect } from 'react';
import {
  User, BookOpen, Clock, Calendar, CheckCircle2, AlertCircle,
  QrCode, Award, ArrowRight, RefreshCw, BarChart3, Search
} from 'lucide-react';
import { academicoApi, asistenciaApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const EstudiantePortalView = () => {
  const { user } = useAuth();
  const registroEstudiante = user?.identificadorReferencia || user?.username || '2024001';

  const [estudianteInfo, setEstudianteInfo] = useState(null);
  const [materiasInscritas, setMateriasInscritas] = useState([]);
  const [clasesHoy, setClasesHoy] = useState([]);
  const [todasLasClases, setTodasLasClases] = useState([]);
  const [asistenciasHistorial, setAsistenciasHistorial] = useState([]);
  const [sesionesActivasEstudiante, setSesionesActivasEstudiante] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('hoy'); // 'hoy', 'materias', 'historial'

  // Modal para marcar asistencia QR
  const [showModalQr, setShowModalQr] = useState(false);
  const [codigoQrInput, setCodigoQrInput] = useState('');
  const [marcandoAsistencia, setMarcandoAsistencia] = useState(false);
  const [mensajeResultado, setMensajeResultado] = useState(null);

  useEffect(() => {
    cargarDatosCompletos();
  }, [registroEstudiante]);

  const cargarDatosCompletos = async () => {
    setLoading(true);
    try {
      // 1. Perfil del estudiante
      try {
        const resEst = await academicoApi.getEstudiante(registroEstudiante);
        setEstudianteInfo(resEst.data);
      } catch (err) {
        console.warn('No se pudo cargar perfil extendido', err);
      }

      // 2. Materias inscritas
      const resMaterias = await academicoApi.getMateriasInscritas(registroEstudiante);
      const materiasData = resMaterias.data || [];
      setMateriasInscritas(materiasData);

      // 3. Clases de hoy segun horario regular
      const resHoy = await academicoApi.getClasesHoy(registroEstudiante);
      setClasesHoy(resHoy.data || []);

      // 4. Todas las clases semanales
      const resHorarios = await academicoApi.getHorariosEstudiante(registroEstudiante);
      setTodasLasClases(resHorarios.data || []);

      // 5. Historial de asistencias
      const resAsist = await asistenciaApi.getAsistenciasByEstudiante(registroEstudiante);
      const asistenciasData = resAsist.data || [];
      setAsistenciasHistorial(asistenciasData);

      // 6. Sesiones activas en tiempo real
      try {
        const resActivas = await asistenciaApi.getSesionesActivas();
        const activasEst = (resActivas.data || []).map(s => {
          const mat = materiasData.find(m => m.grupoId === s.idGrupoReferencia);
          if (!mat) return null;
          const yaMarco = asistenciasData.some(a => a.idSesion === s.id);
          return {
            ...s,
            materiaSigla: mat.materiaSigla,
            materiaNombre: mat.materiaNombre,
            docenteNombreCompleto: mat.docenteNombreCompleto,
            grupoNombre: mat.grupoNombre,
            yaMarco
          };
        }).filter(Boolean);
        setSesionesActivasEstudiante(activasEst);
      } catch (err) {
        console.warn('No se pudieron consultar sesiones activas', err);
      }
    } catch (e) {
      console.error('Error cargando datos del estudiante', e);
    } finally {
      setLoading(false);
    }
  };

  // Calcular la clase actual o proxima del dia
  const calcularClasePrioritaria = () => {
    if (!clasesHoy || clasesHoy.length === 0) return null;

    const ahora = new Date();
    const minutosActuales = ahora.getHours() * 60 + ahora.getMinutes();

    // Buscar si hay una clase en curso
    const enCurso = clasesHoy.find((c) => {
      const [hIni, mIni] = c.horaInicio.split(':').map(Number);
      const [hFin, mFin] = c.horaFin.split(':').map(Number);
      const minIni = hIni * 60 + mIni;
      const minFin = hFin * 60 + mFin;
      return minutosActuales >= minIni && minutosActuales <= minFin;
    });

    if (enCurso) {
      const yaAsistio = asistenciasHistorial.some(
        (a) => a.fechaRegistro === new Date().toISOString().split('T')[0]
      );
      return { clase: enCurso, tipo: 'EN_CURSO', yaAsistio };
    }

    // Si no esta en curso, buscar la proxima clase de hoy que aun no haya comenzado
    const proxima = clasesHoy.find((c) => {
      const [hIni, mIni] = c.horaInicio.split(':').map(Number);
      const minIni = hIni * 60 + mIni;
      return minIni > minutosActuales;
    });

    if (proxima) {
      return { clase: proxima, tipo: 'PROXIMA', yaAsistio: false };
    }

    // Si todas ya pasaron
    const ultima = clasesHoy[clasesHoy.length - 1];
    return { clase: ultima, tipo: 'FINALIZADAS', yaAsistio: true };
  };

  const prioridad = calcularClasePrioritaria();
  const sesionActiva = sesionesActivasEstudiante.length > 0 ? sesionesActivasEstudiante[0] : null;

  const handleMarcarQrSubmit = async (e) => {
    e.preventDefault();
    if (!codigoQrInput.trim()) return;

    setMarcandoAsistencia(true);
    setMensajeResultado(null);
    try {
      const res = await asistenciaApi.marcarAsistenciaQr({
        registroEstudiante: registroEstudiante,
        codigoQr: codigoQrInput.trim(),
        observacion: 'Marcado desde Portal Web Estudiantil'
      });

      setMensajeResultado({
        tipo: 'exito',
        texto: `Asistencia confirmada exitosamente como ${res.data.estadoAsistencia} a las ${res.data.horaRegistro}.`
      });
      setCodigoQrInput('');
      cargarDatosCompletos();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.mensaje || err.message;
      setMensajeResultado({
        tipo: 'error',
        texto: `Error al registrar: ${msg}`
      });
    } finally {
      setMarcandoAsistencia(false);
    }
  };

  // Calcular estadisticas de asistencia agrupadas por materia
  const calcularEstadisticasPorMateria = () => {
    const stats = {};

    materiasInscritas.forEach((m) => {
      stats[m.materiaSigla] = {
        materiaSigla: m.materiaSigla,
        materiaNombre: m.materiaNombre,
        docenteNombre: m.docenteNombreCompleto,
        total: 0,
        presentes: 0,
        atrasos: 0,
        faltas: 0
      };
    });

    asistenciasHistorial.forEach((a) => {
      // Contar como presente o atraso
      const sigla = Object.keys(stats)[0] || 'INF412';
      if (stats[sigla]) {
        stats[sigla].total += 1;
        if (a.estadoAsistencia === 'PRESENTE') stats[sigla].presentes += 1;
        else if (a.estadoAsistencia === 'ATRASO') stats[sigla].atrasos += 1;
        else if (a.estadoAsistencia === 'FALTA') stats[sigla].faltas += 1;
      }
    });

    return Object.values(stats);
  };

  const estadisticasMaterias = calcularEstadisticasPorMateria();

  return (
    <div>
      {/* 1. Header Perfil del Estudiante */}
      <div className="card" style={{
        background: 'linear-gradient(135deg, var(--color-surface) 0%, var(--color-surface-hover) 100%)',
        borderLeft: '4px solid var(--color-primary)',
        padding: '24px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '56px',
              height: '56px',
              borderRadius: '16px',
              background: 'rgba(59, 130, 246, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary)'
            }}>
              <User size={28} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h2 style={{ fontSize: '1.4rem' }}>{estudianteInfo ? `${estudianteInfo.nombre} ${estudianteInfo.apellidos}` : user?.nombreCompleto || 'Estudiante'}</h2>
                <span className="badge badge-activa">Alumno Regular</span>
              </div>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginTop: '4px' }}>
                {estudianteInfo?.carrera || 'Ingenieria Informatica'} &bull; Plan {estudianteInfo?.plan || '2020'}
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <div style={{ padding: '8px 14px', background: 'var(--color-badge-bg)', borderRadius: '10px', border: '1px solid var(--color-border)' }}>
              <div style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Registro</div>
              <div style={{ fontWeight: 700, color: 'var(--color-primary)', fontSize: '0.95rem' }}>{registroEstudiante}</div>
            </div>
            <div style={{ padding: '8px 14px', background: 'var(--color-badge-bg)', borderRadius: '10px', border: '1px solid var(--color-border)' }}>
              <div style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>C.I.</div>
              <div style={{ fontWeight: 700, color: 'var(--color-success)', fontSize: '0.95rem' }}>{estudianteInfo?.ci || user?.ci || '30000003'}</div>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={cargarDatosCompletos} title="Actualizar datos">
              <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
              <span>Refrescar</span>
            </button>
          </div>
        </div>
      </div>

      {/* 2. Tarjeta Atajo Inteligente para Sesion en Vivo / Clase Actual */}
      {sesionActiva ? (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(59, 130, 246, 0.08) 100%)',
          border: '2px solid var(--color-success)',
          position: 'relative',
          overflow: 'hidden'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                <span className="badge badge-activa" style={{ background: 'var(--color-success)', color: '#fff' }}>
                  CLASE EN VIVO - SESION ACTIVA
                </span>
                {sesionActiva.horaInicio && (
                  <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Clock size={14} />
                    {sesionActiva.horaInicio} - {sesionActiva.horaFin}
                  </span>
                )}
              </div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>
                {sesionActiva.materiaSigla} - {sesionActiva.materiaNombre}
              </h3>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginTop: '4px' }}>
                Docente: <strong>{sesionActiva.docenteNombreCompleto}</strong> &bull; Grupo: {sesionActiva.grupoNombre}
              </p>
              <div style={{ marginTop: '6px' }}>
                <span style={{ fontSize: '0.8rem', padding: '3px 8px', background: 'var(--color-badge-bg)', borderRadius: '6px', border: '1px solid var(--color-border)', color: 'var(--color-primary)' }}>
                  Tema: <strong>{sesionActiva.tema}</strong>
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              {sesionActiva.yaMarco ? (
                <div style={{ padding: '10px 18px', background: 'rgba(16, 185, 129, 0.15)', border: '1px solid var(--color-success)', borderRadius: '10px', display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-success)', fontWeight: 700 }}>
                  <CheckCircle2 size={18} />
                  <span>Asistencia Registrada: PRESENTE</span>
                </div>
              ) : (
                <button
                  className="btn btn-primary"
                  style={{ padding: '12px 24px', fontSize: '0.95rem', background: 'var(--color-success)', borderColor: 'var(--color-success)' }}
                  onClick={() => {
                    setCodigoQrInput(sesionActiva.codigoQrGenerado || '');
                    setShowModalQr(true);
                  }}
                >
                  <QrCode size={18} />
                  <span>Marcar Asistencia QR Ahora</span>
                </button>
              )}
            </div>
          </div>
        </div>
      ) : prioridad && prioridad.clase ? (
        <div className="card" style={{
          background: prioridad.tipo === 'EN_CURSO'
            ? 'linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(16, 185, 129, 0.08) 100%)'
            : 'var(--color-surface)',
          border: prioridad.tipo === 'EN_CURSO' ? '2px solid var(--color-primary)' : '1px solid var(--color-border)',
          position: 'relative',
          overflow: 'hidden'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                <span className={`badge ${prioridad.tipo === 'EN_CURSO' ? 'badge-presente' : 'badge-atraso'}`}>
                  {prioridad.tipo === 'EN_CURSO' ? 'Clase En Curso Hoy' : prioridad.tipo === 'PROXIMA' ? 'Siguiente Clase Programada' : 'Jornada Concluida'}
                </span>
                <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Clock size={14} />
                  {prioridad.clase.horaInicio} - {prioridad.clase.horaFin}
                </span>
              </div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>
                {prioridad.clase.materiaSigla} - {prioridad.clase.materiaNombre}
              </h3>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginTop: '4px' }}>
                Docente: <strong>{prioridad.clase.docenteNombreCompleto}</strong> &bull; Grupo: {prioridad.clase.grupoNombre}
              </p>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                className="btn btn-primary"
                style={{ padding: '12px 24px', fontSize: '0.95rem' }}
                onClick={() => setShowModalQr(true)}
              >
                <QrCode size={18} />
                <span>Marcar Asistencia QR</span>
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {/* 3. Selector de Pestañas */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', borderBottom: '1px solid var(--color-border)', paddingBottom: '12px' }}>
        <button
          className={`btn ${activeTab === 'hoy' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('hoy')}
        >
          <Calendar size={16} />
          <span>Clases de Hoy ({clasesHoy.length + sesionesActivasEstudiante.length})</span>
        </button>
        <button
          className={`btn ${activeTab === 'materias' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('materias')}
        >
          <BookOpen size={16} />
          <span>Mis Materias Inscritas ({materiasInscritas.length})</span>
        </button>
        <button
          className={`btn ${activeTab === 'historial' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('historial')}
        >
          <BarChart3 size={16} />
          <span>Historial y Metricas</span>
        </button>
      </div>

      {/* 4. Contenido de las Pestañas */}
      {activeTab === 'hoy' && (
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Horario de Clases y Sesiones en Vivo para Hoy</h3>
            <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)' }}>
              {new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
            </span>
          </div>

          {sesionesActivasEstudiante.length === 0 && clasesHoy.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '36px', color: 'var(--color-text-muted)' }}>
              No tienes clases programadas para el dia de hoy segun tu boleta de inscripcion.
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Horario / Estado</th>
                    <th>Sigla</th>
                    <th>Materia</th>
                    <th>Grupo</th>
                    <th>Docente</th>
                    <th>Detalle</th>
                    <th>Accion</th>
                  </tr>
                </thead>
                <tbody>
                  {/* Sesiones Activas en Vivo */}
                  {sesionesActivasEstudiante.map((s, idx) => (
                    <tr key={`activa-${idx}`} style={{
                      background: 'rgba(16, 185, 129, 0.08)',
                      borderLeft: '4px solid var(--color-success)'
                    }}>
                      <td>
                        <span className="badge badge-activa" style={{ background: 'var(--color-success)', color: '#fff' }}>
                          EN VIVO AHORA
                        </span>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                          {s.horaInicio} - {s.horaFin}
                        </div>
                      </td>
                      <td style={{ fontWeight: 700, color: 'var(--color-success)' }}>{s.materiaSigla}</td>
                      <td style={{ fontWeight: 600 }}>{s.materiaNombre}</td>
                      <td>{s.grupoNombre}</td>
                      <td>{s.docenteNombreCompleto}</td>
                      <td>
                        <span style={{ fontSize: '0.8rem', color: 'var(--color-primary)' }}>
                          Tema: {s.tema}
                        </span>
                      </td>
                      <td>
                        {s.yaMarco ? (
                          <span className="badge badge-presente">Presente</span>
                        ) : (
                          <button
                            className="btn btn-sm btn-primary"
                            style={{ background: 'var(--color-success)', borderColor: 'var(--color-success)' }}
                            onClick={() => {
                              setCodigoQrInput(s.codigoQrGenerado || '');
                              setShowModalQr(true);
                            }}
                          >
                            <QrCode size={14} />
                            <span>Marcar QR</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}

                  {/* Clases Regulares de Hoy */}
                  {clasesHoy.map((c, idx) => (
                    <tr key={`regular-${idx}`} style={{
                      background: c.enCurso ? 'rgba(59, 130, 246, 0.06)' : 'transparent'
                    }}>
                      <td style={{ fontWeight: 600, color: 'var(--color-primary)' }}>
                        {c.horaInicio} - {c.horaFin}
                      </td>
                      <td style={{ fontWeight: 600 }}>{c.materiaSigla}</td>
                      <td>{c.materiaNombre}</td>
                      <td>{c.grupoNombre}</td>
                      <td>{c.docenteNombreCompleto}</td>
                      <td>
                        {c.enCurso ? (
                          <span className="badge badge-activa">En Curso Ahora</span>
                        ) : c.concluida ? (
                          <span className="badge badge-finalizada">Concluida</span>
                        ) : (
                          <span className="badge badge-atraso">Proxima</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => setShowModalQr(true)}
                        >
                          <QrCode size={14} />
                          <span>QR</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === 'materias' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          {materiasInscritas.length === 0 ? (
            <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '36px', color: 'var(--color-text-muted)' }}>
              No se encontraron materias inscritas para el estudiante.
            </div>
          ) : (
            materiasInscritas.map((m) => (
              <div key={m.grupoId} className="card" style={{ marginBottom: 0 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                  <span className="badge badge-activa">{m.materiaSigla}</span>
                  <span style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', fontWeight: 500 }}>
                    Grupo: {m.grupoNombre}
                  </span>
                </div>
                <h4 style={{ fontSize: '1.1rem', marginBottom: '6px' }}>{m.materiaNombre}</h4>
                <p style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', marginBottom: '14px' }}>
                  Docente: {m.docenteNombreCompleto}
                </p>

                <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
                    Horarios de Clase
                  </div>
                  {m.horarios && m.horarios.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {m.horarios.map((h, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                          <span style={{ fontWeight: 600 }}>{h.dia}</span>
                          <span style={{ color: 'var(--color-primary)' }}>{h.horaInicio} - {h.horaFin}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>Sin horarios asignados</span>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {activeTab === 'historial' && (
        <div>
          {/* Metricas por Materia */}
          <div className="stats-grid">
            {estadisticasMaterias.map((s) => (
              <div key={s.materiaSigla} className="stat-card">
                <div className="stat-icon-wrapper">
                  <Award size={24} />
                </div>
                <div>
                  <div className="stat-label">{s.materiaSigla} - {s.materiaNombre}</div>
                  <div className="stat-value" style={{ color: 'var(--color-primary)' }}>
                    {s.presentes + s.atrasos} asistencias
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                    {s.presentes} presentes, {s.atrasos} atrasos
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Tabla de Registros Detallados */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Detalle Cronologico de Asistencias Registradas</h3>
              <span style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)' }}>
                Total registros: {asistenciasHistorial.length}
              </span>
            </div>

            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Hora</th>
                    <th>Metodo</th>
                    <th>Estado</th>
                    <th>Observacion</th>
                  </tr>
                </thead>
                <tbody>
                  {asistenciasHistorial.length === 0 ? (
                    <tr>
                      <td colSpan="5" style={{ textAlign: 'center', padding: '24px', color: 'var(--color-text-muted)' }}>
                        Aun no se han registrado asistencias para este estudiante.
                      </td>
                    </tr>
                  ) : (
                    asistenciasHistorial.map((a) => (
                      <tr key={a.id}>
                        <td style={{ fontWeight: 600 }}>{a.fechaRegistro}</td>
                        <td style={{ color: 'var(--color-primary)' }}>{a.horaRegistro}</td>
                        <td>{a.metodoValidacion}</td>
                        <td>
                          <span className={`badge ${
                            a.estadoAsistencia === 'PRESENTE' ? 'badge-presente' :
                            a.estadoAsistencia === 'ATRASO' ? 'badge-atraso' : 'badge-falta'
                          }`}>
                            {a.estadoAsistencia}
                          </span>
                        </td>
                        <td style={{ color: 'var(--color-text-secondary)' }}>{a.observacion || '-'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Modal para Ingresar / Confirmar Asistencia con Codigo QR */}
      {showModalQr && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 className="modal-title">Marcar Asistencia con Codigo QR</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModalQr(false)}>X</button>
            </div>

            {mensajeResultado && (
              <div style={{
                padding: '12px 16px',
                borderRadius: '8px',
                marginBottom: '16px',
                background: mensajeResultado.tipo === 'exito' ? 'var(--color-success-bg)' : 'var(--color-danger-bg)',
                border: `1px solid ${mensajeResultado.tipo === 'exito' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
                color: mensajeResultado.tipo === 'exito' ? 'var(--color-success)' : 'var(--color-danger)',
                fontSize: '0.875rem'
              }}>
                {mensajeResultado.texto}
              </div>
            )}

            <form onSubmit={handleMarcarQrSubmit}>
              <div className="form-group">
                <label className="form-label">Codigo QR de la Clase Proyectada</label>
                <input
                  type="text"
                  className="form-input"
                  value={codigoQrInput}
                  onChange={(e) => setCodigoQrInput(e.target.value)}
                  placeholder="ej: QR-XXXXXXXXXXXX"
                  required
                  autoFocus
                />
                <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '4px' }}>
                  Ingrese el codigo alfanumerico que aparece debajo del codigo QR proyectado por el docente.
                </p>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModalQr(false)}>
                  Cerrar
                </button>
                <button type="submit" className="btn btn-primary" disabled={marcandoAsistencia}>
                  {marcandoAsistencia ? 'Validando...' : 'Confirmar Asistencia'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
