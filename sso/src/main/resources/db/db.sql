CREATE DATABASE sso;

USE sso;

CREATE TABLE user (
  username VARCHAR(20),
  password VARCHAR(200),
  tel VARCHAR(11),
  sex CHAR(1),
  update_time TIMESTAMP,
  msg VARCHAR(200)
)