
package controller;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import model.Celula;
import model.Cruzamento;
import model.Malha;
import model.Posicao;
import model.Veiculo;
import sync.SincronizacaoStrategy;

public class SimuladorController {
    private Malha malha;
    private Cruzamento gerenciadorCruzamento;
    private SincronizacaoStrategy estrategiaAtual;
    private final List<Veiculo> veiculos = new CopyOnWriteArrayList();
    private int proximoIdVeiculo = 1;
    private Thread threadInsercao;
    private volatile boolean simulacaoAtiva = false;
    private volatile boolean insercaoAtiva = false;
    private int maxVeiculos;
    private int intervaloInsercao;
    private static final Random random = new Random();

    public void carregarMalha(String caminhoArquivo, SincronizacaoStrategy estrategia) throws Exception {
        this.malha = Malha.carregarDeArquivo(caminhoArquivo);
        this.estrategiaAtual = estrategia;
        this.gerenciadorCruzamento = new Cruzamento(this.malha, estrategia);
        System.out.println("Malha carregada: " + this.malha);
        System.out.println("Pontos de entrada: " + this.malha.getPontosEntrada());
        System.out.println("Pontos de saída: " + this.malha.getPontosSaida());
        System.out.println("Estratégia de sincronização: " + estrategia.getNome());
    }

    public void iniciarSimulacao(int maxVeiculos, int intervaloInsercao) {
        if (this.malha == null) {
            throw new IllegalStateException("Malha não carregada");
        } else if (!this.simulacaoAtiva) {
            this.maxVeiculos = maxVeiculos;
            this.intervaloInsercao = intervaloInsercao;
            this.simulacaoAtiva = true;
            this.insercaoAtiva = true;
            this.threadInsercao = new Thread(this::threadInsercaoVeiculos, "Thread-Insercao");
            this.threadInsercao.start();
            Thread threadLimpeza = new Thread(this::threadLimpezaVeiculos, "Thread-Limpeza");
            threadLimpeza.setDaemon(true);
            threadLimpeza.start();
        }
    }

    private void threadInsercaoVeiculos() {
        while(true) {
            if (this.insercaoAtiva && !Thread.interrupted()) {
                try {
                    if (this.getNumVeiculosAtivos() < this.maxVeiculos) {
                        this.inserirVeiculo();
                    }

                    Thread.sleep((long)this.intervaloInsercao);
                    continue;
                } catch (InterruptedException var2) {
                }
            }

            return;
        }
    }

    private void threadLimpezaVeiculos() {
        while(true) {
            if (this.simulacaoAtiva && !Thread.interrupted()) {
                try {
                    this.veiculos.removeIf(Veiculo::isEncerrado);
                    Thread.sleep(1000L);
                    continue;
                } catch (InterruptedException var2) {
                }
            }

            return;
        }
    }

    private void inserirVeiculo() {
        List<Posicao> pontosEntrada = this.malha.getPontosEntrada();
        if (pontosEntrada.isEmpty()) {
            System.err.println("Nenhum ponto de entrada disponível");
            return;
        }

        Posicao entrada = pontosEntrada.get(random.nextInt(pontosEntrada.size()));
        Celula celulaEntrada = this.malha.getCelula(entrada);

        if (!celulaEntrada.estaOcupada()) {
            int direcaoInicial = celulaEntrada.getTipo().getDirecoesPermitidas()[0];
            int velocidade = 400 + random.nextInt(200);
            Veiculo veiculo = new Veiculo(this.proximoIdVeiculo++, entrada, direcaoInicial,
                    this.malha, this.gerenciadorCruzamento, velocidade);
            this.veiculos.add(veiculo);
            veiculo.start();
            System.out.println("Veículo " + veiculo.getVeiculoId() + " inserido em " + entrada +
                    " com direção " + direcaoInicial + " e velocidade " + velocidade + "ms");
        }
    }

    public void encerrarInsercao() {
        this.insercaoAtiva = false;
        if (this.threadInsercao != null) {
            this.threadInsercao.interrupt();
        }
    }

    public void encerrarSimulacao() {
        this.simulacaoAtiva = false;
        this.insercaoAtiva = false;
        if (this.threadInsercao != null) {
            this.threadInsercao.interrupt();
        }

        for(Veiculo veiculo : this.veiculos) {
            veiculo.parar();
            veiculo.interrupt();
        }

        this.veiculos.clear();
        this.proximoIdVeiculo = 1;
    }

    public int getNumVeiculosAtivos() {
        return (int)this.veiculos.stream().filter(Veiculo::isAtivo).count();
    }

    public List<Veiculo> getVeiculos() {
        return new ArrayList(this.veiculos);
    }

    public Malha getMalha() {
        return this.malha;
    }

    public boolean isSimulacaoAtiva() {
        return this.simulacaoAtiva;
    }

    public boolean isInsercaoAtiva() {
        return this.insercaoAtiva;
    }

    public SincronizacaoStrategy getEstrategiaAtual() {
        return this.estrategiaAtual;
    }

    public void trocarEstrategia(SincronizacaoStrategy novaEstrategia) {
        if (this.simulacaoAtiva) {
            throw new IllegalStateException("Não é possível trocar a estratégia com a simulação ativa");
        }
        this.estrategiaAtual = novaEstrategia;
        this.gerenciadorCruzamento = new Cruzamento(this.malha, novaEstrategia);
    }
}
