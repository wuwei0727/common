<!--        SELECT p.name AS placeName,p.map,-->
<!--        &lt;!&ndash;统计数量&ndash;&gt;-->
<!--        IF(#{mapId} = 178,-->
<!--           (SELECT COUNT(*) FROM parking_place p WHERE p.map = #{mapId}),-->
<!--           COUNT(DISTINCT p.id)-->
<!--            ) AS total_places,-->

<!--        &lt;!&ndash;总空闲时长&ndash;&gt;-->
<!--        IF(#{mapId} = 178,-->
<!--           (SELECT COUNT(*) FROM parking_place p WHERE p.map = #{mapId}) *-->
<!--           TIMESTAMPDIFF(SECOND , DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW()),-->
<!--           COUNT(DISTINCT p.id) * TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--            ) AS totalVacantDurations,-->

<!--        &lt;!&ndash;占用时间&ndash;&gt;-->
<!--        SUM(CASE-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                    THEN TIMESTAMPDIFF(SECOND, ur.start, ur.end)-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                    THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                    THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                    THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), ur.end)-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                    THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                    THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                ELSE 0-->
<!--                END-->
<!--            ) AS occupied_time,-->

<!--        &lt;!&ndash;空闲时间&ndash;&gt;-->
<!--        IF(#{mapId} = 178,-->
<!--           (SELECT COUNT(*) FROM parking_place p WHERE p.map = #{mapId}) *-->
<!--           TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW()) - SUM(-->
<!--                CASE-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                THEN TIMESTAMPDIFF(SECOND, ur.start, ur.end)-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), ur.end)-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                ELSE 0-->
<!--                END-->
<!--                ),-->
<!--           (COUNT(DISTINCT p.id) * TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW()) - SUM(-->
<!--                   CASE-->
<!--                       WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                           THEN TIMESTAMPDIFF(SECOND, ur.start, ur.end)-->
<!--                       WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                           THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                       WHEN ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                           THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--                       WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW()-->
<!--                           THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), ur.end)-->
<!--                       WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &gt; NOW()-->
<!--                           THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                       WHEN ur.start &lt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end IS NULL-->
<!--                           THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL #{time} MONTH), NOW())-->
<!--                       ELSE 0-->
<!--                       END-->
<!--               ))-->
<!--            ) AS totalVacantDuration-->

<!--        FROM parking_place p-->
<!--                 LEFT JOIN place_userecord ur ON p.id = ur.place-->
<!--                 LEFT JOIN infrared i ON p.id = i.place-->
<!--                 LEFT JOIN map_2d m ON ur.map = m.id-->

<!--        WHERE ((ur.start &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.start &lt; NOW())-->
<!--           OR (ur.end &gt; DATE_SUB(NOW(), INTERVAL #{time} MONTH) AND ur.end &lt; NOW())-->
<!--           OR (ur.end IS NULL AND ur.start &lt; NOW())) and p.map=#{mapId}-->

<!--        GROUP BY p.name-->
<!--        ORDER BY totalVacantDuration DESC-->
<!--        LIMIT 10;-->


<!--        SELECT p.name AS placeName,-->
<!--        p.map,-->
<!--        IF(178 = 178,-->
<!--        (SELECT COUNT(*) FROM parking_place p WHERE p.map = 178),-->
<!--        COUNT(DISTINCT p.id)-->
<!--        )  AS total_places,-->

<!--        TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW())  AS totalVacantDurations,-->

<!--        SUM(CASE-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end < NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, ur.end)-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end > NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end IS NULL-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end < NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), ur.end)-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end > NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW())-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end IS NULL-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW())-->
<!--        ELSE 0-->
<!--        END-->
<!--        )  AS occupied_time,-->



<!--        (TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()) - SUM(-->
<!--        CASE-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end < NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, ur.end)-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end > NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--        WHEN ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end IS NULL-->
<!--        THEN TIMESTAMPDIFF(SECOND, ur.start, NOW())-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end < NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), ur.end)-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end > NOW()-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW())-->
<!--        WHEN ur.start < DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end IS NULL-->
<!--        THEN TIMESTAMPDIFF(SECOND, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW())-->
<!--        ELSE 0-->
<!--        END-->

<!--        ))/3600  AS totalVacantDuration-->

<!--        FROM parking_place p-->
<!--        LEFT JOIN place_userecord ur ON p.id = ur.place-->
<!--        LEFT JOIN infrared i ON p.id = i.place-->
<!--        LEFT JOIN map_2d m ON ur.map = m.id-->

<!--        WHERE ((ur.start > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.start < NOW())-->
<!--        OR (ur.end > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND ur.end < NOW())-->
<!--        OR (ur.end IS NULL AND ur.start < NOW()))-->
<!--        and p.map = 178-->

<!--        GROUP BY p.name-->
<!--        ORDER BY totalVacantDuration DESC-->
