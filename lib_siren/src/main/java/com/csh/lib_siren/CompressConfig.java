package com.csh.lib_siren;

import android.support.annotation.IntRange;

/**
 * @author chenshanghui
 */
public class CompressConfig{


    /**
     * 小于2000（默认）像素时不压缩
     */
    private int unCompressPixel;


    /**
     * 压缩后的大小不得超过 默认200 KB
     */
    private int maxSize;


    /**
     * 图片质量 压缩成PNG格式（focusAlpha为true时），此设置无效
     */
    @IntRange(from = 0,to = 100)
    private int quality;


    /**
     * 是否保留透明通道，保留压缩成PNG（PNG是质量无损图），不保留即压缩成JPEG，基本都是JPEG压缩，用处不大
     */
    private boolean focusAlpha;

    /**
     * 是否保留源文件
     */
    private boolean isReserveRaw ;


    /**
     * 图片像素最大值 默认0 如果设置此值则进行像素压缩 如设置2073600（1920*1080） 原图 9216000（3840*2400） 压缩结果为 1866240（1728*1080）
     */
    private long maxPixel;


    /**
     * 最大宽 默认 0
     */
    @IntRange(from = 0)
    private int maxWidth;
    /**
     * 最大高 默认 0
     */
    @IntRange(from = 0)
    private int maxHeight;


    /**
     * 缓存目录
     */
    private String cacheDir;

    private ICompress.CompressListener listener;


     CompressConfig(Siren.Builder builder){
        this.maxPixel = builder.maxPixel;
        this.maxSize = builder.maxSize;
        this.cacheDir = builder.cacheDir;
        this.isReserveRaw = builder.isReserveRaw;
        this.quality = builder.quality;
        this.unCompressPixel = builder.unCompressPixel;
        this.maxWidth = builder.maxWidth;
        this.maxHeight = builder.maxHeight;
        this.maxHeight = builder.maxHeight;
        this.listener = builder.listener;
    }

    public ICompress.CompressListener getListener(){
         return listener;
    }

    public int getMaxSize() {
        return maxSize;
    }


    public int getQuality() {
        return quality;
    }


    public String getCacheDir() {
        return cacheDir;
    }

    public boolean isFocusAlpha() {
        return focusAlpha;
    }

    public int getUnCompressPixel() {
        return unCompressPixel;
    }

    public boolean isReserveRaw() {
        return isReserveRaw;
    }

    public long getMaxPixel() {
        return maxPixel;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }


}
