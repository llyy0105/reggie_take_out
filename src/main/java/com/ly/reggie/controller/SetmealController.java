package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.R;
import com.ly.reggie.dto.DishDto;
import com.ly.reggie.dto.SetmealDto;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.Dish;
import com.ly.reggie.entity.Setmeal;
import com.ly.reggie.entity.SetmealDish;
import com.ly.reggie.service.CategoryService;
import com.ly.reggie.service.DishService;
import com.ly.reggie.service.SetmealDishService;
import com.ly.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description  套餐管理
 * @Author ly
 * @create 2023-02-11 19:16
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    // 新增套餐
    @PostMapping
    @ApiOperation(value = "新增套餐接口")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    // 分页查询套餐
    @GetMapping("/page")
    @ApiOperation("分页查询套餐接口")
    public R<Page> page(@ApiParam(name = "page",value = "页码",required = true)int page,
                        @ApiParam(name = "pageSize",value = "每页记录数",required = true) int pageSize,
                        @ApiParam(name = "name",value = "套餐名称",required = false)String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 分页构造器
        Page<Setmeal> pageInfo = new Page(page,pageSize);
        Page<SetmealDto> setmealPage = new Page<>(page,pageSize);

        // 条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 分页查询
        setmealService.page(pageInfo,queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        // 给套餐名称赋值
        List<SetmealDto> list = records.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealPage.setRecords(list);

        return R.success(setmealPage);
    }

    // 根据条件查询对应的套餐数据
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId")
    public R<List<Setmeal>> list(Setmeal setmeal){
        // 条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId())
                .eq(Setmeal::getStatus,1)
                .orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    // 根据id查询套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        log.info("根据id查询套餐信息...");

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    // 修改套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.updateWithDish(setmealDto);

        // 清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("setmeal_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的菜品缓存数据
        String key = "setmeal_" + setmealDto.getCategoryId() + "_";
        redisTemplate.delete(key);

        return R.success("修改套餐成功");
    }

    // 对套餐批量或单个进行删除
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除分类，ids为：{}",ids);
        setmealService.removeWithDish(ids);

        return R.success("套餐删除成功");
    }

    // 对套餐批量或单个进行停售或起售
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status为：{},ids为：{}",status,ids);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Setmeal::getId,ids);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        for (Setmeal setmeal : setmealList) {
            if (setmeal != null){
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
        }

        return R.success("售卖状态修改成功");
    }

    // 移动端点击套餐图片查看套餐具体内容
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);

        // 获取套餐里面的所有菜品
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtoList = setmealDishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 获取菜品id
            Long dishId = item.getDishId();
            Dish dish = dishService.getById(dishId);
            // 将菜品信息填充到dto中
            BeanUtils.copyProperties(dish, dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
}





















