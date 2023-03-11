package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.User;
import com.example.community.service.FollowService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.apache.catalina.Host;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.genertateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发送异常", e);
        }

        //更新当前用户的头像的文件路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    /**
     * 返回值为void因为向浏览器响应不是网页也不是字符串而是二进制的数据（需要通过流向浏览器输出）
     */
    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        System.out.println(filename);
        // 响应文件
        response.setContentType("image/"+"jpeg");
        try (FileInputStream fis = new FileInputStream(filename)){ // 使用try with resource 在资源使用完毕后会自动关闭 （前提是实现closeable)
            ServletOutputStream os = response.getOutputStream();   // 输出流由SpringMVC管理, 会在响应结束后自动关闭
            BufferedInputStream bis = new BufferedInputStream(fis);

            //从输入流中读出字节流，写入输出流中
            byte[] buffer = new byte[1024];
            int b;
            while ((b = bis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: "+e.getMessage());
        }
    }

    @PostMapping("/updatepassword")
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {
        User user = hostHolder.getUser();
        System.out.println(oldPassword);
        System.out.println(newPassword);
        System.out.println(confirmPassword);

        if (!user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt()))) {
            model.addAttribute("oldMsg", "您的密码输入不正确");
            return "/site/setting";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("newMsg", "两次密码不一致！");
            return "/site/setting";
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/logout";
    }

    // 个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user", user);

        //点赞数量
        final int likeCount = likeService.findUserLikeCount(userId);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 是否被登录用户关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }
}
