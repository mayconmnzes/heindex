// Código Completo
import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

// Definição das URLs da API corrigidas com o prefixo /api
const OS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/ordens-servico`;
const USUARIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/usuarios`;
const AREAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/areas`;
const EQUIPAMENTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;


// --- Componente OsCard (Atualizado) ---
function OsCard({ os, navigate }) {
    const isCompleted = os.status === 'CONCLUIDA';
    const isSuggestion = os.status === 'SUGESTAO'; // Verifica se é sugestão

    // Define a cor da borda baseado no status
    let borderColor = '#ddd'; // Padrão
    if (os.status === 'PENDENTE_DE_CORRECAO') borderColor = '#dc3545'; // Vermelho
    else if (os.status === 'AGUARDANDO_VALIDACAO') borderColor = '#fd7e14'; // Laranja
    else if (os.status === 'SUGESTAO') borderColor = '#6f42c1'; // Lilás para sugestão
    else if (os.status === 'EM_EXECUCAO') borderColor = '#ffc107'; // Amarelo
    else if (os.status === 'AGENDADA') borderColor = '#007bff'; // Azul

    const cardStyle = {
        border: `1px solid ${borderColor}`,
        borderLeft: `5px solid ${borderColor}`, // Borda esquerda mais grossa
        // (Outros estilos se necessário)
    };

    const handleButtonClick = () => {
        // Ação Simples: Apenas navega para a página da OS para todos os status
        navigate(`/ordem-servico/${os.id}`);
    };

    // Define o texto do botão com base no status atual
    const buttonText = isCompleted ? 'Ver Histórico' :
                       isSuggestion ? 'Analisar e Agendar' : // Texto para sugestão
                       os.status === 'AGENDADA' ? 'INICIAR EXECUÇÃO' :
                       os.status === 'EM_EXECUCAO' ? 'CONTINUAR / FINALIZAR' :
                       os.status === 'AGUARDANDO_VALIDACAO' ? 'REVISAR E VALIDAR' :
                       'ABRIR PARA CORREÇÃO';

    // Define a cor do botão
    let buttonColor = '#007bff'; // Azul padrão
    if (isCompleted) buttonColor = '#6c757d'; // Cinza para concluída
    else if (isSuggestion) buttonColor = '#6f42c1'; // Lilás para sugestão

    return (
        <div className="os-card" style={cardStyle}>
            <h4>{os.nomeEquipamento} ({os.codigoEquipamento})</h4>
            <p><strong>Técnico:</strong> {os.nomeTecnico || <span style={{color: 'gray', fontStyle:'italic'}}>A definir</span>}</p> {/* Mostra "A definir" se não houver técnico */}
            <p><strong>{isSuggestion ? 'Sugerido p/:' : 'Agendado:'}</strong> {os.dataAgendamento ? new Date(os.dataAgendamento).toLocaleDateString('pt-BR') : 'N/A'}</p> {/* Ajusta o texto da data */}

            <div className="os-card-actions">
                <button
                    onClick={handleButtonClick}
                    style={{width: '100%', backgroundColor: buttonColor}}
                >
                    {buttonText}
                </button>
            </div>
        </div>
    );
}

// --- Componente StatusColumn ---
function StatusColumn({ title, ordens, navigate }) {
    return (
        <div className="status-column">
            <h3>{title} ({ordens.length})</h3>
            <div className="column-body">
                {ordens.length > 0 ? (
                    ordens.map(os => <OsCard key={os.id} os={os} navigate={navigate} />)
                ) : (
                    <p style={{textAlign: 'center', color: '#6c757d', marginTop: '20px'}}>Nenhuma OS neste status.</p>
                )}
            </div>
        </div>
    );
}

// --- Componente Principal do Dashboard (Atualizado) ---
function Dashboard() {
    const [ordensServico, setOrdensServico] = useState([]);
    const [loading, setLoading] = useState(true);
    const user = JSON.parse(localStorage.getItem('user'));
    const navigate = useNavigate();

    // Estados para os filtros
    const [tecnicos, setTecnicos] = useState([]);
    const [areas, setAreas] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [selectedTecnico, setSelectedTecnico] = useState('');
    const [selectedArea, setSelectedArea] = useState('');

    const fetchData = async () => {
        setLoading(true); // Garante que loading seja true no início
        try {
            const [osRes, tecnicosRes, areasRes, equipamentosRes] = await Promise.all([
                axios.get(OS_API_URL), // Busca OSs ordenadas pela data mais antiga
                axios.get(USUARIOS_API_URL),
                axios.get(AREAS_API_URL),
                axios.get(EQUIPAMENTOS_API_URL)
            ]);
            setOrdensServico(osRes.data);
            setTecnicos(tecnicosRes.data.filter(u => u.perfil === 'TECNICO' || u.perfil === 'ADMINISTRADOR'));
            setAreas(areasRes.data);
            setEquipamentos(equipamentosRes.data); // equipamentos contém nomeArea
        } catch (error) {
             console.error("Erro ao buscar dados para o Dashboard:", error);
             alert("Falha ao carregar dados do Dashboard. Verifique a conexão.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    // Lógica de filtragem
    const filteredOrdens = useMemo(() => {
        // Cria um mapa para busca rápida da área do equipamento
        const equipamentoAreaMap = new Map();
        equipamentos.forEach(equip => {
            // Assumindo que equipamentosRes.data contém objetos com id e nomeArea
            equipamentoAreaMap.set(equip.id, equip.nomeArea);
        });

        return ordensServico.filter(os => {
            // Filtro por Técnico (aplica apenas se um técnico for selecionado E a OS não for Sugestão OU se for Sugestão e já tiver técnico - caso raro)
            const matchTecnico = !selectedTecnico || (os.nomeTecnico === selectedTecnico);

            // Filtro por Área
            const areaDoEquipamento = os.equipamentoId ? equipamentoAreaMap.get(os.equipamentoId) : null;
            const matchArea = !selectedArea || (areaDoEquipamento === selectedArea);

             // A OS passa se atender aos dois filtros
            return matchTecnico && matchArea;
        });
    }, [selectedTecnico, selectedArea, ordensServico, equipamentos]);


    if (loading) { return <div className="main-content"><h1>Carregando Ordens de Serviço...</h1></div>; }

    // Separa as OSs FILTRADAS por status, incluindo SUGESTAO
    const sugestoes = filteredOrdens.filter(os => os.status === 'SUGESTAO');
    const agendadas = filteredOrdens.filter(os => os.status === 'AGENDADA');
    const emExecucao = filteredOrdens.filter(os => os.status === 'EM_EXECUCAO');
    const aguardandoValidacao = filteredOrdens.filter(os => os.status === 'AGUARDANDO_VALIDACAO');
    const pendentesCorrecao = filteredOrdens.filter(os => os.status === 'PENDENTE_DE_CORRECAO');
    // const concluidas = filteredOrdens.filter(os => os.status === 'CONCLUIDA'); // Se quiser mostrar

    return (
        <div className="main-content">
            <h1>Dashboard de Manutenção</h1>
            <p>Bem-vindo ao Heimdex, {user?.nomeCompleto}!</p>

            {/* Filtros */}
            <div className="filter-container" style={{padding: '15px', backgroundColor: '#fff', borderRadius: '8px', marginBottom: '20px'}}>
                 <div style={{flex: 1}}>
                    <label>Filtrar por Técnico:</label>
                    <select value={selectedTecnico} onChange={(e) => setSelectedTecnico(e.target.value)}>
                        <option value="">Todos os Técnicos</option>
                        {tecnicos.map(tec => ( <option key={tec.id} value={tec.nomeCompleto}>{tec.nomeCompleto}</option> ))}
                    </select>
                </div>
                <div style={{flex: 1}}>
                    <label>Filtrar por Área:</label>
                    <select value={selectedArea} onChange={(e) => setSelectedArea(e.target.value)}>
                        <option value="">Todas as Áreas</option>
                        {/* Usa a lista de 'areas' para as opções */}
                        {areas.map(area => ( <option key={area.id} value={area.nome}>{area.nome}</option> ))}
                    </select>
                </div>
            </div>

            {/* Quadro Kanban com a nova coluna */}
            <div className="dashboard-board">
                <StatusColumn title="Sugestões" ordens={sugestoes} navigate={navigate} />
                <StatusColumn title="Agendadas" ordens={agendadas} navigate={navigate} />
                <StatusColumn title="Em Execução" ordens={emExecucao} navigate={navigate} />
                <StatusColumn title="Aguardando Validação" ordens={aguardandoValidacao} navigate={navigate} />
                <StatusColumn title="Pendente de Correção" ordens={pendentesCorrecao} navigate={navigate} />
                {/* <StatusColumn title="Concluídas" ordens={concluidas} navigate={navigate} /> */}
            </div>
        </div>
    );
}

export default Dashboard;
