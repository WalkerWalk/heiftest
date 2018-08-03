package com.example.walkerxu.myapplication;

import android.util.Log;

import java.util.List;

/*
audio decoders
MediaCodec is enough here
maybe ffmpegcodecs have better compatibility
 */
public class AudioDecoder {

    private static final String TAG = "AudioDecoder";

    private List<byte[]> mSoundConfigs;
    private List<byte[]> mSounds;


    public void setSoundConfig(byte[] config) {
        Log.i(TAG, "setSoundConfig");
        mSoundConfigs.add(config);
    }

    public void setSound(byte[] sound) {
        Log.i(TAG, "setSound");
        mSounds.add(sound);
    }
}
