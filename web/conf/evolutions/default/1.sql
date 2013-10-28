# --- !Ups

set ignorecase true;

create table account (
  userName								varchar(1023) not null,
  firstName								varchar(1023) not null,
  lastName								varchar(1023) not null,
  userId                  	    		varchar(1023) not null,
  providerId                    		varchar(255) not null,
  email									varchar(1023),
  avatarUrl								varchar(1023),
  constraint pk_account primary key (userName))
;

# --- !Downs

drop table if exists accounts;