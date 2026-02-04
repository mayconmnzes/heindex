import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

// ✅ CORREÇÃO: URLs ajustadas com /api para bater com o Backend e evitar erro de recurso
const OS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/ordens-servico`;
const FOTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/fotos/upload`;
const ESTOQUE_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/pecas`; 
const USUARIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/usuarios`;
const PECAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/pecas`;

const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL.replace('/api', '');

function ExecucaoOS() {
    const { osId } = useParams();
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const [os, setOs] = useState(null);
    const [loading, setLoading] = useState(true);
    const [checklistData, setChecklistData] = useState({});
    const [observacoesTecnico, setObservacoesTecnico] = useState('');
    const [selectedFile, setSelectedFile] = useState(null);

    const [pecasConsumidas, setPecasConsumidas] = useState([]);
    const [pecaParaBaixa, setPecaParaBaixa] = useState(null);
    const [codigoPecaInput, setCodigoPecaInput] = useState('');
    const [quantidadeBaixa, setQuantidadeBaixa] = useState(1);

    const [tecnicos, setTecnicos] = useState([]);
    const [selectedTecnicoId, setSelectedTecnicoId] = useState('');
    const [selectedDataAgendamento, setSelectedDataAgendamento] = useState('');

    const statusLimpo = os?.status?.toUpperCase().replace(/\s/g, '_') || '';

    const isSuggestionMode = statusLimpo === 'SUGESTAO';
    const isExecutionMode = statusLimpo === 'EM_EXECUCAO' || statusLimpo === 'PENDENTE_DE_CORRECAO';
    const isValidationMode = statusLimpo === 'AGUARDANDO_VALIDACAO';
    const isScheduledMode = statusLimpo === 'AGENDADA';

    const canConfirmSchedule = isSuggestionMode && (user.perfil === 'LIDER' || user.perfil === 'ADMINISTRADOR');
    const canStartExecution = isScheduledMode && (user.perfil === 'TECNICO' || user.perfil === 'ADMINISTRADOR');
    const canModify = isExecutionMode && (user.perfil === 'TECNICO' || user.perfil === 'ADMINISTRADOR');
    const canValidate = isValidationMode && (user.perfil === 'LIDER' || user.perfil === 'ADMINISTRADOR');

    const fetchOsDetails = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${OS_API_URL}/${osId}`);
            const osData = response.data;
            setOs(osData);
            setObservacoesTecnico(osData.observacoesTecnico || '');

            if (osData.status === 'SUGESTAO' && osData.dataAgendamento) {
                const formattedDate = osData.dataAgendamento.substring(0, 16);
                setSelectedDataAgendamento(formattedDate);
            } else if (osData.status === 'AGENDADA' && osData.tecnicoId) {
                 setSelectedTecnicoId(osData.tecnicoId.toString());
            }

            const initialData = {};
            const savedResultsMap = osData.resultados?.reduce((map, result) => {
                map[result.itemTemplateId] = result;
                return map;
            }, {});

            osData.checklist?.itens?.forEach(item => {
                const savedResult = savedResultsMap ? savedResultsMap[item.id] : null;
                initialData[item.id] = {
                    status: savedResult?.status || 'PENDENTE',
                    observacao: savedResult?.observacao || ''
                };
            });
            setChecklistData(initialData);

        } catch (error) {
            console.error("Erro ao buscar detalhes da OS:", error);
            navigate('/');
        } finally {
            setLoading(false);
        }
    };

    const fetchPecasConsumidas = async () => {
        try {
            const response = await axios.get(`${ESTOQUE_API_URL}/consumo-os/${osId}`);
            setPecasConsumidas(response.data);
        } catch (error) {
            console.warn("Erro peças consumidas.");
        }
    };

    const fetchTecnicos = async () => {
        try {
            const response = await axios.get(USUARIOS_API_URL);
            setTecnicos(response.data.filter(u => u.perfil === 'TECNICO' || u.perfil === 'ADMINISTRADOR'));
        } catch (error) {
            console.error("Erro técnicos:", error);
        }
    };

    useEffect(() => {
        if (osId) {
            fetchOsDetails();
            fetchPecasConsumidas();
            fetchTecnicos();
        }
    }, [osId]);

    useEffect(() => {
        if (pecasConsumidas.length > 0 && isExecutionMode) {
            const pecasStr = "--- PEÇAS UTILIZADAS ---\n" + 
                             pecasConsumidas.map(p => `- ${p.quantidade}x ${p.nomePeca} (${p.codigoControle})`).join("\n");
            
            setObservacoesTecnico(currentObs => {
                const marker = "--- PEÇAS UTILIZADAS ---";
                const markerIndex = currentObs.indexOf(marker);
                let userPart = currentObs;
                if (markerIndex !== -1) {
                    userPart = currentObs.substring(0, markerIndex);
                }
                return userPart.trim() + "\n\n" + pecasStr;
            });
        }
    }, [pecasConsumidas, isExecutionMode]);

    const handleItemChange = (itemId, type, value) => {
        if (!canModify) return;
        setChecklistData(prev => ({
            ...prev,
            [itemId]: { ...prev[itemId], [type]: value }
        }));
    };

    const handleIniciarOs = async () => {
        if (!canStartExecution) return alert("Sem permissão.");
        if (!window.confirm("Deseja iniciar?")) return;

        try {
            await axios.post(`${OS_API_URL}/${osId}/iniciar`);
            fetchOsDetails();
        } catch (error) {
            alert("Falha ao iniciar.");
        }
    };

    const handleConfirmarAgendamento = async () => {
        if (!canConfirmSchedule) return alert("Sem permissão.");
        const data = {
            tecnicoId: Number(selectedTecnicoId),
            dataAgendamento: selectedDataAgendamento
        };
        try {
            await axios.post(`${OS_API_URL}/${osId}/confirmar-agendamento`, data);
            alert("Agendada!");
            fetchOsDetails();
        } catch (error) {
            alert("Erro agendamento.");
        }
    };

    const handleFinalizarManutencao = async () => {
        if (!canModify) return alert("Sem permissão.");
        
        const resultadosParaEnvio = os.checklist?.itens?.map(item => ({
            itemTemplateId: item.id,
            status: checklistData[item.id]?.status || 'NAO_APLICAVEL',
            observacao: checklistData[item.id]?.observacao || ''
        })) || [];

        try {
            await axios.post(`${OS_API_URL}/${osId}/finalizar`, {
                observacoesTecnico,
                resultados: resultadosParaEnvio
            });
            alert("Finalizada!");
            navigate('/');
        } catch (error) {
            alert("Erro finalizar.");
        }
    };

    const handleValidarReprovar = async (action) => {
        if (!canValidate) return;
        const promptText = action === 'validar' ? 'Obs Aprovação:' : 'Obs Reprovação:';
        const obs = window.prompt(promptText);
        if (action === 'reprovar' && !obs) return alert("Obrigatório.");
        if (obs === null) return;

        try {
            await axios.post(`${OS_API_URL}/${osId}/${action}`, {
                liderId: user.id,
                observacoesLider: obs
            });
            alert(`OS processada!`);
            navigate('/');
        } catch (error) {
            alert("Erro validação.");
        }
    };

    const handleFileChange = (e) => setSelectedFile(e.target.files[0]);

    const handleUploadEvidencia = async (e) => {
        e.preventDefault();
        if (!selectedFile) return;
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            const uploadRes = await axios.post(FOTOS_API_URL, formData);
            await axios.post(`${OS_API_URL}/${osId}/fotos`, JSON.stringify(uploadRes.data), {
                headers: { 'Content-Type': 'application/json' }
            });
            alert("Foto enviada!");
            fetchOsDetails();
        } catch (error) {
            alert("Erro upload.");
        }
    };

    const handleBuscaPecaPorCodigo = async () => {
        try {
            const response = await axios.get(PECAS_API_URL);
            const encontrada = response.data.find(p => p.codigoControle === codigoPecaInput.trim());
            if (encontrada) setPecaParaBaixa(encontrada);
            else alert("Não encontrada.");
        } catch (error) {
            alert("Erro busca.");
        }
    };

    const handleBaixaPeca = async () => {
        if (!pecaParaBaixa) return;
        try {
            await axios.post(`${ESTOQUE_API_URL}/saida/${osId}`, {
                pecaId: pecaParaBaixa.id,
                quantidade: Number(quantidadeBaixa)
            });
            alert("Baixa efetuada!");
            setPecaParaBaixa(null);
            setCodigoPecaInput('');
            fetchPecasConsumidas();
        } catch (error) {
            alert("Erro baixa.");
        }
    };

    if (loading) return <div className="main-content"><h1>Carregando...</h1></div>;
    if (!os) return <div className="main-content">OS não encontrada. <Link to="/">Voltar</Link></div>;

    return (
        <div className="main-content">
            <h1>{isSuggestionMode ? 'Analisar Sugestão' : 'Detalhes'} da OS #{os.id}</h1>
            <a href="#" onClick={(e) => { e.preventDefault(); navigate(-1); }} style={{ display: 'inline-block', marginBottom: '1rem', color: '#007bff', textDecoration: 'none' }}>&larr; Voltar</a>

            <div className="container" style={{flexDirection: 'column', alignItems: 'flex-start'}}>
                
                {isScheduledMode && canStartExecution && (
                    <button onClick={handleIniciarOs} style={{backgroundColor: '#007bff', color: '#fff', marginBottom: '15px', border: 'none', padding: '10px 20px', borderRadius: '4px', cursor: 'pointer'}}>INICIAR EXECUÇÃO</button>
                )}

                <section className="list-section" style={{width: '100%'}}>
                    <h3>Detalhes da OS - Status: <span className={`status status-${os.status.toLowerCase().replace(/_/g, '-')}`}>{os.status.replace(/_/g, ' ')}</span></h3>
                    <p><strong>Equipamento:</strong> {os.nomeEquipamento} ({os.codigoEquipamento})</p>
                    <p><strong>Técnico:</strong> {os.nomeTecnico || 'A definir'}</p>
                    <p><strong>Tipo:</strong> {os.tipoManutencao}</p>
                    <p><strong>Agendado para:</strong> {os.dataAgendamento ? new Date(os.dataAgendamento).toLocaleString('pt-BR') : 'N/A'}</p>
                </section>

                {isSuggestionMode && canConfirmSchedule && (
                    <section className="form-section" style={{width: '100%', backgroundColor: '#f0e6ff', padding: '15px', borderRadius: '8px'}}>
                        <h3>Confirmar Agendamento</h3>
                        <label>Técnico Responsável:</label>
                        <select value={selectedTecnicoId} onChange={(e) => setSelectedTecnicoId(e.target.value)}>
                            <option value="">-- Selecione --</option>
                            {tecnicos.map(t => <option key={t.id} value={t.id}>{t.nomeCompleto}</option>)}
                        </select>
                        <label>Data/Hora:</label>
                        <input type="datetime-local" value={selectedDataAgendamento} onChange={(e) => setSelectedDataAgendamento(e.target.value)} />
                        <button onClick={handleConfirmarAgendamento} style={{backgroundColor: '#6f42c1', color: '#fff', marginTop: '10px'}}>Confirmar Agendamento</button>
                    </section>
                )}

                {/* ✅ SOLUÇÃO DEFINITIVA: Mostra a tabela se houver 'resultados' OU 'checklist' */}
{(isExecutionMode || isValidationMode) && (os.resultados?.length > 0 || os.checklist?.itens?.length > 0) && (
    <section className="list-section" style={{width: '100%', marginTop: '20px'}}> 
        <h3>📋 Checklist da Manutenção</h3>
        <div className="table-container">
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                    <tr style={{background: '#34495e', color: '#fff'}}>
                        <th style={{padding: '10px', width: '15%'}}>Status</th>
                        <th style={{padding: '10px'}}>Descrição</th>
                        <th style={{padding: '10px'}}>Observação</th>
                    </tr>
                </thead>
                <tbody>
                    {/* ✅ Usa 'resultados' se existir, senão usa 'checklist.itens' */}
                    {(os.resultados?.length > 0 ? os.resultados : os.checklist.itens).map(item => {
                        // O ID pode vir como 'id' ou 'itemTemplateId'
                        const itemId = item.itemTemplateId || item.id;
                        // A descrição pode estar no topo ou dentro do template
                        const descricao = item.descricao || item.itemTemplate?.descricao;

                        return (
                            <tr key={itemId} style={{borderBottom: '1px solid #eee'}}>
                                <td style={{padding: '10px'}}>
                                    <select 
                                        value={checklistData[itemId]?.status || item.status || 'PENDENTE'} 
                                        onChange={(e) => handleItemChange(itemId, 'status', e.target.value)} 
                                        disabled={!canModify}
                                    >
                                        <option value="PENDENTE">🟡 Pendente</option>
                                        <option value="OK">🟢 OK</option>
                                        <option value="NAO_CONFORMIDADE">🔴 Não OK</option>
                                        <option value="NAO_APLICAVEL">⚪ N/A</option>
                                    </select>
                                </td>
                                <td style={{padding: '10px'}}>{descricao}</td>
                                <td style={{padding: '10px'}}>
                                    <input 
                                        type="text" 
                                        value={checklistData[itemId]?.observacao || item.observacao || ''} 
                                        onChange={(e) => handleItemChange(itemId, 'observacao', e.target.value)} 
                                        placeholder="Obs..." 
                                        disabled={!canModify} 
                                        style={{width: '100%'}} 
                                    />
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    </section>
)}

                {canModify && (
                    <section className="list-section" style={{width: '100%', marginTop: '20px'}}>
                        <h3>Baixa de Peças</h3>
                        <div className="item-adder">
                            <input value={codigoPecaInput} onChange={e => setCodigoPecaInput(e.target.value)} placeholder="QR Code" />
                            <button onClick={handleBuscaPecaPorCodigo}>Buscar</button>
                        </div>
                        {pecaParaBaixa && (
                            <div style={{marginTop: '10px', background: '#f0fff0', padding: '15px', border: '1px solid #28a745', borderRadius: '6px'}}>
                                <strong>{pecaParaBaixa.nome}</strong> | Estoque: {pecaParaBaixa.estoqueAtual}
                                <br/><input type="number" value={quantidadeBaixa} onChange={e => setQuantidadeBaixa(e.target.value)} style={{width: '80px', marginTop: '5px'}} />
                                <button onClick={handleBaixaPeca} style={{backgroundColor: '#007bff', color: '#fff', marginLeft: '10px'}}>Confirmar</button>
                            </div>
                        )}
                    </section>
                )}

                <section className="list-section" style={{width: '100%', marginTop: '20px'}}>
                    <h3>Peças Utilizadas ({pecasConsumidas.length})</h3>
                    <div className="table-container">
                        <table>
                            <thead><tr><th>Nome</th><th>Código</th><th>Quantidade</th></tr></thead>
                            <tbody>
                                {pecasConsumidas.map(p => <tr key={p.id}><td>{p.nomePeca}</td><td>{p.codigoControle}</td><td>{p.quantidade}</td></tr>)}
                            </tbody>
                        </table>
                    </div>
                </section>

                <section className="list-section" style={{width: '100%', marginTop: '20px'}}>
                    <h3>Evidências</h3>
                    {os?.status !== 'CONCLUIDA' && (
                        <form onSubmit={handleUploadEvidencia} className="item-adder">
                            <input type="file" onChange={handleFileChange} accept="image/*" />
                            <button type="submit" disabled={!selectedFile} style={{backgroundColor: '#28a745', color: '#fff'}}>Anexar</button>
                        </form>
                    )}
                    <div style={{display: 'flex', gap: '10px', marginTop: '10px', flexWrap: 'wrap'}}>
                        {os.fotosEvidencia?.map((f, i) => <img key={i} src={`${BACKEND_BASE_URL}${f}`} alt="evid" style={{width: '100px', height: '100px', objectFit: 'cover'}} />)}
                    </div>
                </section>

                {!isSuggestionMode && (
                    <section className="form-section" style={{width: '100%', marginTop: '20px'}}>
                        <h3>Ações Finais</h3>
                        <textarea rows="4" value={observacoesTecnico} onChange={(e) => setObservacoesTecnico(e.target.value)} disabled={!canModify} style={{width: '100%'}}></textarea>
                        <div style={{marginTop: '20px', display: 'flex', gap: '10px'}}>
                            {canModify && <button onClick={handleFinalizarManutencao} style={{backgroundColor: '#28a745', color: '#fff', padding: '10px 20px', border: 'none', borderRadius: '4px'}}>FINALIZAR</button>}
                        </div>
                    </section>
                )}
            </div>
        </div>
    );
}

export default ExecucaoOS;