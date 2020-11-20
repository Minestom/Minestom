package net.minestom.server.utils.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class useful for debugging purpose.
 */
public final class DebugUtils {

    public final static Logger LOGGER = LoggerFactory.getLogger(DebugUtils.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int OFFSET = 2; // Used to do not show DebugUtils in the stack trace

    /**
     * Prints the current thread stack trace elements.
     *
     * @param maxLine the maximum number of stack trace element
     */
    public static synchronized void printStackTrace(int maxLine) {
        maxLine += OFFSET;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("START STACKTRACE");
        stringBuilder.append(LINE_SEPARATOR);

        for (int i = OFFSET; i < maxLine; i++) {
            if (i >= elements.length)
                break;

            final StackTraceElement element = elements[i];
            final String line = element.getClassName() + "." + element.getMethodName() + " (line:" + element.getLineNumber() + ")";
            stringBuilder.append(line);
            stringBuilder.append(LINE_SEPARATOR);
        }

        stringBuilder.append("END STACKTRACE");

        LOGGER.info(stringBuilder.toString());
    }

    /**
     * Prints the current thread stack trace elements.
     */
    public static synchronized void printStackTrace() {
        printStackTrace(Integer.MAX_VALUE);
    }

}
