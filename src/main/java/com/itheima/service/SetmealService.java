package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时保存套餐和菜品的管联关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐，以及关联的菜品
    public void removeWithDish(List<Long> ids);
}
