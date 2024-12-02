# Guia de Implantação - Sistema E-commerce Tolerante a Falhas

## Pré-requisitos

- Docker e Docker Compose
- Java 17
- Maven
- Git
- Slack Webhook URL (para alertas)
- API de câmbio (para Exchange Service)

## Estrutura do Sistema

```
tf-workspace/
├── services/
│   ├── ecommerce/    # Serviço principal de e-commerce
│   ├── store/        # Gerenciamento de produtos
│   ├── exchange/     # Conversão de moedas
│   ├── fidelity/    # Sistema de pontos/bônus
│   └── monitoring/   # Prometheus, Grafana e Alertmanager
```

## Passos para Implantação

### 1. Configuração Inicial

```bash
# Clone o repositório
git clone <repository-url>
cd tf-workspace

# Compile todos os serviços
cd services/ecommerce && mvn clean package
cd ../store && mvn clean package
cd ../exchange && mvn clean package
cd ../fidelity && mvn clean package
```

### 2. Configuração do Ambiente

1. **Variáveis de Ambiente**
   Crie um arquivo `.env` na raiz do projeto:
   ```env
   # Portas dos serviços
   ECOMMERCE_PORT=8080
   STORE_PORT=8081
   EXCHANGE_PORT=8082
   FIDELITY_PORT=8083

   # Redis
   REDIS_HOST=redis
   REDIS_PORT=6379

   # Monitoramento
   PROMETHEUS_PORT=9090
   GRAFANA_PORT=3000
   ALERTMANAGER_PORT=9093
   ```

2. **Configuração do Slack para Alertas**
   - Atualize o arquivo `services/monitoring/alertmanager/alertmanager.yml`
   - Substitua `YOUR_SLACK_WEBHOOK_URL` pela URL do seu webhook

### 3. Inicialização dos Serviços

```bash
# Inicie os serviços de infraestrutura
docker-compose -f services/monitoring/docker-compose.yml up -d

# Inicie os serviços da aplicação
docker-compose up -d
```

### 4. Verificação da Implantação

1. **Verificar Status dos Serviços**
   ```bash
   docker-compose ps
   ```

2. **Verificar Logs**
   ```bash
   docker-compose logs -f [service-name]
   ```

3. **Acessar Interfaces**
   - Ecommerce API: http://localhost:8080/swagger-ui.html
   - Store API: http://localhost:8081/swagger-ui.html
   - Exchange API: http://localhost:8082/swagger-ui.html
   - Fidelity API: http://localhost:8083/swagger-ui.html
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)
   - Alertmanager: http://localhost:9093

### 5. Monitoramento e Alertas

1. **Grafana**
   - Acesse http://localhost:3000
   - Login: admin
   - Senha: admin
   - Os dashboards são carregados automaticamente

2. **Alertas**
   - Configurados para notificar no Slack
   - Canais:
     - #monitoring: alertas gerais
     - #monitoring-critical: alertas críticos

### 6. Manutenção

1. **Backup**
   ```bash
   # Backup dos volumes Docker
   docker run --rm -v [volume-name]:/volume -v /backup:/backup alpine tar -czf /backup/volume.tar.gz /volume
   ```

2. **Atualização dos Serviços**
   ```bash
   # Atualizar imagens
   docker-compose pull

   # Reiniciar serviços
   docker-compose down
   docker-compose up -d
   ```

3. **Logs**
   - Os logs são coletados pelo Docker
   - Use `docker-compose logs -f [service]` para monitoramento em tempo real

### 7. Troubleshooting

1. **Verificar Saúde dos Serviços**
   - Acesse `/actuator/health` em cada serviço
   - Exemplo: http://localhost:8080/actuator/health

2. **Problemas Comuns**
   - Se o Redis estiver indisponível, o Exchange Service usará cache local
   - Circuit breakers podem estar abertos após falhas
   - Verifique métricas no Grafana para diagnóstico

3. **Reinício de Serviços**
   ```bash
   docker-compose restart [service-name]
   ```

## Considerações de Segurança

1. **Senhas e Credenciais**
   - Altere as senhas padrão do Grafana
   - Mantenha as credenciais em variáveis de ambiente
   - Use secrets do Docker em produção

2. **Rede**
   - Os serviços estão em uma rede Docker isolada
   - Apenas as portas necessárias são expostas
   - Configure firewalls adequadamente

3. **HTTPS**
   - Configure SSL/TLS em produção
   - Use certificados válidos
   - Redirecione HTTP para HTTPS

## Suporte

Para problemas ou dúvidas:
1. Verifique os logs dos serviços
2. Consulte as métricas no Grafana
3. Verifique os alertas no Alertmanager
4. Entre em contato com a equipe de suporte
