import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

// Pega a URL base (https://heindex-api.onrender.com)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Adiciona o prefixo /api obrigatório para todas as rotas
const OS_API_URL = `${API_BASE_URL}/api/ordens-servico`;
const EQUIP_API_URL = `${API_BASE_URL}/api/equipamentos`;
const ESTOQUE_API_URL = `${API_BASE_URL}/api/estoque`;

function HistoricoDetalhe() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [equipamento, setEquipamento] = useState(null);
    const [historicoOS, setHistoricoOS] = useState([]);
    const [consumosAvulsos, setConsumosAvulsos] = useState([]); // Novo estado
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [equipRes, osRes] = await Promise.all([
                    axios.get(`${EQUIP_API_URL}/${id}`),
                    axios.get(`${OS_API_URL}?equipamentoId=${id}`)
                ]);
                
                const dadosEquip = equipRes.data;
                setEquipamento(dadosEquip);
                setHistoricoOS(osRes.data || []);

                // Busca consumos avulsos usando o nome/tag do equipamento
                if (dadosEquip.nome) {
                    const pecasRes = await axios.get(`${ESTOQUE_API_URL}/historico-equipamento?nome=${dadosEquip.nome}`);
                    setConsumosAvulsos(pecasRes.data || []);
                }

            } catch (err) {
                console.error("Erro ao carregar dados", err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    if (loading) return <div className="main-content">Carregando...</div>;
    if (!equipamento) return <div className="main-content">Equipamento não encontrado.</div>;

    return (
        <div className="main-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <Link to="/historico" style={{ textDecoration: 'none', color: '#007bff' }}>← Voltar para a lista</Link>
                <h1>Detalhes: {equipamento.nome}</h1>
            </div>

            {/* Informações Gerais */}
            <div style={{ backgroundColor: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                <h3>Informações do Equipamento</h3>
                <p><strong>Modelo:</strong> {equipamento.nomeModelo}</p>
                <p><strong>Linha:</strong> {equipamento.nomeLinha}</p>
                <p><strong>Área:</strong> {equipamento.nomeArea}</p>
            </div>

            {/* Consumo de Peças Avulsas (Novidade) */}
            <div style={{ marginBottom: '30px' }}>
                <h3>Consumo de Peças (Baixas Avulsas)</h3>
                {consumosAvulsos.length === 0 ? <p>Nenhum consumo avulso registrado.</p> : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {consumosAvulsos.map(consumo => (
                            <div key={consumo.id} style={{
                                backgroundColor: '#fff3cd', // Cor de destaque para avulsos
                                border: '1px solid #ffeeba',
                                borderRadius: '8px',
                                padding: '15px',
                                borderLeft: '5px solid #ffc107'
                            }}>
                                <h4 style={{ margin: 0 }}>SAÍDA AVULSA: {consumo.peca?.nome}</h4>
                                <p style={{ margin: '5px 0 0 0' }}><strong>Quantidade:</strong> {consumo.quantidade}</p>
                                <p style={{ margin: '2px 0 0 0' }}><strong>Data:</strong> {new Date(consumo.dataMovimentacao).toLocaleString('pt-BR')}</p>
                                <p style={{ margin: '2px 0 0 0', fontSize: '0.85rem', color: '#666' }}><em>Obs: {consumo.observacao}</em></p>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Histórico de Ordens de Serviço */}
            <div>
                <h3>Ordens de Serviço</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {historicoOS.map(os => (
                        <div key={os.id} style={{
                            backgroundColor: '#f8f9fa',
                            border: '1px solid #ced4da',
                            borderRadius: '8px',
                            padding: '15px',
                            position: 'relative'
                        }}>
                            <h4 style={{ margin: 0 }}>OS #{os.id} - {os.tipoManutencao}</h4>
                            <p style={{ margin: '5px 0 0 0' }}><strong>Técnico:</strong> {os.nomeTecnico}</p>
                            <p style={{ margin: '2px 0 0 0' }}>
                                <strong>Data:</strong> {os.dataFimExecucao ? new Date(os.dataFimExecucao).toLocaleString('pt-BR') : 'Pendente'}
                            </p>
                            <span style={{
                                position: 'absolute', right: '15px', top: '15px',
                                backgroundColor: os.status === 'CONCLUIDA' ? '#28a745' : '#6c757d',
                                color: '#fff', padding: '4px 10px', borderRadius: '12px', fontSize: '0.7rem'
                            }}>{os.status}</span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default HistoricoDetalhe;
