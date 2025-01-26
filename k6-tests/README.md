# Testes de Desempenho e Disponibilidade - E-commerce System

Este diretório contém os scripts de teste de desempenho e disponibilidade para o sistema e-commerce, implementados usando k6.

## Estrutura do Projeto

```
k6-tests/
├── lib/
│   └── utils.js           # Funções comuns e configurações
├── scenarios/
│   ├── load_test.js       # Teste de carga
│   └── stress_test.js     # Teste de estresse
├── TEST_PLAN.md           # Plano detalhado de testes
├── README.md              # Este arquivo
└── run_tests.ps1          # Script para executar os testes
```

## Tipos de Testes Implementados

### 1. Teste de Carga (Load Test)
- **Objetivo**: Avaliar o comportamento do sistema sob carga normal e pico moderado
- **Perfil de Carga**:
  - 0-2min: Rampa de 0 -> 50 usuários
  - 2-4min: Manter 50 usuários
  - 4-5min: Rampa de 50 -> 0 usuários
- **Métricas Coletadas**:
  - Tempo de resposta médio
  - Taxa de requisições por segundo (RPS)
  - Percentis (p95) de tempo de resposta
  - Taxa de sucesso/erro

### 2. Teste de Estresse (Stress Test)
- **Objetivo**: Encontrar limites do sistema e comportamento sob carga extrema
- **Perfil de Carga**:
  - 0-5min: Rampa de 0 -> 200 usuários
  - 5-8min: Manter 200 usuários
  - 8-10min: Rampa de 200 -> 0 usuários
- **Métricas Coletadas**:
  - Mesmas do teste de carga
  - Ponto de saturação do sistema
  - Tempo de recuperação

## Variações de Teste

Cada teste é executado em duas variações:
1. **Sem Tolerância a Falhas** (`ft=false`)
2. **Com Tolerância a Falhas** (`ft=true`)

## Métricas Analisadas

### Métricas de Desempenho
- **Tempo de Resposta**
  - Média
  - Percentil 95 (p95)
  - Máximo
- **Throughput**
  - Requisições por segundo (RPS)
  - Taxa de sucesso

### Métricas de Disponibilidade
- **Confiabilidade**
  - Taxa de erro
  - Taxa de sucesso
  - Tempo médio entre falhas (MTBF)
- **Resiliência**
  - Tempo de recuperação após falhas
  - Eficácia dos mecanismos de tolerância

## Pré-requisitos

1. Instalar k6:
   ```powershell
   winget install k6
   ```

2. Sistema e-commerce em execução:
   - Todos os serviços devem estar rodando
   - Endpoint `/buy` acessível em `http://localhost:8080`

## Como Executar os Testes

1. Abra um terminal PowerShell

2. Navegue até o diretório do projeto:
   ```powershell
   cd path/to/commerce-enviroment
   ```

3. Execute o script de testes:
   ```powershell
   .\k6-tests\run_tests.ps1
   ```

## Resultados

Os resultados serão salvos no diretório `k6-tests/results/` com a seguinte estrutura:
```
results/
├── load_test/
│   └── YYYYMMDD_HHMMSS_results.json
└── stress_test/
    └── YYYYMMDD_HHMMSS_results.json
```

## Análise de Resultados

Para cada teste, analise:
1. Tempo de resposta médio e percentis
2. Taxa de erro em diferentes níveis de carga
3. Eficácia da tolerância a falhas
4. Ponto de saturação do sistema
5. Comportamento sob carga extrema

## Thresholds (Limites Aceitáveis)

- **Teste de Carga**:
  - p95 tempo de resposta < 2s
  - Taxa de erro < 10%
  - Taxa de sucesso > 90%

- **Teste de Estresse**:
  - p95 tempo de resposta < 3s
  - Taxa de erro < 20%
  - Taxa de sucesso > 80%
