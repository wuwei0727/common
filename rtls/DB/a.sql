DROP PROCEDURE IF EXISTS insert_use_75;

DELIMITER //
CREATE PROCEDURE insert_use_75 ( IN num INT, IN startTime DATETIME, IN endTime DATETIME )
BEGIN
    DECLARE i INT DEFAULT 1;

    myloop: WHILE i <= num DO
            SET @randomTime = DATE_ADD(startTime, INTERVAL FLOOR(RAND() * TIMESTAMPDIFF(MINUTE, startTime, endTime)) MINUTE);

            IF @randomTime > endTime THEN -- 如果随机时间大于结束时间，跳出循环
                LEAVE myloop;
            END IF;

            INSERT INTO `place_userecord` (`id`, `map`, `place`, `license`, `start`)
            VALUES (NULL, 另一个表的id, 另一个表的id, NULL, @randomTime);

            SET i = i + 1;
        END WHILE myloop;
END //
DELIMITER
-- 上面的先运行,在运行下面的，第一个参数是插入数据，第二个是开始时间到结束时间之间的随机时间
CALL insert_use_75(10,'2023-05-23 14:00:00','2023-05-23 16:10:00')

#请给我2023-05-31 12:40:24到2023-05-31 14:12:18之间的假数据至少30条

2023-05-15 15:58:24
2023-05-15 16:02:29
2023-05-15 15:53:06
2023-05-15 15:44:51
2023-05-15 15:52:32
2023-05-15 15:59:38
2023-05-15 16:04:50
2023-05-15 15:55:09
2023-05-15 15:46:06
2023-05-15 15:48:14
2023-05-15 15:47:07
2023-05-15 15:57:21
2023-05-15 16:01:37
2023-05-15 15:49:39
2023-05-15 15:56:12
2023-05-15 15:43:55
2023-05-15 16:12:41
2023-05-15 15:50:26
2023-05-15 15:51:18
2023-05-15 15:54:45
2023-05-15 15:45:11
2023-05-15 16:08:51
2023-05-15 15:42:28
2023-05-15 15:59:09
2023-05-15 15:43:00
2023-05-15 16:08:02
2023-05-15 15:48:42
2023-05-15 16:02:46
2023-05-15 16:08:11
2023-05-15 15:46:52


# 12到14点
2023-05-31 12:45:36
2023-05-31 12:50:27
2023-05-31 12:55:18
2023-05-31 13:00:09
2023-05-31 13:05:00
2023-05-31 13:09:51
2023-05-31 13:14:42
2023-05-31 13:19:33
2023-05-31 13:24:24
2023-05-31 13:29:15
2023-05-31 13:34:06
2023-05-31 13:38:57
2023-05-31 13:43:48
2023-05-31 13:48:39
2023-05-31 13:53:30
2023-05-31 13:58:21
2023-05-31 14:03:12
2023-05-31 14:08:03
2023-05-31 14:12:18
2023-05-31 12:41:12
2023-05-31 12:51:24
2023-05-31 13:01:36
2023-05-31 13:11:48
2023-05-31 13:22:00
2023-05-31 13:32:12
2023-05-31 13:42:24
2023-05-31 13:52:36
2023-05-31 14:02:48
2023-05-31 12:43:00
2023-05-31 14:10:06
