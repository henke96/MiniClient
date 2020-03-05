package miniclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Set;

public class Settings {
    private static final File settingsFile = new File(System.getProperty("user.home") + "/MiniClient/settings.txt");
    private final Properties properties = new Properties();

    public Settings() throws Exception {
        if ((!settingsFile.getParentFile().exists() && !settingsFile.getParentFile().mkdirs()) || !settingsFile.getParentFile().isDirectory()) {
            setDefaults();
            return;
        }
        if (settingsFile.exists()) {
            try (FileInputStream in = new FileInputStream(settingsFile)) {
                properties.load(in);
            }
        }
        if (setDefaults()) {
            try (FileWriter writer = new FileWriter(settingsFile)) {
                String newLine = System.getProperty("line.separator");
                Set<String> keys = properties.stringPropertyNames();
                for (String key : keys) {
                    writer.write(key + "=" + properties.getProperty(key) + newLine);
                }
            }
        }
    }

    public int width() {
        return Integer.parseInt(properties.getProperty("width"));
    }

    public int height() {
        return Integer.parseInt(properties.getProperty("height"));
    }

    public boolean resizableFrame() {
        return Integer.parseInt(properties.getProperty("resizableFrame")) != 0;
    }

    public int world() {
        return tryParseInt(properties.getProperty("world"));
    }

    private boolean setDefaults() {
        boolean anyMissing = false;
        if (setIntIfInvalid("width", 1, Integer.MAX_VALUE, "765")) anyMissing = true;
        if (setIntIfInvalid("height", 1, Integer.MAX_VALUE, "503")) anyMissing = true;
        if (setIntIfInvalid("resizableFrame", 0, 1, "0")) anyMissing = true;
        if (setIntIfInvalid("world", 301, 1000, "516")) anyMissing = true;
        return anyMissing;
    }

    private boolean setIntIfInvalid(String key, int minValue, int maxValue, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            properties.setProperty(key, defaultValue);
            return true;
        } else {
            Integer valueInt = tryParseInt(value);
            if (valueInt == null || valueInt < minValue || valueInt > maxValue) {
                properties.setProperty(key, defaultValue);
                return true;
            }
        }
        return false;
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
