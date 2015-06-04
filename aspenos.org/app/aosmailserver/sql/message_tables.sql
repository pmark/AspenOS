
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



