//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Posicao;

public class MonitorSync implements SincronizacaoStrategy {
    private final Map<Posicao, MonitorCruzamento> monitores = new HashMap();
    private static final long TIMEOUT_MS = 100L;

    public void inicializarCruzamento(Posicao cruzamento) {
        this.monitores.put(cruzamento, new MonitorCruzamento(cruzamento));
    }

    public boolean tentarEntrarCruzamento(Posicao cruzamento, List<Posicao> posicoes, int veiculoId) {
        MonitorCruzamento monitor = (MonitorCruzamento)this.monitores.get(cruzamento);
        if (monitor == null) {
            System.err.println("ERRO: Cruzamento não inicializado: " + String.valueOf(cruzamento));
            return false;
        } else {
            return monitor.tentarEntrar(veiculoId, 100L);
        }
    }

    public void sairCruzamento(Posicao cruzamento, int veiculoId) {
        MonitorCruzamento monitor = (MonitorCruzamento)this.monitores.get(cruzamento);
        if (monitor == null) {
            System.err.println("ERRO: Cruzamento não inicializado: " + String.valueOf(cruzamento));
        } else {
            monitor.sair(veiculoId);
        }
    }

    public String getNome() {
        return "Monitores";
    }

    public boolean cruzamentoOcupado(Posicao cruzamento) {
        MonitorCruzamento monitor = (MonitorCruzamento)this.monitores.get(cruzamento);
        return monitor != null && monitor.estaOcupado();
    }

    public Integer getOcupante(Posicao cruzamento) {
        MonitorCruzamento monitor = (MonitorCruzamento)this.monitores.get(cruzamento);
        return monitor != null ? monitor.getOcupante() : null;
    }

    private static class MonitorCruzamento {
        private Integer veiculoOcupante = null;
        private final Posicao posicao;

        public MonitorCruzamento(Posicao posicao) {
            this.posicao = posicao;
        }

        public synchronized boolean tentarEntrar(int veiculoId, long timeoutMs) {
            long tempoInicio = System.currentTimeMillis();
            long tempoRestante = timeoutMs;

            while(this.veiculoOcupante != null && tempoRestante > 0L) {
                try {
                    this.wait(tempoRestante);
                    long tempoDecorrido = System.currentTimeMillis() - tempoInicio;
                    tempoRestante = timeoutMs - tempoDecorrido;
                } catch (InterruptedException var10) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            if (this.veiculoOcupante == null) {
                this.veiculoOcupante = veiculoId;
                return true;
            } else {
                return false;
            }
        }

        public synchronized void sair(int veiculoId) {
            if (this.veiculoOcupante == null) {
                System.err.println("AVISO: Tentando liberar cruzamento vazio: " + String.valueOf(this.posicao));
            } else if (!this.veiculoOcupante.equals(veiculoId)) {
                System.err.println("ERRO: Veículo " + veiculoId + " tentando liberar cruzamento ocupado por " + this.veiculoOcupante);
            } else {
                this.veiculoOcupante = null;
                this.notifyAll();
            }
        }

        public synchronized boolean estaOcupado() {
            return this.veiculoOcupante != null;
        }

        public synchronized Integer getOcupante() {
            return this.veiculoOcupante;
        }
    }
}
