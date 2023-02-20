package com.ly.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.reggie.common.BaseContext;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.ShoppingCart;
import com.ly.reggie.mapper.ShoppingCartMapper;
import com.ly.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    public void clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrendId());

        shoppingCartService.remove(queryWrapper);
    }
}
