package com.example.walkerxu.myapplication;

import android.util.Log;
import android.view.Surface;

import com.nokia.heif.AudioTrack;
import com.nokia.heif.DerivedImageItem;
import com.nokia.heif.ErrorHandler;
import com.nokia.heif.Exception;
import com.nokia.heif.GridImageItem;
import com.nokia.heif.HEIF;
import com.nokia.heif.HEVCImageItem;
import com.nokia.heif.IdentityImageItem;
import com.nokia.heif.ImageItem;
import com.nokia.heif.ImageSequence;
import com.nokia.heif.OverlayImageItem;
import com.nokia.heif.Size;
import com.nokia.heif.Track;
import com.nokia.heif.VideoSample;
import com.nokia.heif.VideoTrack;

import java.util.List;

public class heifwrapper {

    private static final String TAG = "heifwrapper";

    void Heif2Jpeg(String heifUrl, String jpegUrl) {
    }

    void Jpeg2Heif(String heifUrl, String jpegUrl) {
    }

    /*
    for static heif
     */
    void loadSingleImage(String url, Surface surface) {
        String filename = url;
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            Log.i(TAG, "file:" + filename);
            // Load the file
            heif.load(filename);

            // Get the primary image
            ImageItem primaryImage = heif.getPrimaryImage();

            //get the image size
            int width = primaryImage.getSize().width;
            int height = primaryImage.getSize().height;
            Log.i(TAG, "width:"+ width + " height:" + height);

            //fourCC
            String fourCC = primaryImage.getType().toString();
            Log.i(TAG, "fourcc:" + fourCC);

            // Check the type, assuming that it's a HEVC image
            if (primaryImage instanceof HEVCImageItem)
            {
                HEVCImageItem hevcImageItem = (HEVCImageItem)primaryImage;
                byte[] decoderConfig = hevcImageItem.getDecoderConfig().getConfig();
                byte[] imageData = hevcImageItem.getItemDataAsArray();
                Log.i(TAG, "config length:" + decoderConfig.length + " data length:" + imageData.length);

                // Feed the data to a decoder
                VideoDecoder decoder = new VideoDecoder();

                decoder.setImageConfig(decoderConfig);
                decoder.setImage(imageData);

                decoder.prepare(surface, fourCC, width, height);

                decoder.start();
            }

        }
        // All exceptions thrown by the HEIF library are of the same type
        // Check the error code to see what happened
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    /*
    for sequence heif
     */
    void loadSequenceImage(String url, Surface surface) {
        String filename = url;
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try {
            Log.i(TAG, "file:" + filename);
            // Load the file
            heif.load(filename);

            // Get the primary image
            ImageItem primaryImage = heif.getPrimaryImage();

            //get the image size
            int width = primaryImage.getSize().width;
            int height = primaryImage.getSize().height;
            Log.i(TAG, "width:"+ width + " height:" + height);

            //fourCC
            String fourCC = primaryImage.getType().toString();
            Log.i(TAG, "fourcc:" + fourCC);

            // Check the type, assuming that it's a HEVC image
            if (primaryImage instanceof HEVCImageItem)
            {
                // Feed the data to a decoder
                VideoDecoder decoder = new VideoDecoder();

                int tracks = heif.getTracks().size();
                Log.i(TAG, "tracks:" + tracks);

                VideoTrack vt = null;
                AudioTrack at = null;
                for(int i = 0; i < tracks; i++) {
                    Track track = heif.getTracks().get(i);
                    if(track instanceof VideoTrack) {
                        vt = (VideoTrack)track;
                    } else if(track instanceof AudioTrack) {
                        at = (AudioTrack)track;
                    }
                }

                if(vt != null) {
                    int imagesCount = vt.getVideoSamples().size();
                    Log.i(TAG, "video track sequence size:" + imagesCount);

                    //time scale to control the playback rate
                    decoder.setmTimeScale(vt.getTimescale());

                    //video samples in video track
                    for(int j = 0; j < imagesCount; j++) {
                        VideoSample videoSample = vt.getVideoSamples().get(j);
                        byte[] decoderConfig = videoSample.getDecoderConfig().getConfig();
                        byte[] imageData = videoSample.getSampleDataAsArray();
                        Log.i(TAG, "config length:" + decoderConfig.length + " data length:" + imageData.length);

                        decoder.setImageConfig(decoderConfig);
                        decoder.setImage(imageData);
                    }
                }

                if(at != null) {
                    Log.w(TAG, "audio track is not null, do nothing now.");
                }

                decoder.prepare(surface, fourCC, width, height);

                decoder.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    void createAndSaveAFile() {
        // These should contain the encoded image data and the corresponding decoder config data
        int width = 640;
        int height = 480;
        // Sizes are just placeholders
        byte[] decoderConfig = new byte[1024];
        byte[] imageData = new byte[50000];

        // Filename should have the full path
        String filename = "something/something.heic";

        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            // This example assumes that the data is HEVC
            // The constructor requires the HEIF instance, the size of the image,
            // the decoder config data and the image data
            HEVCImageItem imageItem = new HEVCImageItem(heif, new Size(width, height),
                    decoderConfig, imageData);
            // Every HEIF image should have a primary image
            heif.setPrimaryImage(imageItem);

            // The brands need to be set
            heif.setMajorBrand(HEIF.BRAND_MIF1);
            heif.addCompatibleBrand(HEIF.BRAND_HEIC);

            // And we save the file
            heif.save(filename);

        }
        // All exceptions thrown by the HEIF library are of the same type
        // Check the error code to see what happened
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
    for overlay heif
     */
    void loadOverlayImage(String url, Surface surface) {
        String filename = url;
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            // Load the file
            heif.load(filename);

            // Get the primary image
            ImageItem primaryImage = heif.getPrimaryImage();

            //get the image size
            int width = primaryImage.getSize().width;
            int height = primaryImage.getSize().height;
            Log.i(TAG, "width:"+ width + " height:" + height);

            //fourCC
            String fourCC = primaryImage.getType().toString();
            Log.i(TAG, "fourcc:" + fourCC);

            //the primary image is grid like 2x2, 3x3 etc.
            //the primary image is an individual image??????

            // Check the type, assuming that it's a Grid image
            if (primaryImage instanceof OverlayImageItem)
            {
                VideoDecoder decoder = new VideoDecoder();

                OverlayImageItem overlayImageItem = (OverlayImageItem) primaryImage;

                int imagesCount = overlayImageItem.getOverlayedImages().size();
                Log.i(TAG, "images:" + imagesCount);

                // Go through the overlay imageitem
                for (int i = 0; i < imagesCount; i++)
                {
                    // We assume that the image items are HEVC
                    HEVCImageItem hevcImageItem = (HEVCImageItem) overlayImageItem.getOverlayedImages().get(i).image;
                    byte[] decoderConfig = hevcImageItem.getDecoderConfig().getConfig();
                    byte[] imageData = hevcImageItem.getItemDataAsArray();
                    // Feed the data to a decoder

                    decoder.setImageConfig(decoderConfig);
                    decoder.setImage(imageData);
                }

                decoder.prepare(surface, fourCC, width, height);

                decoder.start();
            }
        }
        // All exceptions thrown by the HEIF library are of the same type
        // Check the error code to see what happened
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    /*
    for identity heif
     */
    void loadIdentityImage(String url, Surface surface) {
        String filename = url;
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            // Load the file
            heif.load(filename);

            // Get the primary image
            ImageItem primaryImage = heif.getPrimaryImage();

            //get the image size
            int width = primaryImage.getSize().width;
            int height = primaryImage.getSize().height;
            Log.i(TAG, "width:"+ width + " height:" + height);

            //fourCC
            String fourCC = primaryImage.getType().toString();
            Log.i(TAG, "fourcc:" + fourCC);

            //the primary image is grid like 2x2, 3x3 etc.
            //the primary image is an individual image??????

            // Check the type, assuming that it's a Grid image
            if (primaryImage instanceof IdentityImageItem)
            {
                VideoDecoder decoder = new VideoDecoder();

                IdentityImageItem identityImageItem = (IdentityImageItem) primaryImage;

                int imagesCount = identityImageItem.getSourceImages().size();
                Log.i(TAG, "images:" + imagesCount);

                // Go through the overlay imageitem
                for (int i = 0; i < imagesCount; i++)
                {
                    // We assume that the image items are HEVC
                    HEVCImageItem hevcImageItem = (HEVCImageItem) identityImageItem.getSourceImages().get(i);
                    byte[] decoderConfig = hevcImageItem.getDecoderConfig().getConfig();
                    byte[] imageData = hevcImageItem.getItemDataAsArray();
                    // Feed the data to a decoder

                    decoder.setImageConfig(decoderConfig);
                    decoder.setImage(imageData);
                }

                decoder.prepare(surface, fourCC, width, height);

                decoder.start();
            }
        }
        // All exceptions thrown by the HEIF library are of the same type
        // Check the error code to see what happened
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    /*
    for grid heif
     */
    void loadGridImage(String url, Surface surface) {
        String filename = url;
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            // Load the file
            heif.load(filename);

            // Get the primary image
            ImageItem primaryImage = heif.getPrimaryImage();

            //get the image size
            int width = primaryImage.getSize().width;
            int height = primaryImage.getSize().height;
            Log.i(TAG, "width:"+ width + " height:" + height);

            //fourCC
            String fourCC = primaryImage.getType().toString();
            Log.i(TAG, "fourcc:" + fourCC);

            //the primary image is grid like 2x2, 3x3 etc.
            //the primary image is an individual image??????

            // Check the type, assuming that it's a Grid image
            if (primaryImage instanceof GridImageItem)
            {
                VideoDecoder decoder = new VideoDecoder();

                GridImageItem gridImageItem = (GridImageItem) primaryImage;

                Log.i(TAG, "row:" + gridImageItem.getRowCount() + "column:" + gridImageItem.getColumnCount());

                // Go through the grid
                for (int rowIndex = 0; rowIndex < gridImageItem.getRowCount(); rowIndex++)
                {
                    for (int columnIndex = 0; columnIndex < gridImageItem.getColumnCount(); columnIndex++)
                    {
                        // We assume that the image items are HEVC
                        HEVCImageItem hevcImageItem = (HEVCImageItem) gridImageItem.getImage(columnIndex, rowIndex);
                        byte[] decoderConfig = hevcImageItem.getDecoderConfig().getConfig();
                        byte[] imageData = hevcImageItem.getItemDataAsArray();
                        // Feed the data to a decoder

                        decoder.setImageConfig(decoderConfig);
                        decoder.setImage(imageData);
                    }
                }

                decoder.prepare(surface, fourCC, width, height);

                decoder.start();
            }
        }
        // All exceptions thrown by the HEIF library are of the same type
        // Check the error code to see what happened
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    void saveGridImage() {
        // These should contain the encoded image data and the corresponding decoder config data
        int width = 640;
        int height = 480;
        // Sizes are just placeholders
        byte[] decoderConfig = new byte[1024];
        byte[] imageData = new byte[50000];

        String filename = "something/grid_something.heic";
        // Create an instance of the HEIF library,
        HEIF heif = new HEIF();
        try
        {
            final int columnCount = 3;
            final int rowCount = 2;
            // As an example, create a 3 x 2 grid
            GridImageItem gridImageItem = new GridImageItem(heif, columnCount, rowCount,
                    new Size (columnCount * width,
                            rowCount * height));
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
            {
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
                {
                    // Create image items for each grid, as an example we're using the same
                    // data for each image
                    HEVCImageItem hevcImageItem = new HEVCImageItem(heif,
                            new Size(640, 480),
                            decoderConfig, imageData);
                    // Set the image to the correct location
                    gridImageItem.setImage(columnIndex, rowIndex, hevcImageItem);
                }
            }

            // Set the grid as a primary image
            heif.setPrimaryImage(gridImageItem);

            // The brands need to be set
            heif.setMajorBrand(HEIF.BRAND_MIF1);
            heif.addCompatibleBrand(HEIF.BRAND_HEIC);

            // And we save the file
            heif.save(filename);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
