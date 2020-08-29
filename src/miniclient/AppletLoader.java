package miniclient;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AppletLoader extends ClassLoader implements AppletStub {
    public final Applet applet;

    private HashMap<String, byte[]> classes = new HashMap<>();
    private HashMap<String, String> parameters = new HashMap<>();
    private static final File localGamepack = new File(System.getProperty("user.home") + "/MiniClient/gamepack.jar");

    public AppletLoader(int world, int width, int height, int connectTimeout) throws Exception {
        world -= 300; // World 301 is actually world 1, etc.
        loadGamepack(world, connectTimeout);
        String initialClass = parameters.get("initial_class");
        initialClass = initialClass.substring(0, initialClass.indexOf('.'));
        applet = (Applet) loadClass(initialClass).newInstance();
        applet.setStub(this);
        applet.resize(width, height);
        applet.setPreferredSize(new Dimension(width, height));
        applet.init();
        applet.start();
    }

    private void loadGamepack(int world, int connectTimeout) throws Exception {
        URL gameUrl = new URL("http://oldschool" + world + ".runescape.com/");
        URLConnection configConnection = new URL(gameUrl + "jav_config.ws").openConnection();
        configConnection.setConnectTimeout(connectTimeout);
        readParameters(new URL(gameUrl + "jav_config.ws"), connectTimeout);

        while (true) {
            URLConnection connection = new URL(gameUrl + parameters.get("initial_jar")).openConnection();
            connection.setConnectTimeout(connectTimeout);
            try (ZipInputStream gamepackIn = new ZipInputStream(connection.getInputStream())) {
                ZipEntry currentEntry = gamepackIn.getNextEntry();
                byte[] manifestBytes = readZipEntry(gamepackIn);
                if (localGamepack.exists()) {
                    try (ZipInputStream localGamepackIn = new ZipInputStream(new FileInputStream(localGamepack))) {
                        localGamepackIn.getNextEntry();
                        if (Arrays.equals(manifestBytes, readZipEntry(localGamepackIn))) {
                            // Local gamepack is up to date.
                            loadGamepackClasses(localGamepackIn, null);
                            return;
                        }
                    }
                }
                // Need to download new gamepack.
                ZipOutputStream localGamepackOut = null;
                try {
                    if ((localGamepack.getParentFile().exists() || localGamepack.getParentFile().mkdirs()) && localGamepack.getParentFile().isDirectory()) {
                        localGamepackOut = new ZipOutputStream(new FileOutputStream(localGamepack));
                        localGamepackOut.putNextEntry(currentEntry);
                        localGamepackOut.write(manifestBytes, 0, manifestBytes.length);
                    }
                    loadGamepackClasses(gamepackIn, localGamepackOut);
                } finally {
                    if (localGamepackOut != null) localGamepackOut.close();
                }
                break;
            } catch (SocketTimeoutException e) {
                System.err.println("Timed out connecting, retrying...");
            }
        }
    }

    private final static byte[] buffer = new byte[1024];
    private byte[] readZipEntry(ZipInputStream gamepackIn) throws Exception {
        ByteArrayOutputStream classBytesOut = new ByteArrayOutputStream();
        int numRead;
        while ((numRead = gamepackIn.read(buffer, 0, buffer.length)) != -1) {
            classBytesOut.write(buffer, 0, numRead);
        }
        return classBytesOut.toByteArray();
    }

    private void loadGamepackClasses(ZipInputStream gamepackIn, ZipOutputStream localGamepackOut) throws Exception {
        ZipEntry currentEntry;
        while ((currentEntry = gamepackIn.getNextEntry()) != null) {
            String name = currentEntry.getName();
            int dotClassIndex = name.indexOf(".class");
            if (dotClassIndex == -1) continue;

            byte[] classBytes = readZipEntry(gamepackIn);
            classes.put(name.substring(0, dotClassIndex), classBytes);
            if (localGamepackOut != null) {
                localGamepackOut.putNextEntry(currentEntry);
                localGamepackOut.write(classBytes, 0, classBytes.length);
            }
        }
    }

    private void readParameters(URL url, int connectTimeout) throws Exception {
        while (true) {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(connectTimeout);
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
            } catch (SocketTimeoutException e) {
                System.err.println("Timed out connecting, retrying...");
            }
        }
    }

    @Override
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

    @Override
    public void appletResize(int width, int height) {}

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL(parameters.get("codebase"));
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public URL getDocumentBase() {
        return getCodeBase();
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
