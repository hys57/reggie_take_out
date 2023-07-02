package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.Dish;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.util.resources.LocaleData;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/shoppingCart")
@RestController
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        Long currentId= BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        if(dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{

            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne!=null){
            //菜品已经在购物车存在
            Integer number=cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //不存在
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);

            cartServiceOne=shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车");
    }

    @PostMapping("/sub")
    public R<Object> sub(@RequestBody ShoppingCart shoppingCart){
        //System.out.println(shoppingCart.getDishId());
        Long currentId = BaseContext.getCurrentId();
        Long dishId=shoppingCart.getDishId();

        LambdaUpdateWrapper<ShoppingCart> updateWrapper=new LambdaUpdateWrapper<>();

        updateWrapper.eq(ShoppingCart::getUserId,currentId)
        .eq(ShoppingCart::getDishId,dishId);

        //获取商品数量
        int number = shoppingCartService.getOne(updateWrapper).getNumber();

        if(number==1){
            shoppingCartService.remove(updateWrapper);
            return R.success("清空");
        }else {
            updateWrapper.set(ShoppingCart::getNumber,number-1);
            shoppingCartService.update(updateWrapper);
        }
        ShoppingCart one = shoppingCartService.getOne(updateWrapper);
        return R.success(one);
    }
}
