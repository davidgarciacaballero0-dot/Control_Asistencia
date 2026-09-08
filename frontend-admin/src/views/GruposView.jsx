import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, Layers, Clock, Users, BookOpen, Upload, Download, Search, CheckCircle, AlertCircle, FileSpreadsheet, Smartphone, Key } from 'lucide-react';
import { academicoApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const GruposView = () => {
  const { user } = useAuth();
  const esDocente = user?.roles?.includes('ROLE_DOCENTE') || user?.rol === 'ROLE_DOCENTE';
  const codigoDocenteActual = user?.identificadorReferencia;

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

  // Modal Importar Estudiantes (CSV / Excel / PDF)
  const [showImportModal, setShowImportModal] = useState(false);
  const [grupoParaImportar, setGrupoParaImportar] = useState(null);
  const [archivoSeleccionado, setArchivoSeleccionado] = useState(null);
  const [archivoPdfSeleccionado, setArchivoPdfSeleccionado] = useState(null);
  const [importando, setImportando] = useState(false);
  const [resultadoImportacion, setResultadoImportacion] = useState(null);
  const [errorImportacion, setErrorImportacion] = useState('');

  // Modal Ver Estudiantes Inscritos
  const [showEstudiantesModal, setShowEstudiantesModal] = useState(false);
  const [grupoParaEstudiantes, setGrupoParaEstudiantes] = useState(null);
  const [estudiantesInscritos, setEstudiantesInscritos] = useState([]);
  const [cargandoEstudiantes, setCargandoEstudiantes] = useState(false);
  const [filtroEstudiante, setFiltroEstudiante] = useState('');

  useEffect(() => {
    cargarDatos();
  }, [user]);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [resGrupos, resMat, resDoc] = await Promise.all([
        academicoApi.getGrupos(),
        academicoApi.getMaterias(),
        academicoApi.getDocentes()
      ]);

      let listaGrupos = resGrupos.data || [];
      if (esDocente && codigoDocenteActual) {
        listaGrupos = listaGrupos.filter(g => g.docenteCodigo === codigoDocenteActual);
      }

      setGrupos(listaGrupos);
      setMaterias(resMat.data || []);
      setDocentes(resDoc.data || []);

      if (resMat.data?.length > 0 && resDoc.data?.length > 0) {
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

  // Metodos de Importacion de Estudiantes
  const handleAbrirImportModal = (grupo) => {
    setGrupoParaImportar(grupo);
    setArchivoSeleccionado(null);
    setArchivoPdfSeleccionado(null);
    setResultadoImportacion(null);
    setErrorImportacion('');
    setShowImportModal(true);
  };

  const handleDescargarPlantillaCsv = () => {
    const csvContent = "Registro,CI,Apellidos,Nombre,Carrera,Plan,Telefono,Email\n" +
      "2024005,10000005,Perez Aguilera,Carlos Andres,187,Plan 2020,70011225,carlos.perez@universidad.edu\n" +
      "2024006,10000006,Vargas Rodriguez,Lucia Elena,187,Plan 2020,70011226,lucia.vargas@universidad.edu\n" +
      "2024007,10000007,Flores Morales,Diego Fernando,187,Plan 2020,70011227,diego.flores@universidad.edu";
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `plantilla_estudiantes_${grupoParaImportar?.materiaSigla || 'grupo'}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleImportarArchivo = async (e) => {
    e.preventDefault();
    if (!archivoSeleccionado || !grupoParaImportar) return;

    setImportando(true);
    setErrorImportacion('');
    setResultadoImportacion(null);

    const data = new FormData();
    data.append('archivo', archivoSeleccionado);
    if (archivoPdfSeleccionado) {
      data.append('archivoPdf', archivoPdfSeleccionado);
    }

    try {
      const res = await academicoApi.importarEstudiantesGrupo(grupoParaImportar.id, data);
      setResultadoImportacion(res.data);
      cargarDatos();
    } catch (err) {
      setErrorImportacion(err.response?.data?.message || err.message || 'Error al procesar el archivo');
    } finally {
      setImportando(false);
    }
  };

  // Metodos de Consulta de Estudiantes Inscritos
  const handleAbrirEstudiantesModal = async (grupo) => {
    setGrupoParaEstudiantes(grupo);
    setFiltroEstudiante('');
    setShowEstudiantesModal(true);
    setCargandoEstudiantes(true);

    try {
      const res = await academicoApi.getEstudiantesByGrupo(grupo.id);
      setEstudiantesInscritos(res.data || []);
    } catch (err) {
      console.error('Error al cargar estudiantes del grupo', err);
      setEstudiantesInscritos([]);
    } finally {
      setCargandoEstudiantes(false);
    }
  };

  const estudiantesFiltrados = estudiantesInscritos.filter(est => {
    const texto = filtroEstudiante.toLowerCase();
    return est.registro.toLowerCase().includes(texto) ||
      est.nombre.toLowerCase().includes(texto) ||
      est.apellidos.toLowerCase().includes(texto) ||
      (est.ci && est.ci.toLowerCase().includes(texto));
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>
            {esDocente ? 'Mis Grupos y Horarios Asignados' : 'Gestion de Grupos y Horarios (CU05)'}
          </h2>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            {esDocente
              ? `Materias, grupos, horarios y listas de estudiantes bajo mi responsabilidad (${user?.nombreCompleto || user?.username})`
              : 'Asignacion de cupos, docentes, franjas horarias e importacion de estudiantes por la administracion academica'}
          </p>
        </div>
        {!esDocente && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            <Plus size={16} />
            <span>Nuevo Grupo</span>
          </button>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '20px' }}>
        {grupos.length === 0 ? (
          <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px' }}>
            <Layers size={40} style={{ color: 'var(--color-text-muted)', margin: '0 auto 12px' }} />
            <h3>No hay grupos registrados</h3>
            <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
              {esDocente ? 'No tiene grupos academicos asignados en este periodo.' : 'Cree un nuevo grupo para asignar materias y docentes.'}
            </p>
          </div>
        ) : (
          grupos.map((g) => (
            <div key={g.id} className="card" style={{ marginBottom: 0, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                  <div>
                    <span className="badge badge-activa" style={{ marginBottom: '6px' }}>{g.materiaSigla}</span>
                    <h3 style={{ fontSize: '1.15rem' }}>{g.materiaNombre}</h3>
                    <div style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem' }}>
                      Grupo: <strong>{g.nombre}</strong> | Cupo: {g.cupo} estudiantes
                    </div>
                  </div>
                  {!esDocente && (
                    <button className="btn btn-danger btn-sm" onClick={() => handleEliminarGrupo(g.id)} style={{ padding: '6px 8px' }}>
                      <Trash2 size={14} />
                    </button>
                  )}
                </div>

                <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px', marginTop: '12px' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginBottom: '4px' }}>Docente Asignado:</div>
                  <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{g.docenteNombreCompleto} ({g.docenteCodigo})</div>
                </div>

                <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px', marginTop: '12px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>Horarios de Clase:</span>
                    {!esDocente && (
                      <button className="btn btn-secondary btn-sm" onClick={() => handleAbrirHorarioModal(g)} style={{ padding: '4px 8px', fontSize: '0.75rem' }}>
                        + Horario
                      </button>
                    )}
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
                          {!esDocente && (
                            <button
                              onClick={() => handleEliminarHorario(h.id)}
                              style={{ background: 'none', border: 'none', color: 'var(--color-danger)', cursor: 'pointer' }}
                            >
                              <Trash2 size={12} />
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Botones de Gestion de Lista de Estudiantes */}
              <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '12px', marginTop: '16px', display: 'flex', gap: '8px' }}>
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => handleAbrirImportModal(g)}
                  style={{ flex: 1, fontSize: '0.8rem', justifyContent: 'center' }}
                  title="Cargar lista oficial en formato CSV o Excel"
                >
                  <Upload size={14} />
                  <span>Importar Lista</span>
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => handleAbrirEstudiantesModal(g)}
                  style={{ flex: 1, fontSize: '0.8rem', justifyContent: 'center' }}
                  title="Ver estudiantes inscritos formalmente"
                >
                  <Users size={14} />
                  <span>Ver Inscritos</span>
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal Importar Estudiantes (CSV / Excel) */}
      {showImportModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '540px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <FileSpreadsheet size={22} color="#10b981" />
                <h3 className="modal-title">Importar Estudiantes ({grupoParaImportar?.materiaSigla} - {grupoParaImportar?.nombre})</h3>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowImportModal(false)}>X</button>
            </div>

            <form onSubmit={handleImportarArchivo}>
              <div style={{ background: 'rgba(59, 130, 246, 0.08)', border: '1px solid rgba(59, 130, 246, 0.2)', padding: '14px', borderRadius: 'var(--radius-md)', marginBottom: '18px' }}>
                <div style={{ fontWeight: 600, fontSize: '0.9rem', marginBottom: '6px', color: '#3b82f6' }}>
                  Estructura del archivo CSV, Excel (.xlsx) o PDF
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', lineHeight: 1.4 }}>
                  El archivo debe contener las columnas: <strong>Registro, CI, Apellidos, Nombre, Carrera, Plan, Telefono, Email</strong>.
                  Tambien puedes adjuntar opcionalmente el archivo PDF con las fotografias de perfil. Al reimportar una lista actualizada, los estudiantes que ya no aparezcan seran dados de baja logica de este grupo para evitar que sigan marcando asistencia, preservando su historial.
                </div>
                <div style={{ marginTop: '10px', padding: '8px 12px', borderRadius: 'var(--radius-sm)', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.25)', fontSize: '0.78rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Smartphone size={15} />
                  <span><strong>Aprovisionamiento Automatico:</strong> Al cargar la lista, se crearan automaticamente las cuentas de cada estudiante para usar la app movil (<strong>Usuario:</strong> Registro | <strong>Contrasena:</strong> CI).</span>
                </div>
                <button
                  type="button"
                  onClick={handleDescargarPlantillaCsv}
                  className="btn btn-secondary btn-sm"
                  style={{ marginTop: '10px', fontSize: '0.75rem', padding: '4px 10px' }}
                >
                  <Download size={12} />
                  <span>Descargar Plantilla de Ejemplo (.CSV)</span>
                </button>
              </div>

              <div className="form-group">
                <label className="form-label">Archivo Principal de Datos (.CSV, .XLSX, .XLS, .PDF)</label>
                <input
                  type="file"
                  className="form-input"
                  accept=".csv,.xlsx,.xls,.pdf"
                  onChange={(e) => setArchivoSeleccionado(e.target.files[0])}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Archivo PDF con Fotos de Perfil (Opcional)</label>
                <input
                  type="file"
                  className="form-input"
                  accept=".pdf"
                  onChange={(e) => setArchivoPdfSeleccionado(e.target.files[0])}
                />
                <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                  Extrae automaticamente las fotografias del PDF y las asocia al perfil de cada estudiante por orden de lista.
                </span>
              </div>

              {errorImportacion && (
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '10px',
                  borderRadius: 'var(--radius-md)',
                  background: 'var(--color-danger-bg)',
                  color: 'var(--color-danger)',
                  fontSize: '0.85rem',
                  marginBottom: '16px'
                }}>
                  <AlertCircle size={16} />
                  <span>{errorImportacion}</span>
                </div>
              )}

              {resultadoImportacion && (
                <div style={{
                  padding: '14px',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(16, 185, 129, 0.12)',
                  border: '1px solid rgba(16, 185, 129, 0.3)',
                  marginBottom: '16px'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#10b981', fontWeight: 600, marginBottom: '8px' }}>
                    <CheckCircle size={18} />
                    <span>Importacion y Sincronizacion Completada</span>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '0.85rem' }}>
                    <div>Total Procesados: <strong>{resultadoImportacion.totalProcesados}</strong></div>
                    <div>Nuevos Creados: <strong>{resultadoImportacion.totalNuevos}</strong></div>
                    <div>Inscritos al Grupo: <strong>{resultadoImportacion.totalInscritos}</strong></div>
                    <div>Actualizados: <strong>{resultadoImportacion.totalActualizados}</strong></div>
                    <div style={{ color: '#f59e0b' }}>Bajas Logicas: <strong>{resultadoImportacion.totalBajasLogicas || 0}</strong></div>
                    <div style={{ color: '#3b82f6' }}>Fotos Asignadas: <strong>{resultadoImportacion.totalFotosProcesadas || 0}</strong></div>
                  </div>

                  {/* Panel de confirmacion de credenciales moviles creadas */}
                  <div style={{
                    marginTop: '12px',
                    padding: '10px 12px',
                    borderRadius: 'var(--radius-sm)',
                    background: 'rgba(59, 130, 246, 0.1)',
                    border: '1px solid rgba(59, 130, 246, 0.3)',
                    fontSize: '0.82rem'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, color: '#3b82f6', marginBottom: '4px' }}>
                      <Smartphone size={15} />
                      <span>Credenciales para App Movil Generadas con Exito</span>
                    </div>
                    <div style={{ color: 'var(--color-text-secondary)', lineHeight: 1.4 }}>
                      Se han creado y habilitado las cuentas de acceso para los <strong>{resultadoImportacion.totalProcesados}</strong> estudiantes:
                    </div>
                    <div style={{ marginTop: '6px', fontSize: '0.8rem', background: 'rgba(0,0,0,0.2)', padding: '6px 10px', borderRadius: '4px' }}>
                      <div>• <strong>Usuario:</strong> Numero de Registro del alumno</div>
                      <div>• <strong>Contrasena:</strong> Carnet de Identidad (CI) o Iniciales del nombre en mayusculas + Registro (ej: GCD217058795)</div>
                    </div>
                    <div style={{ marginTop: '6px', fontSize: '0.78rem', color: '#10b981' }}>
                      Los estudiantes ya pueden ingresar a la app movil y marcar asistencia escaneando el QR de la sesion.
                    </div>
                  </div>
                  {resultadoImportacion.errores?.length > 0 && (
                    <div style={{ marginTop: '10px', fontSize: '0.75rem', color: '#ef4444' }}>
                      <strong>Advertencias:</strong>
                      <ul>
                        {resultadoImportacion.errores.map((err, idx) => (
                          <li key={idx}>{err}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowImportModal(false)}>Cerrar</button>
                <button type="submit" className="btn btn-primary" disabled={importando || !archivoSeleccionado}>
                  {importando ? 'Procesando e Inscribiendo...' : 'Procesar e Inscribir'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Ver Lista de Estudiantes Inscritos */}
      {showEstudiantesModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '780px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Users size={22} color="#3b82f6" />
                <h3 className="modal-title">
                  Estudiantes Inscritos: {grupoParaEstudiantes?.materiaSigla} ({grupoParaEstudiantes?.nombre})
                </h3>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowEstudiantesModal(false)}>X</button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ position: 'relative', width: '300px' }}>
                <Search size={16} style={{ position: 'absolute', left: '10px', top: '10px', color: 'var(--color-text-muted)' }} />
                <input
                  type="text"
                  className="form-input"
                  placeholder="Buscar por registro o nombre..."
                  value={filtroEstudiante}
                  onChange={(e) => setFiltroEstudiante(e.target.value)}
                  style={{ paddingLeft: '34px' }}
                />
              </div>
              <span className="badge badge-activa">
                Total Inscritos: {estudiantesInscritos.length}
              </span>
            </div>

            {cargandoEstudiantes ? (
              <div style={{ textAlign: 'center', padding: '40px', color: '#3b82f6' }}>
                Cargando lista de inscritos...
              </div>
            ) : estudiantesFiltrados.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px', color: 'var(--color-text-muted)' }}>
                {estudiantesInscritos.length === 0
                  ? 'No hay estudiantes inscritos en este grupo. Utilice la opcion "Importar Lista" para cargar el archivo CSV o Excel.'
                  : 'No se encontraron estudiantes con ese criterio de busqueda.'}
              </div>
            ) : (
              <div className="table-container" style={{ maxHeight: '380px', overflowY: 'auto' }}>
                <table>
                  <thead>
                    <tr>
                      <th style={{ width: '50px' }}>Foto</th>
                      <th>Registro</th>
                      <th>CI</th>
                      <th>Nombre Completo</th>
                      <th>Carrera</th>
                      <th>Acceso App Movil</th>
                      <th>Correo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {estudiantesFiltrados.map((est) => (
                      <tr key={est.registro}>
                        <td>
                          {est.fotoBase64 ? (
                            <img
                              src={est.fotoBase64}
                              alt={est.nombre}
                              style={{
                                width: '36px',
                                height: '36px',
                                borderRadius: '50%',
                                objectFit: 'cover',
                                border: '1px solid rgba(59, 130, 246, 0.4)'
                              }}
                            />
                          ) : (
                            <div style={{
                              width: '36px',
                              height: '36px',
                              borderRadius: '50%',
                              background: '#334155',
                              color: '#94a3b8',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontSize: '0.75rem',
                              fontWeight: 600
                            }}>
                              {est.nombre ? est.nombre.charAt(0).toUpperCase() : 'E'}
                            </div>
                          )}
                        </td>
                        <td style={{ fontWeight: 600, color: '#3b82f6' }}>{est.registro}</td>
                        <td>{est.ci || '-'}</td>
                        <td style={{ fontWeight: 500 }}>{est.nombre} {est.apellidos}</td>
                        <td>{est.carrera}</td>
                        <td>
                          <div style={{ display: 'inline-flex', flexDirection: 'column', gap: '2px' }}>
                            <span className="badge badge-activa" style={{ fontSize: '0.7rem', width: 'fit-content' }}>
                              Habilitado
                            </span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                              Clave: <strong style={{ color: 'var(--color-text-primary)' }}>{est.ci || est.registro}</strong>
                            </span>
                          </div>
                        </td>
                        <td style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>{est.correo}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setShowEstudiantesModal(false)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

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
