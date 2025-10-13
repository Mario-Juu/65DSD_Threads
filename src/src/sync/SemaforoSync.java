
package sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import model.Celula;
import model.Malha;
import model.Posicao;

public class SemaforoSync implements SincronizacaoStrategy {
    private final Map<Posicao, Semaphore> semaforos = new HashMap();
    private final Map<Posicao, Map<Integer, List<Posicao>>> caminhosReservados = new HashMap();
    private static final long TIMEOUT_MS = 100L;

    public void inicializarCruzamento(Posicao cruzamento) {
        this.semaforos.put(cruzamento, new Semaphore(1, true));
        this.caminhosReservados.put(cruzamento, new HashMap<>());
    }

    public boolean tentarEntrarCruzamento(Posicao cruzamento, List<Posicao> caminhoDesejado, int veiculoId, int direcaoSaida, Malha malha) {
        Semaphore semaforo = this.semaforos.get(cruzamento);
        if (semaforo == null) {
            return false;
        }

        try {
            // Tenta adquirir o semáforo
            if (!semaforo.tryAcquire(100L, TimeUnit.MILLISECONDS)) {
                return false;
            }

            // Verifica se o caminho está livre
            if (!caminhoEstaLivre(caminhoDesejado, malha)) {
                semaforo.release();
                return false;
            }

            // Reserva todas as células do caminho
            List<Posicao> reservadas = new ArrayList<>();
            for (int i = 1; i < caminhoDesejado.size(); i++) {
                Posicao pos = caminhoDesejado.get(i);
                Celula celula = malha.getCelula(pos);

                if (!celula.tentarOcupar(veiculoId)) {
                    // Falhou ao reservar, libera tudo
                    for (Posicao posReservada : reservadas) {
                        malha.getCelula(posReservada).liberar();
                    }
                    semaforo.release();
                    return false;
                }
                reservadas.add(pos);
            }

            // Armazena o caminho reservado
            synchronized(this.caminhosReservados) {
                this.caminhosReservados.get(cruzamento).put(veiculoId, new ArrayList<>(caminhoDesejado));
            }

            semaforo.release();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean caminhoEstaLivre(List<Posicao> caminho, Malha malha) {
        for (Posicao pos : caminho) {
            Celula celula = malha.getCelula(pos);
            if (celula.estaOcupada()) {
                return false;
            }
        }
        return true;
    }

    public void sairCruzamento(Posicao cruzamento, int veiculoId) {
        synchronized(this.caminhosReservados) {
            Map<Integer, List<Posicao>> caminhos = this.caminhosReservados.get(cruzamento);
            if (caminhos != null) {
                caminhos.remove(veiculoId);
            }
        }
    }

    public String getNome() {
        return "Semáforos";
    }
}
