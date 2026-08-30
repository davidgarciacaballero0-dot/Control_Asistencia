import React from 'react';
import { Shield, Server } from 'lucide-react';

export const Navbar = ({ title }) => {
  return (
    <header className="top-bar">
      <h1 className="top-bar-title">{title}</h1>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
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
