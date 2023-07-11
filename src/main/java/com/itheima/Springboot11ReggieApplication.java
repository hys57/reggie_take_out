package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan  //过滤器生效
@EnableTransactionManagement//开启事务
@EnableCaching//缓存注解
public class Springboot11ReggieApplication {

	public static void main(String[] args) {
		SpringApplication.run(Springboot11ReggieApplication.class, args);
		log.info("项目启动成功");
	}

}
