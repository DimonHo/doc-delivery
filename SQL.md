
- QQ月应助报表
```sql
SELECT
	gmt_create,
	org_name,
	helper_email,
	helper_name,
	doc_title,
	doc_href,
	handler_name AS 处理人,
CASE
		WHEN STATUS = 4 THEN
		"成功" 
		WHEN STATUS = 3 THEN
		"第三方" 
		WHEN is_difficult = 1 THEN
		"失败" ELSE "待应助" 
	END 状态 
FROM
	v_help_record 
WHERE
	help_channel = 1 
	AND DATE_FORMAT( gmt_create, "%Y-%m" ) = "2019-07";
```