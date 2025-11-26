package com.zongshe.pack.Controller;

import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Service.ProfileService;
import com.zongshe.pack.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Tag(name = "用户个人资料接口",description = "用户个人资料更新")
@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Value("${profile.upload.directory}")
    private String uploadDir;

    @Operation(summary = "个人资料更新", description = "路径变量传入个人资料对应id,请求体必须有用户名(注意区别user的account),邮箱,图片url")
    @PutMapping("/{id}")
    public Result<Profile> updateProfile(@Parameter(description = "个人资料id",required = true)@PathVariable Integer id,
                                         @Parameter(description = "请求体",required = true)@RequestBody Profile profile) {
        try{
            profileService.updateProfile(id,profile.getUserName(),profile.getEmail(),profile.getAvatar());
            return Result.ok("个人资料修改完成");
        }
        catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/uploadAvatar")
    @Operation(summary = "上传头像", description = "上传头像图片，返回图片访问URL，需要使用这个URL更新个人资料的头像字段")
    public Result<String> uploadAvatar(@RequestParam("avatar")MultipartFile avatar) {
        try{
            String fileName = UUID.randomUUID().toString() + "."  + getFileExtension(avatar.getOriginalFilename());
            Path targetLocation = Paths.get(uploadDir, fileName);
            Files.createDirectories(targetLocation);
            Files.copy(avatar.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/profile/" + fileName; // 假设这是访问URL的格式
            return Result.ok(fileUrl);
        } catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    // 新增：通过 profile id 查询
    @Operation(summary = "根据 profile id 获取个人资料")
    @GetMapping("/{id}")
    public Result<Profile> getProfileById(@Parameter(description = "profile id", required = true) @PathVariable Integer id) {
        try {
            Profile p = profileService.getProfile(id);
            if (p == null) {
                return Result.fail("未找到对应个人资料");
            }
            return Result.ok(p);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 新增：通过 user id 查询 profile
    @Operation(summary = "根据 userId 获取个人资料")
    @GetMapping("/byUser/{userId}")
    public Result<Profile> getProfileByUserId(@Parameter(description = "user id", required = true) @PathVariable Integer userId) {
        try {
            Profile p = profileService.getProfileByUserId(userId);
            return Result.ok(p);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 获取文件扩展名
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return ""; // 没有扩展名
        }
        return fileName.substring(index + 1);
    }
}
