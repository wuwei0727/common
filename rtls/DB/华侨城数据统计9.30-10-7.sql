


SELECT TIMESTAMPDIFF(DAY, '2024-09-30 00:00:00', '2024-10-07 23:59:59') AS total_days;
SELECT TIMESTAMPDIFF(HOUR, '2024-09-30 00:00:00', '2024-10-07 23:59:59') AS total_hours;

-- 车位导航总数
SELECT SUM(total_count) AS placeNavigationTotal
FROM (
	     SELECT COUNT(*) AS total_count
	     FROM user_active_select_place
	     WHERE
			     addTime >= '2024-09-30 00:00:00'
		   AND addTime <= '2024-10-07 23:59:59'
		   AND map = 178

	     UNION ALL

	     SELECT COUNT(*) AS total_count
	     FROM parking_info_statistics_recommend
	     WHERE
			     start_time >= '2024-09-30 00:00:00'
		   AND start_time <= '2024-10-07 23:59:59'
		   AND map = 178
     ) AS total_counts;


-- 车位导航使用率
SELECT subquery.total_count / placeCount.total_count / 8 AS `placeNavigationUseRate`
FROM (
	     SELECT SUM(total_count) AS total_count
	     FROM (
		          SELECT COUNT(*) AS total_count
		          FROM user_active_select_place
		          WHERE addTime >= '2024-09-30 00:00:00' AND addTime <= '2024-10-07 23:59:59'
			        AND map = 178
		          UNION ALL
		          SELECT COUNT(*) AS total_count
		          FROM parking_info_statistics_recommend
		          WHERE map = 178
			        AND start_time >= '2024-09-30 00:00:00' AND start_time <= '2024-10-07 23:59:59'
	          ) AS subquery
     ) AS subquery,
     (SELECT COUNT(*) AS total_count
      FROM parking_place
      WHERE id IN (SELECT place FROM infrared)
	    AND map = 178
     ) AS placeCount;

-- 车位利用率
SELECT
	p.map,
	(pu.total_count /
		-- 根据 mapId 判断使用 parking_place 还是 infrared
     IF(178 = 178, COUNT(*), (SELECT COUNT(*) FROM infrared WHERE map =178)) / 8) AS mapPlatformUtilizationRate
			 FROM parking_place p
			 JOIN
			 (SELECT map, COUNT(*) AS total_count
			  FROM place_userecord
			  WHERE `start` >= '2024-09-30 00:00:00' AND `start` <= '2024-10-07 23:59:59'
				AND map = 178 AND is_fake_data=0
			  GROUP BY map) AS pu
			 ON p.map = pu.map
			 WHERE
			 (178 = 178 OR id IN (SELECT place FROM infrared))
				 AND p.map = 178
				 GROUP BY p.map
				 ORDER BY mapPlatformUtilizationRate DESC
				 LIMIT 10;


-- 反向寻车总数
SELECT COUNT(p.map) AS reverseCarSearchTotal
FROM parking_info_statistics_findcar p
WHERE `start_time` >= '2024-09-30 00:00:00'
  AND `start_time` <= '2024-10-07 23:59:59'
  AND map = 178

-- 停车场车位空闲率
SELECT ((total_hours - occupied_hours) / total_hours) * 100 AS placeAvailabilityRate
FROM (
	     SELECT (8*24) * COUNT(DISTINCT p.id) AS total_hours, -- 计算7天的总时长
	            COALESCE(SUM(
			                     CASE
				                     WHEN pu.start IS NOT NULL AND pu.end IS NULL THEN -- 处理没有结束时间的情况
					                     CASE
						                     WHEN pu.start < '2024-09-30 00:00:00' THEN (8*24) -- 开始时间早于时段，则占用整个7天（168小时）
						                     ELSE TIMESTAMPDIFF(HOUR, pu.start, NOW()) -- 开始时间晚于时段，则占用从开始时间到现在的时长
						                     END
				                     WHEN pu.start IS NOT NULL AND pu.end IS NOT NULL THEN TIMESTAMPDIFF(HOUR, pu.start, pu.end) -- 处理有开始和结束时间的情况
				                     ELSE 0 -- 没有使用记录的车位被认为在时段内空闲
				                     END
		                     ), 0) AS occupied_hours
	     FROM parking_place p
		          LEFT JOIN infrared i ON p.id = i.place
		          LEFT JOIN place_userecord pu ON p.id = pu.place
		     AND ((pu.start IS NOT NULL AND pu.end IS NOT NULL AND pu.end > pu.start) OR (pu.start IS NOT NULL AND pu.end IS NULL))
		     AND (pu.start >= '2024-09-30 00:00:00' OR pu.end IS NULL)
		     AND pu.start <= '2024-10-07 23:59:59' and is_fake_data=0
	     WHERE i.map IS NOT NULL
		   AND p.map = 178
     ) AS subquery;


