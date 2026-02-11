# Database Scripts - Heimdex

Este diretório contém scripts para gerenciamento do banco de dados PostgreSQL do sistema Heimdex.

## Arquivos

### reset_database.sql

Script completo de reset do banco de dados que:

1. **Remove todas as tabelas existentes** (em ordem correta respeitando foreign keys)
2. **Recria toda a estrutura** do banco baseada nos modelos JPA/Hibernate
3. **Insere o usuário administrador padrão** com senha criptografada BCrypt

#### Como Usar

**Via DBeaver ou outro cliente PostgreSQL:**

1. Conecte-se ao banco de dados PostgreSQL (Aiven ou local)
2. Abra o arquivo `reset_database.sql`
3. Selecione todo o conteúdo (Ctrl+A)
4. Execute o script (Ctrl+Enter ou F5)
5. Aguarde a conclusão
6. Teste o login: `admin` / `admin`

**Via linha de comando:**

```bash
psql -h <host> -U <usuario> -d <database> -f reset_database.sql
```

#### ⚠️ ATENÇÃO

Este script **APAGA TODOS OS DADOS** do banco de dados. Execute apenas quando:

- Estiver configurando um ambiente novo
- Precisar limpar dados de teste/desenvolvimento
- Tiver certeza de que não precisa dos dados existentes
- Tiver um backup dos dados importantes

#### Credenciais Padrão

Após executar o script, você poderá fazer login com:

- **Usuário:** admin
- **Senha:** admin
- **Perfil:** ADMINISTRADOR
- **Email:** admin@heimdex.com

**IMPORTANTE:** Altere a senha padrão após o primeiro login em ambiente de produção!

## Estrutura do Banco de Dados

O script cria as seguintes tabelas:

### Tabelas Base
- `usuarios` - Usuários do sistema
- `areas` - Áreas de produção
- `modelos_equipamento` - Modelos de equipamentos
- `pecas_reposicao` - Peças de reposição

### Tabelas de Produção
- `linhas_de_producao` - Linhas de produção
- `equipamentos` - Equipamentos cadastrados

### Tabelas de Manutenção
- `checklists` - Templates de checklist
- `itens_checklist` - Itens dos checklists
- `ordens_servico` - Ordens de serviço
- `resultados_checklist` - Resultados de execução de checklist
- `planos_manutencao` - Planos de manutenção preventiva

### Tabelas de Suporte
- `fotos_os` - Fotos anexadas às ordens de serviço
- `movimentacoes_estoque` - Movimentações de estoque de peças

### Tabelas de Relacionamento (Many-to-Many)
- `os_tecnicos_executores` - Técnicos executores de OS
- `item_pecas_sugeridas` - Peças sugeridas para itens de checklist
- `pecas_modelos` - Relacionamento peças-modelos

## Validação

O script foi testado e validado em PostgreSQL 12+. Todas as foreign keys estão configuradas corretamente com as ações apropriadas de CASCADE e SET NULL.
