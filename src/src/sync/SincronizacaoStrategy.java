//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package sync;

import java.util.List;
import model.Posicao;

public interface SincronizacaoStrategy {
    boolean tentarEntrarCruzamento(Posicao var1, List<Posicao> var2, int var3);

    void sairCruzamento(Posicao var1, int var2);

    void inicializarCruzamento(Posicao var1);

    String getNome();
}
