/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudsynch.quickshare.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeEncoder {

    private static final String TAG = QRCodeEncoder.class.getSimpleName();
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private final Activity activity;
    private final int dimension;
    private final boolean useVCard;
    private String contents;
    private String displayContents;
    private String title;
    private BarcodeFormat format;

    public QRCodeEncoder(Activity activity, String contents, String format, int dimension, boolean useVCard) throws WriterException {
        this.activity = activity;
        this.dimension = dimension;
        this.useVCard = useVCard;
        this.contents = contents;
        encodeContentsFromZXingIntent(format);
    }

    private static Iterable<String> toIterable(String[] values) {
        return values == null ? null : Arrays.asList(values);
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    String getContents() {
        return contents;
    }

    String getDisplayContents() {
        return displayContents;
    }

    String getTitle() {
        return title;
    }

    boolean isUseVCard() {
        return useVCard;
    }

    // It would be nice if the string encoding lived in the core ZXing library,
    // but we use platform specific code like PhoneNumberUtils, so it can't.
    private void encodeContentsFromZXingIntent(String formatString) {
        // Default to QR_CODE if no format given.
        format = null;

        if (formatString != null) {
            try {
                format = BarcodeFormat.valueOf(formatString);
            } catch (IllegalArgumentException iae) {
            }
        }
    }

    public Bitmap encodeAsBitmap() throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, dimension, dimension, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
