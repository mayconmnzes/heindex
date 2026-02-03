// Código Completo
import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import MultiSelect from 'react-select'; // Ainda necessário para ChecklistModule

// Definição das URLs da API
const AREAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/areas`;
const LINHAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/linhas`;
const MODELOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/modelos`;
const EQUIPAMENTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;
const PECAS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/pecas`;
const CHECKLISTS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/checklists`;
const USUARIOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/usuarios`;
const FOTOS_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/fotos/upload`;
const ESTOQUE_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/estoque`;

// Ajuste para o Cloudinary ou Base URL caso precise acessar a raiz
const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Lista de Perfis para o SELECT do Frontend (Deve espelhar o PerfilUsuario.java)
const PERFIS_DISPONIVEIS = ['ADMINISTRADOR', 'LIDER', 'TECNICO'];
const formatPerfilName = (perfil) => {
    if (!perfil) return 'N/A';
    return perfil.charAt(0).toUpperCase() + perfil.slice(1).toLowerCase().replace(/_/g, ' ');
};

// ===============================================
// 1. MÓDULO DE USUÁRIOS (Sem alterações)
// ===============================================
function UsuarioModule({ users, fetchUsers }) {
    const initialState = { nomeCompleto: '', matricula: '', senha: '', perfil: 'TECNICO' };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleCancelEdit = () => { setForm(initialState); setEditingId(null); };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!editingId && (!form.senha || form.senha.length < 4)) {
            alert("Para novos usuários, a senha deve ter pelo menos 4 caracteres.");
            return;
        }

        try {
            if (editingId) {
                const updateData = form.senha ? form : { ...form, senha: null };
                await axios.put(`${USUARIOS_API_URL}/${editingId}`, updateData);
            } else {
                await axios.post(USUARIOS_API_URL, form);
            }
            handleCancelEdit();
            fetchUsers();
        } catch (error) {
            console.error("Erro ao salvar usuário:", error.response?.data || error.message);
            alert(`Erro ao salvar usuário: ${error.response?.data || 'Matrícula já existente ou erro de servidor.'}`);
        }
    };

    const handleEdit = (user) => {
        setEditingId(user.id);
        setForm({ ...user, senha: '' });
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja deletar este usuário?')) {
            try {
                await axios.delete(`${USUARIOS_API_URL}/${id}`);
                fetchUsers();
            } catch (error) {
                console.error("Erro ao deletar usuário:", error);
                alert("Erro ao deletar usuário.");
            }
        }
    };

    return (
        <section className="container">
            <div className="form-section">
                <h2>{editingId ? 'Editar Usuário' : 'Novo Usuário'}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Nome Completo:</label><input name="nomeCompleto" value={form.nomeCompleto} onChange={handleChange} placeholder="Nome Completo" required />
                    <label>Usuário (Login):</label><input name="matricula" value={form.matricula} onChange={handleChange} placeholder="Usuário" required />
                    <label>Senha:</label>
                    <input
                        name="senha"
                        type="password"
                        value={form.senha}
                        onChange={handleChange}
                        placeholder={editingId ? 'Deixe em branco para não alterar' : 'Senha Obrigatória'}
                        required={!editingId}
                    />
                    <label>Perfil de Acesso:</label>
                    <select name="perfil" value={form.perfil} onChange={handleChange} required>
                        {PERFIS_DISPONIVEIS.map(perfil => (
                            <option key={perfil} value={perfil}>{formatPerfilName(perfil)}</option>
                        ))}
                    </select>

                    <button type="submit">{editingId ? 'Salvar Alterações' : 'Adicionar Usuário'}</button>
                    {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>}
                </form>
            </div>
            <div className="list-section">
                <h2>Usuários Cadastrados</h2>
                <div className="table-container">
                    <table>
                        <thead><tr><th>ID</th><th>Nome</th><th>Login</th><th>Perfil</th><th>Ações</th></tr></thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td>{user.nomeCompleto}</td>
                                    <td>{user.matricula}</td>
                                    <td>{formatPerfilName(user.perfil)}</td>
                                    <td>
                                        <button onClick={() => handleEdit(user)}>Editar</button>
                                        <button onClick={() => handleDelete(user.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 2. MÓDULO DE ÁREAS (Sem alterações)
// ===============================================
function AreaModule({ areas, fetchAreas }) {
    const [areaName, setAreaName] = useState('');
    const [editingAreaId, setEditingAreaId] = useState(null);

    const handleAreaSubmit = async (event) => {
        event.preventDefault();
        if (!areaName) return;
        const areaData = { nome: areaName };
        try {
            if (editingAreaId) { await axios.put(`${AREAS_API_URL}/${editingAreaId}`, areaData); }
            else { await axios.post(AREAS_API_URL, areaData); }
            setAreaName('');
            setEditingAreaId(null);
            fetchAreas();
        } catch (error) { console.error("Erro ao salvar área:", error); alert("Erro ao salvar área. Verifique se o nome já existe."); }
    };
    const handleAreaEdit = (area) => { setEditingAreaId(area.id); setAreaName(area.nome); };
    const handleAreaDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja deletar? Isso pode afetar linhas, modelos e equipamentos.')) {
            try { await axios.delete(`${AREAS_API_URL}/${id}`); fetchAreas(); }
            catch (error) { console.error("Erro ao deletar área:", error); alert("Não foi possível deletar a área. Verifique se ela não está em uso por Linhas ou Modelos."); }
        }
    };

    return (
        <section className="container">
            <div className="form-section">
                <h2>{editingAreaId ? 'Editar Área' : 'Nova Área'}</h2>
                <form onSubmit={handleAreaSubmit}>
                    <label>Nome da Área:</label>
                    <input type="text" value={areaName} onChange={(e) => setAreaName(e.target.value)} placeholder="Ex: SMT" required />
                    <button type="submit">{editingAreaId ? 'Salvar Alterações' : 'Adicionar Área'}</button>
                    {editingAreaId && (<button type="button" onClick={() => { setEditingAreaId(null); setAreaName(''); }} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>)}
                </form>
            </div>
            <div className="list-section">
                <h2>Áreas Cadastradas</h2>
                <div className="table-container">
                    <table>
                        <thead><tr><th>ID</th><th>Nome</th><th>Ações</th></tr></thead>
                        <tbody>
                            {areas.map((area) => (
                                <tr key={area.id}>
                                    <td>{area.id}</td>
                                    <td>{area.nome}</td>
                                    <td><button onClick={() => handleAreaEdit(area)}>Editar</button><button onClick={() => handleAreaDelete(area.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 3. MÓDULO DE LINHAS DE PRODUÇÃO (Sem alterações)
// ===============================================
function LinhaModule({ linhas, fetchLinhas, areas }) {
    const initialState = { nome: '', areaId: '' };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [filterAreaId, setFilterAreaId] = useState(''); // Estado para o filtro

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const handleCancelEdit = () => { setForm(initialState); setEditingId(null); };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.nome || !form.areaId) return alert("Nome e Área são obrigatórios.");

        try {
            if (editingId) {
                await axios.put(`${LINHAS_API_URL}/${editingId}`, form);
            } else {
                await axios.post(LINHAS_API_URL, form);
            }
            handleCancelEdit();
            fetchLinhas();
        } catch (error) {
            alert("Erro ao salvar a linha de produção.");
        }
    };

    const handleEdit = (linha) => {
        setEditingId(linha.id);
        setForm({ nome: linha.nome, areaId: linha.areaId.toString() });
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza? Isso pode afetar equipamentos associados.')) {
            try {
                await axios.delete(`${LINHAS_API_URL}/${id}`);
                fetchLinhas();
            } catch (error) {
                alert("Erro ao deletar a linha. Verifique se não há equipamentos nela.");
            }
        }
    };

    const filteredLinhas = useMemo(() => {
        if (!filterAreaId) {
            return linhas; 
        }
        return linhas.filter(linha => linha.areaId.toString() === filterAreaId);
    }, [filterAreaId, linhas]);

    return (
        <section className="container">
            <div className="form-section">
                <h2>{editingId ? 'Editar Linha' : 'Nova Linha de Produção'}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Nome da Linha:</label>
                    <input name="nome" value={form.nome} onChange={handleChange} placeholder="Ex: Linha 1 SMT" required />
                    <label>Pertence à Área:</label>
                    <select name="areaId" value={form.areaId} onChange={handleChange} required>
                        <option value="">-- Selecione uma Área --</option>
                        {areas.map(area => <option key={area.id} value={area.id}>{area.nome}</option>)}
                    </select>
                    <button type="submit">{editingId ? 'Salvar Alterações' : 'Adicionar Linha'}</button>
                    {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>}
                </form>
            </div>
            <div className="list-section">
                <h2>Linhas Cadastradas</h2>
                
                <div className="filter-container" style={{padding: '0 0 15px 0', borderBottom: '1px solid #eee'}}>
                    <div style={{flex: 1}}>
                        <label>Filtrar por Área:</label>
                        <select value={filterAreaId} onChange={e => setFilterAreaId(e.target.value)}>
                            <option value="">Todas as Áreas</option>
                            {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                        </select>
                    </div>
                </div>

                <div className="table-container">
                    <table>
                        <thead><tr><th>ID</th><th>Nome da Linha</th><th>Área</th><th>Ações</th></tr></thead>
                        <tbody>
                            {filteredLinhas.map((linha) => (
                                <tr key={linha.id}>
                                    <td>{linha.id}</td>
                                    <td>{linha.nome}</td>
                                    <td>{linha.areaNome || 'N/A'}</td>
                                    <td>
                                        <button onClick={() => handleEdit(linha)}>Editar</button>
                                        <button onClick={() => handleDelete(linha.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button>
                                    </td>
                                </tr>
                            ))}
                            {filteredLinhas.length === 0 && (
                                <tr>
                                    <td colSpan="4" style={{textAlign: 'center'}}>Nenhuma linha encontrada para esta área.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 4. MÓDULO DE MODELOS DE EQUIPAMENTO (Layout Corrigido)
// ===============================================
function ModeloModule({ modelos, fetchModelos, areas }) {
    const initialState = { nome: '', fabricante: '', areaId: '' };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [filterAreaId, setFilterAreaId] = useState('');

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const handleCancelEdit = () => { setForm(initialState); setEditingId(null); };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.nome || !form.areaId || !form.fabricante) return alert("Nome, Fabricante e Área são obrigatórios.");

        try {
            if (editingId) {
                await axios.put(`${MODELOS_API_URL}/${editingId}`, form);
            } else {
                await axios.post(MODELOS_API_URL, form);
            }
            handleCancelEdit();
            fetchModelos(); // Deve ser fetchAllData ou uma função específica para modelos
        } catch (error) {
            alert("Erro ao salvar o modelo de equipamento.");
        }
    };

    const handleEdit = (modelo) => {
        setEditingId(modelo.id);
        setForm({
            nome: modelo.nome,
            fabricante: modelo.fabricante,
            areaId: modelo.areaId.toString()
        });
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza? Isso pode afetar equipamentos (tags) e peças associadas.')) {
            try {
                await axios.delete(`${MODELOS_API_URL}/${id}`);
                fetchModelos(); // Deve ser fetchAllData ou uma função específica para modelos
            } catch (error) {
                alert("Erro ao deletar o modelo. Verifique se não há equipamentos (tags) ou peças associadas a ele.");
            }
        }
    };

    const filteredModelos = useMemo(() => {
        if (!filterAreaId) return modelos;
        return modelos.filter(modelo => modelo.areaId.toString() === filterAreaId);
    }, [filterAreaId, modelos]);

    return (
        // --- ALTERAÇÃO AQUI: Removido className="container" ---
        <section style={{ marginBottom: '2rem' }}>
        {/* --- FIM DA ALTERAÇÃO --- */}
            <div className="form-section" style={{marginBottom: '2rem'}}> {/* Adiciona margem inferior ao form */}
                <h2>{editingId ? 'Editar Modelo (Tipo)' : 'Novo Modelo de Equipamento (Tipo)'}</h2>
                <form onSubmit={handleSubmit}>
                    {/* Conteúdo do formulário permanece o mesmo... */}
                    <label>Nome do Modelo (Ex: SM471):</label>
                    <input name="nome" value={form.nome} onChange={handleChange} placeholder="Ex: SM471" required />

                    <label>Fabricante (Ex: Samsung):</label>
                    <input name="fabricante" value={form.fabricante} onChange={handleChange} placeholder="Ex: Samsung" required />

                    <label>Pertence à Área:</label>
                    <select name="areaId" value={form.areaId} onChange={handleChange} required>
                        <option value="">-- Selecione uma Área --</option>
                        {areas.map(area => <option key={area.id} value={area.id}>{area.nome}</option>)}
                    </select>
                    <button type="submit">{editingId ? 'Salvar Alterações' : 'Adicionar Modelo'}</button>
                    {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>}
                </form>
            </div>
            <div className="list-section">
                <h2>Modelos Cadastrados</h2>
                 {/* Conteúdo da lista permanece o mesmo... */}
                <div className="filter-container" style={{padding: '0 0 15px 0', borderBottom: '1px solid #eee'}}>
                    <div style={{flex: 1}}>
                        <label>Filtrar por Área:</label>
                        <select value={filterAreaId} onChange={e => setFilterAreaId(e.target.value)}>
                            <option value="">Todas as Áreas</option>
                            {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                        </select>
                    </div>
                </div>

                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome do Modelo</th>
                                <th>Fabricante</th>
                                <th>Área</th>
                                <th>Qtd. Tags</th>
                                <th>Qtd. Peças</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredModelos.map((modelo) => (
                                <tr key={modelo.id}>
                                    <td>{modelo.id}</td>
                                    <td>{modelo.nome}</td>
                                    <td>{modelo.fabricante}</td>
                                    <td>{modelo.areaNome || 'N/A'}</td>
                                    <td>{modelo.quantidadeInstancias}</td>
                                    <td>{modelo.quantidadePecasAssociadas}</td>
                                    <td>
                                        <button onClick={() => handleEdit(modelo)}>Editar</button>
                                        <button onClick={() => handleDelete(modelo.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button>
                                    </td>
                                </tr>
                            ))}
                            {filteredModelos.length === 0 && (
                                <tr>
                                    <td colSpan="7" style={{textAlign: 'center'}}>Nenhum modelo encontrado para esta área.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 5. MÓDULO DE CHECKLISTS (Sem alterações)
// ===============================================
function ChecklistModule({ checklists, fetchChecklists, areas, pecas }) {
    const [nome, setNome] = useState('');
    const [areaId, setAreaId] = useState('');
    const [currentItem, setCurrentItem] = useState('');
    const [itens, setItens] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [filterAreaId, setFilterAreaId] = useState('');

    const pecasOptions = pecas.map(peca => ({
        value: peca.id,
        label: `${peca.nome} (${peca.codigoControle})`
    }));

    const handleAddItem = (e) => {
        e.preventDefault();
        if (!currentItem.trim()) return;
        setItens([...itens, { descricao: currentItem.trim() }]);
        setCurrentItem('');
    };

    const handleRemoveItem = (indexToRemove) => {
        setItens(itens.filter((_, index) => index !== indexToRemove));
    };

    const handleCancelEdit = () => {
        setNome('');
        setAreaId('');
        setItens([]);
        setCurrentItem('');
        setEditingId(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!nome || !areaId || itens.length === 0) {
            alert("Nome, Área e pelo menos um Passo são obrigatórios.");
            return;
        }

        const checklistData = {
            nome: nome,
            areaId: Number(areaId),
            itens: itens // Envia a lista de objetos
        };

        try {
            if (editingId) {
                await axios.put(`${CHECKLISTS_API_URL}/${editingId}`, checklistData);
            } else {
                await axios.post(CHECKLISTS_API_URL, checklistData);
            }
            handleCancelEdit();
            fetchChecklists();
        } catch (error) {
            console.error("Erro ao salvar checklist:", error.response?.data || error.message);
            alert(`Erro ao salvar checklist: ${error.response?.data?.error || error.response?.data || 'Verifique o console.'}`);
        }
    };

    const handleChecklistEdit = (checklist) => {
        setEditingId(checklist.id);
        setNome(checklist.nome);
        if (checklist.areaId) {
             setAreaId(checklist.areaId.toString());
        } else {
            const areaCorrespondente = areas.find(a => a.nome === checklist.areaNome);
            if (areaCorrespondente) {
                setAreaId(areaCorrespondente.id.toString());
            } else {
                setAreaId('');
            }
        }
        setItens(checklist.itens || []);
    };

    const handleChecklistDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja deletar este modelo? Isso pode afetar equipamentos associados.')) {
            try {
                await axios.delete(`${CHECKLISTS_API_URL}/${id}`);
                fetchChecklists();
            } catch (error) {
                console.error("Erro ao deletar checklist:", error);
                alert("Erro ao deletar modelo. Verifique se não está em uso.");
            }
        }
    };

    const filteredChecklists = useMemo(() => {
        if (!filterAreaId) return checklists;
        
        const selectedArea = areas.find(a => a.id.toString() === filterAreaId);
        
        if (selectedArea) {
            return checklists.filter(cl => cl.areaNome === selectedArea.nome);
        }
        
        return checklists;
    }, [filterAreaId, checklists, areas]);


    return (
        <section className="container">
            <div className="form-section">
                <h2>{editingId ? 'Editar Modelo' : 'Novo Modelo de Checklist'}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Nome do Modelo:</label>
                    <input value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Ex: Checklist Mensal Prensa" required />
                    <label>Área de Aplicação:</label>
                    <select value={areaId} onChange={(e) => setAreaId(e.target.value)} required>
                        <option value="">-- Selecione a Área --</option>
                        {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                    </select>
                    <fieldset className="form-fieldset" style={{ marginTop: '20px' }}>
                        <legend>Passos do Checklist</legend>
                        <label>Novo Passo:</label>
                        <div className="item-adder">
                            <input
                                type="text"
                                value={currentItem}
                                onChange={(e) => setCurrentItem(e.target.value)}
                                placeholder="Ex: Verificar nível do óleo"
                            />
                            <button type="button" onClick={handleAddItem} style={{ minWidth: '100px' }}>Adicionar</button>
                        </div>
                        <p style={{ fontSize: '0.8rem', color: '#6c757d', marginTop: '15px' }}>Passos Adicionados ({itens.length}):</p>
                        <ul className="checklist-preview">
                            {itens.map((item, index) => (
                                <li key={item.id || index}>
                                    <span>{index + 1}. {item.descricao}</span>
                                    <button type="button" onClick={() => handleRemoveItem(index)}>X</button>
                                </li>
                            ))}
                            {itens.length === 0 && <li style={{ justifyContent: 'center', color: '#6c757d' }}>Nenhum passo adicionado.</li>}
                        </ul>
                    </fieldset>
                    <button type="submit" style={{ marginTop: '20px' }}>{editingId ? 'Salvar Alterações' : 'Salvar Modelo de Checklist'}</button>
                    {editingId && (<button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>)}
                </form>
            </div>
            <div className="list-section">
                <h2>Modelos Cadastrados</h2>
                <div className="filter-container" style={{ padding: '0 0 15px 0', borderBottom: '1px solid #eee' }}>
                    <div style={{ flex: 1 }}>
                        <label>Filtrar por Área:</label>
                        <select value={filterAreaId} onChange={e => setFilterAreaId(e.target.value)}>
                            <option value="">Todas as Áreas</option>
                            {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                        </select>
                    </div>
                </div>
                <div className="table-container">
                    <table>
                        <thead><tr><th>ID</th><th>Modelo</th><th>Área</th><th>Nº Passos</th><th>Ações</th></tr></thead>
                        <tbody>
                            {filteredChecklists.map(checklist => (
                                <tr key={checklist.id}>
                                    <td>{checklist.id}</td>
                                    <td>{checklist.nome}</td>
                                    <td>{checklist.areaNome}</td>
                                    <td>{checklist.itens?.length || 0}</td>
                                    <td>
                                        <button onClick={() => handleChecklistEdit(checklist)}>Editar</button>
                                        <button onClick={() => handleChecklistDelete(checklist.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button>
                                    </td>
                                </tr>
                            ))}
                            {filteredChecklists.length === 0 && (<tr><td colSpan="5" style={{ textAlign: 'center' }}>Nenhum checklist encontrado para esta área.</td></tr>)}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 6. MÓDULO DE EQUIPAMENTOS (INSTÂNCIAS/TAGS) (Layout, Reordenação e Correção Map)
// ===============================================
function EquipamentoModule({ equipamentos, fetchEquipamentos, areas, linhas, checklists, modelos }) {
    // Estados do formulário
    const initialState = {
        nome: '', codigo: '', criticidade: 'C',
        linhaId: '', modeloId: '', checklistId: '',
        frequenciaPreventiva: '', dataUltimaPreventiva: ''
    };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [selectedAreaId, setSelectedAreaId] = useState(''); // Filtro do FORMULÁRIO

    // Estados dos filtros da LISTA
    const [filterListAreaId, setFilterListAreaId] = useState('');
    const [filterListLinhaId, setFilterListLinhaId] = useState('');

    // Estado para ordem de exibição da lista
    const [displayedEquipamentos, setDisplayedEquipamentos] = useState([]);

    // Funções do formulário
    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const handleCancelEdit = () => { setForm(initialState); setSelectedAreaId(''); setEditingId(null); };

    const handleSubmit = async (e) => {
        e.preventDefault();
        // Validação
        if (!form.linhaId || !form.modeloId) {
            return alert("Selecione uma Área, um Modelo e uma Linha de produção.");
        }

        const requestData = {
            nome: form.nome, // Esta é a TAG
            codigo: form.codigo,
            criticidade: form.criticidade,
            linhaId: Number(form.linhaId),
            modeloId: Number(form.modeloId), // ID do Modelo (Tipo)
            checklistId: form.checklistId ? Number(form.checklistId) : null,
            frequenciaPreventiva: form.frequenciaPreventiva || null,
            dataUltimaPreventiva: form.dataUltimaPreventiva || null
        };

        try {
            if (editingId) { await axios.put(`${EQUIPAMENTOS_API_URL}/${editingId}`, requestData); }
            else { await axios.post(EQUIPAMENTOS_API_URL, requestData); }
            handleCancelEdit();
            fetchEquipamentos(); // Ou fetchAllData se for a função principal
        } catch (error) { alert(`Erro ao salvar equipamento (tag): ${error.response?.data?.message || error.response?.data || error.message}`); }
    };

    const handleEdit = (equip) => {
        const linha = linhas.find(l => l.nome === equip.nomeLinha && l.areaNome === equip.nomeArea);

        if (linha && equip.modeloId) {
            setSelectedAreaId(linha.areaId.toString());
            setForm({
                nome: equip.nome, // Tag
                codigo: equip.codigo,
                criticidade: equip.criticidade || 'C',
                linhaId: linha.id.toString(),
                modeloId: equip.modeloId.toString(),
                checklistId: equip.checklistId || '',
                frequenciaPreventiva: equip.frequenciaPreventiva || '',
                dataUltimaPreventiva: equip.dataUltimaPreventiva || ''
            });
            setEditingId(equip.id);
        } else {
            console.error("Erro ao carregar dados para edição:", { equip, linha });
            alert("Erro ao carregar dados para edição. Verifique se a Linha e o Modelo associados ainda existem.");
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja deletar esta Tag? Isso pode afetar OS e histórico.')) {
            try {
                await axios.delete(`${EQUIPAMENTOS_API_URL}/${id}`);
                fetchEquipamentos(); // Ou fetchAllData
            } catch (error) {
                const errorMsg = error.response?.data?.message || error.response?.data || 'Verifique se não está em uso.';
                alert(`Erro ao deletar equipamento (tag): ${errorMsg}`);
            }
        }
    };

    // Lógica de filtragem dos dropdowns do formulário
    const filteredLinhasForm = useMemo(() => {
        if (!selectedAreaId) return [];
        if (!Array.isArray(linhas)) return []; // Segurança extra
        return linhas.filter(l => l.areaId.toString() === selectedAreaId);
    }, [selectedAreaId, linhas]);

    const filteredModelosForm = useMemo(() => {
        if (!selectedAreaId) return [];
        if (!Array.isArray(modelos)) { // Segurança extra
             console.error("EquipamentoModule: Prop 'modelos' não é um array!", modelos);
             return [];
        }
        return modelos.filter(m => m.areaId.toString() === selectedAreaId);
    }, [selectedAreaId, modelos]);

    // Lógica de filtragem dos dropdowns da lista
    const filterListLinhasOptions = useMemo(() => {
        if (!filterListAreaId) return [];
        if (!Array.isArray(linhas)) return []; // Segurança extra
        return linhas.filter(l => l.areaId.toString() === filterListAreaId);
    }, [filterListAreaId, linhas]);

    // Lógica para obter a lista FILTRADA (base para a exibição)
    const filteredEquipamentosList = useMemo(() => {
        // Segurança extra: Garante que equipamentos, areas, linhas são arrays
         if (!Array.isArray(equipamentos) || !Array.isArray(areas) || !Array.isArray(linhas)) {
            return [];
         }
        return equipamentos.filter(equip => {
            // Tratamento para caso area/linha não sejam encontrados
            const areaDoFiltro = areas.find(a => a.id.toString() === filterListAreaId);
            const linhaDoFiltro = linhas.find(l => l.id.toString() === filterListLinhaId);
            const areaNomeSelecionada = areaDoFiltro?.nome;
            const linhaNomeSelecionada = linhaDoFiltro?.nome;

            const matchArea = !filterListAreaId || equip.nomeArea === areaNomeSelecionada;
            const matchLinha = !filterListLinhaId || equip.nomeLinha === linhaNomeSelecionada;
            return matchArea && matchLinha;
        });
    }, [filterListAreaId, filterListLinhaId, equipamentos, areas, linhas]);

    // Efeito que atualiza a lista de exibição quando os filtros mudam
    useEffect(() => {
        setDisplayedEquipamentos(filteredEquipamentosList);
    }, [filteredEquipamentosList]);

    // Funções para mover itens na lista de exibição
    const moveEquipamento = (index, direction) => {
        const newIndex = index + direction;
        if (newIndex < 0 || newIndex >= displayedEquipamentos.length) {
            return;
        }
        const newList = [...displayedEquipamentos];
        // Troca eficiente de elementos no array
        [newList[index], newList[newIndex]] = [newList[newIndex], newList[index]];
        setDisplayedEquipamentos(newList);
    };

    return (
        // Removido className="container" para layout vertical
        <section style={{ marginBottom: '2rem' }}>
            {/* --- Formulário --- */}
            <div className="form-section" style={{marginBottom: '2rem'}}>
                <h2>{editingId ? 'Editar Equipamento (Tag)' : 'Novo Equipamento (Tag)'}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-grid-2">
                        <fieldset className="form-fieldset">
                            <legend>Dados e Localização</legend>
                            <div className="form-sub-grid">
                                <label>Área:</label>
                                <select value={selectedAreaId} onChange={e => { setSelectedAreaId(e.target.value); setForm({ ...form, linhaId: '', modeloId: '' }); }} required>
                                    <option value="">-- 1. Selecione a Área --</option>
                                    {/* Segurança extra com Array.isArray */}
                                    {Array.isArray(areas) && areas.map(area => <option key={area.id} value={area.id}>{area.nome}</option>)}
                                </select>

                                <label>Modelo (Tipo):</label>
                                <select name="modeloId" value={form.modeloId} onChange={handleChange} required disabled={!selectedAreaId}>
                                    <option value="">-- 2. Selecione o Modelo --</option>
                                    {/* Correção do erro .map */}
                                    {Array.isArray(filteredModelosForm) && filteredModelosForm.map(modelo => (
                                        <option key={modelo.id} value={modelo.id}>{modelo.nome} ({modelo.fabricante})</option>
                                    ))}
                                </select>

                                <label>Linha de Produção:</label>
                                <select name="linhaId" value={form.linhaId} onChange={handleChange} required disabled={!selectedAreaId}>
                                    <option value="">-- 3. Selecione a Linha --</option>
                                    {/* Segurança extra com Array.isArray */}
                                    {Array.isArray(filteredLinhasForm) && filteredLinhasForm.map(linha => <option key={linha.id} value={linha.id}>{linha.nome}</option>)}
                                </select>

                                <label>Nome da Tag (Ex: SM471A):</label>
                                <input name="nome" value={form.nome} onChange={handleChange} placeholder="Identificação única (Tag)" required />

                                <label>Código (Patrimônio):</label>
                                <input name="codigo" value={form.codigo} onChange={handleChange} placeholder="Opcional" />

                                <label>Criticidade:</label>
                                <select name="criticidade" value={form.criticidade} onChange={handleChange} required>
                                    <option value="C">C (Baixa)</option>
                                    <option value="B">B (Média)</option>
                                    <option value="A">A (Alta)</option>
                                </select>
                            </div>
                        </fieldset>

                        <fieldset className="form-fieldset">
                            <legend>Manutenção e Associações</legend>
                            <div className="form-sub-grid">
                                <label>Frequência da Preventiva:</label>
                                <select name="frequenciaPreventiva" value={form.frequenciaPreventiva} onChange={handleChange}>
                                    <option value="">Não se aplica</option>
                                    <option value="QUINZENAL">Quinzenal</option>
                                    <option value="MENSAL">Mensal</option>
                                    <option value="TRIMESTRAL">Trimestral</option>
                                    <option value="SEMESTRAL">Semestral</option>
                                    <option value="ANUAL">Anual</option>
                                </select>
                                <label>Data da Última Preventiva:</label>
                                <input name="dataUltimaPreventiva" type="date" value={form.dataUltimaPreventiva} onChange={handleChange} />
                                <label>Checklist Padrão:</label>
                                <select name="checklistId" value={form.checklistId || ''} onChange={handleChange}>
                                    <option value="">Nenhum Checklist Padrão</option>
                                     {/* Segurança extra com Array.isArray */}
                                    {Array.isArray(checklists) && checklists.map(cl => <option key={cl.id} value={cl.id}>{cl.nome}</option>)}
                                </select>
                            </div>
                        </fieldset>
                    </div>
                    {/* Botões do formulário */}
                    <div style={{display: 'flex', gap: '10px', marginTop: '20px'}}>
                        <button type="submit">{editingId ? 'Salvar Alterações' : 'Adicionar Equipamento (Tag)'}</button>
                        {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar Edição</button>}
                    </div>
                </form>
            </div>

            {/* --- Lista de Equipamentos --- */}
            <div className="list-section">
                <h2>Equipamentos Cadastrados (Tags)</h2>
                {/* Filtros da lista */}
                <div className="filter-container" style={{padding: '0 0 15px 0', borderBottom: '1px solid #eee', display: 'flex', gap: '1rem'}}>
                     <div style={{flex: 1}}>
                        <label>Filtrar por Área:</label>
                        <select value={filterListAreaId} onChange={e => { setFilterListAreaId(e.target.value); setFilterListLinhaId(''); }}>
                            <option value="">Todas as Áreas</option>
                             {/* Segurança extra com Array.isArray */}
                            {Array.isArray(areas) && areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                        </select>
                    </div>
                     <div style={{flex: 1}}>
                        <label>Filtrar por Linha:</label>
                        <select value={filterListLinhaId} onChange={e => setFilterListLinhaId(e.target.value)} disabled={!filterListAreaId}>
                            <option value="">Todas as Linhas da Área</option>
                             {/* Segurança extra com Array.isArray */}
                            {Array.isArray(filterListLinhasOptions) && filterListLinhasOptions.map(linha => (<option key={linha.id} value={linha.id}>{linha.nome}</option>))}
                        </select>
                    </div>
                </div>
                {/* Tabela */}
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th style={{ width: '80px' }}>Ordem</th>
                                <th>Nome (Tag)</th>
                                <th>Modelo (Tipo)</th>
                                <th>Fabricante</th>
                                <th>Linha</th>
                                <th>Área</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                             {/* Renderiza a lista reordenável */}
                            {Array.isArray(displayedEquipamentos) && displayedEquipamentos.map((equip, index) => (
                                <tr key={equip.id}>
                                    {/* Botões de Ordenação */}
                                    <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                                        <button
                                            onClick={() => moveEquipamento(index, -1)}
                                            disabled={index === 0}
                                            style={{ display: 'block', width: '30px', margin: '0 auto 2px', padding: '2px', fontSize: '0.8rem', lineHeight: '1' }}
                                            title="Mover para cima"
                                        > ▲ </button>
                                        <button
                                            onClick={() => moveEquipamento(index, 1)}
                                            disabled={index === displayedEquipamentos.length - 1}
                                            style={{ display: 'block', width: '30px', margin: '2px auto 0', padding: '2px', fontSize: '0.8rem', lineHeight: '1' }}
                                            title="Mover para baixo"
                                        > ▼ </button>
                                    </td>
                                    {/* Dados do Equipamento */}
                                    <td>{equip.nome}</td>
                                    <td>{equip.nomeModelo || 'N/A'}</td>
                                    <td>{equip.fabricante || 'N/A'}</td>
                                    <td>{equip.nomeLinha || 'N/A'}</td>
                                    <td>{equip.nomeArea || 'N/A'}</td>
                                    {/* Botões de Ação */}
                                    <td>
                                        <button onClick={() => handleEdit(equip)}>Editar</button>
                                        <button onClick={() => handleDelete(equip.id)} style={{ backgroundColor: '#dc3545', marginTop: '5px' }}>Deletar</button>
                                    </td>
                                </tr>
                            ))}
                            {/* Mensagem se a lista estiver vazia */}
                            {(!Array.isArray(displayedEquipamentos) || displayedEquipamentos.length === 0) && (
                                <tr><td colSpan="7" style={{textAlign: 'center'}}>Nenhum equipamento encontrado para os filtros selecionados.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}

// ===============================================
// 7. MÓDULO DE PEÇAS / ESTOQUE (Múltiplas Associações, Área e Foto na Tabela)
// ===============================================
function PecaModule({ pecas, fetchAllData, areas, modelos }) {
    const initialState = {
        nome: '', codigoRequisicao: '', descricaoTecnica: '', estoqueAtual: 0, estoqueMinimo: 0,
        fotos: [], localizacaoPrateleira: '', aplicacao: '',
        associacoes: [{ areaId: '', modeloEquipamentoId: '' }]
    };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [quantidadeMovimentacao, setQuantidadeMovimentacao] = useState(0);
    const [selectedFile, setSelectedFile] = useState(null);
    const [filterListAreaId, setFilterListAreaId] = useState('');
    const [filterListModeloId, setFilterListModeloId] = useState('');

    // --- Handlers de Associação Múltipla ---
    const addAssociacao = () => {
        setForm(prev => ({
            ...prev,
            associacoes: [...prev.associacoes, { areaId: '', modeloEquipamentoId: '' }]
        }));
    };

    const removeAssociacao = (index) => {
        const novas = [...form.associacoes];
        novas.splice(index, 1);
        setForm(prev => ({ ...prev, associacoes: novas }));
    };

    const handleAssociacaoChange = (index, field, value) => {
        const novas = [...form.associacoes];
        novas[index][field] = value;
        if (field === 'areaId') {
            novas[index].modeloEquipamentoId = '';
        }
        setForm(prev => ({ ...prev, associacoes: novas }));
    };

    // --- Handlers Gerais ---
    const handleChange = (e) => { const { name, value } = e.target; setForm({ ...form, [name]: value }); };
    const handleCancelEdit = () => { setForm(initialState); setEditingId(null); setQuantidadeMovimentacao(0); setSelectedFile(null); }; // ✅ Reinicia selectedFile
    const handleFileChange = (e) => { setSelectedFile(e.target.files[0]); };

    // --- CORREÇÃO: Comentado upload de fotos isolado, agora vai via Multipart ---
    /*
    const handleUploadFoto = async (e) => {
        e.preventDefault();
        if (!selectedFile) return alert("Selecione um arquivo de imagem.");
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            const res = await axios.post(FOTOS_API_URL, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            const fotoUrl = res.data;
            setForm(prev => ({ ...prev, fotos: [...prev.fotos, fotoUrl] }));
            setSelectedFile(null);
            if (document.getElementById('file-upload-peca')) document.getElementById('file-upload-peca').value = '';
        } catch (error) {
            console.error("Erro no upload:", error.response?.data || error.message);
            alert("Falha ao fazer upload da foto.");
        }
    };
    */

    const handleRemoveFoto = (urlToRemove) => {
        setForm(prev => ({ ...prev, fotos: prev.fotos.filter(url => url !== urlToRemove) }));
    };

    // ✅ NOVO: Função corrigida para envio Multipart e integração Cloudinary
    const handleSubmit = async (e) => {
        e.preventDefault();
        const modelosIds = form.associacoes
            .map(a => Number(a.modeloEquipamentoId))
            .filter(id => id > 0);

        // --- CORREÇÃO: Estrutura para Multipart Form Data ---
        const formData = new FormData();
        const requestData = {
            nome: form.nome,
            codigoRequisicao: form.codigoRequisicao,
            descricaoTecnica: form.descricaoTecnica,
            estoqueAtual: Number(form.estoqueAtual) || 0,
            estoqueMinimo: Number(form.estoqueMinimo) || 0,
            localizacaoPrateleira: form.localizacaoPrateleira,
            aplicacao: form.aplicacao,
            modelosIds: modelosIds,
            modeloEquipamentoId: modelosIds[0] || null
        };

        // Adiciona o objeto como um Blob JSON e o arquivo ao FormData
        formData.append('peca', new Blob([JSON.stringify(requestData)], { type: "application/json" }));
        if (selectedFile) {
            formData.append('arquivo', selectedFile);
        }

        try {
            // --- CORREÇÃO: Comentado envio antigo JSON-only ---
            /*
            if (editingId) { await axios.put(`${PECAS_API_URL}/${editingId}`, requestData); }
            else { await axios.post(PECAS_API_URL, requestData); }
            */
            
            if (editingId) {
                // Edição continua via JSON simples por enquanto, ou use Multipart se mudar no Java
                await axios.put(`${PECAS_API_URL}/${editingId}`, requestData);
            } else {
                // Novo cadastro usa Multipart para Cloudinary
                await axios.post(PECAS_API_URL, formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
            }
            handleCancelEdit();
            fetchAllData();
        } catch (error) {
            alert(`Erro ao salvar peça: ${error.response?.data?.message || error.response?.data || error.message}`);
        }
    };

    const handleEdit = (peca) => {
        setEditingId(peca.id);
        let associacoesIniciais = [{ areaId: '', modeloEquipamentoId: '' }];

        if (peca.modelosIds && peca.modelosIds.length > 0) {
            associacoesIniciais = peca.modelosIds.map(mId => {
                const mod = modelos.find(m => m.id === mId);
                return { areaId: mod?.areaId.toString() || '', modeloEquipamentoId: mId.toString() };
            });
        } else if (peca.modeloEquipamentoId) {
            const mod = modelos.find(m => m.id === peca.modeloEquipamentoId);
            associacoesIniciais = [{ areaId: mod?.areaId.toString() || '', modeloEquipamentoId: peca.modeloEquipamentoId.toString() }];
        }

        setForm({
            nome: peca.nome,
            codigoRequisicao: peca.codigoRequisicao || '',
            descricaoTecnica: peca.descricaoTecnica || '',
            estoqueAtual: peca.estoqueAtual,
            estoqueMinimo: peca.estoqueMinimo,
            fotos: peca.fotos || [],
            fotoUrl: peca.fotoUrl || '', // ✅ Mantém o campo da URL Cloudinary
            localizacaoPrateleira: peca.localizacaoPrateleira || '',
            aplicacao: peca.aplicacao || '',
            associacoes: associacoesIniciais
        });
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza? Verifique se a peça não possui histórico.')) {
            try {
                await axios.delete(`${PECAS_API_URL}/${id}`);
                fetchAllData();
            } catch (error) {
                console.error("Erro ao deletar peça:", error.response?.data || error.message);
                alert(`Erro ao deletar peça: ${error.response?.data?.message || error.response?.data || 'Verifique o console.'}`);
            }
        }
    };

    const handleMovimentacaoEstoque = async (type) => {
        if (!editingId || !quantidadeMovimentacao || Number(quantidadeMovimentacao) <= 0) {
            return alert("Selecione uma peça em edição e digite uma quantidade válida maior que zero.");
        }
        const url = `${PECAS_API_URL}/${editingId}/${type}`;
        const data = { quantidade: Number(quantidadeMovimentacao) };
        try {
            await axios.post(url, data);
            alert(`Movimentação de ${type.toUpperCase()} registrada com sucesso!`);
            setForm(prevForm => ({
                ...prevForm,
                estoqueAtual: type === 'entrada'
                    ? (prevForm.estoqueAtual || 0) + Number(quantidadeMovimentacao)
                    : (prevForm.estoqueAtual || 0) - Number(quantidadeMovimentacao)
            }));
            setQuantidadeMovimentacao(0);
            fetchAllData();
        } catch (error) {
            console.error(`Falha ao registrar ${type}:`, error.response?.data || error.message);
            alert(`Falha ao registrar ${type}: ${error.response?.data?.message || error.response?.data || 'Erro de rede'}`);
        }
    };

    const handlePrintEtiqueta = (peca) => {
        alert(`Simulando impressão da etiqueta para ${peca.nome} (${peca.codigoControle})`);
    };

    const filterListModelosOptions = useMemo(() => {
        if (!filterListAreaId) return [];
        return modelos.filter(m => m.areaId.toString() === filterListAreaId);
    }, [filterListAreaId, modelos]);

    // ✅ helpers para padronizar ids e não duplicar
    const toNum = (v) => {
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    };
    const uniqueNums = (arr) => {
        const set = new Set();
        (arr || []).forEach(v => {
            const n = toNum(v);
            if (n) set.add(n);
        });
        return Array.from(set);
    };

    const filteredPecasList = useMemo(() => {
        if (!Array.isArray(pecas) || !Array.isArray(modelos) || !Array.isArray(areas)) return [];

        return pecas.filter(peca => {
            // ✅ CORREÇÃO: sempre considerar múltiplos modelos
            const idsModelos = uniqueNums([
                ...(Array.isArray(peca.modelosIds) ? peca.modelosIds : []),
                peca.modeloEquipamentoId
            ]);

            // ✅ CORREÇÃO: filtro por área baseado nos modelos (não em peca.areaId)
            const idsAreas = uniqueNums(idsModelos.map(id => modelos.find(m => m.id === id)?.areaId));

            const matchArea = !filterListAreaId || idsAreas.includes(Number(filterListAreaId));

            const matchModelo = !filterListModeloId || idsModelos.includes(Number(filterListModeloId));

            return matchArea && matchModelo;
        });
    }, [filterListAreaId, filterListModeloId, pecas, modelos, areas]);

    return (
        <section style={{ marginBottom: '2rem' }}>
            <div className="form-section" style={{ marginBottom: '2rem' }}>
                <h2>{editingId ? 'Editar Peça' : 'Nova Peça de Reposição'}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-grid-2">
                        <fieldset className="form-fieldset">
                            <legend>Dados e Estoque</legend>
                            <div className="form-sub-grid">
                                <label>Nome:</label><input name="nome" value={form.nome} onChange={handleChange} required />
                                <label>Cód. Requisição:</label><input name="codigoRequisicao" value={form.codigoRequisicao} onChange={handleChange} />
                                <label>Estoque Atual:</label><input name="estoqueAtual" type="number" value={form.estoqueAtual} onChange={handleChange} required min="0" />
                                <label>Estoque Mínimo:</label><input name="estoqueMinimo" type="number" value={form.estoqueMinimo} onChange={handleChange} required min="0" />
                                <label>Localização (Prat.):</label><input name="localizacaoPrateleira" value={form.localizacaoPrateleira} onChange={handleChange} />
                                <label>Aplicação:</label><input name="aplicacao" value={form.aplicacao} onChange={handleChange} />
                            </div>
                            <label style={{ marginTop: '15px' }}>Descrição Técnica:</label>
                            <textarea name="descricaoTecnica" value={form.descricaoTecnica} onChange={handleChange}></textarea>
                        </fieldset>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                            <fieldset className="form-fieldset">
                                <legend>Associações (Equipamentos Compatíveis)</legend>
                                {form.associacoes.map((assoc, index) => (
                                    <div key={index} style={{
                                        display: 'grid',
                                        gridTemplateColumns: '1fr 1fr auto',
                                        gap: '10px',
                                        alignItems: 'end',
                                        marginBottom: '10px',
                                        padding: '10px',
                                        background: '#f8f9fa',
                                        borderRadius: '4px'
                                    }}>
                                        <div>
                                            <label style={{ fontSize: '0.7rem' }}>Área:</label>
                                            <select
                                                value={assoc.areaId}
                                                onChange={e => handleAssociacaoChange(index, 'areaId', e.target.value)}
                                            >
                                                <option value="">Área</option>
                                                {areas.map(area => <option key={area.id} value={area.id}>{area.nome}</option>)}
                                            </select>
                                        </div>
                                        <div>
                                            <label style={{ fontSize: '0.7rem' }}>Modelo:</label>
                                            <select
                                                value={assoc.modeloEquipamentoId}
                                                onChange={e => handleAssociacaoChange(index, 'modeloEquipamentoId', e.target.value)}
                                                disabled={!assoc.areaId}
                                            >
                                                <option value="">Modelo</option>
                                                {modelos.filter(m => m.areaId.toString() === assoc.areaId).map(m => (
                                                    <option key={m.id} value={m.id}>{m.nome} ({m.fabricante})</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div style={{ display: 'flex', gap: '5px' }}>
                                            {index === form.associacoes.length - 1 && (
                                                <button type="button" onClick={addAssociacao} style={{ backgroundColor: '#28a745', padding: '5px 10px', color: 'white' }}>+</button>
                                            )}
                                            {form.associacoes.length > 1 && (
                                                <button type="button" onClick={() => removeAssociacao(index)} style={{ backgroundColor: '#dc3545', padding: '5px 10px', color: 'white' }}>x</button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </fieldset>

                            <fieldset className="form-fieldset">
                                <legend>Fotos e Mídia</legend>
                                <div className="item-adder">
                                    <input type="file" id="file-upload-peca" onChange={handleFileChange} accept="image/*" />
                                    {/* --- CORREÇÃO: Comentado botão upload, agora integrado no Salvar --- */}
                                    {/* <button type="button" onClick={handleUploadFoto} disabled={!selectedFile} style={{ backgroundColor: '#6c757d', minWidth: '100px' }}>Upload</button> */}
                                </div>
                                <p style={{ fontSize: '0.8rem', color: '#6c757d' }}>URLs Salvas ({form.fotos.length}):</p>
                                <ul className="checklist-preview" style={{ maxHeight: '100px', overflowY: 'auto' }}>
                                    {form.fotos.map((url, index) => (
                                        <li key={index} style={{ fontSize: '0.8rem' }}>
                                            <a href={`${BACKEND_BASE_URL}${url}`} target="_blank" rel="noopener noreferrer">{url.substring(url.lastIndexOf('/') + 1)}</a>
                                            <button type="button" onClick={() => handleRemoveFoto(url)} style={{ marginTop: '0' }}>X</button>
                                        </li>
                                    ))}
                                    {form.fotos.length === 0 && <li style={{ justifyContent: 'center', color: '#6c757d' }}>Nenhuma foto.</li>}
                                </ul>
                            </fieldset>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                        <button type="submit">{editingId ? 'Salvar Alterações' : 'Adicionar Peça'}</button>
                        {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#dc3545' }}>Cancelar Edição</button>}
                    </div>
                </form>
            </div>

            <div className="list-section">
                <h2>Peças Cadastradas e Ações</h2>
                <div className="filter-container" style={{ padding: '0 0 15px 0', borderBottom: '1px solid #eee', display: 'flex', gap: '1rem' }}>
                    <div style={{ flex: 1 }}>
                        <label>Filtrar por Área:</label>
                        <select value={filterListAreaId} onChange={e => { setFilterListAreaId(e.target.value); setFilterListModeloId(''); }}>
                            <option value="">Todas as Áreas</option>
                            {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                        </select>
                    </div>
                    <div style={{ flex: 1 }}>
                        <label>Filtrar por Modelo (Tipo):</label>
                        <select value={filterListModeloId} onChange={e => setFilterListModeloId(e.target.value)} disabled={!filterListAreaId}>
                            <option value="">Todos Modelos da Área</option>
                            {filterListModelosOptions.map(modelo => (<option key={modelo.id} value={modelo.id}>{modelo.nome}</option>))}
                        </select>
                    </div>
                </div>

                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Foto</th>
                                <th>QR Code</th>
                                <th>Nome</th>
                                <th>Cód. Req.</th>
                                <th>Área(s)</th>
                                <th>Modelo(s) Associado(s)</th>
                                <th>Estoque (A/M)</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredPecasList.map(peca => {
                                // ✅ CORREÇÃO: sempre pegar todos os modelosIds (se vier) + fallback do modeloEquipamentoId
                                const idsModelos = uniqueNums([
                                    ...(Array.isArray(peca.modelosIds) ? peca.modelosIds : []),
                                    peca.modeloEquipamentoId
                                ]);

                                // Modelos: nome + fabricante (melhor leitura) — mantém estilo simples na célula
                                const modelosNomes = idsModelos
                                    .map(id => {
                                        const m = modelos.find(mm => mm.id === id);
                                        return m ? `${m.nome} (${m.fabricante})` : null;
                                    })
                                    .filter(Boolean)
                                    .join(', ') || 'N/A';

                                // Áreas: únicas
                                const idsAreas = uniqueNums(idsModelos.map(id => modelos.find(m => m.id === id)?.areaId));
                                const areasNomes = [...new Set(idsAreas.map(id => areas.find(a => a.id === id)?.nome))]
                                    .filter(Boolean)
                                    .join(', ') || 'N/A';

                                return (
                                    <tr key={peca.id} style={peca.estoqueAtual <= peca.estoqueMinimo ? { backgroundColor: '#fff3cd' } : {}}>
                                        <td>
                                            {/* --- CORREÇÃO: Prioriza o novo link Cloudinary dkreomuo2 --- */}
                                            {peca.fotoUrl ? (
                                                <img src={peca.fotoUrl} alt="Peça" style={{ width: '50px', height: '50px', objectFit: 'cover', borderRadius: '4px' }} />
                                            ) : (
                                                /* ✅ CORREÇÃO ABAIXO: peca.fotos (sem cedilha) para alinhar com o .map() */
                                                /* peça.fotos */ peca.fotos && peca.fotos[0] ? (
                                                    <img src={`${BACKEND_BASE_URL}${peca.fotos[0]}`} alt="Peça" style={{ width: '50px', height: '50px', objectFit: 'cover', borderRadius: '4px' }} />
                                                ) : 'Sem foto'
                                            )}
                                        </td>
                                        <td>{peca.codigoControle && <img src={`${PECAS_API_URL}/${peca.id}/qrcode`} alt={peca.codigoControle} style={{ width: '50px' }} />}</td>
                                        <td><strong>{peca.nome}</strong></td>
                                        <td>{peca.codigoRequisicao || 'N/A'}</td>
                                        <td style={{ fontSize: '0.85rem' }}>{areasNomes}</td>

                                        {/* ✅ AQUI: agora mostra todos os modelos associados */}
                                        <td style={{ fontSize: '0.85rem' }}>{modelosNomes}</td>

                                        <td>{peca.estoqueAtual} / {peca.estoqueMinimo}</td>
                                        <td style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                                            <button onClick={() => handleEdit(peca)} style={{ padding: '5px' }}>Editar</button>
                                            <button onClick={() => handleDelete(peca.id)} style={{ backgroundColor: '#dc3545', padding: '5px' }}>Deletar</button>
                                        </td>
                                    </tr>
                                );
                            })}
                            {filteredPecasList.length === 0 && (
                                <tr><td colSpan="8" style={{ textAlign: 'center' }}>Nenhuma peça encontrada.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    );
}
// ===============================================
// 8. COMPONENTE PRINCIPAL (Orquestrador - COMPLETO)
// ===============================================
function Cadastros() {
    // States para guardar os dados
    const [areas, setAreas] = useState([]);
    const [linhas, setLinhas] = useState([]);
    const [modelos, setModelos] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [pecas, setPecas] = useState([]);
    const [checklists, setChecklists] = useState([]);
    const [users, setUsers] = useState([]);
    const [activeTab, setActiveTab] = useState('usuarios');
    const [loading, setLoading] = useState(true); // Estado de loading geral

    // Função unificada para buscar todos os dados
    const fetchAllData = async () => {
        // setLoading(true); // Removido daqui para evitar piscar em cada update
        try {
            const [areasRes, linhasRes, modelosRes, equipamentosRes, pecasRes, checklistsRes, usersRes] = await Promise.all([
                axios.get(AREAS_API_URL),
                axios.get(LINHAS_API_URL),
                axios.get(MODELOS_API_URL),
                axios.get(EQUIPAMENTOS_API_URL),
                axios.get(PECAS_API_URL),
                axios.get(CHECKLISTS_API_URL),
                axios.get(USUARIOS_API_URL)
            ]);
            // Atualiza todos os states com os dados recebidos, garantindo que sejam arrays
            setAreas(Array.isArray(areasRes.data) ? areasRes.data : []);
            setLinhas(Array.isArray(linhasRes.data) ? linhasRes.data : []);
            setModelos(Array.isArray(modelosRes.data) ? modelosRes.data : []);
            setEquipamentos(Array.isArray(equipamentosRes.data) ? equipamentosRes.data : []);
            setPecas(Array.isArray(pecasRes.data) ? pecasRes.data : []);
            setChecklists(Array.isArray(checklistsRes.data) ? checklistsRes.data : []);
            setUsers(Array.isArray(usersRes.data) ? usersRes.data : []);
        } catch (error) {
            console.error("Erro ao buscar dados de cadastro:", error);
            // Reseta states para arrays vazios em caso de erro
            setAreas([]); setLinhas([]); setModelos([]); setEquipamentos([]);
            setPecas([]); setChecklists([]); setUsers([]);
            alert("Falha ao carregar dados do servidor Aiven.");
        } finally {
             // Garante que o loading inicial seja desativado
            if (loading) setLoading(false);
        }
    };

    // Busca os dados quando o componente é montado
    useEffect(() => {
        setLoading(true); // Ativa loading só na montagem inicial
        fetchAllData();
    }, []); // Array vazio significa que roda só uma vez

    // Função para renderizar o módulo ativo
    const renderModule = () => {
        if (loading) {
            return <div style={{textAlign: 'center', padding: '50px', fontSize: '1.2rem'}}>Conectando ao banco Aiven...</div>;
        }

        switch (activeTab) {
            case 'usuarios': return <UsuarioModule users={users} fetchUsers={fetchAllData} />;
            case 'areas': return <AreaModule areas={areas} fetchAreas={fetchAllData} />;
            case 'linhas': return <LinhaModule linhas={linhas} fetchLinhas={fetchAllData} areas={areas} />;
            case 'modelos': return <ModeloModule modelos={modelos} fetchModelos={fetchAllData} areas={areas} fetchAllData={fetchAllData}/>;
            case 'equipamentos': return <EquipamentoModule equipamentos={equipamentos} fetchEquipamentos={fetchAllData} areas={areas} linhas={linhas} checklists={checklists} modelos={modelos} fetchAllData={fetchAllData}/>;
            case 'checklists': return <ChecklistModule checklists={checklists} fetchChecklists={fetchAllData} areas={areas} pecas={pecas} fetchAllData={fetchAllData}/>;
            case 'pecas': return <PecaModule pecas={pecas} fetchAllData={fetchAllData} areas={areas} modelos={modelos} />;
            default: return <UsuarioModule users={users} fetchUsers={fetchAllData} />;
        }
    };

    return (
        <div className="main-content">
            <h1>Painel Administrativo Heimdex</h1>
            {/* Abas de Navegação */}
            <div style={{marginBottom: '20px', display: 'flex', gap: '10px', borderBottom: '1px solid #ddd', flexWrap: 'wrap', paddingBottom: '10px'}}>
                <button onClick={() => setActiveTab('usuarios')} style={{backgroundColor: activeTab === 'usuarios' ? '#007bff' : '#6c757d', marginTop: '0'}}>Usuários</button>
                <button onClick={() => setActiveTab('areas')} style={{backgroundColor: activeTab === 'areas' ? '#007bff' : '#6c757d', marginTop: '0'}}>Áreas</button>
                <button onClick={() => setActiveTab('linhas')} style={{backgroundColor: activeTab === 'linhas' ? '#007bff' : '#6c757d', marginTop: '0'}}>Linhas</button>
                <button onClick={() => setActiveTab('modelos')} style={{backgroundColor: activeTab === 'modelos' ? '#007bff' : '#6c757d', marginTop: '0'}}>Modelos (Tipo)</button>
                <button onClick={() => setActiveTab('equipamentos')} style={{backgroundColor: activeTab === 'equipamentos' ? '#007bff' : '#6c757d', marginTop: '0'}}>Equipamentos (Tag)</button>
                <button onClick={() => setActiveTab('checklists')} style={{backgroundColor: activeTab === 'checklists' ? '#007bff' : '#6c757d', marginTop: '0'}}>Checklists</button>
                <button onClick={() => setActiveTab('pecas')} style={{backgroundColor: activeTab === 'pecas' ? '#007bff' : '#6c757d', marginTop: '0'}}>Peças/Estoque</button>
            </div>
            {/* Renderiza o Módulo Ativo */}
            {renderModule()}
        </div>
    );
}

export default Cadastros;
