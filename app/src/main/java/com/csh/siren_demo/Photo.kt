package com.csh.siren_demo

import com.csh.lib_siren.ICompressObject

/**
 * @author chenshanghui
 * @date 2019/5/22
 */
class Photo(val path:String): ICompressObject {
    override fun getObjectPath(): String = path

}