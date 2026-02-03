// src/pages/Login.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
// Importação das logos conforme solicitado
import logo from '../assets/eletra_logo.png';
import logoHexing from '../assets/LogoHexing.png';
import logoEletra from '../assets/LogoEletra.png';

const API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/usuarios/authenticate`;

function Login({ onLoginSuccess }) {
  const [matricula, setMatricula] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const [showLogo, setShowLogo] = useState(false);

  // Efeito para a imagem aparecer aos poucos (fade-in)
  useEffect(() => {
    setTimeout(() => setShowLogo(true), 300);
  }, []);

  const handleLogin = async (event) => {
    event.preventDefault();
    setError('');

    if (matricula === '' || senha === '') {
      setError('Matrícula e senha são obrigatórios.');
      return;
    }

    try {
      const response = await axios.post(API_URL, { matricula, senha });
      const user = response.data;
      localStorage.setItem('user', JSON.stringify(user));
      onLoginSuccess(user);
    } catch (err) {
      console.error("Erro no login:", err);
      if (err.response && err.response.status === 401) {
        setError('Matrícula ou senha inválida.');
      } else {
        setError('Não foi possível conectar ao servidor.');
      }
    }
  };

  const styles = {
    container: {
      height: '100vh',
      width: '100vw',
      display: 'flex',
      flexDirection: 'column', 
      justifyContent: 'center',
      alignItems: 'center',
      background: 'radial-gradient(circle, #2c3e50 0%, #000000 100%)',
      margin: 0,
      fontFamily: '"Segoe UI", Roboto, Helvetica, Arial, sans-serif',
    },
    card: {
      backgroundColor: 'rgba(255, 255, 255, 0.12)',
      backdropFilter: 'blur(15px)',
      padding: '30px 40px',
      borderRadius: '20px',
      boxShadow: '0 8px 32px 0 rgba(0, 0, 0, 0.8)',
      width: '100%',
      maxWidth: '350px', 
      textAlign: 'center',
      border: '1px solid rgba(255, 255, 255, 0.2)',
    },
    footerText: {
      width: '100%',
      maxWidth: '400px', 
      color: '#ffffff',
      textAlign: 'center',
      marginTop: '20px',
      fontSize: '14px',
      letterSpacing: '1px',
      opacity: 0.8,
    },
    logo: {
      width: '200px',
      marginBottom: '20px',
      opacity: showLogo ? 1 : 0,
      transform: showLogo ? 'scale(1)' : 'scale(0.8)',
      transition: 'opacity 1.5s ease-out, transform 1.2s ease-out',
    },
    titleLogoContainer: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      gap: '40px', // Aumentado o espaço lateral entre elas
      marginTop: '30px', // Mais espaço em relação ao texto mandarim
      opacity: showLogo ? 1 : 0,
      transition: 'opacity 2s ease-in-out',
    },
    secondaryLogo: {
      height: '120px', // ✅ Aumentado para dar mais visibilidade fora do card
      objectFit: 'contain',
    },
    inputGroup: {
      marginBottom: '15px',
      textAlign: 'left',
    },
    label: {
      display: 'block',
      color: '#ffffff',
      marginBottom: '5px',
      fontSize: '14px',
      fontWeight: '500',
    },
    input: {
      width: '100%',
      padding: '12px 15px',
      borderRadius: '8px',
      border: '1px solid rgba(255, 255, 255, 0.3)',
      backgroundColor: 'rgba(0, 0, 0, 0.2)',
      color: '#ffffff',
      outline: 'none',
      fontSize: '16px',
      boxSizing: 'border-box',
      transition: 'border 0.3s ease',
    },
    button: {
      width: '100%',
      padding: '12px',
      borderRadius: '8px',
      border: 'none',
      backgroundColor: '#3498db',
      color: '#ffffff',
      fontSize: '16px',
      fontWeight: '600',
      cursor: 'pointer',
      marginTop: '10px',
      transition: 'background-color 0.3s ease, transform 0.2s ease',
    },
    error: {
      color: '#ff4d4d',
      fontSize: '14px',
      marginTop: '10px',
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <img src={logo} alt="Eletra Logo" style={styles.logo} />
        
        <form onSubmit={handleLogin}>
          <div style={styles.inputGroup}>
            <label style={styles.label}>Usuário:</label>
            <input
              style={styles.input}
              type="text"
              value={matricula}
              onChange={(e) => setMatricula(e.target.value)}
              placeholder="Usuário"
              onFocus={(e) => e.target.style.border = '1px solid #3498db'}
              onBlur={(e) => e.target.style.border = '1px solid rgba(255, 255, 255, 0.3)'}
            />
          </div>
          
          <div style={styles.inputGroup}>
            <label style={styles.label}>Senha:</label>
            <input
              style={styles.input}
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="Digite sua senha"
              onFocus={(e) => e.target.style.border = '1px solid #3498db'}
              onBlur={(e) => e.target.style.border = '1px solid rgba(255, 255, 255, 0.3)'}
            />
          </div>

          {error && <p style={styles.error}>{error}</p>}
          
          <button 
            type="submit" 
            style={styles.button}
            onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
          >
            Entrar
          </button>
        </form>
      </div>

      <div style={styles.footerText}>
        预防性维护与零件管理系统
      </div>

      <div style={styles.titleLogoContainer}>
          <img src={logoHexing} alt="Logo Hexing" style={styles.secondaryLogo} />
          <img 
            src={logoEletra} 
            alt="Logo Eletra" 
            // ✅ Mantido o scale 1.5 para a logo Eletra ser proporcionalmente maior que a Hexing
            style={{...styles.secondaryLogo, transform: 'scale(1.5)'}} 
          />
      </div>
    </div>
  );
}

export default Login;
