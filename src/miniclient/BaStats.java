package miniclient;

import java.awt.Rectangle;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private static final File breakdownFile = new File(System.getProperty("user.home") + "/MiniClient/RoundBreakdown.html");
    private static final String breakdownTemplate =
        "<html>\n" +
        "    <head>\n" +
        "        <title>Round breakdown</title>\n" +
        "    </head>\n" +
        "    <body>\n" +
        "        <font size = 5><b>Round Time: </b>%time</font><br>\n" +
        "        <font size = 4><b>Quickstart Time: </b>%qstime</font><br><br>\n" +
        "        <table border=1 cellspacing=0 cellpadding=5>\n" +
        "            <tr align=\"center\">\n" +
        "                <th>Wave</th>\n" +
        "                <th>Time</th>\n" +
        "                <th>Qs</th>\n" +
        "                <th>Completed</th>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>1</td>\n" +
        "                <td>%wave1</td>\n" +
        "                <td>-</td>\n" +
        "                <td>%exit1</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>2</td>\n" +
        "                <td>%wave2</td>\n" +
        "                <td>%qs2</td>\n" +
        "                <td>%exit2</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>3</td>\n" +
        "                <td>%wave3</td>\n" +
        "                <td>%qs3</td>\n" +
        "                <td>%exit3</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>4</td>\n" +
        "                <td>%wave4</td>\n" +
        "                <td>%qs4</td>\n" +
        "                <td>%exit4</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>5</td>\n" +
        "                <td>%wave5</td>\n" +
        "                <td>%qs5</td>\n" +
        "                <td>%exit5</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>6</td>\n" +
        "                <td>%wave6</td>\n" +
        "                <td>%qs6</td>\n" +
        "                <td>%exit6</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>7</td>\n" +
        "                <td>%wave7</td>\n" +
        "                <td>%qs7</td>\n" +
        "                <td>%exit7</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>8</td>\n" +
        "                <td>%wave8</td>\n" +
        "                <td>%qs8</td>\n" +
        "                <td>%exit8</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>9</td>\n" +
        "                <td>%wave9</td>\n" +
        "                <td>%qs9</td>\n" +
        "                <td>%exit9</td>\n" +
        "            </tr>\n" +
        "            <tr align=\"center\">\n" +
        "                <td>10</td>\n" +
        "                <td>%wave10</td>\n" +
        "                <td>%qs10</td>\n" +
        "                <td>%exit10</td>\n" +
        "            </tr>\n" +
        "        </table>\n" +
        "    </body>\n" +
        "</html>";

    public String infoString;

    private Modder modder;
    private int currentWave = 0; // 0-9.
    private long[] waveEnterTimes = new long[10];
    private long[] waveExitTimes = new long[10];

    private int prevWorldX;
    private int prevWorldY;

    public BaStats(Modder modder) {
        this.modder = modder;
        /*long curr = System.nanoTime();
        waveEnterTimes[0] = curr;
        waveExitTimes[0] = waveEnterTimes[0] + 306 * 100000000L;

        waveEnterTimes[1] = waveExitTimes[0] + 42 * 100000000L;
        waveExitTimes[1] = waveEnterTimes[1] + 378 * 100000000L;

        waveEnterTimes[2] = waveExitTimes[1] + 24 * 100000000L;
        waveExitTimes[2] = waveEnterTimes[2] + 420 * 100000000L;

        waveEnterTimes[3] = waveExitTimes[2] + 42 * 100000000L;
        waveExitTimes[3] = waveEnterTimes[3] + 426 * 100000000L;

        waveEnterTimes[4] = waveExitTimes[3] + 60 * 100000000L;
        waveExitTimes[4] = waveEnterTimes[4] + 492 * 100000000L;

        waveEnterTimes[5] = waveExitTimes[4] + 42 * 100000000L;
        waveExitTimes[5] = waveEnterTimes[5] + 612 * 100000000L;

        waveEnterTimes[6] = waveExitTimes[5] + 42 * 100000000L;
        waveExitTimes[6] = waveEnterTimes[6] + 678 * 100000000L;

        waveEnterTimes[7] = waveExitTimes[6] + 42 * 100000000L;
        waveExitTimes[7] = waveEnterTimes[7] + 744 * 100000000L;

        waveEnterTimes[8] = waveExitTimes[7] + 42 * 100000000L;
        waveExitTimes[8] = waveEnterTimes[8] + 870 * 100000000L;

        waveEnterTimes[9] = waveExitTimes[8] + 42 * 100000000L;
        waveExitTimes[9] = waveEnterTimes[9] + 1055 * 100000000L;
        try {
            writeToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public boolean update(int worldX, int worldY) {
        if (worldX == prevWorldX && worldY == prevWorldY) return false;
        boolean newInfo = false;

        long currentTime = System.nanoTime();

        if (worldX > 6400) { // In instance.
            if (lobbyAreas[currentWave].contains(prevWorldX, prevWorldY)) {
                waveEnterTimes[currentWave] = currentTime;

                System.out.println("Started Wave: " + currentWave + ": " + currentTime);
                System.out.println(worldX + ", " +  worldY + " - " + prevWorldX + ", " + prevWorldY);
                // Exited wave 3: 124197627879800
                // Started round: 124201895075500
                // Started round: 124201898044700
            }
        } else {
            if (prevWorldX > 6400 && lobbyAreas[currentWave + 1].contains(worldX, worldY)) {
                waveExitTimes[currentWave] = currentTime;
                infoString = formatInterval(currentTime - waveEnterTimes[0]) + " | " + formatIntervalSeconds(waveExitTimes[currentWave] - waveEnterTimes[currentWave]) + "s";
                newInfo = true;

                if (currentWave == 9) {
                    currentWave = 0;
                    try {
                        writeToFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ++currentWave;
                }
                System.out.println("Exited wave " + currentWave + ": " + currentTime);
            } else if (infoString != null && currentWave == 0 && !lobbyArea.contains(worldX, worldY) && !upstairsArea.contains(worldX, worldY)) {
                System.out.println("Left ba " + currentWave + " " + worldX + ", " + worldY);
                infoString = null;
                newInfo = true;
            }
        }

        prevWorldX = worldX;
        prevWorldY = worldY;
        return newInfo;
    }

    private void writeToFile() throws Exception {
        long roundTime = waveExitTimes[9] - waveEnterTimes[0];
        String breakdown = breakdownTemplate.replaceFirst("%time", formatInterval(roundTime));
        long totalQsTime = 0;

        for (int i = 0; i < 10; ++i) {
            breakdown = breakdown.replaceFirst("%wave" + (i + 1), formatIntervalSeconds(waveExitTimes[i] - waveEnterTimes[i]));
            breakdown = breakdown.replaceFirst("%exit" + (i + 1), formatInterval(waveExitTimes[i] - waveEnterTimes[0]));
            if (i > 0) {
                long qsTime = waveEnterTimes[i] - waveExitTimes[i - 1];
                totalQsTime += qsTime;
                breakdown = breakdown.replaceFirst("%qs" + (i + 1), formatIntervalSeconds(qsTime));
            }
        }
        breakdown = breakdown.replaceFirst("%qstime", formatIntervalSeconds(totalQsTime) + "s");

        Files.write(Paths.get(breakdownFile.getAbsolutePath()), breakdown.getBytes());
    }

    private static String formatIntervalSeconds(long nanoSeconds) {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        return new DecimalFormat("0.0", dfs).format(nanoSeconds / 1000000000.0);
    }

    private static String formatInterval(long nanoSeconds) {
        long hundreds = nanoSeconds / 10000000;
        long seconds = hundreds / 100;
        long minutes = seconds / 60;
        return String.format("%02d:%02d.%02d", minutes % 60, seconds % 60, hundreds % 100);
    }
}