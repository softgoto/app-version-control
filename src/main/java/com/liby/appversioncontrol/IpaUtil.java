package com.liby.appversioncontrol;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author xuhui
 * @date 2019-11-23 21:12
 */
@SuppressWarnings("WeakerAccess")
public class IpaUtil {

    /**
     * 解压IPA文件，只获取IPA文件的Info.plist文件存储指定位置
     * @param file   zip文件
     * @param unzipDirectory     解压到的目录
     * @throws IOException e
     */
    private static File getZipInfo(File file, String unzipDirectory) throws IOException {
        // 定义输入输出流对象
        InputStream input = null;
        OutputStream output = null;
        File result = null;
        File unzipFile;
        ZipFile zipFile = null;
        try{
            // 创建zip文件对象
            zipFile = new ZipFile(file);
            // 创建本zip文件解压目录
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            unzipFile = new File(unzipDirectory + "/" + name);
            if(unzipFile.exists()){
                deleteDir(unzipFile);
            }
            unzipFile.mkdir();
            // 得到zip文件条目枚举对象
            Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
            // 定义对象
            ZipEntry entry;
            String entryName;
            String[] names;
            int length;
            // 循环读取条目
            while(zipEnum.hasMoreElements()){
                // 得到当前条目
                entry = zipEnum.nextElement();
                entryName = entry.getName();
                // 用/分隔条目名称
                names = entryName.split("\\/");
                length = names.length;
                for(int v = 0; v < length; v++){
                    // 为Info.plist文件,则输出到文件
                    if(entryName.endsWith(".app/Info.plist")){
                        input = zipFile.getInputStream(entry);
                        result = new File(unzipFile.getAbsolutePath() + "/Info.plist");
                        output = new FileOutputStream(result);
                        byte[] buffer = new byte[1024 * 8];
                        int readLen;
                        while((readLen = input.read(buffer, 0, 1024 * 8)) != -1){
                            output.write(buffer, 0, readLen);
                        }
                        break;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            if(input != null){
                input.close();
            }
            if(output != null){
                output.flush();
                output.close();
            }
            // 必须关流，否则文件无法删除
            if(zipFile != null){
                zipFile.close();
            }
        }

        // 如果有必要删除多余的文件
        if(file.exists()){
            file.delete();
        }
        return result;
    }

    /**
     * IPA文件的拷贝，把一个IPA文件复制为Zip文件,同时返回Info.plist文件
     * @param oldFile IPA文件
     * @return File
     */
    private static File getPlistFile(File oldFile) throws IOException {
        int byteRead;
        String filename = oldFile.getAbsolutePath().replaceAll(".ipa", ".zip");
        File newFile = new File(filename);
        if(oldFile.exists()){
            // 创建一个Zip文件
            InputStream inStream = new FileInputStream(oldFile);
            FileOutputStream fs = new FileOutputStream(newFile);
            byte[] buffer = new byte[1444];
            while((byteRead = inStream.read(buffer)) != -1){
                fs.write(buffer, 0, byteRead);
            }
            inStream.close();
            fs.close();
            // 解析Zip文件
            return getZipInfo(newFile, newFile.getParent());
        }else {
            return null;
        }
    }

    /**
     * 通过IPA文件获取Info信息
     * @param ipa ipa文件
     * @return ipa文件元信息
     * @throws IOException e
     * @throws ParserConfigurationException e
     * @throws ParseException e
     * @throws SAXException e
     * @throws PropertyListFormatException e
     */
    public static Map<String, String> getIpaInfo(File ipa) throws IOException, ParserConfigurationException, ParseException, SAXException, PropertyListFormatException {
        File plistFile = getPlistFile(ipa);
        if (plistFile == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plistFile);
        //APP签名标识
        String cfBundleIdentifier = rootDict.objectForKey("CFBundleIdentifier").toString();
        map.put("CFBundleIdentifier", cfBundleIdentifier);
        //应用程序发布版本号 Version
        String cfBundleShortVersionString = rootDict.objectForKey("CFBundleShortVersionString").toString();
        map.put("CFBundleShortVersionString", cfBundleShortVersionString);
        //应用程序内部标示 Build
        String cfBundleVersion = rootDict.objectForKey("CFBundleVersion").toString();
        map.put("CFBundleVersion", cfBundleVersion);
        // 应用展示的名称
        String cfBundleDisplayName = rootDict.objectForKey("CFBundleDisplayName").toString();
        map.put("CFBundleDisplayName", cfBundleDisplayName);

        // 如果有必要，应该删除解压的结果文件
        plistFile.delete();
        plistFile.getParentFile().delete();

        return map;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null || children.length == 0) {
                return false;
            }
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
