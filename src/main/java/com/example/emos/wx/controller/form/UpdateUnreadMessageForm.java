package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 18:40 2022/3/28
 */
@Data
@ApiModel
public class UpdateUnreadMessageForm {
    @NotBlank
    private String id;
}
