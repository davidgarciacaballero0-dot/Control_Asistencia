import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, Users, Search } from 'lucide-react';
import { academicoApi } from '../services/api';

export const DocentesView = () => {
  const [docentes, setDocentes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modoEdicion, setModoEdicion] = useState(false);
  const [busqueda, setBusqueda] = useState('');

  const [formData, setFormData] = useState({
    codigo: '',
    apellidos: '',
    nombre: '',
    ci: '',
    telefono: '',
    correo: ''
  });

  useEffect(() => {
    cargarDocentes();
  }, []);

  const cargarDocentes = async () => {
    setLoading(true);
    try {
      const res = await academicoApi.getDocentes();
      setDocentes(res.data);
    } catch (e) {
      console.error('Error cargando docentes', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirCrear = () => {
    setModoEdicion(false);
    setFormData({ codigo: '', apellidos: '', nombre: '', ci: '', telefono: '', correo: '' });
    setShowModal(true);
  };

  const handleAbrirEditar = (docente) => {
    setModoEdicion(true);
    setFormData(docente);
    setShowModal(true);
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    try {
      if (modoEdicion) {
        await academicoApi.updateDocente(formData.codigo, formData);
      } else {
        await academicoApi.createDocente(formData);
      }
      setShowModal(false);
      cargarDocentes();
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleEliminar = async (codigo) => {
    if (!confirm(`Desea eliminar al docente con codigo ${codigo}?`)) return;
    try {
      await academicoApi.deleteDocente(codigo);
      cargarDocentes();
    } catch (err) {
      alert('Error al eliminar docente: ' + (err.response?.data?.message || err.message));
    }
  };

  const docentesFiltrados = docentes.filter(d =>
    d.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    d.apellidos.toLowerCase().includes(busqueda.toLowerCase()) ||
    d.codigo.toLowerCase().includes(busqueda.toLowerCase()) ||
    (d.ci && d.ci.toLowerCase().includes(busqueda.toLowerCase()))
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Gestion de Docentes (CU02)</h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            Registro y administracion del cuerpo docente universitario
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleAbrirCrear}>
          <Plus size={16} />
          <span>Nuevo Docente</span>
        </button>
      </div>

      <div className="card">
        <div style={{ marginBottom: '18px', maxWidth: '320px', position: 'relative' }}>
          <input
            type="text"
            className="form-input"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por nombre, apellido, CI o codigo..."
            style={{ paddingLeft: '36px' }}
          />
          <Search size={16} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--color-text-muted)' }} />
        </div>

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Codigo</th>
                <th>CI</th>
                <th>Nombre Completo</th>
                <th>Telefono</th>
                <th>Correo Electronico</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {docentesFiltrados.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '24px', color: 'var(--color-text-muted)' }}>
                    No se encontraron docentes registrados.
                  </td>
                </tr>
              ) : (
                docentesFiltrados.map((d) => (
                  <tr key={d.codigo}>
                    <td style={{ fontWeight: 600, color: 'var(--color-primary)' }}>{d.codigo}</td>
                    <td style={{ fontWeight: 500 }}>{d.ci || '-'}</td>
                    <td>{d.nombre} {d.apellidos}</td>
                    <td>{d.telefono || '-'}</td>
                    <td>{d.correo}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleAbrirEditar(d)}>
                          <Edit2 size={14} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleEliminar(d.codigo)}>
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
              <h3 className="modal-title">{modoEdicion ? 'Editar Docente' : 'Registrar Nuevo Docente'}</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}>X</button>
            </div>
            <form onSubmit={handleGuardar}>
              <div className="form-group">
                <label className="form-label">Codigo de Docente</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.codigo}
                  onChange={(e) => setFormData({ ...formData, codigo: e.target.value })}
                  placeholder="ej: DOC-103"
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
                  placeholder="ej: 7891234 LP"
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
