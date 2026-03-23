//
package ils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
	    // Carregamento do grafo escolhido
		System.out.println("Escolha o grafo que será carregado:"
				+ "\n(1) Grafo G | Principal | 4 Vértices  | 6 Arestas"
				+ "\n(2) Grafo H | Teste     | 10 Vértices | 45 Arestas"
				+ "\n(3) Grafo I | Teste     | 50 Vértices | 1225 Arestas");
		
		int escolhaGrafo = scanner.nextInt();
		String grafo = "src/grafoG.txt";
		if (escolhaGrafo == 2) {
			grafo = "src/grafoH.txt";
		} else if (escolhaGrafo == 3) {
			grafo = "src/grafoI.txt";
		}
		
		int[][] grafoG = lerArquivo(grafo);
		
		scanner.close();
	    if (grafoG == null) return;
	    
	    // Parâmetros
	    int maxIteracoes = 50; 
	    int n = grafoG.length;
	    int origem = 0;

	    // Gerando solução inicial
	    int[] melhorSolucaoGlobal = gerarSolucaoAleatoria(n, origem);
	    imprimirSolucao(melhorSolucaoGlobal);
	    int melhorCustoGlobal = calcularCustoTotal(grafoG, melhorSolucaoGlobal);
	    System.out.println("\nCusto Inicial: " + melhorCustoGlobal);
	    
	    // Primeira melhoria local com o 2-opt
	    System.out.println("\nRealizando primeira busca local...");
	    melhorSolucaoGlobal = algoritmo2Opt(melhorSolucaoGlobal, grafoG);
	    imprimirSolucao(melhorSolucaoGlobal);
	    int melhorCustoGlobal2 = calcularCustoTotal(grafoG, melhorSolucaoGlobal);
	    System.out.println("\nCusto pós melhora local : " + melhorCustoGlobal2);

	    // Loop principal do ILS
	    for (int i = 0; i < maxIteracoes; i++) {
	        
	        // Perturbação
	        int[] solucaoPerturbada = doubleBridge(melhorSolucaoGlobal);
	        
	        // Busca Local
	        int[] solucaoRefinada = algoritmo2Opt(solucaoPerturbada, grafoG);
	        int custoRefinado = calcularCustoTotal(grafoG, solucaoRefinada);

	        // Critério de Aceite
	        if (custoRefinado < melhorCustoGlobal) {
	            melhorCustoGlobal = custoRefinado;
	            melhorSolucaoGlobal = solucaoRefinada.clone();
	            System.out.println("\nIteração " + i + ": Nova melhor solução encontrada \nCusto: " + melhorCustoGlobal);
	        }
	    }

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
						+ "\nNº de Arestas: " + nArestas
						+ "\nMatriz de Adjacência: "
						);
				
				int [][] grafo = new int[nVertices][nVertices];
				
				// Lendo o restante do arquivo
				for (int i = 0; i < nVertices; i++) {
					for (int j = 0; j < nVertices; j++) {
						if (leitor.hasNext()) {
							grafo[i][j] = leitor.nextInt();
							System.out.print(grafo[i][j] + " ");
						}
					}
					System.out.println();
				}
				leitor.close();
				return grafo;
			}
			leitor.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Arquivo não encontrado.");
		}
		return null;
	}
	
	/**
	 * Gera uma suloção aleatória inical para o ILS
	 * @param n Número de vértices
	 * @param origem Ponto de partida
	 * @return Um vetor contendo uma solução inicial
	 */
	public static int[] gerarSolucaoAleatoria(int n, int origem) {
	    int[] caminho = new int[n + 1];
	    List<Integer> verticesRestantes = new ArrayList<>();

	    for (int i = 0; i < n; i++) {
	        if (i != origem) {
	        	verticesRestantes.add(i);
	        }
	    }

	    Collections.shuffle(verticesRestantes);

	    caminho[0] = origem;
	    for (int i = 0; i < verticesRestantes.size(); i++) {
	        caminho[i + 1] = verticesRestantes.get(i);
	    }
	    caminho[n] = origem;

	    System.out.println("\nSolução Inicial Aleatória Gerada.");
	    return caminho;
	}

	/**
	 * Calcula o custo total da solução
	 * @param matriz Matriz de adjacencia contendo todos as distâncias entre cada vértice
	 * @param caminho Solução que terá seu custo calculado
	 * @return Custo total da solução
	 */
	public static int calcularCustoTotal(int[][] matriz, int[] caminho) {
		int custo = 0;
		int n = caminho.length;
		
		for (int i = 0; i < n - 1; i++) {
	        custo += matriz[caminho[i]][caminho[i+1]];
	    }
		
		custo += matriz[caminho[n-1]][caminho[0]];
		
		return custo;
	}
	
	/**
	 * Procura por melhores soluções trocando dois segmentos do grafo
	 * @param caminhoAtual O caminho que se deseja buscar
	 * @param matriz Matriz de adjacencia para o calculo do custo total
	 * @return Um caminho melhor ou igual ao anterior
	 */
	public static int[] algoritmo2Opt(int[] caminhoAtual, int[][] matriz) {
		int n = caminhoAtual.length;
		int[] melhorCaminho = caminhoAtual.clone();
		boolean melhora = true;
		
		while(melhora) {
			melhora = false;
			for (int i = 1; i < n-1; i++) {
				for (int j = i+1; j < n; j++) {
					int[] novoCaminho = inverterSegmento(melhorCaminho, i, j);
					
					int custoAntigo = calcularCustoTotal(matriz, melhorCaminho);
					int custoNovo = calcularCustoTotal(matriz, novoCaminho);
					
					if (custoNovo < custoAntigo) {
						melhorCaminho = novoCaminho;
						melhora = true;
					}
				}
			}
		}
		return melhorCaminho;
	}
	
	/**
	 * Troca a posição de dois vértices em uma solução
	 * @param caminho Solução que passará pela inversão de segmentos
	 * @param i Posição do primeiro vértice
	 * @param j	Posição do segundo vértice
	 * @return	A solução com os dois vértices trocados
	 */
	private static int[] inverterSegmento(int[] caminho, int i, int j) {
	    int[] novoCaminho = caminho.clone();
	    while (i < j) {
	        int temp = novoCaminho[i];
	        novoCaminho[i] = novoCaminho[j];
	        novoCaminho[j] = temp;
	        i++;
	        j--;
	    }
	    return novoCaminho;
	}
	
	/**
	 * Imprime na tela uma solução 
	 * @param caminho Vetor que contém a solução
	 */
	public static void imprimirSolucao(int[] caminho) {
		for (int i = 0; i < caminho.length; i++) {
			System.out.print(caminho[i] + " ");
		}
	}
	
	/**
	 * Algoritmo responsável pelo perturbação das soluções
	 * @param caminho Vetor que contém a solução
	 * @return Vetor contendo a solução perturbada
	 */
	public static int[] doubleBridge(int[] caminho) {
	    int n = caminho.length - 1;
	    
	    if (n < 8) {
	        return swap(caminho);
	    }

	    int[] novoCaminho = new int[caminho.length];
	    Random random = new Random();
	    
	    // Divide o caminho
	    int tamParte = n / 4;
	    int corte1 = 1 + random.nextInt(tamParte);
	    int corte2 = corte1 + 1 + random.nextInt(tamParte);
	    int corte3 = corte2 + 1 + random.nextInt(tamParte);
	    
	    int posicao = 0;
	    
	    for (int i = 0; i <= corte1; i++) {
	        novoCaminho[posicao++] = caminho[i];
	    }
	    
	    for (int i = corte3 + 1; i < n; i++) {
	        novoCaminho[posicao++] = caminho[i];
	    }
	    
	    for (int i = corte2 + 1; i <= corte3; i++) {
	        novoCaminho[posicao++] = caminho[i];
	    }
	    
	    for (int i = corte1 + 1; i <= corte2; i++) {
	        novoCaminho[posicao++] = caminho[i];
	    }
	    
	    novoCaminho[n] = caminho[0];
	    
	    return novoCaminho;
	}
	
	/**
	 * Perturbação simples para vértices pequenos
	 * @param caminho Vetor que contém a solução
	 * @return Vetor contendo a solução perturbada
	 */
	private static int[] swap(int[] caminho) {
	    int[] novoCaminho = caminho.clone();
	    Random random = new Random();
	    if (caminho.length > 3) {
	        int i = 1 + random.nextInt(caminho.length - 2);
	        int j = 1 + random.nextInt(caminho.length - 2);
	        int temp = novoCaminho[i];
	        novoCaminho[i] = novoCaminho[j];
	        novoCaminho[j] = temp;
	    }
	    return novoCaminho;
	}
}
