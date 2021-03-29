package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.User;
import com.example.community.service.FollowService;
import com.example.community.service.LikeService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Controller
public class UserController implements CommunityConstant {

    @Autowired
    private UserSerivice userSerivice;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @GetMapping("/setting") //打开此页面的时候，给页面返回一个上传凭证
    public String getSettingPage(Model model) {

        //设置上传文件名称
        String fileName = CommunityUtil.getRandomString();
        //设置上传响应信息
        StringMap stringMap = new StringMap();
        stringMap.put("returnBody", CommunityUtil.getJsonString(0));
        //设置上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, stringMap);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    @PostMapping("/update/header")
    @ResponseBody
    public String updateHeader(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJsonString(1, "文件名不能为空！");
        }
        String headerUrl = headerBucketUrl + "/" + fileName;
        userSerivice.updateHeader(hostHolder.getUser().getId(), headerUrl);
        return CommunityUtil.getJsonString(0, "修改头像成功!");
    }


    //修改密码
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("result", "两次输入的新密码不相同");
        } else {
            int userId = hostHolder.getUser().getId();
            Map<String, Object> map = userSerivice.updatePassowrd(userId, oldPassword, newPassword);
            if (map.containsKey("passwordError")) {
                model.addAttribute("passwordError", map.get("passwordError"));
            } else {
                model.addAttribute("result", map.get("result"));
            }
        }

        return "/site/setting";
    }

    //个人主页
    @GetMapping("/user/profile")
    public String getProfilePage(@RequestParam(name = "userId") int userId, Model model, @RequestParam(name = "flag") int flag) {
        User user = userSerivice.findUserbyId(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        model.addAttribute("likeCount", likeService.findUserLikeCount(userId));

        long followerCount = followService.findFollowerCount(ENTITY_TYPE_User, userId);
        long followeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_User);

        model.addAttribute("followerCount", followerCount);
        model.addAttribute("followeCount", followeCount);

        boolean followStatus = false;
        if (hostHolder.getUser() != null) {
            followStatus = followService.findFollowStatus(hostHolder.getUser().getId(), ENTITY_TYPE_User, userId);
        }
        model.addAttribute("followStatus", followStatus);
        model.addAttribute("flag", flag);


        return "/site/profile";
    }


    /* //上传头像到服务器(已废弃)
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "请选择图片");
            return "/site/setting";
        }
        //拿到上传文件的后缀名
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //如果后缀名是空的，说明文件格式不正确
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.getRandomString() + suffix;

        //把文件重命名后存入本地硬盘中
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("上传文件失败，服务器出现异常！");
        }

        //更新用户头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userSerivice.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }*/

    /*//向浏览器返回图片(已废弃)
    @GetMapping("/user/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //向服务器返回图片
        response.setContentType("image/" + suffix);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
