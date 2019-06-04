# 简易图片压缩封装——[Siren](https://github.com/Sunhee21/Siren)
## 图片压缩方式简略介绍![图片](https://uploader.shimo.im/f/qrLCtWIQ11wPco5T.png!thumbnail)

两个源码比较鲜明易懂的框架

### 鲁班压缩
[https://github.com/Curzibn/Luban](https://github.com/Curzibn/Luban)

鲁班主要向微信朋友圈的压缩质量靠近 代码风格写的舒服 但是里面只用了采样压缩和质量压缩
### 印度老哥写的压缩
[https://github.com/zetbaitsu/Compressor](https://github.com/zetbaitsu/Compressor)

印度老哥里面压缩文件就2个，里面用了像素压缩，设置宽高后按照目标宽高，把原图宽高取目标宽高的最短边按比例压缩，例如： 1920 * 1080 的图，目标宽高2000 * 500，最终成像888*500。

→→结合(抄袭)两者自己封装了一个**Siren**
(不太好的地方就是目前采用回调的方式)
```

```
**用法**
```
Kotlin Code

private lateinit var photos: MutableList<ICompressObject>

val mSiren1 = Siren.with(this)
    .setMaxWidth(960)
    .setMaxHeight(540)
    .setCacheDir("${this.filesDir}/image/960*540_q100")
    .setQuality(100)
    .setCompressListener(object : ICompress.CompressListener {

        override fun onStart() {
            timer = System.currentTimeMillis();
        }

        override fun onNext(file: File?) {
        //每压缩一个文件都会经过这个方法
            Log.d(TAG,"File1-------->${file?.absolutePath}")
        }

        override fun onComplete(photos: MutableList<File>) {
            //压缩成功的图片们
              Toast.makeText(this@MainActivity,photos[0].absolutePath,Toast.LENGTH_SHORT).show()
            Log.d(TAG,"timer-------->${System.currentTimeMillis()-timer}")
        }
        override fun onFail(photos: MutableList<ICompressObject>, error: String?) {
            //压缩失败的图片们
            Toast.makeText(this@MainActivity,error,Toast.LENGTH_SHORT).show()
        }

    })
    .build()//生成一种压缩配置的Siren对象 之后这个对象压缩出来的都是这个配置

mSiren1.load(photos).compress()//单张的方法名一样。不要连续调用，等上次压缩Complete才能再执行load
```

