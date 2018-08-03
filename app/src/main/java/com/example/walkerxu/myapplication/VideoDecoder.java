package com.example.walkerxu.myapplication;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

/*
video decoders
MediaCodec if the platform support the codec such as hevc
ffmpegcodec if the platform does not support the codec such as hevc
 */
public class VideoDecoder extends Thread {
//    private static final String VIDEO = "video/";

    private static final String TAG = "VideoDecoder";

//    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    private boolean eosReceived;

    public static interface FrameUpdateEvents {
        public void onFrame(ByteBuffer buffer, int width, int height);
    }
    private FrameUpdateEvents frameUpdateEvents;

    private int mWidth;
    private int mHeight;

    private int mTimeScale = 0;

    private List<byte[]> mImageConfigs = new ArrayList<byte[]>();
    private List<byte[]> mImages = new ArrayList<byte[]>();

    public void setImageConfig(byte[] config) {
        Log.i(TAG, "setImageConfig");
        mImageConfigs.add(config);
    }

    public void setImage(byte[] image) {
        Log.i(TAG, "setImage");
        mImages.add(image);
    }

    public void setmTimeScale(int timescale) {
        mTimeScale = timescale;
    }

    public boolean prepare(Surface surface, String mime, int width, int height) {
        Log.i(TAG, "prepare:" + mime + "," + width + "," + height);

        frameUpdateEvents = null;
        eosReceived = false;

        /*hvc1 for static image codec hevc
         *grid for multiple images, we assume the codec is hevc
         *you need to read the heif documents to ensure if multiple images
         * have the same codec or config in the grid
         */
        if(mime.equals(heifdefines.IMAGE_TYPE_HVC1)) {
            mime = heifdefines.CODEC_HEVC;
        }
        if(mime.equals(heifdefines.IMAGE_TYPE_GRID)) {
            mime = heifdefines.CODEC_HEVC;
        }
        if(mime.equals(heifdefines.IMAGE_TYPE_IOVL)) {
            mime = heifdefines.CODEC_HEVC;
        }

        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, mime);
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
        Log.i(TAG, "format : " + format);

        mWidth = width;
        mHeight = height;

        try {
            mDecoder = MediaCodec.createDecoderByType(mime);

            mDecoder.configure(format, surface, null, 0 /* Decoder */);

            mDecoder.start();

            Log.i(TAG, "decoder start");

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }

        return true;
    }

    @Override
    public void run() {
        Log.i(TAG, "decoder started");

        BufferInfo info = new BufferInfo();

        boolean isInput = true;
        boolean first = false;
        long startWhen = 0;

        int loop = 0;
        int totalConfigs = mImageConfigs.size();
        int totalImages = mImages.size();

        while (!eosReceived) {
            if (isInput) {
                int inputIndex = mDecoder.dequeueInputBuffer(500000);
                if (inputIndex >= 0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);

                    long sampletime = 0;
                    if(!first) {
                        first = true;
                        inputBuffer.put(mImageConfigs.get(0));
                        mDecoder.queueInputBuffer(inputIndex, 0, mImageConfigs.get(0).length, sampletime, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                        Log.i(TAG, "decode config");
                    } else {
                        loop = loop % totalImages;
                        inputBuffer.put(mImages.get(loop));
                        mDecoder.queueInputBuffer(inputIndex, 0, mImages.get(loop).length, sampletime, 0);
                        Log.i(TAG, "decode data");
                        loop++;
                    }

//                    if(loop > 3) {
//                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
//                        mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        isInput = false;
//                    }
                }
            }

            int outIndex = mDecoder.dequeueOutputBuffer(info, 500000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    mDecoder.getOutputBuffers();
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                    break;

                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.i(TAG, "INFO_TRY_AGAIN_LATER");
                    break;

                default:
                    Log.i(TAG, "default");
                    ByteBuffer buffer = mDecoder.getOutputBuffer(outIndex);
                    if(frameUpdateEvents != null) {
                        frameUpdateEvents.onFrame(buffer,mWidth,mHeight);
                    }
                    if (!first) {
                        startWhen = System.currentTimeMillis();
                        first = true;
                    }
                    try {
//                        long sleepTime = (info.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
//                        Log.d(TAG, "info.presentationTimeUs : " + (info.presentationTimeUs / 1000) + " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime);
//                        if (sleepTime > 0) {
//                            Thread.sleep(sleepTime);
//                        }

                        if(mTimeScale > 0) {
                            Thread.sleep(1000 / mTimeScale);
                        } else {
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i(TAG, "error:" + e.toString());
                    }

                    //render it
                    mDecoder.releaseOutputBuffer(outIndex, true /* Surface init */);

                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        if(frameUpdateEvents != null) {
            frameUpdateEvents.onFrame(null,mWidth,mHeight);
        }

        Log.i(TAG, "stop and release the decoder");
        mDecoder.stop();
        mDecoder.release();
    }

    public void close() {
        Log.i(TAG, "close");
        eosReceived = true;
    }

}
