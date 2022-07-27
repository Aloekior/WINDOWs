CREATE DATABASE IF NOT EXISTS WINDOWs;
USE WINDOWs;

CREATE TABLE IF NOT EXISTS sensors (
    sensor_mac VARCHAR(17) UNIQUE NOT NULL,
    sensor_token VARCHAR(36) UNIQUE NOT NULL,
    sensor_room VARCHAR(20),
    sensor_active BOOLEAN DEFAULT 0,
    PRIMARY KEY (sensor_token)
);

CREATE TABLE IF NOT EXISTS window_history (
    history_entry_id INT AUTO_INCREMENT,
    sensor_mac VARCHAR(17) NOT NULL,
    history_timestamp DATETIME NOT NULL DEFAULT now(),
    history_state BOOLEAN,
    PRIMARY KEY (history_entry_id),
    FOREIGN KEY (sensor_mac) REFERENCES sensors (sensor_mac)
);


/* TODO: create hook for serverListener password during installation */
CREATE USER 'serverListener' IDENTIFIED BY 'a8aKJFAL8%lo113ZZ&Bvm12g_$1!';
GRANT EXECUTE ON PROCEDURE WINDOWs.updateSensorState TO 'serverListener';


CREATE ROLE 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.addSensor TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.changeSensorRoom TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.deactivateSensorByMac TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.deactivateSensorsByRoom TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.checkTokenExists TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.checkSensorExists TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.addReadOnlyUser TO 'serverAdmin';

/* TODO: create hook for serverAdmin during installation */


CREATE ROLE 'user';
GRANT EXECUTE ON PROCEDURE WINDOWs.getRoomState TO 'user';


DELIMITER //

/* FUNCTIONS */

DROP FUNCTION IF EXISTS checkSensorMac;
CREATE FUNCTION checkSensorMac(input_mac VARCHAR(17)) RETURNS INT
BEGIN
    IF ((SELECT COUNT(sensor_mac) FROM sensors WHERE sensor_mac = input_mac) > 0) THEN
        RETURN 1;
    END IF;
    RETURN 0;
END //

DROP FUNCTION IF EXISTS checkSensorRoom;
CREATE FUNCTION checkSensorRoom(input_room VARCHAR(20)) RETURNS INT
BEGIN
    IF ((SELECT COUNT(sensor_room) FROM sensors WHERE sensor_room = input_room) > 0) THEN
        RETURN 1;
    END IF;
    RETURN 0;
END //

DROP FUNCTION IF EXISTS checkSensorToken;
CREATE FUNCTION checkSensorToken(input_token VARCHAR(36)) RETURNS INT
BEGIN
    IF ((SELECT COUNT(sensor_token) FROM sensors WHERE sensor_token = input_token) > 0) THEN
        RETURN 1;
    END IF;
    RETURN 0;
END //

DROP FUNCTION IF EXISTS checkSensorValid;
CREATE FUNCTION checkSensorValid (input_mac VARCHAR(17), input_token VARCHAR(36)) RETURNS INT
    BEGIN
        IF (checkSensorMac(input_mac) = 1 && checkSensorToken(input_token) = 1) THEN
            RETURN 1;
        END IF;
        RETURN 0;
    END //
    
DROP FUNCTION IF EXISTS sensorUpdateRequired;
CREATE FUNCTION sensorUpdateRequired(input_mac VARCHAR(17), input_state INT) RETURNS INT
    BEGIN
        DECLARE maxTimeStamp DATETIME;
        DECLARE currentState INT;
    
        SET maxTimeStamp = (SELECT MAX(history_timestamp) FROM window_history WHERE sensor_mac = input_mac);
        SET currentState = (SELECT history_state FROM window_history WHERE history_timestamp = maxTimeStamp);
    
        IF (currentState = input_state) THEN
            RETURN 0;
        END IF;
        RETURN 1;
    END //

DROP FUNCTION IF EXISTS checkDatabaseUserExists;
CREATE FUNCTION checkDatabaseUserExists(input_name VARCHAR(32)) RETURNS INT
    BEGIN 
        IF ((SELECT COUNT(User) FROM mysql.user WHERE User = input_name) > 0) THEN
            RETURN 1;
        END IF;
        RETURN 0;
    END //


/* PROCEDURES */

