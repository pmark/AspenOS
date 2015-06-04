
CREATE TABLE story (
story_id	serial PRIMARY KEY,
title text,
site text,
locale text,
section text,
file_name text,
category text,
story_date timestamp default now(),
pub_start_date date,
pub_end_date date,
pub_now bool
);


