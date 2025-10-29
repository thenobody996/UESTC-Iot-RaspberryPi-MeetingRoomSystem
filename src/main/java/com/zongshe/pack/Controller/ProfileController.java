package com.zongshe.pack.Controller;

import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户个人资料接口",description = "用户个人资料更新")
@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

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
}
