/*
 * Copyright 2008 ZXing authors
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

import java.util.Map;

/**
 * This is a factory class which finds the appropriate Writer subclass for the BarcodeFormat
 * requested and encodes the barcode with the supplied contents.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class MultiFormatWriter implements Writer {

    @Override
    public BitMatrix encode(String contents,
                            BarcodeFormat format,
                            int width,
                            int height) throws WriterException {
        return encode(contents, format, width, height, null);
    }

    @Override
    public BitMatrix encode(String contents,
                            BarcodeFormat format,
                            int width, int height,
                            Map<EncodeHintType, ?> hints) throws WriterException {

        Writer writer = null;
        switch (format) {
            case EAN_8:
                break;
            case EAN_13:
                break;
            case UPC_A:
                break;
            case QR_CODE:
                writer = new QRCodeWriter();
                break;
            case CODE_39:
                break;
            case CODE_128:
                break;
            case ITF:
                break;
            case PDF_417:
                break;
            case CODABAR:
                break;
            case DATA_MATRIX:
                break;
            case AZTEC:
                break;
            default:
                throw new IllegalArgumentException("No encoder available for format " + format);
        }
        return writer.encode(contents, format, width, height, hints);
    }

}
