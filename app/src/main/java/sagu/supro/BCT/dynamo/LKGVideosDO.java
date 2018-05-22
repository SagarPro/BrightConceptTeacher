package sagu.supro.BCT.dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "bct-mobilehub-1215798483-LKG_Videos")

public class LKGVideosDO {
    private String _videoId;
    private String _videoTitle;
    private String _videoCardImg;
    private String _videoDescription;
    private String _videoUrl;
    private String _videoTopic;

    @DynamoDBHashKey(attributeName = "video_id")
    @DynamoDBAttribute(attributeName = "video_id")
    public String getVideoId() {
        return _videoId;
    }

    public void setVideoId(final String _videoId) {
        this._videoId = _videoId;
    }
    @DynamoDBRangeKey(attributeName = "video_title")
    @DynamoDBAttribute(attributeName = "video_title")
    public String getVideoTitle() {
        return _videoTitle;
    }

    public void setVideoTitle(final String _videoTitle) {
        this._videoTitle = _videoTitle;
    }
    @DynamoDBAttribute(attributeName = "video_card_img")
    public String getVideoCardImg() {
        return _videoCardImg;
    }

    public void setVideoCardImg(final String _videoCardImg) {
        this._videoCardImg = _videoCardImg;
    }
    @DynamoDBAttribute(attributeName = "video_description")
    public String getVideoDescription() {
        return _videoDescription;
    }

    public void setVideoDescription(final String _videoDescription) {
        this._videoDescription = _videoDescription;
    }
    @DynamoDBAttribute(attributeName = "video_url")
    public String getVideoUrl() {
        return _videoUrl;
    }

    public void setVideoUrl(final String _videoUrl) {
        this._videoUrl = _videoUrl;
    }
    @DynamoDBAttribute(attributeName = "video_topic")
    public String getVideoTopic() {
        return _videoTopic;
    }

    public void setVideoTopic(final String _videoTopic) {
        this._videoTopic = _videoTopic;
    }

}
