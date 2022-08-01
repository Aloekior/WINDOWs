CREATE DATABASE IF NOT EXISTS WINDOWs;
USE WINDOWs;

CREATE TABLE IF NOT EXISTS sensors
(
    sensor_mac           VARCHAR(17) UNIQUE NOT NULL,
    sensor_token         VARCHAR(36) UNIQUE NOT NULL,
    sensor_room          VARCHAR(20),
    sensor_window        VARCHAR(20) UNIQUE,
    sensor_active        BOOLEAN DEFAULT 0,
    sensor_current_state BOOLEAN DEFAULT 0,
    PRIMARY KEY (sensor_mac)
);

CREATE TABLE IF NOT EXISTS window_history
(
    history_entry_id  INT AUTO_INCREMENT,
    sensor_mac        VARCHAR(17) NOT NULL,
    history_timestamp DATETIME    NOT NULL DEFAULT now(),
    history_state     BOOLEAN,
    PRIMARY KEY (history_entry_id),
    FOREIGN KEY (sensor_mac) REFERENCES sensors (sensor_mac)
);

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
CREATE FUNCTION checkSensorValid(input_mac VARCHAR(17), input_token VARCHAR(36)) RETURNS INT
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
CREATE PROCEDURE updateSensorState(input_mac VARCHAR(17), input_token VARCHAR(36), input_state BOOLEAN)
BEGIN
    IF (checkSensorValid(input_mac, input_token)) THEN
        IF ((SELECT sensor_active FROM sensors WHERE (sensor_mac = input_mac AND sensor_token = input_token)) = 0) THEN
            UPDATE sensors SET sensor_active = TRUE WHERE sensor_mac = input_mac;
        END IF;
        IF (sensorUpdateRequired(input_mac, input_state) = 1) THEN
            INSERT INTO window_history (sensor_mac, history_state) VALUES (input_mac, input_state);
            UPDATE sensors SET sensor_current_state = input_state WHERE sensor_mac = input_mac;
        END IF;
        SELECT 0;
    ELSE
        SELECT -1;
    END IF;
END //

DROP PROCEDURE IF EXISTS addSensor;
CREATE PROCEDURE addSensor(input_mac VARCHAR(17), input_token VARCHAR(36))
BEGIN
    INSERT INTO sensors (sensor_mac, sensor_token) VALUES (input_mac, input_token);
    SELECT 0;
END //

DROP PROCEDURE IF EXISTS changeSensorRoom;
CREATE PROCEDURE changeSensorRoom(input_mac VARCHAR(17), input_room VARCHAR(20))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        UPDATE sensors SET sensor_room = input_room WHERE sensor_mac = input_mac;
        SELECT 0;
    END IF;
    SELECT -1;
END //

DROP PROCEDURE IF EXISTS changeSensorWindow;
CREATE PROCEDURE changeSensorWindow(input_mac VARCHAR(17), input_window VARCHAR(20))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        UPDATE sensors SET sensor_window = input_window WHERE sensor_mac = input_mac;
        SELECT 0;
    END IF;
    SELECT -1;
END //

DROP PROCEDURE IF EXISTS getSensorStates;
CREATE PROCEDURE getSensorStates(input_room VARCHAR(20))
BEGIN
    IF (input_room = '') THEN
        SELECT sensor_room, sensor_window, sensor_current_state FROM sensors WHERE sensor_active = 1;
    ELSE
        IF (checkSensorRoom(input_room)) THEN
            SELECT sensor_window, sensor_current_state FROM sensors WHERE sensor_room = input_room AND sensor_active = 1;
        ELSE
            SELECT 'Invalid room selected', '';
        END IF;
    END IF;
END //

DROP PROCEDURE IF EXISTS getSensorHistory;
CREATE PROCEDURE getSensorHistory()
BEGIN
    SELECT s.sensor_room, s.sensor_window, h.history_timestamp, h.history_state
    FROM window_history h
             LEFT JOIN sensors s ON s.sensor_mac = h.sensor_mac
    LIMIT 50;
END //

/* Multi-Functional-Procedures */

DROP PROCEDURE IF EXISTS getAllSensors;
CREATE PROCEDURE getAllSensors()
    BEGIN 
        SELECT * FROM sensors;
    END //

DROP PROCEDURE IF EXISTS deactivateSensorByMac;
CREATE PROCEDURE deactivateSensorByMac(input_mac VARCHAR(17))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        UPDATE sensors SET sensor_room = NULL, sensor_active = 0 WHERE sensor_mac = input_mac;
        SELECT 0;
    END IF;
    SELECT -1;
