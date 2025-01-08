package com.tgy.rtls.data.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.mapper.user.SysUserMapMapper;
import com.tgy.rtls.data.mapper.user.SysUserMapper;
import com.tgy.rtls.data.service.user.SysUserService;
import com.tgy.rtls.data.tool.IpInf;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @description 针对表【sys_user(SmallApp用户信息表)】的数据库操作Service实现
 * @createDate 2022-07-21 20:58:41
 */
@Service
//@Transactional
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserMapMapper sysUserMapMapper;
    @Autowired
    private LocalUtil localUtil;
    /**
     * 加密类型
     */
    @Value("${rtls.shiro.algorithmName}")
    private String algorithmName;

    /**
     * 加密次数
     */
    @Value("${rtls.shiro.hashIterations}")
    private Integer hashIterations;
    @Value("${rtls.shiro.salt}")
    private String salt;

    /**
     * @param keyword 关键字
     * @param desc    排序
     * @return
     */
    @Override
    public List<SysUser> getAllUsers(String keyword, Integer enabled, String desc) {
        return sysUserMapper.getAllUsers(keyword, enabled, desc);

    }

    @Override
    public List<SysUser> getAllUsers2(String keyword, Integer enable, Integer createuId, String desc) {
        return sysUserMapper.getAllUsers2(keyword, enable, createuId, desc);

    }

    /**
     * @param userName 用户名
     * @return
     */
    @Override
    public SysUser findUserName(String userName) {
        return sysUserMapper.findUserName(userName);
    }

    @Override
    public int removeByIds(String[] ids) {
        if (!NullUtils.isEmpty(ids)) {
//            for (String userId : ids) {
            sysUserMapMapper.deleteByUserId((ids));
//            }
        }
        return sysUserMapper.removeByIds(ids);
    }

    @Override
    public SysUser queryUser(String userName) {
        return sysUserMapper.getUserName(userName);
    }

    /*
     * 新增smallApp登录记录
     * */
    @Override
    public boolean addLonginRecord(String userName, String ip) {
        try {
            LoginRecord login = new LoginRecord();
            InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址
            //String  = address.getHostAddress();
            login.setIp(ip);

            String res = IpInf.sendGet(IpInf.getIpLocationInf(ip));
            JSONObject json = JSONObject.fromObject(res);
            //IpLocation ipLocation=(IpLocation)JSONObject.toBean(json, IpLocation.class);
            // login.setAddress(ipLocation.getProvince()+","+ipLocation.getCity());
            String city = json.getString("city");
            String Province = json.getString("province");
            if (city.equals("[]")) {
                city = "";
            }
            login.setAddress(ip + "," + Province + " " + city);
            login.setUserName(userName);
            return sysUserMapper.addLonginRecord(login) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Map_2d> getUserByIdMap(Integer userId) {
        return sysUserMapper.getUserByIdMap(userId);
    }

    @Override
    public List<Map_2d> getUserByIdMap1(Integer map) {
        return sysUserMapper.getUserByIdMap1(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setUserPermiss(List<Integer> permissionIds, Integer uid) {
        try {
            sysUserMapper.delUserPermiss(uid);
            return sysUserMapper.insertUserPermiss(permissionIds, uid);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }

    @Override
    public Set<String> findByUserId(Integer userId) {
        return sysUserMapper.findByUserId(userId, localUtil.getLocale());
    }

    @Override
    public boolean updateAddTime(Integer userId, String loginTime) {
        return sysUserMapper.updateAddTime(userId, loginTime) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserMap(Integer userId, String mapid) {
        try {
            return sysUserMapper.insertUserMap(userId, mapid);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }

    @Override
    public SysUser getByUserId(Integer userId) {
        SysUser sysUser = sysUserMapper.getByUserId(userId);
        List<Map_2d> mapName = sysUserMapper.getMapName(userId);
        sysUser.setMapList(mapName);
        return sysUser;
    }

    @Override
    public boolean saveAppUserMap(SysUser sysUser) {
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        sysUser.setCreateuId(member.getUid());
        sysUser.setCreatedTime(new Date());
        sysUser.setRawPassword(sysUser.getPassword());
        sysUser.setPassword(new SimpleHash(algorithmName, sysUser.getPassword(), salt, hashIterations).toString());
        return sysUserMapper.saveAppUserMap(sysUser) > 0;
    }

    @Override
    public boolean updateByUserId(SysUser sysUser) {
        sysUser.setUpdatedTime(new Date());
        sysUser.setRawPassword(sysUser.getPassword());
        if(!NullUtils.isEmpty(sysUser.getPassword())&&!"".equals(sysUser.getPassword())){
            sysUser.setPassword(new SimpleHash(algorithmName, sysUser.getPassword(), salt, 16).toString());
        }
        return sysUserMapper.updateByUserId(sysUser) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delUserMap(List<String> mapIds, Integer userId) {
        try {
            //修改人员 先清空
            sysUserMapper.delUserMap(userId);
            //重新插入
            return sysUserMapper.addUserMap(mapIds, userId);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }

    @Override
    public List<SysUser> getCreateByuid(Integer userId) {
        return sysUserMapper.getCreateByuid(userId);
    }
}




