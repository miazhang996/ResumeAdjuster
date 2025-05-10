package org.example.resumeadjuster.controller;

import org.example.resumeadjuster.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Test purpose
//目的是为了测试用，清除所有的database 里面的数据
// 实际生产中，这个 feature TBD

// *****注意如果需要admin 功能要修改Security Config

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private UserService userService;


    /**
     * 重置数据库，删除所有用户并重置ID序列
     * 警告：此操作不可逆，会永久删除所有用户数据
     *
     * @return 响应状态
     */

    @PostMapping("/reset-database")
    public ResponseEntity<String> resetDatabase() {
        try {
            userService.resetDatabase();
            return ResponseEntity.ok("数据库已成功重置：所有用户已删除，ID序列已重置");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("重置数据库时发生错误: " + e.getMessage());
        }
    }


}
