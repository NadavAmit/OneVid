package com.nadav.onevid.repository;

import com.nadav.onevid.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {
}
