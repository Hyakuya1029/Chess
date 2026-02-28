import java.util.Arrays;

/**
 * 马的极小满覆盖核心逻辑
 */
public class ChessboardCover {
    private int rows;
    private int cols;
    private int[][] board;  // 1=马, 0=空
    // 马的8个走法方向
    private final int[][] dirs = {{-2,-1},{-2,1},{-1,-2},{-1,2},
            {1,-2},{1,2},{2,-1},{2,1}};

    // 正方形棋盘
    public ChessboardCover(int size) {
        this(size, size);
    }

    // 矩形棋盘
    public ChessboardCover(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];
        initMinimalCover();
    }

    /**
     * 初始化极小满覆盖
     */
    private void initMinimalCover() {
        for (int i = 0; i < rows; i++) {
            Arrays.fill(board[i], 1);
        }
        minimizeCover();
    }

    /**
     * 检查是否为满覆盖
     */
    public boolean isFullCover() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 空位且未被攻击
                if (board[i][j] == 0 && !isAttacked(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查位置是否被马攻击
     */
    public boolean isAttacked(int i, int j) {
        for (int[] d : dirs) {
            int x = i + d[0], y = j + d[1];
            // 边界内且有马
            if (x >= 0 && x < rows && y >= 0 && y < cols && board[x][y] == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 极小化覆盖：移除冗余马
     */
    public void minimizeCover() {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j] == 1) {
                        board[i][j] = 0;
                        // 移除后仍满覆盖则为冗余
                        if (isFullCover()) {
                            changed = true;
                        } else {
                            // 必要马，恢复
                            board[i][j] = 1;
                        }
                    }
                }
            }
        } while (changed);
    }

    /**
     * 统计马的数量
     */
    public int countHorses() {
        int count = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 1) count++;
            }
        }
        return count;
    }

    /**
     * 计算合适的单元格大小
     */
    public int calculateOptimalCellSize(int maxPixel, int defaultSize) {
        int size = Math.max(rows, cols);
        int minCellSize = Math.max(20, maxPixel / size);
        return Math.min(defaultSize, minCellSize);
    }

    /**
     * 回溯移除
     */
    public boolean backtrackRemove(int idx, int rows, int cols, int[][] board) {
        int totalSize = rows * cols;
        if (Thread.currentThread().isInterrupted() || idx >= totalSize) {
            return Thread.currentThread().isInterrupted();
        }

        int i = idx / cols;
        int j = idx % cols;

        if (board[i][j] == 1) {
            board[i][j] = 0;
            if (!isFullCover()) {
                board[i][j] = 1;
            }
        }

        return backtrackRemove(idx + 1, rows, cols, board);
    }

    /**
     * 回溯移除（带回调）
     */
    public boolean backtrackRemoveWithCallback(int idx, int rows, int cols, int[][] board, DemoCallback callback) {
        int totalSize = rows * cols;
        if (Thread.currentThread().isInterrupted() || idx >= totalSize) {
            return Thread.currentThread().isInterrupted();
        }

        int i = idx / cols;
        int j = idx % cols;

        if (board[i][j] == 1) {
            board[i][j] = 0;
            callback.onStateChanged();

            if (!isFullCover()) {
                board[i][j] = 1;
                callback.onStateChanged();
            }
        }

        return backtrackRemoveWithCallback(idx + 1, rows, cols, board, callback);
    }

    /**
     * 演示回调接口
     */
    public interface DemoCallback {
        void onStateChanged();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getSize() { return rows; }
    public int[][] getBoard() { return board; }

    // 重置棋盘大小
    public void resetSize(int newSize) {
        resetSize(newSize, newSize);
    }

    public void resetSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];
        initMinimalCover();
    }
}