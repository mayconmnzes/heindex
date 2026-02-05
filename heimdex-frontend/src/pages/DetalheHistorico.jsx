import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const EQUIPAMENTOS_API = `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;
const OS_API = `${import.meta.env.VITE_API_BASE_URL}/api/ordens-servico`;
const ESTOQUE_API = `${import.meta.env.VITE_API_BASE_URL}/api/estoque`;

function formatDateTime(d) {
    if (!d) return '—';
    try {
        const dt = new Date(d);
        return dt.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    } catch {
        return String(d);
    }
}

function DetalheHistorico({ equipamentoId }) {
    const navigate = useNavigate();
    const [equipamentoInfo, setEquipamentoInfo] = useState(null);
    const [historicoOs, setHistoricoOs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // --- NOVO: consumos avulsos (movimentacoes de estoque vinculadas ao equipamento via observacao) ---
    const [consumosAvulsos, setConsumosAvulsos] = useState([]);

    useEffect(() => {
        const fetchDados = async () => {
            setLoading(true);
            setError(null);
            try {
                // equipamento info
                const equipRes = await axios.get(`${EQUIPAMENTOS_API}/${equipamentoId}`);
                const equipamento = equipRes.data;
                setEquipamentoInfo(equipamento);

                // historico de OS para o equipamento
                const histOsRes = await axios.get(`${OS_API}?equipamentoId=${equipamentoId}`);
                setHistoricoOs(histOsRes.data || []);

                // --- novo: buscar consumos avulsos pelo nome do equipamento ---
                // usamos o endpoint que busca movimentações cujo campo observacao contém o nome do equipamento
                if (equipamento && equipamento.nome) {
                    try {
                        const res = await axios.get(`${ESTOQUE_API}/historico-equipamento`, {
                            params: { nome: equipamento.nome }
                        });
                        // res.data é array de MovimentacaoEstoqueDTO (id, pecaId, quantidade, tipoMovimentacao, dataMovimentacao, nomePeca, nomeEquipamento, loginUsuario...)
                        setConsumosAvulsos(Array.isArray(res.data) ? res.data : []);
                    } catch (err) {
                        // não bloqueia a visualização de histórico de OS — apenas loga
                        console.error('Erro ao buscar consumos avulsos:', err);
                        setConsumosAvulsos([]);
                    }
                } else {
                    setConsumosAvulsos([]);
                }
            } catch (err) {
                console.error(err);
                setError('Falha ao carregar histórico.');
            } finally {
                setLoading(false);
            }
        };

        if (equipamentoId) fetchDados();
    }, [equipamentoId]);

    if (loading) return <div className="main-content">Carregando...</div>;
    if (error) return <div className="main-content">{error} <Link to="/">Voltar</Link></div>;
    if (!equipamentoInfo) return <div className="main-content">Equipamento não encontrado. <Link to="/">Voltar</Link></div>;

    return (
        <div className="main-content">
            <Link to="/historico" style={{ display: 'inline-block', marginBottom: '1rem' }}>&larr; Voltar</Link>
            <h1>Histórico de Manutenção - {equipamentoInfo.nome} {equipamentoInfo.codigo ? `(${equipamentoInfo.codigo})` : ''}</h1>

            {/* Seção: Informações do equipamento */}
            <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: 6, marginBottom: 16 }}>
                <p><strong>Modelo:</strong> {equipamentoInfo.nomeModelo || 'N/A'} {equipamentoInfo.fabricante ? `(Fabr: ${equipamentoInfo.fabricante})` : ''}</p>
                <p><strong>Última Preventiva:</strong> {equipamentoInfo.dataUltimaPreventiva ? new Date(equipamentoInfo.dataUltimaPreventiva).toLocaleDateString('pt-BR') : 'N/A'}</p>
                <p><strong>Frequência:</strong> {equipamentoInfo.frequenciaPreventiva || 'N/A'}</p>
            </div>

            {/* Seção: Consumo Avulso de Peças (novo) */}
            <section style={{ marginBottom: 24 }}>
                <h2>Consumo Avulso de Peças</h2>
                {consumosAvulsos.length === 0 ? (
                    <p style={{ color: '#6c757d' }}>Nenhum consumo avulso encontrado para este equipamento.</p>
                ) : (
                    <div className="table-container" style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
                                    <th>Data</th>
                                    <th>Peça</th>
                                    <th>Quantidade</th>
                                    <th>Tipo</th>
                                    <th>Usuário</th>
                                    <th>Observação</th>
                                </tr>
                            </thead>
                            <tbody>
                                {consumosAvulsos.map(mov => (
                                    <tr key={mov.id} style={{ borderBottom: '1px solid #f1f1f1' }}>
                                        <td style={{ padding: '8px 6px' }}>{mov.dataMovimentacao ? new Date(mov.dataMovimentacao).toLocaleString('pt-BR') : '—'}</td>
                                        <td style={{ padding: '8px 6px' }}>{mov.nomePeca || '—'}</td>
                                        <td style={{ padding: '8px 6px' }}>{mov.quantidade ?? '—'}</td>
                                        <td style={{ padding: '8px 6px' }}>{mov.tipoMovimentacao || '—'}</td>
                                        <td style={{ padding: '8px 6px' }}>{mov.loginUsuario || '—'}</td>
                                        <td style={{ padding: '8px 6px' }}>{mov.observacao || (mov.nomeEquipamento ? `Equipamento: ${mov.nomeEquipamento}` : '—')}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {/* Seção: Histórico de Ordens de Serviço */}
            <section>
                <h2>Ordens de Serviço (Histórico)</h2>
                {historicoOs.length === 0 ? (
                    <p style={{ color: '#6c757d' }}>Nenhuma Ordem de Serviço encontrada para este equipamento.</p>
                ) : (
                    <div>
                        {historicoOs.map(os => (
                            <div key={os.id} style={{ border: '1px solid #e9ecef', padding: 12, borderRadius: 6, marginBottom: 8 }}>
                                <h4 style={{ margin: 0 }}>OS #{os.id} — {os.tipoManutencao}</h4>
                                <p style={{ margin: '6px 0' }}><strong>Data Agendada:</strong> {os.dataAgendamento ? new Date(os.dataAgendamento).toLocaleString('pt-BR') : 'N/A'}</p>
                                <p style={{ margin: '6px 0' }}><strong>Status:</strong> {os.status}</p>
                                <p style={{ margin: '6px 0' }}><strong>Início:</strong> {os.dataInicioExecucao ? new Date(os.dataInicioExecucao).toLocaleString('pt-BR') : '—'} — <strong>Fim:</strong> {os.dataFimExecucao ? new Date(os.dataFimExecucao).toLocaleString('pt-BR') : '—'}</p>
                                <div style={{ marginTop: 8 }}>
                                    <Link to={`/ordem-servico/${os.id}`} style={{ color: '#007bff' }}>Ver detalhes</Link>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

export default DetalheHistorico;