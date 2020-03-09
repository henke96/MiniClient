package miniclient;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class MiniClient extends JFrame {
    private Modder modder = new Modder();
    public MiniClient() throws Exception {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Settings settings = new Settings();
        setResizable(settings.resizableFrame());

        AppletLoader loader = new AppletLoader(settings.world(), settings.width(), settings.height(), modder);
        add(loader.applet);
        pack();
        setVisible(true);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Object localPlayer = modder.localPlayerField.get(null);
                    if (localPlayer != null) {
                        int baseX = ((int) modder.baseXField.get(null)) * modder.baseXMult;
                        int baseY = ((int) modder.baseYField.get(null)) * modder.baseYMult;
                        int localX = ((int[]) modder.actorPathXField.get(localPlayer))[0];
                        int localY = ((int[]) modder.actorPathYField.get(localPlayer))[0];

                        int worldX = baseX + localX;
                        int worldY = baseY + localY;

                        setTitle(worldX + ", " + worldY);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1);
    }
    public static void main(String[] args) throws Exception {
        new MiniClient();
    }
}