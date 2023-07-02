package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringredisTemplate;

    //发送短信
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone=user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code={}",code);

            //发送短信业务逻辑

            //将code保存在session
            //session.setAttribute(phone,code);

            //将验证码放在redis,有效期五分钟
            stringredisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码短信发送成功");


        }
        return R.error("手机验证码短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //log.info(map.toString());
        String phone=map.get("phone").toString();
        String code=map.get("code").toString();

        //从session获取验证码
        //Object code1 = session.getAttribute(phone);

        //从redis获取验证码
        Object code1=stringredisTemplate.opsForValue().get(phone);

        if(code1!=null && code1.equals(code)){
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user=userService.getOne(queryWrapper);

            if(user==null){
                //新用户
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            stringredisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }

}
