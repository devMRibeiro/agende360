# Instruções para o Agente Claude

## Linguagem
- Java tradicional, versão 11 ou superior
- Proibido usar: lambdas, streams, method references, funções anônimas
- Use laços for/while e classes internas nomeadas quando necessário

## Estrutura do projeto
- Código-fonte em: src/main/java
- Testes em: src/test/java
- Build com Maven (pom.xml)

## O que pode fazer
- Escrever e melhorar testes unitários com JUnit
- Adicionar Javadoc em classes e métodos públicos
- Corrigir warnings do compilador
- Melhorar tratamento de exceções

## O que NÃO deve fazer
- Alterar a arquitetura geral do projeto
- Remover classes existentes
- Alterar dependências no pom.xml sem necessidade clara