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
// 1. MÓDULO DE USUÁRIOS (Corrigido com campo E-mail)
// ===============================================
function UsuarioModule({ users, fetchUsers, USUARIOS_API_URL, PERFIS_DISPONIVEIS, formatPerfilName }) {
    // Estado inicial agora inclui o campo 'email' exigido pela Model Java
    const initialState = { 
        nomeCompleto: '', 
        matricula: '', 
        email: '', 
        senha: '', 
        perfil: 'TECNICO' 
    };
    
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleCancelEdit = () => { 
        setForm(initialState); 
        setEditingId(null); 
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validação básica de senha para novos cadastros
        if (!editingId && (!form.senha || form.senha.length < 4)) {
            alert("Para novos usuários, a senha deve ter pelo menos 4 caracteres.");
            return;
        }

        try {
            if (editingId) {
                // Na edição, se a senha estiver vazia, enviamos null para não sobrescrever a senha atual
                const updateData = form.senha ? form : { ...form, senha: null };
                await axios.put(`${USUARIOS_API_URL}/${editingId}`, updateData);
            } else {
                // Envio para criação: agora o objeto 'form' contém o campo 'email'
                await axios.post(USUARIOS_API_URL, form);
            }
            handleCancelEdit();
            fetchUsers();
            alert("Usuário salvo com sucesso!");
        } catch (error) {
            console.error("Erro ao salvar usuário:", error.response?.data || error.message);
            // Mensagem detalhada baseada no erro de restrição do banco
            alert(`Erro ao salvar usuário: ${error.response?.data?.message || 'Verifique se o e-mail ou matrícula já existem no sistema.'}`);
        }
    };

    const handleEdit = (user) => {
        setEditingId(user.id);
        // Preenche o formulário com os dados do usuário, limpando o campo de senha por segurança
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
                    <div className="form-grid">
                        <label>Nome Completo:</label>
                        <input 
                            name="nomeCompleto" 
                            value={form.nomeCompleto} 
                            onChange={handleChange} 
                            placeholder="Ex: João Silva" 
                            required 
                        />
                        
                        <label>E-mail Corporativo:</label>
                        {/* Campo crucial adicionado para satisfazer a constraint do banco de dados */}
                        <input 
                            name="email" 
                            type="email" 
                            value={form.email} 
                            onChange={handleChange} 
                            placeholder="usuario@heimdex.com.br" 
                            required 
                        />
                        
                        <label>Usuário (Login/Matrícula):</label>
                        <input 
                            name="matricula" 
                            value={form.matricula} 
                            onChange={handleChange} 
                            placeholder="Matrícula" 
                            required 
                        />
                        
                        <label>Senha:</label>
                        <input
                            name="senha"
                            type="password"
                            value={form.senha}
                            onChange={handleChange}
                            placeholder={editingId ? 'Deixe em branco para manter a atual' : 'Senha Obrigatória'}
                            required={!editingId}
                        />
                        
                        <label>Perfil de Acesso:</label>
                        <select name="perfil" value={form.perfil} onChange={handleChange} required>
                            {PERFIS_DISPONIVEIS.map(perfil => (
                                <option key={perfil} value={perfil}>
                                    {formatPerfilName(perfil)}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
                        <button type="submit" style={{ backgroundColor: '#28a745', color: 'white' }}>
                            {editingId ? 'Salvar Alterações' : 'Adicionar Usuário'}
                        </button>
                        {editingId && (
                            <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d', color: 'white' }}>
                                Cancelar Edição
                            </button>
                        )}
                    </div>
                </form>
            </div>

            <div className="list-section" style={{ marginTop: '30px' }}>
                <h2>Usuários Cadastrados</h2>
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome</th>
                                <th>Login</th>
                                <th>E-mail</th>
                                <th>Perfil</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td><strong>{user.nomeCompleto}</strong></td>
                                    <td>{user.matricula}</td>
                                    <td>{user.email}</td>
                                    <td>{formatPerfilName(user.perfil)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '5px' }}>
                                            <button onClick={() => handleEdit(user)}>Editar</button>
                                            <button onClick={() => handleDelete(user.id)} style={{ backgroundColor: '#dc3545', color: 'white' }}>Deletar</button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {users.length === 0 && (
                                <tr>
                                    <td colSpan="6" style={{ textAlign: 'center' }}>Nenhum usuário cadastrado.</td>
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

        // ✅ CORREÇÃO: Transformando areaId no objeto 'area' que o Java espera
        const linhaData = {
            nome: form.nome,
            area: {
                id: Number(form.areaId) // O Java precisa do objeto Area para mapear
            }
        };

        try {
            if (editingId) {
                await axios.put(`${LINHAS_API_URL}/${editingId}`, linhaData);
            } else {
                await axios.post(LINHAS_API_URL, linhaData);
            }
            handleCancelEdit();
            fetchLinhas();
            alert("Linha salva com sucesso!");
        } catch (error) {
            console.error("Erro ao salvar linha:", error.response?.data || error.message);
            alert("Erro ao salvar a linha de produção. Verifique o console.");
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

        // ✅ CORREÇÃO: Enviando o objeto que o Java espera
        const modeloData = {
            nome: form.nome,
            fabricante: form.fabricante,
            area: {
                id: Number(form.areaId) // O segredo está aqui
            }
        };

        try {
            if (editingId) {
                await axios.put(`${MODELOS_API_URL}/${editingId}`, modeloData);
            } else {
                await axios.post(MODELOS_API_URL, modeloData);
            }
            handleCancelEdit();
            fetchModelos(); 
            alert("Modelo salvo com sucesso!");
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
                fetchModelos();
            } catch (error) {
                alert("Erro ao deletar o modelo. Verifique se não está em uso.");
            }
        }
    };

    const filteredModelos = useMemo(() => {
        if (!filterAreaId) return modelos;
        return modelos.filter(modelo => modelo.areaId.toString() === filterAreaId);
    }, [filterAreaId, modelos]);

    return (
        <section style={{ marginBottom: '2rem' }}>
            <div className="form-section" style={{marginBottom: '2rem'}}>
                <h2>{editingId ? 'Editar Modelo (Tipo)' : 'Novo Modelo de Equipamento (Tipo)'}</h2>
                <form onSubmit={handleSubmit}>
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
                                    <td>
                                        <button onClick={() => handleEdit(modelo)}>Editar</button>
                                        <button onClick={() => handleDelete(modelo.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button>
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
// 5. MÓDULO DE CHECKLISTS (Original de 1499 linhas)
// ===============================================
function ChecklistModule({ checklists, fetchChecklists, areas, pecas }) {
    const [nome, setNome] = useState('');
    const [areaId, setAreaId] = useState('');
    const [currentItem, setCurrentItem] = useState('');
    const [itens, setItens] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [filterAreaId, setFilterAreaId] = useState('');

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
            itens: itens 
        };

        try {
            if (editingId) {
                await axios.put(`${CHECKLISTS_API_URL}/${editingId}`, checklistData);
            } else {
                await axios.post(CHECKLISTS_API_URL, checklistData);
            }
            handleCancelEdit();
            fetchChecklists();
            alert("Checklist salvo!");
        } catch (error) {
            alert("Erro ao salvar checklist.");
        }
    };

    const handleChecklistEdit = (checklist) => {
        setEditingId(checklist.id);
        setNome(checklist.nome);
        const area = areas.find(a => a.nome === checklist.areaNome);
        setAreaId(area ? area.id.toString() : '');
        setItens(checklist.itens || []);
    };

    const handleChecklistDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja deletar este modelo?')) {
            try {
                await axios.delete(`${CHECKLISTS_API_URL}/${id}`);
                fetchChecklists();
            } catch (error) {
                alert("Erro ao deletar modelo.");
            }
        }
    };

    return (
        <section className="container">
            <div className="form-section">
                <h2>{editingId ? 'Editar Checklist' : 'Novo Checklist'}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Nome do Modelo:</label>
                    <input value={nome} onChange={(e) => setNome(e.target.value)} required />
                    <label>Área:</label>
                    <select value={areaId} onChange={(e) => setAreaId(e.target.value)} required>
                        <option value="">-- Selecione --</option>
                        {areas.map(area => (<option key={area.id} value={area.id}>{area.nome}</option>))}
                    </select>
                    <fieldset className="form-fieldset" style={{ marginTop: '20px' }}>
                        <legend>Passos do Checklist</legend>
                        <div className="item-adder">
                            <input value={currentItem} onChange={(e) => setCurrentItem(e.target.value)} />
                            <button type="button" onClick={handleAddItem}>Adicionar</button>
                        </div>
                        <ul className="checklist-preview">
                            {itens.map((item, index) => (
                                <li key={index}>
                                    <span>{index + 1}. {item.descricao}</span>
                                    <button type="button" onClick={() => handleRemoveItem(index)}>X</button>
                                </li>
                            ))}
                        </ul>
                    </fieldset>
                    <button type="submit" style={{ marginTop: '20px' }}>Salvar</button>
                    {editingId && (<button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d' }}>Cancelar</button>)}
                </form>
            </div>
            <div className="list-section">
                <h2>Modelos Cadastrados</h2>
                <table>
                    <thead><tr><th>ID</th><th>Modelo</th><th>Área</th><th>Ações</th></tr></thead>
                    <tbody>
                        {checklists.map(cl => (
                            <tr key={cl.id}>
                                <td>{cl.id}</td><td>{cl.nome}</td><td>{cl.areaNome}</td>
                                <td><button onClick={() => handleChecklistEdit(cl)}>Editar</button><button onClick={() => handleChecklistDelete(cl.id)} style={{ backgroundColor: '#dc3545' }}>Deletar</button></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// ===============================================
// 6. MÓDULO DE EQUIPAMENTOS (TAGS) - CORRIGIDO
// ===============================================
function EquipamentoModule({ equipamentos, fetchEquipamentos, areas, linhas, checklists, modelos, EQUIPAMENTOS_API_URL }) {
    const initialState = { nome: '', codigo: '', criticidade: 'C', linhaId: '', modeloId: '', checklistId: '', frequenciaPreventiva: '', dataUltimaPreventiva: '' };
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [selectedAreaId, setSelectedAreaId] = useState('');
    const [filterListAreaId, setFilterListAreaId] = useState('');
    const [filterListLinhaId, setFilterListLinhaId] = useState('');
    const [displayedEquipamentos, setDisplayedEquipamentos] = useState([]);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const handleCancelEdit = () => { setForm(initialState); setSelectedAreaId(''); setEditingId(null); };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const url = EQUIPAMENTOS_API_URL || `${import.meta.env.VITE_API_BASE_URL}/api/equipamentos`;

        if (!form.linhaId || !form.modeloId) return alert("Selecione Área, Modelo e Linha.");

        const frequenciaTratada = form.frequenciaPreventiva === "" ? null : form.frequenciaPreventiva;
        const dataTratada = form.dataUltimaPreventiva === "" ? null : form.dataUltimaPreventiva;

        const requestData = { 
            nome: form.nome,
            codigo: form.codigo || null,
            criticidade: form.criticidade,
            linhaId: Number(form.linhaId), 
            modeloId: Number(form.modeloId), 
            checklistId: form.checklistId ? Number(form.checklistId) : null,
            frequenciaPreventiva: frequenciaTratada,
            dataUltimaPreventiva: dataTratada
        };

        try {
            if (editingId) { await axios.put(`${url}/${editingId}`, requestData); }
            else { await axios.post(url, requestData); }
            handleCancelEdit(); 
            fetchEquipamentos();
            alert("Equipamento salvo!");
        } catch (error) { 
            console.error("Erro:", error.response?.data);
            alert(`Erro: ${error.response?.data?.error || "Falha na comunicação"}`); 
        }
    };

    const handleEdit = (equip) => {
        const linha = linhas.find(l => l.nome === equip.nomeLinha && l.areaNome === equip.nomeArea);
        if (linha) {
            setSelectedAreaId(linha.areaId.toString());
            setForm({ 
                ...equip, 
                linhaId: linha.id.toString(), 
                modeloId: equip.modeloId?.toString() || '', 
                checklistId: equip.checklistId || '',
                frequenciaPreventiva: equip.frequenciaPreventiva || '',
                dataUltimaPreventiva: equip.dataUltimaPreventiva || ''
            });
            setEditingId(equip.id);
        }
    };

    const filteredLinhasForm = useMemo(() => selectedAreaId ? linhas.filter(l => l.areaId.toString() === selectedAreaId) : [], [selectedAreaId, linhas]);
    const filteredModelosForm = useMemo(() => selectedAreaId ? modelos.filter(m => m.areaId.toString() === selectedAreaId) : [], [selectedAreaId, modelos]);

    const filteredEquipamentosList = useMemo(() => {
        return equipamentos.filter(equip => {
            const area = areas.find(a => a.id.toString() === filterListAreaId);
            const linha = linhas.find(l => l.id.toString() === filterListLinhaId);
            return (!filterListAreaId || equip.nomeArea === area?.nome) && (!filterListLinhaId || equip.nomeLinha === linha?.nome);
        });
    }, [filterListAreaId, filterListLinhaId, equipamentos, areas, linhas]);

    useEffect(() => { setDisplayedEquipamentos(filteredEquipamentosList); }, [filteredEquipamentosList]);

    const moveEquipamento = (index, direction) => {
        const newIndex = index + direction;
        if (newIndex < 0 || newIndex >= displayedEquipamentos.length) return;
        const newList = [...displayedEquipamentos];
        [newList[index], newList[newIndex]] = [newList[newIndex], newList[index]];
        setDisplayedEquipamentos(newList);
    };

    return (
        <section style={{ marginBottom: '2rem' }}>
            <div className="form-section">
                <h2>Equipamentos (Tags)</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <label>Área:</label>
                        <select value={selectedAreaId} onChange={e => { setSelectedAreaId(e.target.value); setForm({...form, linhaId: '', modeloId: ''}) }} required>
                            <option value="">-- Selecione --</option>
                            {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                        </select>
                        <label>Modelo:</label>
                        <select name="modeloId" value={form.modeloId} onChange={handleChange} required disabled={!selectedAreaId}>
                            <option value="">-- Selecione --</option>
                            {filteredModelosForm.map(m => <option key={m.id} value={m.id}>{m.nome}</option>)}
                        </select>
                        <label>Linha:</label>
                        <select name="linhaId" value={form.linhaId} onChange={handleChange} required disabled={!selectedAreaId}>
                            <option value="">-- Selecione --</option>
                            {filteredLinhasForm.map(l => <option key={l.id} value={l.id}>{l.nome}</option>)}
                        </select>
                        <label>Tag:</label><input name="nome" value={form.nome} onChange={handleChange} required />
                        
                        <label>Checklist Padrão:</label>
                        <select name="checklistId" value={form.checklistId} onChange={handleChange}>
                            <option value="">Nenhum</option>
                            {checklists.map(cl => <option key={cl.id} value={cl.id}>{cl.nome}</option>)}
                        </select>

                        <label>Freq. Preventiva:</label>
                        <select name="frequenciaPreventiva" value={form.frequenciaPreventiva} onChange={handleChange}>
                            <option value="">Não Definida</option>
                            <option value="QUINZENAL">Quinzenal</option>
                            <option value="MENSAL">Mensal</option>
                            <option value="TRIMESTRAL">Trimestral</option>
                            <option value="SEMESTRAL">Semestral</option>
                            <option value="ANUAL">Anual</option>
                        </select>

                        {/* ✅ CAMPO RESTAURADO: Data da Última Preventiva */}
                        <label>Última Preventiva:</label>
                        <input 
                            type="date" 
                            name="dataUltimaPreventiva" 
                            value={form.dataUltimaPreventiva} 
                            onChange={handleChange} 
                        />
                    </div>
                    <button type="submit" style={{marginTop: '10px'}}>Salvar</button>
                    {editingId && <button type="button" onClick={handleCancelEdit} style={{ backgroundColor: '#6c757d', color: 'white', marginLeft: '10px' }}>Cancelar</button>}
                </form>
            </div>
            <div className="list-section">
                <table>
                    <thead><tr><th>Ordem</th><th>Tag</th><th>Modelo</th><th>Linha</th><th>Área</th><th>Ações</th></tr></thead>
                    <tbody>
                        {displayedEquipamentos.map((equip, index) => (
                            <tr key={equip.id}>
                                <td><button onClick={() => moveEquipamento(index, -1)}>▲</button><button onClick={() => moveEquipamento(index, 1)}>▼</button></td>
                                <td>{equip.nome}</td><td>{equip.nomeModelo}</td><td>{equip.nomeLinha}</td><td>{equip.nomeArea || 'N/A'}</td>
                                <td><button onClick={() => handleEdit(equip)}>Editar</button></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

// ===============================================
// 7. MÓDULO DE PEÇAS / ESTOQUE
// ===============================================
function PecaModule({ pecas, fetchAllData, areas, modelos }) {
    const initialState = { 
        nome: '', 
        codigoRequisicao: '', 
        descricaoTecnica: '', 
        estoqueAtual: 0, 
        estoqueMinimo: 0, 
        fotos: [], 
        localizacaoPrateleira: '', 
        aplicacao: '', 
        associacoes: [{ areaId: '', modeloEquipamentoId: '' }] 
    };
    
    const [form, setForm] = useState(initialState);
    const [editingId, setEditingId] = useState(null);
    const [selectedFile, setSelectedFile] = useState(null);
    
    // ✅ 1. CORREÇÃO: Topo da função (Estados)
    const [modalImage, setModalImage] = useState(null);
    const [filterListAreaId, setFilterListAreaId] = useState('');
    const [filterListModeloId, setFilterListModeloId] = useState('');

    // ✅ 3. CORREÇÃO: Evento de teclado (ESC para fechar)
    useEffect(() => {
        const handleEsc = (e) => { 
            if (e.key === 'Escape') setModalImage(null); 
        };
        window.addEventListener('keydown', handleEsc);
        return () => window.removeEventListener('keydown', handleEsc);
    }, []);

    const handleAssociacaoChange = (index, field, value) => {
        const novasAssociacoes = [...form.associacoes];
        novasAssociacoes[index][field] = value;
        if (field === 'areaId') {
            novasAssociacoes[index].modeloEquipamentoId = '';
        }
        setForm({ ...form, associacoes: novasAssociacoes });
    };

    const addAssociacao = () => {
        setForm({ ...form, associacoes: [...form.associacoes, { areaId: '', modeloEquipamentoId: '' }] });
    };

    const removeAssociacao = (index) => {
        const novas = form.associacoes.filter((_, i) => i !== index);
        setForm({ ...form, associacoes: novas.length > 0 ? novas : [{ areaId: '', modeloEquipamentoId: '' }] });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const modelosIds = form.associacoes
            .map(a => Number(a.modeloEquipamentoId))
            .filter(id => id > 0);

        if (modelosIds.length === 0) {
            return alert("Selecione pelo menos um modelo de equipamento compatível.");
        }

        const formData = new FormData();
        
        const requestData = { 
            ...form, 
            modelosIds: modelosIds,
            modeloEquipamentoId: modelosIds[0] 
        };

        formData.append('peca', new Blob([JSON.stringify(requestData)], { type: "application/json" }));
        
        if (selectedFile) {
            formData.append('arquivo', selectedFile);
        }

        try {
            if (editingId) {
                await axios.put(`${PECAS_API_URL}/${editingId}`, requestData);
            } else {
                await axios.post(PECAS_API_URL, formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
            }
            
            setForm(initialState);
            setEditingId(null);
            setSelectedFile(null);
            fetchAllData();
            alert("Peça de reposição salva com sucesso!");
        } catch (error) {
            console.error("Erro ao salvar peça:", error);
            alert("Erro ao salvar a peça. Verifique a conexão com o servidor.");
        }
    };

    const handleEdit = (peca) => {
        setEditingId(peca.id);
        const associacoesExistentes = peca.modelosIds?.map(mId => {
            const mod = modelos.find(m => m.id === mId);
            return {
                areaId: mod?.areaId?.toString() || '',
                modeloEquipamentoId: mId.toString()
            };
        }) || [{ areaId: '', modeloEquipamentoId: '' }];

        setForm({
            ...peca,
            associacoes: associacoesExistentes
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleDelete = async (id) => {
        if (window.confirm('Tem certeza que deseja excluir esta peça?')) {
            try {
                await axios.delete(`${PECAS_API_URL}/${id}`);
                fetchAllData();
            } catch (error) {
                alert("Erro ao excluir a peça.");
            }
        }
    };

    const filteredPecasList = useMemo(() => {
        return pecas.filter(peca => {
            const idsModelosDaPeca = [...(peca.modelosIds || []), peca.modeloEquipamentoId].filter(Boolean);
            const idsAreasDaPeca = idsModelosDaPeca.map(mId => modelos.find(m => m.id === mId)?.areaId);
            const matchArea = !filterListAreaId || idsAreasDaPeca.includes(Number(filterListAreaId));
            const matchModelo = !filterListModeloId || idsModelosDaPeca.includes(Number(filterListModeloId));
            return matchArea && matchModelo;
        });
    }, [filterListAreaId, filterListModeloId, pecas, modelos]);

    return (
        <section style={{ marginBottom: '2rem' }}>
            <div className="form-section">
                <h2>{editingId ? 'Editar Peça' : 'Nova Peça de Reposição'}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Nome da Peça:</label>
                    <input name="nome" value={form.nome} onChange={e => setForm({...form, nome: e.target.value})} required />

                    <label>Código de Requisição (Almoxarifado):</label>
                    <input name="codigoRequisicao" value={form.codigoRequisicao} onChange={e => setForm({...form, codigoRequisicao: e.target.value})} />

                    <div style={{ display: 'flex', gap: '15px' }}>
                        <div style={{ flex: 1 }}>
                            <label>Estoque Atual:</label>
                            <input type="number" value={form.estoqueAtual} onChange={e => setForm({...form, estoqueAtual: Number(e.target.value)})} required />
                        </div>
                        <div style={{ flex: 1 }}>
                            <label>Estoque Mínimo:</label>
                            <input type="number" value={form.estoqueMinimo} onChange={e => setForm({...form, estoqueMinimo: Number(e.target.value)})} required />
                        </div>
                    </div>

                    <label>Foto da Peça:</label>
                    <input type="file" onChange={e => setSelectedFile(e.target.files[0])} accept="image/*" />

                    <fieldset className="form-fieldset" style={{ marginTop: '15px' }}>
                        <legend>Equipamentos Compatíveis</legend>
                        {form.associacoes.map((assoc, index) => (
                            <div key={index} style={{ display: 'flex', gap: '10px', marginBottom: '10px', alignItems: 'center' }}>
                                <select value={assoc.areaId} onChange={e => handleAssociacaoChange(index, 'areaId', e.target.value)}>
                                    <option value="">Selecione a Área</option>
                                    {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                                </select>
                                <select value={assoc.modeloEquipamentoId} onChange={e => handleAssociacaoChange(index, 'modeloEquipamentoId', e.target.value)} disabled={!assoc.areaId}>
                                    <option value="">Selecione o Modelo</option>
                                    {modelos.filter(m => m.areaId.toString() === assoc.areaId).map(m => <option key={m.id} value={m.id}>{m.nome}</option>)}
                                </select>
                                <button type="button" onClick={() => removeAssociacao(index)} style={{ backgroundColor: '#ff4d4d', color: 'white', padding: '5px 10px' }}>X</button>
                            </div>
                        ))}
                        <button type="button" onClick={addAssociacao} style={{ backgroundColor: '#007bff', color: 'white', marginTop: '5px' }}>+ Adicionar Compatibilidade</button>
                    </fieldset>

                    <div style={{ marginTop: '20px' }}>
                        <button type="submit" className="btn-save">{editingId ? 'Salvar Alterações' : 'Cadastrar Peça'}</button>
                        {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(initialState); }} style={{ backgroundColor: '#6c757d', marginLeft: '10px' }}>Cancelar</button>}
                    </div>
                </form>
            </div>

            <div className="list-section" style={{ marginTop: '2rem' }}>
                <h3>Peças em Estoque</h3>
                <div className="filter-container" style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
                    <select value={filterListAreaId} onChange={e => setFilterListAreaId(e.target.value)}>
                        <option value="">Todas as Áreas</option>
                        {areas.map(a => <option key={a.id} value={a.id}>{a.nome}</option>)}
                    </select>
                    <select value={filterListModeloId} onChange={e => setFilterListModeloId(e.target.value)}>
                        <option value="">Todos os Modelos</option>
                        {modelos.map(m => <option key={m.id} value={m.id}>{m.nome}</option>)}
                    </select>
                </div>

                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Foto</th>
                                <th>QR</th>
                                <th>Nome da Peça</th>
                                <th>Área(s)</th>
                                <th>Máquina</th>
                                <th>Estoque (A/M)</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredPecasList.map(peca => (
                                <tr key={peca.id}>
                                    <td>
                                        {peca.fotoUrl ? (
                                            <img 
                                                src={`${import.meta.env.VITE_API_BASE_URL}/uploads/${peca.fotoUrl}`} 
                                                alt="peca" 
                                                width="50" 
                                                style={{ borderRadius: '4px', cursor: 'zoom-in' }} 
                                                onClick={() => setModalImage(`${import.meta.env.VITE_API_BASE_URL}/uploads/${peca.fotoUrl}`)}
                                                onError={(e) => { e.target.style.display = 'none'; }}
                                            />
                                        ) : <span style={{ fontSize: '10px' }}>Sem foto</span>}
                                    </td>
                                    <td>
                                        <img 
                                            src={`https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${peca.codigoRequisicao || 'ID-'+peca.id}`} 
                                            width="50" 
                                            alt="qr" 
                                            style={{ cursor: 'zoom-in', border: '1px solid #eee' }}
                                            onClick={() => setModalImage(`https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=${peca.codigoRequisicao || 'ID-'+peca.id}`)}
                                        />
                                    </td>
                                    <td><strong>{peca.nome}</strong><br/><small>{peca.codigoRequisicao}</small></td>
                                    <td>{peca.nomeArea || 'N/A'}</td>
                                    <td>{peca.nomeModeloEquipamento || 'Múltiplos'}</td>
                                    <td style={{ color: peca.estoqueAtual <= peca.estoqueMinimo ? 'red' : 'inherit', fontWeight: 'bold' }}>
                                        {peca.estoqueAtual} / {peca.estoqueMinimo}
                                    </td>
                                    <td>
                                        <button onClick={() => handleEdit(peca)}>Editar</button>
                                        <button onClick={() => handleDelete(peca.id)} style={{ backgroundColor: '#dc3545', color: 'white' }}>Excluir</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* ✅ 2. CORREÇÃO: Final do return (Modal de Visualização) */}
            {modalImage && (
                <div 
                    style={{
                        position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
                        backgroundColor: 'rgba(0,0,0,0.85)', display: 'flex', justifyContent: 'center',
                        alignItems: 'center', zIndex: 10000, cursor: 'pointer'
                    }}
                    onClick={() => setModalImage(null)}
                >
                    <div style={{ position: 'relative', background: '#fff', padding: '15px', borderRadius: '12px' }}>
                        <img 
                            src={modalImage} 
                            style={{ maxWidth: '90vw', maxHeight: '85vh', borderRadius: '8px', display: 'block' }} 
                            alt="Ampliada" 
                        />
                        <div style={{ textAlign: 'center', marginTop: '10px', color: '#333', fontWeight: 'bold' }}>
                            ESC ou Clique para fechar
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
}
// ===============================================
// 8. COMPONENTE PRINCIPAL (Orquestrador)
// ===============================================
function Cadastros() {
    const [areas, setAreas] = useState([]);
    const [linhas, setLinhas] = useState([]);
    const [modelos, setModelos] = useState([]);
    const [equipamentos, setEquipamentos] = useState([]);
    const [pecas, setPecas] = useState([]);
    const [checklists, setChecklists] = useState([]);
    const [users, setUsers] = useState([]);
    const [activeTab, setActiveTab] = useState('usuarios');
    const [loading, setLoading] = useState(true);

    const fetchAllData = async () => {
        try {
            const [areasRes, linhasRes, modelosRes, equipamentosRes, pecasRes, checklistsRes, usersRes] = await Promise.all([
                axios.get(AREAS_API_URL), axios.get(LINHAS_API_URL), axios.get(MODELOS_API_URL),
                axios.get(EQUIPAMENTOS_API_URL), axios.get(PECAS_API_URL), axios.get(CHECKLISTS_API_URL), axios.get(USUARIOS_API_URL)
            ]);
            setAreas(Array.isArray(areasRes.data) ? areasRes.data : []);
            setLinhas(Array.isArray(linhasRes.data) ? linhasRes.data : []);
            setModelos(Array.isArray(modelosRes.data) ? modelosRes.data : []);
            setEquipamentos(Array.isArray(equipamentosRes.data) ? equipamentosRes.data : []);
            setPecas(Array.isArray(pecasRes.data) ? pecasRes.data : []);
            setChecklists(Array.isArray(checklistsRes.data) ? checklistsRes.data : []);
            setUsers(Array.isArray(usersRes.data) ? usersRes.data : []);
        } catch (error) {
            console.error("Erro ao carregar dados:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchAllData(); }, []);

    const renderModule = () => {
        if (loading) return <div style={{textAlign: 'center', padding: '50px'}}>Conectando ao banco local...</div>;

        switch (activeTab) {
            case 'usuarios': 
                return (
                    <UsuarioModule 
                        users={users} 
                        fetchUsers={fetchAllData} 
                        USUARIOS_API_URL={USUARIOS_API_URL} 
                        PERFIS_DISPONIVEIS={PERFIS_DISPONIVEIS} 
                        formatPerfilName={formatPerfilName} 
                    />
                );
            case 'areas': return <AreaModule areas={areas} fetchAreas={fetchAllData} />;
            case 'linhas': return <LinhaModule linhas={linhas} fetchLinhas={fetchAllData} areas={areas} />;
            case 'modelos': return <ModeloModule modelos={modelos} fetchModelos={fetchAllData} areas={areas} />;
            // ✅ CORREÇÃO FINAL: Passando explicitamente a URL para o módulo não dar erro 404
            case 'equipamentos': return <EquipamentoModule equipamentos={equipamentos} fetchEquipamentos={fetchAllData} areas={areas} linhas={linhas} checklists={checklists} modelos={modelos} EQUIPAMENTOS_API_URL={EQUIPAMENTOS_API_URL} />;
            case 'checklists': return <ChecklistModule checklists={checklists} fetchChecklists={fetchAllData} areas={areas} pecas={pecas} />;
            case 'pecas': return <PecaModule pecas={pecas} fetchAllData={fetchAllData} areas={areas} modelos={modelos} />;
            default: return null;
        }
    };

    return (
        <div className="main-content">
            <h1>Painel Administrativo Heimdex (Local)</h1>
            <div style={{marginBottom: '20px', display: 'flex', gap: '10px', borderBottom: '1px solid #ddd', flexWrap: 'wrap', paddingBottom: '10px'}}>
                <button onClick={() => setActiveTab('usuarios')} style={{backgroundColor: activeTab === 'usuarios' ? '#007bff' : '#6c757d'}}>Usuários</button>
                <button onClick={() => setActiveTab('areas')} style={{backgroundColor: activeTab === 'areas' ? '#007bff' : '#6c757d'}}>Áreas</button>
                <button onClick={() => setActiveTab('linhas')} style={{backgroundColor: activeTab === 'linhas' ? '#007bff' : '#6c757d'}}>Linhas</button>
                <button onClick={() => setActiveTab('modelos')} style={{backgroundColor: activeTab === 'modelos' ? '#007bff' : '#6c757d'}}>Modelos</button>
                <button onClick={() => setActiveTab('equipamentos')} style={{backgroundColor: activeTab === 'equipamentos' ? '#007bff' : '#6c757d'}}>Equipamentos</button>
                <button onClick={() => setActiveTab('checklists')} style={{backgroundColor: activeTab === 'checklists' ? '#007bff' : '#6c757d'}}>Checklists</button>
                <button onClick={() => setActiveTab('pecas')} style={{backgroundColor: activeTab === 'pecas' ? '#007bff' : '#6c757d'}}>Peças/Estoque</button>
            </div>
            {renderModule()}
        </div>
    );
}

export default Cadastros;