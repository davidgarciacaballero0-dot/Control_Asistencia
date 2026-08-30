import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/Sidebar';
import { Navbar } from './components/Navbar';
import { LoginView } from './views/LoginView';
import { DashboardView } from './views/DashboardView';
import { SesionesView } from './views/SesionesView';
import { DocentesView } from './views/DocentesView';
import { EstudiantesView } from './views/EstudiantesView';
import { MateriasView } from './views/MateriasView';
import { GruposView } from './views/GruposView';
import { EstudiantePortalView } from './views/EstudiantePortalView';

const MainLayout = ({ theme, toggleTheme }) => {
  const { isAuthenticated, loading, user } = useAuth();
  const esEstudiante = user?.roles?.includes('ROLE_ESTUDIANTE');
  const [activeTab, setActiveTab] = useState(esEstudiante ? 'estudiante-portal' : 'dashboard');

  useEffect(() => {
    if (esEstudiante) {
      setActiveTab('estudiante-portal');
    }
  }, [esEstudiante]);

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', color: '#3b82f6' }}>
        Cargando sistema...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LoginView />;
  }

  const renderView = () => {
    switch (activeTab) {
      case 'estudiante-portal':
        return <EstudiantePortalView />;
      case 'dashboard':
        return <DashboardView setActiveTab={setActiveTab} />;
      case 'sesiones':
        return <SesionesView />;
      case 'docentes':
        return <DocentesView />;
      case 'estudiantes':
        return <EstudiantesView />;
      case 'materias':
        return <MateriasView />;
      case 'grupos':
        return <GruposView />;
      default:
        return esEstudiante ? <EstudiantePortalView /> : <DashboardView setActiveTab={setActiveTab} />;
    }
  };

  const getTitle = () => {
    switch (activeTab) {
      case 'estudiante-portal': return 'Portal del Estudiante - Clases y Asistencia';
      case 'dashboard': return 'Panel de Control Principal';
      case 'sesiones': return 'Control y Proyeccion de Sesiones QR';
      case 'docentes': return 'Gestion Academica - Docentes';
      case 'estudiantes': return 'Gestion Academica - Padron de Estudiantes';
      case 'materias': return 'Gestion Academica - Materias';
      case 'grupos': return 'Gestion Academica - Grupos y Horarios';
      default: return 'Sistema de Asistencia';
    }
  };

  return (
    <div className="app-container">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="main-content">
        <Navbar title={getTitle()} theme={theme} onToggleTheme={toggleTheme} />
        <main className="content-body">
          {renderView()}
        </main>
      </div>
    </div>
  );
};

export const App = () => {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  return (
    <AuthProvider>
      <MainLayout theme={theme} toggleTheme={toggleTheme} />
    </AuthProvider>
  );
};

export default App;
