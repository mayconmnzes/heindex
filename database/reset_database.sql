-- ============================================
-- SCRIPT DE RESET DO BANCO DE DADOS HEIMDEX
-- ============================================
-- ATENÇÃO: Este script APAGA TODOS os dados!
-- Execute apenas se tiver certeza!
--
-- Como executar no DBeaver/Aiven:
-- 1. Conecte no banco PostgreSQL
-- 2. Abra este arquivo
-- 3. Selecione tudo (Ctrl+A)
-- 4. Execute (Ctrl+Enter)
-- 5. Aguarde a conclusão
-- 6. Teste login: admin/admin
-- ============================================

-- ============================================
-- PARTE 1: REMOVER TODAS AS TABELAS
-- ============================================
-- Ordem reversa para respeitar foreign keys

DROP TABLE IF EXISTS planos_manutencao CASCADE;
DROP TABLE IF EXISTS movimentacoes_estoque CASCADE;
DROP TABLE IF EXISTS fotos_os CASCADE;
DROP TABLE IF EXISTS resultados_checklist CASCADE;
DROP TABLE IF EXISTS os_tecnicos_executores CASCADE;
DROP TABLE IF EXISTS ordens_servico CASCADE;
DROP TABLE IF EXISTS equipamentos CASCADE;
DROP TABLE IF EXISTS item_pecas_sugeridas CASCADE;
DROP TABLE IF EXISTS itens_checklist CASCADE;
DROP TABLE IF EXISTS checklists CASCADE;
DROP TABLE IF EXISTS linhas_de_producao CASCADE;
DROP TABLE IF EXISTS pecas_modelos CASCADE;
DROP TABLE IF EXISTS pecas_reposicao CASCADE;
DROP TABLE IF EXISTS modelos_equipamento CASCADE;
DROP TABLE IF EXISTS areas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ============================================
-- PARTE 2: CRIAR ESTRUTURA DO BANCO
-- ============================================

-- Tabela: usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome_completo VARCHAR(255) NOT NULL,
    matricula VARCHAR(255) UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50),
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela: areas
CREATE TABLE areas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- Tabela: modelos_equipamento
CREATE TABLE modelos_equipamento (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    fabricante VARCHAR(100),
    area_id BIGINT,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE SET NULL
);

-- Tabela: pecas_reposicao
CREATE TABLE pecas_reposicao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    codigo_controle VARCHAR(255),
    estoque_atual INTEGER DEFAULT 0,
    estoque_minimo INTEGER DEFAULT 0,
    foto_url VARCHAR(255),
    localizacao_prateleira VARCHAR(255),
    codigo_requisicao VARCHAR(255),
    descricao_tecnica TEXT,
    aplicacao VARCHAR(255),
    qr_url VARCHAR(255)
);

-- Tabela: linhas_de_producao
CREATE TABLE linhas_de_producao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    area_id BIGINT NOT NULL,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE
);

-- Tabela: checklists
CREATE TABLE checklists (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    area_id BIGINT,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE SET NULL
);

-- Tabela: itens_checklist
CREATE TABLE itens_checklist (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    checklist_id BIGINT NOT NULL,
    FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE CASCADE
);

-- Tabela: equipamentos
CREATE TABLE equipamentos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    codigo VARCHAR(255) NOT NULL UNIQUE,
    checklist_id BIGINT,
    criticidade VARCHAR(50),
    frequencia_preventiva VARCHAR(50),
    data_ultima_preventiva DATE,
    status VARCHAR(100),
    linha_id BIGINT,
    modelo_id BIGINT,
    FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE SET NULL,
    FOREIGN KEY (linha_id) REFERENCES linhas_de_producao(id) ON DELETE SET NULL,
    FOREIGN KEY (modelo_id) REFERENCES modelos_equipamento(id) ON DELETE SET NULL
);

