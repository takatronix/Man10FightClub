create table mfc_bet
(
    id           int unsigned auto_increment
        primary key,
    fight_id     int                    null,
    datetime     datetime               not null comment '日付',
    name         varchar(40) default '' not null comment 'ユーザー名',
    uuid         varchar(40) default '' not null comment 'UUID',
    bet          double                 null,
    win          tinyint(1)             null comment '勝敗',
    fighter_uuid varchar(40) default '' not null comment 'UUID',
    fighter_name varchar(40) default '' not null comment 'ユーザー名',
    odds         double                 null comment 'オッズ',
    profit       double                 null comment '収益'
)
    charset = utf8;

create table mfc_fight
(
    id       int unsigned auto_increment
        primary key,
    datetime datetime               not null comment '日付',
    kit      varchar(40) default '' not null,
    stage    varchar(40) default '' not null,
    uuid1    varchar(40)            not null comment 'UUID',
    uuid2    varchar(40) default '' not null comment 'UUID',
    player1  varchar(40) default '' not null comment 'UUID',
    player2  varchar(40) default '' not null comment 'UUID',
    odds1    double                 null,
    odds2    double                 null,
    bet1     int                    null,
    bet2     int                    null,
    totalbet double                 null,
    prize    double                 null,
    result   int                    null comment '0:Cancel 1:player1 2:player2',
    winner   varchar(40) default '' null comment 'UUID',
    loser    varchar(40)            null comment 'UUID',
    duration float                  null
)
    charset = utf8;

create index mfc_fight_winner_loser_index
    on mfc_fight (winner, loser);

create table mfc_player
(
    id       int auto_increment,
    datetime datetime         not null,
    uuid     varchar(36)      not null,
    `kill`   int    default 0 not null,
    death    int    default 0 not null,
    kdr      float  default 0 not null,
    prize    double default 0 null,
    mcid     varchar(16)      not null,
    score    int    default 0 null,
    constraint mfc_player_id_uindex
        unique (id),
    constraint mfc_player_mcid_uindex
        unique (mcid),
    constraint mfc_player_uuid_uindex
        unique (uuid)
);

alter table mfc_player
    add primary key (id);

create table mfcpro_bet
(
    id           int unsigned auto_increment
        primary key,
    fight_id     int                    null,
    datetime     datetime               not null comment '日付',
    name         varchar(40) default '' not null comment 'ユーザー名',
    uuid         varchar(40) default '' not null comment 'UUID',
    bet          double                 null,
    win          tinyint(1)             null comment '勝敗',
    fighter_uuid varchar(40) default '' not null comment 'UUID',
    fighter_name varchar(40) default '' not null comment 'ユーザー名',
    odds         double                 null comment 'オッズ',
    profit       double                 null comment '収益'
)
    charset = utf8;

create table mfcpro_fight
(
    id       int unsigned auto_increment
        primary key,
    datetime datetime               not null comment '日付',
    kit      varchar(40) default '' not null,
    stage    varchar(40) default '' not null,
    uuid1    varchar(40)            not null comment 'UUID',
    uuid2    varchar(40) default '' not null comment 'UUID',
    player1  varchar(40) default '' not null comment 'UUID',
    player2  varchar(40) default '' not null comment 'UUID',
    odds1    double                 null,
    odds2    double                 null,
    bet1     int                    null,
    bet2     int                    null,
    totalbet double                 null,
    prize    double                 null,
    result   int                    null comment '0:Cancel 1:player1 2:player2',
    winner   varchar(40) default '' null comment 'UUID',
    loser    varchar(40)            null comment 'UUID',
    duration float                  null
)
    charset = utf8;

create index mfcpro_fight_winner_loser_index
    on mfcpro_fight (winner, loser);

create table mfcpro_player
(
    id       int auto_increment,
    datetime datetime         not null,
    uuid     varchar(36)      not null,
    `kill`   int    default 0 not null,
    death    int    default 0 not null,
    kdr      float  default 0 not null,
    prize    double default 0 null,
    mcid     varchar(16)      not null,
    score    int    default 0 null,
    constraint mfcpro_player_id_uindex
        unique (id),
    constraint mfcpro_player_mcid_uindex
        unique (mcid),
    constraint mfcpro_player_uuid_uindex
        unique (uuid)
);

alter table mfcpro_player
    add primary key (id);

