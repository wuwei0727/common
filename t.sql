<<<<<<< HEAD
# type 类型（1-14） 必须
# year 年：2024 必须
# month 月：12 必须
# numDays 天数：30 必须
# map: 地图id 不必须需要用得的表需要，有些不需要
# numbers 必须

DROP PROCEDURE IF EXISTS insertUsersTotal;
DELIMITER $

CREATE PROCEDURE insertUsersTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN visit_count INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE j INT;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE last_inserted_id INT;

    -- 插入 wechat_userinfo 表数据
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', LPAD(input_month, 2, '0'), '-', LPAD(random_day, 2, '0'), ' ', LPAD(FLOOR(RAND() * 24), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0')), '%Y-%m-%d %H:%i:%s');
            -- 如果超过当前时间就以当前时间为准
            IF STR_TO_DATE(random_date, '%Y-%m-%d %H:%i:%s') > NOW() THEN
            SET random_date = NOW();
            END IF;
            -- 插入 wechat_userinfo 表
            INSERT INTO wechat_userinfo(nickName, time, is_fake_data)
            VALUES ('微信用户', random_date, 1);

            -- 获取刚插入的用户ID
            SET last_inserted_id = LAST_INSERT_ID();

            -- 插入 map_monthactiveuserrecord 表数据
            SET j = 0;
            WHILE j < visit_count DO
                    INSERT INTO map_monthactiveuserrecord(map, userId, loginTime, is_fake_data)
                    VALUES (mapId, last_inserted_id, random_date, 1);
                    SET j = j + 1;
                END WHILE;

            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;

-- 调用存储过程
CALL insertUsersTotal(2024, 6, 05, 2, 75, 10);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertUserSearchTotal;

DELIMITER $
CREATE PROCEDURE insertUserSearchTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN result INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE logId INT;
    select id into logId from user_search_log where map = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into user_search_log_time(u_s_log_id,time,is_fake_data) values (logId,random_date,1);
            SET i = i + 1;
        END WHILE;
    update user_search_log
    set count=result
    where id=logId;
END$
DELIMITER ;
-- 调用存储过程
CALL insertUserSearchTotal(2023, 5,20,20,75,21);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertTop10Business;

