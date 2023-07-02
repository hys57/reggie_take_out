package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        dishService.saveWithFlavor(dishDto);

        //删除redis缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page ,int pageSize,String name){
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //数据回写
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto=dishService.getByIdWithFlavor(id);


        return R.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);
        //清理redis缓存数据,清理所有
        /*Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/
        //精确清理
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /*//查询菜品
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //状态为一
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(queryWrapper);

        return R.success(list);
    }*/

    //查询菜品
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList=null;
        //设置一个key
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //在redis获取数据
        dishDtoList  = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //有数据
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //状态为一
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(queryWrapper);

        dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();
            Category category=categoryService.getById(categoryId);

            if(category!=null){
                String name = category.getName();
                dishDto.setCategoryName(name);
            }

            Long dishId=item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);

            List<DishFlavor> dishFlavors=dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);

            return dishDto;

        }).collect(Collectors.toList());

        //将数据存到redis中,保存六十分钟
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") int status,@RequestParam("ids") String ids){
        //log.info(ids);
        String[] idArray=ids.split(",");
        List<Long> idsList=new ArrayList<>();
        for(int i=0;i< idArray.length;i++){
            idsList.add(Long.parseLong(idArray[i]));
        }
        //log.info(idsList.toString());

        LambdaUpdateWrapper<Dish> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId,idsList)
                .set(Dish::getStatus,status);

        dishService.update(updateWrapper);

        //删除所有redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);


        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") String ids){
        String[] idArray=ids.split(",");
        List<Long> idsList=new ArrayList<>();
        for(int i=0;i< idArray.length;i++){
            idsList.add(Long.parseLong(idArray[i]));
        }

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,idsList);

        dishService.remove(queryWrapper);

        //删除所有redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("删除成功");

    }
}
