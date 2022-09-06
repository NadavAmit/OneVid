package com.nadav.onevid.repository;

import com.nadav.onevid.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoRepository extends MongoRepository<Video,String> {
}
