import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { useNavigate, useLocation } from 'react-router-dom';
import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, Title } from 'chart.js';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

ChartJS.register(ArcElement, Tooltip, Legend, Title);

const EQUIPAMENTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/equipamentos`;
const USUARIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/usuarios`;
const OS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/ordens-servico`;

function calculateDueDate(lastDateStr, frequency) {
    if (!lastDateStr || !frequency) return null;
    const date = new Date(lastDateStr);
    date.setMinutes(date.getMinutes() + date.getTimezoneOffset());

    switch (frequency) {
        case 'QUINZENAL': date.setDate(date.getDate() + 15); break;
        case 'MENSAL': date.setMonth(date.getMonth() + 1); break;
        case 'TRIMESTRAL': date.setMonth(date.getMonth() + 3); break;
        case 'SEMESTRAL': date.setMonth(date.getMonth() + 6); break;
        case 'ANUAL': date.setFullYear(date.getFullYear() + 1); break;
        default: return null;
    }
    return date;
}

function EquipamentoCard({ equipamento, onPlanejar }) {
    const getStatusInfo = (equip) => {
        const { statusPreventiva, dataUltimaPreventiva, frequenciaPreventiva } = equip;
        const today = new Date();
        today.setHours(0, 0, 0, 0); 
        
        const dueDate = calculateDueDate(dataUltimaPreventiva, frequenciaPreventiva);
        if (dueDate) dueDate.setHours(0, 0, 0, 0);

        let message = '';
        switch (statusPreventiva) {
            case 'ATRASADA':
                let daysLate = 0;
                if (dueDate) {
                    const diffTime = today - dueDate;
                    daysLate = Math.floor(diffTime / (1000 * 60 * 60 * 24));
                }
                message = `PREVENTIVA ATRASADA! (${daysLate > 0 ? daysLate : 0} dias)`;
                return { borderColor: '#dc3545', backgroundColor: '#f8d7da', textColor: '#721c24', message };
            case 'ATENCAO':
                let daysRemaining = 0;
                if (dueDate) {
                    const diffTime = dueDate - today;
                    daysRemaining = Math.floor(diffTime / (1000 * 60 * 60 * 24));
                }
                message = `Preventiva próxima! (${daysRemaining >= 0 ? daysRemaining : 0} dias restantes)`;
                return { borderColor: '#ffc107', backgroundColor: '#fff3cd', textColor: '#856404', message };
            case 'AGENDADA':
                return { borderColor: '#007bff', backgroundColor: '#cce5ff', textColor: '#004085', message: 'Preventiva já planejada.' };
            default:
                return { borderColor: '#28a745', backgroundColor: '#d4edda', textColor: '#155724', message: 'Preventiva em Dia' };
        }
    };

    const statusInfo = getStatusInfo(equipamento);
    const cardStyle = {
        border: '1px solid',
        borderColor: statusInfo.borderColor,
        backgroundColor: statusInfo.backgroundColor,
        padding: '10px',
        borderRadius: '6px',
        marginBottom: '10px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    };

    // --- LÓGICA ALTERADA AQUI ---
    // Agora permite planejar se estiver ATRASADA, ATENCAO ou OK (em dia).
    // Bloqueia apenas se já estiver AGENDADA.
    const podePlanejar = equipamento.statusPreventiva !== 'AGENDADA';

    return (
        <div style={cardStyle}> 
            <h5 style={{ margin: 0, color: statusInfo.textColor }}>{equipamento.nome} ({equipamento.codigo})</h5>
            <p style={{ margin: '5px 0', fontWeight: 'bold', color: statusInfo.textColor }}>{statusInfo.message}</p>
            <p style={{ margin: '5px 0', fontSize: '0.8rem' }}>
                Última Preventiva: {equipamento.dataUltimaPreventiva ? new Date(equipamento.dataUltimaPreventiva).toLocaleDateString('pt-BR') : 'N/A'}
            </p>
            {podePlanejar && (
                <button 
                    onClick={() => onPlanejar(equipamento)} 
                    style={{ 
                        width: '100%', 
                        backgroundColor: statusInfo.borderColor, 
                        color: 'white', 
                        border: 'none',
                        cursor: 'pointer',
                        padding: '5px',
                        borderRadius: '4px'
                    }}
                >
                    {equipamento.statusPreventiva === 'OK' ? 'Antecipar Preventiva' : 'Planejar Preventiva'}
                </button>
            )}
        </div>
    );
}

function PlanejamentoModal({ equipamento, tecnicos, onClose, onSave }) {
    const [tecnicoId, setTecnicoId] = useState('');
    const [dataAgendamento, setDataAgendamento] = useState('');
    
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!tecnicoId || !dataAgendamento) return alert("Técnico e Data são obrigatórios.");
        onSave({
            equipamentoId: equipamento.id,
            tecnicoId: Number(tecnicoId), 
            checklistId: equipamento.checklistId ? Number(equipamento.checklistId) : null,
            dataAgendamento,
            tipoManutencao: 'PREVENTIVA'
        });
    };

    return (
        <div className="timeline-modal-overlay">
            <div className="timeline-modal-content" style={{ width: '500px' }}>
                <h3>Planejar OS para: {equipamento.nome}</h3>
                <form onSubmit={handleSubmit}>
                    <label>Técnico Responsável:</label>
                    <select value={tecnicoId} onChange={(e) => setTecnicoId(e.target.value)} required>
                        <option value="">-- Selecione um Técnico --</option>
                        {tecnicos.map(tec => <option key={tec.id} value={tec.id}>{tec.nomeCompleto}</option>)}
                    </select>
                    <label>Checklist:</label>
                    <input value={equipamento.checklistNome || 'Nenhum checklist padrão'} disabled />
                    <label>Data de Agendamento:</label>
                    <input type="datetime-local" value={dataAgendamento} onChange={(e) => setDataAgendamento(e.target.value)} required />
                    <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                        <button type="submit">Agendar Manutenção</button>
                        <button type="button" onClick={onClose} style={{ backgroundColor: '#6c757d' }}>Cancelar</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function Planejamento() {
    const [loading, setLoading] = useState(true);
    const [equipamentos, setEquipamentos] = useState([]);
    const [tecnicos, setTecnicos] = useState([]);
    const [equipamentoParaPlanejar, setEquipamentoParaPlanejar] = useState(null);
    const location = useLocation();

    const [selectedArea, setSelectedArea] = useState('');
    const [selectedLinha, setSelectedLinha] = useState('');
    const [selectedStatus, setSelectedStatus] = useState('');
    const [selectedMonth, setSelectedMonth] = useState('');
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());

    const [uniqueAreas, setUniqueAreas] = useState([]);
    const [uniqueLinhas, setUniqueLinhas] = useState([]);

    const handleClearFilters = () => {
        setSelectedArea('');
        setSelectedLinha('');
        setSelectedStatus('');
        setSelectedMonth('');
        setSelectedYear(new Date().getFullYear());
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const [equipRes, tecRes] = await Promise.all([
                axios.get(EQUIPAMENTOS_API_URL),
                axios.get(USUARIOS_API_URL)
            ]);
            const allEquips = equipRes.data.filter(e => e.statusPreventiva !== 'NAO_CONFIGURADA');
            setEquipamentos(allEquips);
            setUniqueAreas([...new Set(allEquips.map(e => e.nomeArea || 'Sem Área'))].sort());
            setTecnicos(tecRes.data.filter(u => u.perfil === 'TECNICO' || u.perfil === 'ADMINISTRADOR'));
        } catch (error) { console.error(error); } finally { setLoading(false); }
    };

    useEffect(() => { fetchData(); }, [location]);

    useEffect(() => {
        if (selectedArea) {
            setUniqueLinhas([...new Set(equipamentos.filter(e => (e.nomeArea || 'Sem Área') === selectedArea).map(e => e.nomeLinha || 'Sem Linha'))].sort());
        } else { setUniqueLinhas([]); }
        setSelectedLinha('');
    }, [selectedArea, equipamentos]);

    const filteredEquipamentos = useMemo(() => {
        return equipamentos.filter(equip => {
            const matchArea = selectedArea ? (equip.nomeArea || 'Sem Área') === selectedArea : true;
            const matchLinha = selectedLinha ? (equip.nomeLinha || 'Sem Linha') === selectedLinha : true;
            const matchStatus = selectedStatus ? equip.statusPreventiva === selectedStatus : true;
            
            const dueDate = calculateDueDate(equip.dataUltimaPreventiva, equip.frequenciaPreventiva);
            let matchDate = true;
            if (selectedMonth !== '') {
                matchDate = dueDate && 
                            dueDate.getMonth() === Number(selectedMonth) && 
                            dueDate.getFullYear() === Number(selectedYear);
            } else {
                matchDate = dueDate ? dueDate.getFullYear() === Number(selectedYear) : true;
            }
            return matchArea && matchLinha && matchStatus && matchDate;
        });
    }, [equipamentos, selectedArea, selectedLinha, selectedStatus, selectedMonth, selectedYear]);

    const handleExportPDF = () => {
        const doc = new jsPDF();
        const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
        const mesExtenso = selectedMonth !== '' ? meses[selectedMonth] : 'Todos os Meses';

        doc.setFontSize(18);
        doc.text(`Planejamento de Manutenção - ${mesExtenso} / ${selectedYear}`, 14, 20);
        
        const tableColumn = ["Equipamento", "Tag/Patrimônio", "Área", "Linha", "Status", "Próxima Preventiva"];
        const tableRows = [];

        filteredEquipamentos.forEach(equip => {
            const dueDate = calculateDueDate(equip.dataUltimaPreventiva, equip.frequenciaPreventiva);
            const rowData = [
                equip.nome,
                equip.codigo,
                equip.nomeArea || 'N/A',
                equip.nomeLinha || 'N/A',
                equip.statusPreventiva,
                dueDate ? dueDate.toLocaleDateString('pt-BR') : 'N/A'
            ];
            tableRows.push(rowData);
        });

        autoTable(doc, {
            head: [tableColumn],
            body: tableRows,
            startY: 30,
            theme: 'striped',
            headStyles: { fillColor: [0, 123, 255] }
        });

        doc.save(`planejamento_${mesExtenso.toLowerCase()}_${selectedYear}.pdf`);
    };

    const chartData = useMemo(() => {
        const counts = { OK: 0, ATENCAO: 0, ATRASADA: 0, AGENDADA: 0 };
        filteredEquipamentos.forEach(e => { if (counts[e.statusPreventiva] !== undefined) counts[e.statusPreventiva]++; });
        return {
            labels: ['OK', 'Atenção', 'Atrasada', 'Planejada'],
            datasets: [{
                data: [counts.OK, counts.ATENCAO, counts.ATRASADA, counts.AGENDADA],
                backgroundColor: ['#d4edda', '#fff3cd', '#f8d7da', '#cce5ff'],
                borderColor: ['#28a745', '#ffc107', '#dc3545', '#007bff'],
                borderWidth: 1,
            }],
        };
    }, [filteredEquipamentos]);

    const estruturaAgrupada = useMemo(() => {
        const grouped = {};
        filteredEquipamentos.forEach(equip => {
            const area = equip.nomeArea || "Sem Área";
            const linha = equip.nomeLinha || "Sem Linha";
            if (!grouped[area]) grouped[area] = {};
            if (!grouped[area][linha]) grouped[area][linha] = [];
            grouped[area][linha].push(equip);
        });
        return grouped;
    }, [filteredEquipamentos]);

    const handleSaveOs = async (novaOs) => {
        try {
            await axios.post(OS_API_URL, novaOs);
            alert('Ordem de Serviço criada com sucesso!');
            setEquipamentoParaPlanejar(null);
            fetchData();
        } catch (error) { alert('Falha ao criar a Ordem de Serviço.'); }
    };

    if (loading) return <div className="main-content"><h1>Carregando...</h1></div>;

    return (
        <div className="main-content">
            <h1>Planejamento de Preventivas</h1>
            
            <section className="list-section" style={{maxWidth: '350px', margin: '0 auto 20px'}}>
                <Pie data={chartData} options={{ responsive: true }} />
            </section>

            <div className="filter-container" style={{padding: '15px', backgroundColor: '#fff', borderRadius: '8px', marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'flex-end'}}>
                <div style={{flex: '1 1 150px'}}>
                    <label>Mês:</label>
                    <select value={selectedMonth} onChange={(e) => setSelectedMonth(e.target.value)}>
                        <option value="">Todos os Meses</option>
                        {['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'].map((m, i) => <option key={i} value={i}>{m}</option>)}
                    </select>
                </div>
                <div style={{flex: '1 1 100px'}}>
                    <label>Ano:</label>
                    <input type="number" value={selectedYear} onChange={(e) => setSelectedYear(e.target.value)} />
                </div>
                <div style={{flex: '1 1 150px'}}>
                    <label>Área:</label>
                    <select value={selectedArea} onChange={(e) => setSelectedArea(e.target.value)}>
                        <option value="">Todas</option>
                        {uniqueAreas.map(a => <option key={a} value={a}>{a}</option>)}
                    </select>
                </div>
                <div style={{flex: '1 1 150px'}}>
                    <label>Status:</label>
                    <select value={selectedStatus} onChange={(e) => setSelectedStatus(e.target.value)}>
                        <option value="">Todos</option>
                        <option value="OK">OK</option>
                        <option value="ATENCAO">Atenção</option>
                        <option value="ATRASADA">Atrasada</option>
                        <option value="AGENDADA">Planejada</option>
                    </select>
                </div>
                <div style={{display: 'flex', gap: '10px'}}>
                    <button onClick={handleExportPDF} style={{backgroundColor: '#28a745', color: 'white', height: '38px', marginTop: '0'}}>Exportar PDF</button>
                    <button onClick={handleClearFilters} style={{backgroundColor: '#dc3545', color: 'white', height: '38px', marginTop: '0'}}>Limpar Filtros</button>
                </div>
            </div>

            <div className="dashboard-board">
                {Object.keys(estruturaAgrupada).sort().map(area => (
                    <div key={area} className="status-column">
                        <h3>{area}</h3>
                        <div className="column-body">
                            {Object.keys(estruturaAgrupada[area]).sort().map(linha => (
                                <div key={linha} style={{marginBottom: '15px'}}>
                                    <h4 style={{backgroundColor: '#eee', padding: '5px'}}>{linha}</h4>
                                    {estruturaAgrupada[area][linha].map(equip => (
                                        <EquipamentoCard key={equip.id} equipamento={equip} onPlanejar={setEquipamentoParaPlanejar} />
                                    ))}
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {equipamentoParaPlanejar && (
                <PlanejamentoModal 
                    equipamento={equipamentoParaPlanejar} 
                    tecnicos={tecnicos} 
                    onClose={() => setEquipamentoParaPlanejar(null)} 
                    onSave={handleSaveOs} 
                />
            )}
        </div>
    );
}

export default Planejamento;