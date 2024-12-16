# Plano de Desenvolvimento - E-commerce Tolerante a Falhas

## 1. Setup Inicial
- [x] Criar estrutura base do projeto
- [ ] Configurar Docker e Docker Compose
- [ ] Definir rede Docker para comunicação entre serviços
- [x] Configurar ambiente de desenvolvimento
- [ ] Criar Dockerfile base para os serviços

## 2. Desenvolvimento dos Serviços

### 2.1 Ecommerce Service (`/buy`)
- [x] Implementar endpoint REST
- [x] Configurar timeout global
- [x] Implementar circuit breaker para chamadas aos serviços
- [x] Implementar lógica de orquestração
- [x] Adicionar logs e métricas
- [ ] Implementar tratamento de erros
- [ ] Testes unitários

### 2.2 Store Service (`/product`, `/sell`)
- [x] Implementar endpoints REST
- [x] Criar modelo de dados para produtos
- [x] Implementar lógica de consulta de produtos
- [x] Implementar lógica de venda
- [x] Adicionar logs e métricas
- [x] Implementar tratamento de erros
- [ ] Testes unitários

### 2.3 Exchange Service (`/exchange`)
- [x] Implementar endpoint REST
- [x] Configurar sistema de cache
  - [x] Implementar TTL
  - [x] Implementar fallback para último valor
- [ ] Configurar replicação
- [ ] Implementar load balancing
- [x] Adicionar logs e métricas
- [ ] Implementar tratamento de erros
- [ ] Testes unitários

### 2.4 Fidelity Service (`/bonus`)
- [x] Implementar endpoint REST
- [x] Criar modelo de dados para pontos
- [x] Implementar sistema de fila para bônus pendentes
- [x] Implementar lógica de retry para bônus falhos
- [x] Adicionar logs e métricas
- [x] Implementar tratamento de erros
- [ ] Testes unitários

## 3. Implementação de Mecanismos de Tolerância

### 3.1 Circuit Breaker
- [x] Implementar padrão circuit breaker
- [x] Configurar estados (fechado, aberto, meio-aberto)
- [x] Definir thresholds
- [x] Implementar métricas de estado

### 3.2 Cache
- [x] Implementar sistema de cache distribuído
- [x] Configurar TTL
- [x] Implementar estratégia de invalidação
- [x] Configurar armazenamento de fallback

### 3.3 Retry Pattern
- [x] Implementar backoff exponencial
- [x] Configurar número máximo de tentativas
- [x] Implementar delay entre tentativas
- [x] Adicionar logs de retry

## 4. Cenários de Falha
- [x] Implementar falhas programadas no Exchange Service
- [x] Implementar latência artificial
- [x] Implementar indisponibilidade temporária no Fidelity Service
- [ ] Criar scripts de teste de falha

## 5. Monitoramento
- [x] Configurar coleta de métricas
  - [x] Endpoints Prometheus configurados
  - [x] Métricas de latência (@Timed)
  - [x] Métricas de circuit breaker
  - [x] Métricas de cache
  - [x] Métricas de sistema
- [x] Implementar health checks
  - [x] Ecommerce: Verificação de serviços externos
  - [x] Store: Monitoramento de estoque
  - [x] Exchange: Verificação do Redis
  - [x] Fidelity: Monitoramento de bônus pendentes
- [x] Configurar dashboards
  - [x] Configuração do Prometheus
  - [x] Configuração do Grafana
  - [x] Dashboard de latência
  - [x] Dashboard de circuit breaker
  - [x] Dashboard de métricas de negócio
- [x] Implementar alertas
  - [x] Alertas de latência alta
  - [x] Alertas de circuit breaker
  - [x] Alertas de taxa de erro
  - [x] Alertas de estoque baixo
  - [x] Alertas de atraso no processamento de bônus
  - [x] Alertas de serviço indisponível
  - [x] Integração com Slack

## 6. Testes
- [ ] Testes de integração
- [ ] Testes de carga
- [ ] Testes de resiliência
- [ ] Testes de failover
- [ ] Documentar resultados dos testes

## 7. Documentação
- [x] Criar guia de implantação
  - [x] Pré-requisitos
  - [x] Estrutura do sistema
  - [x] Passos de implantação
  - [x] Configuração do ambiente
  - [x] Monitoramento e alertas
  - [x] Manutenção e troubleshooting
  - [x] Considerações de segurança
- [x] Documentar APIs (Swagger/OpenAPI)
- [ ] Criar guia de desenvolvimento

## 8. Deploy e CI/CD
- [x] Configurar pipeline CI/CD
  - [x] Pipeline de CI (GitHub Actions)
    - [x] Validação de código
    - [x] Testes unitários
    - [x] Build dos serviços
    - [x] Testes de integração
    - [x] Análise Sonar
  - [x] Pipeline de CD (GitHub Actions)
    - [x] Publicação de imagens Docker
    - [x] Deploy em staging
    - [x] Testes em staging
    - [x] Deploy em produção
    - [x] Notificações Slack
- [ ] Criar scripts de deploy
- [ ] Implementar estratégia de backup

## Ordem de Desenvolvimento Sugerida
1. Setup inicial do projeto
2. Store Service (mais simples, base para os outros)
3. Exchange Service (com replicação)
4. Fidelity Service (com sistema de fila)
5. Ecommerce Service (orquestrador)
6. Implementação dos mecanismos de tolerância
7. Cenários de falha e testes
8. Monitoramento e documentação
