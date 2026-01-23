// Código Completo Atualizado - App.jsx (Mobile com Baixa de Peças e Histórico)
import React, { useState, useEffect, useMemo, useRef } from 'react';
import axios from 'axios';

// --- CONFIGURAÇÃO DA API ---
// import.meta.env.VITE_API_BASE_URL pegará automaticamente a URL do Render: https://heindex-api.onrender.com
const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api`; 
const BACKEND_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api`;

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

  const login = (userData) => { localStorage.setItem('heimdex_user', JSON.stringify(userData)); setUser(userData); };
  const logout = () => { localStorage.removeItem('heimdex_user'); setUser(null); };
  return { user, login, logout, loading };
};

// --- COMPONENTES DE UI ---
const LoadingSpinner = () => ( <div className="loading-overlay"><div className="spinner"></div></div> );
const Modal = ({ message, onClose, type = 'info' }) => ( <div className="modal-overlay"><div className={`modal-content modal-${type}`}><p>{message}</p><button onClick={onClose}>Fechar</button></div></div> );
const BottomNavBar = ({ active, setActive }) => (
  <nav className="bottom-nav">
    <button className={active === 'preventivas' ? 'active' : ''} onClick={() => setActive('preventivas')}><i className="fa fa-check-square"></i><span>Preventivas</span></button>
    <button className={active === 'consulta' ? 'active' : ''} onClick={() => setActive('consulta')}><i className="fa fa-search"></i><span>Consulta</span></button>
    <button className={active === 'gestao' ? 'active' : ''} onClick={() => setActive('gestao')}><i className="fa fa-cubes"></i><span>Gestão</span></button>
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
    if (!matricula || !senha) { setError('Matrícula e senha são obrigatórios.'); return; }
    setIsLoading(true); setError('');
    try {
      const response = await api.post('/usuarios/authenticate', { matricula, senha });
      onLogin(response.data);
    } catch (err) {
      setError('Erro de conexão ou credenciais inválidas.');
    } finally { setIsLoading(false); }
  };

  return (
    <div className="login-container">
      {isLoading && <LoadingSpinner />}
      <div className="login-header"><h1>Heimdex Manutenção</h1><p>Acesso Móvel</p></div>
      <form onSubmit={handleLogin} className="login-form">
        {error && <p className="error-message">{error}</p>}
        <div className="input-group">
          <label>Matrícula</label>
          <input type="text" value={matricula} onChange={(e) => setMatricula(e.target.value)} placeholder="Digite sua matrícula" autoCapitalize="none" required />
        </div>
        <div className="input-group">
          <label>Senha</label>
          <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} placeholder="Digite sua senha" required />
        </div>
        <button type="submit" disabled={isLoading}>Entrar</button>
      </form>
    </div>
  );
};

