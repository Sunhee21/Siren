package com.csh.lib_siren;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.*;

public class SirenCore {
        private File srcImg;
        private File tagImg;
        private int srcWidth;
        private int srcHeight;
        private CompressConfig compressConfig;

        public SirenCore(File srcImg, File tagImg, CompressConfig compressConfig) throws IOException {
            this.tagImg = tagImg;
            this.srcImg = srcImg;
            this.compressConfig = compressConfig;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;//先不改原图比例，取到原始宽高

            BitmapFactory.decodeStream(new FileInputStream(srcImg), null, options);
            this.srcWidth = options.outWidth;
            this.srcHeight = options.outHeight;
        }


        private Bitmap rotatingImage(Bitmap bitmap, int angle) {
            Matrix matrix = new Matrix();

            matrix.postRotate(angle);

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }


        /**
         * 赋值给 BitmapFactory.Options().inSampleSize
         *
         * @return 根据原图计算采样率 压缩
         */
        private int computeSize(int width, int height) {
            Log.d("SirenCore", "srcWidth: " + width + "srcHeight: " + height);
            int tempWidth = width % 2 == 1 ? width + 1 : width;
            int tempHeight = height % 2 == 1 ? height + 1 : height;

            int longSide = Math.max(tempWidth, tempHeight);
            int shortSide = Math.min(tempWidth, tempHeight);

            float scale = ((float) shortSide / longSide);
            if (scale <= 1 && scale > 0.5625) {
                if (longSide < 1664) {
                    return 1;
                } else if (longSide < 4990) {
                    return 2;
                } else if (longSide > 4990 && longSide < 10240) {
                    return 4;
                } else {
                    return longSide / 1280 == 0 ? 1 : longSide / 1280;
                }
            } else if (scale <= 0.5625 && scale > 0.5) {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            } else {
                return (int) Math.ceil(longSide / (1280.0 / scale));
            }
        }

        private static final String TAG = "SirenCore";

        public File compress() throws IOException {
            BitmapFactory.Options options = new BitmapFactory.Options();
//            单位像素占用内存大小从小到大排列： [ALPHA_8, RGB_56, ARGB_4444, ARGB_8888, RGBA_F16]
//            options.inPreferredConfig = Bitmap.Config.RGB_565; 经测试对实际文件大小没改变，好像是影响的每像素内存占用 先不考虑这个

            int actualHeight = this.srcHeight;
            int actualWidth = this.srcWidth;

            boolean isScaleCompress = false;//防止设置和原图相同像素 不满足这个条件actualHeight > reqHeight || actualWidth > reqWidth

            if (compressConfig.getMaxWidth() > 0 || compressConfig.getMaxHeight() > 0) {

                int reqWidth = compressConfig.getMaxWidth();
                int reqHeight = compressConfig.getMaxHeight();

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = reqWidth*1f / reqHeight;

                if (actualHeight > reqHeight || actualWidth > reqWidth) {
                    isScaleCompress = true;
                    //If Height is greater
                    if (imgRatio < maxRatio) {
                        imgRatio = reqHeight*1f / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight =  reqHeight;

                    }  //If Width is greater
                    else if (imgRatio > maxRatio) {
                        imgRatio = reqWidth*1f / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth =  reqWidth;
                    } else {
                        actualHeight =  reqHeight;
                        actualWidth =  reqWidth;
                    }
                }
            }

            options.inSampleSize = computeSize(actualWidth, actualHeight);//采样率压缩，使图片分辨率变小，和缩放压缩差不多效果
            Bitmap tagBitmap = BitmapFactory.decodeStream(new FileInputStream(srcImg), null, options);


            if (isScaleCompress) {
                //如果设置了最大宽或高则进行像素压缩
                Bitmap scaledBitmap = null;
                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }
                //把原图进行尺寸缩放，对图片观感有点损伤
                float ratioX = actualWidth / (float) tagBitmap.getWidth();
                float ratioY = actualHeight / (float) tagBitmap.getHeight();
                float middleX = actualWidth / 2.0f;
                float middleY = actualHeight / 2.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(tagBitmap, middleX - tagBitmap.getWidth() / 2,
                        middleY - tagBitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
                tagBitmap.recycle();
                tagBitmap = scaledBitmap;
            }
//
//
//            if (compressConfig.getMaxPixel() > 0 && tagBitmap.getWidth() * tagBitmap.getHeight() > compressConfig.getMaxPixel()) {//如果设置了不允许超过的像素最大值则进行像素压缩
//                tagBitmap = createScaledBitmap(tagBitmap, compressConfig.getMaxPixel(), true);//把原图进行尺寸缩放，对图片观感有点损伤
//            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            if (SirenUtils.SINGLE.isJPG(new FileInputStream(srcImg))) {
                tagBitmap = rotatingImage(tagBitmap, SirenUtils.SINGLE.getOrientation(new FileInputStream(srcImg)));
            }

            tagBitmap.compress(compressConfig.isFocusAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG
                    , compressConfig.getQuality()//PNG 将无视quality 参数
                    , stream);
            tagBitmap.recycle();

            FileOutputStream fos = new FileOutputStream(tagImg);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();

            return tagImg;
        }


        public static Bitmap createScaledBitmap(@NonNull Bitmap src, long maxPixel,
                                                boolean filter) {
            Matrix m = new Matrix();

            final int width = src.getWidth();
            final int height = src.getHeight();

            float ratio = width * 1.0f / height;//得到比例
            int x = (int) Math.sqrt(maxPixel / ratio); //假设有一个值x  (x^2) * ratio <= maxPixel  width = ratio*x  height = x  width * height <= maxPixel
            while (x * x * ratio > maxPixel) x--;

            final float sx = x * ratio / width;
            final float sy = x * 1f / height;
            m.setScale(sx, sy);
            return Bitmap.createBitmap(src, 0, 0, width, height, m, filter);
        }

    }