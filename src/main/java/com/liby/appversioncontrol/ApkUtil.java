package com.liby.appversioncontrol;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;

/**
 * @author xuhui
 * @date 2019-11-23 22:43
 */
@SuppressWarnings("WeakerAccess")
public class ApkUtil {

    /**
     * 获取APK文件相关信息
     * @param file apk文件
     * @return apk元数据
     * @throws IOException e
     */
    public static ApkMeta getApkInfo(File file) throws IOException {
        ApkFile apkFile = new ApkFile(file);
        return apkFile.getApkMeta();
    }

}
