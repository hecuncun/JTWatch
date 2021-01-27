package com.lhzw.bluetooth.dfu

import android.content.Context
import android.util.Log
import com.lhzw.bluetooth.dfu.uitls.FileHelper
import com.lhzw.bluetooth.dfu.uitls.ZipHelper
import com.lhzw.bluetooth.uitls.BaseUtils
import java.io.File

/**
 * Date： 2020/6/18 0018
 * Time： 17:15
 * Created by xtqb.
 */

class DfuBeanEvent(val mContext: Context, private val filePath: String, val path: String) {
    private var apolloConfig: String? = null
    private var apolloBinSize: Int = 0
    private var apolloBinPath: String? = null
    private var apolloDatSize = 0
    private var apolloBootSettingPath: String? = null
    private var nrf52ZipFilePath: String? = null
    private var nrf52Config: String? = null
    private var nrf52BinSize = 0
    private var nrf52BinPath: String? = null
    private var nrf52DatSize = 0
    private val TAG = DfuBeanEvent::class.java.simpleName
    private var dfuConfigCallbacks: DfuConfigCallBack? = null
    private var nrf52BootSettingPath: String? = null


    init {
        try {
            dfuConfigCallbacks = mContext as DfuConfigCallBack
        } catch (e: ClassCastException) {
            throw java.lang.ClassCastException("转换异常 : ${e.message}")
        }

        val file = File("$path/unZipDfu")
        if (file.exists()) {
            deleteFile(file)
        }
        createDfuFileFolder(file)
    }

    private fun deleteFile(file: File?) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            println("文件删除失败,请检查文件路径是否正确")
            return
        }
        //取得这个目录下的所有子文件对象
        val files = file.listFiles()
        //遍历该目录下的文件对象
        for (f in files) {
            //打印文件名
            val name = file.name
            println(name)
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory) {
                deleteFile(f)
            } else {
                f.delete()
            }
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete()
    }

    private fun createDfuFileFolder(file: File) {
        apolloConfig = null
        nrf52Config = null
        if (file.mkdir()) {
            try {
                ZipHelper.UnZipFolder(filePath, file.absolutePath)
                val apolloFile = File("${file.absolutePath}/dfu_image.zip")
                if (apolloFile.exists()) {
                    ZipHelper.UnZipFolder(apolloFile.absolutePath, "${file.absoluteFile}/dfu_image")
                    val apolloConfigFile = File("${file.absoluteFile}/dfu_image/config.txt")
                    if (apolloConfigFile.exists()) {
                        apolloConfig = FileHelper.readFile(apolloConfigFile.absolutePath)
                    }
                    val apolloZipFile = File("${file.absoluteFile}/dfu_image/bootloader_application_update.zip")
                    if (apolloZipFile.exists()) {
                        ZipHelper.UnZipFolder(apolloZipFile.absolutePath, "${file.absolutePath}/dfu_image/bootloader_application_update")
                        val apolloBinFile = File("${file.absoluteFile}/dfu_image/bootloader_application_update/dfu.bin")
                        if (apolloBinFile.exists()) {
                            apolloBinSize = apolloBinFile.length().toInt()
                            apolloBinPath = apolloBinFile.absolutePath
                        }
                        val apolloDatFile = File("${file.absoluteFile}/dfu_image/bootloader_application_update/dfu.dat")
                        if (apolloDatFile.exists()) {
                            apolloDatSize = apolloDatFile.length().toInt()
                            apolloBootSettingPath = apolloDatFile.absolutePath
                        }
                    }
                }

                val nrf52File = File("${file.absoluteFile}/dfu_image_ble.zip")
                if (nrf52File.exists()) {
                    nrf52ZipFilePath = nrf52File.absolutePath
                    ZipHelper.UnZipFolder(nrf52ZipFilePath, "${file.absoluteFile}/dfu_image_ble")
                    val nrf52ConfigFile = File("${file.absoluteFile}/dfu_image_ble/config.txt")
                    if (nrf52ConfigFile.exists()) {
                        nrf52Config = FileHelper.readFile(nrf52ConfigFile.absolutePath)
                    }
                    val nrf52ZipFile = File("${file.absoluteFile}/dfu_image_ble/bootloader_application.zip")
                    if (nrf52ZipFile.exists()) {
                        ZipHelper.UnZipFolder(nrf52ZipFile.absolutePath, "${file.absoluteFile}/dfu_image_ble/bootloader_application")
                        val nrf52BinFile = File("${file.absoluteFile}/dfu_image_ble/bootloader_application/dfu.bin")
                        if (nrf52BinFile.exists()) {
                            nrf52BinSize = nrf52BinFile.length().toInt()
                            nrf52BinPath = nrf52BinFile.absolutePath
                        }
                    }
                    val nrf52DatFile = File("${file.absoluteFile}/dfu_image_ble/bootloader_application/dfu.dat")
                    if (nrf52DatFile.exists()) {
                        nrf52DatSize = nrf52DatFile.length().toInt()
                        nrf52BootSettingPath = nrf52DatFile.absolutePath
                    }
                }
                Log.e("UPDATEWATCH", "apollo : $apolloConfig   nrf52Config ： $nrf52Config")
                BaseUtils.ifNotNull(apolloConfig, nrf52Config) { apollo, nrf52 ->
                    if (apollo.isNotEmpty() && nrf52.isNotEmpty()) {
                        dfuConfigCallbacks?.onDfuConfigCallback("升级包：\r\nAPOLLO:\r\n$apolloConfig\r\nNRF52832:\r\n$nrf52Config")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "解压失败 : ${e.message}")
                dfuConfigCallbacks?.onDfuConfigCallback("")
            }
        }
    }

    fun getApolloBinSize() = apolloBinSize
    fun getApolloBinPath() = apolloBinPath
    fun getApolloDatSize() = apolloDatSize
    fun getApolloBootSettingPath() = apolloBootSettingPath
    fun getNrf52BinSize() = nrf52BinSize
    fun getNrf52BinPath() = nrf52BinPath
    fun getNrf52DatSize() = nrf52DatSize
    fun getNrf52BootSettingPath() = nrf52BootSettingPath
}