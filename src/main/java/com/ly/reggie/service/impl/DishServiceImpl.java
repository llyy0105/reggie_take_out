package com.ly.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.reggie.dto.DishDto;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.Dish;
import com.ly.reggie.entity.DishFlavor;
import com.ly.reggie.mapper.CategoryMapper;
import com.ly.reggie.mapper.DishMapper;
import com.ly.reggie.service.DishFlavorService;
import com.ly.reggie.service.DishService;
import org.apache.logging.log4j.util.Strings;
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
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    // 新增菜品，同时插入菜品对应的口味数据
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息
        this.save(dishDto);

        // 菜品id
        Long dishId = dishDto.getId();
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }

        // 保存菜品口味数据到菜品口味表
        dishFlavorService.saveBatch(flavors);
    }

    // 根据id查询菜品信息和对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        // 查询菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    // 根据id修改菜品信息和对应的口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表的基本信息
        this.updateById(dishDto);

        // 清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        // 添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map(item -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
















