
package sync;

import java.util.List;
import model.Malha;
import model.Posicao;

public interface SincronizacaoStrategy {
    boolean tentarEntrarCruzamento(Posicao cruzamento, List<Posicao> caminhoDesejado, int veiculoId, int direcaoSaida, Malha malha);

    void sairCruzamento(Posicao cruzamento, int veiculoId);

    void inicializarCruzamento(Posicao cruzamento);

    String getNome();
}
