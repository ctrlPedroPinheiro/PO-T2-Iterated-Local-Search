# PO-T2-Iterated-Local-Search

Trabalho da disciplina de **Pesquisa Operacional** aplicando a meta-heurística **Iterated Local Search (ILS)** ao **Problema do Caixeiro Viajante (PCV/TSP)**.

## 1) Objetivo do projeto

O objetivo é encontrar uma rota de menor custo que:

- visite todos os vértices exatamente uma vez;
- retorne ao vértice de origem;
- minimize a soma das distâncias da rota.

Como o TSP é um problema combinatório difícil para busca exata em instâncias maiores, o projeto usa uma abordagem heurística:

1. gera uma solução inicial gulosa;
2. melhora localmente com **2-opt**;
3. aplica ciclos de perturbação + nova busca local (**ILS**);
4. mantém a melhor solução global encontrada.

## 2) Estrutura do projeto

```text
PO-T2-Iterated-Local-Search/
├── README.md
└── POT2/
    ├── src/
    │   ├── module-info.java
    │   ├── grafoG.txt
    │   ├── grafoH.txt
    │   ├── grafoI.txt
    │   ├── grafoJ.txt
    │   └── ils/
    │       └── Main.java
    └── bin/
```

Arquivo principal do algoritmo: `POT2/src/ils/Main.java`.

## 3) Formato dos arquivos de entrada (grafos)

Cada arquivo `.txt` contém:

1. **Primeira linha**: `nVertices nArestas`
2. **Linhas seguintes**: matriz de adjacência `n x n`, com os custos entre os vértices.

Exemplo (`grafoG.txt`):

```text
4 6
0 7 1 3
7 0 5 8
1 5 0 6
3 8 6 0
```

## 4) Como compilar e executar

No diretório raiz do repositório:

```bash
javac POT2/src/module-info.java POT2/src/ils/Main.java
java -cp POT2/src ils.Main
```

Ao executar, escolha o grafo:

- `1` → `grafoG.txt` (4 vértices)
- `2` → `grafoH.txt` (10 vértices)
- `3` → `grafoI.txt` (50 vértices)
- `4` → `grafoJ.txt` (100 vértices)

Execução não interativa (exemplo com grafo H):

```bash
printf '2\n' | java -cp POT2/src ils.Main
```

## 5) Passo a passo do processo no código

Esta seção descreve o fluxo completo executado em `main`.

### Passo 1 — Seleção do grafo

O programa exibe um menu e lê a opção com `Scanner`.

- Define um caminho base: `POT2/src/`
- Mapeia a opção para o arquivo correspondente (`grafoG/H/I/J.txt`)

### Passo 2 — Medição de tempo

Antes da execução do algoritmo, armazena `startTime = System.nanoTime()`.

No final, calcula:

- duração em nanossegundos;
- milissegundos;
- segundos formatados.

### Passo 3 — Leitura do arquivo (`lerArquivo`)

Método: `public static int[][] lerArquivo(String caminho)`

Fluxo:

1. abre o arquivo com `File` + `Scanner`;
2. lê `nVertices` e `nArestas`;
3. cria `int[][] grafo = new int[nVertices][nVertices]`;
4. preenche a matriz inteira lendo os valores do arquivo;
5. retorna a matriz de adjacência.

Se o arquivo não existir, imprime erro e retorna `null`.

### Passo 4 — Definição de parâmetros

No `main`, após leitura do grafo:

- `maxIteracoes = 100`
- `origem = 0`

### Passo 5 — Solução inicial gulosa (`gerarSolucaoInicial`)

Método: `public static int[] gerarSolucaoInicial(int[][] matriz, int origem)`

Como funciona:

1. começa no vértice de origem;
2. marca vértices visitados;
3. em cada passo, escolhe o **vizinho não visitado de menor custo**;
4. monta um vetor `ciclo` de tamanho `n + 1`;
5. fecha o ciclo com `ciclo[n] = origem`.

