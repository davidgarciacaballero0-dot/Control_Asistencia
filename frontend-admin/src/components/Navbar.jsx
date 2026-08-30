import React from 'react';
import { Server, Sun, Moon } from 'lucide-react';

export const Navbar = ({ title, theme, onToggleTheme }) => {
  return (
    <header className="top-bar">
      <h1 className="top-bar-title">{title}</h1>
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        <button
          onClick={onToggleTheme}
          className="btn btn-secondary btn-sm"
          style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '7px 12px' }}
          title={theme === 'light' ? 'Cambiar a Modo Oscuro' : 'Cambiar a Modo Claro'}
        >
          {theme === 'light' ? <Moon size={15} /> : <Sun size={15} />}
          <span>{theme === 'light' ? 'Modo Oscuro' : 'Modo Claro'}</span>
        </button>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 12px',
          background: 'rgba(16, 185, 129, 0.1)',
          border: '1px solid rgba(16, 185, 129, 0.2)',
          borderRadius: '9999px',
          fontSize: '0.75rem',
          color: '#10b981',
          fontWeight: '500'
        }}>
          <Server size={14} />
          <span>Gateway Activo (8080)</span>
        </div>
      </div>
    </header>
  );
};
