package com.csh.siren_demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.csh.lib_siren.Siren
import com.csh.lib_siren.ICompressObject
import com.csh.lib_siren.CompressConfig
import com.csh.lib_siren.ICompress
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.log

/**
 * @author sunhee
 * @intro
 * @date 2019/5/23
 */
class MainActivity : AppCompatActivity() {

    private lateinit var photos: MutableList<ICompressObject>
    private val TAG = "MainActivity";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         photos = mutableListOf<ICompressObject>()

        for (i in 1 .. 10){
            copyAssetAndWrite("$i.jpg")
            val pic = "$cacheDir/$i.jpg"
            photos.add(Photo(pic))
        }
        photos.add(Photo("故意弄一个错误的图片地址"))

        initClick()
    }

    private fun initClick() {
        var timer = 0L
        val mSiren1 = Siren.with(this)
            .setMaxWidth(960)
            .setMaxHeight(540)
            .setCacheDir("${this.filesDir}/image/960*540_q100")
            .setQuality(100)
            .setCompressListener(object : ICompress.CompressListener {
                override fun onComplete(photos: MutableList<File>) {
                    Toast.makeText(this@MainActivity,photos[0].absolutePath,Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"耗时1-------->${System.currentTimeMillis()-timer}")
                }
                override fun onFail(photos: MutableList<ICompressObject>, error: String?) {
                    Toast.makeText(this@MainActivity,error,Toast.LENGTH_SHORT).show()
                }

            })
            .build()
        button.setOnClickListener {
            timer = System.currentTimeMillis()
            mSiren1.load(photos).compress()
        }

        val mSiren2 = Siren.with(this)
            .setMaxWidth(1920)
            .setMaxHeight(1080)
            .setCacheDir("${this.filesDir}/image/1920*1080_q60")
            .setQuality(60)
            .setCompressListener(object : ICompress.CompressListener {
                override fun onComplete(photos: MutableList<File>) {
                    Log.d(TAG,"耗时2-------->${System.currentTimeMillis()-timer}")
                    Toast.makeText(this@MainActivity,photos[0].absolutePath,Toast.LENGTH_SHORT).show()
                }
                override fun onFail(photos: MutableList<ICompressObject>, error: String?) {
                    Toast.makeText(this@MainActivity,error,Toast.LENGTH_SHORT).show()
                }

            })
            .build()

        button2.setOnClickListener {
            mSiren2.load(photos).compress()
        }

        val mSiren3 = Siren.with(this)
            .setMaxWidth(960)
            .setMaxHeight(540)
            .setCacheDir("${this.filesDir}/image/960*540_q60")
            .setQuality(60)
            .setCompressListener(object : ICompress.CompressListener {
                override fun onComplete(photos: MutableList<File>) {
                    Log.d(TAG,"耗时3-------->${System.currentTimeMillis()-timer}")
                    Toast.makeText(this@MainActivity,photos[0].absolutePath,Toast.LENGTH_SHORT).show()
                }
                override fun onFail(photos: MutableList<ICompressObject>, error: String?) {
                    Toast.makeText(this@MainActivity,error,Toast.LENGTH_SHORT).show()
                }

            })
            .build()

        button3.setOnClickListener {
            mSiren3.load(photos).compress()
        }


    }


    /**
     * 只是个把图片从asset复制指定目录的方法
     */
    private fun copyAssetAndWrite(fileName: String): Boolean {
        try {
            val cacheDir = cacheDir
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val outFile = File(cacheDir, fileName)
            if (!outFile.exists()) {
                val res = outFile.createNewFile()
                if (!res) {
                    return false
                }
            } else {
                if (outFile.length() > 10) {//表示已经写入一次
                    return true
                }
            }
            val af = assets.open(fileName)
            val fos = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var byteCount: Int
            byteCount = af.read(buffer)
            while (byteCount != -1) {
                fos.write(buffer, 0, byteCount)
                byteCount = af.read(buffer)
            }
            fos.flush()
            af.close()
            fos.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }


}
