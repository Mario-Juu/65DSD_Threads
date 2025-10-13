
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.MainFrame;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Não foi possível configurar Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
        System.out.println("========================================");
        System.out.println("Simulador de Tráfego em Malha Viária");
        System.out.println("========================================");
        System.out.println("Aplicação iniciada com sucesso!");
        System.out.println("Carregue uma malha para começar a simulação.");
        System.out.println("========================================");
    }
}
