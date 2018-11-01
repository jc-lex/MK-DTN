package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import java.io.InputStream;
import java.util.Scanner;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Nicolas Gramlich
 * @since 15:50:31 - 14.07.2010
 */
class SystemUtils {

    // NOTE: it worked well on a Samsung
    public static int getMaxCPUFrequencyInKHz() throws Exception {
        return readSystemFileAsInt(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
        );
    }

    private static int readSystemFileAsInt(final String pSystemFile) throws Exception {
        InputStream in;
        try {
            final Process process
                    = new ProcessBuilder("/system/bin/cat", pSystemFile).start();

            in = process.getInputStream();
            final String content = readFully(in);
            return Integer.parseInt(content);
        } catch (final Exception e) {
            throw new Exception(e);
        }
    }

    private static String readFully(final InputStream pInputStream) {
        final StringBuilder sb = new StringBuilder();
        final Scanner sc = new Scanner(pInputStream);
        while(sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        return sb.toString();
    }
}


