import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, GraduationCap, Search } from 'lucide-react';
import { academicoApi } from '../services/api';

export const EstudiantesView = () => {
  const [estudiantes, setEstudiantes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modoEdicion, setModoEdicion] = useState(false);
  const [busqueda, setBusqueda] = useState('');

  const [formData, setFormData] = useState({
    registro: '',
    apellidos: '',
    nombre: '',
    ci: '',
    telefono: '',
    correo: '',
    carrera: 'Ingenieria Informatica',
    plan: '2020'
  });

  useEffect(() => {
    cargarEstudiantes();
  }, []);

  const cargarEstudiantes = async () => {
    setLoading(true);
    try {
      const res = await academicoApi.getEstudiantes();
      setEstudiantes(res.data);
    } catch (e) {
      console.error('Error cargando estudiantes', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirCrear = () => {
    setModoEdicion(false);
    setFormData({
      registro: '',
      apellidos: '',
      nombre: '',
      ci: '',
      telefono: '',
      correo: '',
      carrera: 'Ingenieria Informatica',
      plan: '2020'
    });
    setShowModal(true);
  };

  const handleAbrirEditar = (est) => {
    setModoEdicion(true);
    setFormData(est);
    setShowModal(true);
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    try {
      if (modoEdicion) {
        await academicoApi.updateEstudiante(formData.registro, formData);
      } else {
        await academicoApi.createEstudiante(formData);
      }
      setShowModal(false);
      cargarEstudiantes();
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEliminar = async (registro) => {
    if (!confirm(`Desea eliminar al estudiante con registro ${registro}?`)) return;
    try {
      await academicoApi.deleteEstudiante(registro);
      cargarEstudiantes();
    } catch (err) {
      alert('Error al eliminar: ' + (err.response?.data?.message || err.message));
    }
  };

  const estudiantesFiltrados = estudiantes.filter(e =>
    e.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    e.apellidos.toLowerCase().includes(busqueda.toLowerCase()) ||
    e.registro.toLowerCase().includes(busqueda.toLowerCase()) ||
    (e.ci && e.ci.toLowerCase().includes(busqueda.toLowerCase())) ||
    e.carrera.toLowerCase().includes(busqueda.toLowerCase())
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Gestion de Estudiantes (CU03)</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Padron estudiantil universitario e inscripciones
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleAbrirCrear}>
          <Plus size={16} />
          <span>Nuevo Estudiante</span>
        </button>
      </div>

      <div className="card">
        <div style={{ marginBottom: '18px', maxWidth: '340px', position: 'relative' }}>
          <input
            type="text"
            className="form-input"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por nombre, registro, CI, carrera..."
            style={{ paddingLeft: '36px' }}
          />
          <Search size={16} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--color-text-muted)' }} />
        </div>

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Registro</th>
                <th>CI</th>
                <th>Nombre Completo</th>
                <th>Carrera</th>
                <th>Plan</th>
                <th>Telefono</th>
                <th>Correo</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {estudiantesFiltrados.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '24px', color: 'var(--color-text-muted)' }}>
                    No se encontraron estudiantes registrados.
                  </td>
                </tr>
              ) : (
                estudiantesFiltrados.map((e) => (
                  <tr key={e.registro}>
                    <td style={{ fontWeight: 600, color: 'var(--color-primary)' }}>{e.registro}</td>
                    <td style={{ fontWeight: 500 }}>{e.ci || '-'}</td>
                    <td>{e.nombre} {e.apellidos}</td>
                    <td>{e.carrera}</td>
                    <td>{e.plan}</td>
                    <td>{e.telefono || '-'}</td>
                    <td>{e.correo}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleAbrirEditar(e)}>
                          <Edit2 size={14} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleEliminar(e.registro)}>
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
              <h3 className="modal-title">{modoEdicion ? 'Editar Estudiante' : 'Registrar Nuevo Estudiante'}</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>X</button>
            </div>
            <form onSubmit={handleGuardar}>
              <div className="form-group">
                <label className="form-label">Numero de Registro Universitario</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.registro}
                  onChange={(e) => setFormData({ ...formData, registro: e.target.value })}
                  placeholder="ej: 2024005"
                  required
                  disabled={modoEdicion}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Cedula de Identidad (CI)</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.ci || ''}
                  onChange={(e) => setFormData({ ...formData, ci: e.target.value })}
                  placeholder="ej: 8901234 SC"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Nombre</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.nombre}
                  onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Apellidos</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.apellidos}
                  onChange={(e) => setFormData({ ...formData, apellidos: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Carrera</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.carrera}
                  onChange={(e) => setFormData({ ...formData, carrera: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Plan de Estudios</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.plan}
                  onChange={(e) => setFormData({ ...formData, plan: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Telefono</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.telefono}
                  onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Correo Electronico</label>
                <input
                  type="email"
                  className="form-input"
                  value={formData.correo}
                  onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
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
