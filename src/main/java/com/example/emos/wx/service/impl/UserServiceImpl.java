package com.example.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MessageService;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 11:01 2022/3/21
 */

@Service
@Slf4j
@Scope("prototype")//因为用到ThreadLocal，所以必须加scope注解
public class UserServiceImpl implements UserService {
    @Value("${wx.app-id}")
    private String appId;
    @Value("${wx.app-secret}")
    private String appSecret;
    @Autowired
    private TbUserDao userDao;
    @Autowired
    private MessageTask messageTask;
    @Autowired
    private MessageService messageService;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }


    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        //如果邀请码是000000，代表是注册超级管理员
        if ("000000".equals(registerCode)) {
            boolean bool = userDao.haveRootUser();
            if (!bool) {
                String openId = getOpenId(code);
                HashMap param = new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userDao.insert(param);
                int id = userDao.searchIdByOpenId(openId);

                MessageEntity entity=new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎您注册成为超级管理员，请及时更新你的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id+"",entity);

                return id;
            } else {
                throw new EmosException("已存在超级管理员，无法绑定！");
            }
            //注册普通员工
        } else {
            return 0;
        }
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if (id == null) {
            throw new EmosException("账户不存在");
        }
        //TODO 从消息队列中接收消息，转移到消息表
        //messageTask.receiveAsync(id + "");

        //for (int i = 1; i <= 100; i++) {
        //    MessageEntity message = new MessageEntity();
        //    message.setUuid(IdUtil.simpleUUID());
        //    message.setSenderId(0);
        //    message.setSenderName("系统消息");
        //    message.setMsg("这是第" + i + "条测试消息");
        //    message.setSendTime(new Date());
        //    String idd = messageService.insertMessage(message);
        //
        //    MessageRefEntity ref = new MessageRefEntity();
        //    ref.setMessageId(idd);
        //    ref.setReceiverId(6);
        //    ref.setLastFlag(true);
        //    ref.setReadFlag(false);
        //    messageService.insertRef(ref);
        //}

        return id;
    }

    @Override
    public TbUser searchById(int id) {
        TbUser user = userDao.searchById(id);
        return user;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }
}
