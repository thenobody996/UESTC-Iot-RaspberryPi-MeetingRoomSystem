package com.zongshe.pack.Controller;

import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PutMapping("/{id}")
    public Result<Profile> updateProfile(@PathVariable Integer id, @RequestBody Profile profile) {
        try{
            profileService.updateProfile(id,profile.getUserName(),profile.getEmail(),profile.getAvatar());
            return Result.ok("个人资料修改完成");
        }
        catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
}
