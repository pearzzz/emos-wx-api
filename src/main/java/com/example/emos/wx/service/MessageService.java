package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 16:49 2022/3/28
 */
public interface MessageService {
    public String insertMessage(MessageEntity entity);
    public List<HashMap> searchMessageByPage(int userId, long start, int length);
    public HashMap searchMessageById(String id);
    public String insertRef(MessageRefEntity entity);
    public long searchUnreadCount(int userId);
    public long searchLastCount(int userId);
    public long updateUnreadMessage(String id);
    public long deleteMessageRefById(String id);
    public long deleteUserMessageRef(int userId);
}
