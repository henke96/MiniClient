package miniclient;

import javax.swing.JFrame;

public class MiniClient extends JFrame {
    public MiniClient() throws Exception {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AppletLoader loader = new AppletLoader(216, 765, 503);
        setResizable(false);
        add(loader.applet);
        pack();
        setVisible(true);
    }
    public static void main(String[] args) throws Exception {
        new MiniClient();
    }
}