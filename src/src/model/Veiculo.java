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
                int var9 = this.id;
                System.out.println("Veículo " + var9 + " iniciado em " + String.valueOf(this.posicaoAtual));

                while(this.ativo && !Thread.interrupted()) {
                    Celula celulaAtual = this.malha.getCelula(this.posicaoAtual);
                    if (celulaAtual != null && celulaAtual.isSaida(this.malha.getNumLinhas(), this.malha.getNumColunas())) {
                        var9 = this.id;
                        System.out.println("Veículo " + var9 + " chegou à saída em " + String.valueOf(this.posicaoAtual));
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

            int var10001 = this.id;
            System.err.println("Veículo " + var10001 + " não conseguiu ocupar posição inicial: " + String.valueOf(this.posicaoAtual));
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
        } else {
            TipoSegmento tipoAtual = celulaAtual.getTipo();
            Posicao cruzamentoAtual = this.gerenciadorCruzamento.getPosicaoCruzamento(this.posicaoAtual);
            boolean estouEmCruzamento = cruzamentoAtual != null;
            int proximaDirecao = this.determinarProximaDirecao(tipoAtual, estouEmCruzamento);
            if (proximaDirecao == -1) {
                return false;
            } else {
                Posicao proximaPosicao = this.posicaoAtual.proximaPosicao(proximaDirecao);
                if (proximaPosicao != null && this.malha.posicaoValida(proximaPosicao)) {
                    Celula proximaCelula = this.malha.getCelula(proximaPosicao);
                    if (proximaCelula != null && proximaCelula.getTipo() != TipoSegmento.NADA) {
                        Posicao proximoCruzamento = this.gerenciadorCruzamento.getPosicaoCruzamento(proximaPosicao);
                        boolean proximoEhCruzamento = proximoCruzamento != null;
                        if (!estouEmCruzamento && proximoEhCruzamento) {
                            return this.entrarCruzamento(proximoCruzamento, proximaPosicao, proximaDirecao);
                        } else if (estouEmCruzamento && !proximoEhCruzamento) {
                            return this.sairCruzamento(proximaPosicao, proximaDirecao);
                        } else {
                            return estouEmCruzamento && proximoEhCruzamento ? this.atravessarCruzamento(proximaPosicao, proximaDirecao) : this.moverParaPosicao(proximaPosicao, proximaDirecao);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
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
        } else {
            int direcaoSaidaEscolhida = (Integer)saidasPossiveis.get(random.nextInt(saidasPossiveis.size()));
            List<Posicao> caminho = this.calcularCaminhoCruzamento(proximaPosicao, proximaDirecao, direcaoSaidaEscolhida, posCruzamento);
            if (caminho.isEmpty()) {
                return false;
            } else {
                for(Posicao pos : caminho) {
                    Celula cel = this.malha.getCelula(pos);
                    if (cel != null && cel.estaOcupada()) {
                        return false;
                    }
                }

                Posicao ultimaCelulaCruzamento = (Posicao)caminho.get(caminho.size() - 1);
                Posicao celulaSaida = ultimaCelulaCruzamento.proximaPosicao(direcaoSaidaEscolhida);
                if (celulaSaida != null && this.malha.posicaoValida(celulaSaida)) {
                    Celula celSaida = this.malha.getCelula(celulaSaida);
                    if (celSaida != null && celSaida.getTipo() != TipoSegmento.NADA && celSaida.estaOcupada()) {
                        return false;
                    }
                }

                if (!this.gerenciadorCruzamento.tentarEntrar(posCruzamento, this.id)) {
                    return false;
                } else {
                    for(Posicao pos : caminho) {
                        Celula cel = this.malha.getCelula(pos);
                        if (cel != null && cel.estaOcupada()) {
                            this.gerenciadorCruzamento.sair(posCruzamento, this.id);
                            return false;
                        }
                    }

                    if (this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
                        this.cruzamentoOcupado = posCruzamento;
                        this.direcaoEscolhida = direcaoSaidaEscolhida;
                        int var10001 = this.id;
                        System.out.println("Veículo " + var10001 + " ENTROU no cruzamento " + String.valueOf(posCruzamento) + " (entrada: dir " + proximaDirecao + ", saída: dir " + direcaoSaidaEscolhida + ", caminho: " + caminho.size() + " células)");
                        return true;
                    } else {
                        this.gerenciadorCruzamento.sair(posCruzamento, this.id);
                        return false;
                    }
                }
            }
        }
    }

    private boolean atravessarCruzamento(Posicao proximaPosicao, int proximaDirecao) {
        Celula proximaCelula = this.malha.getCelula(proximaPosicao);
        if (proximaCelula.estaOcupada()) {
            return false;
        } else if (this.moverParaPosicao(proximaPosicao, proximaDirecao)) {
            int var10001 = this.id;
            System.out.println("Veículo " + var10001 + " ATRAVESSANDO cruzamento em " + String.valueOf(proximaPosicao) + " (direção: " + proximaDirecao + ")");
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
            int var10001 = this.id;
            System.out.println("Veículo " + var10001 + " SAIU do cruzamento " + String.valueOf(this.cruzamentoOcupado));
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
