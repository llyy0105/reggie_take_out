package com.ly.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.reggie.dto.DishDto;
import com.ly.reggie.entity.Dish;
import com.ly.reggie.entity.Employee;

import java.util.List;

/**
 * @Description
 * @Author ly
 * @create 2023-02-08 17:37
 */
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);
    void updateWithFlavor(DishDto dishDto);
    void deleteByIds(List<Long> ids);
}
