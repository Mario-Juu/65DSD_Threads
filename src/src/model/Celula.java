//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package model;

public class Celula {
    private final TipoSegmento tipo;
    private final Posicao posicao;
    private Integer veiculoId;

    public Celula(TipoSegmento tipo, Posicao posicao) {
        this.tipo = tipo;
        this.posicao = posicao;
        this.veiculoId = null;
    }

    public TipoSegmento getTipo() {
        return this.tipo;
    }

    public Posicao getPosicao() {
        return this.posicao;
    }

    public synchronized Integer getVeiculoId() {
        return this.veiculoId;
    }

    public synchronized void setVeiculoId(Integer veiculoId) {
        this.veiculoId = veiculoId;
    }

    public synchronized boolean estaOcupada() {
        return this.veiculoId != null;
    }

    public synchronized boolean estaLivre() {
        return this.veiculoId == null;
    }

    public synchronized boolean tentarOcupar(Integer veiculoId) {
        if (this.veiculoId == null) {
            this.veiculoId = veiculoId;
            return true;
        } else {
            return false;
        }
    }

    public synchronized void liberar() {
        this.veiculoId = null;
    }

    public boolean isEntrada(int numLinhas, int numColunas) {
        int l = this.posicao.getLinha();
        int c = this.posicao.getColuna();
        if (l == 0 && this.tipo == TipoSegmento.ESTRADA_BAIXO) {
            return true;
        } else if (l == numLinhas - 1 && this.tipo == TipoSegmento.ESTRADA_CIMA) {
            return true;
        } else if (c == 0 && this.tipo == TipoSegmento.ESTRADA_DIREITA) {
            return true;
        } else {
            return c == numColunas - 1 && this.tipo == TipoSegmento.ESTRADA_ESQUERDA;
        }
    }

    public boolean isSaida(int numLinhas, int numColunas) {
        int l = this.posicao.getLinha();
        int c = this.posicao.getColuna();
        if (l == 0 && this.tipo == TipoSegmento.ESTRADA_CIMA) {
            return true;
        } else if (l == numLinhas - 1 && this.tipo == TipoSegmento.ESTRADA_BAIXO) {
            return true;
        } else if (c == 0 && this.tipo == TipoSegmento.ESTRADA_ESQUERDA) {
            return true;
        } else {
            return c == numColunas - 1 && this.tipo == TipoSegmento.ESTRADA_DIREITA;
        }
    }

    public String toString() {
        String var10000 = String.valueOf(this.tipo);
        return "Celula{tipo=" + var10000 + ", posicao=" + String.valueOf(this.posicao) + ", veiculoId=" + this.veiculoId + "}";
    }
}
