package miniclient;

import javax.swing.JFrame;

public class MiniClient extends JFrame {
    public MiniClient() throws Exception {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Settings settings = new Settings();
        setResizable(settings.resizableFrame());

        AppletLoader loader = new AppletLoader(settings.world(), settings.width(), settings.height());
        add(loader.applet);
        pack();
        setVisible(true);
    }
    public static void main(String[] args) throws Exception {
        new MiniClient();
    }
}