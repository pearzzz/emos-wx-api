package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 16:31 2022/3/28
 */
@Data
@Document(collection = "message")
public class MessageEntity {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private Integer senderId;

    private String senderPhoto = "https://emos-wx-1310426559.cos.ap-beijing.myqcloud.com/header/robot.jpg";

    private String senderName;

    private String msg;

    @Indexed
    private Date sendTime;
}
