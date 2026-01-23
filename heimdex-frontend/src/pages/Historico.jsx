import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const AREAS_API_URL = `${API_BASE_URL}/areas`;
const LINHAS_API_URL = `${API_BASE_URL}/linhas`;
const EQUIPAMENTOS_API_URL = `${API_BASE_URL}/equipamentos`;

function Historico() {
    const [areas, setAreas] = useState([]);
    const [linhas, setLinhas] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [selectedAreaId, setSelectedAreaId] = useState('');
    const [selectedLinhaId, setSelectedLinhaId] = useState('');
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [areasRes, linhasRes, equipamentosRes] = await Promise.all([
                    axios.get(AREAS_API_URL),
                    axios.get(LINHAS_API_URL),
                    axios.get(EQUIPAMENTOS_API_URL)
                ]);
                setAreas(areasRes.data || []);
                setLinhas(linhasRes.data || []);
                setEquipamentos(equipamentosRes.data || []);
            } catch (err) {
                console.error("Falha ao carregar dados.");
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const linhasDisponiveis = useMemo(() => 
        linhas.filter(l => l.areaId?.toString() === selectedAreaId), 
    [selectedAreaId, linhas]);

    const equipamentosFiltrados = useMemo(() => {
        let filtrados = equipamentos;
        if (selectedAreaId) {
            const areaNome = areas.find(a => a.id?.toString() === selectedAreaId)?.nome;
            filtrados = filtrados.filter(e => e.nomeArea === areaNome);
        }
        if (selectedLinhaId) {
            const linhaNome = linhas.find(l => l.id?.toString() === selectedLinhaId)?.nome;
            filtrados = filtrados.filter(e => e.nomeLinha === linhaNome);
        }
        return filtrados;
    }, [selectedAreaId, selectedLinhaId, equipamentos, areas, linhas]);

    if (loading) return <div className="main-content">Carregando...</div>;

    return (
        <div className="main-content">
            <h1>Histórico de Equipamentos</h1>
            
            <div className="filter-container" style={{ padding: '15px', backgroundColor: '#fff', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                    <label>Área:</label>
                    <select value={selectedAreaId} onChange={e => { setSelectedAreaId(e.target.value); setSelectedLinhaId(''); }}>
                        <option value="">Todas</option>
                        {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                    </select>
                </div>
                <div style={{ flex: 1 }}>
                    <label>Linha:</label>
                    <select value={selectedLinhaId} onChange={e => setSelectedLinhaId(e.target.value)} disabled={!selectedAreaId}>
                        <option value="">Todas</option>
                        {linhasDisponiveis.map(l => <option key={l.id} value={l.id}>{l.nome}</option>)}
                    </select>
                </div>
            </div>

            <section className="list-section">
                <table>
                    <thead>
                        <tr>
                            <th>Nome (Tag)</th>
                            <th>Modelo</th>
                            <th>Linha</th>
                            <th>Área</th>
                            <th>Ação</th>
                        </tr>
                    </thead>
                    <tbody>
                        {equipamentosFiltrados.map(equip => (
                            <tr key={equip.id}>
                                <td>{equip.nome}</td>
                                <td>{equip.nomeModelo || 'N/A'}</td>
                                <td>{equip.nomeLinha || 'N/A'}</td>
                                <td>{equip.nomeArea || 'N/A'}</td>
                                <td>
                                    <button onClick={() => navigate(`/historico/${equip.id}`)}>Ver Detalhes</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </section>
        </div>
    );
}

export default Historico;