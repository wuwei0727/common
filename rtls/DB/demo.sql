INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (5, 1, '权限管理', '0', '5/', 'auth:view', '/member(company)/**', 'auth:view', 'auth:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (11, 1, '车位规划', '0', '11/', 'park:getPlace', 'park:getPlace', 'park:getPlace', 'park:getPlace');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (12, 1, '添加车位规划', '11', '11/12', 'park:add', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (13, 1, '删除车位规划', '11', '11/13', 'park:del', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (14, 1, '编辑车位规划', '11', '11/14', 'park:edit', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (15, 1, '查看车位规划', '11', '11/15', 'park:see', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (21, 1, '公司管理', '0', '21/', 'company:getCompany', 'company:getCompany', 'company:getCompany',
        'company:getCompany');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (22, 1, '添加公司', '21', '21/22', 'company:add', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (23, 1, '删除公司', '21', '21/23', 'company:del', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (24, 1, '编辑公司', '21', '21/24', 'company:edit', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (25, 1, '查看公司', '21', '21/25', 'company:see', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (31, 1, '通道违停', '0', '31', 'violate:getWeiTing', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (32, 1, '添加通道违停', '31', '31/32', 'violate:add', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (33, 1, '删除通道违停', '31', '31/33', 'violate:del', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (34, 1, '编辑通道违停', '31', '31/34', 'violate:edit', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (35, 1, '查看通道违停', '31', '31/35', 'violate:see', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (41, 1, '商家信息', '0', '41/', 'business:getShangjia', 'warn/**', 'warn:view', 'warn:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (42, 1, '添加商家', '41', '41/42', 'business:add', 'park:addShangjia', 'park:addShangjia', 'park:addShangjia');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (43, 1, '删除商家', '41', '41/43', 'business:del', 'record/**', 'record:view', 'record:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (44, 1, '编辑商家', '41', '41/44', 'business:edit', 'warn/updateWarnRuleType', 'warnrule:update', 'warnrule:update');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (45, 1, '查看商家', '41', '41/45', 'business:see', 'warn/getWarnRecordSel', 'warn:sel', 'warn:sel');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (51, 1, '蓝牙信标', '0', '51/', 'sub:getSubSel', 'company/addCompany', 'company:view', 'company:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (52, 1, '添加蓝牙信标', '51', '51/52', 'sub:add', 'company/addCompany', 'company:view', 'company:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (53, 1, '删除蓝牙信标', '51', '51/53', 'sub:del', 'company/addCompany', 'company:view', 'company:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (54, 1, '编辑蓝牙信标', '51', '51/54', 'sub:edit', 'company/addCompany', 'company:view', 'company:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (55, 1, '查看蓝牙信标', '51', '51/55', 'sub:see', 'company/addCompany', 'company:view', 'company:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (56, 1, '网关', '0', '56/', 'gateway:getGatewaySel', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (57, 1, '添加网关', '56', '56/57', 'gateway:add', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (58, 1, '删除网关', '56', '56/58', 'gateway:del', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (59, 1, '编辑网关', '56', '56/59', 'gateway:edit', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (60, 1, '查看网关', '56', '56/60', 'gateway:see', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (61, 1, '车位检测器', '0', '61/', 'infrared:getInfrared', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (62, 1, '添加车位检测器', '61', '61/62', 'infrared:add', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (63, 1, '删除车位检测器', '61', '61/63', 'infrared:del', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (64, 1, '编辑车位检测器', '61', '61/64', 'infrared:edit', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (65, 1, '查看车位检测器', '61', '61/65', 'infrared:see', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (66, 1, '地图管理', '0', '66/', 'map:getMap2dSel', 'map/**', 'map:view', 'map:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (67, 1, '添加地图', '66', '66/67', 'map:add', 'map/addMap', 'map:add', 'map:add');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (68, 1, '删除地图', '66', '66/68', 'map:del', 'map/updateMap', 'map:update', 'map:update');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (69, 1, '编辑地图', '66', '66/69', 'map:edit', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (70, 1, '查看地图', '66', '66/70', 'map:see', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (81, 1, '人员账户管理', '0', '81/', 'member:getMemberSel', 'member:view', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (82, 1, '添加人员账户', '81', '81/82', 'member:add', 'map/addMap', 'map:add', 'map:add');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (83, 1, '删除人员账户', '81', '81/83', 'member:del', 'map/updateMap', 'map:update', 'map:update');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (84, 1, '编辑人员账户', '81', '81/84', 'member:edit', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (85, 1, '查看人员账户', '81', '81/85', 'member:see', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (86, 1, '账户权限管理', '0', '86/', 'company:getCompanySel', 'company:getCompanySel', 'ew',
        'compa');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (87, 1, '添加账户权限', '86', '86/87', 'company:add', 'map/addMap', 'map:add', 'map:add');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (88, 1, '删除账户权限', '86', '86/88', 'company:del', 'map/updateMap', 'map:update', 'map:update');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (89, 1, '编辑账户权限', '86', '86/89', 'company:edit', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (90, 1, '查看账户权限', '86', '86/90', 'company:see', 'map/delMap', 'map:del', 'map:del');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (91, 1, '登录日志', '0', '91/', 'loginLog:view', 'member:view', '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (92, 1, '统计数据', '0', '92/', 'overview:view', 'overview:view', 'overview:view', 'overview:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (93, 1, '数据中心统计数据', '92', '92/93', 'overview:view', 'overview:view', 'overview:view', 'overview:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (94, 1, '地图绑定管理', '0', '94/', 'mapbinding:index', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (95, 1, '地图绑定', '94', '94/95', 'map:binding', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (96, 1, '地图解绑', '94', '94/95', 'map:unbind', NULL, '', '');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (97, 1, '商家管理', '0', '97/', 'shangjia:view', NULL, 'shangjia:view', 'shangjia:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (98, 1, '车场管理', '0', '98/', 'place:view', 'place:view', 'place:view', 'place:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (99, 1, '设备管理', '0', '99/', 'device:view', 'device:view', 'device:view', 'device:view');
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (100, 1, '用户管理', '0', '100/', 'member:view', 'member:view', 'member:view', 'member:view');
-- 人员账户
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (101, 1, '人员权限设置', '81', '81/101', 'member:permiss', 'member/permiss', 'member:permiss', 'member:permiss');
-- 账户权限
INSERT INTO `park`.`permission` (`id`, `enabled`, `name`, `parentid`, `parentids`, `permission`, `url`, `name_en`,
                                 `name_ko`)
VALUES (102, 1, '账户权限', '86', '86/102', 'company:permiss', 'company/permiss', 'company:permiss', 'company:permiss');



