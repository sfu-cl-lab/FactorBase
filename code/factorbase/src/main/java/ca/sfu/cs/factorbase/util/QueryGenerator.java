package ca.sfu.cs.factorbase.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Class to generate queries used by FactorBase.
 */
public final class QueryGenerator {

    private static final String JOIN_ON_STRING_INCLUDE_NULL = "({0}.{2} = {1}.{2} OR {0}.{2} IS NULL AND {1}.{2} IS NULL)";
    private static final String JOIN_ON_STRING_NO_NULL = "{0}.{2} = {1}.{2}";
    private static final StringBuilder builder = new StringBuilder();
    private static final StringBuilder escapedBuilder = new StringBuilder();

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
        StringJoiner conditions = new StringJoiner(" AND ");
        String joinString = JOIN_ON_STRING_NO_NULL;

        if (joinOnNull) {
            joinString = JOIN_ON_STRING_INCLUDE_NULL;
        }

        for (String column : columns) {
            escapedBuilder.setLength(0);
            escapedBuilder.append("`").append(column).append("`");
            conditions.add(
                MessageFormat.format(
                    joinString,
                    tableA,
                    tableB,
                    escapedBuilder.toString()
                )
            );
        }

        return conditions.toString();
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
        builder.setLength(0);
        builder.append("SELECT ").append(columnsA).append(" ");
        builder.append("FROM ").append(tableA).append(" ");
        builder.append("WHERE NOT EXISTS (");
        builder.append("SELECT NULL ");
        builder.append("FROM ").append(tableB).append(" ");
        builder.append("WHERE ").append(whereClauseJoin);
        builder.append(")");

        return builder.toString();
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
        builder.setLength(0);
        builder.append("SELECT * ");
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
        builder.setLength(0);
        builder.append("SELECT ");
        builder.append(table1).append(".").append(subtractionColumn);
        builder.append(" - ");
        builder.append("IFNULL(");
        builder.append(table2).append(".").append(subtractionColumn).append(", ");
        builder.append("0");
        builder.append(") AS ").append(subtractionColumn);
        StringJoiner csv = new StringJoiner(", ");

        for (String column : joinOnColumns) {
            escapedBuilder.setLength(0);
            escapedBuilder.append(table1).append(".`").append(column).append("`");
            csv.add(escapedBuilder.toString());
        }

        if (csv.length() != 0) {
            builder.append(", ").append(csv.toString()).append(" ");
        }

        builder.append("FROM ").append(table1).append(" ");
        builder.append("LEFT JOIN ").append(table2).append(" ");
        builder.append("ON ").append(constructWhereClauseJoin(joinOnColumns, table1, table2, false));

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
        builder.setLength(0);
        builder.append("INSERT INTO ");
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
        builder.setLength(0);
        builder.append("TRUNCATE ");
        builder.append(table);
        builder.append(";");

        return builder.toString();
    }


    /**
     * Generates a MySQL SELECT String that extracts "Entries" from the MetaQueries table based on the provided
     * criteria.
     *
     * @param latticePoint - the point in the lattice that the "Entries" value belongs to.
     * @param tableType - the type of table the query we are trying to extract generates, e.g. STAR.
     * @param clause - the type of query we are trying to extract the "Entries" for, e.g. SELECT.
     * @param selectDistinct - true if the DISTINCT keyword should be used; otherwise false.
     * @return a String that extracts "Entries" from the MetaQueries table based on the provided criteria.
     */
    public static String createMetaQueriesExtractionQuery(
        String latticePoint,
        String tableType,
        String clause,
        boolean selectDistinct
    ) {
        builder.setLength(0);
        if (selectDistinct) {
            builder.append("SELECT DISTINCT Entries ");
        } else {
            builder.append("SELECT Entries ");
        }
        builder.append("FROM ").append("MetaQueries ");
        builder.append("WHERE ").append("Lattice_Point = '").append(latticePoint).append("' ");
        builder.append("AND ").append("TableType = '").append(tableType).append("' ");
        builder.append("AND ").append("ClauseType = '").append(clause).append("';");

        return builder.toString();
    }
}