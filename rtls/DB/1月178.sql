-- 车位导航总数
SELECT
	SUM(total_count) AS placeNavigationTotal
FROM
	(         SELECT
		          COUNT(*) AS total_count
	          FROM
		          user_active_select_place
	          WHERE
			          addTime >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
		        AND addTime <= NOW()
		        and map = 178
	          UNION
		          ALL         SELECT
		          COUNT(*) AS total_count
	          FROM
		          parking_info_statistics_recommend
	          WHERE
			          start_time >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
		        AND start_time <= NOW()
		        and map = 178
	) AS total_counts

-- 车位导航使用率
SELECT
		subquery.total_count / placeCount.total_count / 30 AS `placeNavigationUseRate`
FROM
	(         SELECT
		          SUM(total_count) AS total_count
	          FROM
		          (         SELECT
			                    COUNT(*) AS total_count
		                    FROM
			                    user_active_select_place
		                    WHERE
				                    addTime >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
			                  AND addTime <= NOW()
			                  and map=178
		                    UNION
			                    ALL          SELECT
			                    COUNT(*) AS total_count
		                    FROM
			                    parking_info_statistics_recommend
		                    WHERE
				                    map=178
			                  and start_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
			                  AND start_time <= NOW()
		          ) AS subquery         ) AS subquery,
	(SELECT
		 COUNT(*) AS total_count
	 FROM
		 parking_place
	 WHERE
			 id IN (
			 SELECT
				 place
			 FROM
				 infrared
		 )
	   and map=178
	) AS placeCount
-- 反向寻车总数

select
	count(p.map) as reverseCarSearchTotal
from
	parking_info_statistics_findcar p
WHERE
		`start_time` >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
  AND `start_time` <= NOW()
  and map =178

-- 用户总数
select count(distinct userid) userTotal
from map_monthactiveuserrecord
where map =178 and `loginTime` >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
  AND `loginTime` <= NOW()
-- 访问总次数
select
	count(userid) as useFrequency
from
	map_monthactiveuserrecord
where
		map=178
  and loginTime >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
  AND loginTime <= NOW()

-- 用户检索总数
select count(uslt.u_s_log_id ) userSearchTotal
from user_search_log usl
	     left JOIN user_search_log_time uslt ON usl.id = uslt.u_s_log_id
where map=178 and uslt.time >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
  AND uslt.time <= NOW()

-- 位置分享总数
SELECT
	count(lslt.l_s_log_id) locationShareTotal
FROM
	location_sharing_log lsl
		left JOIN
	location_sharing_log_time lslt
	ON lsl.id = lslt.l_s_log_id
WHERE
		map = 178
  and lslt.time >= DATE_SUB( NOW(), INTERVAL 1 MONTH )
  AND lslt.time <= NOW()
