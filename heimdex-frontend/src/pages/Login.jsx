// src/pages/Login.jsx
import React, { useState } from 'react';
import axios from 'axios';

const API_URL = ${import.meta.env.VITE_API_BASE_URL}/login

// Adicionamos 'onLoginSuccess' como uma propriedade que o componente recebe
function Login({ onLoginSuccess }) {
  const [matricula, setMatricula] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');

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

      // 1. Salva os dados do usuário no localStorage do navegador.
      // localStorage é um "pequeno baú" que guarda textos no navegador.
      // JSON.stringify transforma nosso objeto de usuário em um texto para ser guardado.
      localStorage.setItem('user', JSON.stringify(user));

      // 2. Chama a função que recebemos para avisar que o login foi um sucesso.
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

  return (
    <div>
      <h2>Login - Sistema Heimdex</h2>
      {/* O resto do formulário continua igual... */}
      <form onSubmit={handleLogin}>
        <div>
          <label>Matrícula:</label>
          <input
            type="text"
            value={matricula}
            onChange={(e) => setMatricula(e.target.value)}
            placeholder="Digite sua matrícula"
          />
        </div>
        <div>
          <label>Senha:</label>
          <input
            type="password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            placeholder="Digite sua senha"
          />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit">Entrar</button>
      </form>
    </div>
  );
}

export default Login;
