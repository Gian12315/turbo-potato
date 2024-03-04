CREATE DATABASE IF NOT EXISTS topicos;
USE topicos;

CREATE TABLE IF NOT EXISTS datos (
       id    INT PRIMARY KEY AUTO_INCREMENT,
       humedad FLOAT NOT NULL,
       hora    DATETIME NOT NULL
);


INSERT INTO datos (humedad, hora) VALUES (0.50, "2023-02-28 14:15:00");
INSERT INTO datos (humedad, hora) VALUES (0.80, "2023-02-28 15:15:00");
INSERT INTO datos (humedad, hora) VALUES (0.90, "2023-02-28 16:15:00");
