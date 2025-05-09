alter table T_USER add column USE_TOTPKEY_C varchar(100);

-- Create user request table
create table T_USER_REQUEST (
    URE_ID_C varchar(36) not null,
    URE_USERNAME_C varchar(50) not null,
    URE_EMAIL_C varchar(100) not null,
    URE_PASSWORD_C varchar(100) not null,
    URE_STATUS_C varchar(1) not null,
    URE_CREATEDATE_D datetime not null,
    URE_PROCESSDATE_D datetime,
    URE_PROCESSUSERID_C varchar(36),
    primary key (URE_ID_C)
);

-- Update database version
update T_CONFIG set CFG_VALUE_C = '9' where CFG_ID_C = 'DB_VERSION';
