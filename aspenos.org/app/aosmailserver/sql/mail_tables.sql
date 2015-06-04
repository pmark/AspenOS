
CREATE TABLE list (
list_id			serial PRIMARY KEY,
list_name		text,
list_sys_name	text,
list_desc		text,
site_name		text
);

CREATE TABLE subscriber (
subscriber_id	serial PRIMARY KEY,
name			text,
email			text UNIQUE
);

CREATE TABLE list_subscribers (
list_id			int4,
subscriber_id	int4
);


CREATE TABLE message_template (
mt_id	serial PRIMARY KEY,
mt_body text,
mt_name text,
mt_type text
);

CREATE TABLE sent_message (
mt_id	int4,
list_id int4,
status text,
send_datetime timestamp default now()
);

CREATE TABLE outbox (
mt_id	int4,
list_id int4,
send_datetime timestamp default now()
);


