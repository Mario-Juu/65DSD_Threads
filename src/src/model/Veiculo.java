
package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Veiculo extends Thread {
    private final int id;
    private final Malha malha;
    private final Cruzamento gerenciadorCruzamento;
    private final int velocidade;
    private final Color cor;
    private Posicao posicaoAtual;
    private int direcaoAtual;
    private volatile boolean ativo;
    private volatile boolean encerrado;
    private Posicao cruzamentoOcupado = null;
    private Integer direcaoEscolhida = null;
    private List<Posicao> caminhoReservado = null;
    private static final Random random = new Random();

    public Veiculo(int id, Posicao posicaoInicial, int direcaoInicial, Malha malha, Cruzamento gerenciadorCruzamento, int velocidade) {
        this.id = id;
        this.posicaoAtual = posicaoInicial;
        this.direcaoAtual = direcaoInicial;
        this.malha = malha;
        this.gerenciadorCruzamento = gerenciadorCruzamento;
        this.velocidade = velocidade;
        this.ativo = true;
        this.encerrado = false;
        this.cor = this.gerarCorAleatoria();
        this.setName("Veiculo-" + id);
    }

    private Color gerarCorAleatoria() {
        Color[] coresPredefinidas = new Color[]{new Color(255, 69, 0), new Color(0, 191, 255), new Color(50, 205, 50), new Color(255, 215, 0), new Color(138, 43, 226), new Color(255, 20, 147), new Color(0, 255, 127), new Color(255, 140, 0), new Color(147, 112, 219), new Color(0, 206, 209)};
        return coresPredefinidas[random.nextInt(coresPredefinidas.length)];
    }

    public void run() {
        try {
            Celula celulaInicial = this.malha.getCelula(this.posicaoAtual);
            if (celulaInicial != null && celulaInicial.tentarOcupar(this.id)) {
                System.out.println("Veículo " + this.id + " iniciado em " + this.posicaoAtual);

                while(this.ativo && !Thread.interrupted()) {
                    Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
                    if (celulaAtual != null && celulaAtual.isSaida(this.malha.getNumLinhas(), this.malha.getNumColunas())) {
                        System.out.println("Veículo " + this.id + " chegou à saída em " + this.posicaoAtual);
                        return;
                    }

                    if (!this.mover()) {
                        sleep((long)(this.velocidade / 2));
                    } else {
                        sleep((long)this.velocidade);
                    }
                }

                return;
            }

            System.err.println("Veículo " + this.id + " não conseguiu ocupar posição inicial: " + this.posicaoAtual);
            this.ativo = false;
            this.encerrado = true;
        } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
            return;
        } finally {
            this.liberarRecursos();
            this.ativo = false;
            this.encerrado = true;
            System.out.println("Veículo " + this.id + " finalizado");
        }

    }

    private boolean mover() {
        Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
        TipoSegmento tipoAtual = celulaAtual.getTipo();
        Posicao cruzamentoAtual = this.gerenciadorCruzamento.getPosicaoCruzamento(this.posicaoAtual);
        boolean estouEmCruzamento = cruzamentoAtual != null;

        int proximaDirecao = this.determinarProximaDirecao(tipoAtual, estouEmCruzamento);
        if (proximaDirecao == -1) {
            return false;
        }

        Posicao proximaPosicao = this.posicaoAtual.proximaPosicao(proximaDirecao);
        Celula proximaCelula = this.malha.getCelula(proximaPosicao);

        Posicao proximoCruzamento = this.gerenciadorCruzamento.getPosicaoCruzamento(proximaPosicao);
        boolean proximoEhCruzamento = proximoCruzamento != null;

        return this.executarMovimento(estouEmCruzamento, proximoEhCruzamento,
                proximoCruzamento, proximaPosicao, proximaDirecao);
    }

    private boolean executarMovimento(boolean estouEmCruzamento, boolean proximoEhCruzamento,
                                       Posicao proximoCruzamento, Posicao proximaPosicao, int proximaDirecao) {
        // Veículo saindo de estrada e entrando em cruzamento
        if (!estouEmCruzamento && proximoEhCruzamento) {
            return this.entrarCruzamento(proximoCruzamento, proximaPosicao, proximaDirecao);
        }

        // Veículo saindo de cruzamento para estrada
        if (estouEmCruzamento && !proximoEhCruzamento) {
            return this.sairCruzamento(proximaPosicao, proximaDirecao);
        }

        // Veículo atravessando dentro do cruzamento
        if (estouEmCruzamento && proximoEhCruzamento) {
            return this.atravessarCruzamento(proximaPosicao, proximaDirecao);
        }

        // Movimento normal em estrada
        return this.moverParaPosicao(proximaPosicao, proximaDirecao);
    }

    private int determinarProximaDirecao(TipoSegmento tipoAtual, boolean estouEmCruzamento) {
        if (estouEmCruzamento) {
            return this.direcaoEscolhida != null ? this.direcaoEscolhida : this.direcaoAtual;
        } else {
            int[] direccoes = tipoAtual.getDirecoesPermitidas();
            return direccoes.length > 0 ? direccoes[0] : -1;
        }
    }

    private List<Posicao> calcularCaminhoCruzamento(Posicao posEntrada, int direcaoEntrada, int direcaoSaida, Posicao idCruzamento) {
        List<Posicao> caminho = new ArrayList();
        List<Posicao> celulasCruzamento = this.malha.getCelulasCruzamento(idCruzamento);
        Posicao atual = posEntrada;
        int direcaoAtual = direcaoSaida;
        caminho.add(posEntrada);

        for(int iteracoes = 0; iteracoes < 20; iteracoes++) {
            Posicao proxima = atual.proximaPosicao(direcaoAtual);

            if (!celulasCruzamento.contains(proxima)) {
                break;
            }

            Celula celulaAtual = this.malha.getCelula(atual);
            if (!celulaAtual.getTipo().permiteDirecao(direcaoAtual)) {
                break;
            }

            caminho.add(proxima);
            atual = proxima;

            Celula celulaProxima = this.malha.getCelula(proxima);
            int[] direcoesProxima = celulaProxima.getTipo().getDirecoesPermitidas();

            if (direcoesProxima.length > 0) {
                direcaoAtual = direcoesProxima.length > 1
                    ? direcoesProxima[random.nextInt(direcoesProxima.length)]
                    : direcoesProxima[0];
            }
        }

        return caminho;
    }

    private boolean entrarCruzamento(Posicao posCruzamento, Posicao proximaPosicao, int proximaDirecao) {
        List<CaminhoValido> caminhosValidos = this.calcularCaminhosValidos(posCruzamento, proximaPosicao, proximaDirecao);

        if (caminhosValidos.isEmpty()) {
            return false;
        }

        CaminhoValido caminhoEscolhido = caminhosValidos.get(random.nextInt(caminhosValidos.size()));

        if (!this.gerenciadorCruzamento.tentarEntrar(posCruzamento, this.id, caminhoEscolhido.caminho, caminhoEscolhido.direcaoSaida)) {
            return false;
        }

        if (!this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            this.gerenciadorCruzamento.sair(posCruzamento, this.id);
            return false;
        }

        this.cruzamentoOcupado = posCruzamento;
        this.direcaoEscolhida = caminhoEscolhido.direcaoSaida;
        this.caminhoReservado = caminhoEscolhido.caminho;

        System.out.println("Veículo " + this.id + " ENTROU no cruzamento " + posCruzamento +
                " (entrada: dir " + proximaDirecao + ", saída: dir " + caminhoEscolhido.direcaoSaida +
                ", caminho: " + caminhoEscolhido.caminho + ")");

        return true;
    }

    private List<CaminhoValido> calcularCaminhosValidos(Posicao posCruzamento, Posicao proximaPosicao, int proximaDirecao) {
        Celula celulaEntrada = this.malha.getCelula(proximaPosicao);
        List<Integer> saidasPossiveis = celulaEntrada.getTipo().getDirecoesSaida(proximaDirecao);
        List<CaminhoValido> caminhosValidos = new ArrayList<>();

        for (int direcaoSaida : saidasPossiveis) {
            List<Posicao> caminho = this.calcularCaminhoCruzamento(proximaPosicao, proximaDirecao, direcaoSaida, posCruzamento);

            if (!caminho.isEmpty() && this.caminhoSaiDoCruzamento(caminho, posCruzamento)) {
                caminhosValidos.add(new CaminhoValido(direcaoSaida, caminho));
            }
        }

        return caminhosValidos;
    }

    private boolean caminhoSaiDoCruzamento(List<Posicao> caminho, Posicao idCruzamento) {
        if (caminho.isEmpty()) {
            return false;
        }

        Posicao ultimaPosicao = caminho.get(caminho.size() - 1);
        Celula ultimaCelula = this.malha.getCelula(ultimaPosicao);
        int[] direcoesUltima = ultimaCelula.getTipo().getDirecoesPermitidas();

        if (direcoesUltima.length == 0) {
            return false;
        }

        List<Posicao> celulasCruzamento = this.malha.getCelulasCruzamento(idCruzamento);
        for (int direcaoSaida : direcoesUltima) {
            Posicao proximaPosicao = ultimaPosicao.proximaPosicao(direcaoSaida);
            if (!celulasCruzamento.contains(proximaPosicao)) {
                return true;
            }
        }

        return false;
    }

    // Classe auxiliar para armazenar caminhos válidos
    private static class CaminhoValido {
        final int direcaoSaida;
        final List<Posicao> caminho;

        CaminhoValido(int direcaoSaida, List<Posicao> caminho) {
            this.direcaoSaida = direcaoSaida;
            this.caminho = caminho;
        }
    }

    private boolean atravessarCruzamento(Posicao proximaPosicao, int proximaDirecao) {
        if (!this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            return false;
        }

        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        int[] direcoesProxima = proximaCelula.getTipo().getDirecoesPermitidas();

        if (direcoesProxima.length > 0) {
            this.direcaoEscolhida = direcoesProxima.length > 1
                ? direcoesProxima[random.nextInt(direcoesProxima.length)]
                : direcoesProxima[0];
        }

        System.out.println("Veículo " + this.id + " ATRAVESSANDO cruzamento em " + proximaPosicao +
                " (direção: " + proximaDirecao + ", próxima: " + this.direcaoEscolhida + ")");
        return true;
    }

    private boolean sairCruzamento(Posicao proximaPosicao, int proximaDirecao) {
        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        if (proximaCelula.estaOcupada()) {
            return false;
        }

        if (!this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            return false;
        }

        System.out.println("Veículo " + this.id + " SAIU do cruzamento " + this.cruzamentoOcupado);
        this.liberarCaminhoCruzamento();
        return true;
    }

    private void liberarCaminhoCruzamento() {
        if (this.caminhoReservado != null) {
            for (Posicao pos : this.caminhoReservado) {
                if (!pos.equals(this.posicaoAtual)) {
                    Celula celula = this.malha.getCelula(pos);
                    if (celula.getVeiculoId() != null && celula.getVeiculoId().equals(this.id)) {
                        celula.liberar();
                    }
                }
            }
            this.caminhoReservado = null;
        }

        if (this.cruzamentoOcupado != null) {
            this.gerenciadorCruzamento.sair(this.cruzamentoOcupado, this.id);
            this.cruzamentoOcupado = null;
        }

        this.direcaoEscolhida = null;
    }

    private void liberarRecursos() {
        this.liberarCaminhoCruzamento();

        Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
        if (celulaAtual != null) {
            celulaAtual.liberar();
        }
    }

    private boolean moverParaPosicao(Posicao novaPosicao, int novaDirecao) {
        Celula proximaCelula = this.malha.getCelula(novaPosicao);
        boolean jaOcupadaPorMim = Integer.valueOf(this.id).equals(proximaCelula.getVeiculoId());

        if (!jaOcupadaPorMim && !proximaCelula.tentarOcupar(this.id)) {
            return false;
        }

        Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
        if (celulaAtual != null) {
            celulaAtual.liberar();
        }

        this.posicaoAtual = novaPosicao;
        this.direcaoAtual = novaDirecao;
        return true;
    }

    public void parar() {
        this.ativo = false;
    }

    public int getVeiculoId() {
        return this.id;
    }

    public Posicao getPosicaoAtual() {
        return this.posicaoAtual;
    }

    public int getDirecaoAtual() {
        return this.direcaoAtual;
    }

    public Color getCor() {
        return this.cor;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public boolean isEncerrado() {
        return this.encerrado;
    }
}
