select s.* from subscriber s, list_subscribers ls, list l where l.list_id=ls.list_id and s.subscriber_id=ls.subscriber_id and l.list_id=1;
