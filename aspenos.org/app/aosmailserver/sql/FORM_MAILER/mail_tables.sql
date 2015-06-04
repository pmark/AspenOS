
CREATE TABLE form_mailer (
fm_id	serial PRIMARY KEY,
fm_name text,
send_to text,
send_from text,
formatter text DEFAULT 'org.aspenos.app.aosmailserver.util.DefFMFormatter',
param_from text,
use_param_from bool,
site_name text
);


