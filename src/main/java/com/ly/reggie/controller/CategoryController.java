package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.Category;
import com.ly.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Description  分类管理
 * @Author ly
 * @create 2023-02-10 0:53
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 新增分类
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增种类，种类信息为：{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    // 分页查询分类
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 分页构造器
        Page pageInfo = new Page(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件，按照sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        // 分页查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    // 根据id删除分类
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类，id为：{}",id);

//        categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("分类信息删除成功");
    }

    // 根据id修改分类信息
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info(category.toString());
        categoryService.updateById(category);

        return R.success("修改成功");
    }

    // 根据条件查询分类数据
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        // 条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        lambdaQueryWrapper.orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);

        return R.success(list);
    }

}




















