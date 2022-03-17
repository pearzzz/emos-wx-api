package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 14:09 2022/3/17
 */

@ApiModel
@Data
public class TestSayHelloForm {

    @NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")
    @ApiModelProperty("姓名")
    private String name;
}
