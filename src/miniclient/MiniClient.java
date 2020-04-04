package miniclient;

import javax.swing.JFrame;

public class MiniClient extends JFrame {
    private static final String baseTitle = "Runescape";

    public final Modder modder = new Modder();
    private BaStats baStats = new BaStats(modder);

    public MiniClient() throws Exception {
        super(baseTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Settings settings = new Settings();
        setResizable(settings.resizableFrame());

        AppletLoader loader = new AppletLoader(settings.world(), settings.width(), settings.height(), modder);
        add(loader.applet);
        pack();
        setVisible(true);

        // Start main loop.
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
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
                                setTitle(baseTitle + " | " + baStats.infoString);
                                } else {
                                    setTitle(baseTitle);
                                }
                            }
                        }
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    public static void main(String[] args) throws Exception {
        new MiniClient();
    }
}