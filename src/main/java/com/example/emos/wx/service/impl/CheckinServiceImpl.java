package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.*;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 19:05 2022/3/22
 */
@Service
@Slf4j
@Scope("prototype")
public class CheckinServiceImpl implements CheckinService {
    @Value("${emos.code}")
    private String code;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Autowired
    private TbCheckinDao tbCheckinDao;

    @Autowired
    private TbWorkdayDao tbWorkdayDao;

    @Autowired
    private TbHolidaysDao tbHolidaysDao;

    @Autowired
    private SystemConstants constants;

    @Override
    public String validCanCheckin(int userId, String date) {
        boolean isHoliday = tbHolidaysDao.searchTodayIsHoliday() != null;
        boolean isWorkday = tbWorkdayDao.searchTodayIsWorkday() != null;
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (isWorkday) {
            type = "工作日";
        } else if (isHoliday) {
            type = "节假日";
        }

        if ("节假日".equals(type)) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.getAttendanceEndTime();
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)) {
                return "没到上班考勤开始时间";
            } else if (now.isAfter(attendanceEnd)) {
                return "超过了上班考勤结束时间";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("start", start);
                map.put("end", end);
                map.put("date", date);
                boolean bool = tbCheckinDao.haveCheckin(map) != null;
                return bool ? "今日已考勤，无需重复考勤" : "可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {
        DateTime d1 = DateUtil.date();
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        DateTime d3 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
        int status = 1;
        if (d1.compareTo(d2) < 0) {
            status = 1;
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {
            status = 2;
        } else {
            throw new EmosException("超出考勤时间，无法考勤");
        }

        int userId = (Integer) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            //throw new EmosException("不存在人脸模型");
            //查询疫情风险等级
            int risk = 1;
            String address = (String) param.get("address");
            String country = (String) param.get("country");
            String province = (String) param.get("province");
            String city = (String) param.get("city");
            String district = (String) param.get("district");

            if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                String code = cityDao.searchCode(city);
                String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements elements = document.getElementsByClass("list-content");
                    if (elements.size() > 0) {
                        Element element = elements.get(0);
                        String result = element.select("p:last-child").text();

                        //result = "高风险";

                        if ("高风险".equals(result)) {
                            risk = 3;
                            //发送告警邮件
                            HashMap<String, String> map = userDao.searchNameAndDept(userId);
                            String name = map.get("name");
                            String deptName = map.get("dept_name");
                            deptName = deptName != null ? deptName : "";
                            SimpleMailMessage message = new SimpleMailMessage();
                            message.setTo(hrEmail);
                            message.setSubject("员工" + name + "身处高风险疫情地区警告");
                            message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                            emailTask.sendAsync(message);
                        } else if ("中风险".equals(result)) {
                            risk = 2;
                        }
                    }
                } catch (IOException e) {
                    log.error("执行异常", e);
                    throw new EmosException("获取风险等级失败");
                }
            }
            //保存签到记录
            TbCheckin checkin = new TbCheckin();
            checkin.setUserId(userId);
            checkin.setAddress(address);
            checkin.setCountry(country);
            checkin.setProvince(province);
            checkin.setCity(city);
            checkin.setDistrict(district);
            checkin.setStatus((byte) status);
            checkin.setRisk(risk);
            checkin.setDate(DateUtil.today());
            checkin.setCreateTime(d1);
            tbCheckinDao.insert(checkin);
        } else {
            String path = (String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常");
            }
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException(body);
            } else if ("False".equals(body)) {
                throw new EmosException("签到无效，非本人签到");
            } else if ("True".equals(body)) {
                //查询疫情风险等级
                int risk = 1;
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                String city = (String) param.get("city");
                String district = (String) param.get("district");

                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                    String code = cityDao.searchCode(city);
                    String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                    try {
                        Document document = Jsoup.connect(url).get();
                        Elements elements = document.getElementsByClass("list-content");
                        if (elements.size() > 0) {
                            Element element = elements.get(0);
                            String result = element.select("p:last-child").text();
                            if ("高风险".equals(result)) {
                                risk = 3;
                                //发送告警邮件
                                HashMap<String, String> map = userDao.searchNameAndDept(userId);
                                String name = map.get("name");
                                String deptName = map.get("dept_name");
                                deptName = deptName != null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");
                                message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                emailTask.sendAsync(message);
                            } else if ("中风险".equals(result)) {
                                risk = 2;
                            }
                        }
                    } catch (IOException e) {
                        log.error("执行异常", e);
                        throw new EmosException("获取风险等级失败");
                    }
                }
                //保存签到记录
                TbCheckin checkin = new TbCheckin();
                checkin.setUserId(userId);
                checkin.setAddress(address);
                checkin.setCountry(country);
                checkin.setProvince(province);
                checkin.setCity(city);
                checkin.setDistrict(district);
                checkin.setStatus((byte) status);
                checkin.setRisk(risk);
                checkin.setDate(DateUtil.today());
                checkin.setCreateTime(d1);
                tbCheckinDao.insert(checkin);
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code", code);
        HttpResponse response = request.execute();
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException(body);
        } else {
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelDao.insert(entity);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = tbCheckinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long checkinDays = tbCheckinDao.searchCheckinDays(userId);
        return checkinDays;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = tbCheckinDao.searchWeekCheckin(param);
        ArrayList holidaysList = tbHolidaysDao.searchHolidaysInRange(param);
        ArrayList workdayList = tbWorkdayDao.searchWorkdayInRange(param);
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list = new ArrayList<>();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            //判断one这一天是节假日还是工作日
            String type = "工作日";
            if (one.isWeekend()) {
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {
                type = "工作日";
            }
            String status = "";

            //如果one这一天是工作日，并且在今天之前（未来的日子不能算缺勤或者其他状态）
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "缺勤";

                //如果one是今天，但是还未到考勤结束时间，也不能认定其为缺勤（因为有之后某个时刻打卡的可能）
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
                String today = DateUtil.today();
                if (date.equals(today) && DateUtil.date().isBefore(endTime)) {
                    status = "";
                }

                //看打卡列表checkinList里是否包含有one这一天的考勤记录，如果有则获取其考勤状态status
                for (HashMap<String, String> map : checkinList) {
                    if (map.containsValue(date)) {
                        status = map.get("status");
                        break;
                    }
                }
            }
            HashMap map = new HashMap();
            map.put("date", date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return searchWeekCheckin(param);
    }
}
