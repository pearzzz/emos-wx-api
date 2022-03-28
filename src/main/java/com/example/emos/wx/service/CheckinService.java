package com.example.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 19:04 2022/3/22
 */
public interface CheckinService {
    public String validCanCheckin(int userId, String date);

    public void checkin(HashMap param);

    public void createFaceModel(int userId, String path);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    public ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
