package com.ly.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.reggie.entity.SetmealDish;
import com.ly.reggie.entity.User;
import com.ly.reggie.mapper.SetmealDishMapper;
import com.ly.reggie.mapper.UserMapper;
import com.ly.reggie.service.SetmealDishService;
import com.ly.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author ly
 * @create 2023-02-10 0:56
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
