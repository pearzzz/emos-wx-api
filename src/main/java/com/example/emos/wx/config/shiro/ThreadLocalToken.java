package com.example.emos.wx.config.shiro;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 15:01 2022/3/18
 */
public class ThreadLocalToken {
    private ThreadLocal<String> local = new ThreadLocal<>();
    
    public void setToken(String token) {
        local.set(token);
    }
    
    public String getToken() {
        return local.get();
    }
    
    public void clear() {
        local.remove();
    }
}
