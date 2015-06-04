CREATE TABLE principal (
principal_id	serial PRIMARY KEY,
username	text,
password	text,
host_site	text,
selected_role	int2
);

CREATE TABLE role (
role_id		serial PRIMARY KEY,
name		text,
role_group	text,
vendor		text
);

CREATE TABLE prinroles (
principal_id	int4,
role_id		int4
);

CREATE TABLE session (
session_id	serial PRIMARY KEY,
system_sid	text,
principal_id	int4,
status		int2,
lastaccess	timestamp,
starttime	datetime,
endtime		datetime
);
