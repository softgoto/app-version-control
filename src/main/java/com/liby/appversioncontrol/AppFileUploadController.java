package com.liby.appversioncontrol;

import com.google.gson.Gson;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * @author xuhui
 * @date 2019-11-23 11:29
 */
@Controller
@RequestMapping("/file")
public class AppFileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(AppFileUploadController.class);

    private static final String ANDROID_FILE_SUFFIX = "apk";
    private static final String IOS_FILE_SUFFIX = "ipa";

    @Value("${file.upload.dir}")
    private String filePath;

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam("env") String env, @RequestParam("type") String type) {
        if (file.isEmpty()) {
            return "请选择文件";
        }
        if (StringUtils.isEmpty(env)) {
            return "请选择服务环境";
        }
        if (StringUtils.isEmpty(type)) {
            return "请选择版本类型";
        }

        String fileName = file.getOriginalFilename();
        String suffix = "";
        if (!StringUtils.isEmpty(fileName)) {
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        logger.info("EVENT=APP文件上传，文件信息| env={}, type={} fileName={}, suffix={}", env, type, fileName, suffix);

        File dest = new File(filePath + fileName);
        try {
            //检查同名文件删除
            if (dest.exists()) {
                dest.delete();
            }

            file.transferTo(dest);
            logger.info("EVENT=文件已存储到服务器");
            //解析APP版本信息
            if (ANDROID_FILE_SUFFIX.equals(suffix)) {
                ApkMeta apkMeta = ApkUtil.getApkInfo(dest);
                logger.info("EVENT=APK包信息| apkMeta={}", new Gson().toJson(apkMeta));
            }else if (IOS_FILE_SUFFIX.equals(suffix)) {
                Map<String, String> map = IpaUtil.getIpaInfo(dest);
                if (map == null) {
                    logger.info("EVENT=读取plist文件失败| fileName={}", fileName);
                    return "上传失败";
                }
                logger.info("EVENT=IPA包信息| ipaInfo={}",new Gson().toJson(map));
            }
            return "上传成功";
        } catch (Exception e) {
            logger.error("EVENT=APP文件上传异常| e={}", e.toString());
        }
        return "上传失败";
    }

}
