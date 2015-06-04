alter table story rename column section_name to section;
alter table story rename column publish_start_date to pub_start_date;
alter table story rename column publish_end_date to pub_end_date;
alter table story rename column publish_now to pub_now;

alter table story rename column body to file_name;
alter table story add column category text;
alter table story add column locale text;

update story set locale='en_us';
