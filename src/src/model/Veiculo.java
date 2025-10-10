//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
            if (this.cruzamentoOcupado != null) {
                this.gerenciadorCruzamento.sair(this.cruzamentoOcupado, this.id);
                this.cruzamentoOcupado = null;
            }

            Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
            if (celulaAtual != null) {
                celulaAtual.liberar();
            }

            this.ativo = false;
            this.encerrado = true;
            System.out.println("Veículo " + this.id + " finalizado");
        }

    }

    private boolean mover() {
        Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
        if (celulaAtual == null) {
            return false;
        }

        TipoSegmento tipoAtual = celulaAtual.getTipo();
        Posicao cruzamentoAtual = this.gerenciadorCruzamento.getPosicaoCruzamento(this.posicaoAtual);
        boolean estouEmCruzamento = cruzamentoAtual != null;

        int proximaDirecao = this.determinarProximaDirecao(tipoAtual, estouEmCruzamento);
        if (proximaDirecao == -1) {
            return false;
        }

        Posicao proximaPosicao = this.posicaoAtual.proximaPosicao(proximaDirecao);
        if (!this.isPosicaoValida(proximaPosicao)) {
            return false;
        }

        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        if (proximaCelula == null || proximaCelula.getTipo() == TipoSegmento.NADA) {
            return false;
        }

        Posicao proximoCruzamento = this.gerenciadorCruzamento.getPosicaoCruzamento(proximaPosicao);
        boolean proximoEhCruzamento = proximoCruzamento != null;

        return this.executarMovimento(estouEmCruzamento, proximoEhCruzamento,
                proximoCruzamento, proximaPosicao, proximaDirecao);
    }

    private boolean isPosicaoValida(Posicao posicao) {
        return posicao != null && this.malha.posicaoValida(posicao);
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

    private int getDirecaoOposta(int direcao) {
        switch (direcao) {
            case 1 -> {
                return 3;
            }
            case 2 -> {
                return 4;
            }
            case 3 -> {
                return 1;
            }
            case 4 -> {
                return 2;
            }
            default -> {
                return direcao;
            }
        }
    }

    private List<Posicao> calcularCaminhoCruzamento(Posicao posEntrada, int direcaoEntrada, int direcaoSaida, Posicao idCruzamento) {
        List<Posicao> caminho = new ArrayList();
        List<Posicao> celulasCruzamento = this.malha.getCelulasCruzamento(idCruzamento);
        Posicao atual = posEntrada;
        caminho.add(posEntrada);
        int maxIteracoes = 20;

        Posicao proxima;
        for(int iteracoes = 0; iteracoes < maxIteracoes; atual = proxima) {
            ++iteracoes;
            proxima = atual.proximaPosicao(direcaoSaida);
            if (proxima == null || !this.malha.posicaoValida(proxima)) {
                break;
            }

            Celula celProxima = this.malha.getCelula(proxima);
            if (celProxima == null || celProxima.getTipo() == TipoSegmento.NADA || !celulasCruzamento.contains(proxima)) {
                break;
            }

            caminho.add(proxima);
        }

        return caminho;
    }

    private boolean entrarCruzamento(Posicao posCruzamento, Posicao proximaPosicao, int proximaDirecao) {
        Celula celulaEntrada = this.malha.getCelula(proximaPosicao);
        TipoSegmento tipoEntrada = celulaEntrada.getTipo();
        List<Integer> saidasPossiveis = tipoEntrada.getDirecoesSaida(proximaDirecao);

        if (saidasPossiveis.isEmpty()) {
            return false;
        }

        int direcaoSaidaEscolhida = saidasPossiveis.get(random.nextInt(saidasPossiveis.size()));
        List<Posicao> caminho = this.calcularCaminhoCruzamento(proximaPosicao, proximaDirecao,
                direcaoSaidaEscolhida, posCruzamento);

        if (caminho.isEmpty()) {
            return false;
        }

        // Verifica se o caminho está livre
        if (!this.verificarCaminhoLivre(caminho)) {
            return false;
        }

        // Verifica se a célula de saída está livre
        if (!this.verificarCelulaSaidaLivre(caminho, direcaoSaidaEscolhida)) {
            return false;
        }

        // Tenta adquirir permissão para entrar no cruzamento
        if (!this.gerenciadorCruzamento.tentarEntrar(posCruzamento, this.id)) {
            return false;
        }

        // Double-check: verifica novamente se o caminho ainda está livre
        if (!this.verificarCaminhoLivre(caminho)) {
            this.gerenciadorCruzamento.sair(posCruzamento, this.id);
            return false;
        }

        // Executa o movimento
        if (this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            this.cruzamentoOcupado = posCruzamento;
            this.direcaoEscolhida = direcaoSaidaEscolhida;
            System.out.println("Veículo " + this.id + " ENTROU no cruzamento " + posCruzamento +
                    " (entrada: dir " + proximaDirecao + ", saída: dir " + direcaoSaidaEscolhida +
                    ", caminho: " + caminho.size() + " células)");
            return true;
        } else {
            this.gerenciadorCruzamento.sair(posCruzamento, this.id);
            return false;
        }
    }

    private boolean verificarCaminhoLivre(List<Posicao> caminho) {
        for (Posicao pos : caminho) {
            Celula celula = this.malha.getCelula(pos);
            if (celula != null && celula.estaOcupada()) {
                return false;
            }
        }
        return true;
    }

    private boolean verificarCelulaSaidaLivre(List<Posicao> caminho, int direcaoSaida) {
        Posicao ultimaCelulaCruzamento = caminho.get(caminho.size() - 1);
        Posicao celulaSaida = ultimaCelulaCruzamento.proximaPosicao(direcaoSaida);

        if (celulaSaida != null && this.malha.posicaoValida(celulaSaida)) {
            Celula celSaida = this.malha.getCelula(celulaSaida);
            if (celSaida != null && celSaida.getTipo() != TipoSegmento.NADA && celSaida.estaOcupada()) {
                return false;
            }
        }
        return true;
    }

    private boolean atravessarCruzamento(Posicao proximaPosicao, int proximaDirecao) {
        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        if (proximaCelula.estaOcupada()) {
            return false;
        } else if (this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            System.out.println("Veículo " + this.id + " ATRAVESSANDO cruzamento em " + proximaPosicao +
                    " (direção: " + proximaDirecao + ")");
            return true;
        } else {
            return false;
        }
    }

    private boolean sairCruzamento(Posicao proximaPosicao, int proximaDirecao) {
        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        if (proximaCelula.estaOcupada()) {
            return false;
        } else if (this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            System.out.println("Veículo " + this.id + " SAIU do cruzamento " + this.cruzamentoOcupado);
            this.gerenciadorCruzamento.sair(this.cruzamentoOcupado, this.id);
            this.cruzamentoOcupado = null;
            this.direcaoEscolhida = null;
            return true;
        } else {
            return false;
        }
    }

    private boolean moverParaPosicao(Posicao novaPosicao, int novaDirecao) {
        Celula proximaCelula = this.malha.getCelula(novaPosicao);
        if (!proximaCelula.tentarOcupar(this.id)) {
            return false;
        } else {
            Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
            if (celulaAtual != null) {
                celulaAtual.liberar();
            }

            this.posicaoAtual = novaPosicao;
            this.direcaoAtual = novaDirecao;
            return true;
        }
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
