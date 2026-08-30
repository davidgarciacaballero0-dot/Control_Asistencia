import React, { useState } from 'react';
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

const MainLayout = () => {
  const { isAuthenticated, loading } = useAuth();
  const [activeTab, setActiveTab] = useState('dashboard');

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
        return <DashboardView setActiveTab={setActiveTab} />;
    }
  };

  const getTitle = () => {
    switch (activeTab) {
      case 'dashboard': return 'Panel de Control Principal';
      case 'sesiones': return 'Control y Proyeccion de Sesiones QR';
      case 'docentes': return 'Gestion Academica - Docentes';
      case 'estudiantes': return 'Gestion Academica - Estudiantes';
      case 'materias': return 'Gestion Academica - Materias';
      case 'grupos': return 'Gestion Academica - Grupos y Horarios';
      default: return 'Sistema de Asistencia';
    }
  };

  return (
    <div className="app-container">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="main-content">
        <Navbar title={getTitle()} />
        <main className="content-body">
          {renderView()}
        </main>
      </div>
    </div>
  );
};

export const App = () => {
  return (
    <AuthProvider>
      <MainLayout />
    </AuthProvider>
  );
};

export default App;
