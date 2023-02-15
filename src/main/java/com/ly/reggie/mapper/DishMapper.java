package com.ly.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.reggie.entity.Category;
import com.ly.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description
 * @Author ly
 * @create 2023-02-10 0:55
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
