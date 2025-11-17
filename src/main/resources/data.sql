-- password04
insert into app_user ( email , password ,  username , role ) values ('admin1@wp.pl', '$2a$10$Ex02DfV0RrCd2ugBxnbEnOqnNy9tr.6pu0ZFVdbbSqRZHv9O1K0hu', 'admin1', 'ADMIN');
insert into shop(id,name) values('1','olx');
insert into shop(id,name) values('2','allegro');
insert into shop(id,name) values('3','allegroLokalnie');
insert into offer_update_config(id, type, interval_in_minutes) values('1','MANUAL',null);