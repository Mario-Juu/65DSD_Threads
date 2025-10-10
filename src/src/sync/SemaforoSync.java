//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import model.Posicao;

public class SemaforoSync implements SincronizacaoStrategy {
    private final Map<Posicao, Semaphore> semaforos = new HashMap();
    private final Map<Posicao, Integer> ocupacao = new HashMap();
    private static final long TIMEOUT_MS = 100L;

    public void inicializarCruzamento(Posicao cruzamento) {
        this.semaforos.put(cruzamento, new Semaphore(1, true));
        this.ocupacao.put(cruzamento, null);
    }

    public boolean tentarEntrarCruzamento(Posicao cruzamento, List<Posicao> posicoes, int veiculoId) {
        Semaphore semaforo = (Semaphore)this.semaforos.get(cruzamento);
        if (semaforo == null) {
            System.err.println("ERRO: Cruzamento não inicializado: " + String.valueOf(cruzamento));
            return false;
        } else {
            try {
                boolean adquirido = semaforo.tryAcquire(100L, TimeUnit.MILLISECONDS);
                if (adquirido) {
                    synchronized(this.ocupacao) {
                        this.ocupacao.put(cruzamento, veiculoId);
                    }

                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException var9) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    public void sairCruzamento(Posicao cruzamento, int veiculoId) {
        Semaphore semaforo = (Semaphore)this.semaforos.get(cruzamento);
        if (semaforo == null) {
            System.err.println("ERRO: Cruzamento não inicializado: " + String.valueOf(cruzamento));
        } else {
            synchronized(this.ocupacao) {
                Integer ocupante = (Integer)this.ocupacao.get(cruzamento);
                if (ocupante == null) {
                    System.err.println("AVISO: Tentando liberar cruzamento vazio: " + String.valueOf(cruzamento));
                    return;
                }

                if (!ocupante.equals(veiculoId)) {
                    System.err.println("ERRO: Veículo " + veiculoId + " tentando liberar cruzamento ocupado por " + ocupante);
                    return;
                }

                this.ocupacao.put(cruzamento, null);
            }

            semaforo.release();
        }
    }

    public String getNome() {
        return "Semáforos";
    }

    public boolean cruzamentoOcupado(Posicao cruzamento) {
        synchronized(this.ocupacao) {
            Integer ocupante = (Integer)this.ocupacao.get(cruzamento);
            return ocupante != null;
        }
    }

    public Integer getOcupante(Posicao cruzamento) {
        synchronized(this.ocupacao) {
            return (Integer)this.ocupacao.get(cruzamento);
        }
    }
}
