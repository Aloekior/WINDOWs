CREATE DATABASE WINDOWs;
USE WINDOWs;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT,
    user_name VARCHAR(20) UNIQUE NOT NULL,
    user_password VARCHAR(30) NOT NULL DEFAULT md5('password'),
    PRIMARY KEY (user_id)
);

CREATE TABLE sensors (
    sensor_mac VARCHAR(12) UNIQUE NOT NULL,
    sensor_token VARCHAR(20) UNIQUE NOT NULL,
    sensor_room VARCHAR(20),
    sensor_name VARCHAR(20),
    sensor_inUse BOOLEAN DEFAULT 0,
    PRIMARY KEY (sensor_token)
);

CREATE TABLE window_history (
    history_entry_id INT AUTO_INCREMENT,
    sensor_token VARCHAR(20) NOT NULL,
    history_timestamp DATETIME NOT NULL DEFAULT now(),
    history_state BOOLEAN,
    PRIMARY KEY (history_entry_id),
    FOREIGN KEY (sensor_token) REFERENCES sensors (sensor_token)
);

CREATE USER 'serverAdmin'@'localhost' IDENTIFIED BY 'password';
GRANT EXECUTE ON PROCEDURE WINDOWs.addNewInactiveSensor TO 'serverAdmin'@'localhost';
GRANT INSERT ON WINDOWs.sensors TO 'serverAdmin'@'localhost';
GRANT UPDATE ON WINDOWs.sensors TO 'serverAdmin'@'localhost';

DELIMITER //
CREATE PROCEDURE addNewInactiveSensor (input_idToken VARCHAR(20), input_name VARCHAR(20))
    BEGIN 
        INSERT INTO sensors (sensor_token, sensor_name) VALUES (input_idToken, input_name);
    end //
CREATE PROCEDURE addNewActiveSensor (input_idToken VARCHAR(20), input_room VARCHAR(20), input_name VARCHAR(20))
    BEGIN 
        INSERT INTO sensors (sensor_token, sensor_room, sensor_name) VALUES (input_idToken, input_room, input_name);
    end //
CREATE PROCEDURE activateSensorByToken (input_token VARCHAR(20), input_room VARCHAR(20))
    BEGIN 
        UPDATE sensors SET sensor_room = input_room, sensor_inUse = 1 WHERE sensor_token = input_token;
    end //
CREATE PROCEDURE changeSensorRoom (input_token VARCHAR(20), input_room VARCHAR(20))
    BEGIN 
        UPDATE sensors SET sensor_room = input_room WHERE sensor_token = input_token;
    end //
CREATE PROCEDURE deactivateSensorByToken (input_token VARCHAR(20))
    BEGIN 
        UPDATE sensors SET sensor_room = NULL, sensor_inUse = 0 WHERE sensor_token = input_token;
    end //
CREATE PROCEDURE getSensorState (input_idToken VARCHAR(20))
    BEGIN
        SELECT history_state FROM window_history WHERE sensor_token = input_idToken;
    end //
CREATE PROCEDURE changeSensorState (input_idToken VARCHAR(12), input_state BOOLEAN)
    BEGIN 
        INSERT INTO window_history (sensor_token, history_state) VALUES (input_idToken, input_state); 
    end //
DELIMITER ;