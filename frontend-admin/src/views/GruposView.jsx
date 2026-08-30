import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, Layers, Clock } from 'lucide-react';
import { academicoApi } from '../services/api';

export const GruposView = () => {
  const [grupos, setGrupos] = useState([]);
  const [materias, setMaterias] = useState([]);
  const [docentes, setDocentes] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal Crear Grupo
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    nombre: 'SC',
    cupo: 40,
    materiaSigla: '',
    docenteCodigo: ''
  });

  // Modal Agregar Horario
  const [showHorarioModal, setShowHorarioModal] = useState(false);
  const [grupoParaHorario, setGrupoParaHorario] = useState(null);
  const [horarioData, setHorarioData] = useState({
    dia: 'LUNES',
    horaInicio: '07:00',
    horaFin: '09:15'
  });

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [resGrupos, resMat, resDoc] = await Promise.all([
        academicoApi.getGrupos(),
        academicoApi.getMaterias(),
        academicoApi.getDocentes()
      ]);
      setGrupos(resGrupos.data);
      setMaterias(resMat.data);
      setDocentes(resDoc.data);

      if (resMat.data.length > 0 && resDoc.data.length > 0) {
        setFormData({
          nombre: 'SC',
          cupo: 40,
          materiaSigla: resMat.data[0].sigla,
          docenteCodigo: resDoc.data[0].codigo
        });
      }
    } catch (e) {
      console.error('Error cargando grupos', e);
    } finally {
      setLoading(false);
    }
  };

  const handleCrearGrupo = async (e) => {
    e.preventDefault();
    try {
      await academicoApi.createGrupo(formData);
      setShowModal(false);
      cargarDatos();
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEliminarGrupo = async (id) => {
    if (!confirm(`Desea eliminar el grupo #${id}?`)) return;
    try {
      await academicoApi.deleteGrupo(id);
      cargarDatos();
    } catch (err) {
      alert('Error al eliminar grupo: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleAbrirHorarioModal = (grupo) => {
    setGrupoParaHorario(grupo);
    setShowHorarioModal(true);
  };

  const handleAgregarHorario = async (e) => {
    e.preventDefault();
    try {
      await academicoApi.addHorario(grupoParaHorario.id, horarioData);
      setShowHorarioModal(false);
      cargarDatos();
    } catch (err) {
      alert('Error al agregar horario: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEliminarHorario = async (horarioId) => {
    if (!confirm('Desea eliminar este horario?')) return;
    try {
      await academicoApi.deleteHorario(horarioId);
      cargarDatos();
    } catch (err) {
      alert('Error al eliminar horario: ' + err.message);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Gestion de Grupos y Horarios (CU05)</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Asignacion de cupos, docentes y franjas horarias
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <Plus size={16} />
          <span>Nuevo Grupo</span>
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '20px' }}>
        {grupos.map((g) => (
          <div key={g.id} className="card" style={{ marginBottom: 0 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
              <div>
                <span className="badge badge-activa" style={{ marginBottom: '6px' }}>{g.materiaSigla}</span>
                <h3 style={{ fontSize: '1.15rem' }}>{g.materiaNombre}</h3>
                <div style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem' }}>
                  Grupo: <strong>{g.nombre}</strong> | Cupo: {g.cupo} estudiantes
                </div>
              </div>
              <button className="btn btn-danger btn-sm" onClick={() => handleEliminarGrupo(g.id)} style={{ padding: '6px 8px' }}>
                <Trash2 size={14} />
              </button>
            </div>

            <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px', marginTop: '12px' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginBottom: '4px' }}>Docente Asignado:</div>
              <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{g.docenteNombreCompleto} ({g.docenteCodigo})</div>
            </div>

            <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px', marginTop: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>Horarios de Clase:</span>
                <button className="btn btn-secondary btn-sm" onClick={() => handleAbrirHorarioModal(g)} style={{ padding: '4px 8px', fontSize: '0.75rem' }}>
                  + Horario
                </button>
              </div>

              {g.horarios?.length === 0 ? (
                <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>Sin horarios asignados.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {g.horarios?.map((h) => (
                    <div key={h.id} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      background: 'rgba(255, 255, 255, 0.02)',
                      padding: '6px 10px',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.8rem'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Clock size={12} color="#3b82f6" />
                        <span><strong>{h.dia}:</strong> {h.horaInicio} - {h.horaFin}</span>
                      </div>
                      <button
                        onClick={() => handleEliminarHorario(h.id)}
                        style={{ background: 'none', border: 'none', color: 'var(--color-danger)', cursor: 'pointer' }}
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Modal Crear Grupo */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 className="modal-title">Registrar Nuevo Grupo</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>X</button>
            </div>
            <form onSubmit={handleCrearGrupo}>
              <div className="form-group">
                <label className="form-label">Materia</label>
                <select
                  className="form-select"
                  value={formData.materiaSigla}
                  onChange={(e) => setFormData({ ...formData, materiaSigla: e.target.value })}
                  required
                >
                  {materias.map((m) => (
                    <option key={m.sigla} value={m.sigla}>{m.sigla} - {m.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Docente</label>
                <select
                  className="form-select"
                  value={formData.docenteCodigo}
                  onChange={(e) => setFormData({ ...formData, docenteCodigo: e.target.value })}
                  required
                >
                  {docentes.map((d) => (
                    <option key={d.codigo} value={d.codigo}>{d.codigo} - {d.nombre} {d.apellidos}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Nombre o Sigla del Grupo</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.nombre}
                  onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                  placeholder="ej: SC, SA, SB"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Cupo Maximo de Estudiantes</label>
                <input
                  type="number"
                  className="form-input"
                  value={formData.cupo}
                  onChange={(e) => setFormData({ ...formData, cupo: Number(e.target.value) })}
                  min={1}
                  required
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary">Guardar Grupo</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Agregar Horario */}
      {showHorarioModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 className="modal-title">Agregar Horario a {grupoParaHorario?.materiaSigla} ({grupoParaHorario?.nombre})</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowHorarioModal(false)}>X</button>
            </div>
            <form onSubmit={handleAgregarHorario}>
              <div className="form-group">
                <label className="form-label">Dia de la Semana</label>
                <select
                  className="form-select"
                  value={horarioData.dia}
                  onChange={(e) => setHorarioData({ ...horarioData, dia: e.target.value })}
                  required
                >
                  <option value="LUNES">LUNES</option>
                  <option value="MARTES">MARTES</option>
                  <option value="MIERCOLES">MIERCOLES</option>
                  <option value="JUEVES">JUEVES</option>
                  <option value="VIERNES">VIERNES</option>
                  <option value="SABADO">SABADO</option>
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label className="form-label">Hora Inicio (HH:mm)</label>
                  <input
                    type="time"
                    className="form-input"
                    value={horarioData.horaInicio}
                    onChange={(e) => setHorarioData({ ...horarioData, horaInicio: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Hora Fin (HH:mm)</label>
                  <input
                    type="time"
                    className="form-input"
                    value={horarioData.horaFin}
                    onChange={(e) => setHorarioData({ ...horarioData, horaFin: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowHorarioModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary">Agregar Horario</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
