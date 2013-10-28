# --- !Ups

set ignorecase true;

create table Accounts (
  providerID                        int not null,
  providerName                      varchar(255) not null,
  email								varchar(255),
  constraint accountID primary key (id))
;

# --- !Downs

drop table if exists Accounts;