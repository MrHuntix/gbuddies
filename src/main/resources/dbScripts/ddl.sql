create table BUDDY_GRAPH
(
	id int auto_increment
		primary key,
	userId int not null,
	userBuddy int not null
)
;

create table GEN_GYM
(
	id int auto_increment
		primary key,
	name varchar(100) not null,
	website varchar(500) not null
)
;

create table GEN_PROFILE_PIC
(
	picId int auto_increment
		primary key,
	userImage blob null
)
;

create table GEN_USER
(
	userId int auto_increment
		primary key,
	userName varchar(30) not null,
	emailId varchar(30) not null,
	mobileNo varchar(15) not null,
	password varchar(200) not null,
	roles varchar(250) default 'role_admin' not null,
	about varchar(1024) null,
	picId int null,
	constraint GEN_USER_PROFILE_PIC__fk
		foreign key (picId) references GEN_PROFILE_PIC (picId)
			on update cascade on delete cascade
)
;

create index GEN_USER_PROFILE_PIC__fk
	on GEN_USER (picId)
;

create table GYM_BRANCH
(
	id int auto_increment
		primary key,
	gymId int not null,
	locality varchar(150) not null,
	city varchar(50) not null,
	latitude double not null,
	longitude double not null,
	contact varchar(50) not null,
	constraint GYM_BRANCH_FOREIGN_KEY
		foreign key (gymId) references GEN_GYM (id)
			on update cascade on delete cascade
)
;

create index GYM_ADDRESS_FOREIGN_KEY
	on GYM_BRANCH (gymId)
;

create table MATCHES
(
	id int auto_increment
		primary key,
	lookup_id int not null,
	gymId int not null,
	branchid int not null,
	requester int not null,
	requestee int not null
)
;

create index MATCH_FK_LOOKUP_ID
	on MATCHES (lookup_id)
;

create table MATCH_LOOKUP
(
	id int auto_increment
		primary key,
	gymid int not null,
	branchid int not null,
	requesterid int not null,
	status varchar(250) not null
)
;

alter table MATCHES
	add constraint MATCH_FK_LOOKUP_ID
		foreign key (lookup_id) references MATCH_LOOKUP (id)
			on update cascade on delete cascade
;

create table MATCH_REQUEST
(
	id int auto_increment
		primary key,
	lookupRequesterId int not null,
	lookupRequesteeId int not null,
	userRequesterId int not null,
	userRequesteeId int not null,
	status varchar(20) default 'REQUESTED' not null
)
;

