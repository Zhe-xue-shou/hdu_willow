package com.hdu.hdufpga.controller;

import com.hdu.hdufpga.entity.Result;
import com.hdu.hdufpga.entity.dto.UserStatisticDTO;
import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController<UserService, UserPO> {

    @Resource
    UserService userService;

    //level >= 2
    @Override
    public Result create(@RequestBody UserPO userPO) {
        return super.create(userPO);
    }

    //level >= 3
    @Override
    public Result delete(@RequestBody UserPO userPO) {
        return super.delete(userPO);
    }

    //level >= 3
    @Override
    public Result update(@RequestBody UserPO userPO) {
        return super.update(userPO);
    }

    //level >= 2
    @Override
    public Result get(Integer id) {
        return super.get(id);
    }

    //level >= 2
    @Override
    public Result listPage(Integer current, Integer size) {
        return super.listPage(current, size);
    }

    @GetMapping("/getStatistics")
    public Result getUserStatistics() {
        try {
            return Result.ok(userService.getCurrentUserStatistics());
        } catch (Exception e) {
            log.error("获取用户统计信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
