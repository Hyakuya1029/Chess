import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class ChessboardGUI extends JFrame {
    private ChessboardCover cover;
    private JPanel chessPanel;
    private JTextField sizeField;
    private JTextField rowsField;
    private JTextField colsField;
    private JLabel horseCountLabel;
    private BufferedImage horseImg;

    private Thread demoThread;
    private JButton genBtn;
    private JButton demoBtn;
    private JButton stopBtn;

    private static final int CELL_SIZE = 50;
    private static final int MAX_BOARD_PIXEL = 600;
    private int currentCellSize;
    private static final int BOARD_PADDING = 28;
    private static final Color LIGHT_CELL = new Color(240, 217, 181);
    private static final Color DARK_CELL = new Color(181, 136, 99);
    private static final Color GRID_COLOR = new Color(90, 70, 50);
    private static final Color LABEL_COLOR = new Color(60, 60, 60);
    private static final Color FRAME_COLOR = new Color(120, 85, 55);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 40);
    private static final String IMAGE_PATH = "horse.png";

    public ChessboardGUI() {
        System.setProperty("file.encoding", "UTF-8");
        cover = new ChessboardCover(8);
        this.currentCellSize = CELL_SIZE;
        loadHorseImage();
        initGUI();
    }

    /**
     * 初始化界面
     */
    private void initGUI() {
        setTitle("国际象棋中马的极小满覆盖");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        controlPanel.setBackground(new Color(248, 248, 248));

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(248, 248, 248));
        inputPanel.add(new JLabel("行数："));

        rowsField = new JTextField("8", 3);
        rowsField.setFont(rowsField.getFont().deriveFont(12f));
        inputPanel.add(rowsField);

        inputPanel.add(new JLabel("列数："));

        colsField = new JTextField("8", 3);
        colsField.setFont(colsField.getFont().deriveFont(12f));
        inputPanel.add(colsField);

        controlPanel.add(inputPanel);

        sizeField = new JTextField("8", 5);
        sizeField.setVisible(false);

        genBtn = createStyledButton("生成极小覆盖");
        genBtn.addActionListener(e -> regenerateCover());
        controlPanel.add(genBtn);

        demoBtn = createStyledButton("极小满覆盖试探过程演示");
        demoBtn.addActionListener(e -> demoBacktracking());
        controlPanel.add(demoBtn);

        stopBtn = createStyledButton("停止演示");
        stopBtn.addActionListener(e -> stopDemo());
        stopBtn.setEnabled(false);
        controlPanel.add(stopBtn);

        JButton exportBtn = createStyledButton("导出方案");
        exportBtn.addActionListener(e -> {
            SolutionExporter exporter = new SolutionExporter(cover.getRows(), cover.getCols(), cover.getBoard());
            exporter.printToConsole();
            exporter.showExportDialog(this);
        });
        controlPanel.add(exportBtn);

        horseCountLabel = new JLabel("马数: 0");
        horseCountLabel.setFont(horseCountLabel.getFont().deriveFont(12f));
        horseCountLabel.setForeground(new Color(80, 80, 80));
        controlPanel.add(horseCountLabel);

        add(controlPanel, BorderLayout.NORTH);

        chessPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChessboard(g);
            }

            @Override
            public Dimension getPreferredSize() {
                int rows = cover.getRows();
                int cols = cover.getCols();
                int boardHeight = rows * currentCellSize + BOARD_PADDING * 2;
                int boardWidth = cols * currentCellSize + BOARD_PADDING * 2;
                return new Dimension(boardWidth, boardHeight);
            }
        };
        chessPanel.setBackground(new Color(245, 245, 245));
        add(chessPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * 创建样式按钮
     */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制圆角背景
                Color bgColor = getModel().isPressed() ?
                    new Color(100, 70, 40) :
                    new Color(140, 100, 60);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 绘制文字
                super.paintComponent(g);
            }
        };
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(140, 100, 60));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 12, 6, 12));
        return btn;
    }

    /**
     * 更新马的数量显示
     */
    private void updateHorseCount() {
        int count = cover.countHorses();
        horseCountLabel.setText("马数: " + count);
    }

    /**
     * 更新单元格大小
     */
    private void updateCellSize() {
        currentCellSize = cover.calculateOptimalCellSize(MAX_BOARD_PIXEL, CELL_SIZE);
    }

    /**
     * 绘制棋盘和马
     */
    private void drawChessboard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int rows = cover.getRows();
        int cols = cover.getCols();
        int[][] board = cover.getBoard();
        int boardHeight = rows * currentCellSize;
        int boardWidth = cols * currentCellSize;
        int frameWidth = boardWidth + 12;
        int frameHeight = boardHeight + 12;

        // 自适应居中（至少保留最小边距）
        int offsetX = Math.max(BOARD_PADDING, (chessPanel.getWidth() - boardWidth) / 2);
        int offsetY = Math.max(BOARD_PADDING, (chessPanel.getHeight() - boardHeight) / 2);
        int frameX = offsetX - 6;
        int frameY = offsetY - 6;

        // 0. 阴影和木质边框
        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(frameX + 4, frameY + 4, frameWidth, frameHeight, 12, 12);
        g2d.setColor(FRAME_COLOR);
        g2d.fillRoundRect(frameX, frameY, frameWidth, frameHeight, 12, 12);

        // 1. 绘制棋盘格子（黑白交替）
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = offsetX + j * currentCellSize;
                int y = offsetY + i * currentCellSize;
                g2d.setColor(((i + j) % 2 == 0) ? LIGHT_CELL : DARK_CELL);
                g2d.fillRect(x, y, currentCellSize, currentCellSize);
                g2d.setColor(GRID_COLOR);
                g2d.drawRect(x, y, currentCellSize, currentCellSize);

                if (board[i][j] == 1) {
                    drawHorse(g2d, x, y);
                }
            }
        }

        // 2. 行列标记（r1-rn / c1-cn）
        g2d.setColor(LABEL_COLOR);
        int fontSize = Math.max(8, currentCellSize / 6);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, fontSize));

        // 列标记（c1, c2, c3, ...）
        for (int j = 0; j < cols; j++) {
            String col = "c" + (j + 1);
            int colX = offsetX + j * currentCellSize + currentCellSize / 2 - 8;
            int colY = offsetY + boardHeight + 22;
            g2d.drawString(col, colX, colY);
        }

        // 行标记（r1, r2, r3, ...）
        for (int i = 0; i < rows; i++) {
            String row = "r" + (i + 1);
            int rowX = offsetX - 22;
            int rowY = offsetY + i * currentCellSize + currentCellSize / 2 + 4;
            g2d.drawString(row, rowX, rowY);
        }

        g2d.dispose();
    }

    /**
     * 绘制马
     */
    private void drawHorse(Graphics g, int x, int y) {
        if (horseImg != null) {
            int imgSize = Math.max(10, currentCellSize - 10);
            Image scaledImg = horseImg.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH);
            int imgX = x + (currentCellSize - imgSize) / 2;
            int imgY = y + (currentCellSize - imgSize) / 2;
            g.drawImage(scaledImg, imgX, imgY, null);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(30, 30, 30));
            int circleX = x + Math.max(3, (currentCellSize - 14) / 2);
            int circleY = y + Math.max(3, (currentCellSize - 14) / 2);
            g2d.fillOval(circleX, circleY, Math.max(4, currentCellSize - 14), Math.max(4, currentCellSize - 14));
        }
    }

    /**
     * 加载马的图片
     */
    private void loadHorseImage() {
        try {
            File imgFile = new File(IMAGE_PATH);
            if (imgFile.exists()) {
                horseImg = ImageIO.read(imgFile);
            } else {
                InputStream is = getClass().getResourceAsStream("/" + IMAGE_PATH);
                if (is != null) {
                    horseImg = ImageIO.read(is);
                }
            }
            if (horseImg != null) {
                horseImg = resizeImage(horseImg, CELL_SIZE - 10, CELL_SIZE - 10);
            }
        } catch (Exception e) {
            // 加载失败，使用兜底圆形
            horseImg = null;
        }
    }

    /**
     * 缩放图片
     */
    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    /**
     * 生成极小覆盖
     */
    private void regenerateCover() {
        try {
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());

            if (rows < 1 || cols < 1) {
                JOptionPane.showMessageDialog(this, "行数和列数都不能小于1！");
                return;
            }
            cover.resetSize(rows, cols);
            updateCellSize();
            updateHorseCount();
            chessPanel.repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字！");
        }
    }

    /**
     * 演示回溯过程
     */
    private void demoBacktracking() {
        if (demoThread != null && demoThread.isAlive()) {
            JOptionPane.showMessageDialog(this, "演示已在进行中，请先停止当前演示！");
            return;
        }

        genBtn.setEnabled(false);
        demoBtn.setEnabled(false);
        stopBtn.setEnabled(true);

        int rows = cover.getRows();
        int cols = cover.getCols();
        int[][] board = cover.getBoard();

        for (int i = 0; i < rows; i++) {
            Arrays.fill(board[i], 0);
        }
        SwingUtilities.invokeLater(() -> {
            updateHorseCount();
            chessPanel.repaint();
        });
        sleepFix(300);

        demoThread = new Thread(() -> {
            try {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        if (Thread.currentThread().isInterrupted()) return;
                        board[i][j] = 1;
                        SwingUtilities.invokeLater(() -> {
                            updateHorseCount();
                            chessPanel.repaint();
                        });
                        sleepFix(100);
                    }
                }
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "开始回溯：尝试移除冗余马")
                );

                demoBacktrackHelper(0, rows, cols, board);

                SwingUtilities.invokeLater(() -> {
                    updateHorseCount();
                    JOptionPane.showMessageDialog(this, "演示完成！已生成极小满覆盖");
                    chessPanel.repaint();
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    genBtn.setEnabled(true);
                    demoBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                    demoThread = null;
                });
            }
        });
        demoThread.start();
    }

    /**
     * 演示回溯的辅助方法
     */
    private void demoBacktrackHelper(int idx, int rows, int cols, int[][] board) {
        int totalSize = rows * cols;
        if (Thread.currentThread().isInterrupted() || idx >= totalSize) {
            return;
        }
        int i = idx / cols;
        int j = idx % cols;

        if (board[i][j] == 1) {
            // 尝试移除
            board[i][j] = 0;
            SwingUtilities.invokeLater(() -> {
                updateHorseCount();
                chessPanel.repaint();
            });
            sleepFix(150);

            if (!cover.isFullCover()) {
                // 失败：回退恢复
                board[i][j] = 1;
                SwingUtilities.invokeLater(() -> {
                    updateHorseCount();
                    chessPanel.repaint();
                });
                sleepFix(150);
            }
        }

        demoBacktrackHelper(idx + 1, rows, cols, board);
    }

    /**
     * 停止演示
     */
    private void stopDemo() {
        if (demoThread != null && demoThread.isAlive()) {
            demoThread.interrupt();
            genBtn.setEnabled(true);
            demoBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            JOptionPane.showMessageDialog(this, "演示已停止");
        }
    }

    /**
     * 线程延迟
     */
    private void sleepFix(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    // 导出功能已移至 SolutionExporter 类
}
