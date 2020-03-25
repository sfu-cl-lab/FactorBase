package ca.sfu.cs.factorbase.util;

import java.util.logging.Logger;

/**
 * Class to help log information for a FactorBase run.
 */
public final class RuntimeLogger {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private RuntimeLogger() {
    }


    /**
     * Helper method to write out the run times in a consistent format.
     * @param logger - the logger to write the runtime to.
     * @param stage - the part of the FactorBase program that was run.
     * @param start - the start time for the given stage (ms).
     * @param end - the end time for the given stage (ms).
     */
    public static void logRunTime(Logger logger, String stage, long start, long end) {
        logger.info("Runtime[" + stage + "]: " + String.valueOf(end - start) + "ms.");
    }
}