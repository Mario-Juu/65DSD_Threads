
package sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Celula;
import model.Malha;
import model.Posicao;

public class MonitorSync implements SincronizacaoStrategy {
    private final Map<Posicao, MonitorCruzamento> monitores = new HashMap();
    private static final long TIMEOUT_MS = 100L;

    public void inicializarCruzamento(Posicao cruzamento) {
        this.monitores.put(cruzamento, new MonitorCruzamento(cruzamento));
    }

    public boolean tentarEntrarCruzamento(Posicao cruzamento, List<Posicao> caminhoDesejado, int veiculoId, int direcaoSaida, Malha malha) {
        MonitorCruzamento monitor = this.monitores.get(cruzamento);
        return monitor != null && monitor.tentarEntrarComCaminho(veiculoId, caminhoDesejado, malha, 100L);
    }

    public void sairCruzamento(Posicao cruzamento, int veiculoId) {
        MonitorCruzamento monitor = this.monitores.get(cruzamento);
        if (monitor != null) {
            monitor.sair(veiculoId);
        }
    }

    public String getNome() {
        return "Monitores";
    }

    private static class MonitorCruzamento {
        private final Map<Integer, List<Posicao>> caminhosReservados = new HashMap<>();
        private final Posicao posicao;

        public MonitorCruzamento(Posicao posicao) {
            this.posicao = posicao;
        }

        public synchronized boolean tentarEntrarComCaminho(int veiculoId, List<Posicao> caminhoDesejado, Malha malha, long timeoutMs) {
            long tempoInicio = System.currentTimeMillis();
            long tempoRestante = timeoutMs;

            // Espera até que o caminho esteja livre
            while (!caminhoEstaLivre(caminhoDesejado, malha) && tempoRestante > 0L) {
                try {
                    this.wait(tempoRestante);
                    long tempoDecorrido = System.currentTimeMillis() - tempoInicio;
                    tempoRestante = timeoutMs - tempoDecorrido;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            // Verifica se o caminho está livre
            if (!caminhoEstaLivre(caminhoDesejado, malha)) {
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
                    return false;
                }
                reservadas.add(pos);
            }

            // Armazena o caminho reservado por este veículo
            caminhosReservados.put(veiculoId, new ArrayList<>(caminhoDesejado));
            return true;
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

        public synchronized void sair(int veiculoId) {
            List<Posicao> caminho = caminhosReservados.remove(veiculoId);
            if (caminho != null) {
                this.notifyAll();
            }
        }
    }
}
