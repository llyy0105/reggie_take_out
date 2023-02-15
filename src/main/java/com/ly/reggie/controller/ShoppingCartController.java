package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ly.reggie.common.BaseContext;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.ShoppingCart;
import com.ly.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车管理
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    // 添加购物车
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);

        // 设置用户id，指定当前是哪个用户的购物车数据
        Long currendId = BaseContext.getCurrendId();
        shoppingCart.setUserId(currendId);

        // 查询当前菜品或者是套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currendId);

        if (dishId != null){
            // 添加的为菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            // 添加的为套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null){
            // 如果已经存在，就在原来数量上+1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 如果不存在，则添加到购物车
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    // 减少购物车
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);

        // 设置用户id，指定当前是哪个用户的购物车数据
        Long currendId = BaseContext.getCurrendId();
        shoppingCart.setUserId(currendId);

        // 查询当前菜品或者是套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currendId);

        if (dishId != null){
            // 减少的为菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            // 减少的为套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        Integer number = cartServiceOne.getNumber();
        if (number > 1){
            // 如果数量大于1，就在原来数量上-1
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 如果数量为1，则从购物车删除
            shoppingCartService.removeById(cartServiceOne);
        }

        return R.success("删除成功");
    }

    // 查看购物车
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrendId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    // 清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrendId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}




















