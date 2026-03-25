// T2-PO-Bruna-Lucas-Messias-Nathalia-Pedro-Wellington
package ils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class Main {

	private static final Random random = new Random();

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
	    // Carregamento do grafo escolhido
		System.out.println("Escolha o grafo que será carregado:"
				+ "\n(1) Grafo G | Principal | 4 Vértices   | 6 Arestas"
				+ "\n(2) Grafo H | Teste     | 10 Vértices  | 45 Arestas"
				+ "\n(3) Grafo I | Teste     | 50 Vértices  | 1225 Arestas"
				+ "\n(4) Grafo J | Teste     | 100 Vértices | 4950 Arestas");
		
		int escolhaGrafo = scanner.nextInt();
		String caminhoRelativo = "POT2" + File.separator + "src" + File.separator;
		String grafoEscolhido = caminhoRelativo + "grafoG.txt";
		if (escolhaGrafo == 2) {
			grafoEscolhido = caminhoRelativo + "grafoH.txt";
		} else if (escolhaGrafo == 3) {
			grafoEscolhido = caminhoRelativo + "grafoI.txt";
		} else if (escolhaGrafo == 4) {
			grafoEscolhido = caminhoRelativo + "grafoJ.txt";
		}

	    long startTime = System.nanoTime();

		int[][] grafo = lerArquivo(grafoEscolhido);
		
		scanner.close();
	    if (grafo == null) return;
	    
	    // Parâmetros
	    int maxIteracoes = 100; 
	    int origem = 0;

	    // Gerando solução inicial
	    int[] melhorSolucaoGlobal = gerarSolucaoInicial(grafo, origem);
	    imprimirSolucao(melhorSolucaoGlobal);
	    int melhorCustoGlobal = calcularCustoTotal(grafo, melhorSolucaoGlobal);
	    System.out.println("\nCusto Inicial: " + melhorCustoGlobal);
	    
	    // Primeira melhoria local com o 2-opt
	    System.out.println("\nRealizando primeira busca local...");
	    melhorSolucaoGlobal = buscaLocal(melhorSolucaoGlobal, grafo);
	    imprimirSolucao(melhorSolucaoGlobal);
	    int melhorCustoGlobal2 = calcularCustoTotal(grafo, melhorSolucaoGlobal);
	    System.out.println("\nCusto pós melhora local : " + melhorCustoGlobal2);

	    // Loop principal do ILS
	    for (int i = 0; i < maxIteracoes; i++) {
	        
	        // Perturbação
	        int[] solucaoPerturbada = doubleBridge(melhorSolucaoGlobal);
	        
	        // Busca Local
	        int[] solucaoRefinada = buscaLocal(solucaoPerturbada, grafo);
	        int custoRefinado = calcularCustoTotal(grafo, solucaoRefinada);

	        // Critério de Aceite
	        if (custoRefinado < melhorCustoGlobal) {
	            melhorCustoGlobal = custoRefinado;
	            melhorSolucaoGlobal = solucaoRefinada;
	            System.out.println("\nIteração " + i + ": Nova melhor solução encontrada \nCusto: " + melhorCustoGlobal);
	        }
	    }

	    long endTime = System.nanoTime();
	    long durationNs = endTime - startTime;
	    long durationMs = durationNs / 1_000_000;
	    double durationSec = durationNs / 1_000_000_000.0;
	    System.out.println("\nTempo de execução do ILS: " + durationMs + " ms (" + String.format("%.3f", durationSec) + " s)");

	    System.out.println("\nResultado Final do ILS");
	    System.out.print("Melhor Rota: ");
	    imprimirSolucao(melhorSolucaoGlobal);
	    System.out.println("\nMenor Custo Encontrado: " + melhorCustoGlobal);
	}
	
	/**
	 * Leitura do arquivo contendo o grafo utilizado ao longo do ILS
	 * @param caminho Local do arquivo
	 * @return Matriz de adjacencia do grafo
	 */
	public static int[][] lerArquivo(String caminho) {
		try {
			File arquivo = new File(caminho);
			Scanner leitor = new Scanner(arquivo);
			
			// Leitura
			if (leitor.hasNext()) {
				// Linha 1 n vertices e arestas
				int nVertices = leitor.nextInt();
				int nArestas = leitor.nextInt();
				
				System.out.println("\nGrafo"
						+ "\nNº de Vértices: " + nVertices
						+ "\nNº de Arestas: " + nArestas);
				
				int [][] grafo = new int[nVertices][nVertices];
				
				// Lendo o restante do arquivo
				for (int i = 0; i < nVertices; i++) {
					for (int j = 0; j < nVertices; j++) {
						if (leitor.hasNext()) {
							grafo[i][j] = leitor.nextInt();
						}
					}
				}
				leitor.close();
				return grafo;
			}
			leitor.close();
		} catch (FileNotFoundException e) {
			System.err.println("Arquivo não encontrado.");
		}
		return null;
	}
	
	/**
	 * Gera uma solução inicial gulosa
	 * @param n Número de vértices do grafo
	 * @param origem Vértice de origem para a solução
	 * @return Solução inicial gerada
	 */
	public static int[] gerarSolucaoInicial(int[][] matriz, int origem) {
		int n = matriz.length;
		int[] ciclo = new int[n + 1];
		boolean[] visitado = new boolean[n];

		ciclo[0] = origem;
		visitado[origem] = true;

		for (int i = 1; i < n; i++) {
			int ultimo = ciclo[i - 1];
			int melhorVizinho = -1;
			int menorCusto = Integer.MAX_VALUE;

			for (int j = 0; j < n; j++) {
				if (!visitado[j] && matriz[ultimo][j] < menorCusto) {
					menorCusto = matriz[ultimo][j];
					melhorVizinho = j;
				}
			}

			ciclo[i] = melhorVizinho;
			visitado[melhorVizinho] = true;
		}

		ciclo[n] = origem;

		System.out.println("\nSolução Inicial Gulosa Gerada.");
		return ciclo;
	}

	/**
	 * Calcula o custo total da solução
	 * @param matriz Matriz de adjacencia contendo todos as distâncias entre cada vértice
	 * @param ciclo Solução que terá seu custo calculado
	 * @return Custo total da solução
	 */
	public static int calcularCustoTotal(int[][] matriz, int[] ciclo) {
		int custo = 0;
		int n = ciclo.length - 1;
		
		for (int i = 0; i < n; i++) {
	        custo += matriz[ciclo[i]][ciclo[i+1]];
	    }
		
		return custo;
	}
	
	/**
	 * Procura por melhores soluções trocando dois segmentos do grafo
	 * @param cicloAtual O ciclo que se deseja buscar
	 * @param matriz Matriz de adjacencia para o calculo do custo total
	 * @return Um ciclo melhor ou igual ao anterior
	 */
	public static int[] buscaLocal(int[] ciclo, int[][] matriz) {
		int n = ciclo.length;
		boolean melhora = true;

		while (melhora) {
			melhora = false;

			for (int i = 1; i < n - 2; i++) {
				for (int j = i + 1; j < n - 1; j++) {

					int a = ciclo[i - 1];
					int b = ciclo[i];
					int c = ciclo[j];
					int d = ciclo[j + 1];

					int delta =
							- matriz[a][b]
							- matriz[c][d]
							+ matriz[a][c]
							+ matriz[b][d];

					if (delta < 0) {
						inverterSegmento(ciclo, i, j);
						melhora = true;

						break;
					}
				}
				if (melhora) break;
			}
		}

		return ciclo;
	}
	
	/**
	 * Troca a posição de dois vértices em uma solução
	 * @param ciclo Solução que passará pela inversão de segmentos
	 * @param i Posição do primeiro vértice
	 * @param j	Posição do segundo vértice
	 */
	private static void inverterSegmento(int[] ciclo, int i, int j) {
	    while (i < j) {
        int temp = ciclo[i];
        ciclo[i] = ciclo[j];
        ciclo[j] = temp;
        i++;
        j--;
    }
	}
	
	/**
	 * Imprime na tela uma solução 
	 * @param ciclo Vetor que contém a solução
	 */
	public static void imprimirSolucao(int[] ciclo) {
		for (int i = 0; i < ciclo.length; i++) {
			System.out.print(ciclo[i] + " ");
		}
	}
	
	/**
	 * Algoritmo responsável pelo perturbação das soluções
	 * @param ciclo Vetor que contém a solução
	 * @return Vetor contendo a solução perturbada
	 */
	public static int[] doubleBridge(int[] ciclo) {
			int n = ciclo.length - 1;

		if (n < 8) {
			return swap(ciclo);
		}

		int[] novo = new int[ciclo.length];

		// Faz 3 cortes aleatórios para criar 4 partes
		int a = 1 + random.nextInt(n - 3);
		int b = a + 1 + random.nextInt(n - a - 2);
		int c = b + 1 + random.nextInt(n - b - 1);

		int pos = 0;

		// A
		for (int i = 0; i <= a; i++) novo[pos++] = ciclo[i];

		// D
		for (int i = c + 1; i < n; i++) novo[pos++] = ciclo[i];

		// C
		for (int i = b + 1; i <= c; i++) novo[pos++] = ciclo[i];

		// B
		for (int i = a + 1; i <= b; i++) novo[pos++] = ciclo[i];

		novo[n] = novo[0];

		return novo;
	}
	
	/**
	 * Perturbação simples para vértices pequenos
	 * @param ciclo Vetor que contém a solução
	 * @return Vetor contendo a solução perturbada
	 */
	private static int[] swap(int[] ciclo) {
	    int[] novo = ciclo.clone();

		if (ciclo.length > 3) {
			int i = 1 + random.nextInt(ciclo.length - 2);
			int j;
			do {
				j = 1 + random.nextInt(ciclo.length - 2);
			} while (i == j);

			int temp = novo[i];
			novo[i] = novo[j];
			novo[j] = temp;
		}

		return novo;
	}
}
