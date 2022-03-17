package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 10:16 2022/3/17
 */

@RestController
@RequestMapping("/test")
@Api("测试Web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("easist tset method")
    public R sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return R.ok().put("message", "Hello," + form.getName());
    }
}
