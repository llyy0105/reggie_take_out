package com.ly.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.reggie.entity.Category;

/**
 * @Description
 * @Author ly
 * @create 2023-02-10 0:56
 */
public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
