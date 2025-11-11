package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Long countByIsDeletedFalse();

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
    User findByAccountAndIsDeletedFalse(String username);

    /**
     * 按用户名模糊查找用户列表
     * @param username
     * @return
     */
    List<User> findByAccountContainingAndIsDeletedFalse(String username);


    @Query("select m from User u join u.meetings m where u.id = :userId and m.isDeleted = false")
    List<Meeting> findActiveMeetingsByUserId(@Param("userId") Integer userId);

}
