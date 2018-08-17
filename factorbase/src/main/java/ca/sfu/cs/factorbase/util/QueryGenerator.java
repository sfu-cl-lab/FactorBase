package ca.sfu.cs.factorbase.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to generate queries used by FactorBase.
 */
public final class QueryGenerator {

    private static final String JOIN_ON_STRING = "({0}.{2} = {1}.{2} OR {0}.{2} IS NULL AND {1}.{2} IS NULL)";

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private QueryGenerator() {
    }

    /**
     * Generate the String used to join rows between tableA and tableB.
     *
     * @param columns - a list of the columns to join tableA and tableB on.
     * @param tableA - the name of one table that will be joined with the second table.
     * @param tableB - the name of a second table, which will be joined with the first table.
     * @return a String that joins tableA and tableB on the given attributes plus NULL.
     */
    private static String constructWhereClauseJoin(List<String> columns, String tableA, String tableB) {
        ArrayList<String> conditions = new ArrayList<String>();

        for (String column : columns) {
            conditions.add(MessageFormat.format(JOIN_ON_STRING, tableA, tableB, "`" + column + "`"));
        }

        return String.join(" AND ", conditions);
    }

    /**
     * Generates a MySQL SELECT String that returns the items in tableA that are not in tableB.
     *
     * @param columnsA - a CSV of the columns to select from tableA.
     * @param columnsB - a list of the columns to join tableA and tableB on.
     * @param tableA - the table name to find unique rows relative to tableB.
     * @param tableB - the table name to determine what is unique in tableA.
     * @return a String that returns the rows unique to tableA relative to tableB when executed.
     */
    public static String createDifferenceQuery(String columnsA, List<String> columnsB, String tableA, String tableB) {
        String whereClauseJoin = constructWhereClauseJoin(columnsB, tableA, tableB);
        return
            "SELECT " + columnsA + " " +
            "FROM " + tableA + " " +
            "WHERE NOT EXISTS (" +
                "SELECT NULL " +
                "FROM " + tableB + " " +
                "WHERE " + whereClauseJoin +
            ")";
    }
}
