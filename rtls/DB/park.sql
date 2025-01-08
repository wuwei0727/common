-- ----------------------------
-- 用户信息表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
create table sys_user (
                          user_id 			bigint(20) 		NOT NULL AUTO_INCREMENT   comment '用户ID',
                          user_name 		varchar(50) 	CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户昵称',
                          password 			varchar(50) 	CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
                          login_time timestamp NULL DEFAULT NULL COMMENT '登录时间',
                          createdTime timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updatedTime timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          primary key (user_id)
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'web端APP用户信息表' ROW_FORMAT = Compact;
-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1, 'admin', '123456','2022-07-27 13:14:20','2020-05-27 10:18:30','2021-05-27 10:18:30');
insert into sys_user values(2, '123456', '123456','2022-07-27 13:14:20','2020-05-27 10:18:30','2021-05-27 10:18:30');


-- --------------------------
-- 2、web端APP用户信息表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
create table sys_user (
                          user_id 			bigint(20) 		NOT NULL AUTO_INCREMENT   comment '用户ID',
                          user_name 		varchar(50) 	CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户昵称',
                          password 			varchar(50) 	CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
                          login_time timestamp NULL DEFAULT NULL COMMENT '登录时间',
                          createdTime timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updatedTime timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          primary key (user_id)
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'web端APP用户信息表' ROW_FORMAT = Compact;
-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1, 'admin', '123456','2022-07-27 13:14:20','2020-05-27 10:18:30','2021-05-27 10:18:30');
insert into sys_user values(2, '123456', '123456','2022-07-27 13:14:20','2020-05-27 10:18:30','2021-05-27 10:18:30');