-- 地图用户总数
SELECT COUNT(DISTINCT userid) AS userTotal
FROM map_monthactiveuserrecord
WHERE map = 178
  AND `loginTime` >= '2024-09-30 00:00:00'
  AND `loginTime` <= '2024-10-07 23:59:59'


-- 地图访问次数
SELECT COUNT(userid) AS useFrequency
FROM map_monthactiveuserrecord
WHERE map = 178
  AND loginTime >= '2024-09-30 00:00:00'
  AND loginTime <= '2024-10-07 23:59:59'


-- 活跃用户数
SELECT
	COUNT(DISTINCT userid) AS activeUsers
FROM
	map_monthactiveuserrecord
WHERE
		loginTime >= '2024-09-30 00:00:00'
  AND loginTime <= '2024-10-07 23:59:59'
  AND map = 178 and is_fake_data=0;

-- 用户检索总数
SELECT COUNT(uslt.u_s_log_id) AS userSearchTotal
FROM user_search_log usl
	     LEFT JOIN user_search_log_time uslt ON usl.id = uslt.u_s_log_id
WHERE uslt.time >= '2024-09-30 00:00:00'
  AND uslt.time <= '2024-10-07 23:59:59'
  AND usl.map = 178

-- 位置分享总数
SELECT COUNT(lslt.l_s_log_id) AS locationShareTotal
FROM location_sharing_log lsl
	     LEFT JOIN location_sharing_log_time lslt ON lsl.id = lslt.l_s_log_id
WHERE lslt.time >= '2024-09-30 00:00:00'
  AND lslt.time <= '2024-10-07 23:59:59'
  AND lsl.map = 178

-- 车位使用次数
SELECT
	(SELECT p.name FROM parking_place p WHERE p.id = u.place) AS placeName,
	(SELECT COUNT(*)
	 FROM place_userecord
	 WHERE map = 178 and is_fake_data=0 AND (
			 (end IS NOT NULL AND TIMESTAMPDIFF(MINUTE, start, end) >= 30)
			 OR (end IS NULL AND TIMESTAMPDIFF(MINUTE, start, '2024-10-07 23:59:59') >= 30)
		 )
	   AND ((`start` >= '2024-09-30 00:00:00' AND `start` <= '2024-10-07 23:59:59')
		 OR (`end` >= '2024-09-30 00:00:00' AND `start` <= '2024-10-07 23:59:59')
		 OR (`end` IS NULL AND `start` <= '2024-10-07 23:59:59'))) AS total,
	COUNT(*) AS placeUseTotal
FROM place_userecord u
WHERE map = 178 and is_fake_data=0 AND (
		(end IS NOT NULL AND TIMESTAMPDIFF(MINUTE, start, end) >= 30)
		OR (end IS NULL AND TIMESTAMPDIFF(MINUTE, start, '2024-10-07 23:59:59') >= 30)
	)
  AND ((`start` >= '2024-09-30 00:00:00' AND `start` <= '2024-10-07 23:59:59')
	OR (`end` >= '2024-09-30 00:00:00' AND `start` <= '2024-10-07 23:59:59')
	OR (`end` IS NULL AND `start` <= '2024-10-07 23:59:59'))
GROUP BY placeName
ORDER BY placeUseTotal DESC
LIMIT 10;


-- 车位空闲总时长
SELECT
	(SELECT name FROM parking_place WHERE id = place) AS placeName,
	((8* 24 * 60 * 60) - SUM(TIMESTAMPDIFF(SECOND, start, end))) / 3600 AS totalVacantDuration
		FROM (
		-- 车位有使用记录的情况
		SELECT
		place,
		start,
		end
		FROM place_userecord
		WHERE map = 178
		AND (
		(start >= '2024-09-30 00:00:00' AND start <= '2024-10-07 23:59:59')
		OR (end >= '2024-09-30 00:00:00' AND end <= '2024-10-07 23:59:59')
		OR (start <= '2024-09-30 00:00:00' AND end >= '2024-10-07 23:59:59')
		)

		UNION ALL

		-- 没有使用记录的车位
		SELECT
		pp.id AS place,
		'2024-09-30 00:00:00' AS start,
		'2024-10-07 23:59:59' AS end
		FROM parking_place pp
		LEFT JOIN place_userecord pur ON pp.id = pur.place
		WHERE pur.place IS NULL
		AND pp.map = 178
		) AS records
		GROUP BY place
		ORDER BY totalVacantDuration DESC
		LIMIT 10;








