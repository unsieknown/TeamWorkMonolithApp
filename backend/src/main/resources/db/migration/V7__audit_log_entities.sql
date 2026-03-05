use `team-work`;

create table if not exists `audit_log_entities`
(
    `event_id`   binary(16)   not null,
    `event_type` varchar(50)  not null,
    `user_id`    binary(16)   null,
    `session_id` binary(16)   null,
    `method`     varchar(10)  not null,
    `uri`        varchar(255) not null,
    `status`     int          not null,
    `ip`         varchar(45)  not null,
    `user_agent` varchar(255) not null,
    `duration`   bigint       not null,
    `timestamp`  timestamp    not null default current_timestamp(),
    `details`    varchar(512) null,
    constraint `pk_ale` primary key (`event_id`)
);

create index `idx_ale_user_id` on `audit_log_entities` (`user_id`);
create index `idx_ale_event_type` on `audit_log_entities` (`event_type`);