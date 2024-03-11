CREATE PROCEDURE `find_values`()
BEGIN
DECLARE done INT DEFAULT 0;
DECLARE tablename VARCHAR(30);
DECLARE columnname VARCHAR(30);
DECLARE cur1 CURSOR FOR 
	SELECT TABLE_NAME, COLUMN_NAME FROM AttributeColumns;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
OPEN cur1;

DROP TABLE IF EXISTS Attribute_Value;
CREATE TABLE
	Attribute_Value (
	COLUMN_NAME VARCHAR(269),
	VALUE VARCHAR(30));

LOOP1: LOOP
	FETCH cur1 INTO tablename, columnname;
    IF done = 1 THEN 
        LEAVE LOOP1; 
    END IF;
	SET @SQLQUERY = 'INSERT INTO Attribute_Value (';
	SET @SQLQUERY = CONCAT(@SQLQUERY, 'SELECT distinct "', columnname);
	SET @SQLQUERY = CONCAT(@SQLQUERY, '", ', columnname);
	SET @SQLQUERY = CONCAT(@SQLQUERY, ' FROM  @database@.', tablename, ')');

	PREPARE stmt1 FROM @SQLQUERY;
    EXECUTE stmt1;
END LOOP LOOP1;

CLOSE cur1;

END//