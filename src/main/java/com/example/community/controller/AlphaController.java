package com.example.community.controller;

import com.example.community.service.AlphaService;
import com.example.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayhello() {
        return "hello springboot";
    }



    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        //获取请求数据
        System.out.println(request.getMethod()); //请求方法
        System.out.println(request.getServletPath()); //请求路径
        final Enumeration<String> enumeration = request.getHeaderNames(); //消息头
        while (enumeration.hasMoreElements()) {
            final String name = enumeration.nextElement();
            final String value = request.getHeader(name);
            System.out.println(name +": " + value);
        }

        //获得请求参数
        final String code = request.getParameter("code");
        System.out.println(code);

        //返回相应数据
        response.setContentType("text/html;charset=utf-8"); //返回数据的类型
        try (final PrintWriter writer = response.getWriter() ){ //获取输出流
            writer.write("<h>牛客网</h>");  //写入输出数据
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get请求
    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "10") int limit) { //参数名与请求参数名保持一致就会自动赋值给对应参数
        System.out.println(current);
        System.out.println(limit);

        return "some students";
    }

    // /student/123
    @RequestMapping(value = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // POST
    @RequestMapping(value = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) { //方法参数与form表单name一致，将自动赋值
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        final ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", "30");
        mav.setViewName("/demo/view"); //设定模板名 （模板为templates目录下的html文件，但无需添加.HTML后缀）
        return mav;
    }

    @RequestMapping(value = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", "80");
        return "/demo/view";
    }

    // 响应JSON数据 （异步请求）
    // Java对象 ——> JSON字符串——> JS对象
    @RequestMapping(value = "/emp", method = RequestMethod.GET)
    @ResponseBody //需要加上此注解，表示返回JSON数据格式, 方法的返回对象将被自动转成JSON格式的数据返回浏览器
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 30);
        emp.put("salary", 8000.00);
        return emp;
    }

    @RequestMapping(value = "/emps", method = RequestMethod.GET)
    @ResponseBody //需要加上此注解，表示返回JSON数据格式, 方法的返回对象将被自动转成JSON格式的数据返回浏览器
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 30);
        emp.put("salary", 8000.00);
        list.add(emp);

        Map<String, Object> emp2 = new HashMap<>();
        emp.put("name", "小明");
        emp.put("age", 23);
        emp.put("salary", 5000.00);

        list.add(emp2);
        return list;
    }

    @RequestMapping(value = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        //创建Cookie:
        Cookie cookie = new Cookie("code", CommunityUtil.genertateUUID());
        //设置Cookie生效范围
        cookie.setPath("/community/alpha");
        //设置Cookie的生存时间（默认存在内存，浏览器关闭，清除cookie,设置时间后将保存在硬盘中）
        cookie.setMaxAge(60 * 10);
        //发送cookie
        response.addCookie(cookie);
        return "set cookie";
    }


    @RequestMapping(value = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(@CookieValue("code") String code) { //添加一个获得Cookie的注解，（cookie的key）
        System.out.println(code);
        return "get cookie";
    }

    //Session
    @RequestMapping(value = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) { //session直接通过SpringMVC注入
        session.setAttribute("id", 1);
        session.setAttribute("name", "test");
        return "set session";
    }

    @RequestMapping(value = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "set session";
    }


    // ajax示例
    @RequestMapping(value = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功");
    }

}
