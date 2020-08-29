package miniclient;

import javax.swing.JFrame;

public class MiniClient {
    public static void main(String[] args) throws Exception {
        Settings settings = new Settings();
        if (settings.opengl()) System.setProperty("sun.java2d.opengl", "true");

        JFrame frame = new JFrame("Runescape");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(settings.resizableFrame());
        AppletLoader loader = new AppletLoader(settings.world(), settings.width(), settings.height(), settings.connectTimout());
        frame.add(loader.applet);
        frame.pack();
        frame.setVisible(true);
    }
}