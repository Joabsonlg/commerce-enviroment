# TODO List - Projeto de Tolerância a Falhas

## 1. Revisão e Ajuste dos Serviços

### 1.1 Serviço E-commerce
- [x] Implementar endpoint POST `/buy` com parâmetros:
  - product (id)
  - user (id)
  - ft (boolean) - flag de tolerância a falhas
- [x] Implementar chamadas para outros serviços:
  - Store: GET `/product`
  - Exchange: GET `/exchange`
  - Store: POST `/sell`
  - Fidelity: POST `/bonus`
- [x] Implementar mecanismos de tolerância a falhas:
  - Exchange: Cache do último valor válido
  - Fidelity: Log e processamento assíncrono
  - Store: Circuit breaker com fallback
- [x] Configurar timeout global de 1s para todas as requisições
- [x] Implementar circuit breaker para cada serviço
- [x] Implementar logs detalhados para rastreamento de falhas

### 1.2 Serviço Store
- [x] Implementar endpoint GET `/product` retornando:
  - id
  - name
  - value
- [x] Implementar endpoint POST `/sell` retornando:
  - transaction_id (único)
- [x] Implementar falha: Omission (p=0.2, d=0s) no Request 1
- [x] Implementar falha: Error (p=0.1, d=5s) no Request 3
- [ ] Implementar persistência MongoDB

### 1.3 Serviço Exchange
- [x] Implementar endpoint GET `/exchange` retornando:
  - taxa de conversão (número real positivo)
- [x] Implementar falha: Crash (p=0.1, d=indefinido)
- [ ] Implementar persistência MongoDB

### 1.4 Serviço Fidelity
- [x] Implementar endpoint POST `/bonus` com parâmetros:
  - user (id)
  - bonus (inteiro)
- [x] Implementar falha: Time (p=0.1, d=30s, delay=2s)
- [ ] Implementar persistência MongoDB

## 2. Infraestrutura

### 2.1 Docker
- [ ] Revisar Dockerfiles de cada serviço
- [ ] Otimizar docker-compose.yml
- [ ] Configurar redes Docker corretamente
- [ ] Configurar volumes para persistência

### 2.2 Monitoramento
- [ ] Implementar health checks
- [ ] Configurar métricas do Actuator
- [ ] Implementar logs centralizados
- [ ] Configurar dashboard de monitoramento

## 3. Documentação

### 3.1 README.md
- [ ] Atualizar descrição do projeto
- [ ] Adicionar instruções detalhadas de instalação
- [ ] Documentar endpoints de cada serviço
- [ ] Adicionar exemplos de uso
- [ ] Documentar configurações de tolerância a falhas

### 3.2 Relatório
- [ ] Documentar estratégias de tolerância a falhas
- [ ] Explicar implementação de cada mecanismo
- [ ] Analisar limitações das soluções
- [ ] Discutir estratégias alternativas
- [ ] Preparar apresentação em vídeo

## 4. Testes

### 4.1 Testes Unitários
- [ ] Implementar testes para cada serviço
- [ ] Testar mecanismos de tolerância a falhas
- [ ] Testar diferentes cenários de falha

### 4.2 Testes de Integração
- [ ] Testar comunicação entre serviços
- [ ] Testar comportamento com falhas
- [ ] Testar timeout global
- [ ] Testar circuit breaker

### 4.3 Testes de Carga
- [ ] Testar comportamento sob carga
- [ ] Verificar tempos de resposta
- [ ] Analisar impacto das falhas
