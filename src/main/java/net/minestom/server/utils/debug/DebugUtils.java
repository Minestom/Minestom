package net.minestom.server.utils.debug;

import net.minestom.server.utils.PropertyUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class useful for debugging purpose.
 */
@ApiStatus.Internal
public final class DebugUtils {
    public static boolean INSIDE_TEST = PropertyUtils.getBoolean("minestom.inside-test", false);

    private final static Logger LOGGER = LoggerFactory.getLogger(DebugUtils.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Prints the current thread stack trace elements.
     */
    public static synchronized void printStackTrace() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("START STACKTRACE");
        stringBuilder.append(LINE_SEPARATOR);

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
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

}
