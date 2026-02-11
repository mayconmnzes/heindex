# 🔄 Migration Runners

## O que são?

Os runners de migration são tarefas que executam automaticamente ao iniciar a aplicação para:
- Regenerar QR Codes faltantes (`RegenerateQrRunner`)
- Preencher códigos de controle vazios (`FillCodigoControleRunner`)

## Por que estão desabilitados por padrão?

Esses runners tentam acessar tabelas do banco de dados. Se o banco estiver vazio (primeira execução), eles causam erro porque as tabelas ainda não foram criadas.

## Como habilitar?

### Apenas para migração de dados antigos:

1. **No Render**, adicione a variável de ambiente temporariamente:
   ```
   SPRING_PROFILES_ACTIVE=migration
   ```

2. Faça o deploy

3. **IMPORTANTE:** Após a migração ser concluída, **remova** a variável `SPRING_PROFILES_ACTIVE` e faça redeploy

### Localmente:

Execute com o profile de migration:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=migration
```

Ou no IntelliJ/Eclipse:
- Active Profiles: `migration`

## ⚠️ Avisos

- **NÃO** deixe `SPRING_PROFILES_ACTIVE=migration` ativo em produção permanentemente
- Esses runners devem executar apenas UMA VEZ para corrigir dados antigos
- Em bancos novos/vazios, eles não são necessários
