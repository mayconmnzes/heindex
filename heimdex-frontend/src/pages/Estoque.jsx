import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL;
const AREAS_API_URL = `${API_BASE}/areas`;
const MODELOS_API_URL = `${API_BASE}/modelos`;
const PECAS_API_URL = `${API_BASE}/pecas`;
const EQUIPAMENTOS_API_URL = `${API_BASE}/equipamentos`;
const ESTOQUE_API_URL = `${API_BASE}/estoque`;
const BACKEND_BASE_URL = API_BASE.replace('/api', '');

function Estoque() {
    const [areas, setAreas] = useState([]);
    const [modelos, setModelos] = useState([]);
    const [pecas, setPecas] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [loading, setLoading] = useState(true);

    const [selectedAreaId, setSelectedAreaId] = useState('');
    const [selectedModeloId, setSelectedModeloId] = useState('');

    const [showModal, setShowModal] = useState(false);
    const [selectedPeca, setSelectedPeca] = useState(null);
    const [baixaForm, setBaixaForm] = useState({ equipamentoId: '', quantidade: 1 });

    const fetchData = async () => {
        setLoading(true);
        try {
            const [areasRes, modelosRes, pecasRes, equipRes] = await Promise.all([
                axios.get(AREAS_API_URL),
                axios.get(MODELOS_API_URL),
                axios.get(PECAS_API_URL),
                axios.get(EQUIPAMENTOS_API_URL)
            ]);
            setAreas(areasRes.data);
            setModelos(modelosRes.data);
            setPecas(pecasRes.data);
            setEquipamentos(equipRes.data);
        } catch (error) {
            console.error("Erro ao carregar dados:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    const filteredPecas = useMemo(() => {
        const selectedAreaName = areas.find(a => a.id === Number(selectedAreaId))?.nome;
        return pecas.map(peca => {
            const mod = modelos.find(m => m.id === peca.modeloEquipamentoId);
            const area = areas.find(a => a.id === mod?.areaId);
            return { ...peca, nomeArea: area ? area.nome : 'N/A' };
        }).filter(peca => {
            const matchArea = !selectedAreaId || (peca.nomeArea === selectedAreaName);
            const matchModelo = !selectedModeloId || (peca.modeloEquipamentoId === Number(selectedModeloId));
            return matchArea && matchModelo;
        });
    }, [pecas, modelos, areas, selectedAreaId, selectedModeloId]);

    const handleConfirmarBaixa = async () => {
        if (!baixaForm.equipamentoId) return alert("Selecione uma máquina!");
        
        try {
            // Chamada para a rota de saída avulsa
            await axios.post(`${ESTOQUE_API_URL}/saida-avulsa/${baixaForm.equipamentoId}`, {
                pecaId: selectedPeca.id,
                quantidade: baixaForm.quantidade
            });

            alert("Baixa realizada com sucesso!");
            setShowModal(false);
            setBaixaForm({ equipamentoId: '', quantidade: 1 });
            fetchData();
        } catch (error) {
            console.error("Erro 404/Erro de processamento:", error);
            alert("Erro: Certifique-se de que reiniciou o servidor Java.");
        }
    };

    if (loading) return <div className="main-content"><h1>Carregando...</h1></div>;

    return (
        <div className="main-content">
            <h1>Consulta de Estoque</h1>

            <div className="filter-container" style={{display: 'flex', gap: '20px', marginBottom: '20px', background: '#fff', padding: '15px', borderRadius: '8px'}}>
                <div style={{flex: 1}}>
                    <label>Área:</label>
                    <select value={selectedAreaId} onChange={(e) => {setSelectedAreaId(e.target.value); setSelectedModeloId('');}}>
                        <option value="">Todas</option>
                        {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                    </select>
                </div>
                <div style={{flex: 1}}>
                    <label>Modelo:</label>
                    <select value={selectedModeloId} onChange={(e) => setSelectedModeloId(e.target.value)} disabled={!selectedAreaId}>
                        <option value="">Todos</option>
                        {modelos.filter(m => m.areaId == selectedAreaId).map(m => <option key={m.id} value={m.id}>{m.nome}</option>)}
                    </select>
                </div>
            </div>

            <section className="list-section">
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Peça</th>
                                <th>Código</th>
                                <th>Estoque</th>
                                <th>Ação</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredPecas.map(peca => (
                                <tr key={peca.id}>
                                    <td>{peca.nome}</td>
                                    <td>{peca.codigoControle}</td>
                                    <td>{peca.estoqueAtual}</td>
                                    <td>
                                        <button onClick={() => {setSelectedPeca(peca); setShowModal(true);}} style={{background: '#f39c12', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer'}}>
                                            Consumir
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>

            {showModal && (
                <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000}}>
                    <div style={{background: '#fff', padding: '25px', borderRadius: '8px', width: '400px'}}>
                        <h3>Baixar Peça: {selectedPeca?.nome}</h3>
                        <div style={{marginTop: '15px'}}>
                            <label>Em qual máquina?</label>
                            <select style={{width: '100%', padding: '10px'}} value={baixaForm.equipamentoId} onChange={e => setBaixaForm({...baixaForm, equipamentoId: e.target.value})}>
                                <option value="">Selecione...</option>
                                {equipamentos.map(eq => <option key={eq.id} value={eq.id}>{eq.nome}</option>)}
                            </select>
                        </div>
                        <div style={{marginTop: '15px'}}>
                            <label>Quantidade:</label>
                            <input type="number" style={{width: '100%', padding: '10px'}} value={baixaForm.quantidade} onChange={e => setBaixaForm({...baixaForm, quantidade: parseInt(e.target.value) || 1})}/>
                        </div>
                        <div style={{marginTop: '25px', display: 'flex', justifyContent: 'flex-end', gap: '10px'}}>
                            <button onClick={() => setShowModal(false)}>Cancelar</button>
                            <button onClick={handleConfirmarBaixa} style={{background: '#27ae60', color: '#fff', border: 'none', padding: '8px 15px', borderRadius: '4px', cursor: 'pointer'}}>Confirmar Baixa</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Estoque;