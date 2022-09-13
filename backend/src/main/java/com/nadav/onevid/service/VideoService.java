package com.nadav.onevid.service;

import com.nadav.onevid.dto.UploadVideoResponse;
import com.nadav.onevid.dto.VideoDto;
import com.nadav.onevid.model.Video;
import com.nadav.onevid.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;
    private final VideoRepository videoRepository;
    public UploadVideoResponse uploadVideo(MultipartFile multipartFile) {

        String videoUrl = s3Service.uploadFile(multipartFile);
        var video = new Video();
        video.setVideoUrl(videoUrl);

        var savedVideo = videoRepository.save(video);

        return new UploadVideoResponse(savedVideo.getId(), savedVideo.getVideoUrl());
    }

    public VideoDto editVideo(VideoDto videoDto) {
        //Find the video by videoId
        var savedVideo = getVideoById(videoDto.getId());

       //Map the videoDto fields to video
       savedVideo.setTitle(videoDto.getTitle());
       savedVideo.setDescription(videoDto.getDescription());
       savedVideo.setVideoStatus(videoDto.getVideoStatus());
       savedVideo.setTags(videoDto.getTags());
       savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());

        //save the video to the DB
        videoRepository.save(savedVideo);

        return videoDto;
    }

    public String uploadThumbnail(MultipartFile file, String videoId) {
        Video savedVideo = getVideoById(videoId);

        String thumbnailUrl = s3Service.uploadFile(file);
        savedVideo.setThumbnailUrl(thumbnailUrl);

        videoRepository.save(savedVideo);

        return thumbnailUrl;
    }

    Video getVideoById(String videoId){
        return videoRepository.findById(videoId)
                .orElseThrow(()-> new IllegalArgumentException("Cannot find video by id - " + videoId));
    }

    public VideoDto getVideoDetails(String videoId){
        Video savedVideo = getVideoById(videoId);

        VideoDto videoDto = new VideoDto();
        videoDto.setVideoUrl(savedVideo.getVideoUrl());
        videoDto.setThumbnailUrl(savedVideo.getThumbnailUrl());
        videoDto.setId(savedVideo.getId());
        videoDto.setTitle(savedVideo.getTitle());
        videoDto.setDescription(savedVideo.getDescription());
        videoDto.setVideoStatus(savedVideo.getVideoStatus());
        videoDto.setTags(savedVideo.getTags());

        return videoDto;
    }
}
