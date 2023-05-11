package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.R;
import com.ly.reggie.dto.DishDto;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.Dish;
import com.ly.reggie.entity.DishFlavor;
import com.ly.reggie.service.CategoryService;
import com.ly.reggie.service.DishFlavorService;
import com.ly.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description  菜品管理
 * @Author ly
 * @create 2023-02-10 18:11
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    // 新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    // 分页查询菜品
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page(page,pageSize);

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(name),Dish::getName,name);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 分页查询
        dishService.page(pageInfo,queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = new ArrayList<>();
        // 给分类名称赋值
        records.forEach(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            list.add(dishDto);
        });

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    // 根据id查询菜品信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        log.info("根据id查询菜品信息...");
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    // 修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    // 根据条件查询对应的菜品数据
////    @GetMapping("/list")
////    public R<List<Dish>> list(Dish dish){
////        // 条件构造器
////        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
////        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId())
////                .eq(Dish::getStatus,1)
////                .orderByAsc(Dish::getSort)
////                .orderByDesc(Dish::getUpdateTime);
////
////        List<Dish> list = dishService.list(queryWrapper);
////
////        return R.success(list);
////    }

    // 根据条件查询对应的菜品数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;

        // 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_";

        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null){
            // 如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId())
                .eq(Dish::getStatus,1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        // 给分类名称赋值
        dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 当前菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，查询数据库，将查询到的菜品数据缓存到redis
        redisTemplate.opsForValue().set(key,dishDtoList,60,TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    // 对菜品批量或单个进行停售或起售
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status为：{},ids为：{}",status,ids);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> dishList = dishService.list(queryWrapper);

        for (Dish dish : dishList) {
            if (dish != null){
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }

        return R.success("售卖状态修改成功");
    }

    // 对菜品批量或单个进行删除
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除菜品，ids为：{}",ids);

        // 删除菜品
        dishService.deleteByIds(ids);

        // 删除菜品对应的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);

        return R.success("菜品删除成功");
    }
}












