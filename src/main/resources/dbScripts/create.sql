CREATE TABLE `GYM` (
  `id` int auto_increment primary key,
  `name` varchar(100) NOT NULL,
  `website` varchar(250) NOT NULL,
  `created_date` date DEFAULT NULL,
  `last_updated_date` date DEFAULT NULL,
  `hostname` varchar(250) NOT NULL
);

CREATE TABLE `BRANCH` (
  `id` int auto_increment primary key,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `contact` varchar(50) NOT NULL,
  `address_id` int NOT NULL,
  `gym_id` int NOT NULL
);

CREATE TABLE `ADDRESS` (
  `id` int auto_increment primary key,
  `state` varchar(50) NOT NULL,
  `city` varchar(250) NOT NULL,
  `pincode` bigint NOT NULL
);

ALTER TABLE BRANCH ADD CONSTRAINT FK_BRANCH_GYM_GYM_ID FOREIGN KEY (GYM_ID) REFERENCES GYM(ID);
ALTER TABLE BRANCH ADD CONSTRAINT FK_BRANCH_ADDRESS_ADDRESS_ID FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESS(ID);

CREATE TABLE `USER` (
  `id` int auto_increment primary key,
  `name` varchar(30) NOT NULL,
  `mobile` varchar(15) NOT NULL,
  `password` varchar(25) NOT NULL,
  `pic_url` varchar(250) NOT NULL,
  `address_id` int NOT NULL,
  `role` varchar(50) NOT NULL,
  `bio` varchar(500) NOT NULL,
  `last_login_date` date DEFAULT NULL,
  `deleted_date` date DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `last_updated_date` date DEFAULT NULL,
  `hostname` varchar(250) NOT NULL
);
ALTER TABLE BRANCH ADD CONSTRAINT FK_USER_ADDRESS_ADDRESS_ID FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESS(ID);

CREATE TABLE `MATCH_LOOKUP` (
  `id` int auto_increment primary key,
  `gym_id` int NOT NULL,
  `branch_id` int NOT NULL,
  `requester_id` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_date` date DEFAULT NULL,
  `last_updated_date` date DEFAULT NULL,
  `hostname` varchar(250) NOT NULL
);

CREATE TABLE `MATCH_REQUEST` (
  `id` int auto_increment primary key,
  `lookup_requester_id` int NOT NULL,
  `lookup_requestee_id` int NOT NULL,
  `user_requester_id` int NOT NULL,
  `user_requestee_id` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_date` date DEFAULT NULL,
  `last_updated_date` date DEFAULT NULL,
  `hostname` varchar(250) NOT NULL
);

CREATE TABLE `BUDDY_GRAPH` (
  `id` int auto_increment primary key,
  `user_id` int NOT NULL,
  `buddy_id` int NOT NULL,
  `match_request_id` int NOT NULL,
  `created_date` date DEFAULT NULL,
  `last_updated_date` date DEFAULT NULL,
  `hostname` varchar(250) NOT NULL
);


DROP TABLE GYM;
DROP TABLE BRANCH;
DROP TABLE USER;
DROP TABLE MATCH_LOOKUP;
DROP TABLE MATCH_REQUEST;
DROP TABLE BUDDY_GRAPH;
