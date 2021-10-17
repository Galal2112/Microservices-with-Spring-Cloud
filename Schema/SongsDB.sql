CREATE DATABASE Songs;

USE Songs;

CREATE TABLE IF NOT EXISTS `song` (
`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, 
`title` VARCHAR(100) NOT NULL, 
`artist` VARCHAR(100), 
`label` VARCHAR(100), 
`album` VARCHAR(100), 
`released` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `songs_list` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, 
	`ownerId` VARCHAR(50) NOT NULL , 
    `name` VARCHAR(100) NOT NULL, 
	`accessibility` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `songList_songs`(
	`songId` INT NOT NULL ,
    `songListId` INT NOT NULL ,
    FOREIGN KEY (songId) REFERENCES song(id), 
    FOREIGN KEY (songListId) REFERENCES songs_list(id),
    UNIQUE (songId, songListId)
);
