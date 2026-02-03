import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const RELATORIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/relatorios`;
const EQUIPAMENTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;
const AREAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/areas`;
const LINHAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/linhas`;
const MODELOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/modelos`;
const ESTOQUE_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/estoque`;

// BACKEND_BASE_URL corrigida para apontar para a raiz do Render sem manipulações
const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function Relatorios() {
    const [pecasBaixas, setPecasBaixas] = useState([]);
    const [loadingEstoque, setLoadingEstoque] = useState(false);
    const [curvaAbcData, setCurvaAbcData] = useState([]);
    const [loadingAbc, setLoadingAbc] = useState(false);
    const [consumoPorEquipamento, setConsumoPorEquipamento] = useState([]);
    const [movimentacoesAvulsas, setMovimentacoesAvulsas] = useState([]);
    const [loadingConsumo, setLoadingConsumo] = useState(false);

    const [kpiMttr, setKpiMttr] = useState(null);
    const [loadingMttr, setLoadingMttr] = useState(true);
    const [kpiOsAbertas, setKpiOsAbertas] = useState(null);
    const [loadingOsAbertas, setLoadingOsAbertas] = useState(true);

    const [equipamentos, setEquipamentos] = useState([]);
    const [areas, setAreas] = useState([]);
    const [linhas, setLinhas] = useState([]);
    const [modelos, setModelos] = useState([]);

    const [selectedAreaIdConsumo, setSelectedAreaIdConsumo] = useState('');
    const [selectedLinhaIdConsumo, setSelectedLinhaIdConsumo] = useState('');
    const [selectedEquipamentoIdConsumo, setSelectedEquipamentoIdConsumo] = useState('');

    const fetchAllData = async () => {
        setLoadingConsumo(true);
        try {
            const [
                _pecasBaixasRes, _curvaAbcRes, _consumoRes,
                kpiMttrRes, kpiOsAbertasRes,
                equipamentosRes, areasRes, linhasRes, modelosRes
            ] = await Promise.all([
                axios.get(`${RELATORIOS_API_URL}/estoque-baixo`),
                axios.get(`${RELATORIOS_API_URL}/curva-abc`),
                axios.get(`${RELATORIOS_API_URL}/consumo-por-equipamento`),
                axios.get(`${RELATORIOS_API_URL}/kpi/mttr`),
                axios.get(`${RELATORIOS_API_URL}/kpi/os-abertas-mes`),
                axios.get(EQUIPAMENTOS_API_URL),
                axios.get(AREAS_API_URL),
                axios.get(LINHAS_API_URL),
                axios.get(MODELOS_API_URL)
            ]);

            setPecasBaixas(_pecasBaixasRes.data);
            setCurvaAbcData(_curvaAbcRes.data);
            setConsumoPorEquipamento(_consumoRes.data);
            setKpiMttr(kpiMttrRes.data);
            setKpiOsAbertas(kpiOsAbertasRes.data);
            setEquipamentos(equipamentosRes.data);
            setAreas(areasRes.data);
            setLinhas(linhasRes.data);
            setModelos(modelosRes.data);

            const avulsasRes = await axios.get(`${ESTOQUE_API_URL}/historico-equipamento?nome=`);
            setMovimentacoesAvulsas(avulsasRes.data.filter(m => m.tipoMovimentacao === 'SAIDA_AVULSA'));

            setLoadingEstoque(false); setLoadingAbc(false); setLoadingConsumo(false);
            setLoadingMttr(false); setLoadingOsAbertas(false);
        } catch (error) {
            console.error("Erro geral:", error);
            setLoadingConsumo(false);
        }
    };

    useEffect(() => { fetchAllData(); }, []);

    const getAbcStyle = (classe) => {
        switch (classe) {
            case 'A': return { backgroundColor: '#28a745', color: 'white', fontWeight: 'bold' };
            case 'B': return { backgroundColor: '#ffc107', color: '#333', fontWeight: 'bold' };
            case 'C': return { backgroundColor: '#17a2b8', color: 'white', fontWeight: 'bold' };
            default: return { backgroundColor: '#6c757d', color: 'white' };
        }
    };

    // --- LÓGICA DO GRÁFICO UNIFICADO E FILTRADO ---
    const chartConsumoData = useMemo(() => {
        const consolidadoOS = {};
        const consolidadoAvulso = {};

        // 1. Filtrar equipamentos que devem aparecer
        const equipamentosValidos = equipamentos.filter(eq => {
            const linhaRel = linhas.find(l => l.nome === eq.nomeLinha && l.areaNome === eq.nomeArea);
            const matchArea = !selectedAreaIdConsumo || (linhaRel?.areaId === Number(selectedAreaIdConsumo));
            const matchLinha = !selectedLinhaIdConsumo || (linhaRel?.id === Number(selectedLinhaIdConsumo));
            const matchEquip = !selectedEquipamentoIdConsumo || (eq.id === Number(selectedEquipamentoIdConsumo));
            return matchArea && matchLinha && matchEquip;
        });

        // 2. Consolidar somas para os equipamentos filtrados
        equipamentosValidos.forEach(eq => {
            const nomeEq = eq.nome;
            
            // Soma via OS
            const dadosOS = consumoPorEquipamento.find(c => c.nomeEquipamento === nomeEq);
            consolidadoOS[nomeEq] = dadosOS ? dadosOS.pecasConsumidas.reduce((acc, p) => acc + p.totalConsumido, 0) : 0;

            // Soma Avulso
            consolidadoAvulso[nomeEq] = movimentacoesAvulsas.reduce((acc, mov) => {
                const match = mov.observacao?.match(/equipamento:\s*(.*)/i);
                return (match && match[1].trim() === nomeEq) ? acc + mov.quantidade : acc;
            }, 0);
        });

        // 3. Ordenar pelos 10 maiores (Soma de OS + Avulso)
        const sortedTags = equipamentosValidos
            .map(eq => eq.nome)
            .filter(nome => (consolidadoOS[nome] + consolidadoAvulso[nome]) > 0)
            .sort((a, b) => (consolidadoOS[b] + consolidadoAvulso[b]) - (consolidadoOS[a] + consolidadoAvulso[a]))
            .slice(0, 10);

        return {
            labels: sortedTags,
            datasets: [
                {
                    label: 'Consumo em OS (Preventiva/Corretiva)',
                    data: sortedTags.map(tag => consolidadoOS[tag]),
                    backgroundColor: 'rgba(54, 162, 235, 0.8)', // Azul
                },
                {
                    label: 'Consumo Avulso (Emergencial)',
                    data: sortedTags.map(tag => consolidadoAvulso[tag]),
                    backgroundColor: 'rgba(255, 159, 64, 0.8)', // Laranja
                }
            ]
        };
    }, [equipamentos, consumoPorEquipamento, movimentacoesAvulsas, selectedAreaIdConsumo, selectedLinhaIdConsumo, selectedEquipamentoIdConsumo, linhas]);

    const enrichedPecasBaixas = useMemo(() => {
        if (pecasBaixas.length === 0 || modelos.length === 0 || areas.length === 0) return pecasBaixas;
        const modelosMap = new Map(modelos.map(m => [m.id, m]));
        const areasMap = new Map(areas.map(a => [a.id, a]));
        return pecasBaixas.map(peca => {
            const modelo = modelosMap.get(peca.modeloEquipamentoId);
            const area = areasMap.get(modelo?.areaId);
            return { ...peca, nomeArea: area ? area.nome : 'N/A' };
        });
    }, [pecasBaixas, modelos, areas]);

    const filteredTabelaConsumo = useMemo(() => {
        return consumoPorEquipamento.filter(item => {
            const eq = equipamentos.find(e => e.id === item.equipamentoId);
            if (!eq) return false;
            const linhaRel = linhas.find(l => l.nome === eq.nomeLinha && l.areaNome === eq.nomeArea);
            const matchArea = !selectedAreaIdConsumo || (linhaRel?.areaId === Number(selectedAreaIdConsumo));
            const matchEquip = !selectedEquipamentoIdConsumo || (item.equipamentoId === Number(selectedEquipamentoIdConsumo));
            return matchArea && matchEquip;
        });
    }, [consumoPorEquipamento, equipamentos, selectedAreaIdConsumo, selectedEquipamentoIdConsumo, linhas]);

    return (
        <div className="main-content">
            <h1>Relatórios e KPIs</h1>
            <button onClick={fetchAllData} style={{ position: 'absolute', top: '20px', right: '20px', backgroundColor: '#17a2b8' }}>Atualizar Tudo</button>

            {/* KPIs */}
            <section className="container" style={{ gap: '10px', marginBottom: '30px', display: 'flex' }}>
                <div className="form-section" style={{ flex: 1, textAlign: 'center' }}>
                    <h3>MTTR (Reparo Médio)</h3>
                    <p style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>{kpiMttr?.toFixed(1)}h</p>
                    <span style={{ ...getAbcStyle(kpiMttr > 2 ? 'C' : 'A'), padding: '5px 10px', borderRadius: '4px' }}>Meta: 2h</span>
                </div>
                <div className="form-section" style={{ flex: 1, textAlign: 'center' }}>
                    <h3>OS Abertas (Mês)</h3>
                    <p style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>{kpiOsAbertas}</p>
                    <span style={{ ...getAbcStyle(kpiOsAbertas > 40 ? 'B' : 'A'), padding: '5px 10px', borderRadius: '4px' }}>Meta: 40</span>
                </div>
            </section>

            {/* GRÁFICO COM FILTRO E LEGENDA DUPLA */}
            <section className="list-section" style={{ marginBottom: '30px', padding: '20px', background: '#fff' }}>
                <h2>Consumo por Máquina (Filtro Aplicado)</h2>
                
                <div className="filter-container" style={{padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '1rem'}}>
                    <div style={{flex: 1}}>
                        <label>Área:</label>
                        <select value={selectedAreaIdConsumo} onChange={e => { setSelectedAreaIdConsumo(e.target.value); setSelectedLinhaIdConsumo(''); }}>
                            <option value="">Todas</option>
                            {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                        </select>
                    </div>
                    <div style={{flex: 1}}>
                        <label>Linha:</label>
                        <select value={selectedLinhaIdConsumo} onChange={e => setSelectedLinhaIdConsumo(e.target.value)} disabled={!selectedAreaIdConsumo}>
                            <option value="">Todas</option>
                            {linhas.filter(l => l.areaId === Number(selectedAreaIdConsumo)).map(l => <option key={l.id} value={l.id}>{l.nome}</option>)}
                        </select>
                    </div>
                </div>

                <div style={{ height: '350px' }}>
                    <Bar 
                        data={chartConsumoData} 
                        options={{ 
                            responsive: true, maintainAspectRatio: false,
                            scales: { x: { stacked: true }, y: { stacked: true } }, // Barras empilhadas para ver o total
                            plugins: { 
                                legend: { position: 'top' },
                                title: { display: true, text: 'Consumo Detalhado por Equipamento' }
                            }
                        }} 
                    />
                </div>
            </section>

            {/* CURVA ABC */}
            <section className="list-section" style={{ marginBottom: '30px' }}>
                <h2>Curva ABC</h2>
                <div className="table-container">
                    <table>
                        <thead>
                            <tr><th>Classe</th><th>Peça</th><th>Total</th><th>% Acumulada</th></tr>
                        </thead>
                        <tbody>
                            {curvaAbcData.map(item => (
                                <tr key={item.pecaId}>
                                    <td><span style={{ ...getAbcStyle(item.classificacao), padding: '4px 12px', borderRadius: '20px', fontSize: '0.8rem' }}>Classe {item.classificacao}</span></td>
                                    <td>{item.nomePeca}</td>
                                    <td>{item.totalConsumido}</td>
                                    <td>{item.percentualAcumulado.toFixed(1)}%</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* COMPRAS */}
            <section className="list-section">
                <h2>Relatório de Compras (Estoque Crítico)</h2>
                <div className="table-container">
                    <table>
                        <thead>
                            <tr><th>Peça</th><th>Área</th><th>Atual</th><th>Mínimo</th><th>Solicitar</th></tr>
                        </thead>
                        <tbody>
                            {enrichedPecasBaixas.map(peca => (
                                <tr key={peca.id}>
                                    <td>{peca.nome}</td>
                                    <td>{peca.nomeArea}</td>
                                    <td>{peca.estoqueAtual}</td>
                                    <td>{peca.estoqueMinimo}</td>
                                    <td style={{ fontWeight: 'bold', color: 'red' }}>{Math.max(0, peca.estoqueMinimo - peca.estoqueAtual)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
}

export default Relatorios;
