# Especificação do Projeto – Parte 1 
## (Tolerância a Falhas)

**Objetivo:**  
Adotar estratégias e técnicas de tolerância a falhas no sistema de exemplo

**Equipe:**  
Máximo 3 pessoas, mas todos precisam participar da apresentação

### Requisitos gerais
- Linguagem e frameworks de livre escolha
- Uso de containers dockers para executar cada um dos serviços
- Todas as requisições aos serviços devem usar REST
- Todas as requisições devem ter um timeout de 1s
- Todas as implementações dos mecanismos de tolerância a falhas devem ser condicionadas ao valor de um parâmetro que será passado no Request 0. Em outras palavras, deve-se ser possível ligar e desligar a tolerância a falhas apenas mudando o valor de um parâmetro de requisição.

---

## Visão Geral do Sistema

![Visão Geral do Sistema](data:image)

---

## Especificação das Requisições

### Request 0:
Algum agente externo (postman, curl, etc.) envia um request para o E-commerce, via POST para o endpoint `/buy`, com os seguintes parâmetros:
- **product** – id do produto a ser comprado
- **user** – id do usuário que está executando a compra
- **ft** – parâmetro que vai indicar se a tolerância a falhas está ativada ou não (true ou false) 

Após todo o processamento o agente externo recebe uma resposta indicando se a compra foi realizada com sucesso (**HTTP Response Code**) e um identificador aleatório da transação que será gerada no **Request 3**.

---

### Request 1:
O E-commerce envia um request para o Store, via GET para o endpoint `/product`, com os seguintes parâmetros:
- **product** – id do produto a ser comprado

A resposta deve ser um JSON com os dados do produto consultado e deve ter as seguintes informações:  
**id**, **name**, **value**.

---

### Request 2:
O E-commerce envia um request para o Exchange, via GET para o endpoint `/exchange`, sem parâmetros.  
A resposta deve ser um número real positivo que indica a taxa de conversão da moeda.

---

### Request 3:
O E-commerce envia um request para o Store, via POST para o endpoint `/sell`, com os seguintes parâmetros:
- **product** – id do produto a ser comprado

A resposta deve conter um id único da transação (gerado automaticamente) que representa essa venda.

---

### Request 4:
O E-commerce envia um request para o Fidelity, via POST para o endpoint `/bonus`, com os seguintes parâmetros:
- **user** – id do usuário que está executando a compra
- **bonus** – um valor inteiro mais próximo do valor do produto antes da conversão

A resposta deve indicar o sucesso da operação (**HTTP Response Code**).

---

## Especificação das Falhas

As falhas são representadas no formato `Fail (Type, Probability, Duration)`, com o seguinte significado:
- **Type:** O tipo de falha (**Omission**, **Error**, **Crash=Stop**, **Time**)
- **Probability:** A probabilidade de a falha ocorrer
- **Duration:** Quando a falha acontece, por quanto tempo o componente fica neste mesmo estado de falha

### Estas falhas podem acontecer em cada uma das requisições da seguinte forma:
- **Request 1:** `Fail (Omission, 0.2, 0s)`
- **Request 2:** `Fail (Crash, 0.1, _ )`
- **Request 3:** `Fail (Error, 0.1, 5s)`
- **Request 4:** `Fail (Time=2s, 0.1, 30s)`

---

## Especificação dos Mecanismos de Tolerância a Falhas

O serviço de E-commerce precisa implementar mecanismos para tolerar as falhas nos outros serviços, de acordo com os seguintes requisitos:
- **Falha no Exchange** → Estratégia: Usar o último valor válido de taxa de conversão
- **Falha no Fidelity** → Estratégia: Registrar no log e processar quando possível
- **Falha no Store** → Estratégia: Use seus conhecimentos. Faça seu nome!

---

## Forma de entrega
- Link para o(s) repositório(s) no github, com todo o código, especificação dos containers e orientação no README sobre como rodar o seu sistema
- Relatório contendo os seguintes itens:
  - Explicação das estratégias adotadas para tolerar cada uma das falhas;
  - Indicação de onde e como elas foram implementadas;
  - Análise das limitações do que você fez;
  - Indicação de outras estratégias que poderiam ter sido usadas com base no que foi apresentado durante a disciplina.
- Vídeo de aproximadamente 20 min apresentando o relatório (**todos os participantes devem apresentar**).
