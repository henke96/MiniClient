import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import javax.swing.JFrame;

public class MiniClient implements AppletStub {
    private HashMap<String, String> parameters = new HashMap<>();

    public MiniClient() throws Exception {
        // Jagex adds 300 to worlds in game, so 'oldschool216' is world 516, etc.
        String gameUrl = "http://oldschool2126.runescape.com/";

        // Create window.
        JFrame frame = new JFrame("Runescape");
        frame.setSize(200, 0);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setVisible(true);

        // Read applet parameters, see http://oldschool1.runescape.com/jav_config.ws for example.
        while (true) {
            URLConnection connection = new URL(gameUrl + "jav_config.ws").openConnection();
            connection.setConnectTimeout(300);
            connection.setReadTimeout(300);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int eqIndex = line.indexOf('=');
                    if (eqIndex == -1) continue;

                    String name = line.substring(0, eqIndex);
                    if (!name.equals("msg")) {
                        if (name.equals("param")) {
                            int secondEqIndex = line.indexOf('=', eqIndex + 2);
                            parameters.put(line.substring(eqIndex + 1, secondEqIndex), line.substring(secondEqIndex + 1));
                        } else {
                            parameters.put(name, line.substring(eqIndex + 1));
                        }
                    }
                }
                break;
            } catch (SocketTimeoutException e) {}
        }

        // Load Runescape applet.
        URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(gameUrl + parameters.get("initial_jar")) });
        String initialClass = parameters.get("initial_class");
        initialClass = initialClass.substring(0, initialClass.indexOf('.'));
        Applet applet = (Applet) classLoader.loadClass(initialClass).newInstance();
        applet.setStub(this);
        applet.resize(765, 503);
        applet.setPreferredSize(applet.getSize());
        applet.init();
        applet.start();
        frame.add(applet);
        frame.pack();
    }

    public boolean isActive() { return true; }

    public URL getDocumentBase() { return getCodeBase(); }

    public URL getCodeBase() {
        try {
            return new URL(parameters.get("codebase"));
        } catch (Throwable e) {
            return null;
        }
    }

    public String getParameter(String name) { return parameters.get(name); }

    public AppletContext getAppletContext() { return null; }

    public void appletResize(int width, int height) {}

    public static void main(String[] args) {
        try {
            System.setProperty("sun.java2d.opengl", "true");
            new MiniClient();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}