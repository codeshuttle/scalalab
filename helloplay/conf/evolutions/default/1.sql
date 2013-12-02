# Tasks schema
 
# --- !Ups

CREATE TABLE task (
    id int NOT NULL AUTO_INCREMENT,
    label varchar(255),
    PRIMARY KEY (id)
);
 
CREATE TABLE user (
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255),
    email varchar(255),
    password varchar(255),
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE task;
DROP TABLE user;

