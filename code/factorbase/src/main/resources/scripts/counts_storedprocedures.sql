CREATE PROCEDURE `getColumnsInfo`(tableNames TEXT)
BEGIN
    SELECT
        column_name,
        CONCAT(
            data_type,
            '(',
            IFNULL(numeric_precision, character_maximum_length),
            ')'
        ) AS DataType
    FROM
        information_schema.columns
    WHERE
        table_schema = '@database@'
    AND
        find_in_set(table_name, tableNames);
END//