
CREATE TABLE story (
story_id	serial PRIMARY KEY,
body text,
title text,
section_name text,
site text,
story_date timestamp default now(),
publish_start_date date,
publish_end_date date,
publish_now bool
);


