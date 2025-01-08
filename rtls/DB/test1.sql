INSERT INTO `permission` (`id`, `enabled`, `name`, `parentid`, `permission`)
VALUES (null, 1, 'AI智能摄像机管理', '0', ''),
       (null, 1, '添加AI智能摄像机管理', '', 'cac:add'),
       (null, 1, '删除AI智能摄像机管理', '', 'cac:del'),
       (null, 1, '编辑AI智能摄像机管理', '', 'cac:edit'),
       (null, 1, '查看AI智能摄像机管理', '', 'cac:see');

INSERT INTO sys_company_permission(company_id, permission_id)
VALUES (1,212),
       (1,213),
       (1,214),
       (1,215);


INSERT INTO sys_member_permission(uid, permission_id)
VALUES (1,212),
       (1,213),
       (1,214),
       (1,215)