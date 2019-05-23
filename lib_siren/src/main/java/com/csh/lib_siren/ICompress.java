package com.csh.lib_siren;

import android.content.Context;
import com.csh.lib_siren.ICompressObject;

import java.io.File;
import java.util.List;

/**
 * @author chenshanghui
 * @date 2019/1/28
 */
public interface ICompress {

    void compress();


    interface CompressListener {

        void onComplete(List<File> photos);
        void onFail(List<ICompressObject> photos, String error);

    }

}
