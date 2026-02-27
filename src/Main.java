import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 在Swing事件线程中启动界面，避免卡顿
        SwingUtilities.invokeLater(() -> new ChessboardGUI());
    }
}