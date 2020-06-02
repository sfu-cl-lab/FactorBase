package ca.sfu.cs.factorbase.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Class to generate queries used by FactorBase.
 */
public final class QueryGenerator {

    private static final String JOIN_ON_STRING_INCLUDE_NULL = "({0}.{2} = {1}.{2} OR {0}.{2} IS NULL AND {1}.{2} IS NULL)";
    private static final String JOIN_ON_STRING_NO_NULL = "{0}.{2} = {1}.{2}";

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
     * @param joinOnNull - True if you want to join rows where both column values are NULL; otherwise false.
     * @return a String that joins tableA and tableB on the given attributes plus NULL.
     */
    private static String constructWhereClauseJoin(List<String> columns, String tableA, String tableB, Boolean joinOnNull) {
        ArrayList<String> conditions = new ArrayList<String>();
        String joinString = JOIN_ON_STRING_NO_NULL;

        if (joinOnNull) {
            joinString = JOIN_ON_STRING_INCLUDE_NULL;
        }

        for (String column : columns) {
            conditions.add(MessageFormat.format(joinString, tableA, tableB, "`" + column + "`"));
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
        String whereClauseJoin = constructWhereClauseJoin(columnsB, tableA, tableB, true);
        return
            "SELECT " + columnsA + " " +
            "FROM " + tableA + " " +
            "WHERE NOT EXISTS (" +
                "SELECT NULL " +
                "FROM " + tableB + " " +
                "WHERE " + whereClauseJoin +
            ")";
    }

    /**
     * Generates a MySQL SELECT String that returns the items for the specified table that have a column
     * value that matches at least one of the items in the given list of Strings {@code inItems}.
     *
     * @param tableName - the table to search for matching values.
     * @param columnName - the column to match the values on.
     * @param inItems - the values for a row to be considered a match.
     * @return a String that returns the rows that have values in the column specified by {@code columnName}
     *         match any of the values in {@code inItems}.
     */
    public static String createSimpleInQuery(String tableName, String columnName, List<String> inItems) {
        StringBuilder builder = new StringBuilder("SELECT * ");
        builder.append("FROM ").append(tableName).append(" ");
        builder.append("WHERE ").append(columnName).append(" IN (");
        StringJoiner quotedCSV = new StringJoiner("\",\"", "\"", "\"");

        for (String item : inItems) {
            quotedCSV.add(item);
        }

        builder.append(quotedCSV.toString());
        builder.append(")");

        return builder.toString();
    }


    /**
     * Generates a MySQL SELECT String that returns all the rows in the table {@code table1}, with the column
     * {@code subtractionColumn} subtracted by the same column in the table {@code table2}.
     *
     * Note: The table {@code table2} should contain column value combinations (based on the columns
     *       {@code joinOnColumns}) that are a subset of the ones found in the table {@code table1}.  Any value unique
     *       to {@code table1} will be subtracted by 0.
     *
     * @param table1 - The table to have its values in the column {@code subtractionColumn} subtracted by the column
     *                 {@code subtractionColumn} in the table {@code table2}.
     * @Param table2 - The table to match rows with in table {@code table1} and subtract by the values found in the
     *                 column {@code subtractionColumn}.
     * @param subtractionColumn - The common column found in {@code table1} and {@code table2} that should be subtracted
     *                            from {@code table1}.  e.g. table1.MULT - table2.MULT.
     * @param joinOnColumns - The columns used to join the rows of tables {@code table1} and {@code table2}.
     * @return a String that will subtract the value in the common column between tables {@code table1} and
     *         {@code table2} for any rows that match based on the columns {@code joinOnColumns}.
     *
     */
    public static String createSubtractionQuery(String table1, String table2, String subtractionColumn, List<String> joinOnColumns) {
        StringBuilder builder = new StringBuilder("SELECT ");
        builder.append(table1 + "." + subtractionColumn + " - ");
        builder.append("IFNULL(" + table2 + "." + subtractionColumn + ", 0) AS " + subtractionColumn);
        StringJoiner csv = new StringJoiner(", ");

        for (String column : joinOnColumns) {
            csv.add(table1 + ".`" + column + "`");
        }

        if (csv.length() != 0) {
            builder.append(", " + csv.toString() + " ");
        }

        builder.append("FROM " + table1 + " ");
        builder.append("LEFT JOIN " + table2 + " ");
        builder.append("ON " + constructWhereClauseJoin(joinOnColumns, table1, table2, false));

        return builder.toString();
    }


    /**
     * Generates a MySQL INSERT String that inserts the given data into the specified table, which should have only one
     * column.
     *
     * @param table - the name of the table to insert the data into.
     * @param child - the child variable to insert into the table.
     * @param parents - the parent variables to insert into the table.
     * @return a String that will insert the given variables into the specified table.
     */
    public static String createSimpleExtendedInsertQuery(String table, String child, Set<String> parents) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(table);
        builder.append(" VALUES ('");
        builder.append(child);
        builder.append("')");

        StringJoiner csv = new StringJoiner("'), ('", "('", "')");
        for (String parent : parents) {
            csv.add(parent);
        }

        if (parents.size() != 0) {
            builder.append(", ");
            builder.append(csv.toString());
        }

        builder.append(";");

        return builder.toString();
    }


    /**
     * Generates a MySQL TRUNCATE String that removes all the data from the specified table.
     *
     * @param table - the name of the table to truncate.
     * @return a String that will remove all the data from the specified table.
     */
    public static String createTruncateQuery(String table) {
        StringBuilder builder = new StringBuilder("TRUNCATE ");
        builder.append(table);
        builder.append(";");

        return builder.toString();
    }
}