use `team-work`;

start transaction;

create table `sessions`
(
    `session_id`    binary(16)   not null,
    `user_agent`    varchar(255) not null,
    `ip_address`    varchar(45)  not null,
    `created_at`    timestamp    not null,
    `last_activity` timestamp    null,
    `revoked`       boolean      not null default 0,
    constraint `pk_session` primary key (`session_id`)
);

alter table `refresh_token_families`
    add column `session_id` binary(16) null;

create temporary table `tmp_family_session` as
select id                  as family_id,
       UUID_TO_BIN(UUID()) as session_id
from refresh_token_families
where refresh_token_families.session_id is null;

insert into sessions (session_id, user_agent, ip_address, created_at, revoked)
select session_id,
       'legacy_administrator',
       '0.0.0.0',
       now(),
       0
from tmp_family_session;

update `refresh_token_families` rtf
    join tmp_family_session tmp on rtf.id = tmp.family_id
set rtf.session_id = tmp.session_id
where rtf.session_id is null;

drop temporary table tmp_family_session;

alter table refresh_token_families
    modify column session_id binary(16) not null;

alter table refresh_token_families
    add constraint `fk_family_session`
        foreign key (`session_id`) references `sessions` (`session_id`);

create index `idx_rtf_session` on `refresh_token_families` (`session_id`);

commit;