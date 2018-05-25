package sagu.supro.BCT.leanback_lib;

import java.io.Serializable;

public class Video implements Serializable{
    private String id;
    private String title;
    private String description;
    private String cardImageUrl;
    private String videoUrl;
    private String videoTopic;

    public Video(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setVideoTopic(String videoTopic){
        this.videoTopic = videoTopic;
    }

    public String getVideoTopic(){ return videoTopic; }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}';
    }
}