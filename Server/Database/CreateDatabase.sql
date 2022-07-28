CREATE DATABASE IF NOT EXISTS WINDOWs;
USE WINDOWs;

CREATE TABLE IF NOT EXISTS sensors (
    sensor_mac VARCHAR(17) UNIQUE NOT NULL,
    sensor_token VARCHAR(36) UNIQUE NOT NULL,
    sensor_room VARCHAR(20),
    sensor_room_window VARCHAR(10) UNIQUE,
    sensor_active BOOLEAN DEFAULT 0,
    sensor_current_state BOOLEAN DEFAULT 0,
    PRIMARY KEY (sensor_mac)
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
GRANT EXECUTE ON PROCEDURE WINDOWs.createRemoveUser TO 'serverAdmin';
GRANT EXECUTE ON PROCEDURE WINDOWs.getRoomState TO 'serverAdmin';

/* TODO: create hook for serverAdmin during installation */


CREATE ROLE 'windowsUser';
GRANT EXECUTE ON PROCEDURE WINDOWs.getRoomState TO 'windowsUser';

CREATE USER 'windowsadmin' IDENTIFIED BY 'password';
GRANT 'serverAdmin' TO 'windowsadmin';

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
                UPDATE sensors SET sensor_current_state = input_state WHERE sensor_mac = input_mac;
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

DROP PROCEDURE IF EXISTS changeSensorWindow;
CREATE PROCEDURE changeSensorWindow (input_mac VARCHAR(17), input_room_window VARCHAR(10))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        UPDATE sensors SET sensor_room_window = input_room_window WHERE sensor_mac = input_mac;
    END IF;
END //

DROP PROCEDURE IF EXISTS getRoomState;
CREATE PROCEDURE getRoomState (input_room VARCHAR(20))
    BEGIN
        IF (checkSensorRoom(input_room)) THEN
            SELECT h.history_state FROM window_history h LEFT JOIN sensors s ON h.sensor_mac = s.sensor_mac WHERE s.sensor_room = input_room;
        END IF;
    END //

DROP PROCEDURE IF EXISTS getStates;
CREATE PROCEDURE getStates()
    BEGIN 
       SELECT sensor_room, sensor_current_state FROM sensors; 
    END //

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
        UPDATE sensors SET sensor_room = NULL, sensor_room_window = NULL, sensor_active = 0 WHERE sensor_room = input_room;
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

DROP PROCEDURE IF EXISTS createRemoveUser;
CREATE PROCEDURE createRemoveUser(IN input_username VARCHAR(32), IN input_password VARCHAR(200))
    BEGIN
        IF (SELECT COUNT(User) FROM mysql.user WHERE User = input_username > 0) THEN
            SELECT -1;
            IF (input_password = '') THEN
                SET @`username` := CONCAT('\'', input_username, '\'');
                DROP USER @`username`;
                SELECT 0;
            END IF;
        ELSE
            SET @`username` := CONCAT('\'', input_username, '\''),
                @`password` := CONCAT('\'', input_password, '\'');
            SET @`create` := CONCAT('CREATE USER ', @`username`, ' IDENTIFIED BY ', @`password`);
            PREPARE `createUser` FROM @`create`;
            EXECUTE `createUser`;
            DEALLOCATE PREPARE `createUser`;
            SET @`grant` := CONCAT('GRANT \'windowsUser\' TO ', @`username`);
            PREPARE `grantPrivileges` FROM @`grant`;
            EXECUTE `grantPrivileges`;
            DEALLOCATE PREPARE `grantPrivileges`;
            FLUSH PRIVILEGES;
            SELECT 0;
        END IF;
    END //

DELIMITER ;
