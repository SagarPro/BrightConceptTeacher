/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package sagu.supro.BCT.leanback_lib;

import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerAdapter;
import android.support.v17.leanback.media.PlaybackTransportControlGlue;
import android.support.v17.leanback.widget.PlaybackControlsRow;

import java.io.File;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment {

    private PlaybackTransportControlGlue<MediaPlayerAdapter> mTransportControlGlue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Video currentVideo =
                (Video) getActivity().getIntent().getSerializableExtra(DetailsActivity.VIDEO);
        String type = getActivity().getIntent().getStringExtra("Type");

        VideoSupportFragmentGlueHost glueHost =
                new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);

        MediaPlayerAdapter playerAdapter = new MediaPlayerAdapter(getContext());
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE);

        if (type.equals("Online")) {
            mTransportControlGlue = new PlaybackTransportControlGlue<>(getContext(), playerAdapter);
            mTransportControlGlue.setHost(glueHost);
            mTransportControlGlue.setTitle(currentVideo.getTitle());
            mTransportControlGlue.playWhenPrepared();
            //playerAdapter.setDataSource(Uri.parse(currentVideo.getVideoUrl()));
            String videoPath = currentVideo.getVideoUrl();
            Uri uri;
            if (videoPath.contains("https"))
                uri = Uri.parse(videoPath.replace("https", "http"));
            else
                uri = Uri.parse(videoPath);
            playerAdapter.setDataSource(uri);
        } else {
            String videoName = getActivity().getIntent().getStringExtra("VideoName");
            String videoPath = getActivity().getIntent().getStringExtra("VideoPath");
            mTransportControlGlue = new PlaybackTransportControlGlue<>(getContext(), playerAdapter);
            mTransportControlGlue.setHost(glueHost);
            mTransportControlGlue.setTitle(videoName);
            mTransportControlGlue.playWhenPrepared();
            playerAdapter.setDataSource(Uri.fromFile(new File(videoPath)));
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransportControlGlue != null) {
            mTransportControlGlue.pause();
        }
    }
}