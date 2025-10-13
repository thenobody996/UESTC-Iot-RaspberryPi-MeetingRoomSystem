package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Profile;
import com.zongshe.pack.Repository.ProfileReposity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private ProfileReposity profileRepository;


}
