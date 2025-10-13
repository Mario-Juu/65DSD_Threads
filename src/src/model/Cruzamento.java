
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

    public boolean tentarEntrar(Posicao posicao, int veiculoId, List<Posicao> caminhoDesejado, int direcaoSaida) {
        return this.estrategia.tentarEntrarCruzamento(posicao, caminhoDesejado, veiculoId, direcaoSaida, this.malha);
    }

    public void sair(Posicao posicao, int veiculoId) {
        this.estrategia.sairCruzamento(posicao, veiculoId);
    }

    public Posicao getPosicaoCruzamento(Posicao posicao) {
        return this.malha.getPosicaoCruzamento(posicao);
    }

    public SincronizacaoStrategy getEstrategia() {
        return this.estrategia;
    }
}
