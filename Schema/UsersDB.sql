CREATE DATABASE Users;

USE Users;

CREATE TABLE IF NOT EXISTS `user` ( 
	`userId` VARCHAR(50) NOT NULL , 
	`password` VARCHAR(50) NOT NULL ,
	`firstName` VARCHAR(50) NOT NULL ,
	`lastName` VARCHAR(50) NOT NULL ,
	`authToken` VARCHAR(50) , 
	 PRIMARY KEY (`userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

desc user;

INSERT INTO user (userId, password, firstName, lastName )
VALUES ('mmuster', 'pass1234', 'Maxime', 'Muster');

INSERT INTO user (userId, password, firstName, lastName )
VALUES ('eschuler', 'pass1234', 'Elena', 'Schuler');
