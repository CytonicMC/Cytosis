-- apply alter tables
alter table cytonic_reports
    add column if not exists notified boolean default false not null;
alter table cytonic_reports
    add column if not exists resolution_context jsonb;
