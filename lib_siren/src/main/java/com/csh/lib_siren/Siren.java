package com.csh.lib_siren;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片压缩
 *
 * @author chenshanghui
 */
public class Siren implements ICompress {

    private static final String TAG = "Siren";

    private CompressListener compressListener;


    private List<ICompressObject> mCompressObjects = new ArrayList<>();
    private List<File> mResults;
    private List<ICompressObject> mFails;


    private CompressConfig mCompressConfig;


//    private ExecutorService FIXED_THREAD_EXECUTOR;//线程池实例  设置5个核心线程参与压缩 效率提升不明显
    private CountDownLatch countDownLatch; //类似计数器，统计所有线程执行完


    private Siren(CompressConfig mCompressConfig) {
//        FIXED_THREAD_EXECUTOR = Executors.newFixedThreadPool(5);
        this.mCompressConfig = mCompressConfig;
        this.compressListener = mCompressConfig.getListener();
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public Siren load(List<ICompressObject> objects) {
        this.mCompressObjects.clear();
        this.mCompressObjects.addAll(objects);
        return this;
    }


    /**
     * 开始压缩
     */
    @Override
    public void compress() {
        mResults = new ArrayList<>();
        mFails = new ArrayList<>();
        countDownLatch = new CountDownLatch(mCompressObjects.size());//计数器重新初始化
        compressListener.onStart();
        if (mCompressObjects == null || mCompressObjects.isEmpty()) {
            compressListener.onFail(mCompressObjects, "集合内容为空");
            return;
        }

        for (int i = 0; i < mCompressObjects.size(); i++) {
            if (mCompressObjects.get(i) == null) {
                compressListener.onFail(mCompressObjects, "索引 " + i + " 为空");
                return;
            }
        }

        compress(mCompressObjects.get(0));
    }


    /**
     * 进行压缩
     *
     * @param obj
     */
    private void compress(ICompressObject obj) {
        CompressTask task = new CompressTask(obj, mCompressConfig.getCacheDir());
        AsyncTask.SERIAL_EXECUTOR.execute(task);
        nextCompress(obj);
    }


    /**
     * 下一张
     *
     * @param photo
     */
    private void nextCompress(ICompressObject photo) {
        int index = mCompressObjects.indexOf(photo);

        if (index == mCompressObjects.size() - 1) {
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(SIREN_COMPRESS_DONE);
                }
            });
        } else {
            compress(mCompressObjects.get(index + 1));
        }
    }


    /**
     * 压缩完成
     */
    private void compressDone() {
        if (mFails.size() > 0) compressListener.onFail(mFails, "存在压缩失败的图片");
        compressListener.onComplete(mResults);
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SIREN_COMPRESS_SUCCESS: {
                    File file = new File((String) msg.obj);
                    mResults.add(file);
                    compressListener.onNext(file);
                    break;
                }
                case SIREN_COMPRESS_DONE: {
                    compressDone();
                    break;
                }
                case SIREN_COMPRESS_FAIL: {
                    mFails.add((ICompressObject) msg.obj);
                    break;
                }
            }
        }
    };

    /**
     * 压缩任务
     */
    class CompressTask implements Runnable {

        ICompressObject obj;
        String outputDir;

        public CompressTask(ICompressObject obj, String outputDir) {
            this.obj = obj;
            this.outputDir = outputDir;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "run: 压缩任务进行");
                if (TextUtils.isEmpty(obj.getObjectPath())) {
                    Message msg = handler.obtainMessage(SIREN_COMPRESS_FAIL, obj);
                    handler.sendMessage(msg);
                    return;
                }

                File srcFile = new File(obj.getObjectPath());
                if (srcFile.length() == 0) {
                    Message msg = handler.obtainMessage(SIREN_COMPRESS_FAIL, obj);
                    handler.sendMessage(msg);
                    return;
                }

                //小于给定不压缩尺寸直接跳过，路径设置成原文件路径
                if (srcFile.length() < mCompressConfig.getMaxSize()) {
                    Message msg = handler.obtainMessage(SIREN_COMPRESS_SUCCESS, obj.getObjectPath());
                    handler.sendMessage(msg);
                    return;
                }

                File tagFile = new File(outputDir + "/" + srcFile.getName());//输出文件


                File result = new SirenCore(srcFile, tagFile, mCompressConfig).compress();
                Message msg = handler.obtainMessage(SIREN_COMPRESS_SUCCESS, result.getAbsolutePath());
                handler.sendMessage(msg);
            } catch (IOException e) {
                Message msg = handler.obtainMessage(SIREN_COMPRESS_FAIL, obj);
                handler.sendMessage(msg);
            } finally {
                countDownLatch.countDown();//线程池计数器 构造函数使用多少的值 就会等计数到那个值表示线程池执行完成
            }
        }
    }





    private static final int SIREN_COMPRESS_DONE = 0x02;
    private static final int SIREN_COMPRESS_FAIL = 0x01;
    private static final int SIREN_COMPRESS_SUCCESS = 0x00;


    public static class Builder {


        private static final String DEFAULT_DISK_CACHE_DIR = "compress_disk_cache";

        int unCompressPixel = 2000;


        int maxSize = 200 * 1024;


        @IntRange(from = 0, to = 100)
        int quality = 60;

        boolean focusAlpha;

        boolean isReserveRaw = true;

        long maxPixel = 0;

        @IntRange(from = 0)
        int maxWidth;

        @IntRange(from = 0)
        int maxHeight;

        String cacheDir;

        CompressListener listener;

        Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setUnCompressPixel(int unCompressPixel) {
            this.unCompressPixel = unCompressPixel;
            return this;
        }


        public Builder setCompressListener(@NonNull CompressListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder setQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder setFocusAlpha(boolean focusAlpha) {
            this.focusAlpha = focusAlpha;
            return this;
        }

        public Builder setReserveRaw(boolean reserveRaw) {
            isReserveRaw = reserveRaw;
            return this;
        }

        public Builder setMaxPixel(long maxPixel) {
            this.maxPixel = maxPixel;
            return this;
        }

        public Builder setCacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }


        public Siren build() {
            //如果没设置缓存目录，使用默认的
            if (TextUtils.isEmpty(cacheDir)) {
                cacheDir = SirenUtils.SINGLE.getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR).getAbsolutePath();
            } else
                cacheDir = SirenUtils.SINGLE.isFileNotExists(new File(cacheDir))
                        ? SirenUtils.SINGLE.getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR).getAbsolutePath()
                        : cacheDir;

            return new Siren(new CompressConfig(this));
        }

    }

}
