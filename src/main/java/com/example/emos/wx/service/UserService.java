package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;

import java.util.HashMap;
import java.util.Set;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 11:00 2022/3/21
 */
public interface UserService {
    public int registerUser(String registerCode, String code, String nickname, String photo);

    public Set<String> searchUserPermissions(int userId);

    public Integer login(String code);

    public TbUser searchById(int id);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);
}
