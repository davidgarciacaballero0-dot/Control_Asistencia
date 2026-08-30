import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, BookOpen } from 'lucide-react';
import { academicoApi } from '../services/api';

export const MateriasView = () => {
  const [materias, setMaterias] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modoEdicion, setModoEdicion] = useState(false);

  const [formData, setFormData] = useState({
    sigla: '',
    nombre: ''
  });

  useEffect(() => {
    cargarMaterias();
  }, []);

  const cargarMaterias = async () => {
    setLoading(true);
    try {
      const res = await academicoApi.getMaterias();
      setMaterias(res.data);
    } catch (e) {
      console.error('Error cargando materias', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirCrear = () => {
    setModoEdicion(false);
    setFormData({ sigla: '', nombre: '' });
    setShowModal(true);
  };

  const handleAbrirEditar = (m) => {
    setModoEdicion(true);
    setFormData(m);
    setShowModal(true);
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    try {
      if (modoEdicion) {
        await academicoApi.updateMateria(formData.sigla, formData);
      } else {
        await academicoApi.createMateria(formData);
      }
      setShowModal(false);
      cargarMaterias();
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEliminar = async (sigla) => {
    if (!confirm(`Desea eliminar la materia con sigla ${sigla}?`)) return;
    try {
      await academicoApi.deleteMateria(sigla);
      cargarMaterias();
    } catch (err) {
      alert('Error al eliminar: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Gestion de Materias (CU04)</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Catalogo de asignaturas y planes de estudio
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleAbrirCrear}>
          <Plus size={16} />
          <span>Nueva Materia</span>
        </button>
      </div>

      <div className="card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Sigla</th>
                <th>Nombre de la Asignatura</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {materias.length === 0 ? (
                <tr>
                  <td colSpan="3" style={{ textAlign: 'center', padding: '24px', color: 'var(--color-text-muted)' }}>
                    No hay materias registradas.
                  </td>
                </tr>
              ) : (
                materias.map((m) => (
                  <tr key={m.sigla}>
                    <td style={{ fontWeight: 600, color: 'var(--color-primary)' }}>{m.sigla}</td>
                    <td>{m.nombre}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleAbrirEditar(m)}>
                          <Edit2 size={14} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleEliminar(m.sigla)}>
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3 className="modal-title">{modoEdicion ? 'Editar Materia' : 'Nueva Materia'}</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>X</button>
            </div>
            <form onSubmit={handleGuardar}>
              <div className="form-group">
                <label className="form-label">Sigla de la Asignatura</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.sigla}
                  onChange={(e) => setFormData({ ...formData, sigla: e.target.value.toUpperCase() })}
                  placeholder="ej: INF412"
                  required
                  disabled={modoEdicion}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Nombre de la Materia</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.nombre}
                  onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                  placeholder="ej: Arquitectura de Software"
                  required
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary">
                  {modoEdicion ? 'Actualizar' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
