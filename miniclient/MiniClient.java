package miniclient;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;

import miniclient.BaStats;
import miniclient.Modder;

public class MiniClient extends ClassLoader implements AppletStub {
    private HashMap<String, String> parameters = new HashMap<>();
    private HashMap<String, byte[]> classes = new HashMap<>();
    public Modder modder = new Modder();
    public BaStats baStats = new BaStats();
    public JFrame frame = new JFrame("Runescape");

    public static MiniClient client;

    public MiniClient() throws Exception {
        client = this;
        frame.setSize(200, 0);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setVisible(true);

        // Jagex adds 300 to worlds in game, so 'oldschool216' is world 516, etc.
        String gameUrl = "http://oldschool216.runescape.com/";

        while (true) {
            // Read applet parameters, see http://oldschool1.runescape.com/jav_config.ws for example.
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
            } catch (SocketTimeoutException e) {
                continue;
            }

            // Load classes from gamepack.
            connection = new URL(gameUrl + parameters.get("initial_jar")).openConnection();
            connection.setConnectTimeout(300);
            connection.setReadTimeout(300);
            try (ZipInputStream gamepackIn = new ZipInputStream(connection.getInputStream())) {
                ZipEntry currentEntry;
                byte[] buffer = new byte[1024];
                while ((currentEntry = gamepackIn.getNextEntry()) != null) {
                    String name = currentEntry.getName();
                    int dotClassIndex = name.indexOf(".class");
                    if (dotClassIndex == -1) continue;
                    name = name.substring(0, dotClassIndex);

                    ByteArrayOutputStream classBytes = new ByteArrayOutputStream();
                    int numRead;
                    while ((numRead = gamepackIn.read(buffer, 0, buffer.length)) != -1) {
                        classBytes.write(buffer, 0, numRead);
                    }
                    classes.put(name, modder.processClass(name, classBytes.toByteArray()));
                }
                modder.finalize(this);
            } catch (SocketTimeoutException e) {
                continue;
            }
            break;
        }

        // Load Runescape applet.
        String initialClass = parameters.get("initial_class");
        initialClass = initialClass.substring(0, initialClass.indexOf('.'));
        Applet applet = (Applet) loadClass(initialClass).newInstance();
        applet.setStub(this);
        applet.resize(765, 503);
        applet.setPreferredSize(applet.getSize());
        applet.init();
        applet.start();
        frame.add(applet);
        frame.pack();
    }

    // Modder injects a call to this into the client.
    public static void onTick() {
        try {
            Object localPlayer = client.modder.localPlayerField.get(null);
            if (localPlayer != null) {
                int baseX = ((int) client.modder.baseXField.get(null)) * client.modder.baseXMult;
                int baseY = ((int) client.modder.baseYField.get(null)) * client.modder.baseYMult;
                int[] pathX = (int[]) client.modder.actorPathXField.get(localPlayer);
                int[] pathY = (int[]) client.modder.actorPathYField.get(localPlayer);
                int worldX = baseX + pathX[0];
                int worldY = baseY + pathY[0];
                if (client.baStats.update(worldX, worldY)) {
                    if (client.baStats.infoString != null) {
                        client.frame.setTitle("Runescape | " + client.baStats.infoString);
                    } else {
                        client.frame.setTitle("Runescape");
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(name);
        if (result != null) return result;

        byte[] classData = classes.get(name);
        if (classData != null) {
            result = defineClass(name, classData, 0, classData.length);
            if (result != null) return result;
        }
        return super.loadClass(name);
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