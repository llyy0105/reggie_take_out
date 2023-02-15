package com.ly.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.reggie.common.CustomException;
import com.ly.reggie.dto.SetmealDto;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.Setmeal;
import com.ly.reggie.entity.SetmealDish;
import com.ly.reggie.mapper.CategoryMapper;
import com.ly.reggie.mapper.SetmealMapper;
import com.ly.reggie.service.CategoryService;
import com.ly.reggie.service.DishService;
import com.ly.reggie.service.SetmealDishService;
import com.ly.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description
 * @Author ly
 * @create 2023-02-10 0:56
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    // 新增套餐，同时需要保存套餐和菜品的关联关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息
        this.save(setmealDto);

        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        for (SetmealDish dish : dishes) {
            dish.setSetmealId(setmealDto.getId());
        }

        // 保存套餐与菜品的关联关系
        setmealDishService.saveBatch(dishes);
    }

    // 删除套餐，同时需要删除套餐与菜品的关联关系
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态,确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if (count > 0){
            // 如果不能删除，抛出一个也无异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 如果可以删除，先删除套餐的信息
        this.removeByIds(ids);

        // 然后删除套餐与菜品的关联关系
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}













