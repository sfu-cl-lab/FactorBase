package ca.sfu.cs.factorbase.util;

/**
 * Class to generate queries used by FactorBase.
 */
public final class QueryGenerator {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private QueryGenerator() {
    }


    /**
     * Generates a MySQL SELECT string that returns the items in tableA that are not in tableB.
     *
     * @param columnsA - a CSV of the columns to select from tableA.
     * @param columnsB - a CSV of the columns to join tableA and tableB on.
     * @param tableA - the table name to find unique rows relative to tableB.
     * @param tableB - the table name to determine what is unique in tableA.
     * @return a String that returns the rows unique to tableA relative to tableB when executed.
     */
    public static String createDifferenceQuery(String columnsA, String columnsB, String tableA, String tableB) {
        return
            "SELECT " + columnsA + " " +
            "FROM " + tableA + " " +
            "WHERE (" + columnsB + ") " +
            "NOT IN (" +
                "SELECT " + columnsB + " " +
                "FROM " + tableB +
            ")";
    }
}
