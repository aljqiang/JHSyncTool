package com.ljq.ftp.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ¼��ͬ������(ͨ���ļ����䷽ʽͬ��¼���ļ������ط�����)
 * User: Larry Lai
 * Date: 2016-08-26
 * Time: 15:51
 * Version: 1.0
 */

public class FileTranserUtil {

    /**
     * Զ��¼���ļ�·��
     */
    private static StringBuffer remotePath;
    /**
     * ���ش洢·��
     */
    private static StringBuffer localPath;
    /**
     * ������
     */
    static Timer timer;
    /**
     * ͬ��ʱ�䣨���������λ����
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
     * �ļ�����
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
     * �����ļ�Ŀ¼
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
//                    log.info("=============================�ļ�·����\n" + files[i].getPath().replace("\\","/"));
                    if (k != 0) {
                        filePath += ",";
                    }

                    filePath += files[i].getPath().replace("\\", "/");
                    k++;

                } else if (files[i].isDirectory()) {
                    LogUtil.error("�Ҳ����ļ�.");
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
     * Main����
     *
     * @param args
     */
    public static void main(String[] args) {

        // ����ʱ������ֹjava��õ�ʱ����ϵͳ��ʱ����һ��
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);

        FileTranserUtil.setParameter();
        LogUtil.info("¼���ļ�ͬ����������...");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                FileTranserUtil.setParameter();

                try {

                    LogUtil.info("########################��ʼͬ��¼���ļ�########################");

                    // ���ڸ�ʽ��
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    String date = dateFormat.format(new Date());

                    // ƴ���ļ�·��
                    remotePath.append(date);
                    localPath.append(date);

                    // ���ش洢·���������򴴽�
                    File fileLocal = new File(localPath.toString());
                    if (!fileLocal.exists()) {
                        fileLocal.mkdir();
                    }

                    // �ѱ�������¼���ļ��ŵ�������ж�¼���ļ��Ƿ��Ѵ���
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
                    // �����ķ�ʽͬ��¼���ļ�
                    for (int i = 0; i < remotePathFileNames.length; i++) {
                        String fileName = remotePathFileNames[i];

                        if (!filesCheckArray.contains(fileName + ".wav")) {
                            flag = true;

                            LogUtil.info(" ��ʼ��¼���ļ�:[" + fileName + ".wav" + "]ͬ���������ļ�Ŀ¼[" + localPath.toString() + "]");

                            // �����ƶ��ļ�������Ŀ¼
                            File dataFile = new File(remotePath.toString() + "\\" + fileName);
                            File destFile = new File(localPath.toString() + "\\" + fileName + ".wav");

//                            LogUtil.info("��ʼ��¼���ļ�:[" + dataFile.getAbsolutePath() + "]ͬ���������ļ�Ŀ¼[" + destFile.getAbsolutePath() + "]");

                            // Ǩ���ļ�
                            boolean result = FileTranserUtil.copy(dataFile, destFile);
                        }
                    }

                    if (!flag) {
                        LogUtil.info("û���µ�¼���ļ���ͬ��.");
                    }

                    LogUtil.info("########################����ͬ��¼���ļ�########################");

                } catch (Exception e) {
                    LogUtil.error("¼���ļ�ͬ���쳣������Ϊ:" + e);
                }

            }
        }, 1000, syncTime * 1000);
    }
}
