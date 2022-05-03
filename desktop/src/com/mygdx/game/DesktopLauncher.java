package com.mygdx.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class DesktopLauncher {
    public static void main(String[] arg) {
        if (restartJVM()) {
            return;
        }
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Jetpack-Soyride");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new MyGdxGame(), config);
    }

    /**
     * Checks if user is on Mac and if the XstartOnFirstThread option has been enabled,
     * if not, it launches a new JVM and executes the same main method, settings, parameters
     * with the XstartOnFirstThread option enabled
     * Ref: https://jvm-gaming.org/t/starting-jvm-on-mac-with-xstartonfirstthread-programmatically/57547
     * @return true if on Mac and XstartOnFirstThread is disabled, else false
     */
    public static boolean restartJVM() {
        String osName = System.getProperty("os.name");

        // if not a mac return false
        if (!osName.startsWith("Mac") && !osName.startsWith("Darwin")) {
            return false;
        }

        // get current jvm process pid
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        // get environment variable on whether XstartOnFirstThread is enabled
        String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);

        // if environment variable is "1" then XstartOnFirstThread is enabled
        if (env != null && env.equals("1")) {
            return false;
        }

        // restart jvm with -XstartOnFirstThread
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        String jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        ArrayList<String> jvmArgs = new ArrayList<String>();

        jvmArgs.add(jvmPath);
        jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.addAll(inputArguments);
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
        jvmArgs.add(mainClass);

        // if you don't need console output, just enable these two lines
        // and delete bits after it. This JVM will then terminate.
        //ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
        //processBuilder.start();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
