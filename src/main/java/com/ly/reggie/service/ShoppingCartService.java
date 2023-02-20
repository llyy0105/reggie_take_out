package com.ly.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.AddressBook;
import com.ly.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    void clean();
}
