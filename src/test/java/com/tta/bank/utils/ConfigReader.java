package com.tta.bank.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads config.properties. Any value can be overridden on the CLI: mvn test -Dbrowser=firefox */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConfigReader() { }

    public static String get(String key) {
        String sys = System.getProperty(key);
        return sys != null ? sys : PROPS.getProperty(key);
    }

    public static String get(String key, String def) {
        String v = get(key);
        return v == null ? def : v;
    }

    public static int getInt(String key, int def) {
        String v = get(key);
        return v == null ? def : Integer.parseInt(v.trim());
    }

    public static boolean getBool(String key, boolean def) {
        String v = get(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    public static String baseUrl() {
        String u = get("base.url");
        return u.endsWith("/") ? u : u + "/";
    }
}