-- ----------------------------
-- 2、用户地图表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_map`;
CREATE TABLE `sys_user_map`  (
                                 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                 `map_id`  bigint(20) NOT NULL COMMENT '地图ID',
                                 INDEX `user_id`(`user_id`) USING BTREE,
                                 PRIMARY KEY (`user_id`, `map_id`) USING BTREE
)ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户和地图关联表' ROW_FORMAT = Compact;

insert into sys_user_map values(1, 74);
insert into sys_user_map values(1, 75);
insert into sys_user_map values(1, 76);

-- 更新地图。 添加字段
ALTER TABLE map_2d ADD qrcode VARCHAR(100);
ALTER TABLE map_2d ADD qrcodelocal VARCHAR(100);



-- -------------
-- 权限表数据
-- -------------
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 5, 1, '权限管理', '0', '5/', 'auth:view', '/member(company)/**', 'auth:view', 'auth:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 11, 1, '车位规划', '0', '11/', 'park:getPlace', 'park:getPlace', 'park:getPlace', 'park:getPlace' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 12, 1, '添加车位规划', '11', '11/12', 'park:add', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 13, 1, '删除车位规划', '11', '11/13', 'park:del', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 14, 1, '编辑车位规划', '11', '11/14', 'park:edit', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 15, 1, '查看车位规划', '11', '11/15', 'park:see', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 21, 1, '公司管理', '0', '21/', 'company:getCompany', 'company:getCompany', 'company:getCompany', 'company:getCompany' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 22, 1, '添加公司', '21', '21/22', 'company:add', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 23, 1, '删除公司', '21', '21/23', 'company:del', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 24, 1, '编辑公司', '21', '21/24', 'company:edit', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 25, 1, '查看公司', '21', '21/25', 'company:see', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 31, 1, '通道违停', '0', '31', 'violate:getWeiTing', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 32, 1, '添加通道违停', '31', '31/32', 'violate:add', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 33, 1, '删除通道违停', '31', '31/33', 'violate:del', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 34, 1, '编辑通道违停', '31', '31/34', 'violate:edit', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 35, 1, '查看通道违停', '31', '31/35', 'violate:see', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 41, 1, '商家信息', '0', '41/', 'business:getShangjia', 'warn/**', 'warn:view', 'warn:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 42, 1, '添加商家', '41', '41/42', 'business:add', 'park:addShangjia', 'park:addShangjia', 'park:addShangjia' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 43, 1, '删除商家', '41', '41/43', 'business:del', 'record/**', 'record:view', 'record:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 44, 1, '编辑商家', '41', '41/44', 'business:edit', 'warn/updateWarnRuleType', 'warnrule:update', 'warnrule:update' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 45, 1, '查看商家', '41', '41/45', 'business:see', 'warn/getWarnRecordSel', 'warn:sel', 'warn:sel' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 51, 1, '蓝牙信标', '0', '51/', 'sub:getSubSel', 'company/addCompany', 'company:view', 'company:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 52, 1, '添加蓝牙信标', '51', '51/52', 'sub:add', 'company/addCompany', 'company:view', 'company:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 53, 1, '删除蓝牙信标', '51', '51/53', 'sub:del', 'company/addCompany', 'company:view', 'company:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 54, 1, '编辑蓝牙信标', '51', '51/54', 'sub:edit', 'company/addCompany', 'company:view', 'company:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 55, 1, '查看蓝牙信标', '51', '51/55', 'sub:see', 'company/addCompany', 'company:view', 'company:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 56, 1, '网关', '0', '56/', 'gateway:getGatewaySel', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 57, 1, '添加网关', '56', '56/57', 'gateway:add', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 58, 1, '删除网关', '56', '56/58', 'gateway:del', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 59, 1, '编辑网关', '56', '56/59', 'gateway:edit', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 60, 1, '查看网关', '56', '56/60', 'gateway:see', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 61, 1, '车位检测器', '0', '61/', 'infrared:getInfrared', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 62, 1, '添加车位检测器', '61', '61/62', 'infrared:add', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 63, 1, '删除车位检测器', '61', '61/63', 'infrared:del', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 64, 1, '编辑车位检测器', '61', '61/64', 'infrared:edit', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 65, 1, '查看车位检测器', '61', '61/65', 'infrared:see', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 66, 1, '地图管理', '0', '66/', 'map:getMap2dSel', 'map/**', 'map:view', 'map:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 67, 1, '添加地图', '66', '66/67', 'map:add', 'map/addMap', 'map:add', 'map:add' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 68, 1, '删除地图', '66', '66/68', 'map:del', 'map/updateMap', 'map:update', 'map:update' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 69, 1, '编辑地图', '66', '66/69', 'map:edit', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 70, 1, '查看地图', '66', '66/70', 'map:see', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 81, 1, '人员账户管理', '0', '81/', 'member:getMemberSel', 'member:view', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 82, 1, '添加人员账户', '81', '81/82', 'member:add', 'map/addMap', 'map:add', 'map:add' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 83, 1, '删除人员账户', '81', '81/83', 'member:del', 'map/updateMap', 'map:update', 'map:update' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 84, 1, '编辑人员账户', '81', '81/84', 'member:edit', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 85, 1, '查看人员账户', '81', '81/85', 'member:see', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 86, 1, '账户权限管理', '0', '86/', 'role:getCompanySel', 'company:getCompanySel', 'ew', 'compa' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 87, 1, '添加账户权限', '86', '86/87', 'role:add', 'map/addMap', 'map:add', 'map:add' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 88, 1, '删除账户权限', '86', '86/88', 'role:del', 'map/updateMap', 'map:update', 'map:update' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 89, 1, '编辑账户权限', '86', '86/89', 'role:edit', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 90, 1, '查看账户权限', '86', '86/90', 'role:see', 'map/delMap', 'map:del', 'map:del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 91, 1, '登录日志', '0', '91/', 'loginLog:view', 'member:view', '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 92, 1, '统计数据', '0', '92/', 'overview:view', 'overview:view', 'overview:view', 'overview:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 93, 1, '数据中心统计数据', '92', '92/93', 'overview:view', 'overview:view', 'overview:view', 'overview:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 94, 1, '地图绑定管理', '0', '94/', 'mapbinding:index', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 95, 1, '地图绑定', '94', '94/95', 'map:bindings', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 96, 1, '地图解绑', '94', '94/95', 'map:unbind', NULL, '', '' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 97, 1, '商家管理', '0', '97/', 'shangjia:view', NULL, 'shangjia:view', 'shangjia:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 98, 1, '车场管理', '0', '98/', 'place:view', 'place:view', 'place:view', 'place:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 99, 1, '设备管理', '0', '99/', 'device:view', 'device:view', 'device:view', 'device:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 100, 1, '用户管理', '0', '100/', 'member:view', 'member:view', 'member:view', 'member:view' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 101, 1, '人员权限设置', '81', '81/101', 'member:permiss', 'member/permiss', 'member:permiss', 'member:permiss' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 102, 1, '账户权限设置', '86', '86/102', 'role:permiss', 'company/permiss', 'company:permiss', 'company:permiss' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 103, 1, '小程序用户管理', '0', '103', 'appUser:idex', '', 'index', 'index' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 104, 1, '添加用户', '103', '103/104', 'appUser:add', NULL, 'add', 'add' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 105, 1, '删除用户', '103', '103/105', 'appUser:del', NULL, 'del', 'del' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 106, 1, '编辑用户', '103', '103/106', 'appUser:edit', NULL, 'edit', 'edit' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 107, 1, '查看用户', '103', '103/107', 'appUser:see', NULL, 'see', 'see' );
INSERT INTO `park`.`permission` ( `id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`, `name_ko` )
VALUES
( 108, 1, '用户权限设置', '103', '103/108', 'appUser:permiss', NULL, 'permiss', 'permiss' );


-- -----------------------
-- 当前用户可操作的权限
-- -----------------------
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 12);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 13);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 14);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 15);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 22);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 23);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 24);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 25);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 32);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 33);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 34);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 35);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 42);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 43);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 44);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 45);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 52);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 53);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 54);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 55);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 57);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 58);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 59);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 60);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 62);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 63);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 64);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 65);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 67);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 68);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 69);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 70);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 82);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 83);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 84);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 85);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 87);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 88);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 89);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 90);
# INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 93);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 95);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 96);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 101);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 102);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 104);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 105);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 106);
INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 107);
# INSERT INTO `park`.`sys_member_permission` (`uid`, `permission_id`) VALUES (1, 108);


-- ----------------
-- 权限设置的菜单
-- ----------------
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 12);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 13);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 14);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 15);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 22);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 23);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 24);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 25);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 32);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 33);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 34);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 35);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 42);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 43);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 44);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 45);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 52);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 53);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 54);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 55);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 57);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 58);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 59);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 60);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 62);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 63);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 64);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 65);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 67);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 68);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 69);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 70);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 82);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 83);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 84);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 85);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 87);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 88);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 89);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 90);
# INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 93);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 95);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 96);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 101);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 102);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 104);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 105);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 106);
INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 107);
# INSERT INTO `park`.`sys_company_permission` (`company_id`, `permission_id`) VALUES (1, 108);

