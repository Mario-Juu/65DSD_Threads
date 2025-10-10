//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package model;

import java.util.List;
import java.util.Map;
import sync.SincronizacaoStrategy;

public class Cruzamento {
    private final Malha malha;
    private final SincronizacaoStrategy estrategia;

    public Cruzamento(Malha malha, SincronizacaoStrategy estrategia) {
        this.malha = malha;
        this.estrategia = estrategia;
        Map<Posicao, List<Posicao>> cruzamentos = malha.getCruzamentos();

        for(Posicao posCruzamento : cruzamentos.keySet()) {
            estrategia.inicializarCruzamento(posCruzamento);
        }

    }

    public boolean tentarEntrar(Posicao posicao, int veiculoId) {
        List<Posicao> celulasCruzamento = this.malha.getCelulasCruzamento(posicao);
        if (celulasCruzamento.isEmpty()) {
            System.err.println("ERRO: Cruzamento não encontrado: " + posicao);
            return false;
        }
        return this.estrategia.tentarEntrarCruzamento(posicao, celulasCruzamento, veiculoId);
    }

    public void sair(Posicao posicao, int veiculoId) {
        this.estrategia.sairCruzamento(posicao, veiculoId);
    }

    public Posicao getPosicaoCruzamento(Posicao posicao) {
        return this.malha.getPosicaoCruzamento(posicao);
    }

    public boolean isCruzamento(Posicao posicao) {
        return this.malha.isCruzamento(posicao);
    }

    public SincronizacaoStrategy getEstrategia() {
        return this.estrategia;
    }
}
