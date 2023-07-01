package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，操作dish表和dish_flavor表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品所有的信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品，操作dish表和dish_flavor表
    public void updateWithFlavor(DishDto dishDto);
}
