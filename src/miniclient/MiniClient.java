package miniclient;

import javax.swing.JFrame;

public class MiniClient {
    private static final String baseTitle = "Runescape";

    public static final Modder modder = new Modder();
    private static final BaStats baStats = new BaStats(modder);
    private static final JFrame frame = new JFrame(baseTitle);

    // Modder injects a call to this into the client.
    public static void onTick() {
        try {
            Object localPlayer = modder.localPlayerField.get(null);
            if (localPlayer != null) {
                int baseX = ((int) modder.baseXField.get(null)) * modder.baseXMult;
                int baseY = ((int) modder.baseYField.get(null)) * modder.baseYMult;
                int[] pathX = (int[]) modder.actorPathXField.get(localPlayer);
                int[] pathY = (int[]) modder.actorPathYField.get(localPlayer);
                int worldX = baseX + pathX[0];
                int worldY = baseY + pathY[0];
                if (baStats.update(worldX, worldY)) {
                    if (baStats.infoString != null) {
                    frame.setTitle(baseTitle + " | " + baStats.infoString);
                    } else {
                        frame.setTitle(baseTitle);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Settings settings = new Settings();
        frame.setResizable(settings.resizableFrame());

        AppletLoader loader = new AppletLoader(settings.world(), settings.width(), settings.height(), modder);
        frame.add(loader.applet);
        frame.pack();
        frame.setVisible(true);
    }
}