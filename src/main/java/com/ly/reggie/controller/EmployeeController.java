package com.ly.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.reggie.common.R;
import com.ly.reggie.entity.Employee;
import com.ly.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Description  员工管理
 * @Author ly
 * @create 2023-02-08 17:43
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // 员工登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // 将提交的密码进行MD5加密
        String username = employee.getUsername();
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,username);
        Employee emp = employeeService.getOne(queryWrapper);

        // 如果没有查询到则返回失败结果
        if (emp == null){
            return R.error("账号不存在");
        }

        // 如果密码不一致则返回失败结果
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }

        // 查看员工状态，如果已禁用则登陆失败
        if (emp.getStatus() != 1){
            return R.error("账号已禁用");
        }

        // 登录成功，将员工id传入session
        HttpSession session = request.getSession();
        session.setAttribute("employee",emp.getId());

        return R.success(emp);
    }

    // 员工退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清理session中保存的当前员工的id
        HttpSession session = request.getSession();
        session.removeAttribute("employee");

        return R.success("退出成功");
    }

    // 新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息为：{}",employee.toString());
        HttpSession session = request.getSession();

        // 设置初始密码123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 获得当前登录用户的id
//        Long empId = (Long) session.getAttribute("employee");

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    // 分页查询员工
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 构建分页构造器
        Page pageInfo = new Page(page,pageSize);

        // 构建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(name),Employee::getUsername,name);
        queryWrapper.orderByAsc(Employee::getCreateTime);

        // 执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    // 根据id修改员工信息
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

//        Long empId = (Long) request.getSession().getAttribute("employee");

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);

        employeeService.updateById(employee);

        return R.success("修改成功");
    }

    // 根据id查询员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);

        if (employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

}




















