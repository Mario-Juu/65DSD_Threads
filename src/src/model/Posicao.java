//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package model;

import java.util.Objects;

public class Posicao {
    private final int linha;
    private final int coluna;

    public Posicao(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return this.linha;
    }

    public int getColuna() {
        return this.coluna;
    }

    public Posicao proximaPosicao(int direcao) {
        switch (direcao) {
            case 1 -> {
                return new Posicao(this.linha - 1, this.coluna);
            }
            case 2 -> {
                return new Posicao(this.linha, this.coluna + 1);
            }
            case 3 -> {
                return new Posicao(this.linha + 1, this.coluna);
            }
            case 4 -> {
                return new Posicao(this.linha, this.coluna - 1);
            }
            default -> {
                return null;
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Posicao posicao = (Posicao)o;
            return this.linha == posicao.linha && this.coluna == posicao.coluna;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.linha, this.coluna});
    }

    public String toString() {
        return "(" + this.linha + "," + this.coluna + ")";
    }
}
