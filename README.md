# service-track-auth-lambda

Serviço de autenticação do ServiceTrack. Valida credenciais de usuário e emite tokens JWT.
Construído com Quarkus + Kotlin e empacotado para execução como AWS Lambda.

## Stack

- Kotlin 2.4 / JVM 21
- Quarkus 3.37.2
- Hibernate ORM com Panache (Kotlin)
- PostgreSQL (JDBC)
- SmallRye JWT (assinatura/verificação RS256 via chaves PEM)
- AWS Lambda (quarkus-amazon-lambda-rest)
- Gradle (wrapper)

## Arquitetura

Arquitetura hexagonal (ports & adapters):

```
src/main/kotlin/br/com/servicetrack/
  application/          Casos de uso e DTOs de entrada/saída
    dto/
    service/            AutenticacaoService
  domain/               Núcleo de negócio, sem dependências de framework
    model/              IdentidadeAutenticada, TokenAutenticacao
    vo/                 Value objects: Cpf, Senha, UsuarioId
    ports/in/           Contratos de entrada (IAutenticacaoUseCase)
    ports/out/          Contratos de saída (IVerificadorDeCredencialPort, ITokenProviderPort)
    exception/          Exceções de domínio
  infrastructure/       Adaptadores concretos
    adapter/resource/   Endpoints REST (AutenticacaoResource)
    adapter/security/   JwtTokenProvider
    adapter/usuarios/   Cliente REST do serviço de usuários
    adapter/web/        ExceptionMappers, RequestIdFilter
```

O `domain` não conhece Quarkus. As dependências apontam para dentro, das camadas externas
para os ports do domínio.

## Endpoints

### POST /autenticacao

Autentica um usuário por **CPF** e retorna um token JWT.

Requisição:

```json
{
  "cpf": "12345678901",
  "senha": "senhaForte123"
}
```

Resposta (200):

```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "expiraEmSegundos": 3600
}
```

Validações: `cpf` com 11 dígitos, com ou sem pontuação (`123.456.789-01` também é aceito);
`senha` com mínimo de 8 caracteres. Credenciais inválidas ou usuário inativo retornam erro de
credenciais.

> O path é `/autenticacao`, sem sufixo. É o mesmo que o contrato do API Gateway expõe em
> `apis/service-track-api-ext/openApi.yaml` no repositório `service-track-aws-iac`. Os dois
> precisam mudar juntos: divergência aqui quebra em runtime e nenhum teste de contrato atual
> detecta.

## Chaves JWT (PEM)

O serviço assina tokens com uma chave privada RSA e os verifica com a chave pública.

| Arquivo | Uso | Versionado |
|---------|-----|------------|
| `src/main/resources/publicKey.pem` | Verificação (`mp.jwt.verify.publickey.location`) | Sim (chave pública) |
| `src/main/resources/privateKey.pem` | Assinatura (`smallrye.jwt.sign.key.location`) | Não (gitignored) |

A chave privada nunca deve ser commitada. Ela está no `.gitignore` e a pipeline de CI
falha se qualquer chave privada for versionada.

