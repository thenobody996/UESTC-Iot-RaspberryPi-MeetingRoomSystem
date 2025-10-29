package com.zongshe.pack.Controller;


import com.zongshe.pack.Common.LoginRequest;
import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
@Tag(name = "用户相关接口", description = "用户注册、登录、查询、删除等操作")
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
    @Operation(summary = "用户注册", description = "注册新用户，传入账号和密码")
    @PostMapping("/register")
    public Result<User> Register(@Parameter(description = "登录请求体，包括账号和密码", required = true)
                                     @RequestBody LoginRequest loginRequest) {
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
    @Operation(summary = "用户登录", description = "用户登录，传入账号和密码,返回登录结果")
    @PostMapping("/login")
    public Result<User> Login(@Parameter(description = "登录请求体，包括账号和密码", required = true)
                                  @RequestBody LoginRequest loginRequest) {
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
    @Operation(summary = "获取所有用户信息", description = "返回所有未删除用户列表")
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
    @Operation(summary = "按id查找用户", description = "路径变量为要查找的用户id,返回用户信息")
    @GetMapping("/{id}")
    public ResponseEntity<User> GetUserById(@Parameter(description = "路径变量:用户id", required = true)
                                                @PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    /**
     * 根据账户名模糊搜索用户
     * @param account
     * @return
     */
    @Operation(summary = "按账户名模糊搜索用户", description = "路径变量为要搜索的账户名,返回匹配的用户列表")
    @GetMapping("/search/{account}")
    public ResponseEntity<List<User>> SearchUserByAccount(@Parameter(description = "查询参数:账户名", required = true)
                                                    @PathVariable String account) {
        try {
            List<User> users = userService.searchUsersByAccount(account);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    /**
     * 根据ID删除用户
     * @param id
     * @return
     */
    @Operation(summary = "按id删除用户", description = "路径变量为要删除的用户id")
    @DeleteMapping("/delete/{id}")
    public Result<User> RemoveUserById(@Parameter(description = "路径变量:用户id", required = true)
                                           @PathVariable Integer id) {
        try {
            userService.removeUser(id);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
