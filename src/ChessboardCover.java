import java.util.Arrays;

/**
 * 马的极小满覆盖核心逻辑
 * 包含：满覆盖检查、极小化、覆盖生成
 */
public class ChessboardCover {
    private int size;          // 棋盘大小
    private int[][] board;     // 棋盘数组（1=有马，0=无马）
    // 马的8个走法方向（行偏移，列偏移）
    private final int[][] dirs = {{-2,-1},{-2,1},{-1,-2},{-1,2},
            {1,-2},{1,2},{2,-1},{2,1}};

    public ChessboardCover(int size) {
        this.size = size;
        this.board = new int[size][size];
        // 初始化：生成极小满覆盖
        initMinimalCover();
    }

    /**
     * 初始化极小满覆盖：先铺满马保证满覆盖，再极小化
     */
    private void initMinimalCover() {
        for (int i = 0; i < size; i++) {
            Arrays.fill(board[i], 1);
        }
        minimizeCover();
    }

    /**
     * 检查是否为满覆盖：所有空位都能被马攻击到
     */
    public boolean isFullCover() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // 空位且未被攻击 → 不满足满覆盖
                if (board[i][j] == 0 && !isAttacked(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查位置(i,j)是否被马攻击到
     */
    public boolean isAttacked(int i, int j) {
        for (int[] d : dirs) {
            int x = i + d[0], y = j + d[1];
            // 边界检查 + 该位置有马
            if (x >= 0 && x < size && y >= 0 && y < size && board[x][y] == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 极小化覆盖：移除所有冗余马（无法再移除任何马而不破坏满覆盖）
     */
    public void minimizeCover() {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == 1) {
                        // 试移除这匹马
                        board[i][j] = 0;
                        // 移除后仍满覆盖 → 冗余马，保留移除状态
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

    // Getter方法
    public int getSize() { return size; }
    public int[][] getBoard() { return board; }
    // 重置棋盘大小
    public void resetSize(int newSize) {
        this.size = newSize;
        this.board = new int[newSize][newSize];
        initMinimalCover();
    }
}