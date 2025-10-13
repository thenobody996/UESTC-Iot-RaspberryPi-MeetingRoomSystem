package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 查找所有未删除的用户
     * @return
     */
    List<User> findByIsDeletedFalse();

    /**
     * 按id获取用户
     * @param id
     * @return
     */
    User findByIdAndIsDeletedFalse(Integer id);

    /**
     * 按用户名查找指定用户
     * @param username
     * @return
     */
    User findByUsernameAndIsDeletedFalse(String username);

    /**
     * 按用户名模糊查找用户列表
     * @param username
     * @return
     */
    List<User> findByUsernameContainingAndIsDeletedFalse(String username);

}
