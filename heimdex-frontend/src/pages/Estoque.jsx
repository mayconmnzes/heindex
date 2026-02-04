import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const AREAS_API_URL = `${API_BASE}/api/areas`;
const MODELOS_API_URL = `${API_BASE}/api/modelos`;
const PECAS_API_URL = `${API_BASE}/api/pecas`;
const EQUIPAMENTOS_API_URL = `${API_BASE}/api/equipamentos`;

function Estoque() {
    const [areas, setAreas] = useState([]);
    const [modelos, setModelos] = useState([]);
    const [pecas, setPecas] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [loading, setLoading] = useState(true);

    // --- ESTADOS DE FILTRO ---
    const [filterAreaId, setFilterAreaId] = useState('');
    const [filterEquipamentoId, setFilterEquipamentoId] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    // --- ESTADOS DE MODAIS ---
    const [showModalBaixa, setShowModalBaixa] = useState(false);
    const [showModalEntrada, setShowModalEntrada] = useState(false);
    const [showModalHistorico, setShowModalHistorico] = useState(false);
    const [showModalRelatorioGeral, setShowModalRelatorioGeral] = useState(false);
    
    // ✅ 1. CORREÇÃO: Estado para o Zoom das Imagens
    const [modalImage, setModalImage] = useState(null);
    
    // --- ESTADOS DE SELEÇÃO E RELATÓRIO ---
    const [selectedPeca, setSelectedPeca] = useState(null);
    const [historicoPeca, setHistoricoPeca] = useState([]);
    const [relatorioFiltro, setRelatorioFiltro] = useState({ todos: true, meses: [], ano: new Date().getFullYear() });
    
    const [baixaForm, setBaixaForm] = useState({ equipamentoId: '', quantidade: 1 });
    const [entradaForm, setEntradaForm] = useState({ quantidade: 1 });

    const mesesOpcoes = [
        { val: 0, nome: 'Jan' }, { val: 1, nome: 'Fev' }, { val: 2, nome: 'Mar' },
        { val: 3, nome: 'Abr' }, { val: 4, nome: 'Mai' }, { val: 5, nome: 'Jun' },
        { val: 6, nome: 'Jul' }, { val: 7, nome: 'Ago' }, { val: 8, nome: 'Set' },
        { val: 9, nome: 'Out' }, { val: 10, nome: 'Nov' }, { val: 11, nome: 'Dez' }
    ];

    const uniqueNums = (arr) => {
        const set = new Set();
        (arr || []).forEach(v => {
            const n = Number(v);
            if (Number.isFinite(n)) set.add(n);
        });
        return Array.from(set);
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const [areasRes, modelosRes, pecasRes, equipRes] = await Promise.all([
                axios.get(AREAS_API_URL),
                axios.get(MODELOS_API_URL),
                axios.get(PECAS_API_URL),
                axios.get(EQUIPAMENTOS_API_URL)
            ]);

            const equipamentosPadronizados = equipRes.data.map(e => ({
                ...e,
                areaId: e.areaId || e.idArea || e.area?.id || e.id_area 
            }));

            setAreas(areasRes.data);
            setModelos(modelosRes.data);
            setPecas(pecasRes.data);
            setEquipamentos(equipamentosPadronizados);
        } catch (error) {
            console.error("Erro ao carregar dados:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    // ✅ 3. CORREÇÃO: Fechar modal com ESC
    useEffect(() => {
        const handleEsc = (e) => { if (e.key === 'Escape') setModalImage(null); };
        window.addEventListener('keydown', handleEsc);
        return () => window.removeEventListener('keydown', handleEsc);
    }, []);

    const maquinasFiltradasParaSelect = useMemo(() => {
        if (!filterAreaId) return equipamentos;
        return equipamentos.filter(e => Number(e.areaId) === Number(filterAreaId));
    }, [equipamentos, filterAreaId]);

    const filteredPecas = useMemo(() => {
        return pecas.filter(peca => {
            const idsModelosPeca = uniqueNums([...(peca.modelosIds || []), peca.modeloEquipamentoId]);
            const idsAreasPeca = uniqueNums(idsModelosPeca.map(mId => modelos.find(m => m.id === mId)?.areaId));
            const matchSearch = !searchTerm || peca.nome?.toLowerCase().includes(searchTerm.toLowerCase()) || peca.codigoControle?.toLowerCase().includes(searchTerm.toLowerCase());
            const matchArea = !filterAreaId || idsAreasPeca.includes(Number(filterAreaId));
            const maquinaSelecionada = equipamentos.find(e => e.id === Number(filterEquipamentoId));
            const idModeloDaMaquina = maquinaSelecionada?.modeloId || maquinaSelecionada?.modelo?.id;
            const matchEquipamento = !filterEquipamentoId || idsModelosPeca.includes(Number(idModeloDaMaquina));
            return matchSearch && matchArea && matchEquipamento;
        });
    }, [pecas, modelos, equipamentos, searchTerm, filterAreaId, filterEquipamentoId]);

    const handleExportarRelatorioGeral = async () => {
        try {
            const res = await axios.get(`${PECAS_API_URL}/historico/geral`); 
            let dados = res.data;

            if (!relatorioFiltro.todos) {
                dados = dados.filter(h => {
                    const data = new Date(h.dataMovimentacao);
                    const matchAno = data.getFullYear() === Number(relatorioFiltro.ano);
                    const matchMes = relatorioFiltro.meses.includes(data.getMonth());
                    return matchAno && matchMes;
                });
            }

            const headers = ["Data/Hora", "Tipo", "Peca", "Cod. Req.", "Quantidade", "Maquina", "Area", "Responsavel"];
            const rows = dados.map(h => [
                new Date(h.dataMovimentacao).toLocaleString('pt-BR'),
                h.tipoMovimentacao,
                h.nomePeca || "N/A",
                h.codigoRequisicao || "N/A",
                h.quantidade,
                h.nomeEquipamento || "Geral",
                h.nomeArea || "N/A",
                h.loginUsuario || h.nomeUsuario || "Sistema" 
            ]);

            let csvContent = "data:text/csv;charset=utf-8,\uFEFF" + headers.join(",") + "\n" + rows.map(e => e.join(",")).join("\n");
            const encodedUri = encodeURI(csvContent);
            const link = document.createElement("a");
            link.setAttribute("href", encodedUri);
            link.setAttribute("download", `RELATORIO_ESTOQUE_${relatorioFiltro.todos ? 'GERAL' : relatorioFiltro.ano}.csv`);
            document.body.appendChild(link);
            link.click();
            setShowModalRelatorioGeral(false);
        } catch (error) {
            alert("Erro ao exportar relatório.");
        }
    };

    // ✅ CORREÇÃO: Função adicionada para evitar tela em branco
    const handleExportarCriticos = () => {
        const itensCriticos = pecas.filter(p => p.estoqueAtual <= p.estoqueMinimo);
        if (itensCriticos.length === 0) return alert("Nenhum item com estoque crítico!");
        
        const headers = ["ID", "Nome da Peca", "Codigo Requisicao", "Estoque Atual", "Estoque Minimo", "Area"];
        const rows = itensCriticos.map(p => [p.id, p.nome, p.codigoRequisicao || "N/A", p.estoqueAtual, p.estoqueMinimo, p.nomeArea || "N/A"]);

        let csvContent = "data:text/csv;charset=utf-8,\uFEFF" + headers.join(",") + "\n" + rows.map(e => e.join(",")).join("\n");
        const link = document.createElement("a");
        link.setAttribute("href", encodeURI(csvContent));
        link.setAttribute("download", `COMPRAS_NECESSARIAS_${new Date().toLocaleDateString('pt-BR')}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const handleVerHistorico = async (peca) => {
        setSelectedPeca(peca);
        try {
            const res = await axios.get(`${PECAS_API_URL}/${peca.id}/historico`);
            setHistoricoPeca(res.data);
            setShowModalHistorico(true);
        } catch (error) { alert("Erro ao carregar histórico."); }
    };

    const handleConfirmarBaixa = async () => {
        if (!baixaForm.equipamentoId) return alert("Selecione uma máquina!");
        const user = JSON.parse(localStorage.getItem('user') || localStorage.getItem('user'));
        console.log("Usuário logado enviando ID:", user?.id);
        try {
            await axios.post(`${PECAS_API_URL}/${selectedPeca.id}/saida`, {
                pecaId: selectedPeca.id,
                quantidade: baixaForm.quantidade,
                equipamentoId: baixaForm.equipamentoId,
                usuarioId: user?.id 
            });
            alert("Baixa realizada!"); setShowModalBaixa(false); fetchData();
        } catch (error) { alert("Erro na baixa."); }
    };

    const handleConfirmarEntrada = async () => {
        const user = JSON.parse(localStorage.getItem('user') || localStorage.getItem('user'));
        try {
            await axios.post(`${PECAS_API_URL}/${selectedPeca.id}/entrada`, {
                quantidade: entradaForm.quantidade,
                usuarioId: user?.id 
            });
            alert("Entrada registrada!"); setShowModalEntrada(false); fetchData();
        } catch (error) { alert("Erro na entrada."); }
    };

    if (loading) return <div className="main-content"><h1>Carregando...</h1></div>;

    return (
        <div className="main-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1>Consulta de Estoque</h1>
                
                {/* ✅ CONTAINER DE BOTÕES DE EXPORTAÇÃO */}
                <div style={{ display: 'flex', gap: '10px' }}>
                    
                    {/* NOVO BOTÃO: Itens Críticos (Abaixo do mínimo) */}
                    <button 
                        onClick={handleExportarCriticos}
                        style={{ 
                            background: '#e74c3c', 
                            color: '#fff', 
                            border: 'none', 
                            padding: '10px 20px', 
                            borderRadius: '6px', 
                            cursor: 'pointer', 
                            fontWeight: 'bold', 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px' 
                        }}
                    >
                        <i className="fa fa-warning"></i> BAIXAR ITENS CRÍTICOS
                    </button>

                    {/* BOTÃO EXISTENTE: Relatório Geral */}
                    <button 
                        onClick={() => setShowModalRelatorioGeral(true)}
                        style={{ 
                            background: '#27ae60', 
                            color: '#fff', 
                            border: 'none', 
                            padding: '10px 20px', 
                            borderRadius: '6px', 
                            cursor: 'pointer', 
                            fontWeight: 'bold', 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px' 
                        }}
                    >
                        <i className="fa fa-file-excel-o"></i> EXPORTAR RELATÓRIO
                    </button>
                </div>
            </div>
          
            <div className="filter-container" style={{ display: 'flex', gap: '15px', marginBottom: '20px', background: '#f8f9fa', padding: '15px', borderRadius: '8px' }}>
                <div style={{ flex: 1 }}><label>Pesquisar:</label><input type="text" placeholder="Nome ou Código..." value={searchTerm} onChange={e => setSearchTerm(e.target.value)} style={{ width: '100%', padding: '8px' }} /></div>
                <div style={{ flex: 1 }}><label>Área:</label><select value={filterAreaId} onChange={e => { setFilterAreaId(e.target.value); setFilterEquipamentoId(''); }}><option value="">Todas as Áreas</option>{areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}</select></div>
                <div style={{ flex: 1 }}><label>Máquina:</label><select value={filterEquipamentoId} onChange={e => setFilterEquipamentoId(e.target.value)}><option value="">Todas as Máquinas</option>{maquinasFiltradasParaSelect.map(eq => (<option key={eq.id} value={eq.id}>{eq.nome}</option>))}</select></div>
            </div>

            <section className="list-section">
                <div className="table-container">
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: '#34495e', color: '#0a0a0a' }}>
                                <th>Foto</th><th>QR Code</th><th>Nome</th><th>Cód. Req.</th><th>Área(s)</th><th>Modelo(s) Associado(s)</th><th>Estoque (A/M)</th><th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredPecas.map(peca => {
                                const exibirArea = peca.nomeArea || 'N/A';
                                const exibirModelo = peca.nomeModeloEquipamento || 'Múltiplos';

                                return (
                                    <tr key={peca.id} style={peca.estoqueAtual <= peca.estoqueMinimo ? { backgroundColor: '#fff3cd' } : { borderBottom: '1px solid #eee' }}>
                                        <td style={{ textAlign: 'center' }}>
                                            {peca.fotoUrl ? (
                                                <img 
                                                    src={`${API_BASE}/uploads/${peca.fotoUrl}`} 
                                                    alt="P" 
                                                    style={{ width: '45px', height: '45px', objectFit: 'cover', borderRadius: '4px', cursor: 'zoom-in' }} 
                                                    onClick={() => setModalImage(`${API_BASE}/uploads/${peca.fotoUrl}`)}
                                                />
                                            ) : '-'}
                                        </td>
                                        
                                        <td style={{ textAlign: 'center' }}>
                                            <img 
                                                src={`https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${peca.codigoRequisicao || 'ID-'+peca.id}`} 
                                                alt="QR" 
                                                style={{ width: '40px', cursor: 'zoom-in' }} 
                                                onClick={() => setModalImage(`https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=${peca.codigoRequisicao || 'ID-'+peca.id}`)}
                                            />
                                        </td>
                                        
                                        <td><strong>{peca.nome}</strong></td>
                                        <td>{peca.codigoRequisicao || 'N/A'}</td>

                                        <td style={{ fontSize: '0.8rem' }}>{exibirArea}</td>
                                        <td style={{ fontSize: '0.8rem' }}>{exibirModelo}</td>

                                        <td 
                                            className={peca.estoqueAtual <= peca.estoqueMinimo ? 'estoque-critico' : ''}
                                            style={{ textAlign: 'center', padding: '10px' }}
                                        >
                                            {peca.estoqueAtual} / {peca.estoqueMinimo}
                                        </td>

                                        <td>
                                            <button onClick={() => {setSelectedPeca(peca); setShowModalBaixa(true);}} style={{background: '#f39c12', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', marginRight: '4px'}}>Consumir</button>
                                            <button onClick={() => {setSelectedPeca(peca); setShowModalEntrada(true);}} style={{background: '#2ecc71', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', marginRight: '4px'}}>Entrada</button>
                                            <button onClick={() => handleVerHistorico(peca)} style={{background: '#34495e', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer'}}>Histórico</button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </section>

            {modalImage && (
                <div 
                    style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(0,0,0,0.85)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 10000, cursor: 'pointer' }}
                    onClick={() => setModalImage(null)}
                >
                    <div style={{ position: 'relative', background: '#fff', padding: '15px', borderRadius: '12px' }}>
                        <img src={modalImage} style={{ maxWidth: '90vw', maxHeight: '85vh', borderRadius: '8px', display: 'block' }} alt="Ampliada" />
                        <div style={{ textAlign: 'center', marginTop: '10px', color: '#333', fontWeight: 'bold' }}>ESC ou Clique para fechar</div>
                    </div>
                </div>
            )}

            {showModalRelatorioGeral && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1001}}>
                    <div style={{background: '#fff', padding: '30px', borderRadius: '12px', width: '550px'}}>
                        <h2 style={{ marginBottom: '20px', color: '#333' }}>Exportar Histórico de Consumo</h2>
                        
                        <div style={{ marginBottom: '20px' }}>
                            <button 
                                onClick={() => setRelatorioFiltro({ ...relatorioFiltro, todos: true, meses: [] })}
                                style={{ 
                                    width: '100%', 
                                    padding: '12px', 
                                    background: relatorioFiltro.todos ? '#27ae60' : '#f1f1f1', 
                                    color: relatorioFiltro.todos ? '#fff' : '#333', 
                                    border: '1px solid #ddd', 
                                    borderRadius: '6px', 
                                    fontWeight: 'bold', 
                                    cursor: 'pointer',
                                    marginBottom: '15px'
                                }}
                            >
                                GERAR TUDO DESDE O INÍCIO
                            </button>
                            
                            <p style={{ fontSize: '0.9rem', color: '#666', marginBottom: '10px' }}>Ou selecione meses específicos de <strong>{relatorioFiltro.ano}</strong>:</p>
                            
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px' }}>
                                {mesesOpcoes.map(m => {
                                    const selecionado = relatorioFiltro.meses.includes(m.val);
                                    return (
                                        <button
                                            key={m.val}
                                            onClick={() => {
                                                let novosMeses = [...relatorioFiltro.meses];
                                                if (selecionado) {
                                                    novosMeses = novosMeses.filter(v => v !== m.val);
                                                } else {
                                                    novosMeses.push(m.val);
                                                }
                                                setRelatorioFiltro({ ...relatorioFiltro, todos: false, meses: novosMeses });
                                            }}
                                            style={{
                                                padding: '8px',
                                                borderRadius: '4px',
                                                border: '1px solid #ddd',
                                                cursor: 'pointer',
                                                fontSize: '0.85rem',
                                                background: selecionado ? '#3498db' : '#fff',
                                                color: selecionado ? '#fff' : '#333',
                                                fontWeight: selecionado ? 'bold' : 'normal'
                                            }}
                                        >
                                            {m.nome}
                                        </button>
                                    );
                                })}
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '10px', marginTop: '30px' }}>
                            <button 
                                onClick={handleExportarRelatorioGeral} 
                                style={{ flex: 2, background: '#27ae60', color: '#fff', border: 'none', padding: '12px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}
                            >
                                BAIXAR RELATÓRIO {relatorioFiltro.meses.length > 0 && `(${relatorioFiltro.meses.length} MESES)`}
                            </button>
                            <button 
                                onClick={() => setShowModalRelatorioGeral(false)} 
                                style={{ flex: 1, background: '#95a5a6', color: '#fff', border: 'none', padding: '12px', borderRadius: '6px', cursor: 'pointer' }}
                            >
                                FECHAR
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {showModalBaixa && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '400px'}}>
                        <h3 style={{ color: '#333' }}>Consumir Peça: {selectedPeca?.nome}</h3>
                        
                        <label style={{ display: 'block', marginTop: '10px', color: '#333' }}>Máquina Destino:</label>
                        <select 
                            style={{width: '100%', padding: '10px', marginBottom: '15px'}} 
                            value={baixaForm.equipamentoId} 
                            onChange={e => setBaixaForm({...baixaForm, equipamentoId: e.target.value})}
                        >
                            <option value="">Selecione...</option>
                            {equipamentos.map(eq => <option key={eq.id} value={eq.id}>{eq.nome}</option>)}
                        </select>

                        <label style={{ display: 'block', color: '#333' }}>Quantidade a Consumir:</label>
                        <input 
                            type="number" 
                            style={{width: '100%', padding: '10px', marginBottom: '15px', border: '1px solid #ccc', borderRadius: '4px'}} 
                            value={baixaForm.quantidade} 
                            onChange={e => setBaixaForm({...baixaForm, quantidade: parseInt(e.target.value) || 1})} 
                            min="1"
                        />

                        <button 
                            onClick={handleConfirmarBaixa} 
                            style={{ background: '#e67e22', color: '#fff', width: '100%', padding: '12px', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}
                        >Confirmar Baixa</button>

                        <button 
                            onClick={() => setShowModalBaixa(false)} 
                            style={{ marginTop: '10px', width: '100%', border: 'none', background: 'none', color: '#666', cursor: 'pointer', fontWeight: 'bold' }}
                        >Cancelar</button>
                    </div>
                </div>
            )}
            
            {showModalEntrada && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '400px'}}>
                        <h3 style={{ color: '#333' }}>Entrada de Estoque: {selectedPeca?.nome}</h3>
                        
                        <label style={{ display: 'block', marginBottom: '5px', color: '#333' }}>Quantidade de Entrada:</label>
                        <input type="number" style={{width: '100%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px'}} value={entradaForm.quantidade} onChange={e => setEntradaForm({quantidade: parseInt(e.target.value) || 1})} min="1"/>
                        
                        <button onClick={handleConfirmarEntrada} style={{marginTop: '15px', background: '#2ecc71', color: '#fff', width: '100%', padding: '12px', border:'none', borderRadius:'4px', fontWeight: 'bold', cursor: 'pointer'}}>Confirmar Entrada</button>
                        
                        <button 
                            onClick={() => setShowModalEntrada(false)} 
                            style={{marginTop: '10px', width: '100%', border:'none', background:'none', color: '#666', cursor: 'pointer', fontWeight: '500'}}
                        >Cancelar</button>
                    </div>
                </div>
            )}

            {showModalHistorico && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '800px', maxHeight: '70vh', overflowY: 'auto'}}>
                        <h3>Histórico Individual: {selectedPeca?.nome}</h3>
                        <table style={{width: '100%', borderCollapse: 'collapse', marginTop: '10px'}}>
                            <thead style={{background: '#f8f9fa'}}>
                                <tr>
                                    <th>Data</th>
                                    <th>Tipo</th>
                                    <th>Qtd</th>
                                    <th>Máquina</th>
                                    <th>Responsável</th>
                                </tr>
                            </thead>
                            <tbody>
                                {historicoPeca.map((h, i) => (
                                    <tr key={i}>
                                        <td style={{padding:'8px'}}>{new Date(h.dataMovimentacao).toLocaleString()}</td>
                                        <td style={{color: h.tipoMovimentacao === 'ENTRADA' ? 'green' : 'red'}}>{h.tipoMovimentacao}</td>
                                        <td>{h.quantidade}</td>
                                        <td>{h.nomeEquipamento || "-"}</td>
                                        <td style={{ fontWeight: 'bold' }}>{h.loginUsuario || "Sistema"}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <button onClick={() => setShowModalHistorico(false)} style={{marginTop: '20px', padding:'8px 20px', cursor: 'pointer', background: '#34495e', color: '#fff', border: 'none', borderRadius: '4px'}}>Fechar</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Estoque;