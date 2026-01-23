// Código Novo - src/pages/DetalheHistorico.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
// --- ALTERAÇÃO 1: Importa useNavigate ---
import { useParams, Link, useNavigate } from 'react-router-dom';

// URLs da API (Corrigidas com a sintaxe de Template String)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const EQUIPAMENTOS_API_URL = `${API_BASE_URL}/equipamentos`;

// BACKEND_BASE_URL para imagens, se mostrar fotos da OS
// (Corrigido para usar a variável de ambiente)
const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL.replace('/api', '');

// Função auxiliar para formatar datas
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'N/A';
    try {
        return new Date(dateTimeString).toLocaleString('pt-BR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch (e) {
        return 'Data inválida';
    }
};

// Função auxiliar para obter classe CSS do status da OS
const getStatusClass = (status) => {
    if (!status) return '';
    return `status-${status.toLowerCase().replace(/_/g, '-')}`;
};


function DetalheHistorico() {
    // Pega o ID do equipamento da URL
    const { equipamentoId } = useParams();
    
    // --- ALTERAÇÃO 2: Inicializa o navigate ---
    const navigate = useNavigate();

    // Estados
    const [equipamentoInfo, setEquipamentoInfo] = useState(null); // Para mostrar nome, etc.
    const [historicoOs, setHistoricoOs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Busca os dados do histórico e as infos do equipamento
    useEffect(() => {
        if (!equipamentoId) {
            setError("ID do equipamento não encontrado na URL.");
            setLoading(false);
            return;
        }

        const fetchHistorico = async () => {
            setLoading(true);
            setError(null);
            try {
                // Busca o histórico de OSs
                const historicoRes = await axios.get(`${EQUIPAMENTOS_API_URL}/${equipamentoId}/historico`);
                // Garante que a ordem seja da mais recente para a mais antiga
                const sortedHistorico = (Array.isArray(historicoRes.data) ? historicoRes.data : [])
                                        .sort((a, b) => new Date(b.dataFimExecucao || b.dataAgendamento) - new Date(a.dataFimExecucao || a.dataAgendamento));
                setHistoricoOs(sortedHistorico);

                // Opcional: Busca as informações do equipamento para o cabeçalho
                try {
                     const equipRes = await axios.get(`${EQUIPAMENTOS_API_URL}/${equipamentoId}`);
                     setEquipamentoInfo(equipRes.data);
                } catch (equipError) {
                    console.warn("Não foi possível buscar detalhes do equipamento:", equipError);
                    // Continua mesmo sem os detalhes, usará dados da primeira OS se houver
                    if(historicoRes.data && historicoRes.data.length > 0) {
                        setEquipamentoInfo({
                            nome: historicoRes.data[0].nomeEquipamento,
                            codigo: historicoRes.data[0].codigoEquipamento,
                        })
                    }
                }

            } catch (err) {
                console.error("Erro ao buscar histórico do equipamento:", err);
                setError(`Falha ao carregar histórico: ${err.response?.data?.message || err.message || 'Erro desconhecido'}`);
                setHistoricoOs([]);
                setEquipamentoInfo(null);
            } finally {
                setLoading(false);
            }
        };

        fetchHistorico();
    }, [equipamentoId]); // Re-executa se o ID mudar

    
    // --- ALTERAÇÃO 3: Função para renderizar o Card Clicável ---
    const renderOsCard = (os) => {
        const statusClass = getStatusClass(os.status);
        let borderColor = '#ddd';
        if (os.status === 'CONCLUIDA') borderColor = '#6c757d'; // Cinza
        else if (os.status === 'PENDENTE_DE_CORRECAO') borderColor = '#dc3545'; // Vermelho
        else if (os.status === 'AGUARDANDO_VALIDACAO') borderColor = '#fd7e14'; // Laranja
        else if (os.status === 'EM_EXECUCAO') borderColor = '#ffc107'; // Amarelo
        else if (os.status === 'AGENDADA') borderColor = '#007bff'; // Azul

        const cardStyle = {
            border: `1px solid ${borderColor}`,
            borderLeft: `5px solid ${borderColor}`,
            padding: '12px',
            borderRadius: '6px',
            marginBottom: '10px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
            cursor: 'pointer', // Faz parecer clicável
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
        };

        return (
            <div 
                key={os.id} 
                className="os-card-historico" // Use uma classe se quiser estilizar mais no CSS
                style={cardStyle} 
                onClick={() => navigate(`/ordem-servico/${os.id}`)} // Ação de clique
                title="Clique para ver os detalhes completos"
            >
                <div>
                    <h4 style={{ margin: 0 }}>
                        {/* Mostra o tipo (Preventiva, Corretiva...) */}
                        OS #{os.id} - {os.tipoManutencao || 'Manutenção'}
                    </h4>
                    <p style={{ fontSize: '0.9rem', color: '#333', margin: '4px 0' }}>
                        <strong>Técnico:</strong> {os.nomeTecnico || 'N/A'}
                    </p>
                    <p style={{ fontSize: '0.85rem', color: '#666', margin: '4px 0' }}>
                        <strong>Data de Conclusão:</strong> {formatDateTime(os.dataFimExecucao)}
                    </p>
                </div>
                <span className={`status ${statusClass}`}>
                    {os.status ? os.status.replace(/_/g, ' ') : 'N/A'}
                </span>
            </div>
        );
    };


    return (
        <div className="main-content">
            {/* Link para voltar */}
            <Link to="/historico" style={{ display: 'inline-block', marginBottom: '1rem' }}>
                &larr; Voltar para a lista
            </Link>

            {/* Cabeçalho com informações do equipamento */}
            {equipamentoInfo && (
                <div style={{ padding: '15px', backgroundColor: '#e9ecef', borderRadius: '8px', marginBottom: '20px' }}>
                    <h2>Histórico de Manutenção</h2>
                    <h3 style={{ marginTop: '5px' }}>
                        Equipamento: {equipamentoInfo.nome || 'Carregando...'}
                        {equipamentoInfo.codigo && ` (${equipamentoInfo.codigo})`}
                    </h3>
                     {/* Adicione mais detalhes se buscar o EquipamentoResponseDTO completo */}
                     {equipamentoInfo.nomeModelo && <p style={{ margin: '2px 0', fontSize: '0.9rem' }}><strong>Modelo:</strong> {equipamentoInfo.nomeModelo} (Fabr: {equipamentoInfo.fabricante || 'N/A'})</p> }
                     {equipamentoInfo.nomeLinha && <p style={{ margin: '2px 0', fontSize: '0.9rem' }}><strong>Local:</strong> {equipamentoInfo.nomeArea || 'N/A'} / {equipamentoInfo.nomeLinha}</p>}
                </div>
            )}
             {!equipamentoInfo && !loading && !error && <h2>Histórico de Manutenção</h2>}


            {loading && <p>Carregando histórico...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}

            {/* --- ALTERAÇÃO 4: A lista agora usa a nova função de renderização --- */}
            {!loading && !error && (
                <div>
                    {historicoOs.length === 0 ? (
                        <p>Nenhum registro de Ordem de Serviço encontrado para este equipamento.</p>
                    ) : (
                        // Mapeia e renderiza os cards clicáveis
                        historicoOs.map(os => renderOsCard(os))
                    )}
                </div>
            )}
        </div>
    );
}

export default DetalheHistorico;