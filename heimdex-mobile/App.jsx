import React, { useState, useEffect, useMemo, useRef } from 'react';
import axios from 'axios';
import { Html5QrcodeScanner } from 'html5-qrcode';

// --- ASSETS ---
import logoPrincipal from './assets/eletra_logo.png'; 
import logoSecundaria1 from './assets/LogoEletra.png'; 
import logoSecundaria2 from './assets/LogoHexing.png';

const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api`; 
const api = axios.create({ baseURL: API_BASE_URL });

// --- HOOK DE AUTENTICAÇÃO ---
const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const storedUser = localStorage.getItem('heimdex_user');
      if (storedUser) { setUser(JSON.parse(storedUser)); }
    } catch (error) {
      localStorage.removeItem('heimdex_user');
    } finally { setLoading(false); }
  }, []);

  const login = (userData) => { 
    localStorage.setItem('heimdex_user', JSON.stringify(userData)); 
    setUser(userData); 
  };
  const logout = () => { 
    localStorage.removeItem('heimdex_user'); 
    setUser(null); 
  };
  return { user, login, logout, loading };
};

// --- SCANNER QR ---
const QRScanner = ({ onScanSuccess, onClose }) => {
  useEffect(() => {
    const scanner = new Html5QrcodeScanner("reader", { 
      fps: 10, 
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1.0
    });
    scanner.render((decodedText) => {
      scanner.clear();
      onScanSuccess(decodedText);
    }, () => {});
    return () => scanner.clear();
  }, []);

  return (
    <div className="qr-scanner-overlay">
      <div className="qr-scanner-content glass-card">
        <div id="reader"></div>
        <button className="primary-btn mt-10" onClick={onClose}>CANCELAR</button>
      </div>
    </div>
  );
};

const LoadingSpinner = () => ( <div className="loading-overlay"><div className="spinner"></div></div> );

const BottomNavBar = ({ active, setActive }) => (
  <nav className="bottom-nav">
    <button className={active === 'preventivas' ? 'active' : ''} onClick={() => setActive('preventivas')}>
      <i className="fa fa-tasks"></i><span>Ordens</span>
    </button>
    <button className={active === 'consulta' ? 'active' : ''} onClick={() => setActive('consulta')}>
      <i className="fa fa-search"></i><span>Estoque</span>
    </button>
    <button className={active === 'gestao' ? 'active' : ''} onClick={() => setActive('gestao')}>
      <i className="fa fa-plus-circle"></i><span>Entrada</span>
    </button>
  </nav>
);

// --- TELA DE LOGIN ---
const LoginScreen = ({ onLogin }) => {
  const [matricula, setMatricula] = useState('');
  const [senha, setSenha] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true); setError('');
    try {
      const response = await api.post('/usuarios/authenticate', { matricula, senha });
      onLogin(response.data);
    } catch (err) { setError('Usuário ou senha inválidos.'); }
    finally { setIsLoading(false); }
  };

  return (
    <div className="login-page">
      {isLoading && <LoadingSpinner />}
      <div className="login-container-wrapper">
        <div className="glass-card login-card">
          <img src={logoPrincipal} alt="Logo" className="login-main-logo" />
          <p className="subtitle">SISTEMA DE MANUTENÇÃO</p>
          <form onSubmit={handleLogin} className="modern-form">
            {error && <div className="error-badge">{error}</div>}
            <div className="input-field">
              <i className="fa fa-user"></i>
              <input type="text" value={matricula} onChange={e => setMatricula(e.target.value)} placeholder="Usuário" required />
            </div>
            <div className="input-field">
              <i className="fa fa-lock"></i>
              <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} placeholder="Senha" required />
            </div>
            <button type="submit" className="primary-btn">ENTRAR</button>
          </form>
        </div>
        <div className="login-external-logos">
          <img src={logoSecundaria1} className="partner-logo" alt="Eletra" />
          <img src={logoSecundaria2} className="partner-logo" alt="Hexing" />
        </div>
      </div>
    </div>
  );
};

// --- TELA DE PREVENTIVAS ---
const PreventivasScreen = ({ user, onSelectOS, onLogout }) => {
  const [ordens, setOrdens] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!user?.nomeCompleto) return;
    api.get('/ordens-servico').then(res => {
      const filtradas = res.data.filter(os => 
        os.nomeTecnico === user.nomeCompleto && 
        (os.status === 'AGENDADA' || os.status === 'PENDENTE_DE_CORRECAO')
      );
      setOrdens(filtradas);
      setIsLoading(false);
    }).catch(() => setIsLoading(false));
  }, [user]);

  return (
    <div className="modern-page internal">
      <header className="main-header">
        <div className="user-info">
          <span>Olá, <strong>{user?.nomeCompleto ? user.nomeCompleto.split(' ')[0] : 'Técnico'}</strong></span>
          <small>Unidade Móvel</small>
        </div>
        <button className="icon-btn-logout" onClick={onLogout}><i className="fa fa-power-off"></i></button>
      </header>
      <div className="content">
        <h2 className="section-title">Minhas Atividades</h2>
        {isLoading ? <LoadingSpinner /> : (
          <div className="card-list">
            {ordens.map(os => (
              <div key={os.id} className="glass-card os-item" onClick={() => onSelectOS(os.id)}>
                <div className="os-badge" style={{background: os.status === 'AGENDADA' ? '#3498db' : '#e67e22'}}>
                  {os.status === 'AGENDADA' ? 'AGENDA' : 'CORREÇÃO'}
                </div>
                <h3>{os.nomeEquipamento}</h3>
                <div className="os-footer">
                  <span><i className="fa fa-tag"></i> {os.codigoEquipamento}</span>
                  <span><i className="fa fa-calendar"></i> {new Date(os.dataAgendamento).toLocaleDateString()}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

// --- TELA DE CONSULTA DE ESTOQUE COM FILTROS DE ÁREA E MÁQUINA ---
const ConsultaEstoqueScreen = () => {
  const [pecas, setPecas] = useState([]);
  const [areas, setAreas] = useState([]);
  const [equipamentos, setEquipamentos] = useState([]);
  
  const [filtroTexto, setFiltroTexto] = useState('');
  const [areaSelecionada, setAreaSelecionada] = useState('');
  const [maquinaSelecionada, setMaquinaSelecionada] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const carregarDados = async () => {
      try {
        const [pecasRes, areasRes, equipRes] = await Promise.all([
          api.get('/pecas'),
          api.get('/areas'),
          api.get('/equipamentos')
        ]);
        setPecas(pecasRes.data);
        setAreas(areasRes.data);
        setEquipamentos(equipRes.data);
      } catch (err) {
        console.error("Erro ao carregar dados do estoque", err);
      } finally {
        setLoading(false);
      }
    };
    carregarDados();
  }, []);

  const pecasFiltradas = useMemo(() => {
    return pecas.filter(p => {
      const matchTexto = p.nome.toLowerCase().includes(filtroTexto.toLowerCase()) || p.codigoControle.includes(filtroTexto);
      const matchArea = areaSelecionada ? p.areaNome === areaSelecionada : true;
      const matchMaquina = maquinaSelecionada ? p.equipamentoNome === maquinaSelecionada : true;
      return matchTexto && matchArea && matchMaquina;
    });
  }, [pecas, filtroTexto, areaSelecionada, maquinaSelecionada]);

  return (
    <div className="modern-page internal">
      <header className="main-header"><div className="user-info"><span>Consulta de <strong>Estoque</strong></span></div></header>
      <div className="content">
        <div className="glass-card filter-container">
          <div className="input-field">
            <i className="fa fa-search"></i>
            <input type="text" placeholder="Nome ou código..." value={filtroTexto} onChange={e => setFiltroTexto(e.target.value)} />
          </div>
          
          <div className="filter-row">
            <div className="select-field">
              <select value={areaSelecionada} onChange={e => setAreaSelecionada(e.target.value)}>
                <option value="">Todas as Áreas</option>
                {areas.map(a => <option key={a.id} value={a.nome}>{a.nome}</option>)}
              </select>
            </div>
            
            <div className="select-field">
              <select value={maquinaSelecionada} onChange={e => setMaquinaSelecionada(e.target.value)}>
                <option value="">Todas Máquinas</option>
                {equipamentos.filter(e => areaSelecionada ? e.areaNome === areaSelecionada : true).map(e => (
                  <option key={e.id} value={e.nome}>{e.nome}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {loading && <LoadingSpinner />}
        <div className="card-list" style={{marginTop: '20px'}}>
          {pecasFiltradas.length === 0 && !loading && <p className="empty-msg">Nenhuma peça encontrada.</p>}
          {pecasFiltradas.map(p => (
            <div key={p.id} className="glass-card os-item" style={{borderLeft: p.estoqueAtual <= p.estoqueMinimo ? '4px solid #ff4757' : '4px solid #2ecc71'}}>
              <div className="peca-header">
                <h3>{p.nome}</h3>
                <span className="stock-count">{p.estoqueAtual} UN</span>
              </div>
              <p className="peca-info">Cod: {p.codigoControle} | {p.areaNome} | {p.equipamentoNome}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// --- TELA DE GESTÃO/ENTRADA ---
const GestaoEstoqueScreen = () => {
  const [codigo, setCodigo] = useState('');
  const [peca, setPeca] = useState(null);
  const [isScanning, setIsScanning] = useState(false);

  const buscar = async (val = codigo) => {
    if (!val) return;
    try {
      const res = await api.get('/pecas');
      const achada = res.data.find(p => p.codigoControle === val.trim());
      if (achada) setPeca(achada);
      else alert("Peça não encontrada.");
    } catch { alert("Erro na busca."); }
  };

  return (
    <div className="modern-page internal">
      {isScanning && <QRScanner onScanSuccess={(t) => {setCodigo(t); buscar(t); setIsScanning(false);}} onClose={() => setIsScanning(false)} />}
      <header className="main-header"><div className="user-info"><span>Entrada de <strong>Material</strong></span></div></header>
      <div className="content">
        <div className="input-field">
          <input type="text" value={codigo} onChange={e => setCodigo(e.target.value)} placeholder="Bipar QR Code" />
          <button onClick={() => setIsScanning(true)} className="icon-btn-scanner"><i className="fa fa-camera"></i></button>
          <button onClick={() => buscar()} className="inline-btn">LER</button>
        </div>
        {peca && (
          <div className="glass-card" style={{marginTop:'20px', textAlign:'center'}}>
            <h3>{peca.nome}</h3>
            <p style={{margin:'15px 0'}}>Estoque Atual: {peca.estoqueAtual}</p>
            <button className="primary-btn" onClick={async () => {
              await api.post(`/pecas/${peca.id}/entrada`, { quantidade: 1 });
              alert("Entrada de +1 registrada!"); setPeca(null); setCodigo('');
            }}>CONFIRMAR +1 UNIDADE</button>
          </div>
        )}
      </div>
    </div>
  );
};

// --- DETALHE DA OS ---
const DetalheOSScreen = ({ osId, onBack }) => {
    const [os, setOs] = useState(null);
    const [isScanning, setIsScanning] = useState(false);
    const [codigoPeca, setCodigoPeca] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.get(`/ordens-servico/${osId}`).then(res => {
            setOs(res.data);
            setLoading(false);
        });
    }, [osId]);

    const handleBaixa = async () => {
        if (!codigoPeca) return;
        try {
          const resPecas = await api.get('/pecas');
          const p = resPecas.data.find(x => x.codigoControle === codigoPeca.trim());
          if (!p) return alert("Peça não encontrada.");
          await api.post(`/estoque/saida/${osId}`, { pecaId: p.id, quantidade: 1 });
          alert("Peça utilizada!"); setCodigoPeca('');
        } catch(e) { alert("Erro ao processar baixa."); }
    };

    if (loading) return <LoadingSpinner />;

    return (
        <div className="modern-page internal">
            {isScanning && <QRScanner onScanSuccess={(t) => {setCodigoPeca(t); setIsScanning(false);}} onClose={() => setIsScanning(false)} />}
            <header className="main-header">
                <button className="back-float" onClick={onBack}><i className="fa fa-chevron-left"></i></button>
                <div className="user-info"><span>OS <strong>#{os?.id}</strong></span></div>
            </header>
            <div className="content">
                <div className="glass-card">
                  <h3>{os?.nomeEquipamento}</h3>
                  <p>{os?.codigoEquipamento}</p>
                </div>
                <div className="section-spacer"></div>
                <div className="glass-card">
                    <h4>Baixar Peça para esta OS</h4>
                    <div className="input-field">
                        <input type="text" value={codigoPeca} onChange={e=>setCodigoPeca(e.target.value)} placeholder="QR Code Peça" />
                        <button onClick={() => setIsScanning(true)} className="icon-btn-scanner"><i className="fa fa-camera"></i></button>
                    </div>
                    {codigoPeca && <button className="primary-btn" onClick={handleBaixa}>BAIXAR PEÇA</button>}
                </div>
            </div>
        </div>
    );
};

// --- COMPONENTE PRINCIPAL ---
const MainApp = () => {
  const { user, logout } = useAuth();
  const [currentPage, setCurrentPage] = useState('preventivas');
  const [selectedOS, setSelectedOS] = useState(null);

  if (selectedOS) return <DetalheOSScreen osId={selectedOS} onBack={() => setSelectedOS(null)} />;

  return (
    <div className="app-shell">
      {currentPage === 'preventivas' && <PreventivasScreen user={user} onSelectOS={setSelectedOS} onLogout={logout} />}
      {currentPage === 'consulta' && <ConsultaEstoqueScreen />}
      {currentPage === 'gestao' && <GestaoEstoqueScreen />}
      <BottomNavBar active={currentPage} setActive={setCurrentPage} />
    </div>
  );
};

export default function App() {
  const { user, login, loading } = useAuth();
  if (loading) return <LoadingSpinner />;
  return ( <> <GlobalStyles /> {user ? <MainApp /> : <LoginScreen onLogin={login} />} </> );
}

// --- ESTILOS ---
const GlobalStyles = () => (
  <style>{`
    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css');
    :root { --main-bg: #0f111a; --card-bg: #1c1f2b; --accent: #3498db; --text: #e0e0e0; --glass: rgba(255, 255, 255, 0.05); }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', sans-serif; background: var(--main-bg); color: var(--text); overflow: hidden; }
    
    .login-page { height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
    .login-container-wrapper { display: flex; flex-direction: column; align-items: center; width: 100%; max-width: 400px; }
    .glass-card { background: var(--card-bg); border: 1px solid rgba(255,255,255,0.08); border-radius: 16px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
    .login-main-logo { width: 180px; margin-bottom: 10px; }
    .subtitle { font-size: 10px; letter-spacing: 3px; color: var(--accent); font-weight: bold; margin-bottom: 30px; }
    
    .input-field { background: var(--glass); border-radius: 12px; display: flex; align-items: center; padding: 0 15px; margin-bottom: 12px; border: 1px solid rgba(255,255,255,0.1); width: 100%; }
    .input-field i { color: var(--accent); margin-right: 15px; }
    .input-field input { background: none; border: none; color: white; padding: 15px 0; width: 100%; outline: none; }
    
    .filter-row { display: flex; gap: 10px; width: 100%; }
    .select-field { flex: 1; background: var(--glass); border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); padding: 5px; }
    .select-field select { background: transparent; border: none; color: white; width: 100%; padding: 10px; outline: none; font-size: 12px; }
    .select-field select option { background: #1c1f2b; }

    .primary-btn { background: var(--accent); color: white; border: none; border-radius: 12px; padding: 16px; font-weight: bold; width: 100%; cursor: pointer; }
    .partner-logo { height: 80px; object-fit: contain; }
    .login-external-logos { display: flex; justify-content: center; gap: 30px; margin-top: 40px; }
    .login-footer-text { margin-top: 20px; color: #636e72; font-size: 9px; letter-spacing: 1px; text-align: center; }

    .main-header { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #161922; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .user-info strong { color: var(--accent); font-size: 18px; }
    .content { padding: 20px; overflow-y: auto; height: calc(100vh - 75px); padding-bottom: 100px; }
    
    .peca-header { display: flex; justify-content: space-between; align-items: center; }
    .stock-count { color: var(--accent); font-weight: bold; font-size: 14px; }
    .peca-info { font-size: 11px; color: #888; margin-top: 5px; }

    .os-item { position: relative; margin-bottom: 15px; }
    .os-badge { position: absolute; top: 0; right: 0; padding: 5px 12px; font-size: 9px; font-weight: bold; border-bottom-left-radius: 12px; color: white; }
    .os-footer { display: flex; gap: 15px; margin-top: 15px; font-size: 11px; color: #888; }
    
    .bottom-nav { position: fixed; bottom: 0; width: 100%; height: 75px; background: #161922; border-top: 1px solid rgba(255,255,255,0.05); display: flex; padding-bottom: 15px; }
    .bottom-nav button { flex: 1; background: none; border: none; color: #555; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; }
    .bottom-nav button.active { color: var(--accent); }
    
    .qr-scanner-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 20px; }
    #reader { width: 100%; background: white; border-radius: 12px; overflow: hidden; }
    .icon-btn-scanner { background: none; border: none; color: var(--accent); font-size: 20px; margin-right: 10px; }
    .inline-btn { background: #333; color: white; border: none; padding: 8px 15px; border-radius: 8px; font-size: 10px; font-weight: bold; }
    .loading-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15,17,26,0.8); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .spinner { border: 3px solid #1c1f2b; border-top: 3px solid var(--accent); border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
    .section-spacer { height: 20px; }
    .back-float { background: none; border: none; color: white; font-size: 18px; margin-right: 15px; }
    .empty-msg { text-align: center; margin-top: 50px; color: #555; font-size: 14px; }
    .mt-10 { margin-top: 10px; }
  `}</style>
);