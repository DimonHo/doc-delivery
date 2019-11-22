-- 创建触发器,注意语句不要格式化，不要换行！
-- CREATE TRIGGER insert_literature_gmt_create BEFORE INSERT ON literature FOR EACH ROW BEGIN SET new.gmt_create = now(); END;
-- CREATE TRIGGER update_literature_gmt_modified BEFORE UPDATE ON literature FOR EACH ROW BEGIN SET new.gmt_modified = now(); END;
--
-- CREATE TRIGGER insert_help_record_gmt_create BEFORE INSERT ON help_record FOR EACH ROW BEGIN SET new.gmt_create = now(); END;
-- CREATE TRIGGER update_help_record_gmt_modified BEFORE UPDATE ON help_record FOR EACH ROW BEGIN SET new.gmt_modified = now(); END;
--
-- CREATE TRIGGER insert_audit_msg_gmt_create BEFORE INSERT ON audit_msg FOR EACH ROW BEGIN SET new.gmt_create = now(); END;
-- CREATE TRIGGER update_audit_msg_gmt_modified BEFORE UPDATE ON audit_msg FOR EACH ROW BEGIN SET new.gmt_modified = now(); END;
--
-- CREATE TRIGGER insert_give_record_gmt_create BEFORE INSERT ON give_record FOR EACH ROW BEGIN SET new.gmt_create = now(); END;
-- CREATE TRIGGER update_give_record_gmt_modified BEFORE UPDATE ON give_record FOR EACH ROW BEGIN SET new.gmt_modified = now(); END;
--
-- CREATE TRIGGER insert_doc_file_gmt_create BEFORE INSERT ON doc_file FOR EACH ROW BEGIN SET new.gmt_create = now(); END;
-- CREATE TRIGGER update_doc_file_gmt_modified BEFORE UPDATE ON doc_file FOR EACH ROW BEGIN SET new.gmt_modified = now(); END;

-- 初始化测试数据
-- INSERT INTO audit_msg (msg)
-- VALUES ("文不对题"), ("文档无法打开"), ("文档错误");
-- insert into literature (doc_title,doc_href) select title,url FROM spischolar.t_delivery GROUP BY title,url,path;
-- INSERT INTO help_record ( literature_id, helper_email, help_channel, org_name, helper_id ) SELECT t2.id, t1.email, t1.product_id, t1.org_name, t1.member_id FROM spischolar.t_delivery t1, literature t2 WHERE t1.title = t2.doc_title AND t1.url = t2.doc_href;
-- INSERT INTO give_record ( help_record_id, auditor_id, auditor_name, giver_type ) SELECT t3.id, t1.procesor_id, t1.procesor_name, t1.process_type FROM spischolar.t_delivery t1, literature t2, help_record t3 WHERE t1.title = t2.doc_title AND t1.url = t2.doc_href AND t2.id = t3.literature_id;


-- drop PROCEDURE IF EXISTS give_timeout;
-- DROP EVENT IF EXISTS e_give_timeout;
-- CREATE PROCEDURE give_timeout () BEGIN DECLARE helpRecordId INT DEFAULT 0 ; SELECT help_record_id INTO helpRecordId FROM give_record WHERE doc_file_id IS NULL AND 15 < TIMESTAMPDIFF(MINUTE, gmt_create, now()) ; DELETE FROM give_record WHERE help_record_id = helpRecordId AND doc_file_id IS NULL ; UPDATE help_record SET STATUS = 0 WHERE STATUS = 1 AND id = helpRecordId ; END;
-- CREATE EVENT e_give_timeout ON SCHEDULE EVERY 60 SECOND STARTS TIMESTAMP '2018-05-31 00:00:00' ON COMPLETION PRESERVE DO CALL give_timeout ();


-- ALTER TABLE help_record
--  ADD COLUMN is_anonymous  bit(1) NULL DEFAULT b'0' COMMENT '0：不匿名，1：匿名' AFTER literature_id,
--  ADD COLUMN is_send  bit(1) NULL DEFAULT b'1' COMMENT '0：未发送邮件，1：已发送邮件' AFTER is_anonymous,
--  ADD COLUMN remark  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '求助详情' AFTER is_send;


