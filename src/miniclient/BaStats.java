package miniclient;

import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class BaStats {
    private static final Rectangle[] lobbyAreas = new Rectangle[] {
        new Rectangle(2576, 5291, 8, 8),
        new Rectangle(2584, 5291, 8, 8),
        new Rectangle(2595, 5291, 8, 8),
        new Rectangle(2603, 5291, 8, 8),
        new Rectangle(2576, 5281, 8, 8),
        new Rectangle(2584, 5281, 8, 8),
        new Rectangle(2595, 5281, 8, 8),
        new Rectangle(2603, 5281, 8, 8),
        new Rectangle(2576, 5271, 8, 8),
        new Rectangle(2584, 5271, 8, 8),
        new Rectangle(2587, 5259, 32, 12)
    };
    private static final Rectangle lobbyArea = new Rectangle(2576, 5259, 35, 42);
    private static final Rectangle upstairsArea = new Rectangle(2529, 3567, 20, 20);
    private static final Rectangle arrowRoomArea = new Rectangle(2595, 5271, 16, 8);
    public String infoString;

    private int currentWave = 0; // 0-9.
    private long[] waveEnterTimes = new long[10];
    private long[] waveExitTimes = new long[10];

    private int prevWorldX;
    private int prevWorldY;

    public boolean update(int worldX, int worldY) {
        boolean newInfo = false;

        long currentTime = System.nanoTime();

        if (worldX > 6400) { // In instance.
            if (lobbyAreas[currentWave].contains(prevWorldX, prevWorldY) ||
                arrowRoomArea.contains(prevWorldX, prevWorldY)
            ) {
                waveEnterTimes[currentWave] = currentTime;
            }
        } else {
            if (prevWorldX > 6400 && lobbyAreas[currentWave + 1].contains(worldX, worldY)) {
                waveExitTimes[currentWave] = currentTime;
                infoString = createInfoString();
                newInfo = true;

                if (currentWave == 9) {
                    currentWave = 0;
                } else {
                    ++currentWave;
                }
            } else if (infoString != null && currentWave == 0 && !lobbyArea.contains(worldX, worldY) && !upstairsArea.contains(worldX, worldY)) {
                infoString = null;
                newInfo = true;
            }
        }

        prevWorldX = worldX;
        prevWorldY = worldY;
        return newInfo;
    }

    private String qsTimeUpToWave(int wave) {
        long total = 0;
        for (int i = 1; i <= wave; ++i) {
            total += waveEnterTimes[i] - waveExitTimes[i - 1];
        }
        return formatIntervalSeconds(total);
    }

    private String createInfoString() {
        StringBuilder builder = new StringBuilder();
        builder.append(formatInterval(waveExitTimes[currentWave] - waveEnterTimes[0]));
        builder.append(" |");
        for (int i = 0; i <= currentWave; ++i) {
            if (i == 5) {
                builder.append(" -");
            }
            builder.append(" ");
            builder.append(formatIntervalSeconds(waveExitTimes[i] - waveEnterTimes[i]));
        }
        if (currentWave == 9) {
            builder.append(" | ");
            builder.append(qsTimeUpToWave(9));
        }
        return builder.toString();
    }

    private static String formatIntervalSeconds(long nanoSeconds) {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        return new DecimalFormat("0.0", dfs).format(nanoSeconds / 1000000000.0);
    }

    private static String formatInterval(long nanoSeconds) {
        long tenths = nanoSeconds / 100000000;
        long seconds = tenths / 10;
        long minutes = seconds / 60;
        return String.format("%02d:%02d.%d", minutes, seconds % 60, tenths % 10);
    }
}