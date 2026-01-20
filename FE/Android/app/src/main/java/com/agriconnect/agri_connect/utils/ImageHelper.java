package com.agriconnect.agri_connect.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.agriconnect.agri_connect.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Helper class for loading images from various sources
 * Supports: URL, Base64 data URI, local file path
 */
public class ImageHelper {

    /**
     * Load image into ImageView from any source (URL, Base64, or local path)
     */
    public static void loadImage(Context context, String imageSource, ImageView imageView) {
        loadImage(context, imageSource, imageView, R.drawable.ic_placeholder);
    }

    /**
     * Load image into ImageView with custom placeholder
     */
    public static void loadImage(Context context, String imageSource, ImageView imageView, int placeholder) {
        if (context == null || imageView == null)
            return;

        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        // Check if it's a Base64 data URI
        if (imageSource.startsWith("data:image")) {
            loadBase64Image(imageSource, imageView, placeholder);
        } else {
            // Load from URL using Glide
            loadUrlImage(context, imageSource, imageView, placeholder);
        }
    }

    /**
     * Load Base64 encoded image
     */
    private static void loadBase64Image(String dataUri, ImageView imageView, int placeholder) {
        try {
            // Extract Base64 content from data URI
            String base64Content = dataUri;
            if (dataUri.contains(",")) {
                base64Content = dataUri.substring(dataUri.indexOf(",") + 1);
            }

            // Decode Base64 to Bitmap
            byte[] decodedBytes = Base64.decode(base64Content, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(placeholder);
        }
    }

    /**
     * Load image from URL using Glide
     */
    private static void loadUrlImage(Context context, String url, ImageView imageView, int placeholder) {
        try {
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(placeholder);
        }
    }

    /**
     * Load circular image (for avatars)
     */
    public static void loadCircularImage(Context context, String imageSource, ImageView imageView) {
        loadCircularImage(context, imageSource, imageView, R.drawable.ic_farmer);
    }

    /**
     * Load circular image with custom placeholder
     */
    public static void loadCircularImage(Context context, String imageSource, ImageView imageView, int placeholder) {
        if (context == null || imageView == null)
            return;

        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        // Check if it's a Base64 data URI
        if (imageSource.startsWith("data:image")) {
            loadBase64Image(imageSource, imageView, placeholder);
        } else {
            // Load from URL using Glide with circle crop
            try {
                Glide.with(context)
                        .load(imageSource)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(placeholder);
            }
        }
    }
}
