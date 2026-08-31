import React from 'react';
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  BookOpen,
  Layers,
  QrCode,
  LogOut,
  ShieldCheck
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar = ({ activeTab, setActiveTab }) => {
  const { user, logout } = useAuth();

  const esAdmin = user?.roles?.includes('ROLE_ADMIN');
  const esDocente = user?.roles?.includes('ROLE_DOCENTE');
  const esEstudiante = user?.roles?.includes('ROLE_ESTUDIANTE');

  let menuItems = [];

  if (esDocente) {
    menuItems = [
      { id: 'sesiones', label: 'Control de Asistencia QR', icon: QrCode },
      { id: 'grupos', label: 'Mis Grupos y Horarios', icon: Layers },
      { id: 'dashboard', label: 'Resumen de Mis Clases', icon: LayoutDashboard },
    ];
  } else if (esEstudiante) {
    menuItems = [
      { id: 'estudiante-portal', label: 'Portal del Estudiante', icon: GraduationCap },
    ];
  } else {
    // Por defecto / ROLE_ADMIN
    menuItems = [
      { id: 'dashboard', label: 'Panel Principal', icon: LayoutDashboard },
      { id: 'sesiones', label: 'Monitoreo de Sesiones QR', icon: QrCode },
      { id: 'docentes', label: 'Gestion Docentes', icon: Users },
      { id: 'estudiantes', label: 'Padron Estudiantes', icon: GraduationCap },
      { id: 'materias', label: 'Gestion Materias', icon: BookOpen },
      { id: 'grupos', label: 'Grupos y Horarios', icon: Layers },
    ];
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <ShieldCheck size={26} color="#3b82f6" />
          <span>Asistencia MS</span>
        </div>
        <div className="sidebar-subtitle">Control Universitario</div>
      </div>

      <nav className="nav-list">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <div key={item.id} className={`nav-item ${isActive ? 'active' : ''}`}>
              <button onClick={() => setActiveTab(item.id)}>
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            </div>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="user-badge">
          <div className="user-info">
            <span className="user-name">{user?.nombreCompleto || user?.username}</span>
            <span className="user-role">{user?.roles?.[0] || 'USUARIO'}</span>
          </div>
          <button
            onClick={logout}
            className="btn btn-secondary btn-sm"
            title="Cerrar sesion"
            style={{ padding: '6px 8px' }}
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  );
};