DELIMITER $
CREATE PROCEDURE insertTop10Business(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN businessId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    DECLARE businessName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    select name into businessName from shangjia where id = businessId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into each_map_search_business(map,map_name,business_id,business_name,addTime,is_fake_data) values (mapId,mapName,businessId,businessName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertTop10Business(2023, 5,20,1,75,3);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertLocationShareTotal;

DELIMITER $
CREATE PROCEDURE insertLocationShareTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN result INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE logId INT;
    DECLARE mapName VARCHAR(255);
    select id into logId from location_sharing_log where map = mapId;
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into location_sharing_log_time(l_s_log_id,time,is_fake_data) values (logId,random_date,1);
            SET i = i + 1;
        END WHILE;
    update location_sharing_log
    set count=result
    where id=logId;
END$
DELIMITER ;
-- 调用存储过程
CALL insertLocationShareTotal(2023, 5,20,1,75,1);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceUseTotal;

DELIMITER $
CREATE PROCEDURE insertPlaceUseTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;

    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into parking_info_statistics_use(map,place,placeName,start_time,is_fake_data) values (mapId,placeId,placeName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceUseTotal(2023, 5,20,1,75,15483,'E001');

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceNavigationTotal;

DELIMITER $
CREATE PROCEDURE insertPlaceNavigationTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into user_active_select_place(map,map_name,place,place_name,addTime,`desc`,is_fake_data) values (mapId,mapName,placeId,placeName,random_date,'Fake',1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceNavigationTotal(2023, 5,20,1,75,15483,'E001');


-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceNavigationUseRate;

DELIMITER $
CREATE PROCEDURE insertPlaceNavigationUseRate(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into each_map_search_place(map,map_name,place,place_name,addTime,is_fake_data) values (mapId,mapName,placeId,placeName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceNavigationUseRate(2023, 5,20,1,75,15483,'E001');

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertReservationTotal;

DELIMITER $
CREATE PROCEDURE insertReservationTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255),
    IN license varchar(255),
    IN reservationPerson varchar(255),
    IN phone varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE end_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            SET end_date = DATE_ADD(random_date, INTERVAL 1 HOUR);
            insert into vip_parking(map,place,name,license,reservation_person,phone,duration,start_time,end_time,is_fake_data)
            values (mapId,placeId,placeName,license,reservationPerson,phone,1,random_date,end_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertReservationTotal(2023, 5,20,1,75,15483,'E001','粤111111','张','18674122856');


-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertReverseCarSearchTotal;

DELIMITER $
CREATE PROCEDURE insertReverseCarSearchTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255),
    IN userId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into parking_info_statistics_findcar(user_id,map,place,placeName,start_time,timestamp,is_fake_data)
            values (userId,mapId,placeId,placeName,now(),random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertReverseCarSearchTotal(2023, 5,20,5.0,75,15483,'E001',1);
-- --------------------------------------------------------------------------------------------------------------------

车位使用记录 随机当天相差小时
DROP PROCEDURE IF EXISTS insertPlaceUseRecordTotal;


DELIMITER //

CREATE PROCEDURE insertPlaceUseRecordTotal(
    IN input_year INT,
    IN input_month INT,
    IN input_day INT,
    IN number_of_records INT,
    IN hour_difference INT,
    IN mapId INT,
    IN placeId INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE start_time DATETIME;
    DECLARE end_time DATETIME;

    WHILE i <= number_of_records DO
            SET start_time = CONCAT(input_year, '-', LPAD(input_month, 2, '0'), '-', LPAD(input_day, 2, '0'), ' ', LPAD(FLOOR(RAND() * 24), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'));
            -- 如果超过当前时间就以当前时间为准
            IF STR_TO_DATE(start_time, '%Y-%m-%d %H:%i:%s') > NOW() THEN
                SET start_time = NOW();
            END IF;
            SET end_time = DATE_ADD(start_time, INTERVAL hour_difference HOUR);

            -- 确保结束时间仍然在同一天内
            IF DATE(end_time) = DATE(start_time) THEN
                INSERT INTO place_userecord(map, place, start, end, is_fake_data)
                VALUES (mapId, placeId, start_time, end_time, 1);
                SET i = i + 1;
            END IF;
        END WHILE;
END //

DELIMITER ;
-- --------------------------------------------------------------------------------------------------------------------------------------
根据用户id添加访问次数


DELIMITER $
CREATE PROCEDURE insertUserVisitCount(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN userId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into map_monthactiveuserrecord(userid,map,loginTime,is_fake_data) values (userId,mapId,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertUserVisitCount(2023, 5,20,1,75,19);


SELECT
        ((UNIX_TIMESTAMP(NOW(3)) * 1000 - 1577836800000) << 22) |
        (FLOOR(RAND() * 1024) << 12) |
        FLOOR(RAND() * 4096) AS snowflake_id;


=======
# type 类型（1-14） 必须
# year 年：2024 必须
# month 月：12 必须
# numDays 天数：30 必须
# map: 地图id 不必须需要用得的表需要，有些不需要
# numbers 必须

DROP PROCEDURE IF EXISTS insertUsersTotal;
DELIMITER $

CREATE PROCEDURE insertUsersTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN visit_count INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE j INT;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE last_inserted_id INT;

    -- 插入 wechat_userinfo 表数据
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', LPAD(input_month, 2, '0'), '-', LPAD(random_day, 2, '0'), ' ', LPAD(FLOOR(RAND() * 24), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0')), '%Y-%m-%d %H:%i:%s');
            -- 如果超过当前时间就以当前时间为准
            IF STR_TO_DATE(random_date, '%Y-%m-%d %H:%i:%s') > NOW() THEN
            SET random_date = NOW();
            END IF;
            -- 插入 wechat_userinfo 表
            INSERT INTO wechat_userinfo(nickName, time, is_fake_data)
            VALUES ('微信用户', random_date, 1);

            -- 获取刚插入的用户ID
            SET last_inserted_id = LAST_INSERT_ID();

            -- 插入 map_monthactiveuserrecord 表数据
            SET j = 0;
            WHILE j < visit_count DO
                    INSERT INTO map_monthactiveuserrecord(map, userId, loginTime, is_fake_data)
                    VALUES (mapId, last_inserted_id, random_date, 1);
                    SET j = j + 1;
                END WHILE;

            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;

-- 调用存储过程
CALL insertUsersTotal(2024, 6, 05, 2, 75, 10);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertUserSearchTotal;

DELIMITER $
CREATE PROCEDURE insertUserSearchTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN result INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE logId INT;
    select id into logId from user_search_log where map = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into user_search_log_time(u_s_log_id,time,is_fake_data) values (logId,random_date,1);
            SET i = i + 1;
        END WHILE;
    update user_search_log
    set count=result
    where id=logId;
END$
DELIMITER ;
-- 调用存储过程
CALL insertUserSearchTotal(2023, 5,20,20,75,21);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertTop10Business;

DELIMITER $
CREATE PROCEDURE insertTop10Business(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN businessId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    DECLARE businessName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    select name into businessName from shangjia where id = businessId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into each_map_search_business(map,map_name,business_id,business_name,addTime,is_fake_data) values (mapId,mapName,businessId,businessName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertTop10Business(2023, 5,20,1,75,3);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertLocationShareTotal;

DELIMITER $
CREATE PROCEDURE insertLocationShareTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN result INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE logId INT;
    DECLARE mapName VARCHAR(255);
    select id into logId from location_sharing_log where map = mapId;
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into location_sharing_log_time(l_s_log_id,time,is_fake_data) values (logId,random_date,1);
            SET i = i + 1;
        END WHILE;
    update location_sharing_log
    set count=result
    where id=logId;
END$
DELIMITER ;
-- 调用存储过程
CALL insertLocationShareTotal(2023, 5,20,1,75,1);

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceUseTotal;

DELIMITER $
CREATE PROCEDURE insertPlaceUseTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;

    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into parking_info_statistics_use(map,place,placeName,start_time,is_fake_data) values (mapId,placeId,placeName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceUseTotal(2023, 5,20,1,75,15483,'E001');

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceNavigationTotal;

DELIMITER $
CREATE PROCEDURE insertPlaceNavigationTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into user_active_select_place(map,map_name,place,place_name,addTime,`desc`,is_fake_data) values (mapId,mapName,placeId,placeName,random_date,'Fake',1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceNavigationTotal(2023, 5,20,1,75,15483,'E001');


-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertPlaceNavigationUseRate;

DELIMITER $
CREATE PROCEDURE insertPlaceNavigationUseRate(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE mapName VARCHAR(255);
    select name into mapName from map_2d where id = mapId;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into each_map_search_place(map,map_name,place,place_name,addTime,is_fake_data) values (mapId,mapName,placeId,placeName,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertPlaceNavigationUseRate(2023, 5,20,1,75,15483,'E001');

-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertReservationTotal;

DELIMITER $
CREATE PROCEDURE insertReservationTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255),
    IN license varchar(255),
    IN reservationPerson varchar(255),
    IN phone varchar(255)

)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    DECLARE end_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            SET end_date = DATE_ADD(random_date, INTERVAL 1 HOUR);
            insert into vip_parking(map,place,name,license,reservation_person,phone,duration,start_time,end_time,is_fake_data)
            values (mapId,placeId,placeName,license,reservationPerson,phone,1,random_date,end_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertReservationTotal(2023, 5,20,1,75,15483,'E001','粤111111','张','18674122856');


-- --------------------------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insertReverseCarSearchTotal;

DELIMITER $
CREATE PROCEDURE insertReverseCarSearchTotal(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN placeId INT,
    IN placeName varchar(255),
    IN userId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into parking_info_statistics_findcar(user_id,map,place,placeName,start_time,timestamp,is_fake_data)
            values (userId,mapId,placeId,placeName,now(),random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertReverseCarSearchTotal(2023, 5,20,5.0,75,15483,'E001',1);
-- --------------------------------------------------------------------------------------------------------------------

车位使用记录 随机当天相差小时
DROP PROCEDURE IF EXISTS insertPlaceUseRecordTotal;


DELIMITER //

CREATE PROCEDURE insertPlaceUseRecordTotal(
    IN input_year INT,
    IN input_month INT,
    IN input_day INT,
    IN number_of_records INT,
    IN hour_difference INT,
    IN mapId INT,
    IN placeId INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE start_time DATETIME;
    DECLARE end_time DATETIME;

    WHILE i <= number_of_records DO
            SET start_time = CONCAT(input_year, '-', LPAD(input_month, 2, '0'), '-', LPAD(input_day, 2, '0'), ' ', LPAD(FLOOR(RAND() * 24), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'), ':', LPAD(FLOOR(RAND() * 60), 2, '0'));
            -- 如果超过当前时间就以当前时间为准
            IF STR_TO_DATE(start_time, '%Y-%m-%d %H:%i:%s') > NOW() THEN
                SET start_time = NOW();
            END IF;
            SET end_time = DATE_ADD(start_time, INTERVAL hour_difference HOUR);

            -- 确保结束时间仍然在同一天内
            IF DATE(end_time) = DATE(start_time) THEN
                INSERT INTO place_userecord(map, place, start, end, is_fake_data)
                VALUES (mapId, placeId, start_time, end_time, 1);
                SET i = i + 1;
            END IF;
        END WHILE;
END //

DELIMITER ;
-- --------------------------------------------------------------------------------------------------------------------------------------
根据用户id添加访问次数


DELIMITER $
CREATE PROCEDURE insertUserVisitCount(
    IN input_year INT,
    IN input_month INT,
    IN days_in_month INT,
    IN number_of_records INT,
    IN mapId INT,
    IN userId INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_date DATETIME;
    WHILE i < number_of_records DO
            SET random_day = FLOOR(1 + (RAND() * days_in_month));
            SET random_date = STR_TO_DATE(CONCAT(input_year, '-', input_month, '-', random_day, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60)), '%Y-%m-%d %H:%i:%s');
            insert into map_monthactiveuserrecord(userid,map,loginTime,is_fake_data) values (userId,mapId,random_date,1);
            SET i = i + 1;
        END WHILE;
END$
DELIMITER ;
-- 调用存储过程
CALL insertUserVisitCount(2023, 5,20,1,75,19);


SELECT
        ((UNIX_TIMESTAMP(NOW(3)) * 1000 - 1577836800000) << 22) |
        (FLOOR(RAND() * 1024) << 12) |
        FLOOR(RAND() * 4096) AS snowflake_id;


>>>>>>> a52e230533672924aa45f4b18aba0c7a14438dd8