-- Tabela: ordens_servico
CREATE TABLE ordens_servico (
    id BIGSERIAL PRIMARY KEY,
    equipamento_id BIGINT NOT NULL,
    lider_validador_id BIGINT,
    checklist_id BIGINT,
    status VARCHAR(50) NOT NULL,
    tipo_manutencao VARCHAR(50),
    data_agendamento TIMESTAMP,
    data_inicio_execucao TIMESTAMP,
    data_fim_execucao TIMESTAMP,
    data_validacao TIMESTAMP,
    observacoes_tecnico TEXT,
    observacoes_lider TEXT,
    FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id) ON DELETE CASCADE,
    FOREIGN KEY (lider_validador_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE SET NULL
);

-- Tabela: resultados_checklist
CREATE TABLE resultados_checklist (
    id BIGSERIAL PRIMARY KEY,
    item_template_id BIGINT NOT NULL,
    ordem_servico_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    observacao TEXT,
    FOREIGN KEY (item_template_id) REFERENCES itens_checklist(id) ON DELETE CASCADE,
    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
);

-- Tabela: fotos_os
CREATE TABLE fotos_os (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL,
    url TEXT NOT NULL,
    descricao VARCHAR(255),
    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
);

-- Tabela: movimentacoes_estoque
CREATE TABLE movimentacoes_estoque (
    id BIGSERIAL PRIMARY KEY,
    quantidade INTEGER,
    tipo VARCHAR(50),
    tipo_movimentacao VARCHAR(50),
    data_hora TIMESTAMP,
    data_movimentacao TIMESTAMP,
    observacao TEXT,
    peca_id BIGINT,
    equipamento_id BIGINT,
    ordem_servico_id BIGINT,
    usuario_id BIGINT,
    FOREIGN KEY (peca_id) REFERENCES pecas_reposicao(id) ON DELETE SET NULL,
    FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id) ON DELETE SET NULL,
    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela: planos_manutencao
CREATE TABLE planos_manutencao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    periodicidade_dias INTEGER,
    ativo BOOLEAN DEFAULT FALSE,
    proxima_execucao DATE,
    equipamento_id BIGINT,
    FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id) ON DELETE SET NULL
);

-- ============================================
-- TABELAS DE RELACIONAMENTO MANY-TO-MANY
-- ============================================

-- Relacionamento: OS <-> Tecnicos Executores
CREATE TABLE os_tecnicos_executores (
    ordem_servico_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (ordem_servico_id, usuario_id),
    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Relacionamento: Itens Checklist <-> Pecas Sugeridas
CREATE TABLE item_pecas_sugeridas (
    item_checklist_id BIGINT NOT NULL,
    peca_id BIGINT NOT NULL,
    PRIMARY KEY (item_checklist_id, peca_id),
    FOREIGN KEY (item_checklist_id) REFERENCES itens_checklist(id) ON DELETE CASCADE,
    FOREIGN KEY (peca_id) REFERENCES pecas_reposicao(id) ON DELETE CASCADE
);

-- Relacionamento: Pecas <-> Modelos de Equipamento
CREATE TABLE pecas_modelos (
    peca_id BIGINT NOT NULL,
    modelo_id BIGINT NOT NULL,
    PRIMARY KEY (peca_id, modelo_id),
    FOREIGN KEY (peca_id) REFERENCES pecas_reposicao(id) ON DELETE CASCADE,
    FOREIGN KEY (modelo_id) REFERENCES modelos_equipamento(id) ON DELETE CASCADE
);

-- ============================================
-- PARTE 3: INSERIR DADOS INICIAIS
-- ============================================

-- Inserir usuário admin com senha criptografada BCrypt
-- Usuário: admin
-- Senha: admin
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZrVaL95.MdEH0pQRz4V8WvFn0gYdP3xQz5nXRzm
INSERT INTO usuarios (nome_completo, matricula, senha, perfil, email) 
VALUES (
    'Administrador Padrão',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZrVaL95.MdEH0pQRz4V8WvFn0gYdP3xQz5nXRzm',
    'ADMINISTRADOR',
    'admin@heimdex.com'
);

-- ============================================
-- FIM DO SCRIPT
-- ============================================
-- Banco de dados resetado com sucesso!
-- Login disponível: admin/admin
-- ============================================
