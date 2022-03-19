package com.example.emos.wx.aop;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shrio.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 10:32 2022/3/19
 */

@Aspect
@Component
public class TokenAspect {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //方法执行结果
        R r = (R) point.proceed();
        ////如果ThreadLocal中存在Token，说明是更新的Token
        String token = threadLocalToken.getToken();
        if (token != null) {
            //往响应中放置Token
            r.put("token", token);
            threadLocalToken.clear();
        }

        return r;
    }
}
