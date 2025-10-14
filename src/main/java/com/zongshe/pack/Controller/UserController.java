package com.zongshe.pack.Controller;


import com.zongshe.pack.Common.LoginRequest;
import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8089")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 注册接口
     * @param loginRequest
     * @return
     */
    @PostMapping("/register")
    public Result<User> Register(@RequestBody LoginRequest loginRequest) {
        try {
            User newUser = userService.register(
                    loginRequest.getAccount(),
                    loginRequest.getPassword());
            return Result.ok(newUser);
        }  catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    /**
     * 登录接口
     * @param loginRequest
     * @return
     */
    @PostMapping("/login")
    public Result<User> Login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(
                    loginRequest.getAccount(),
                    loginRequest.getPassword());
            return Result.ok(user);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取所有用户信息
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<User>> GetAllUser() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    /**
     * 根据ID获取用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> GetUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<User> RemoveUserById(@PathVariable Integer id) {
        try {
            userService.removeUser(id);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