-- insert into permission(level,org_id,org_name,today_total,total) values (1,null,null,5,10),(2,null,null,6,11),(3,null,null,7,12),(6,null,null,8,14),(7,null,null,10,null);
-- insert into channel (id,name,url,template) values (1,"QQ","http://paper.hnlat.com","qq/%s.ftl"),(2,"Spischolar学术资源在线","http://www.spischolar.com","spis/%s.ftl"),(3,"智汇云","http://www.yunscholar.com","zhy/%s.ftl"),(4,"crscholar核心论文库","http://www.crscholar.com","crs/%s.ftl"),(0,"paper","http://paper.hnlat.com","paper/%s.ftl");


-- CREATE
-- 	OR REPLACE VIEW v_help_record (
-- 		`id`,
-- 		`gmt_create`,
-- 		`gmt_modified`,
-- 		`helper_email`,
-- 		`helper_ip`,
-- 		`helper_name`,
-- 		`org_flag`,
-- 		`org_name`,
-- 		`help_channel`,
-- 		`channel_name`,
-- 		`channel_url`,
-- 		`channel_template`,
-- 		`bccs`,
-- 		`exp`,
-- 		`is_anonymous`,
-- 		`is_send`,
-- 		`literature_id`,
-- 		`status`,
-- 		`remark`,
-- 		`doc_title`,
-- 		`doc_href`,
-- 		`author`,
-- 		`doi`,
-- 		`summary`,
-- 		`unid`,
-- 		`year`,
-- 	  `is_difficult`
-- 	) AS SELECT
-- 		`t1`.`id` AS `id`,
-- 		`t1`.`gmt_create` AS `gmt_create`,
-- 		`t1`.`gmt_modified` AS `gmt_modified`,
-- 		`t1`.`helper_email` AS `helper_email`,
-- 		`t1`.`helper_ip` AS `helper_ip`,
-- 		`t1`.`helper_name` AS `helper_name`,
-- 		`t1`.`org_flag` AS `org_flag`,
-- 		`t1`.`org_name` AS `org_name`,
-- 		`t1`.`help_channel` AS `help_channel`,
-- 		`t3`.`name` AS `channel_name`,
-- 		`t3`.`url` AS `channel_url`,
-- 		`t3`.`template` AS `channel_template`,
-- 		`t3`.`bccs` AS `bccs`,
-- 		`t3`.`exp` AS `exp`,
-- 		`t1`.`is_anonymous` AS `is_anonymous`,
-- 		`t1`.`is_send` AS `is_send`,
-- 		`t1`.`literature_id` AS `literature_id`,
-- 		`t1`.`status` AS `status`,
-- 		`t1`.`remark` AS `remark`,
-- 		`t2`.`doc_title` AS `doc_title`,
-- 		`t2`.`doc_href` AS `doc_href`,
-- 		`t2`.`author` AS `author`,
-- 		`t2`.`doi` AS `doi`,
-- 		`t2`.`summary` AS `summary`,
-- 		`t2`.`unid` AS `unid`,
-- 		`t2`.`year` AS `year`,
-- 	  `t1`.`is_difficult`
-- 	FROM
-- 		( ( `help_record` `t1` JOIN `literature` `t2` ) JOIN `channel` `t3` )
-- WHERE
-- 	( ( `t1`.`literature_id` = `t2`.`id` ) AND ( `t1`.`help_channel` = `t3`.`id` ) );


-- QQ报表导出
# SELECT
#     gmt_create,
#     org_name,
#     helper_email,
#     helper_name,
#     doc_title,
#     doc_href,
#     handler_name AS 处理人,
#     CASE
#
#         WHEN STATUS = 4 THEN
#             "成功"
#         WHEN STATUS = 3 THEN
#             "第三方"
#         WHEN is_difficult = 1 THEN
#             "失败" ELSE "待应助"
#         END 状态
# FROM
#     v_help_record
# WHERE
#         help_channel = 1
#   AND DATE_FORMAT( gmt_create, "%Y-%m" ) = "2019-08";

# insert into permission (gmt_create,gmt_modified,level,org_id,org_flag,org_name,today_total,total) values
# (now(),now(),1,166,"haut","河南工业大学",999,null),
# (now(),now(),2,166,"haut","河南工业大学",999,null),
# (now(),now(),3,166,"haut","河南工业大学",999,null),
# (now(),now(),6,166,"haut","河南工业大学",999,null),
# (now(),now(),7,166,"haut","河南工业大学",999,null);