# Trabalho Interdisciplinar 
#### (Programação orientada a objeto, Gerenciamento de dados)

___

### [Tarefas](./trabalhoInterdisciplinar.pdf)
- #### Q1 (1,0)
  1. [x] Criar uma tabela `tb_pokemon` com as colunas: id **INT**, pokemon (nome) **VARCHAR** e tipo **VARCHAR**.  
     - **=>** [tb_pokemon.sql](sql/tb_pokemon.sql)
  2. [x] Inserir dados via programação.
        - **=>** [Main.java](src/Main.java#L22)

- #### Q2 (2,0)
  1. [x] Criar outras tabelas `tb_pokemon_eletrico`, `tb_pokemon_fogo` e `tb_pokemon_voador)`.
     - **=>** [tb_pokemon_eletrico.sql](sql/tb_pokemon_eletrico.sql), [tb_pokemon_fogo.sql](sql/tb_pokemon_fogo.sql), [tb_pokemon_voador.sql](sql/tb_pokemon_voador.sql)
  2. [x] Preencher conforme o tipo de cada pokemon. Buscando os dados da tabela principal e inserindo em sua respectiva tabela (para essas tabelas o id não pode ser auto incrementado).
     - **=>** [DBSet.java](src/DBSet.java#L170)

- #### Q3 (2,0)
  1. [x] Programaticamente controle a inserção dos pokemons, não permitindo a existencia de pokemons duplicados na tabela do seu tipo específico.
     - **=>** [DBSet.java](src/DBSet.java#L13)

- #### Q4 (1,0)
  1. [x] Criar uma tabela `tb_pokemon_totalizador`
     - **=>** [tb_pokemon_totalizador.sql](sql/tb_pokemon_totalizador.sql)
  2. [x] Registrar quantidade de cada pokemon pelo seu tipo a partir do ArrayList recuperado do banco de dados. Exemplo: 2 elétricos, 2 de fogo, 2 duplicados, etc.
        - **=>** [DBSet.java](src/DBSet.java#L207)

- #### Q5 (1,0)
  1. [x] Fazer o delete dos pokemons duplicados na tabela principal `tb_pokemon`
     - **=>** [DBUtils.java](src/DBUtils.java#L112)
  2. [x] Jogar dentro da tabela `tb_pokemon_deletados`
     - **=>** [DBSet.java](src/DBSet.java#L109)

- #### Q6 (2,0)
  - Validação de Dados Através de Script SQL e Classe Java
    2. [x] Desenvolver script SQL que extraia informações relevantes da tabela `tb_pokemon` e das tabelas de tipo (elétrico, fogo, voador). O script deve gerar uma consulta que inclua a **contagem de Pokémons por tipo, a lista de IDs e nomes, e qualquer outra informação relevante para validação**.
        - **=>** [getValidationInfo.sql](sql/getValidationInfo.sql)
  
    - A classe deve ser capaz de:
        - [x] **Carregar os Dados**: Implemente métodos que conectem ao banco de dados, executem o script SQL e armazenem os resultados em uma estrutura adequada, como um ArrayList ou HashMap.
          - **=>** [Validation.java](src/Validation.java#L15) 
        - [x] **Validar os Resultados**: Compare os dados recuperados do banco de dados com os totalizadores esperados e as regras de negócio definidas nas questões anteriores, como a **não duplicidade em tabelas específicas de tipos** e a **integridade dos dados após exclusões**.
          - **=>** [Validation.java](src/Validation.java#L82)
___

### [Tabelas](./sql)

#### `tb_pokemon`
  - `id` **INT** NOT NULL Primary Key
    - `pokemon` **VARCHAR(50)** NOT NULL
    - `tipo` **VARCHAR(20)** NOT NULL
     
#### `tb_pokemon_eletrico`
  - `id` **INT** NOT NULL Foreign Key
    
#### `tb_pokemon_fogo`
  - `id` **INT** NOT NULL Foreign Key
    
#### `tb_pokemon_voador`
  - `id` **INT** NOT NULL Foreign Key

#### `tb_pokemon_totalizador`
  - `id` **INT** NOT NULL Primary Key
    - `tipo` **VARCHAR(10)** NOT NULL
    - `quantidade` **INT** NOT NULL

#### `tb_pokemon_deletados`
  - `id` **INT** NOT NULL Primary Key
    - `pokemon` **VARCHAR(50)** NOT NULL
    - `tipo` **VARCHAR(20)** NOT NULL

___

### [Classes](./src)

#### [Main](./src/Main.java)
- Classe principal (de teste)

#### [Pokemon](./src/Pokemon.java)
- Classe que representa o objeto da tabela `tb_pokemon`
- Atributos: `id` **int**, `nome` **String** e `tipo` **String**
- Métodos:
    - Construtores:
        - `Pokemon(int id, String nome, String tipo)`
        - `Pokemon(String nome, String tipo)` (ID nulo)
    - `toString()` retorna uma representação legivel em String (Necessária pra printar no console)
    - `getId()`, `getNome()` e `getTipo()` retornam os respectivos atributos

#### [Quantifier](./src/Quantificador.java)
- Classe que representa uma contagem da tabela `tb_pokemon_totalizador`
- Atributos: `id` **int**, `tipo` **String** e `quantidade` **int**
- Métodos:
    - Construtores:
        - `Quantifier(int id, String tipo, int quantidade)`
        - `Quantifier(String tipo, int quantidade)` (ID nulo)
    - `toString()` retorna uma representação legivel em String (Necessária pra printar no console)
    - `getId()`, `getTipo()` e `getQuantidade()` retornam os respectivos atributos

#### [Utils](./src/Utils.java)
- Classe estática com métodos utilitários
- Métodos:
    - `equalsInsensitive(String a, String b)` compara duas strings ignorando maiúsculas, minúsculas e acentos
    - `normalizeAcento(String str)` remove acentos de uma string

#### [ErrorHandler](./src/ErrorHandler.java)
- Classe estática que trata exceções
- Métodos:
    - `handleError(Exception e)` printa o erro formatado no console

#### [DBConnection](./src/DBConnection.java)
- Classe que cuida da conexão com a DB
- Métodos:
    - `getConnection()` retorna uma conexão com a DB

#### [DBTables](./src/DBTables.java)
- Classe de verificações das tabelas
- Métodos:
    - `checkTables()` verifica a existencia das tabelas e dos quantificadores na `tb_pokemon_totalizador`
    - `createTables()` cria as tabelas e os quantificadores caso não existam

#### [DBGet](./src/DBGet.java)
- Classe com métodos de busca na DB
- Métodos:
    - `getPokemons()` retorna uma lista com todos os pokemons da tabela `tb_pokemon`
    - `getPokemon(List<Integer> ids)` retorna uma lista com todos os pokemons da tabela `tb_pokemon` com os ids especificados
    - `getPokemon(String tipo)` retorna uma lista com todos os pokemons da tabela do tipo especificado
    - `getTypeTable(String tipo)` retorna uma lista de ids dos pokemons da tabela do tipo especificado
    - `getCountTable()` retorna uma lista com todos os quantificadores da tabela `tb_pokemon_totalizador`
    - `getDuplicatesInMainTable()` retorna uma lista com os pokemons duplicados na tabela principal `tb_pokemon`
    - `getTypeTables()` retorna uma lista com os nomes das tabelas de tipos
    - `duplicateInMainTable(String nome, String tipo)` retorna verdadeiro ou falso se já existe um pokemon com o mesmo nome e tipo na tabela principal `tb_pokemon`
    - `duplicateInTypeTable(String nome, String tipo)` retorna verdadeiro ou falso se já existe um pokemon com o mesmo nome na tabela do tipo especificado

#### [DBSet](./src/DBSet.java)
- Classe com métodos de inserção na DB
- Métodos:
    - `insertPokemon(Pokemon pokemon)` insere um pokemon na tabela `tb_pokemon`
    - `insertPokemon(List<Pokemon> pokemons)` insere uma lista de pokemons na tabela `tb_pokemon`
    - `insertTypeTable(String tipo, Set<Integer> ids)` insere uma lista de ids na tabela do tipo especificado
    - `deletePokemon(List<Integer> ids)` deleta os pokemons com os ids especificados da tabela `tb_pokemon`
    - `purgeTables` deleta todos os dados das tabelas e reinicia os auto increment
    - `refreshTypeTables()` atualiza as tabelas de tipos com base na tabela principal `tb_pokemon`
    - `updateCountTable()` atualiza a tabela de contagem `tb_pokemon_totalizador`
    - `deleteDuplicatesInMainTable()` deleta os pokemons duplicados na tabela principal `tb_pokemon`

#### [Validation](./src/Validation.java)
- Classe de validação
- Métodos:
    - `loadDB()` carrega os dados da DB com a procedure `getValidationInfo()`
    - `validateDB()` valida os dados carregados com base nas regras de negócio
