package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProfileReposity extends JpaRepository<Profile, Integer> {

    Profile findByUserNameAndIsDeletedFalse(String name);

    Profile findByIdAndIsDeletedFalse(int id);

    Profile findByUserAndIsDeletedFalse(User user);

}
