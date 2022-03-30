package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.SysConfigDao;
import com.example.emos.wx.db.pojo.SysConfig;
import com.example.emos.wx.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author pearz
 */
@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync
public class EmosWxApiApplication {

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Autowired
    private MessageService messageService;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(one -> {
            String paramKey = one.getParamKey();
            paramKey = StrUtil.toCamelCase(paramKey);
            String paramValue = one.getParamValue();

            try {
                Field field = constants.getClass().getDeclaredField(paramKey);
                field.set(constants, paramValue);
            } catch (Exception e) {
                log.error("执行异常", e);
            }
        });

        new File(imageFolder).mkdirs();
    }

    /**
     * TODO 生成测试数据
    */
    //@Test
    //void contextLoad() {
    //    for (int i = 1; i <= 100; i++) {
    //        MessageEntity message = new MessageEntity();
    //        message.setUuid(IdUtil.simpleUUID());
    //        message.setSenderId(0);
    //        message.setSenderName("系统消息");
    //        message.setMsg("这是第" + i + "条测试消息");
    //        message.setSendTime(new Date());
    //        String id = messageService.insertMessage(message);
    //
    //        MessageRefEntity ref = new MessageRefEntity();
    //        ref.setMessageId(id);
    //        ref.setReceiverId(6);
    //        ref.setLastFlag(true);
    //        ref.setReadFlag(false);
    //        messageService.insertRef(ref);
    //    }
    //}
}
