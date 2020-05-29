CREATE PROCEDURE `getColumnsInfo`(tableNames TEXT)
BEGIN
    SELECT
        column_name,
        CASE WHEN
            data_type = 'enum'
        THEN
            CONCAT("VARCHAR(", CHARACTER_MAXIMUM_LENGTH, ")")
        ELSE
            column_type
        END AS DataType
    FROM
        information_schema.columns
    WHERE
        table_schema = '@database@'
    AND
        find_in_set(table_name, tableNames);
END//