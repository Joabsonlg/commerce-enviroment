# Plano de Testes de Desempenho e Disponibilidade

## 1. Descrição do Sistema

### 1.1 Arquitetura
- Sistema de e-commerce distribuído baseado em microsserviços
- Comunicação REST entre serviços
- 4 serviços principais:
  - E-commerce Service (Orquestrador)
  - Store Service (Produtos e Vendas)
  - Exchange Service (Taxas de Câmbio)
  - Fidelity Service (Programa de Fidelidade)

### 1.2 Tecnologias
- Spring Boot (Java)
- Docker para containerização
- REST para comunicação entre serviços
- Circuit Breaker com Resilience4j
- Cache local para tolerância a falhas

## 2. Planejamento dos Testes

### 2.1 Tipos de Testes

#### 2.1.1 Teste de Carga (Load Test)
- **Objetivo**: Avaliar o comportamento do sistema sob carga normal e pico
- **Duração**: 5 minutos
- **Cenários**:
  - Rampa gradual: 0 -> 50 VUs em 2 minutos
  - Plateau: 50 VUs por 2 minutos
  - Rampa de descida: 50 -> 0 VUs em 1 minuto
- **Variações**:
  - Com tolerância a falhas (ft=true)
  - Sem tolerância a falhas (ft=false)

#### 2.1.2 Teste de Estresse (Stress Test)
- **Objetivo**: Encontrar o ponto de quebra do sistema
- **Duração**: 10 minutos
- **Cenários**:
  - Rampa agressiva: 0 -> 200 VUs em 5 minutos
  - Plateau alto: 200 VUs por 3 minutos
  - Rampa de descida: 200 -> 0 VUs em 2 minutos
- **Variações**:
  - Com tolerância a falhas (ft=true)
  - Sem tolerância a falhas (ft=false)

### 2.2 Métricas de Desempenho

#### 2.2.1 Métricas de Tempo
- Tempo de resposta médio
- Percentis (p90, p95, p99)
- Taxa de requisições por segundo (RPS)

#### 2.2.2 Métricas de Recursos
- Uso de CPU
- Uso de memória
- Latência de rede entre serviços

### 2.3 Métricas de Disponibilidade

#### 2.3.1 Métricas de Confiabilidade
- Taxa de sucesso de requisições
- Taxa de erros
- Tempo médio entre falhas (MTBF)
- Tempo médio de recuperação (MTTR)

#### 2.3.2 Métricas de Tolerância
- Taxa de ativação do circuit breaker
- Taxa de uso do cache de fallback
- Tempo de recuperação após falhas

## 3. Estrutura dos Testes

### 3.1 Cenários Base
1. **Compra Simples**
   - Endpoint: POST /buy
   - Parâmetros: product=1, user=1
   - Variação: ft=true/false

2. **Compra com Falhas Programadas**
   - Store Service: Omission (20%) e Error (10%)
   - Exchange Service: Crash (10%)
   - Fidelity Service: Time (10%)

### 3.2 Thresholds (Limites Aceitáveis)
- Tempo de resposta p95 < 2s
- Taxa de erro < 1%
- Disponibilidade > 99.9%
- RPS sustentado > 100

## 4. Ambiente de Execução

### 4.1 Pré-requisitos
- Docker e Docker Compose instalados
- k6 instalado
- Todos os serviços em execução
- Rede isolada para testes

### 4.2 Monitoramento
- Logs dos serviços
- Métricas do Docker
- Dashboard k6
- Grafana (opcional)

## 5. Execução dos Testes

### 5.1 Ordem de Execução
1. Testes de carga sem tolerância a falhas
2. Testes de carga com tolerância a falhas
3. Testes de estresse sem tolerância a falhas
4. Testes de estresse com tolerância a falhas

### 5.2 Análise de Resultados
- Comparação de métricas com/sem tolerância
- Identificação de gargalos
- Avaliação da eficácia dos mecanismos de tolerância
- Recomendações de melhorias
