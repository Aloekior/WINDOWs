CREATE DATABASE IF NOT EXISTS WINDOWs;
USE WINDOWs;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT,
    user_name VARCHAR(20) UNIQUE NOT NULL,
    user_password VARCHAR(30) NOT NULL DEFAULT md5('password'),
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS sensors (
    sensor_mac VARCHAR(12) UNIQUE NOT NULL,
    sensor_token VARCHAR(8) UNIQUE NOT NULL,
    sensor_room VARCHAR(20),
    sensor_name VARCHAR(20),
    sensor_active BOOLEAN DEFAULT 0,
    PRIMARY KEY (sensor_token)
);

CREATE TABLE IF NOT EXISTS window_history (
    history_entry_id INT AUTO_INCREMENT,
    sensor_mac VARCHAR(12) NOT NULL,
    history_timestamp DATETIME NOT NULL DEFAULT now(),
    history_state BOOLEAN,
    PRIMARY KEY (history_entry_id),
    FOREIGN KEY (sensor_mac) REFERENCES sensors (sensor_mac)
);

CREATE USER 'serverDaemon'@'localhost' IDENTIFIED BY 'a8aKJFAL8%lo113ZZ&Bvmm12g_$1!';
CREATE USER 'serverCLI'@'localhost' IDENTIFIED BY '!uJmng8124n!fLffas%Po3$(axyq';


CREATE USER 'serverAdmin'@'localhost' IDENTIFIED BY 'password';
GRANT EXECUTE ON PROCEDURE WINDOWs.changeSensorState TO 'serverDaemon'@'localhost';
GRANT INSERT ON WINDOWs.sensors TO 'serverAdmin'@'localhost';
GRANT UPDATE ON WINDOWs.sensors TO 'serverAdmin'@'localhost';


DELIMITER //


/* FUNCTIONS */


DROP FUNCTION IF EXISTS checkSensorMac;
CREATE FUNCTION checkSensorMac(input_mac VARCHAR(12)) RETURNS INT
BEGIN
    IF ((SELECT COUNT(sensor_mac) FROM sensors WHERE sensor_mac = input_mac) > 0) THEN
        RETURN 1;
    END IF;
    RETURN 0;
END //

DROP FUNCTION IF EXISTS checkSensorToken;
CREATE FUNCTION checkSensorToken(input_token VARCHAR(4)) RETURNS INT
BEGIN
    IF ((SELECT COUNT(sensor_token) FROM sensors WHERE sensor_token = input_token) > 0) THEN
        RETURN 1;
    END IF;
    RETURN 0;
END //

DROP FUNCTION IF EXISTS checkSensorValid;
CREATE FUNCTION checkSensorValid (input_mac VARCHAR(12), input_token VARCHAR(4)) RETURNS INT
    BEGIN
        IF (checkSensorMac(input_mac) = 1 && checkSensorToken(input_token) = 1) THEN
            RETURN 1;
        END IF;
        RETURN 0;
    END //
    
DROP FUNCTION IF EXISTS sensorUpdateRequired;
CREATE FUNCTION sensorUpdateRequired(input_mac VARCHAR(12), input_state INT) RETURNS INT
    BEGIN
        DECLARE maxTimeStamp DATETIME;
        DECLARE currentState INT;
    
        SET maxTimeStamp = (SELECT MAX(history_timestamp) FROM window_history WHERE sensor_mac = input_mac);
        SET currentState = (SELECT history_state FROM window_history WHERE history_timestamp = maxTimeStamp);
    
        IF (currentState = input_state) THEN
            RETURN 0;
        END IF;
        RETURN 1;
    END;




/* PROCEDURES */

DROP PROCEDURE IF EXISTS addSensor;
CREATE PROCEDURE addSensor (input_mac VARCHAR(12), input_token VARCHAR(4))
    BEGIN
        INSERT INTO sensors (sensor_mac, sensor_token) VALUES (input_mac, input_token);
        SELECT 0;
    END //


DROP PROCEDURE IF EXISTS changeSensorRoom;
CREATE PROCEDURE changeSensorRoom (input_mac VARCHAR(12), input_token VARCHAR(4), input_room VARCHAR(20))
    BEGIN
        IF (checkSensorValid(input_mac,input_token)) THEN
            UPDATE sensors SET sensor_room = input_room WHERE sensor_token = input_token;
        END IF;
    END //
    
DROP PROCEDURE IF EXISTS deactivateSensor;
CREATE PROCEDURE deactivateSensor (input_mac VARCHAR(12), input_token VARCHAR(4))
    BEGIN
        IF (checkSensorValid(input_mac,input_token)) THEN
            UPDATE sensors SET sensor_room = NULL, sensor_active = 0 WHERE sensor_token = input_token;
        END IF;
    END //


DROP PROCEDURE IF EXISTS getSensorState;
CREATE PROCEDURE getSensorState (input_mac VARCHAR(12), input_token VARCHAR(4))
    BEGIN
        IF (checkSensorValid(input_mac,input_token)) THEN
            SELECT history_state FROM window_history WHERE sensor_mac = input_mac;
        END IF;
    END //


DROP PROCEDURE IF EXISTS changeSensorState;
CREATE PROCEDURE changeSensorState (input_mac VARCHAR(12), input_token VARCHAR(4), input_state BOOLEAN)
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

DROP PROCEDURE IF EXISTS checkSensorExists;
CREATE PROCEDURE checkSensorExists (input_mac VARCHAR(12))
    BEGIN
        SELECT checkSensorMac(input_mac);
    END //

DELIMITER ;
