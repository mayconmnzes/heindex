// Código Completo Modificado - App.jsx (Web)
import React, { useState, useEffect } from 'react';
// CORREÇÃO: BrowserRouter as Router -> apenas BrowserRouter
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Sidebar from './components/Sidebar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Cadastros from './pages/Cadastros';
import Planejamento from './pages/Planejamento';
import ExecucaoOS from './pages/ExecucaoOS';
import Relatorios from './pages/Relatorios';
import Estoque from './pages/Estoque';
import DetalhesEquipamento from './pages/DetalhesEquipamento';

// --- ADIÇÃO: Importe as novas páginas de Histórico ---
// Certifique-se de criar esses arquivos depois
import Historico from './pages/Historico'; // Página principal do histórico
import DetalheHistorico from './pages/DetalheHistorico'; // Página de detalhes do histórico
// --- FIM ADIÇÃO ---

import './App.css';
import './Sidebar.css'; // Mantenha esta linha se o CSS do Sidebar estiver aqui

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null); // Adicionado para passar pro Sidebar, se necessário

  useEffect(() => {
    // Lógica aprimorada para verificar usuário e autenticação
    try {
        const storedUser = localStorage.getItem('user'); // Use a chave correta
        if (storedUser) {
            setUser(JSON.parse(storedUser)); // Guarda o usuário no estado
            setIsAuthenticated(true);
        } else {
            setIsAuthenticated(false);
            setUser(null);
        }
    } catch(e) {
        console.error("Erro ao verificar usuário no localStorage:", e);
        localStorage.removeItem('user');
        setIsAuthenticated(false);
        setUser(null);
    } finally {
        setIsLoading(false);
    }
  }, []);

  const handleLoginSuccess = (loggedInUser) => {
    // Recebe o usuário do componente Login
    setUser(loggedInUser);
    setIsAuthenticated(true);
    // O componente Login já deve salvar no localStorage
  };

  // Componente auxiliar para proteger rotas
  const ProtectedRoute = ({ children }) => {
    return isAuthenticated ? children : <Navigate to="/login" />;
  };

  if (isLoading) {
    // Pode usar um componente Spinner visualmente melhor aqui
    return <div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}}>Carregando...</div>;
  }

  return (
    // Usa BrowserRouter em vez de Router diretamente
    <BrowserRouter basename="/heindex">
      <div className="App-layout">
        {/* Renderiza Sidebar apenas se autenticado */}
        {isAuthenticated && <Sidebar user={user} />} {/* Passa user pro Sidebar */}

        <main className="main-content-layout">
          <Routes>
            {/* Rota de Login (acessível apenas se NÃO autenticado) */}
            <Route
              path="/login"
              element={!isAuthenticated ? <Login onLoginSuccess={handleLoginSuccess} /> : <Navigate to="/" />}
            />

            {/* Rotas Protegidas (acessíveis apenas se autenticado) */}
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/cadastros" element={<ProtectedRoute><Cadastros /></ProtectedRoute>} />
            <Route path="/planejamento" element={<ProtectedRoute><Planejamento /></ProtectedRoute>} />
            <Route path="/estoque" element={<ProtectedRoute><Estoque /></ProtectedRoute>} />
            <Route path="/relatorios" element={<ProtectedRoute><Relatorios /></ProtectedRoute>} />
            <Route path="/ordem-servico/:osId" element={<ProtectedRoute><ExecucaoOS /></ProtectedRoute>} />
            <Route path="/historico/:id" element={<DetalhesEquipamento />} />
            
            {/* --- ADIÇÃO: Novas Rotas de Histórico --- */}
            <Route path="/historico" element={<ProtectedRoute><Historico /></ProtectedRoute>} />
            <Route path="/historico/:equipamentoId" element={<ProtectedRoute><DetalheHistorico /></ProtectedRoute>} />
            {/* --- FIM ADIÇÃO --- */}

            {/* Rota Fallback: Redireciona para login ou dashboard */}
            <Route path="*" element={isAuthenticated ? <Navigate to="/" /> : <Navigate to="/login" />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
