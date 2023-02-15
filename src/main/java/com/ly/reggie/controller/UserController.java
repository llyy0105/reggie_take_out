package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.User;
import com.ly.reggie.service.CategoryService;
import com.ly.reggie.service.UserService;
import com.ly.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;


/**
 * @Description  用户管理
 * @Author ly
 * @create 2023-02-10 0:53
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 发送手机短信验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 获取手机号
        String phone = user.getPhone();

        if (Strings.isNotEmpty(phone)){
            // 生成随机的验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}",code);

            // 需要将生成的验证码保存到session
            session.setAttribute(phone,code);
            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");
    }

    // 移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();

        // 从session中获取保存的验证码
        String cacheCode = session.getAttribute(phone).toString();

        // 进行验证码比对
        if (cacheCode != null && cacheCode.equals(code)){
            // 验证码一致，登陆成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            // 判断当前手机号是否为新用户，如果是新用户则自动完成注册
            User user = userService.getOne(queryWrapper);
            if (user == null){
                // 给当前手机号完成注册
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        // 验证码不一致，登陆失败
        return R.error("验证码有误");
    }
}




















