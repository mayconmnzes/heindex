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

    // ✅ NOVO: Lógica de Exportação Geral (Melhorada)
    const handleExportarRelatorioGeral = async () => {
        try {
            // Tentamos o endpoint de histórico global que centraliza todas as movimentações
            const res = await axios.get(`${PECAS_API_URL}/historico/geral`); 
            let dados = res.data;

            // Filtro por Ano e Meses selecionados
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
                h.nomeUsuario || "Sistema"
            ]);

            // UTF-8 BOM para o Excel abrir com acentuação correta
            let csvContent = "data:text/csv;charset=utf-8,\uFEFF" + headers.join(",") + "\n" + rows.map(e => e.join(",")).join("\n");
            const encodedUri = encodeURI(csvContent);
            const link = document.createElement("a");
            link.setAttribute("href", encodedUri);
            link.setAttribute("download", `RELATORIO_ESTOQUE_${relatorioFiltro.todos ? 'GERAL' : relatorioFiltro.ano}.csv`);
            document.body.appendChild(link);
            link.click();
            setShowModalRelatorioGeral(false);
        } catch (error) {
            alert("Erro: O endpoint '/api/pecas/historico/geral' não foi encontrado no backend.");
        }
    };

    // --- HANDLERS PADRÃO ---
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
        const user = JSON.parse(localStorage.getItem('heimdex_user') || localStorage.getItem('user'));
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
        const user = JSON.parse(localStorage.getItem('heimdex_user') || localStorage.getItem('user'));
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
                <button 
                    onClick={() => setShowModalRelatorioGeral(true)}
                    style={{ background: '#27ae60', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                    <i className="fa fa-file-excel-o"></i> EXPORTAR RELATÓRIO
                </button>
            </div>

            {/* --- FILTROS --- */}
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
                                const idsModelos = uniqueNums([...(peca.modelosIds || []), peca.modeloEquipamentoId]);
                                const modelosNomes = idsModelos.map(id => modelos.find(mm => mm.id === id)?.nome).filter(Boolean).join(', ') || 'N/A';
                                const areasNomes = [...new Set(idsModelos.map(id => areas.find(a => a.id === modelos.find(m => m.id === id)?.areaId)?.nome))].filter(Boolean).join(', ') || 'N/A';
                                return (
                                    <tr key={peca.id} style={peca.estoqueAtual <= peca.estoqueMinimo ? { backgroundColor: '#fff3cd' } : { borderBottom: '1px solid #eee' }}>
                                        <td style={{ textAlign: 'center' }}>{peca.fotoUrl ? <img src={peca.fotoUrl} alt="P" style={{ width: '45px', height: '45px', objectFit: 'cover', borderRadius: '4px' }} /> : '-'}</td>
                                        <td style={{ textAlign: 'center' }}><img src={`${PECAS_API_URL}/${peca.id}/qrcode`} alt="QR" style={{ width: '40px' }} /></td>
                                        <td><strong>{peca.nome}</strong></td>
                                        <td>{peca.codigoRequisicao || 'N/A'}</td>
                                        <td style={{ fontSize: '0.8rem' }}>{areasNomes}</td>
                                        <td style={{ fontSize: '0.8rem' }}>{modelosNomes}</td>
                                        <td style={{ fontWeight: 'bold', color: peca.estoqueAtual <= peca.estoqueMinimo ? 'red' : 'inherit', textAlign: 'center' }}>{peca.estoqueAtual} / {peca.estoqueMinimo}</td>
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

            {/* ✅ MODAL DE RELATÓRIO GERAL (CENTRO DE EXPORTAÇÃO) */}
            {showModalRelatorioGeral && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1001}}>
                    <div style={{background: '#fff', padding: '30px', borderRadius: '12px', width: '500px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)'}}>
                        <h2 style={{ marginBottom: '10px' }}>Exportar Histórico de Consumo</h2>
                        <p style={{ color: '#666', marginBottom: '20px' }}>Selecione o período para gerar o arquivo .CSV consolidado.</p>
                        
                        <div style={{ marginBottom: '20px' }}>
                            <button 
                                onClick={() => setRelatorioFiltro({ ...relatorioFiltro, todos: true, meses: [] })}
                                style={{ width: '100%', padding: '12px', background: relatorioFiltro.todos ? '#27ae60' : '#eee', color: relatorioFiltro.todos ? '#fff' : '#333', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}
                            >GERAR TUDO DESDE O INÍCIO</button>
                        </div>

                        <div style={{ borderTop: '1px solid #eee', paddingTop: '20px' }}>
                            <label style={{ fontWeight: 'bold' }}>Ou filtrar por período:</label>
                            <div style={{ display: 'flex', gap: '10px', marginTop: '10px', marginBottom: '15px' }}>
                                <input type="number" value={relatorioFiltro.ano} onChange={e => setRelatorioFiltro({...relatorioFiltro, ano: e.target.value, todos: false})} style={{ padding: '8px', width: '80px', borderRadius: '4px', border: '1px solid #ddd' }} />
                                <span style={{ alignSelf: 'center' }}>Ano de referência</span>
                            </div>
                            
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px' }}>
                                {mesesOpcoes.map(m => (
                                    <button 
                                        key={m.val}
                                        onClick={() => {
                                            const jaExiste = relatorioFiltro.meses.includes(m.val);
                                            const novosMeses = jaExiste ? relatorioFiltro.meses.filter(x => x !== m.val) : [...relatorioFiltro.meses, m.val];
                                            setRelatorioFiltro({ ...relatorioFiltro, meses: novosMeses, todos: false });
                                        }}
                                        style={{ padding: '8px', fontSize: '0.8rem', borderRadius: '4px', border: '1px solid #ddd', background: relatorioFiltro.meses.includes(m.val) ? '#3498db' : '#fff', color: relatorioFiltro.meses.includes(m.val) ? '#fff' : '#333', cursor: 'pointer' }}
                                    >{m.nome}</button>
                                ))}
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '10px', marginTop: '30px' }}>
                            <button onClick={handleExportarRelatorioGeral} style={{ flex: 2, background: '#27ae60', color: '#fff', border: 'none', padding: '12px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>BAIXAR RELATÓRIO</button>
                            <button onClick={() => setShowModalRelatorioGeral(false)} style={{ flex: 1, background: '#95a5a6', color: '#fff', border: 'none', padding: '12px', borderRadius: '6px', cursor: 'pointer' }}>FECHAR</button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAIS DE BAIXA / ENTRADA / HISTORICO INDIVIDUAL (MANTIDOS) */}
            {showModalBaixa && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '400px'}}>
                        <h3>Consumir Peça: {selectedPeca?.nome}</h3>
                        <label>Máquina Destino:</label>
                        <select style={{width: '100%', padding: '10px'}} value={baixaForm.equipamentoId} onChange={e => setBaixaForm({...baixaForm, equipamentoId: e.target.value})}><option value="">Selecione...</option>{equipamentos.map(eq => <option key={eq.id} value={eq.id}>{eq.nome}</option>)}</select>
                        <label>Quantidade:</label>
                        <input type="number" style={{width: '100%', padding: '10px'}} value={baixaForm.quantidade} onChange={e => setBaixaForm({...baixaForm, quantidade: parseInt(e.target.value) || 1})} min="1"/>
                        <button onClick={handleConfirmarBaixa} style={{marginTop: '15px', background: '#e67e22', color: '#fff', width: '100%', padding: '10px', border:'none', borderRadius:'4px'}}>Confirmar Baixa</button>
                        <button onClick={() => setShowModalBaixa(false)} style={{marginTop: '10px', width: '100%', border:'none', background:'none'}}>Cancelar</button>
                    </div>
                </div>
            )}

            {showModalEntrada && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '400px'}}>
                        <h3>Entrada de Estoque: {selectedPeca?.nome}</h3>
                        <input type="number" style={{width: '100%', padding: '10px'}} value={entradaForm.quantidade} onChange={e => setEntradaForm({quantidade: parseInt(e.target.value) || 1})} min="1"/>
                        <button onClick={handleConfirmarEntrada} style={{marginTop: '15px', background: '#2ecc71', color: '#fff', width: '100%', padding: '10px', border:'none', borderRadius:'4px'}}>Confirmar Entrada</button>
                        <button onClick={() => setShowModalEntrada(false)} style={{marginTop: '10px', width: '100%', border:'none', background:'none'}}>Cancelar</button>
                    </div>
                </div>
            )}

            {showModalHistorico && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '700px', maxHeight: '70vh', overflowY: 'auto'}}>
                        <h3>Histórico Individual: {selectedPeca?.nome}</h3>
                        <table style={{width: '100%', borderCollapse: 'collapse', marginTop: '10px'}}>
                            <thead style={{background: '#f8f9fa'}}><tr><th>Data</th><th>Tipo</th><th>Qtd</th><th>Máquina</th></tr></thead>
                            <tbody>{historicoPeca.map((h, i) => (<tr key={i}><td style={{padding:'8px'}}>{new Date(h.dataMovimentacao).toLocaleString()}</td><td style={{color: h.tipoMovimentacao === 'ENTRADA' ? 'green' : 'red'}}>{h.tipoMovimentacao}</td><td>{h.quantidade}</td><td>{h.nomeEquipamento || "-"}</td></tr>))}</tbody>
                        </table>
                        <button onClick={() => setShowModalHistorico(false)} style={{marginTop: '20px', padding:'8px 20px'}}>Fechar</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Estoque;