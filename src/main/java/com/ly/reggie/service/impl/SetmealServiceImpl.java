package com.ly.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.reggie.common.CustomException;
import com.ly.reggie.dto.SetmealDto;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.DishFlavor;
import com.ly.reggie.entity.Setmeal;
import com.ly.reggie.entity.SetmealDish;
import com.ly.reggie.mapper.CategoryMapper;
import com.ly.reggie.mapper.SetmealMapper;
import com.ly.reggie.service.CategoryService;
import com.ly.reggie.service.DishService;
import com.ly.reggie.service.SetmealDishService;
import com.ly.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    // 根据套餐id查询套餐信息和对应的菜品信息
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        // 查询套餐基本信息
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        // 查询套餐对应的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    // 根据id修改套餐信息和对应的菜品信息
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        // 更新setmeal表的基本信息
        this.updateById(setmealDto);

        // 清理当前套餐对应菜品数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        // 添加当前提交过来的菜品数据
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();

        setmealDishList.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);
    }
}













