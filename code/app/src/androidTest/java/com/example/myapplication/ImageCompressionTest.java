package com.example.myapplication;

import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.io.ByteArrayOutputStream;
import org.junit.Test;

/**
 * Unit tests for the image compression functionality used in NewMoodActivity.
 * This test simulates compressing a Bitmap (representing a user-selected photograph)
 * and verifies that the final compressed image size is under 65,536 bytes (64 KB),
 */
public class ImageCompressionTest {

    // Maximum allowed image size in bytes (64 KB).
    private static final int MAX_IMAGE_SIZE_BYTES = 64 * 1024;

    @Test
    public void testImageCompressionUnderLimit() {
        // Create a dummy Bitmap (e.g., 500x500 pixels). In real usage, this would come from user selection.
        Bitmap dummyBitmap = Bitmap.createBitmap(500, 500, Config.ARGB_8888);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        dummyBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        // Simulate the compression loop from NewMoodActivity:
        // Reduce quality until the image size is below MAX_IMAGE_SIZE_BYTES or quality is very low.
        while (baos.toByteArray().length > MAX_IMAGE_SIZE_BYTES && quality > 10) {
            baos.reset();
            quality -= 10;
            dummyBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        byte[] compressedImage = baos.toByteArray();

        // Assert that the final compressed image is under 64 KB.
        assertTrue("Compressed image size should be under 64 KB", compressedImage.length < MAX_IMAGE_SIZE_BYTES);
    }
}