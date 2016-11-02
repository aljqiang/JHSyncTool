package com.ljq.ftp.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 录音同步工具(通过文件传输方式同步录音文件到本地服务器)
 * User: Larry Lai
 * Date: 2016-08-26
 * Time: 15:51
 * Version: 1.0
 */

public class FileTranserDateCycleUtil {

    /**
     * 远端录音文件路径
     */
    private static StringBuffer remotePath;
    /**
     * 本地存储路径
     */
    private static StringBuffer localPath;
    /**
     * 调度器
     */
    static Timer timer;
    /**
     * 同步时间（间隔），单位：秒
     */
    private static int syncTime;

    public static void setParameter() {
        Properties properties = new Properties();
        try {
            InputStream in = FTPCommonUtil.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

            remotePath = new StringBuffer(String.valueOf(properties.getProperty("download.remotePath")));
            localPath = new StringBuffer(String.valueOf(properties.getProperty("download.localPath")));
            syncTime = Integer.valueOf(properties.getProperty("syncTime"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件复制
     *
     * @param src
     * @param dist
     * @return
     */
    public static boolean copy(File src, File dist) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dist);
            byte[] buf = new byte['?'];
            int length = in.read(buf);
            while (length > 0) {
                out.write(buf, 0, length);

                length = in.read(buf);
            }
            out.close();
            in.close();

            return true;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 遍历文件目录
     */
    private static String[] fileReader(String pathInput) {
        File file = new File(pathInput);
        File[] files = file.listFiles();

        String[] filepathList = null;

        try {
            int k = 0;
            String filePath = "";

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
//                    log.info("=============================文件路径：\n" + files[i].getPath().replace("\\","/"));
                    if (k != 0) {
                        filePath += ",";
                    }

                    filePath += files[i].getPath().replace("\\", "/");
                    k++;

                } else if (files[i].isDirectory()) {
                    LogUtil.error("找不到文件.");
                }
            }

            filepathList = filePath.split(",");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return filepathList;
    }

    /**
     * Main方法
     *
     * @param args
     */
    public static void main(String[] args) {

        // 设置时区，防止java获得的时区跟系统的时区不一样
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);

        FileTranserUtil.setParameter();
        LogUtil.info("录音文件同步程序启动...");

        try {

            LogUtil.info("########################开始同步录音文件########################");

            Date startDate = new SimpleDateFormat("yyyyMMdd").parse("20160801");  // 定义起始日期
            Date endDate = new SimpleDateFormat("yyyyMMdd").parse("20161103");  // 定义结束日期

            Calendar calendar = Calendar.getInstance();  // 定义日期实例
            calendar.setTime(startDate);  // 设置日期起始时间
            while (calendar.getTime().before(endDate)) {  // 判断是否到结束日期

                FileTranserUtil.setParameter();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String date = sdf.format(calendar.getTime());
                System.out.println(date);  // 输出日期结果
                calendar.add(Calendar.DAY_OF_YEAR, 1);  // 进行当前日期加1

                // 拼接文件路径
                remotePath.append(date);
                localPath.append(date);

                // 本地存储路径不存在则创建
                File fileLocal = new File(localPath.toString());
                if (!fileLocal.exists()) {
                    fileLocal.mkdir();
                }

                // 把本地已有录音文件放到数组里，判断录音文件是否已存在
                File[] files = fileLocal.listFiles();
                List<String> filesCheckArray = new ArrayList<String>();

                for (int j = 0; j < files.length; j++) {
                    if (files[j].isFile()) {
                        filesCheckArray.add(files[j].getName());
                    }
                }


                File remotePathFile = new File(remotePath.toString());
                String[] remotePathFileNames = remotePathFile.list();

                boolean flag = false;
                // 增量的方式同步录音文件
                for (int i = 0; remotePathFileNames != null && i < remotePathFileNames.length; i++) {
                    String fileName = remotePathFileNames[i];

                    if (!filesCheckArray.contains(fileName + ".wav")) {
                        flag = true;

                        LogUtil.info(" 开始将录音文件:[" + fileName + ".wav" + "]同步到本地文件目录[" + localPath.toString() + "]");

                        // 复制移动文件到发送目录
                        File dataFile = new File(remotePath.toString() + "\\" + fileName);

//                            File destFile = new File(localPath.toString() + "\\" + fileName + ".wav");
                        // 解决追加文件后缀.wav文件大小变小的问题
                        File destFile = new File(localPath.toString(), fileName + ".wav");

//                            LogUtil.info("开始将录音文件:[" + dataFile.getAbsolutePath() + "]同步到本地文件目录[" + destFile.getAbsolutePath() + "]");

                        // 迁移文件
                        boolean result = FileTranserUtil.copy(dataFile, destFile);
                    }
                }

                if (!flag) {
                    LogUtil.info("没有新的录音文件可同步.");
                }

                LogUtil.info("########################结束同步录音文件########################");

            }

        } catch (Exception e) {
            LogUtil.error("录音文件同步异常，错误为:" + e);
        }

    }
}