**Em `hml` e `prd` o par não é gerado à mão.** O Terraform do
[service-track-aws-iac](https://github.com/Claudio712005/service-track-aws-iac) cria um par por
ambiente e o entrega a esta função e à aplicação no mesmo apply, por variável de ambiente
(`MP_JWT_VERIFY_PUBLICKEY`, `SMALLRYE_JWT_SIGN_KEY`). Ver `IAC-ADR-018`.

**Localmente o par tem que ser o mesmo da aplicação.** Ela verifica o token emitido aqui e
também assina com essa chave privada, nos tokens de decisão de orçamento. Dois pares
diferentes fazem todo login local terminar em 401 na aplicação — na nuvem o sintoma não
aparece, porque as duas pontas recebem a chave por variável de ambiente e o classpath é
ignorado.

O par versionado aqui é o mesmo de
`service-track-api/software/service-track-api/_infrastructure/src/main/resources/keys/`.
Para trabalhar local, copie a chave privada de lá:

```shell
cp ../../service-track-api/software/service-track-api/_infrastructure/src/main/resources/keys/privateKey.pem \
   src/main/resources/privateKey.pem
```

Gerar um par novo só faz sentido trocando os dois repositórios ao mesmo tempo:

```shell
openssl genpkey -algorithm RSA -out src/main/resources/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/privateKey.pem -out src/main/resources/publicKey.pem
```

## CI/CD

| Esteira | Dispara em | O que faz |
|---|---|---|
| **CI** | push e PR | `secret-guard` falha se houver chave privada versionada; `build` valida o wrapper do Gradle, roda os testes em JDK 21 e publica os relatórios |
| **CD** | push na `main`, ou Run workflow | publica a imagem e atualiza a função |

O CD constrói a imagem, publica em `servicetrack-<env>-auth-lambda`, aplica o portão de
vulnerabilidade e chama `aws lambda update-function-code` em `servicetrack-<env>-auth`.

Push na `main` entrega em `hml`. Promoção para `prd` é manual, e informando `image_tag`
promove uma imagem já publicada sem reconstruir.

Os nomes do repositório e da função são **derivados do ambiente**, não recebidos de fora: o
padrão é `servicetrack-<env>-*`. Por isso nenhum repositório precisa escrever variável neste.

A esteira confere que a função existe antes de construir, e para com mensagem explícita se a
infraestrutura do ambiente ainda não foi aplicada.

> O Terraform gerencia a infraestrutura da função, não o código
> (`lifecycle { ignore_changes = [image_uri] }`). Por isso o deploy não exige `terraform apply`
> — importante numa conta cujo login expira a cada laboratório.

### Secrets necessárias

| Secret | Onde |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Environments `hml` e `prd` deste repositório |

Renovadas a cada laboratório da AWS Academy.

---

## Configuração

Definida em `src/main/resources/application.properties`.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `ST_USU_BASE_URL` | `http://localhost:8080` | Base do serviço de usuários. Em AWS, a URL da API Gateway privada, lida de `/servicetrack/<env>/api-interna/base-url` no SSM |
| `servicetrack.jwt.expiracao-segundos` | `3600` | Validade do token em segundos |
| `mp.jwt.verify.issuer` | `service-track-api` | Issuer do JWT. Identificador do sistema, não nome de repositório |

Timeouts da chamada ao serviço de usuários: **1s para conectar, 2s para ler**. São agressivos de
propósito — a função é faturada por duração e o API Gateway corta em 29s, então esperar não é
gratuito.

O perfil `local` (`application-local.properties`) sobrescreve apenas o nível de log.

## Esta função não tem banco de dados

Ela **não lê tabela nenhuma**. Para conferir uma credencial, chama
`POST /credenciais/verificacao` no `service-track-usuarios-veiculos`, que compara o hash e
devolve a identidade confirmada com os papéis que entram no token.

Isso fecha a dívida `A-06`: antes existiam duas definições do mesmo schema, uma aqui e outra nas
migrations do monólito, sem dono único. Agora o dono do dado de usuário é um só, e esta função
não carrega credencial de banco.

**Ela continua dentro da VPC**, porque a API que ela chama é privada e só resolve lá dentro. O
que saiu foi a credencial, não o requisito de rede.

### Por que não há circuit breaker

O estado de um disjuntor vive em memória e morre com o contêiner da função. Com concorrência N,
são N disjuntores independentes, cada um precisando atingir o próprio limiar antes de abrir — o
ganho de "parar de insistir" fica dividido por N.

E não há resposta degradada possível: se o serviço de usuários está fora, autenticação não
acontece. O disjuntor trocaria "esperar e falhar" por "falhar rápido", que é exatamente o que o
timeout já faz, de forma determinística e sem estado.

Retentativa também não: o endpoint tem limite de requisições e devolve `429`, e insistir piora.
`401` nunca se retenta.

Tratamento por status:

| Resposta do serviço de usuários | O que a função devolve |
|---|---|
| `200` | token |
| `401`, `404` | `401`, credenciais inválidas |
| `429` | `503` |
| `5xx`, timeout, falha de conexão | `503` |

## Execução

Modo de desenvolvimento (live reload, Dev UI em <http://localhost:8080/q/dev/>):

```shell
./gradlew quarkusDev
```

Build e testes:

```shell
./gradlew build
```

### Execução local fora da AWS

O build padrão empacota a função com `quarkus-amazon-lambda-rest`, que substitui **em tempo
de build** o servidor HTTP por um despachante acionado pelo runtime da AWS. Rodar esse
artefato numa máquina sobe a aplicação e não abre porta nenhuma.

Para rodar como serviço HTTP comum — em compose, ou direto na máquina — existe a variante
`execucaoLocal`, que apenas deixa a extensão de fora. O restante é o mesmo código, os mesmos
recursos JAX-RS e os mesmos testes:

```shell
./gradlew build -x test -PexecucaoLocal=true
java -jar build/quarkus-app/quarkus-run.jar
```

```
Installed features: [agroal, cdi, ..., rest, security, smallrye-jwt, vertx]
Listening on: http://0.0.0.0:8080
```

O que vai para a AWS continua sendo o build sem a propriedade. Decisão em `GLOBAL-RFC-007`.

Executável nativo:

```shell
./gradlew build -Dquarkus.native.enabled=true
# Sem GraalVM local, buildar em container:
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

## Testes

```shell
./gradlew test
```

Testes de unidade usam fakes dos ports, sem necessidade de banco ou chaves PEM.

## Docker / Lambda

O `Dockerfile` na raiz faz build multi-stage (JDK 21) e empacota como imagem de Lambda
(`public.ecr.aws/lambda/java:21`), usando o handler
`io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler`.

```shell
docker build -t service-track-auth-lambda .
```

### Imagem para o compose local

O `Dockerfile.local` constrói a variante HTTP e entrega um runtime JVM comum. É a imagem que
o `docker-compose.yaml` de `service-track-api` consome como serviço `auth`:

```shell
docker build -f Dockerfile.local -t servicetrack-auth:local .
```

Não exige JDK na máquina — o build acontece dentro da imagem. Refazer sempre que o código de
autenticação mudar.


