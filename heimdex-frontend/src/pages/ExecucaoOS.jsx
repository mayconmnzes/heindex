import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

// OBS: Adicionei o prefixo /api nas URLs para bater com o backend Spring Boot (endpoints ficam em /api/...)
const OS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/ordens-servico`;
const FOTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/fotos/upload`;
const ESTOQUE_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/estoque`;
const USUARIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/usuarios`;
const PECAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/pecas`;
const EQUIPAMENTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;

const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL.replace('/api', '');

/**
 * parseDateToLocal
 * Interpreta strings retornadas do backend como datas LOCAIS.
 * Suporta:
 *  - "YYYY-MM-DD" (LocalDate) -> cria Date no timezone local (00:00 local)
 *  - "YYYY-MM-DDTHH:mm" ou "YYYY-MM-DDTHH:mm:ss" (LocalDateTime sem timezone) -> interpreta como local
 *  - fallback -> new Date(...)
 */
function parseDateToLocal(dateStr) {
  if (!dateStr) return null;
  if (typeof dateStr === 'object' && dateStr instanceof Date) return dateStr;

  // YYYY-MM-DD
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    const [y, m, d] = dateStr.split('-').map(Number);
    return new Date(y, m - 1, d);
  }

  // YYYY-MM-DDTHH:mm or YYYY-MM-DDTHH:mm:ss (no timezone) -> treat as local
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?$/.test(dateStr)) {
    const [datePart, timePart] = dateStr.split('T');
    const [y, m, d] = datePart.split('-').map(Number);
    const [hh, mm, ss = '0'] = timePart.split(':');
    return new Date(y, m - 1, d, Number(hh), Number(mm), Number(ss));
  }

  // Fallback (may contain timezone)
  const parsed = new Date(dateStr);
  return isNaN(parsed) ? null : parsed;
}

function ExecucaoOS() {
    const { osId } = useParams();
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const [os, setOs] = useState(null);
    const [loading, setLoading] = useState(true);
    const [checklistData, setChecklistData] = useState({});
    const [observacoesTecnico, setObservacoesTecnico] = useState('');
    const [selectedFile, setSelectedFile] = useState(null);

    // Estados para baixa de peças
    const [pecasConsumidas, setPecasConsumidas] = useState([]);
    const [pecaParaBaixa, setPecaParaBaixa] = useState(null);
    const [codigoPecaInput, setCodigoPecaInput] = useState('');
    const [quantidadeBaixa, setQuantidadeBaixa] = useState(1);

    // --- ESTADOS PARA CONFIRMAÇÃO DE AGENDAMENTO ---
    const [tecnicos, setTecnicos] = useState([]);
    const [selectedTecnicoId, setSelectedTecnicoId] = useState('');
    const [selectedDataAgendamento, setSelectedDataAgendamento] = useState('');

    // Permissões e Modos (Calculados dinamicamente)
    const isSuggestionMode = os?.status === 'SUGESTAO';
    const isExecutionMode = os?.status === 'EM_EXECUCAO' || os?.status === 'PENDENTE_DE_CORRECAO';
    const isValidationMode = os?.status === 'AGUARDANDO_VALIDACAO';
    const isScheduledMode = os?.status === 'AGENDADA';

    // Permissões de Ação
    const canConfirmSchedule = isSuggestionMode && (user.perfil === 'LIDER' || user.perfil === 'ADMINISTRADOR');
    const canStartExecution = isScheduledMode && (user.perfil === 'TECNICO' || user.perfil === 'ADMINISTRADOR');
    const canModify = isExecutionMode && (user.perfil === 'TECNICO' || user.perfil === 'ADMINISTRADOR');
    const canValidate = isValidationMode && (user.perfil === 'LIDER' || user.perfil === 'ADMINISTRADOR');

    // --- FUNÇÕES DE FETCH ---
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
                // Se já estiver agendada, pré-seleciona o técnico (se houver)
                setSelectedTecnicoId(osData.tecnicoId.toString());
            }

            // Prepara dados iniciais do checklist a partir de resultados salvos (se houver)
            const initialData = {};
            const savedResultsMap = osData.resultados?.reduce((map, result) => {
                map[result.itemTemplateId] = result;
                return map;
            }, {});
            (osData.checklist?.itens || []).forEach(item => {
                const savedResult = savedResultsMap ? savedResultsMap[item.id] : null;
                initialData[item.id] = {
                    status: savedResult?.status || 'PENDENTE',
                    observacao: savedResult?.observacao || ''
                };
            });
            setChecklistData(initialData);

        } catch (error) {
            console.error("Erro ao buscar detalhes da OS:", error);
            alert("Erro ao carregar OS. Verifique o ID ou a conexão.");
            navigate('/'); // Volta ao dashboard se não encontrar a OS
        } finally {
            setLoading(false);
        }
    };

    const fetchPecasConsumidas = async () => {
        try {
            const response = await axios.get(`${ESTOQUE_API_URL}/consumo-os/${osId}`);
            setPecasConsumidas(response.data);
        } catch (error) { console.error("Erro ao buscar peças consumidas:", error); }
    };
    const fetchTecnicos = async () => {
        try {
            const response = await axios.get(USUARIOS_API_URL);
            setTecnicos(response.data.filter(u => u.perfil === 'TECNICO' || u.perfil === 'ADMINISTRADOR'));
        } catch (error) { console.error("Erro ao buscar técnicos:", error); }
    };

    useEffect(() => {
        if (osId) {
            fetchOsDetails();
            fetchPecasConsumidas();
            fetchTecnicos();
        }
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [osId]); // Dependência apenas em osId para rodar só na troca de OS

    useEffect(() => {
        if (pecasConsumidas.length > 0 && isExecutionMode) {
            const pecasStr = "--- PEÇAS UTILIZADAS ---\n" +
                             pecasConsumidas.map(p => `- ${p.quantidade}x ${p.nomePeca} (${p.codigoControle})`).join("\n");
            setObservacoesTecnico(currentObs => {
                const marker = "--- PEÇAS UTILIZADAS ---";
                const markerIndex = currentObs.indexOf(marker);
                let userPart = currentObs;
                if (markerIndex !== -1) { userPart = currentObs.substring(0, markerIndex); }
                return userPart.trim() + "\n\n" + pecasStr;
            });
        }
    }, [pecasConsumidas, isExecutionMode]);

    // --- HANDLERS DE AÇÃO ---
    const handleItemChange = (itemId, type, value) => {
        if (!canModify) return;
        setChecklistData(prev => ({ ...prev, [itemId]: { ...prev[itemId], [type]: value } }));
    };

    const handleIniciarOs = async () => {
        if (!canStartExecution) return alert("A OS não está agendada ou você não tem permissão.");
        
        // Verificando 'nomeTecnico' em vez de 'tecnicoId'
        if (!os?.nomeTecnico) return alert("Erro: Técnico não definido para esta OS agendada.");

        if (!window.confirm("Deseja realmente iniciar a execução?")) return;
        try {
            await axios.post(`${OS_API_URL}/${osId}/iniciar`);
            fetchOsDetails(); // Rebusca para atualizar status
        } catch (error) { alert(`Falha ao iniciar: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`); }
    };

    const handleConfirmarAgendamento = async () => {
        if (!canConfirmSchedule) return alert("Você não tem permissão para confirmar esta sugestão.");
        if (!selectedTecnicoId || !selectedDataAgendamento) {
            return alert("Selecione um técnico e uma data/hora para agendar.");
        }
        // Validação básica da data (não pode ser no passado)
        if (new Date(selectedDataAgendamento) < new Date()) {
             return alert("A data de agendamento não pode ser no passado.");
        }

        const data = {
            tecnicoId: Number(selectedTecnicoId),
            dataAgendamento: selectedDataAgendamento // Formato YYYY-MM-DDTHH:mm
        };

        try {
            await axios.post(`${OS_API_URL}/${osId}/confirmar-agendamento`, data);
            alert("OS Agendada com sucesso!");
            fetchOsDetails(); // Atualiza a tela
        } catch (error) {
            console.error("Erro ao confirmar agendamento:", error.response);
            alert(`Falha ao agendar: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`);
        }
    };


    const handleFinalizarManutencao = async () => {
        if (!canModify) return alert("A OS precisa estar em execução ou em correção e você ter permissão.");

        const itensPendentes = os.checklist?.itens?.filter(item => (checklistData[item.id]?.status || 'PENDENTE') === 'PENDENTE') || [];
        if (itensPendentes.length > 0) {
            if (!window.confirm(`Ainda há ${itensPendentes.length} itens pendentes no checklist. Deseja finalizar assim mesmo?`)) return;
        }

        const resultadosParaEnvio = os.checklist?.itens?.map(item => ({
            itemTemplateId: item.id,
            status: checklistData[item.id]?.status || 'NAO_APLICAVEL',
            observacao: checklistData[item.id]?.observacao || ''
        })) || []; // Envia array vazio se não houver checklist/itens

        const data = { observacoesTecnico, resultados: resultadosParaEnvio };

        try {
            await axios.post(`${OS_API_URL}/${osId}/finalizar`, data);
            alert("Manutenção finalizada! Aguardando validação.");
            navigate('/'); // Volta ao dashboard após finalizar
        } catch (error) { alert(`Falha ao finalizar: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`); }
    };

    // CORREÇÃO: handleValidarReprovar atualizada para não forçar reload e manter botões visíveis.
    const handleValidarReprovar = async (action) => {
        if (!canValidate) return;
        const promptText = action === 'validar' ? 'Obs. para Aprovação (Opcional):' : 'Obs. para Reprovação (Obrigatório):';
        const observacoesLider = window.prompt(promptText);

        if (observacoesLider === null) return; // Cancelou o prompt
        if (action === 'reprovar' && observacoesLider.trim() === '') return alert("Observação obrigatória para reprovar.");

        const url = `${OS_API_URL}/${osId}/${action}`;
        const data = { liderId: user.id, observacoesLider };

        try {
            // Faz a requisição; controller retorna a OS atualizada (se implementado) ou 200
            const res = await axios.post(url, data);

            alert(`OS ${action === 'validar' ? 'APROVADA' : 'REPROVADA'} com sucesso!`);

            // Recarrega detalhes da OS atual (atualiza botão/estado na mesma tela)
            await fetchOsDetails();

            // Navega para Planejamento sem forçar reload completo (a página de Planejamento faz fetch ao montar)
            // Se você não quiser navegar automaticamente, comente a linha abaixo.
            navigate('/planejamento');
        } catch (error) {
            alert(`Falha ao ${action}: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`);
        }
    };

    const handleFileChange = (e) => { setSelectedFile(e.target.files[0]); };

    const handleUploadEvidencia = async (e) => {
        e.preventDefault();
        if (!selectedFile) return alert("Selecione um arquivo.");
        // Permite anexar em qualquer status, exceto concluída? Ou só em execução?
        // Vamos permitir anexar se NÃO estiver concluída, por flexibilidade.
        if (os?.status === 'CONCLUIDA') return alert("Não é possível anexar fotos a uma OS Concluída.");

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const uploadRes = await axios.post(FOTOS_API_URL, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            const fotoUrl = uploadRes.data; // Já é string de texto

            // Associa URL à OS (envia como texto plano ou JSON simples)
            await axios.post(`${OS_API_URL}/${osId}/fotos`, JSON.stringify(fotoUrl), { headers: { 'Content-Type': 'application/json' } });

            alert("Foto anexada com sucesso!");
            setSelectedFile(null);
            if(document.getElementById('file-evidencia')) document.getElementById('file-evidencia').value = '';
            fetchOsDetails(); // Rebusca para mostrar nova foto
        } catch (error) {
            console.error("Erro upload/anexo:", error.response?.data || error.message);
            alert("Falha ao anexar foto.");
        }
    };


    const handleBuscaPecaPorCodigo = async () => {
        if (!codigoPecaInput) return;
        try {
            // Idealmente, ter um endpoint `/api/pecas/by-codigo/{codigo}` no backend
            const response = await axios.get(PECAS_API_URL); // Temporário: busca todas
            const pecaEncontrada = response.data.find(p => p.codigoControle === codigoPecaInput.trim());
            if (pecaEncontrada) { setPecaParaBaixa(pecaEncontrada); }
            else { alert("Peça não encontrada."); setPecaParaBaixa(null); }
        } catch (error) { alert("Erro ao buscar peça."); }
    };

    const handleBaixaPeca = async () => {
        if (!pecaParaBaixa || quantidadeBaixa <= 0) return alert("Busque peça e defina qtd > 0.");
        if (quantidadeBaixa > pecaParaBaixa.estoqueAtual) return alert(`Estoque insuficiente! (${pecaParaBaixa.estoqueAtual})`);

        const data = { pecaId: pecaParaBaixa.id, quantidade: Number(quantidadeBaixa) };

        try {
            await axios.post(`${ESTOQUE_API_URL}/saida/${osId}`, data);
            alert("Baixa registrada!");
            setCodigoPecaInput(''); setPecaParaBaixa(null); setQuantidadeBaixa(1);
            fetchPecasConsumidas(); // Atualiza lista
            // Opcional: Atualizar estoque da peça na tela (requer fetchPecas ou cálculo local)
        } catch (error) { alert(`Falha ao dar baixa: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`); }
    };

    // --- RENDERIZAÇÃO ---
    if (loading) return <div className="main-content">Carregando...</div>;
    if (!os) return <div className="main-content">Ordem de Serviço não encontrada. <Link to="/">Voltar</Link></div>;

    // formata datas usando parseDateToLocal para evitar shift de dia por timezone
    const dtAgendamento = parseDateToLocal(os.dataAgendamento);
    const dtInicio = parseDateToLocal(os.dataInicioExecucao);
    const dtFim = parseDateToLocal(os.dataFimExecucao);
    const dtValidacao = parseDateToLocal(os.dataValidacao);

    return (
        <div className="main-content">
            <h1>{isSuggestionMode ? 'Analisar Sugestão' : 'Detalhes'} da OS #{os.id}</h1>
            
            {/* --- ⭐⭐ CORREÇÃO APLICADA AQUI ⭐⭐ --- */}
            {/* O Link agora usa navigate(-1) para voltar para a tela anterior (seja Dashboard ou Histórico) */}
            <a 
              href="#" 
              onClick={(e) => { e.preventDefault(); navigate(-1); }}
              style={{ display: 'inline-block', marginBottom: '1rem', color: '#007bff', textDecoration: 'none' }}
            >
                &larr; Voltar
            </a>
            {/* --- Fim da Correção --- */}


            <div className="container" style={{flexDirection: 'column', alignItems: 'flex-start'}}>

                {/* Botão INICIAR (Visível apenas se AGENDADA e usuário tem permissão) */}
                {isScheduledMode && canStartExecution && (
                    <button onClick={handleIniciarOs} style={{backgroundColor: '#007bff', marginBottom: '15px'}}>INICIAR EXECUÇÃO</button>
                )}

                {/* Seção Detalhes da OS */}
                <section className="list-section" style={{width: '100%'}}>
                    <h3>Detalhes da OS - Status: <span className={`status status-${os.status.toLowerCase().replace(/_/g, '-')}`}>{os.status.replace(/_/g, ' ')}</span></h3>
                     <p><strong>Equipamento:</strong> {os.nomeEquipamento} ({os.codigoEquipamento})</p>
                    <p><strong>Técnico:</strong> {os.nomeTecnico || <span style={{color: 'gray', fontStyle:'italic'}}>A definir</span>}</p>
                    <p><strong>{isSuggestionMode ? 'Sugerido p/:' : 'Agendado:'}</strong> {dtAgendamento ? dtAgendamento.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short'}) : 'N/A'}</p>
                    <p><strong>Início Execução:</strong> {dtInicio ? dtInicio.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short'}) : 'Pendente'}</p>
                    <p><strong>Fim Execução:</strong> {dtFim ? dtFim.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short'}) : 'Pendente'}</p>
                    <p><strong>Validação Líder:</strong> {dtValidacao ? `${dtValidacao.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short'})} por ${os.nomeLider || '?'}` : 'Pendente'}</p>
                    {os.observacoesLider && (<p style={{marginTop: '15px', borderLeft: '3px solid #ffc107', paddingLeft: '10px', fontSize: '0.9rem'}}><strong>Obs. Líder:</strong> {os.observacoesLider}</p>)}
                </section>

                {/* Seção de Confirmação de Agendamento */}
                {isSuggestionMode && canConfirmSchedule && (
                    <section className="form-section" style={{width: '100%', backgroundColor: '#f0e6ff'}}>
                        <h3>Confirmar Agendamento</h3>
                        <label>Técnico Responsável:</label>
                        <select value={selectedTecnicoId} onChange={(e) => setSelectedTecnicoId(e.target.value)} required>
                            <option value="">-- Selecione um Técnico --</option>
                            {tecnicos.map(tec => <option key={tec.id} value={tec.id}>{tec.nomeCompleto}</option>)}
                        </select>
                        <label>Data e Hora do Agendamento:</label>
                        <input type="datetime-local" value={selectedDataAgendamento} onChange={(e) => setSelectedDataAgendamento(e.target.value)} required />
                        <button onClick={handleConfirmarAgendamento} style={{backgroundColor: '#6f42c1'}}>Confirmar e Agendar OS</button>
                    </section>
                )}

                {/* Seção Baixa de Peças (Visível se pode modificar) */}
                {canModify && (
                <section className="list-section" style={{width: '100%'}}>
                    <h3>Baixa de Peças de Reposição</h3>
                    <div className="item-adder" style={{alignItems: 'flex-end'}}>
                        <div style={{flex: 2}}>
                            <label>Código da Peça (QR Code)</label>
                            <input value={codigoPecaInput} onChange={e => setCodigoPecaInput(e.target.value)} placeholder="PECA-00001" />
                        </div>
                        <button type="button" onClick={handleBuscaPecaPorCodigo} style={{height: '40px', minWidth:'80px'}}>Buscar</button>
                    </div>
                    {pecaParaBaixa && (
                        <div style={{border: '1px solid #28a745', padding: '15px', borderRadius: '6px', backgroundColor: '#e6ffe6', marginTop: '10px'}}>
                            <h4>Peça: {pecaParaBaixa.nome} (Estoque: {pecaParaBaixa.estoqueAtual})</h4>
                            <div className="item-adder">
                                <label style={{margin: '0', alignSelf:'center'}}>Qtd:</label>
                                <input type="number" value={quantidadeBaixa} onChange={e => setQuantidadeBaixa(e.target.value)} min="1" max={pecaParaBaixa.estoqueAtual} style={{maxWidth: '80px'}} />
                                <button type="button" onClick={handleBaixaPeca} style={{backgroundColor: '#007bff'}}>Confirmar Baixa</button>
                                <button type="button" onClick={() => setPecaParaBaixa(null)} style={{backgroundColor: '#6c757d'}}>Cancelar</button>
                            </div>
                        </div>
                    )}
                </section>
                )}

                {/* Seção Checklist (mantive a estrutura original, mas mostro caso haja resultados salvos mesmo se os.checklist for nulo) */}
                {(os.checklist || (os.resultados && os.resultados.length > 0)) && (
                    <section className="list-section" style={{width: '100%'}}> 
                        <h3>Checklist: {os.checklist?.nome || 'Checklist (resultados)'}</h3>
                        <div className="table-container">
                            <table>
                                <thead><tr><th style={{width: '15%'}}>Status</th><th>Descrição</th><th>Obs. Item</th></tr></thead>
                                <tbody>
                                    {/* Se houver resultados (itens já criados para a OS), renderizamos por eles; senão usamos os.checklist.itens */}
                                    {(os.resultados && os.resultados.length > 0) ? (
                                        os.resultados.map(r => {
                                            const itemId = r.itemTemplateId;
                                            return (
                                            <React.Fragment key={itemId}>
                                                <tr>
                                                    <td>
                                                        <select value={checklistData[itemId]?.status || r.status || 'PENDENTE'} onChange={(e) => handleItemChange(itemId, 'status', e.target.value)} disabled={!canModify} >
                                                            <option value="PENDENTE">Pendente</option> <option value="OK">OK</option> <option value="NAO_CONFORMIDADE">Não OK</option> <option value="NAO_APLICAVEL">N/A</option>
                                                        </select>
                                                    </td>
                                                    <td>{r.descricao || (r.itemTemplate ? r.itemTemplate.descricao : 'Item')}</td>
                                                    <td><input type="text" value={checklistData[itemId]?.observacao || r.observacao || ''} onChange={(e) => handleItemChange(itemId, 'observacao', e.target.value)} placeholder="Obs. item" disabled={!canModify} /></td>
                                                </tr>
                                                {/* Se tivermos peças sugeridas no template (através de r.itemTemplate.pecasSugeridas), mostramos também */}
                                                {r.itemTemplate?.pecasSugeridas && r.itemTemplate.pecasSugeridas.length > 0 && (
                                                <tr style={{backgroundColor: '#f8f9fa'}}><td/><td colSpan="2"><div style={{fontSize: '0.85rem', color: '#555'}}><strong>Peças Sugeridas:</strong><ul style={{margin: '5px 0 0 20px', padding: 0, listStyleType: 'disc'}}>{r.itemTemplate.pecasSugeridas.map(p => (<li key={p.id}>{p.nome} ({p.codigoControle})</li>))}</ul></div></td></tr>
                                                )}
                                            </React.Fragment>
                                            );
                                        })
                                    ) : (
                                        os.checklist?.itens?.map(item => (
                                        <React.Fragment key={item.id}>
                                            <tr>
                                                <td>
                                                    <select value={checklistData[item.id]?.status || 'PENDENTE'} onChange={(e) => handleItemChange(item.id, 'status', e.target.value)} disabled={!canModify} >
                                                        <option value="PENDENTE">Pendente</option> <option value="OK">OK</option> <option value="NAO_CONFORMIDADE">Não OK</option> <option value="NAO_APLICAVEL">N/A</option>
                                                    </select>
                                                </td>
                                                <td>{item.descricao}</td>
                                                <td><input type="text" value={checklistData[item.id]?.observacao || ''} onChange={(e) => handleItemChange(item.id, 'observacao', e.target.value)} placeholder="Obs. item" disabled={!canModify} /></td>
                                            </tr>
                                            {/* Peças Sugeridas */}
                                            {item.pecasSugeridas && item.pecasSugeridas.length > 0 && (
                                            <tr style={{backgroundColor: '#f8f9fa'}}><td/><td colSpan="2"><div style={{fontSize: '0.85rem', color: '#555'}}><strong>Peças Sugeridas:</strong><ul style={{margin: '5px 0 0 20px', padding: 0, listStyleType: 'disc'}}>{item.pecasSugeridas.map(p => (<li key={p.id}>{p.nome} ({p.codigoControle})</li>))}</ul></div></td></tr>
                                            )}
                                        </React.Fragment>
                                        ))
                                    )}
                                    {/* Caso não exista checklist nem resultados */}
                                    {(!(os.checklist?.itens && os.checklist.itens.length > 0) && !(os.resultados && os.resultados.length > 0)) && ( <tr><td colSpan="3" style={{textAlign: 'center'}}>Checklist sem itens.</td></tr> )}
                                </tbody>
                            </table>
                        </div>
                    </section>
                )}

                {/* Seção Peças Consumidas */}
                <section className="list-section" style={{width: '100%'}}>
                    <h3>Peças Consumidas ({pecasConsumidas.length})</h3>
                    <div className="table-container">
                        <table>
                            <thead><tr><th>Nome</th><th>Código</th><th>Qtd.</th><th>Data Baixa</th></tr></thead>
                            <tbody>
                                {pecasConsumidas.map(p => {
                                    const dtBaixa = parseDateToLocal(p.dataBaixa);
                                    return ( <tr key={p.id}><td>{p.nomePeca}</td><td>{p.codigoControle}</td><td>{p.quantidade}</td><td>{dtBaixa ? dtBaixa.toLocaleString('pt-BR') : '—'}</td></tr> );
                                })}
                                {pecasConsumidas.length === 0 && ( <tr><td colSpan="4" style={{textAlign: 'center'}}>Nenhuma peça consumida.</td></tr> )}
                            </tbody>
                        </table>
                    </div>
                </section>

                {/* Seção Anexo de Fotos */}
                <section className="list-section" style={{width: '100%'}}>
                    <h3>Anexo de Fotos ({os.fotosEvidencia?.length || 0})</h3>
                    {/* Permite anexar se NÃO estiver concluída */}
                    {os?.status !== 'CONCLUIDA' && (
                        <div style={{marginBottom: '15px', border: '1px solid #ddd', padding: '15px', borderRadius: '6px'}}>
                            <form onSubmit={handleUploadEvidencia} className="item-adder" style={{marginBottom: '0'}}>
                                <input type="file" id="file-evidencia" onChange={handleFileChange} accept="image/*" />
                                <button type="submit" disabled={!selectedFile} style={{backgroundColor: '#28a745', minWidth: '150px'}}>ANEXAR FOTO</button>
                            </form>
                        </div>
                    )}
                    <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap'}}>
                        {os.fotosEvidencia?.map((fotoUrl, index) => (
                            <a key={index} href={`${BACKEND_BASE_URL}${fotoUrl}`} target="_blank" rel="noopener noreferrer">
                                <img src={`${BACKEND_BASE_URL}${fotoUrl}`} alt={`Evidência ${index + 1}`} style={{width: '100px', height: '100px', objectFit: 'cover', border: '1px solid #ddd', borderRadius: '4px'}} onError={(e) => e.target.style.display='none'} />
                            </a>
                        ))}
                        {(!os.fotosEvidencia || os.fotosEvidencia.length === 0) && <p style={{color: '#6c757d'}}>Nenhuma foto anexada.</p>}
                    </div>
                </section>

                {/* Seção Observações e Ação Final */}
                {/* Só mostra se NÃO for sugestão */}
                {!isSuggestionMode && (
                    <section className="form-section" style={{width: '100%'}}>
                        <h3>Observações e Ação Final</h3>
                        <label>Observações do Técnico:</label>
                        <textarea rows="4" placeholder="Detalhes da execução..." value={observacoesTecnico} onChange={(e) => setObservacoesTecnico(e.target.value)} disabled={!canModify && !isValidationMode}></textarea>
                        <div style={{marginTop: '20px', display: 'flex', gap: '10px'}}>
                            {/* Botão FINALIZAR (Aparece se pode Modificar) */}
                            {canModify && (<button onClick={handleFinalizarManutencao} style={{backgroundColor: '#007bff'}}>FINALIZAR MANUTENÇÃO</button>)}
                            {/* Botões VALIDAR/REPROVAR (Aparece se pode Validar) */}
                            {canValidate && (
                                <>
                                    <button onClick={() => handleValidarReprovar('validar')} style={{backgroundColor: '#28a745'}}>APROVAR VALIDAÇÃO</button>
                                    <button onClick={() => handleValidarReprovar('reprovar')} style={{backgroundColor: '#dc3545'}}>REPROVAR E DEVOLVER</button>
                                </>
                            )}
                        </div>
                    </section>
                )}
            </div>
        </div>
    );
}

export default ExecucaoOS;