import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class ChessboardGUI extends JFrame {
    private ChessboardCover cover;    // 核心逻辑实例
    private JPanel chessPanel;        // 棋盘绘制面板
    private JTextField sizeField;     // 棋盘大小输入框
    private JLabel horseCountLabel;   // 马的数量标签
    private BufferedImage horseImg;   // 马的图片

    // 棋盘格子大小（像素）
    private static final int CELL_SIZE = 50;
    private static final int MAX_BOARD_PIXEL = 600;  // 最大棋盘展示像素
    private int currentCellSize;  // 动态单元格大小
    private static final int BOARD_PADDING = 28;
    private static final Color LIGHT_CELL = new Color(240, 217, 181);
    private static final Color DARK_CELL = new Color(181, 136, 99);
    private static final Color GRID_COLOR = new Color(90, 70, 50);
    private static final Color LABEL_COLOR = new Color(60, 60, 60);
    private static final Color FRAME_COLOR = new Color(120, 85, 55);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 40);

    // 其他常量
    private static final String IMAGE_PATH = "horse.png";

    public ChessboardGUI() {
        // 初始化核心逻辑（默认8×8）
        cover = new ChessboardCover(8);
        this.currentCellSize = CELL_SIZE;
        // 加载马的图片
        loadHorseImage();
        // 初始化界面
        initGUI();
    }

    /**
     * 初始化Swing界面
     */
    private void initGUI() {
        setTitle("国际象棋中马的极小满覆盖");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. 控制面板（顶部：输入框+按钮）
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        controlPanel.setBackground(new Color(248, 248, 248));
        controlPanel.add(new JLabel("棋盘大小："));

        sizeField = new JTextField("8", 5);
        sizeField.setFont(sizeField.getFont().deriveFont(12f));
        controlPanel.add(sizeField);

        // 生成按钮（圆角、主题色）
        JButton genBtn = createStyledButton("生成极小覆盖");
        genBtn.addActionListener(e -> regenerateCover());
        controlPanel.add(genBtn);

        // 演示按钮
        JButton demoBtn = createStyledButton("极小满覆盖试探过程演示");
        demoBtn.addActionListener(e -> demoBacktracking());
        controlPanel.add(demoBtn);

        // 马的数量显示
        horseCountLabel = new JLabel("马数: 0");
        horseCountLabel.setFont(horseCountLabel.getFont().deriveFont(12f));
        horseCountLabel.setForeground(new Color(80, 80, 80));
        controlPanel.add(horseCountLabel);

        add(controlPanel, BorderLayout.NORTH);

        // 2. 棋盘面板（中间：绘制棋盘）
        chessPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChessboard(g);
            }

            @Override
            public Dimension getPreferredSize() {
                int size = cover.getSize();
                int boardSize = size * currentCellSize + BOARD_PADDING * 2;
                return new Dimension(boardSize, boardSize);
            }
        };
        chessPanel.setBackground(new Color(245, 245, 245));
        add(chessPanel, BorderLayout.CENTER);

        // 窗口设置
        pack();
        setLocationRelativeTo(null); // 居中
        setVisible(true);
    }

    /**
     * 创建有圆角和主题色的按钮
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
        int count = countHorses();
        horseCountLabel.setText("马数: " + count);
    }

    /**
     * 统计棋盘上的马数
     */
    private int countHorses() {
        int[][] board = cover.getBoard();
        int count = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 1) count++;
            }
        }
        return count;
    }

    /**
     * 计算合适的单元格大小（防止超出屏幕）
     */
    private void updateCellSize() {
        int size = cover.getSize();
        int minCellSize = Math.max(20, MAX_BOARD_PIXEL / size);  // 最小20px
        currentCellSize = Math.min(CELL_SIZE, minCellSize);
    }

    /**
     * 绘制棋盘和马
     */
    private void drawChessboard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int size = cover.getSize();
        int[][] board = cover.getBoard();
        int boardSize = size * currentCellSize;
        int frameSize = boardSize + 12;

        // 自适应居中（至少保留最小边距）
        int offsetX = Math.max(BOARD_PADDING, (chessPanel.getWidth() - boardSize) / 2);
        int offsetY = Math.max(BOARD_PADDING, (chessPanel.getHeight() - boardSize) / 2);
        int frameX = offsetX - 6;
        int frameY = offsetY - 6;

        // 0. 阴影和木质边框
        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(frameX + 4, frameY + 4, frameSize, frameSize, 12, 12);
        g2d.setColor(FRAME_COLOR);
        g2d.fillRoundRect(frameX, frameY, frameSize, frameSize, 12, 12);

        // 1. 绘制棋盘格子（黑白交替）
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
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

        // 2. 行列标记（a-h / 1-8）
        g2d.setColor(LABEL_COLOR);
        int fontSize = Math.max(8, currentCellSize / 6);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, fontSize));
        for (int i = 0; i < size; i++) {
            String col = String.valueOf((char) ('a' + i));
            String row = String.valueOf(size - i);
            int colX = offsetX + i * currentCellSize + currentCellSize / 2 - 4;
            int colY = offsetY + boardSize + 22;
            int rowX = offsetX - 20;
            int rowY = offsetY + i * currentCellSize + currentCellSize / 2 + 4;
            g2d.drawString(col, colX, colY);
            g2d.drawString(row, rowX, rowY);
        }

        g2d.dispose();
    }

    /**
     * 绘制马（图片/兜底圆形）
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
     * 加载马的图片（兼容根目录/src目录）
     */
    private void loadHorseImage() {
        try {
            // 优先读取项目根目录的horse.png
            File imgFile = new File(IMAGE_PATH);
            if (imgFile.exists()) {
                horseImg = ImageIO.read(imgFile);
            } else {
                // 读取src目录下的horse.png
                InputStream is = getClass().getResourceAsStream("/" + IMAGE_PATH);
                if (is != null) {
                    horseImg = ImageIO.read(is);
                }
            }
            // 缩放图片到合适大小
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
     * 重新生成极小覆盖（根据输入的棋盘大小）
     */
    private void regenerateCover() {
        try {
            int newSize = Integer.parseInt(sizeField.getText().trim());
            if (newSize < 1) {
                JOptionPane.showMessageDialog(this, "棋盘大小不能小于1！");
                return;
            }
            cover.resetSize(newSize);
            updateCellSize();
            updateHorseCount();
            chessPanel.repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字！");
        }
    }

    /**
     * 演示试探过程（回溯版：逐步尝试移除，失败则回退）
     */
    private void demoBacktracking() {
        int size = cover.getSize();
        int[][] board = cover.getBoard();

        // 1. 清空棋盘
        for (int i = 0; i < size; i++) {
            Arrays.fill(board[i], 0);
        }
        SwingUtilities.invokeLater(() -> {
            updateHorseCount();
            chessPanel.repaint();
        });
        sleepFix(300);

        // 2. 先铺满马（演示起点）
        new Thread(() -> {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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

            // 3. 回溯尝试移除
            backtrackRemove(0, size, board);

            SwingUtilities.invokeLater(() -> {
                updateHorseCount();
                JOptionPane.showMessageDialog(this, "演示完成！已生成极小满覆盖");
                chessPanel.repaint();
            });
        }).start();
    }

    /**
     * 回溯移除：逐格尝试移除，失败就回退
     */
    private void backtrackRemove(int idx, int size, int[][] board) {
        if (idx >= size * size) {
            return;
        }
        int i = idx / size;
        int j = idx % size;

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

        backtrackRemove(idx + 1, size, board);
    }

    /**
     * 封装sleep，避免异常
     */
    private void sleepFix(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessboardGUI::new);
    }
}
