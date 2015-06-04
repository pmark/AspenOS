
CREATE TABLE template (
template_id		serial PRIMARY KEY,
name			text,
file_path		text
);

CREATE TABLE resource (
resource_id		serial PRIMARY KEY,
name			text unique
);

CREATE TABLE webevent (
webevent_id		serial PRIMARY KEY,
name			text,
classname		text,
menu_name		text,
menu_sel_name	text
);

CREATE TABLE resourcetemplates (
template_id		int4,
resource_id		int4,
role_id			int4
);

CREATE TABLE webeventresources (
webevent_id		int4,
resource_id		int4,
ordinal			int4
);




CREATE TABLE menu (
menu_id			serial PRIMARY KEY,
name			text,
parent_name		text,
type			text
);

CREATE TABLE menubtn (
menu_name		text UNIQUE,
icon_name		text,
event_name		text,
ordinal			int2
);

CREATE TABLE icon (
name		text UNIQUE,
alt		text,
type		text,
default_image	text,
mouseover_image	text,
select_image	text,
style_class	text,
label		text
);

CREATE TABLE texttmpl (
role_id		int4,
placement_code	text,
text_data	text,
description	text
);



