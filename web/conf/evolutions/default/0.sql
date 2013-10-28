# --- !Ups

set ignorecase true;

create table accounts (
  username							varchar(1023) not null,
  firstname							varchar(1023) not null,
  lastname							varchar(1023) not null,
  providerId                  	    bigint not null,
  providerName                    	varchar(255) not null,
  email								varchar(1023) not null,
  constraint primary key (username));

# --- !Downs

drop table if exists accounts;