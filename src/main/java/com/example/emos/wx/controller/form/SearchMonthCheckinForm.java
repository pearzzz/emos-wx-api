package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 13:44 2022/3/26
 */

@Data
@ApiModel
public class SearchMonthCheckinForm {
    @NotNull
    @Range(min = 2000, max = 2100)
    private Integer year;

    @NotNull
    @Range(min = 1, max = 12)
    private Integer month;
}
