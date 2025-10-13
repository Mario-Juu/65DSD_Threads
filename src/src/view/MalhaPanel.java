
package view;

import controller.SimuladorController;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import javax.swing.Timer;
import model.Celula;
import model.Malha;
import model.Posicao;
import model.TipoSegmento;
import model.Veiculo;

public class MalhaPanel extends JPanel {
    private final SimuladorController controller;
    private int tamanhoCelula = 40;
    private static final Color COR_FUNDO = new Color(240, 245, 250);
    private static final Color COR_ESTRADA = new Color(60, 60, 60);
    private static final Color COR_LINHA_ESTRADA = new Color(245, 245, 245);
    private static final Color COR_CRUZAMENTO = new Color(80, 80, 80);
    private static final Color COR_ENTRADA = new Color(46, 204, 113);
    private static final Color COR_SAIDA = new Color(231, 76, 60);
    private static final Color COR_SETA = new Color(255, 235, 59);
    private static final Color COR_GRID = new Color(200, 200, 200);
    private static final Color COR_BORDA = new Color(100, 100, 100);

    public MalhaPanel(SimuladorController controller) {
        this.controller = controller;
        this.setBackground(COR_FUNDO);
        Timer timer = new Timer(16, (e) -> this.repaint());
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Malha malha = this.controller.getMalha();
        if (malha == null) {
            this.desenharMensagem(g2d, "Nenhuma malha carregada");
        } else {
            this.desenharMalha(g2d, malha);
            this.desenharVeiculos(g2d, malha);
            this.desenharInformacoes(g2d, malha);
        }
    }

    private void desenharMalha(Graphics2D g2d, Malha malha) {
        int numLinhas = malha.getNumLinhas();
        int numColunas = malha.getNumColunas();

        for(int i = 0; i < numLinhas; ++i) {
            for(int j = 0; j < numColunas; ++j) {
                Celula celula = malha.getCelula(i, j);
                this.desenharCelula(g2d, celula, i, j, malha);
            }
        }

    }

    private void desenharCelula(Graphics2D g2d, Celula celula, int linha, int coluna, Malha malha) {
        int x = coluna * this.tamanhoCelula;
        int y = linha * this.tamanhoCelula;
        TipoSegmento tipo = celula.getTipo();
        if (tipo == TipoSegmento.NADA) {
            g2d.setColor(COR_FUNDO);
            g2d.fillRect(x, y, this.tamanhoCelula, this.tamanhoCelula);
        } else {
            boolean isEntrada = celula.isEntrada(malha.getNumLinhas(), malha.getNumColunas());
            boolean isSaida = celula.isSaida(malha.getNumLinhas(), malha.getNumColunas());
            Color cor;
            if (isEntrada) {
                cor = COR_ENTRADA;
            } else if (isSaida) {
                cor = COR_SAIDA;
            } else if (tipo.isCruzamento()) {
                cor = COR_CRUZAMENTO;
            } else {
                cor = COR_ESTRADA;
            }

            g2d.setColor(cor);
            g2d.fillRect(x, y, this.tamanhoCelula, this.tamanhoCelula);
            if (!tipo.isCruzamento() && !isEntrada && !isSaida) {
                this.desenharLinhasEstrada(g2d, tipo, x, y);
            }

            g2d.setColor(COR_BORDA);
            g2d.setStroke(new BasicStroke(1.0F));
            g2d.drawRect(x, y, this.tamanhoCelula, this.tamanhoCelula);
            this.desenharSetaDirecao(g2d, tipo, x, y, isEntrada, isSaida);
            if (isEntrada || isSaida) {
                this.desenharDestaqueEspecial(g2d, x, y, isEntrada);
            }

        }
    }

    private void desenharLinhasEstrada(Graphics2D g2d, TipoSegmento tipo, int x, int y) {
        g2d.setColor(COR_LINHA_ESTRADA);
        g2d.setStroke(new BasicStroke(2.0F, 0, 2, 0.0F, new float[]{10.0F, 10.0F}, 0.0F));
        int[] dirs = tipo.getDirecoesPermitidas();
        if (dirs.length > 0) {
            int dir = dirs[0];
            int cx = x + this.tamanhoCelula / 2;
            int cy = y + this.tamanhoCelula / 2;
            if (dir != 1 && dir != 3) {
                g2d.drawLine(x, cy, x + this.tamanhoCelula, cy);
            } else {
                g2d.drawLine(cx, y, cx, y + this.tamanhoCelula);
            }
        }

        g2d.setStroke(new BasicStroke(1.0F));
    }

    private void desenharDestaqueEspecial(Graphics2D g2d, int x, int y, boolean isEntrada) {
        g2d.setStroke(new BasicStroke(3.0F));
        g2d.setColor(isEntrada ? COR_ENTRADA.brighter() : COR_SAIDA.brighter());
        g2d.drawRect(x + 2, y + 2, this.tamanhoCelula - 4, this.tamanhoCelula - 4);
        g2d.setStroke(new BasicStroke(1.0F));
    }

