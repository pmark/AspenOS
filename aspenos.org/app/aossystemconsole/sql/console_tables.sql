CREATE TABLE vendor (
vendor_id serial PRIMARY KEY,
display_name text,
system_name text
);

CREATE TABLE app (
app_id serial PRIMARY KEY,
display_name text,
system_name text,
jar_path text,
vendor_id int4,
reggrp_id int4
);

CREATE TABLE reggrp (
reggrp_id serial PRIMARY KEY,
reggrp_key text,
reggrp_name text,
vendor_name text
);

