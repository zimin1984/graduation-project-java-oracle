

create table users (
 user_id  number not null,
 user_name  varchar(50),
 password varchar(50),
 enabled_department varchar(50),
 privilege number
);

create table departments (
 department_id  number not null,
 department_name  varchar(50)
);

create table places (
 place_id  number not null,
 place_name  varchar(250),
 department_name  varchar(50)
);

create table workers (
 worker_id  number not null,
 fio  varchar(250),
 doljnost varchar(50),
 phone varchar(15),
 work_group varchar(50),
 department_id  number not null
);

create table tasks (
 task_id  number not null,
 task_name varchar(256),
 task_content varchar(356),
 place varchar(55),
 executors varchar(256),
 begin_date date,
 end_date date,
 vypolneno varchar(50),
 td_id varchar(50),
 task_problems varchar(50),
 work_id  number not null
);

create table temp_tasks (
 task_id number not null,
 task_name varchar(256),
 task_content varchar(356),
 place varchar(55),
 executors varchar(256),
 begin_date date,
 end_date date,
 vypolneno varchar(50),
 td_id varchar(50),
 task_problems varchar(50),
 work_id  number
);

create table works (
 work_id number not null,
 work_name varchar(256),
 coordinator varchar(50),
 status varchar(50),
 vladelets varchar(50),
 department_id number not null
);


alter table users
add constraint  users_pk  primary key( user_id )enable;
alter table departments
add constraint  departments_pk  primary key( department_id )enable;
alter table workers
add constraint  workers_pk  primary key( worker_id )enable;
alter table tasks
add constraint  tasks_pk  primary key( task_id )enable;
alter table works
add constraint  works_pk  primary key( work_id )enable;


alter table workers
add constraint  workers_fk  foreign key( department_id ) 
references departments ( department_id ) enable;
alter table tasks
add constraint  tasks_fk  foreign key( work_id ) 
references works ( work_id ) enable;
alter table works
add constraint  works_fk  foreign key( department_id ) 
references departments ( department_id ) enable;

create sequence users_sequence start with 1 increment by 1;
create sequence departments_sequence start with 1 increment by 1;
create sequence places_sequence start with 1 increment by 1;
create sequence workers_sequence start with 1 increment by 1;
create sequence works_sequence start with 1 increment by 1;
create sequence tasks_sequence start with 1 increment by 1;
create sequence temp_tasks_sequence start with 1 increment by 1;

insert into users (user_id, user_name, password, enabled_department,privilege)
  values (users_sequence.nextval, 'administrator','no password','все','3');