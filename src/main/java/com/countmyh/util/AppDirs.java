package com.countmyh.util;

import java.nio.file.Path;

/**
 * Resolves the app data directory in a way that works inside the macOS App Store sandbox.
 *
 * GraalVM native images call getpwuid() for System.getProperty("user.home"), which returns
 * the real home directory and bypasses the sandbox redirect. System.getenv("HOME") is
 * correctly set by macOS to the container path, so we prefer that.
 */
public final class AppDirs {

    public static final Path DATA_DIR = Path.of(home(), ".countmyhours");

    private AppDirs() {}

    private static String home() {
        String home = System.getenv("HOME");
        return (home != null && !home.isEmpty()) ? home : System.getProperty("user.home");
    }
}
