package com.example.community.controller;

import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;
    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }



    @PostMapping("/register")
    public String register(Model model, User user) { //只要form表单中参数名与user的属性名相同，SpringMVC会自动注入对应参数的值封装到user中
        final Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty())  { //注册成功，重新定向页面到主页，（只有激活账号后才重定向到登录页面）
            model.addAttribute("msg", "注册成功， 我们已经向您的邮箱发送了激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/register";
        }
    }
    // http://localhost:8080/community/activation/101/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId")int userId,
                             @PathVariable("code") String code) {
        final int res = userService.activation(userId, code);
        if (res == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以使用了！");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        } else if (res == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经被激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response /*HttpSession session*/) {
        //生成验证码
        String text = kaptchaProducer.createText();
        final BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入Session
        // session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.genertateUUID();
        Cookie cookie =  new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60); //设置Cookie失效时间为60秒
        cookie.setPath(contextPath); // cookie 有效路径
        response.addCookie(cookie);
        //将验证码存入Redis
        String RedisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(RedisKey, text, 60, TimeUnit.SECONDS); //设置过期时间为60秒



        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            final ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png", os);
        } catch (IOException e) {
           logger.error("响应验证码失败"+ e.getMessage());
        }
    }


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(String username,
                        String password,
                        String code,
                        boolean rememberme,
                        Model model,
                      /*  HttpSession session,*/
                        HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //核对验证码
       /* String kaptcha = (String) session.getAttribute("kaptcha");*/
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equals(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login"; //返回login页面
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFUALT_EXPIRED_SECONDS;
        final Map<String, Object> map = userService.login(username, password, expiredSeconds);

        if (map.containsKey("ticket")) {
            Cookie cookie =  new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index"; //重定向到首页的访问路径
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