END //

DROP PROCEDURE IF EXISTS deactivateSensorsByRoom;
CREATE PROCEDURE deactivateSensorsByRoom(input_room VARCHAR(20))
BEGIN
    IF (checkSensorRoom(input_room)) THEN
        UPDATE sensors SET sensor_room = NULL, sensor_window = NULL, sensor_active = 0 WHERE sensor_room = input_room;
        SELECT 0;
    END IF;
    SELECT -1;
END //

DROP PROCEDURE IF EXISTS checkTokenExists;
CREATE PROCEDURE checkTokenExists(input_token VARCHAR(36))
BEGIN
    IF (checkSensorToken(input_token)) THEN
        SELECT 1;
    ELSE
        SELECT 0;
    END IF;
END //

DROP PROCEDURE IF EXISTS checkSensorExists;
CREATE PROCEDURE checkSensorExists(input_mac VARCHAR(17))
BEGIN
    IF (checkSensorMac(input_mac)) THEN
        SELECT sensor_token FROM sensors WHERE sensor_mac = input_mac;
    ELSE
        SELECT '';
    END IF;
END //

DROP PROCEDURE IF EXISTS createRemoveUser;
CREATE PROCEDURE createRemoveUser(IN input_username VARCHAR(32), IN input_password VARCHAR(200), isAdmin BOOLEAN)
BEGIN
    SET @`username` := CONCAT('\'', input_username, '\''),
        @`password` := CONCAT('\'', input_password, '\'');
    IF (SELECT COUNT(User) FROM mysql.user WHERE User = input_username > 0) THEN
        SELECT -1;
        IF (input_password = '') THEN
            SET @`dropUser` := CONCAT('DROP USER ', @`username`);
            PREPARE `dropUser` FROM @`dropUser`;
            EXECUTE `dropUser`;
            DEALLOCATE PREPARE `dropUser`;
            SELECT 0;
        END IF;
    ELSE
        SET @`create` := CONCAT('CREATE USER ', @`username`, ' IDENTIFIED BY ', @`password`);
        PREPARE `createUser` FROM @`create`;
        EXECUTE `createUser`;
        DEALLOCATE PREPARE `createUser`;

        IF (isAdmin) THEN
            SET @`role` := 'windowsadmin';
        ELSE
            SET @`role` := 'windowsuser';
        END IF;

        SET @`grant` := CONCAT('GRANT ', @`role`, ' TO ', @`username`);
        PREPARE `grantPrivileges` FROM @`grant`;
        EXECUTE `grantPrivileges`;
        DEALLOCATE PREPARE `grantPrivileges`;

        SET @`defaultRole` := CONCAT('SET DEFAULT ROLE ', @`role`, ' FOR ', @`username`);
        PREPARE `setDefaultRole` FROM @`defaultRole`;
        EXECUTE `setDefaultRole`;
        DEALLOCATE PREPARE `setDefaultRole`;

        FLUSH PRIVILEGES;
        SELECT 0;
    END IF;
END //

DELIMITER ;


/* TODO: create hook for serverListener password during installation */
DROP USER IF EXISTS 'serverListener';
CREATE USER 'serverListener' IDENTIFIED BY 'a8aKJFAL8%lo113ZZ&Bvm12g_$1!';
GRANT EXECUTE ON PROCEDURE WINDOWs.updateSensorState TO 'serverListener';

DROP ROLE IF EXISTS windowsuser;
CREATE ROLE windowsuser;
GRANT EXECUTE ON PROCEDURE WINDOWs.getSensorStates TO windowsuser;
GRANT EXECUTE ON PROCEDURE WINDOWs.getSensorHistory TO windowsuser;

DROP ROLE IF EXISTS windowsadmin;
CREATE ROLE windowsadmin;
GRANT windowsuser TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.addSensor TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.changeSensorRoom TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.changeSensorWindow TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.getAllSensors TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.deactivateSensorByMac TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.deactivateSensorsByRoom TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.checkTokenExists TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.checkSensorExists TO windowsadmin;
GRANT EXECUTE ON PROCEDURE WINDOWs.createRemoveUser TO windowsadmin;


/* TODO: create hook for windowsadmin during installation */
/*CREATE USER 'windowsadmin' IDENTIFIED BY 'password';
GRANT 'serverAdmin' TO 'windowsadmin'; */