Resultado: rota inicial viável para o TSP.

### Passo 6 — Avaliação do custo (`calcularCustoTotal`)

Método: `public static int calcularCustoTotal(int[][] matriz, int[] ciclo)`

Como funciona:

- percorre os arcos consecutivos da rota (`ciclo[i] -> ciclo[i+1]`);
- soma os custos na matriz de adjacência;
- retorna o custo total.

Observação: o ciclo já vem fechado (primeiro vértice repetido no final), então a soma percorre apenas os pares consecutivos.

### Passo 7 — Primeira busca local 2-opt (`buscaLocal`)

Método: `public static int[] buscaLocal(int[] ciclo, int[][] matriz)`

Lógica:

1. tenta pares de índices `(i, j)` dentro do ciclo;
2. calcula o `delta` de troca de arestas:
   - remove `(a,b)` e `(c,d)`
   - adiciona `(a,c)` e `(b,d)`
3. se `delta < 0`, aplica melhora invertendo o segmento `i..j`;
4. repete até não encontrar mais melhoria.

A inversão é feita por `inverterSegmento(int[] ciclo, int i, int j)`.

### Passo 8 — Loop principal do ILS

Executa `maxIteracoes` vezes:

1. **Perturbação** da melhor solução atual (`doubleBridge`);
2. **Refino local** da solução perturbada (`buscaLocal`);
3. **Avaliação** do custo refinado;
4. **Critério de aceite**: se custo refinado for menor que o melhor global, atualiza a melhor solução.

### Passo 9 — Perturbação (`doubleBridge` e `swap`)

Método principal: `public static int[] doubleBridge(int[] ciclo)`

- Para instâncias maiores (`n >= 8`):
  - faz 3 cortes aleatórios no ciclo;
  - reorganiza 4 blocos (A, D, C, B);
  - fecha o ciclo no final.
- Para instâncias pequenas:
  - usa fallback `swap(int[] ciclo)`, trocando dois vértices aleatórios (exceto origem/fim).

Objetivo da perturbação: escapar de ótimos locais mantendo estrutura de rota válida.

### Passo 10 — Saída final

O programa imprime:

- rota inicial e custo inicial;
- custo após a primeira busca local;
- eventuais melhorias por iteração do ILS;
- tempo total de execução;
- melhor rota final e menor custo encontrado.

## 6) Descrição resumida dos métodos

- `main(String[] args)`: orquestra todo o processo.
- `lerArquivo(String caminho)`: lê instância e monta matriz.
- `gerarSolucaoInicial(int[][], int)`: constrói solução gulosa.
- `calcularCustoTotal(int[][], int[])`: calcula custo da rota.
- `buscaLocal(int[], int[][])`: aplica 2-opt até estabilizar.
- `inverterSegmento(int[], int, int)`: utilitário do 2-opt.
- `imprimirSolucao(int[])`: imprime rota no console.
- `doubleBridge(int[])`: perturbação principal do ILS.
- `swap(int[])`: perturbação simples para instâncias pequenas.

## 7) Parâmetros e comportamento atual

- Iterações do ILS: `100`
- Origem fixa: vértice `0`
- Critério de aceite: somente melhora estrita (`custoRefinado < melhorCustoGlobal`)
- Aleatoriedade: `java.util.Random` sem semente fixa (resultados podem variar entre execuções)

## 8) Exemplo de fluxo esperado de saída

```text
Escolha o grafo...
Grafo
Nº de Vértices: ...
Nº de Arestas: ...

Solução Inicial Gulosa Gerada.
...rota...
Custo Inicial: ...

Realizando primeira busca local...
...rota melhorada...
Custo pós melhora local : ...

Iteração X: Nova melhor solução encontrada
Custo: ...

Tempo de execução do ILS: ... ms (... s)
Resultado Final do ILS
Melhor Rota: ...
Menor Custo Encontrado: ...
```

## 9) Integrantes

`Bruna, Lucas, Messias, Nathalia, Pedro e Wellington`
