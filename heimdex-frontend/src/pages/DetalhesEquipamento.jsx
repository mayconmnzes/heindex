import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import html2pdf from 'html2pdf.js'; // Importação da biblioteca de PDF

const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api`;

// BACKEND_URL deve ser apenas a base do Render
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL;

function DetalhesEquipamento() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [equipamento, setEquipamento] = useState(null);
    const [historico, setHistorico] = useState([]);
    const [loading, setLoading] = useState(true);
    const [fotoAberta, setFotoAberta] = useState(null);
    
    // Referência para o conteúdo que será transformado em PDF
    const relatorioRef = useRef();

    useEffect(() => {
        const fetchDados = async () => {
            setLoading(true);
            try {
                const equipRes = await axios.get(`${API_BASE_URL}/equipamentos/${id}`);
                const equipData = equipRes.data;
                setEquipamento(equipData);

                const [osRes, pecasRes] = await Promise.all([
                    axios.get(`${API_BASE_URL}/ordens-servico?equipamentoId=${id}`),
                    axios.get(`${API_BASE_URL}/estoque/historico-equipamento?nome=${equipData.nome}`)
                ]);

                const osFiltradas = osRes.data.filter(os => {
                    return String(os.equipamentoId) === String(id) || os.nomeEquipamento === equipData.nome;
                });

                const listaOS = osFiltradas.map(os => ({
                    ...os,
                    tipoRegistro: 'OS',
                    dataOrdenacao: os.dataFimExecucao || os.dataAgendamento,
                    corDestaque: os.status === 'CONCLUIDA' ? '#28a745' : '#007bff'
                }));

                const listaConsumo = pecasRes.data.map(mov => ({
                    ...mov,
                    tipoRegistro: 'CONSUMO',
                    dataOrdenacao: mov.dataMovimentacao,
                    corDestaque: '#f39c12'
                }));

                const unificado = [...listaOS, ...listaConsumo].sort((a, b) => 
                    new Date(b.dataOrdenacao) - new Date(a.dataOrdenacao)
                );

                setHistorico(unificado);
            } catch (error) {
                console.error("Erro ao carregar histórico:", error);
            } finally {
                setLoading(false);
            }
        };

        if (id) fetchDados();
    }, [id]);

    // Função para gerar o PDF
    const exportarPDF = () => {
        const element = relatorioRef.current;
        const options = {
            margin: [10, 10, 10, 10],
            filename: `Historico_${equipamento?.nome}_${new Date().toLocaleDateString()}.pdf`,
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, logging: false },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };

        html2pdf().set(options).from(element).save();
    };

    const renderImagemUrl = (url) => {
        if (!url) return '';
        if (url.startsWith('http')) return url;
        const path = url.startsWith('/') ? url : `/${url}`;
        return `${BACKEND_URL}${path}`;
    };

    if (loading) return <div className="main-content"><h1>Carregando histórico...</h1></div>;

    return (
        <div className="main-content">
            <header style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <button onClick={() => navigate(-1)} style={{ padding: '10px 15px', cursor: 'pointer', borderRadius: '4px', border: '1px solid #ccc' }}>
                        ← Voltar
                    </button>
                    <div>
                        <h1 style={{ margin: 0 }}>Histórico Individual: {equipamento?.nome}</h1>
                        <p style={{ margin: 0, color: '#666' }}>Local: {equipamento?.nomeArea} | Linha: {equipamento?.nomeLinha}</p>
                    </div>
                </div>
                
                {/* BOTÃO DE EXPORTAR PDF */}
                <button 
                    onClick={exportarPDF}
                    style={{ background: '#e74c3c', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                    📄 Exportar PDF
                </button>
            </header>

            {/* DIV REF PARA O RELATÓRIO */}
            <div ref={relatorioRef} style={{ padding: '5px' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '20px' }}>
                    {historico.length > 0 ? (
                        historico.map((item, index) => (
                            <div key={index} style={{
                                background: '#fff', borderRadius: '12px', padding: '20px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.1)', borderTop: `6px solid ${item.corDestaque}`,
                                pageBreakInside: 'avoid' // Evita que o card seja cortado no meio entre páginas do PDF
                            }}>
                                
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                                    <small style={{ color: '#888' }}>{new Date(item.dataOrdenacao).toLocaleString('pt-BR')}</small>
                                    <span style={{ fontWeight: 'bold', fontSize: '0.8rem', color: item.corDestaque }}>
                                        {item.tipoRegistro === 'OS' ? (item.status || 'STATUS') : 'CONSUMO AVULSO'}
                                    </span>
                                </div>

                                {item.tipoRegistro === 'OS' ? (
                                    <>
                                        <h3 style={{ margin: '0 0 5px 0' }}>{item.tipoManutencao} #{item.id}</h3>
                                        <p style={{ fontSize: '0.85rem', color: '#dc3545', fontWeight: 'bold', marginBottom: '10px' }}>
                                            Equipamento: {item.nomeEquipamento}
                                        </p>
                                        
                                        <div style={{ fontSize: '0.9rem', color: '#444' }}>
                                            <p><strong>Responsável:</strong> {item.nomeTecnico || 'Não atribuído'}</p>
                                            <p><strong>Relatório:</strong> {item.observacoesTecnico || 'Sem relato técnico.'}</p>
                                        </div>

                                        <div style={{ background: '#f8f9fa', padding: '10px', borderRadius: '8px', marginTop: '10px' }}>
                                            <strong>🛠 Peças/Serviços:</strong>
                                            <ul style={{ margin: '5px 0', paddingLeft: '20px', fontSize: '0.85rem' }}>
                                                {item.pecasConsumidas?.length > 0 ? item.pecasConsumidas.map((p, i) => (
                                                    <li key={i}>{p.nomePeca} (x{p.quantidadeBaixada})</li>
                                                )) : <li>Nenhum item registrado</li>}
                                            </ul>
                                        </div>

                                        {item.fotosEvidencia?.length > 0 && (
                                            <div style={{ marginTop: '15px', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                                {item.fotosEvidencia.map((url, i) => (
                                                    <img 
                                                        key={i} 
                                                        src={renderImagemUrl(url)} 
                                                        alt="Evidência" 
                                                        onClick={() => setFotoAberta(renderImagemUrl(url))}
                                                        style={{ width: '70px', height: '70px', borderRadius: '6px', objectFit: 'cover', border: '1px solid #eee', cursor: 'pointer' }} 
                                                        crossOrigin="anonymous" // Necessário para o PDF carregar imagens de outro domínio
                                                    />
                                                ))}
                                            </div>
                                        )}
                                    </>
                                ) : (
                                    <>
                                        <h3 style={{ margin: '0 0 10px 0' }}>Baixa Direta de Estoque</h3>
                                        <p><strong>Peça:</strong> {item.peca?.nome || item.nomePeca}</p>
                                        <p><strong>Quantidade:</strong> {item.quantidade}</p>
                                        <p><strong>Motivo/Obs:</strong> {item.observacao || 'Uso imediato no equipamento.'}</p>
                                    </>
                                )}
                            </div>
                        ))
                    ) : (
                        <p>Nenhuma atividade registrada.</p>
                    )}
                </div>
            </div>

            {/* MODAL DE ZOOM (O PDF IGNORA ESTE COMPONENTE POR ESTAR FORA DA REF) */}
            {fotoAberta && (
                <div 
                    style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.85)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}
                    onClick={() => setFotoAberta(null)}
                >
                    <img src={fotoAberta} alt="Zoom" style={{ maxWidth: '90%', maxHeight: '90%', borderRadius: '8px' }} />
                </div>
            )}
        </div>
    );
}

export default DetalhesEquipamento;