DROP PROCEDURE IF EXISTS updateSensorState;
CREATE PROCEDURE updateSensorState (input_mac VARCHAR(17), input_token VARCHAR(36), input_state BOOLEAN)
    BEGIN
        IF (checkSensorValid(input_mac, input_token)) THEN
            IF ((SELECT sensor_active FROM sensors WHERE (sensor_mac = input_mac AND sensor_token = input_token)) = 0) THEN
                UPDATE sensors SET sensor_active = TRUE WHERE sensor_mac = input_mac;
            END IF;
            IF (sensorUpdateRequired(input_mac,input_state) = 1) THEN
                INSERT INTO window_history (sensor_mac, history_state) VALUES (input_mac, input_state);
            END IF;
            SELECT 0;
        ELSE
            SELECT -1;
        END IF;
    END //

DROP PROCEDURE IF EXISTS addSensor;
CREATE PROCEDURE addSensor (input_mac VARCHAR(17), input_token VARCHAR(36))
    BEGIN
        INSERT INTO sensors (sensor_mac, sensor_token) VALUES (input_mac, input_token);
        SELECT 0;
    END //

DROP PROCEDURE IF EXISTS changeSensorRoom;
CREATE PROCEDURE changeSensorRoom (input_mac VARCHAR(17), input_room VARCHAR(20))
    BEGIN
        IF (checkSensorMac(input_mac)) THEN
            UPDATE sensors SET sensor_room = input_room WHERE sensor_mac = input_mac;
        END IF;
    END //

DROP PROCEDURE IF EXISTS getRoomState;
CREATE PROCEDURE getRoomState (input_room VARCHAR(20))
    BEGIN
        IF (checkSensorRoom(input_room)) THEN
            SELECT h.history_state FROM window_history h LEFT JOIN sensors s ON h.sensor_mac = s.sensor_mac WHERE s.sensor_room = input_room;
        END IF;
    END //

/*
DROP PROCEDURE IF EXISTS addReadOnlyUser;
CREATE PROCEDURE addReadOnlyUser(input_username VARCHAR(32), input_password VARCHAR(50))
    BEGIN
        IF (checkDatabaseUserExists(@username)) THEN
            SELECT -1;
        ELSE
            CREATE USER input_username IDENTIFIED BY input_password;
            GRANT 'user' TO input_username;
        END IF;
    END //
*/

/*
DROP PROCEDURE IF EXISTS `add_User`;
CREATE PROCEDURE `add_User`(IN `p_Name` VARCHAR(45), IN `p_Passw` VARCHAR(200))
BEGIN
    DECLARE `_HOST` CHAR(14) DEFAULT '@\'localhost\'';
    SET `p_Name` := CONCAT('\'', REPLACE(TRIM(`p_Name`), CHAR(39), CONCAT(CHAR(92), CHAR(39))), '\''),
        `p_Passw` := CONCAT('\'', REPLACE(`p_Passw`, CHAR(39), CONCAT(CHAR(92), CHAR(39))), '\'');
    SET @`sql` := CONCAT('CREATE USER ', `p_Name`, `_HOST`, ' IDENTIFIED BY ', `p_Passw`);
    PREPARE `stmt` FROM @`sql`;
    EXECUTE `stmt`;
    SET @`sql` := CONCAT('GRANT ALL PRIVILEGES ON *.* TO ', `p_Name`, `_HOST`);
    PREPARE `stmt` FROM @`sql`;
    EXECUTE `stmt`;
    DEALLOCATE PREPARE `stmt`;
    FLUSH PRIVILEGES;
END //

 */



/* Multi-Functional-Procedures */

DROP PROCEDURE IF EXISTS deactivateSensorByMac;
CREATE PROCEDURE deactivateSensorByMac (input_mac VARCHAR(17))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        UPDATE sensors SET sensor_room = NULL, sensor_active = 0 WHERE sensor_mac = input_mac;
    END IF;
END //

DROP PROCEDURE IF EXISTS deactivateSensorsByRoom;
CREATE PROCEDURE deactivateSensorsByRoom (input_room VARCHAR(20))
BEGIN
    IF (checkSensorRoom(input_room)) THEN
        UPDATE sensors SET sensor_room = NULL, sensor_active = 0 WHERE sensor_room = input_room;
    END IF;
END //

DROP PROCEDURE IF EXISTS checkTokenExists;
CREATE PROCEDURE checkTokenExists (input_token VARCHAR(36))
    BEGIN 
        IF (checkSensorToken(input_token)) THEN
           SELECT 1;
        ELSE
            SELECT 0;
        END IF;
    END //

DROP PROCEDURE IF EXISTS checkSensorExists;
CREATE PROCEDURE checkSensorExists (input_mac VARCHAR(17))
    BEGIN
        IF (checkSensorMac(input_mac)) THEN
            SELECT sensor_token FROM sensors WHERE sensor_mac = input_mac;
        ELSE
            SELECT '';
        END IF;
    END //

DELIMITER ;
