package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Repository.ProfileReposity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private ProfileReposity profileRepository;

    @Autowired
    private UserService userService;

    public Profile updateProfile(Integer id, String userName, String email, String avatar) throws Exception {
        Profile profile = profileRepository.findByIdAndIsDeletedFalse(id);
        if (profile != null) {
            if (userName != null) profile.setUserName(userName);
            if (email != null) profile.setEmail(email);
            if (avatar != null) profile.setAvatar(avatar);
            return profileRepository.save(profile);
        }
        else {
            throw new Exception("未查找到对应个人数据");
        }

    }

    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    public Profile getProfile(Integer id) {return profileRepository.findByIdAndIsDeletedFalse(id);}

    public Profile getProfileByUserId(Integer userId) throws Exception {
        User user = userService.getUserById(userId);
        if (user != null) {
            return profileRepository.findByUserAndIsDeletedFalse(user);
        }
        throw new Exception("未查找到对应用户");
    }
}
