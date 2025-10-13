
package view;

import controller.SimuladorController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import sync.MonitorSync;
import sync.SemaforoSync;
import sync.SincronizacaoStrategy;

public class MainFrame extends JFrame {
    private final SimuladorController controller = new SimuladorController();
    private MalhaPanel malhaPanel;
    private JTextField txtMaxVeiculos;
    private JTextField txtIntervaloInsercao;
    private JComboBox<String> cmbEstrategia;
    private JButton btnCarregarMalha;
    private JButton btnIniciar;
    private JButton btnEncerrarInsercao;
    private JButton btnEncerrarSimulacao;
    private JLabel lblStatus;
    private JLabel lblVeiculosAtivos;
    private JSlider sliderZoom;
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    private static final Color COR_SUCESSO = new Color(46, 204, 113);
    private static final Color COR_PERIGO = new Color(231, 76, 60);
    private static final Color COR_AVISO = new Color(241, 196, 15);
    private static final Color COR_FUNDO = new Color(236, 240, 241);

    public MainFrame() {
        this.setTitle("Simulador de Tráfego em Malha Viária");
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(COR_FUNDO);
        this.criarPainelControles();
        this.criarPainelVisualizacao();
        this.criarBarraStatus();
        Timer timerStatus = new Timer(500, (e) -> this.atualizarStatus());
        timerStatus.start();
        this.pack();
        this.setLocationRelativeTo((Component)null);
        this.setMinimumSize(new Dimension(1000, 700));
    }

