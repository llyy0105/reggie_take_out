package com.ly.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @Author ly
 * @create 2023-02-08 17:36
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
