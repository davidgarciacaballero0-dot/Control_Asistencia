import React, { useState, useEffect } from 'react';
import { Users, GraduationCap, BookOpen, QrCode, CheckCircle2, Clock, AlertTriangle } from 'lucide-react';
import { academicoApi, asistenciaApi } from '../services/api';

export const DashboardView = ({ setActiveTab }) => {
  const [stats, setStats] = useState({
    estudiantes: 0,
    docentes: 0,
    materias: 0,
    sesionesActivas: 0,
  });
  const [sesiones, setSesiones] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [resEst, resDoc, resMat, resSes] = await Promise.allSettled([
        academicoApi.getEstudiantes(),
        academicoApi.getDocentes(),
        academicoApi.getMaterias(),
        asistenciaApi.getSesionesActivas(),
      ]);

      setStats({
        estudiantes: resEst.status === 'fulfilled' ? resEst.value.data.length : 0,
        docentes: resDoc.status === 'fulfilled' ? resDoc.value.data.length : 0,
        materias: resMat.status === 'fulfilled' ? resMat.value.data.length : 0,
        sesionesActivas: resSes.status === 'fulfilled' ? resSes.value.data.length : 0,
      });

      if (resSes.status === 'fulfilled') {
        setSesiones(resSes.value.data);
      }
    } catch (e) {
      console.error('Error cargando dashboard', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' }}>
            <GraduationCap size={24} />
          </div>
          <div>
            <div className="stat-value">{stats.estudiantes}</div>
            <div className="stat-label">Estudiantes Registrados</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>
            <Users size={24} />
          </div>
          <div>
            <div className="stat-value">{stats.docentes}</div>
            <div className="stat-label">Docentes Activos</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' }}>
            <BookOpen size={24} />
          </div>
          <div>
            <div className="stat-value">{stats.materias}</div>
            <div className="stat-label">Materias Ofertadas</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(139, 92, 246, 0.15)', color: '#8b5cf6' }}>
            <QrCode size={24} />
          </div>
          <div>
            <div className="stat-value">{stats.sesionesActivas}</div>
            <div className="stat-label">Sesiones QR en Curso</div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2 className="card-title">Sesiones de Clase Activas</h2>
          <button className="btn btn-primary btn-sm" onClick={() => setActiveTab('sesiones')}>
            Gestionar Sesiones
          </button>
        </div>

        {sesiones.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '30px', color: 'var(--color-text-muted)' }}>
            No hay sesiones de clase activas en este momento.
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Fecha</th>
                  <th>Hora Inicio</th>
                  <th>Grupo ID</th>
                  <th>Tema</th>
                  <th>Codigo QR</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {sesiones.map((s) => (
                  <tr key={s.id}>
                    <td>{s.id}</td>
                    <td>{s.fecha}</td>
                    <td>{s.horaInicio}</td>
                    <td>Grupo #{s.idGrupoReferencia}</td>
                    <td>{s.tema}</td>
                    <td style={{ fontFamily: 'monospace', color: 'var(--color-primary)' }}>{s.codigoQrGenerado}</td>
                    <td>
                      <span className="badge badge-activa">ACTIVA</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