    private void desenharSetaDirecao(Graphics2D g2d, TipoSegmento tipo, int x, int y, boolean isEntrada, boolean isSaida) {
        int[] direcoes = tipo.getDirecoesPermitidas();
        if (direcoes.length != 0) {
            if (isEntrada) {
                g2d.setColor(Color.WHITE);
            } else if (isSaida) {
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(COR_SETA);
            }

            int centroX = x + this.tamanhoCelula / 2;
            int centroY = y + this.tamanhoCelula / 2;
            int tamanhoSeta = this.tamanhoCelula / 3;

            for(int direcao : direcoes) {
                this.desenharSeta(g2d, centroX, centroY, direcao, tamanhoSeta);
            }

        }
    }

    private void desenharSeta(Graphics2D g2d, int x, int y, int direcao, int tamanho) {
        AffineTransform original = g2d.getTransform();
        g2d.translate(x, y);
        double angulo = (double)0.0F;
        switch (direcao) {
            case 1 -> angulo = (-Math.PI / 2D);
            case 2 -> angulo = (double)0.0F;
            case 3 -> angulo = (Math.PI / 2D);
            case 4 -> angulo = Math.PI;
        }

        g2d.rotate(angulo);
        int[] xPoints = new int[]{tamanho, -tamanho / 2, -tamanho / 2};
        int[] yPoints = new int[]{0, -tamanho / 2, tamanho / 2};
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5F));
        g2d.drawPolygon(xPoints, yPoints, 3);
        g2d.setTransform(original);
    }

    private void desenharVeiculos(Graphics2D g2d, Malha malha) {
        for(Veiculo veiculo : this.controller.getVeiculos()) {
            if (veiculo.isAtivo()) {
                Posicao pos = veiculo.getPosicaoAtual();
                if (pos != null && malha.posicaoValida(pos)) {
                    int x = pos.getColuna() * this.tamanhoCelula;
                    int y = pos.getLinha() * this.tamanhoCelula;
                    int raio = (int)((double)this.tamanhoCelula * (double)0.5F);
                    int centroX = x + this.tamanhoCelula / 2 - raio / 2;
                    int centroY = y + this.tamanhoCelula / 2 - raio / 2;
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval(centroX + 2, centroY + 2, raio, raio);
                    g2d.setColor(veiculo.getCor());
                    g2d.fillOval(centroX, centroY, raio, raio);
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2.0F));
                    g2d.drawOval(centroX, centroY, raio, raio);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", 1, this.tamanhoCelula / 4));
                    String id = String.valueOf(veiculo.getVeiculoId());
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (this.tamanhoCelula - fm.stringWidth(id)) / 2;
                    int textY = y + (this.tamanhoCelula - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(3.0F));

                    for(int dx = -1; dx <= 1; ++dx) {
                        for(int dy = -1; dy <= 1; ++dy) {
                            if (dx != 0 || dy != 0) {
                                g2d.drawString(id, textX + dx, textY + dy);
                            }
                        }
                    }

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(id, textX, textY);
                }
            }
        }

    }

    private void desenharInformacoes(Graphics2D g2d, Malha malha) {
        int infoY = malha.getNumLinhas() * this.tamanhoCelula + 30;
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(10, infoY - 20, 500, 35, 10, 10);
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(2.0F));
        g2d.drawRoundRect(10, infoY - 20, 500, 35, 10, 10);
        g2d.setColor(new Color(30, 30, 30));
        g2d.setFont(new Font("Segoe UI", 1, 14));
        int numVeiculos = this.controller.getNumVeiculosAtivos();
        String estrategia = this.controller.getEstrategiaAtual() != null ? this.controller.getEstrategiaAtual().getNome() : "N/A";
        String info = String.format("Veículos Ativos: %d   |   Sincronização: %s", numVeiculos, estrategia);
        g2d.drawString(info, 20, infoY);
    }

    private void desenharMensagem(Graphics2D g2d, String mensagem) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Segoe UI", 1, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (this.getWidth() - fm.stringWidth(mensagem)) / 2;
        int y = this.getHeight() / 2;
        g2d.drawString(mensagem, x, y);
    }

    public Dimension getPreferredSize() {
        Malha malha = this.controller.getMalha();
        if (malha == null) {
            return new Dimension(800, 800);
        } else {
            int largura = malha.getNumColunas() * this.tamanhoCelula + 20;
            int altura = malha.getNumLinhas() * this.tamanhoCelula + 80;
            return new Dimension(largura, altura);
        }
    }

    public void setTamanhoCelula(int tamanho) {
        this.tamanhoCelula = Math.max(20, Math.min(60, tamanho));
        this.revalidate();
        this.repaint();
    }

    public int getTamanhoCelula() {
        return this.tamanhoCelula;
    }
}