    private void criarPainelControles() {
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, 1));
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titulo = new JLabel("Controles da Simulação");
        titulo.setFont(new Font("Segoe UI", 1, 18));
        titulo.setForeground(new Color(44, 62, 80));
        titulo.setAlignmentX(0.0F);
        painelPrincipal.add(titulo);
        painelPrincipal.add(Box.createVerticalStrut(15));
        JPanel linha1 = new JPanel(new FlowLayout(0, 10, 5));
        linha1.setBackground(Color.WHITE);
        this.btnCarregarMalha = this.criarBotaoEstilizado("Carregar Malha", COR_PRIMARIA);
        this.btnCarregarMalha.addActionListener((e) -> this.carregarMalha());
        linha1.add(this.btnCarregarMalha);
        linha1.add(Box.createHorizontalStrut(20));
        linha1.add(new JLabel("Sincronização:"));
        this.cmbEstrategia = new JComboBox(new String[]{"Semáforos", "Monitores"});
        this.cmbEstrategia.setFont(new Font("Segoe UI", 0, 14));
        this.cmbEstrategia.setPreferredSize(new Dimension(140, 30));
        this.cmbEstrategia.addActionListener((e) -> this.trocarEstrategia());
        linha1.add(this.cmbEstrategia);
        linha1.setAlignmentX(0.0F);
        painelPrincipal.add(linha1);
        painelPrincipal.add(Box.createVerticalStrut(10));
        JPanel linha2 = new JPanel(new FlowLayout(0, 10, 5));
        linha2.setBackground(Color.WHITE);
        linha2.add(new JLabel("Máx. Veículos:"));
        this.txtMaxVeiculos = new JTextField("10", 5);
        this.txtMaxVeiculos.setFont(new Font("Segoe UI", 0, 14));
        linha2.add(this.txtMaxVeiculos);
        linha2.add(Box.createHorizontalStrut(20));
        linha2.add(new JLabel("Intervalo (ms):"));
        this.txtIntervaloInsercao = new JTextField("800", 5);
        this.txtIntervaloInsercao.setFont(new Font("Segoe UI", 0, 14));
        linha2.add(this.txtIntervaloInsercao);
        linha2.add(Box.createHorizontalStrut(20));
        linha2.add(new JLabel("Zoom:"));
        this.sliderZoom = new JSlider(20, 60, 40);
        this.sliderZoom.setMajorTickSpacing(10);
        this.sliderZoom.setPaintTicks(true);
        this.sliderZoom.setPreferredSize(new Dimension(150, 30));
        this.sliderZoom.setBackground(Color.WHITE);
        this.sliderZoom.addChangeListener((e) -> {
            if (this.malhaPanel != null) {
                this.malhaPanel.setTamanhoCelula(this.sliderZoom.getValue());
            }

        });
        linha2.add(this.sliderZoom);
        linha2.setAlignmentX(0.0F);
        painelPrincipal.add(linha2);
        painelPrincipal.add(Box.createVerticalStrut(10));
        JPanel linha3 = new JPanel(new FlowLayout(0, 10, 5));
        linha3.setBackground(Color.WHITE);
        this.btnIniciar = this.criarBotaoEstilizado("Iniciar Simulação", COR_SUCESSO);
        this.btnIniciar.setEnabled(false);
        this.btnIniciar.addActionListener((e) -> this.iniciarSimulacao());
        linha3.add(this.btnIniciar);
        this.btnEncerrarInsercao = this.criarBotaoEstilizado("Encerrar Inserção", COR_AVISO);
        this.btnEncerrarInsercao.setEnabled(false);
        this.btnEncerrarInsercao.addActionListener((e) -> this.encerrarInsercao());
        linha3.add(this.btnEncerrarInsercao);
        this.btnEncerrarSimulacao = this.criarBotaoEstilizado("Encerrar Simulação", COR_PERIGO);
        this.btnEncerrarSimulacao.setEnabled(false);
        this.btnEncerrarSimulacao.addActionListener((e) -> this.encerrarSimulacao());
        linha3.add(this.btnEncerrarSimulacao);
        linha3.setAlignmentX(0.0F);
        painelPrincipal.add(linha3);
        this.add(painelPrincipal, "North");
    }

    private JButton criarBotaoEstilizado(String texto, final Color cor) {
        final JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", 1, 13));
        botao.setForeground(Color.WHITE);
        botao.setBackground(cor);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(true);
        botao.setPreferredSize(new Dimension(180, 35));
        botao.setCursor(new Cursor(12));
        botao.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                botao.setBackground(cor.brighter());
            }

            public void mouseExited(MouseEvent evt) {
                botao.setBackground(cor);
            }
        });
        return botao;
    }

    private void criarPainelVisualizacao() {
        this.malhaPanel = new MalhaPanel(this.controller);
        JScrollPane scrollPane = new JScrollPane(this.malhaPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.getViewport().setBackground(new Color(240, 245, 250));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        this.add(scrollPane, "Center");
    }

    private void criarBarraStatus() {
        JPanel barraStatus = new JPanel(new BorderLayout());
        barraStatus.setBackground(new Color(44, 62, 80));
        barraStatus.setBorder(new EmptyBorder(10, 15, 10, 15));
        this.lblStatus = new JLabel("Aguardando carregar malha...");
        this.lblStatus.setFont(new Font("Segoe UI", 0, 13));
        this.lblStatus.setForeground(Color.WHITE);
        barraStatus.add(this.lblStatus, "West");
        this.lblVeiculosAtivos = new JLabel("Veículos: 0");
        this.lblVeiculosAtivos.setFont(new Font("Segoe UI", 1, 13));
        this.lblVeiculosAtivos.setForeground(new Color(46, 204, 113));
        barraStatus.add(this.lblVeiculosAtivos, "East");
        this.add(barraStatus, "South");
    }

    private void carregarMalha() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Malha (*.txt)", new String[]{"txt"}));
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Selecione o arquivo de malha");
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == 0) {
            File arquivo = fileChooser.getSelectedFile();

            try {
                SincronizacaoStrategy estrategia = this.getEstrategiaSelecionada();
                this.controller.carregarMalha(arquivo.getAbsolutePath(), estrategia);
                this.lblStatus.setText("Malha carregada: " + arquivo.getName());
                this.btnIniciar.setEnabled(true);
                this.malhaPanel.revalidate();
                this.malhaPanel.repaint();
                this.pack();
                JOptionPane.showMessageDialog(this, "Malha carregada com sucesso!\n\n" + String.valueOf(this.controller.getMalha()), "Sucesso", 1);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar malha:\n" + ex.getMessage(), "Erro", 0);
                ex.printStackTrace();
            }
        }

    }

    private void iniciarSimulacao() {
        try {
            int maxVeiculos = Integer.parseInt(this.txtMaxVeiculos.getText());
            int intervalo = Integer.parseInt(this.txtIntervaloInsercao.getText());
            if (maxVeiculos <= 0 || intervalo <= 0) {
                throw new NumberFormatException();
            }

            this.controller.iniciarSimulacao(maxVeiculos, intervalo);
            this.lblStatus.setText("Simulação em execução...");
            this.btnIniciar.setEnabled(false);
            this.btnEncerrarInsercao.setEnabled(true);
            this.btnEncerrarSimulacao.setEnabled(true);
            this.btnCarregarMalha.setEnabled(false);
            this.cmbEstrategia.setEnabled(false);
            this.txtMaxVeiculos.setEnabled(false);
            this.txtIntervaloInsercao.setEnabled(false);
        } catch (NumberFormatException var3) {
            JOptionPane.showMessageDialog(this, "Por favor, insira valores numéricos válidos maiores que zero.", "Erro de Validação", 0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao iniciar simulação:\n" + ex.getMessage(), "Erro", 0);
            ex.printStackTrace();
        }

    }

    private void encerrarInsercao() {
        this.controller.encerrarInsercao();
        this.lblStatus.setText("Inserção encerrada. Aguardando veículos finalizarem...");
        this.btnEncerrarInsercao.setEnabled(false);
    }

    private void encerrarSimulacao() {
        int confirmacao = JOptionPane.showConfirmDialog(this, "Deseja encerrar a simulação imediatamente?\nTodos os veículos serão removidos.", "Confirmar Encerramento", 0, 2);
        if (confirmacao == 0) {
            this.controller.encerrarSimulacao();
            this.lblStatus.setText("Simulação encerrada.");
            this.btnIniciar.setEnabled(true);
            this.btnEncerrarInsercao.setEnabled(false);
            this.btnEncerrarSimulacao.setEnabled(false);
            this.btnCarregarMalha.setEnabled(true);
            this.cmbEstrategia.setEnabled(true);
            this.txtMaxVeiculos.setEnabled(true);
            this.txtIntervaloInsercao.setEnabled(true);
        }

    }

    private void trocarEstrategia() {
        if (this.controller.isSimulacaoAtiva()) {
            JOptionPane.showMessageDialog(this, "Não é possível trocar a estratégia com a simulação ativa.\nEncerre a simulação primeiro.", "Aviso", 2);
            SwingUtilities.invokeLater(() -> {
                String estrategiaAtual = this.controller.getEstrategiaAtual().getNome();
                this.cmbEstrategia.setSelectedItem(estrategiaAtual);
            });
        } else {
            if (this.controller.getMalha() != null) {
                try {
                    SincronizacaoStrategy novaEstrategia = this.getEstrategiaSelecionada();
                    this.controller.trocarEstrategia(novaEstrategia);
                    this.lblStatus.setText("Estratégia alterada para: " + novaEstrategia.getNome());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao trocar estratégia:\n" + ex.getMessage(), "Erro", 0);
                }
            }

        }
    }

    private SincronizacaoStrategy getEstrategiaSelecionada() {
        String selecao = (String)this.cmbEstrategia.getSelectedItem();
        return (SincronizacaoStrategy)("Monitores".equals(selecao) ? new MonitorSync() : new SemaforoSync());
    }

    private void atualizarStatus() {
        if (this.controller.isSimulacaoAtiva()) {
            int numVeiculos = this.controller.getNumVeiculosAtivos();
            this.lblVeiculosAtivos.setText("Veículos: " + numVeiculos);
            if (!this.controller.isInsercaoAtiva() && numVeiculos == 0) {
                this.lblStatus.setText("Todos os veículos finalizaram.");
                this.btnIniciar.setEnabled(true);
                this.btnEncerrarInsercao.setEnabled(false);
                this.btnEncerrarSimulacao.setEnabled(false);
                this.btnCarregarMalha.setEnabled(true);
                this.cmbEstrategia.setEnabled(true);
                this.txtMaxVeiculos.setEnabled(true);
                this.txtIntervaloInsercao.setEnabled(true);
                this.controller.encerrarSimulacao();
            }
        } else {
            this.lblVeiculosAtivos.setText("Veículos: 0");
        }

    }
}
