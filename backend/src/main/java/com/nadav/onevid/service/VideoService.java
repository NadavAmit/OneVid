package com.nadav.onevid.service;

import com.nadav.onevid.dto.CommentDto;
import com.nadav.onevid.dto.UploadVideoResponse;
import com.nadav.onevid.dto.VideoDto;
import com.nadav.onevid.model.Comment;
import com.nadav.onevid.model.Video;
import com.nadav.onevid.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;

    private final UserService userService;
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

        increaseVideoCount(savedVideo);
        userService.addVideoToHistory(videoId);


        VideoDto videoDto = mapToVideoDto(savedVideo);

        return videoDto;
    }

    private void increaseVideoCount(Video savedVideo) {
        savedVideo.incrementViewCount();
        videoRepository.save(savedVideo);
    }

    public VideoDto likeVideo(String videoId) {
        Video video = getVideoById(videoId);

        if(userService.ifLikedVideo(videoId)){
            video.decrementLikes();
            userService.removeFromLikedVideos(videoId);
        } else if(userService.ifDisLikedVideo(videoId)){
            video.decrementDisLikes();
            userService.removeFromDisLikedVideos(videoId);
            video.incrementLikes();
            userService.addToLikedVideos(videoId);
        }else {
            video.incrementLikes();
            userService.addToLikedVideos(videoId);
        }

        videoRepository.save(video);

        VideoDto videoDto = mapToVideoDto(video);

        return videoDto;
    }

    public VideoDto disLikeVideo(String videoId) {
        Video video = getVideoById(videoId);

        if(userService.ifDisLikedVideo(videoId)){
            video.decrementDisLikes();
            userService.removeFromDisLikedVideos(videoId);
        } else if(userService.ifLikedVideo(videoId)){
            video.decrementLikes();
            userService.removeFromLikedVideos(videoId);
            video.incrementDisLikes();
            userService.addToDisLikedVideos(videoId);
        }else {
            video.incrementDisLikes();
            userService.addToDisLikedVideos(videoId);
        }

        videoRepository.save(video);

        VideoDto videoDto = mapToVideoDto(video);

        return videoDto;
    }

    private VideoDto mapToVideoDto(Video video) {
        VideoDto videoDto = new VideoDto();
        videoDto.setVideoUrl(video.getVideoUrl());
        videoDto.setThumbnailUrl(video.getThumbnailUrl());
        videoDto.setId(video.getId());
        videoDto.setTitle(video.getTitle());
        videoDto.setDescription(video.getDescription());
        videoDto.setVideoStatus(video.getVideoStatus());
        videoDto.setTags(video.getTags());
        videoDto.setLikeCount(video.getLikes().get());
        videoDto.setDislikeCount(video.getDislikes().get());
        videoDto.setViewCount(video.getViewCount().get());
        return videoDto;
    }

    public void addComment(String videoId, CommentDto commentDto) {
        Video video = getVideoById(videoId);
        Comment comment = new Comment();
        comment.setText(commentDto.getCommentText());
        comment.setAuthorId(commentDto.getAuthorId());
        video.addComment(comment);

        videoRepository.save(video);
    }

    public List<CommentDto> getAllComments(String videoId) {
        Video video = getVideoById(videoId);
        List<Comment> commentList = video.getCommentList();

        return commentList.stream().map(this::mapToCommentDto).toList();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText(comment.getText());
        commentDto.setAuthorId(comment.getAuthorId());
        return commentDto;
    }

    public List<VideoDto> getAllVideos() {
        return videoRepository.findAll().stream().map(this::mapToVideoDto).toList();
    }
}
