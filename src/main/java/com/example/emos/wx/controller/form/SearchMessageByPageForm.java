package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 17:23 2022/3/28
 */
@Data
@ApiModel
public class SearchMessageByPageForm {
    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Range(min = 1, max = 40)
    private Integer length;
}
