package sagu.supro.BCT.utils;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class AWSProvider {

    public CognitoCachingCredentialsProvider getCredentialsProvider(Context context){
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:3acca93b-6b51-48cd-afd5-00fdd0280ace",
                Regions.US_EAST_1
        );
        //"PoolId": "ap-south-1:7124d20c-97cf-430d-a43c-6b98dac26b02",
        return credentialsProvider;
    }

}
