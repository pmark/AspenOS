
INSERT INTO principal (username,password,selected_role) VALUES ('user', '12345','1');
INSERT INTO principal (username,password,selected_role) VALUES ('admin','12345','2');

INSERT INTO role (name,role_group,vendor) VALUES ('user', 'users','aspenos');
INSERT INTO role (name,role_group,vendor) VALUES ('admin','admins','aspenos');

INSERT INTO prinroles VALUES ('1','1');
INSERT INTO prinroles VALUES ('2','1');
INSERT INTO prinroles VALUES ('2','2');