// --- TELA DE PREVENTIVAS ---
const PreventivasScreen = ({ user, onSelectOS, onLogout }) => {
  const [ordens, setOrdens] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchOrdens = async () => {
      setIsLoading(true);
      try {
        const [agRes, penRes] = await Promise.all([
          api.get('/ordens-servico?status=AGENDADA'),
          api.get('/ordens-servico?status=PENDENTE_DE_CORRECAO')
        ]);
        const filtradas = [...agRes.data, ...penRes.data].filter(os => os.nomeTecnico === user.nomeCompleto);
        setOrdens(filtradas);
      } catch (err) { console.error(err); } finally { setIsLoading(false); }
    };
    if (user) fetchOrdens();
  }, [user]);

  return (
    <div className="screen-container">
      <header className="screen-header">
        <h1>Minhas Preventivas</h1>
        <button onClick={onLogout} className="logout-button"><i className="fa fa-sign-out"></i></button>
      </header>
      <div className="screen-content">
        {isLoading && <LoadingSpinner />}
        <div className="os-list">
          {ordens.map(os => (
            <div key={os.id} className="os-card" onClick={() => onSelectOS(os.id)}>
              <div className="os-card-header">
                <h3>{os.nomeEquipamento}</h3>
                <span className={`status-pill ${os.status === 'AGENDADA' ? 'status-agendada' : 'status-pendente'}`}>{os.status.replace(/_/g, ' ')}</span>
              </div>
              <p><strong>Código:</strong> {os.codigoEquipamento}</p>
              <p><strong>Agendado:</strong> {new Date(os.dataAgendamento).toLocaleDateString('pt-BR')}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// --- TELA DE DETALHE DA OS (COM BAIXA DE PEÇAS ADICIONADA) ---
const DetalheOSScreen = ({ osId, onBack }) => {
    const [os, setOs] = useState(null);
    const [checklistRespostas, setChecklistRespostas] = useState({});
    const [observacoes, setObservacoes] = useState('');
    const [foto, setFoto] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [modal, setModal] = useState({ show: false, message: '', type: 'info' });
    const fileInputRef = useRef(null);

    // NOVOS ESTADOS PARA BAIXA DE PEÇAS
    const [codigoPecaInput, setCodigoPecaInput] = useState('');
    const [pecaParaBaixa, setPecaParaBaixa] = useState(null);
    const [quantidadeBaixa, setQuantidadeBaixa] = useState(1);
    const [pecasConsumidas, setPecasConsumidas] = useState([]);

    const fetchDetalhes = async () => {
        setIsLoading(true);
        try {
            const response = await api.get(`/ordens-servico/${osId}`);
            setOs(response.data);
            setObservacoes(response.data.observacoesTecnico || '');
            const initialRespostas = {};
            response.data.checklist?.itens?.forEach(item => {
                const salvo = response.data.resultados?.find(r => r.itemTemplateId === item.id);
                initialRespostas[item.id] = { status: salvo ? salvo.status : 'PENDENTE', observacao: salvo ? salvo.observacao : '' };
            });
            setChecklistRespostas(initialRespostas);

            // Busca histórico de peças usadas
            const consumoRes = await api.get(`/estoque/consumo-os/${osId}`);
            setPecasConsumidas(consumoRes.data);
        } catch (err) { console.error(err); } finally { setIsLoading(false); }
    };

    useEffect(() => { fetchDetalhes(); }, [osId]);

    // LÓGICA DE BUSCA E BAIXA
    const handleBuscaPeca = async () => {
        if (!codigoPecaInput) return;
        try {
            const response = await api.get('/pecas');
            const encontrada = response.data.find(p => p.codigoControle === codigoPecaInput.trim());
            if (encontrada) setPecaParaBaixa(encontrada);
            else alert("Peça não encontrada.");
        } catch (error) { alert("Erro ao buscar peça."); }
    };

    const handleConfirmarBaixa = async () => {
        if (!pecaParaBaixa || quantidadeBaixa <= 0) return;
        if (quantidadeBaixa > pecaParaBaixa.estoqueAtual) return alert("Estoque insuficiente!");

        try {
            await api.post(`/estoque/saida/${osId}`, { pecaId: pecaParaBaixa.id, quantidade: Number(quantidadeBaixa) });
            alert("Baixa realizada e vinculada à máquina!");
            setCodigoPecaInput(''); setPecaParaBaixa(null); setQuantidadeBaixa(1);
            const consumoRes = await api.get(`/estoque/consumo-os/${osId}`);
            setPecasConsumidas(consumoRes.data);
        } catch (error) { alert("Erro ao registrar baixa."); }
    };

    const handleChecklistChange = (itemId, status) => {
        setChecklistRespostas(prev => ({ ...prev, [itemId]: { ...(prev[itemId] || {}), status } }));
    };

    const handleFotoChange = (e) => {
        const file = e.target.files[0];
        if (file) setFoto({ file, preview: URL.createObjectURL(file) });
    };

    const finalizar = async () => {
        setIsSubmitting(true);
        try {
            if (os.status === 'AGENDADA') await api.post(`/ordens-servico/${os.id}/iniciar`);
            if (foto) {
                const form = new FormData(); form.append('file', foto.file);
                const up = await api.post('/fotos/upload', form);
                await api.post(`/ordens-servico/${os.id}/fotos`, JSON.stringify(up.data), { headers: {'Content-Type': 'application/json'} });
            }
            const resultados = Object.keys(checklistRespostas).map(id => ({
                itemTemplateId: Number(id), 
                status: checklistRespostas[id].status || 'PENDENTE',
                observacao: checklistRespostas[id].observacao || ''
            }));
            await api.post(`/ordens-servico/${os.id}/finalizar`, { observacoesTecnico: observacoes, resultados });
            setModal({ show: true, message: 'Preventiva Finalizada!', type: 'success' });
        } catch (e) { setIsSubmitting(false); alert("Falha ao finalizar."); }
    };

    if (isLoading) return <LoadingSpinner />;

    return (
        <div className="screen-container detail-screen">
             {modal.show && <Modal message={modal.message} onClose={() => {setModal({show:false}); if(modal.type==='success') onBack();}} type={modal.type} />}
            <header className="screen-header"><button onClick={onBack} className="back-button"><i className="fa fa-arrow-left"></i></button><h1>OS #{os?.id}</h1></header>
            
            <div className="screen-content">
                <div className="detail-section">
                    <h3>{os?.nomeEquipamento} ({os?.codigoEquipamento})</h3>
                    {os?.status === 'PENDENTE_DE_CORRECAO' && <p className="obs-lider"><strong>Obs Líder:</strong> {os.observacoesLider}</p>}
                </div>

                {/* --- NOVA SEÇÃO: BAIXA DE PEÇAS NO MOBILE --- */}
                <div className="detail-section" style={{backgroundColor: '#f8f9fa'}}>
                    <h4>Baixa de Peças de Reposição</h4>
                    <div className="input-group" style={{display: 'flex', gap: '8px', marginBottom: '10px'}}>
                        <input type="text" value={codigoPecaInput} onChange={e=>setCodigoPecaInput(e.target.value)} placeholder="Código QR da Peça" style={{flex:1}} />
                        <button onClick={handleBuscaPeca} style={{padding: '0 15px', background: '#333', color: '#fff', border: 'none', borderRadius: '4px'}}>Buscar</button>
                    </div>

                    {pecaParaBaixa && (
                        <div style={{border: '1px solid #28a745', padding: '15px', borderRadius: '6px', background: '#e6ffe6'}}>
                            <p><strong>{pecaParaBaixa.nome}</strong> (Estoque: {pecaParaBaixa.estoqueAtual})</p>
                            <div className="input-group" style={{display: 'flex', gap: '10px', marginTop: '10px', alignItems: 'center'}}>
                                <label style={{margin:0}}>Qtd:</label>
                                <input type="number" value={quantidadeBaixa} onChange={e=>setQuantidadeBaixa(e.target.value)} min="1" style={{width: '70px'}} />
                                <button onClick={handleConfirmarBaixa} style={{flex:1, background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', height: '40px'}}>Confirmar Uso</button>
                            </div>
                        </div>
                    )}

                    {pecasConsumidas.length > 0 && (
                        <div style={{marginTop: '15px', borderTop: '1px solid #ddd', paddingTop: '10px'}}>
                            <p style={{fontSize: '0.85rem'}}><strong>Peças Utilizadas nesta OS:</strong></p>
                            <ul style={{fontSize: '0.85rem', paddingLeft: '1.2rem', marginTop: '5px'}}>
                                {pecasConsumidas.map(p => <li key={p.id}>{p.quantidade}x {p.nomePeca}</li>)}
                            </ul>
                        </div>
                    )}
                </div>

                {os?.checklist?.itens?.map(item => (
                    <div key={item.id} className="checklist-item">
                        <p>{item.descricao}</p>
                        <div className="checklist-options">
                            <button className={checklistRespostas[item.id]?.status === 'OK' ? 'active ok' : ''} onClick={() => handleChecklistChange(item.id, 'OK')}>OK</button>
                            <button className={checklistRespostas[item.id]?.status === 'NAO_CONFORMIDADE' ? 'active nao-ok' : ''} onClick={() => handleChecklistChange(item.id, 'NAO_CONFORMIDADE')}>Não OK</button>
                            <button className={checklistRespostas[item.id]?.status === 'NAO_APLICAVEL' ? 'active na' : ''} onClick={() => handleChecklistChange(item.id, 'NAO_APLICAVEL')}>N/A</button>
                        </div>
                    </div>
                ))}

                <div className="detail-section">
                    <h4>Evidência Fotográfica</h4>
                    <input type="file" accept="image/*" capture="environment" ref={fileInputRef} onChange={handleFotoChange} style={{display:'none'}} />
                    <button className="photo-button" onClick={()=>fileInputRef.current.click()}>{foto ? 'Trocar Foto' : 'Tirar Foto (Obrigatório)'}</button>
                    {foto && <img src={foto.preview} className="photo-preview" alt="Evidência" />}
                </div>

                <div className="detail-section">
                    <h4>Observações do Técnico</h4>
                    <textarea value={observacoes} onChange={e=>setObservacoes(e.target.value)} rows="4" placeholder="Detalhes do serviço..." />
                </div>
            </div>

            <div className="sticky-footer">
                <button onClick={finalizar} disabled={isSubmitting || (os?.status==='AGENDADA' && !foto)}>
                    {isSubmitting ? 'Finalizando...' : 'Finalizar Preventiva'}
                </button>
            </div>
        </div>
    );
};

// --- TELAS DE CONSULTA E GESTÃO ---
const ConsultaEstoqueScreen = ({ onLogout }) => (
    <div className="screen-container">
        <header className="screen-header"><h1>Consulta de Estoque</h1><button onClick={onLogout} className="logout-button"><i className="fa fa-sign-out"></i></button></header>
        <div className="screen-content"><p style={{textAlign:'center', marginTop:'2rem'}}>Módulo de consulta de peças em desenvolvimento.</p></div>
    </div>
);

const GestaoEstoqueScreen = ({ onLogout }) => (
    <div className="screen-container">
        <header className="screen-header"><h1>Gestão de Estoque</h1><button onClick={onLogout} className="logout-button"><i className="fa fa-sign-out"></i></button></header>
        <div className="screen-content"><p style={{textAlign:'center', marginTop:'2rem'}}>Módulo de entrada de material em desenvolvimento.</p></div>
    </div>
);

// --- COMPONENTE PRINCIPAL ---
const MainApp = () => {
  const { user, logout } = useAuth();
  const [currentPage, setCurrentPage] = useState('preventivas');
  const [selectedOS, setSelectedOS] = useState(null);

  if (selectedOS) return <DetalheOSScreen osId={selectedOS} onBack={() => setSelectedOS(null)} />;

  return (
    <div className="app-container">
      <div className="content-area">
        {currentPage === 'preventivas' && <PreventivasScreen user={user} onSelectOS={setSelectedOS} onLogout={logout} />}
        {currentPage === 'consulta' && <ConsultaEstoqueScreen onLogout={logout} />}
        {currentPage === 'gestao' && <GestaoEstoqueScreen onLogout={logout} />}
      </div>
      <BottomNavBar active={currentPage} setActive={setCurrentPage} />
    </div>
  );
};

export default function App() {
  const { user, login, logout, loading } = useAuth();
  if (loading) return <LoadingSpinner />;
  return (
    <>
      <GlobalStyles />
      {user ? <MainApp /> : <LoginScreen onLogin={login} />}
    </>
  );
}

// --- ESTILOS GLOBAIS (PRESERVADOS) ---
const GlobalStyles = () => (
  <style>{`
    @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css');
    :root { --primary-color: #000; --secondary-color: #ffc107; --background-color: #f4f4f6; --surface-color: #ffffff; --success-color: #28a745; --error-color: #dc3545; }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: sans-serif; background-color: var(--background-color); color: #333; }
    .app-container { display: flex; flex-direction: column; height: 100vh; max-width: 600px; margin: 0 auto; background: var(--surface-color); box-shadow: 0 0 20px rgba(0,0,0,0.1); }
    .content-area { flex: 1; overflow-y: auto; }
    .screen-header { display: flex; justify-content: space-between; align-items: center; padding: 1rem; border-bottom: 1px solid #ddd; }
    .screen-content { padding: 1rem; padding-bottom: 140px; }
    .os-card { border: 1px solid #ddd; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
    .os-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 5px; }
    .status-pill { padding: 4px 10px; border-radius: 12px; font-size: 0.7rem; font-weight: bold; color: #fff; text-transform: uppercase; }
    .status-agendada { background: #007bff; }
    .status-pendente { background: #fd7e14; }
    .bottom-nav { display: flex; position: fixed; bottom: 0; width: 100%; max-width: 600px; height: 65px; background: #fff; border-top: 1px solid #ddd; z-index: 100; }
    .bottom-nav button { flex: 1; border: none; background: none; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 0.7rem; color: #666; cursor: pointer; }
    .bottom-nav button i { font-size: 1.4rem; margin-bottom: 3px; }
    .bottom-nav button.active { color: var(--primary-color); font-weight: bold; }
    .detail-section { padding: 1rem 0; border-bottom: 1px solid #eee; }
    .checklist-item { margin-bottom: 1.5rem; }
    .checklist-options { display: flex; gap: 8px; margin-top: 8px; }
    .checklist-options button { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 6px; background: #f8f9fa; font-weight: bold; }
    .checklist-options button.active.ok { background: var(--success-color); color: #fff; border-color: #1e7e34; }
    .checklist-options button.active.nao-ok { background: var(--error-color); color: #fff; border-color: #bd2130; }
    .checklist-options button.active.na { background: #6c757d; color: #fff; border-color: #545b62; }
    .photo-button { width: 100%; padding: 15px; background: #000; color: #fff; border: none; border-radius: 6px; font-weight: bold; font-size: 1rem; }
    .photo-preview { width: 100%; margin-top: 15px; border-radius: 8px; border: 1px solid #ddd; }
    .sticky-footer { position: fixed; bottom: 65px; width: 100%; max-width: 600px; padding: 1rem; background: #fff; border-top: 1px solid #ddd; z-index: 90; }
    .sticky-footer button { width: 100%; padding: 18px; background: var(--success-color); color: #fff; border: none; border-radius: 6px; font-weight: bold; font-size: 1.1rem; }
    .sticky-footer button:disabled { background: #94d3a2; cursor: not-allowed; }
    .input-group { margin-bottom: 1rem; }
    .input-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 0.9rem; }
    .input-group input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 1rem; }
    textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 1rem; font-family: sans-serif; }
    .login-container { height: 100vh; background: #000; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; color: #fff; }
    .login-header { text-align: center; margin-bottom: 2rem; }
    .login-form { background: #fff; width: 100%; padding: 2rem; border-radius: 12px; color: #333; }
    .login-form button { width: 100%; padding: 15px; background: #000; color: #fff; border: none; border-radius: 6px; font-weight: bold; margin-top: 10px; }
    .loading-overlay { position: absolute; top:0; left:0; width:100%; height:100%; background:rgba(255,255,255,0.8); display:flex; align-items:center; justify-content:center; z-index:1000; }
    .spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #000; border-radius: 50%; animation: spin 1s linear infinite; }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
  `}</style>
);
