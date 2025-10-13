package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Repository.ProfileReposity;
import com.zongshe.pack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileReposity profileReposity;

    public List<User> getAllUser() {return userRepository.findByIsDeletedFalse();}

    public User getUserById(int id) {return userRepository.findByIdAndIsDeletedFalse(id);}

    public User addUser(User user) {
        User savedUser = userRepository.save(user);
        //创建对应的个人资料
        Profile profile = new Profile();
        profile.setUser(user);

        profileReposity.save(profile);
        return savedUser;
    }

    public User removeUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user != null) {
            user.setDeleted(true);
            return userRepository.save(user);
        }
        return null;
    }
}
