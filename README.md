# CDC Orders

Projeto prático para demonstrar **Change Data Capture (CDC)** utilizando
Spring Boot, PostgreSQL, Debezium, Kafka Connect e Apache Kafka.

O objetivo é mostrar como alterações realizadas no banco de dados podem ser
capturadas e propagadas para outros sistemas **sem que a aplicação responsável
pela escrita precise publicar eventos diretamente**.

## Arquitetura

```text
┌─────────────────┐
│   Spring Boot   │
│  Orders Service │
└────────┬────────┘
         │
         │ INSERT / UPDATE / DELETE
         ▼
┌─────────────────┐
│   PostgreSQL    │
└────────┬────────┘
         │
         │ WAL
         ▼
┌─────────────────┐
│ Logical Decoding│
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ Debezium PostgreSQL     │
│ Connector               │
└────────┬────────────────┘
         │
         │ executado pelo
         ▼
┌─────────────────┐
│  Kafka Connect  │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│      Apache Kafka       │
│ orders.public.orders    │
└─────────────────────────┘
```

### O que é integração de dados com CDC? 
CDC é uma abordagem para identificar e capturar alterações em uma fonte de dados. Ao realizar
`INSERT`, `UPDATE` ou `DELETE` em um banco, podemos capturar essas mudanças e
propagá-las para outros sistemas: replicação, analytics, execução de
regras de negócio etc.

### CDC pode ajudar a reduzir o acoplamento entre aplicações? 
Sim. Uma aplicação pode simplesmente persistir seus dados no banco, sem conhecer
os sistemas interessados nessas alterações. No caso de log-based CDC,
ela não precisa publicar eventos ou executar consultas adicionais para
informar que um dado mudou.

### Mas isso elimina o acoplamento? 
Não. O pipeline de CDC pode estar acoplado ao schema do banco e os consumidores precisam conhecer o contrato dos eventos produzidos. 
Alterar uma tabela ou coluna pode impactar esse fluxo.

### Como funciona a captura através dos logs do banco? 
Alterações no banco são registradas em seu transaction log. Uma ferramenta de CDC pode
acompanhar esse mecanismo, identificar as mudanças e transformá-las em
informações que possam ser propagadas. Mas o transaction log é uma
estrutura interna do banco, não um log amigável como: "cliente 1 alterou
sua idade para 30".

### Então, como isso funciona na prática?

Na implementação utilizei Spring Boot + PostgreSQL.

No PostgreSQL existe o WAL (Write-Ahead Log). Em operações de INSERT,
UPDATE ou DELETE, o PostgreSQL registra informações sobre as alterações
no WAL antes que as mudanças correspondentes precisem estar persistidas
nos arquivos de dados.

Importante: o WAL não foi criado para CDC. Ele é um mecanismo do
PostgreSQL usado, entre outras coisas, para recuperação e replicação. O
log-based CDC aproveita esse mecanismo para identificar mudanças.

Outros bancos possuem mecanismos equivalentes: MySQL → Binary Log
(binlog) Oracle → Redo Log

No PostgreSQL temos ainda o Logical Decoding, que permite interpretar as
mudanças do WAL em um nível lógico, adequado para replicação e CDC.

### E o que é o Debezium? 
O Debezium é uma plataforma open source especializada em CDC, com connectors para fontes como PostgreSQL, MySQL,
SQL Server, Oracle e MongoDB. No exemplo, usamos o Debezium PostgreSQL
Connector para capturar as mudanças disponibilizadas pelo PostgreSQL via
logical decoding.

### E onde entra o Kafka Connect? 
Kafka Connect é uma infraestrutura do ecossistema Kafka para integração com sistemas externos. Na
demonstração, ele é o runtime que executa o Debezium PostgreSQL
Connector e fornece a infraestrutura para publicar os registros em
tópicos Kafka.

No final: Spring Boot → PostgreSQL → WAL → Logical Decoding → Debezium
PostgreSQL Connector → Kafka Connect → Kafka

E o mais interessante: a aplicação Spring Boot não sabe que nada disso
existe. Ela simplesmente persiste seus dados no PostgreSQL.

### Em breve
Vou trazer outras implementações além de log-based para comparativo.