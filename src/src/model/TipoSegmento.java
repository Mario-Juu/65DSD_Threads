//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package model;

import java.util.ArrayList;
import java.util.List;

public enum TipoSegmento {
    NADA(0, new int[0], false),
    ESTRADA_CIMA(1, new int[]{1}, false),
    ESTRADA_DIREITA(2, new int[]{2}, false),
    ESTRADA_BAIXO(3, new int[]{3}, false),
    ESTRADA_ESQUERDA(4, new int[]{4}, false),
    CRUZAMENTO_CIMA(5, new int[]{1}, true),
    CRUZAMENTO_DIREITA(6, new int[]{2}, true),
    CRUZAMENTO_BAIXO(7, new int[]{3}, true),
    CRUZAMENTO_ESQUERDA(8, new int[]{4}, true),
    CRUZAMENTO_CIMA_DIREITA(9, new int[]{1, 2}, true),
    CRUZAMENTO_CIMA_ESQUERDA(10, new int[]{1, 4}, true),
    CRUZAMENTO_DIREITA_BAIXO(11, new int[]{2, 3}, true),
    CRUZAMENTO_BAIXO_ESQUERDA(12, new int[]{3, 4}, true);

    private final int codigo;
    private final int[] direcoesPermitidas;
    private final boolean isCruzamento;

    private TipoSegmento(int codigo, int[] direcoesPermitidas, boolean isCruzamento) {
        this.codigo = codigo;
        this.direcoesPermitidas = direcoesPermitidas;
        this.isCruzamento = isCruzamento;
    }

    public int getCodigo() {
        return this.codigo;
    }

    public int[] getDirecoesPermitidas() {
        return this.direcoesPermitidas;
    }

    public boolean isCruzamento() {
        return this.isCruzamento;
    }

    public static TipoSegmento fromCodigo(int codigo) {
        for(TipoSegmento tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }

        return NADA;
    }

    public boolean permiteDirecao(int direcao) {
        for(int dir : this.direcoesPermitidas) {
            if (dir == direcao) {
                return true;
            }
        }

        return false;
    }

    public List<Integer> getDirecoesSaida(int direcaoEntrada) {
        List<Integer> saidas = new ArrayList();
        if (this.isCruzamento) {
            int direcaoOposta = this.getDirecaoOposta(direcaoEntrada);

            for(int dir : this.direcoesPermitidas) {
                if (dir != direcaoOposta) {
                    saidas.add(dir);
                }
            }
        } else if (this.direcoesPermitidas.length > 0) {
            saidas.add(this.direcoesPermitidas[0]);
        }

        return saidas;
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

    public String toString() {
        String var10000 = this.name();
        return var10000 + "(" + this.codigo + ")";
    }
}
