// Código Completo Modificado - Sidebar.jsx
import React from 'react';
// ADIÇÃO: Importa Link e useLocation
import { Link, useLocation } from 'react-router-dom';

import '../Sidebar.css';
import EletraLogo from '../assets/eletra_logo.png'; // Verifique o caminho

// Recebe user como prop do App.jsx
function Sidebar({ user }) {
    // Usa o user recebido por prop, com fallback se não vier
    const currentUser = user || JSON.parse(localStorage.getItem('user') || '{}');
    const location = useLocation(); // Hook para saber a rota ativa

    const handleLogout = () => {
        localStorage.removeItem('user');
        // Usar window.location força recarregamento completo, limpando estados
        window.location.href = '/login';
    };

    // Função auxiliar para verificar se o link está ativo
    const isActive = (path) => location.pathname === path || (path !== "/" && location.pathname.startsWith(path));


    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <img src={EletraLogo} alt="Eletra Logo" className="sidebar-logo" />
                <div className="sidebar-info">
                    <p style={{ marginTop: '5px', fontWeight: 'bold', color: '#ffc107' }}>
                        Usuário: {currentUser.nomeCompleto || 'Não identificado'}
                    </p>
                    <p>Perfil: {currentUser.perfil || 'N/A'}</p>
                </div>
            </div>

            <div className="sidebar-menu">
                {/* Link Início (Dashboard) */}
                <Link to="/" className={`menu-item ${isActive('/') ? 'active' : ''}`}>
                    <i className="fa fa-home"></i> Início (Dashboard)
                </Link>

                {/* Seção Operação */}
                <div className="menu-section-title">OPERAÇÃO</div>
                <Link to="/planejamento" className={`menu-item ${isActive('/planejamento') ? 'active' : ''}`}>
                    <i className="fa fa-calendar-plus-o"></i> Planejamento
                </Link>
                <Link to="/estoque" className={`menu-item ${isActive('/estoque') ? 'active' : ''}`}>
                    <i className="fa fa-archive"></i> Consulta de Estoque
                </Link>

                 {/* Seção Análise / Consulta */}
                 <div className="menu-section-title">ANÁLISE</div>
                 {/* --- ADIÇÃO: Link para Histórico --- */}
                 <Link to="/historico" className={`menu-item ${isActive('/historico') ? 'active' : ''}`}>
                     <i className="fa fa-history"></i> Histórico Equipamentos
                 </Link>
                 {/* --- FIM ADIÇÃO --- */}
                <Link to="/relatorios" className={`menu-item ${isActive('/relatorios') ? 'active' : ''}`}>
                    <i className="fa fa-bar-chart"></i> Relatórios e KPIs
                </Link>

                {/* Seção Administração */}
                <div className="menu-section-title">ADMINISTRAÇÃO</div>
                <Link to="/cadastros" className={`menu-item ${isActive('/cadastros') ? 'active' : ''}`}>
                    <i className="fa fa-cogs"></i> Cadastros
                </Link>

                {/* Botão Sair */}
                {/* Mudado para div clicável para melhor controle de estilo/ação */}
                <div className="menu-item logout-button" onClick={handleLogout} style={{ marginTop: 'auto', color: '#ff8a8a', cursor: 'pointer' }}>
                    <i className="fa fa-sign-out"></i> Sair do Sistema
                </div>
            </div>
        </div>
    );
}

export default Sidebar;

/*
Lembre-se de adicionar/verificar estas classes no seu CSS se quiser o efeito visual:

.sidebar-menu a.active, .sidebar-menu .menu-item.active {
    background-color: #485058;
    color: #fff;
    font-weight: bold;
}

.sidebar-menu .logout-button:hover {
     background-color: #5e3337; // Um vermelho mais escuro no hover
     color: white;
}
*/