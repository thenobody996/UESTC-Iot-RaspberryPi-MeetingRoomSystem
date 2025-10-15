package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Repository.ProfileReposity;
import com.zongshe.pack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileReposity profileReposity;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public User register(String account, String password) throws Exception {
        User existingUser = userRepository.findByAccountAndIsDeletedFalse(account);
        if (existingUser != null) {
            throw new Exception("用户名已存在");
        }
        User newUser = new User();
        newUser.setAccount(account);
        newUser.setPassword(bCryptPasswordEncoder.encode(password));
        return addUser(newUser);
    }

    public User login(String account, String password) throws Exception {
        User user = userRepository.findByAccountAndIsDeletedFalse(account);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new Exception("密码错误");
        }
        return user;
    }

    public List<User> getAllUsers() {return userRepository.findByIsDeletedFalse();}

    public User getUserById(int id) {return userRepository.findByIdAndIsDeletedFalse(id);}

    public List<User> searchUsersByAccount(String account) {
        return userRepository.findByAccountContainingAndIsDeletedFalse(account);
    }

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
            Profile profile = profileReposity.findByIdAndIsDeletedFalse(userId);
            if(profile != null) {
                profile.setIsDeleted(true);
            }
            user.setDeleted(true);
            return userRepository.save(user);
        }
        return null;
    }
}
