package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.BaseContext;
import com.ly.reggie.common.R;
import com.ly.reggie.dto.OrdersDto;
import com.ly.reggie.dto.SetmealDto;
import com.ly.reggie.entity.*;
import com.ly.reggie.service.OrderDetailService;
import com.ly.reggie.service.OrderService;
import com.ly.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    // 用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);

        orderService.submit(orders);
        return R.success("下单成功");
    }

    // 修改订单状态
    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){
        Long orderId = Long.parseLong(map.get("id"));
        Integer status = Integer.parseInt(map.get("status"));

        if (orderId == null || status == null){
            return R.error("传入的信息不合法");
        }

        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);

        return R.success("订单状态修改成功");
    }

    // 后台查看和展示客户订单
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number,String beginTime,String endTime){
        log.info("page = {},pageSize = {},name = {},beginTime = {},endTime = {}",page,pageSize,number,beginTime,endTime);

        // 分页构造器
        Page<Orders> pageInfo = new Page(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(number != null,Orders::getNumber,number)
                .gt(Strings.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(Strings.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        // 分页查询
        orderService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    // 用户端展示订单分页查询
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        log.info("page = {},pageSize = {}",page,pageSize);

        // 分页构造器
        Page<Orders> pageInfo = new Page(page,pageSize);
        Page<OrdersDto> pageDto = new Page(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 根据当前用户id查询，并根据更新时间降序排列
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrendId())
                .orderByDesc(Orders::getOrderTime);

        // 分页查询
        orderService.page(pageInfo,queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        List<Orders> records = pageInfo.getRecords();

        // 给订单细节赋值
        List<OrdersDto> ordersDtoList = records.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            // 订单id
            Long orderId = item.getId();
            // 根据id获得订单细节
            List<OrderDetail> orderDetailList = orderService.getOrderDetailListByOrderId(orderId);

            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(ordersDtoList);

        return R.success(pageDto);
    }

    // 用户端再来一单
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        Long id = Long.parseLong(map.get("id"));

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车清空
        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrendId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(item -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());

            //菜品id
            Long dishId = item.getDishId();
            //套餐id
            Long setmealId = item.getSetmealId();
            if(dishId != null) {
                shoppingCart.setDishId(dishId);
            }else {
                shoppingCart.setSetmealId(setmealId);
            }

            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }

}


















