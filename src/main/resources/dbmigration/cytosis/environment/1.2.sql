-- apply changes
create table cytonic_player_data (
  uuid                          uuid not null,
  username                      varchar not null,
  skin_signature                varchar,
  skin_textures                 varchar,
  ip                            varchar not null,
  proxy                         varchar,
  version                       varchar not null,
  constraint pk_cytonic_player_data primary key (uuid)
);

create table cytonic_sessions (
  uuid                          uuid not null,
  created_at                    timestamptz not null,
  username                      varchar not null,
  skin_signature                varchar not null,
  skin_textures                 varchar not null,
  client_ip                     varchar not null,
  proxy                         varchar,
  server_id                     varchar,
  version                       varchar not null,
  constraint pk_cytonic_sessions primary key (uuid)
);

