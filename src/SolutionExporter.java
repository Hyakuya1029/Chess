import java.io.File;
import javax.swing.*;

/**
 * 方案导出器
 */
public class SolutionExporter {
    private int rows;
    private int cols;
    private int[][] board;

    public SolutionExporter(int rows, int cols, int[][] board) {
        this.rows = rows;
        this.cols = cols;
        this.board = board;
    }

    /**
     * 生成格式化报告
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();

        report.append("=============== 极小满覆盖方案 ===============\n");
        report.append(String.format("棋盘大小：%d 行 × %d 列\n", rows, cols));
        report.append("----------------------------------------\n\n");

        // 坐标列表
        report.append("【坐标列表】\n");
        int horseCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 1) {
                    horseCount++;
                    report.append(String.format("  马 %d: r%d c%d\n", horseCount, i + 1, j + 1));
                }
            }
        }
        report.append(String.format("\n总计：%d 匹马\n\n", horseCount));

        // 矩阵形式
        report.append("【矩阵形式】（1=有马, 0=无马）\n");
        report.append("   ");
        for (int j = 0; j < cols; j++) {
            report.append(String.format("c%-3d", j + 1));
        }
        report.append("\n");

        for (int i = 0; i < rows; i++) {
            report.append(String.format("r%-2d", i + 1));
            for (int j = 0; j < cols; j++) {
                report.append(String.format("%-4d", board[i][j]));
            }
            report.append("\n");
        }

        report.append("==========================================\n");

        return report.toString();
    }

    /**
     * 打印到控制台
     */
    public void printToConsole() {
        System.out.println(generateReport());
    }

    /**
     * 显示导出对话框
     */
    public void showExportDialog(JFrame parentFrame) {
        String content = generateReport();

        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new javax.swing.plaf.FontUIResource("宋体", java.awt.Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));

        JPanel dialogPanel = new JPanel(new java.awt.BorderLayout());
        dialogPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("保存到文件");
        saveBtn.addActionListener(e -> saveToFile(parentFrame, content));
        buttonPanel.add(saveBtn);
        dialogPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parentFrame, dialogPanel, "导出极小满覆盖方案",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 保存到文件
     */
    public void saveToFile(JFrame parentFrame, String content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存极小满覆盖方案");
        fileChooser.setSelectedFile(new File("极小满覆盖方案_" + System.currentTimeMillis() + ".txt"));

        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                java.nio.file.Files.write(
                    file.toPath(),
                    content.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
                JOptionPane.showMessageDialog(parentFrame,
                    "方案已成功保存到：\n" + file.getAbsolutePath(),
                    "保存成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame,
                    "保存失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 直接保存到指定路径
     */
    public void saveToFile(String filePath) throws Exception {
        String content = generateReport();
        java.nio.file.Files.write(
            new File(filePath).toPath(),
            content.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}

