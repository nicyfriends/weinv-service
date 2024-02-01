
----------------------------------------------------------------
                -- WEINV-SERVICES-SERVER --
----------------------------------------------------------------
--INSERT INTO users (created_by, created_at, updated_by, updated_at, id, uuid, username, first_name, last_name, email, roles, language, enabled)
--  VALUES ('System'::character varying, now(), 'System'::character varying, now(),1,'85249f2a-e86c-11ea-adc1-0242ac120002','weinvAdmin','nicy','malanda','malandanicy@gmail.com','admin','EN','true');

-- Authority represents an individual permission.
-- Role represents a group of permissions.


----------------------------------------------------------------
                -- WEINV-AUTHORIZATION-SERVER --
----------------------------------------------------------------
---- The encrypted client_secret is `weinv1@client.secret`
---- Access_token_validity: 20min
---- Refresh_token_validity: 8h
--INSERT INTO oauth_client_details (resource_ids, client_id, client_secret, scope, authorized_grant_types, authorities, access_token_validity, refresh_token_validity, web_server_redirect_uri, additional_information)
--VALUES ('EVENT-INVITATION', 'weinv', '$2a$12$5iyG9zrPNRhi8akByKeA/ez0nDiFocUMdmoiXe82iNk8xN7dbn..q', 'read,write', 'password,refresh_token,client_credentials', 'client', 1200, 28800, 'https://weinv-services.cleverapps.io/weinv/login', '{}');
--
---- The encrypted password is `weinv1@admin.secret`
--INSERT INTO users (id, username, password, email) VALUES (1, 'weinv.admin', '$2a$12$BtGQ9IDSmsYSfjofVeISre.Wlvrl9hvkvAW2GjAtFnr1bHVyAGhDG', 'malandanicy@gmail.com');
---- The encrypted password is `password`
--INSERT INTO users (id, username, password, email) VALUES (2, 'responsibleTest', '$2a$12$PpwKquVwHdEJ4Qqj.KYPNe6FATlc3auhCfH6TQ3InjzbKyP/w9C8S', 'nicy.lab.noreply@gmail.com');
--INSERT INTO users (id, username, password, email) VALUES (3, 'guestTest', '$2a$12$PpwKquVwHdEJ4Qqj.KYPNe6FATlc3auhCfH6TQ3InjzbKyP/w9C8S', 'nicy.lab.noreply@gmail.com');
--
--
--INSERT INTO authorities (id, name) VALUES (1, 'admin');
--INSERT INTO authorities (id, name) VALUES (2, 'user');
--INSERT INTO authorities (id, name) VALUES (3, 'guest');
--INSERT INTO authorities (id, name) VALUES (4, 'client');
--
--INSERT INTO user_authority (username, authority_name) VALUES ('weinv.admin', 'admin');
--INSERT INTO user_authority (username, authority_name) VALUES ('responsibleTest', 'user');
--INSERT INTO user_authority (username, authority_name) VALUES ('guestTest', 'guest');


-- Authority represents an individual permission.
-- Role represents a group of permissions.





