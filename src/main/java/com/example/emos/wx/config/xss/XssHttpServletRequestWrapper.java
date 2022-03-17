package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 15:05 2022/3/17
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * TODO 不定义构造器没办法接收传入的请求对象
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String parameter = super.getParameter(name);
        if (!StrUtil.hasEmpty(parameter)) {
            //HtmlUtil.filter()用来转义，也就是去掉html、JavaScript等的标签，如<script>
            parameter = HtmlUtil.filter(parameter);
        }
        return parameter;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameters = super.getParameterValues(name);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String parameter = parameters[i];
                if (!StrUtil.hasEmpty(parameter)) {
                    parameter = HtmlUtil.filter(parameter);
                }
                parameters[i] = parameter;
            }
        }
        return parameters;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();

        for (String key : parameterMap.keySet()) {
            String[] values = parameterMap.get(key);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StrUtil.hasEmpty(value)) {
                    value = HtmlUtil.filter(value);
                }
                values[i] = value;
            }
            map.put(key, values);
        }

        return map;
    }

    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        if (!StrUtil.hasEmpty(header)) {
            header = HtmlUtil.filter(header);
        }
        return header;
    }

    /**
     * 重写该方法十分重要，因为Spring MVC框架就是通过这个方法从请求里提取客户端提交的数据，
     * 然后把这些数据封装到form对象里边,不对数据进行处理后端项目就不具备抵御跨站攻击的能力
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream in = super.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line = bufferedReader.readLine();
        while (line != null) {
            stringBuffer.append(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        inputStreamReader.close();
        in.close();

        Map<String, Object> map = JSONUtil.parseObj(stringBuffer.toString());
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof String) {
                if (!StrUtil.hasEmpty(value.toString())) {
                    result.put(key, HtmlUtil.filter(value.toString()));
                }
            } else {
                result.put(key, value);
            }
        }

        String jsonStr = JSONUtil.toJsonStr(result);
        ByteArrayInputStream bain = new ByteArrayInputStream(jsonStr.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            //必须覆盖该方法
            @Override
            public int read() throws IOException {
                return bain.read();
            }
        };
    }
}
