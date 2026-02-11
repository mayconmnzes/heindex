# 🚀 Configuração do Render para Heimdex API

## 📋 Variáveis de Ambiente Necessárias

Para conectar ao MySQL Aiven, configure estas variáveis no Render:

### 1. Acessar Environment Variables
1. Vá em https://dashboard.render.com/
2. Clique no serviço **heindex-api**
3. Menu lateral → **Environment**
4. Clique em **Add Environment Variable**

### 2. Adicionar Variáveis

#### MySQL do Aiven:
```
DB_URL=jdbc:mysql://heindex-mayconmnzes-6dd6.c.aivencloud.com:24620/defaultdb?ssl-mode=REQUIRED&useSSL=true&allowPublicKeyRetrieval=true
DB_USER=avnadmin
DB_PASSWORD=<your-aiven-password-here>
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_DIALECT=org.hibernate.dialect.MySQLDialect
```

#### Cloudinary (opcional - se não usar as variáveis do application.properties):
```
CLOUDINARY_NAME=<your-cloudinary-name>
CLOUDINARY_KEY=<your-cloudinary-key>
CLOUDINARY_SECRET=<your-cloudinary-secret>
```

### 3. Fazer Deploy
1. Clique em **Save Changes**
2. Vá em **Manual Deploy**
3. Clique em **Clear build cache & deploy**
4. Aguarde 2-3 minutos

### 4. Verificar Logs
Após deploy, vá em **Logs** e procure por:

✅ **Sucesso:**
```
========================================
✅ Backend Heimdex conectado ao MySQL (Aiven)
🔗 Database: jdbc:mysql://heindex-mayconmnzes-6dd6...
========================================
>>> ✅ Usuário admin criado com sucesso!
>>> 📋 Login: admin
>>> 🔑 Senha: admin
```

❌ **Erro (H2 ativo):**
```
⚠️  Backend Heimdex usando banco H2 Local (desenvolvimento)
⚠️  ATENÇÃO: Configure as variáveis DB_URL, DB_USER, DB_PASSWORD no Render!
```

## 🔐 Credenciais Padrão

Após deploy com sucesso:
- **Login:** admin
- **Senha:** admin
- **Email:** admin@heindex.com

## 🆘 Troubleshooting

### Problema: Ainda aparece H2 nos logs
**Solução:** Verifique se as variáveis DB_URL, DB_USER, DB_PASSWORD estão corretas no Render

### Problema: Erro 401 ao fazer login
**Solução:** Execute no DBeaver/MySQL:
```sql
UPDATE usuarios SET perfil = 'ADMINISTRADOR' WHERE matricula = 'admin';
```

### Problema: Tabela usuarios não existe
**Solução:** O Hibernate cria automaticamente com `ddl-auto=update`
