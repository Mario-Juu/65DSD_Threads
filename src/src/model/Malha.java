//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Malha {
    private final int numLinhas;
    private final int numColunas;
    private final Celula[][] grid;
    private final List<Posicao> pontosEntrada;
    private final List<Posicao> pontosSaida;
    private final Map<Posicao, List<Posicao>> cruzamentos;

    public Malha(int numLinhas, int numColunas) {
        this.numLinhas = numLinhas;
        this.numColunas = numColunas;
        this.grid = new Celula[numLinhas][numColunas];
        this.pontosEntrada = new ArrayList();
        this.pontosSaida = new ArrayList();
        this.cruzamentos = new HashMap();
    }

    public static Malha carregarDeArquivo(String caminhoArquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            int numLinhas = Integer.parseInt(br.readLine().trim());
            int numColunas = Integer.parseInt(br.readLine().trim());
            Malha malha = new Malha(numLinhas, numColunas);

            for(int i = 0; i < numLinhas; ++i) {
                String linha = br.readLine();
                if (linha == null) {
                    throw new IOException("Arquivo incompleto: esperado " + numLinhas + " linhas de dados");
                }

                String[] valores = linha.trim().split("\\s+");
                if (valores.length != numColunas) {
                    throw new IOException("Linha " + i + " tem " + valores.length + " colunas, esperado " + numColunas);
                }

                for(int j = 0; j < numColunas; ++j) {
                    int codigo = Integer.parseInt(valores[j]);
                    TipoSegmento tipo = TipoSegmento.fromCodigo(codigo);
                    Posicao pos = new Posicao(i, j);
                    Celula celula = new Celula(tipo, pos);
                    malha.grid[i][j] = celula;

                    if (celula.isEntrada(numLinhas, numColunas)) {
                        malha.pontosEntrada.add(pos);
                    }
                    if (celula.isSaida(numLinhas, numColunas)) {
                        malha.pontosSaida.add(pos);
                    }
                }
            }

            malha.identificarCruzamentos();
            return malha;
        }
    }

    private void identificarCruzamentos() {
        boolean[][] visitado = new boolean[this.numLinhas][this.numColunas];

        for(int i = 0; i < this.numLinhas; ++i) {
            for(int j = 0; j < this.numColunas; ++j) {
                Celula celula = this.grid[i][j];
                if (celula.getTipo().isCruzamento() && !visitado[i][j]) {
                    List<Posicao> celulasCruzamento = new ArrayList<>();
                    this.identificarCruzamentoRecursivo(i, j, visitado, celulasCruzamento);
                    Posicao idCruzamento = celulasCruzamento.get(0);
                    this.cruzamentos.put(idCruzamento, celulasCruzamento);
                    System.out.println("Cruzamento identificado em " + idCruzamento + " com " +
                            celulasCruzamento.size() + " células: " + celulasCruzamento);
                }
            }
        }

    }

    private void identificarCruzamentoRecursivo(int i, int j, boolean[][] visitado, List<Posicao> celulasCruzamento) {
        if (this.posicaoValida(i, j) && !visitado[i][j]) {
            Celula celula = this.grid[i][j];
            if (celula.getTipo().isCruzamento()) {
                visitado[i][j] = true;
                celulasCruzamento.add(celula.getPosicao());
                this.identificarCruzamentoRecursivo(i - 1, j, visitado, celulasCruzamento);
                this.identificarCruzamentoRecursivo(i + 1, j, visitado, celulasCruzamento);
                this.identificarCruzamentoRecursivo(i, j - 1, visitado, celulasCruzamento);
                this.identificarCruzamentoRecursivo(i, j + 1, visitado, celulasCruzamento);
            }
        }
    }

    public boolean posicaoValida(int linha, int coluna) {
        return linha >= 0 && linha < this.numLinhas && coluna >= 0 && coluna < this.numColunas;
    }

    public boolean posicaoValida(Posicao pos) {
        return this.posicaoValida(pos.getLinha(), pos.getColuna());
    }

    public Celula getCelula(Posicao pos) {
        return !this.posicaoValida(pos) ? null : this.grid[pos.getLinha()][pos.getColuna()];
    }

    public Celula getCelula(int linha, int coluna) {
        return !this.posicaoValida(linha, coluna) ? null : this.grid[linha][coluna];
    }

    public boolean isCruzamento(Posicao pos) {
        return this.cruzamentos.containsKey(pos);
    }

    public List<Posicao> getCelulasCruzamento(Posicao posCruzamento) {
        return this.cruzamentos.getOrDefault(posCruzamento, new ArrayList<>());
    }

    public Posicao getPosicaoCruzamento(Posicao pos) {
        for(Map.Entry<Posicao, List<Posicao>> entry : this.cruzamentos.entrySet()) {
            if (entry.getValue().contains(pos)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getNumLinhas() {
        return this.numLinhas;
    }

    public int getNumColunas() {
        return this.numColunas;
    }

    public List<Posicao> getPontosEntrada() {
        return new ArrayList(this.pontosEntrada);
    }

    public List<Posicao> getPontosSaida() {
        return new ArrayList(this.pontosSaida);
    }

    public Map<Posicao, List<Posicao>> getCruzamentos() {
        return new HashMap(this.cruzamentos);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Malha ").append(this.numLinhas).append("x").append(this.numColunas).append("\n");
        sb.append("Pontos de entrada: ").append(this.pontosEntrada.size()).append("\n");
        sb.append("Pontos de saída: ").append(this.pontosSaida.size()).append("\n");
        sb.append("Cruzamentos: ").append(this.cruzamentos.size()).append("\n");
        return sb.toString();
    }
}
