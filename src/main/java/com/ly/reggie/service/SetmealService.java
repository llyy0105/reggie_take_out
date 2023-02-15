package com.ly.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.reggie.dto.SetmealDto;
import com.ly.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Description
 * @Author ly
 * @create 2023-02-08 17:37
 */
public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    void removeWithDish(List<Long> ids);
}
