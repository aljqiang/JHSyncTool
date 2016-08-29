package com.ljq.ftp.util;

import com.enterprisedt.net.ftp.FTPException;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 录音同步工具(通过FTP方式同步录音文件到本地服务器)
 * User: Larry Lai
 * Date: 2016-08-24
 * Time: 10:39
 * Version: 1.0
 */

public class FTPCommonUtil_bak20160826 {

    private FTPClient ftp = null;
    /**
     * Ftp服务器
     */
    private static String server;
    /**
     * IP
     */
    private static String ip;

    /**
     * 用户名
     */
    private static String uname;
    /**
     * 密码
     */
    private static String password;
    /**
     * 连接端口，默认21
     */
    private static int port = 21;
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


    public FTPCommonUtil_bak20160826(String server, int port, String uname, String password) {
        this.server = server;
        if (this.port > 0) {
            this.port = port;
        }
        this.uname = uname;
        this.password = password;
        //初始化
        ftp = new FTPClient();
    }

    /**
     * 连接FTP服务器
     *
     * @return
     * @throws Exception
     */
    public boolean connectFTPServer() throws Exception {
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.configure(getFTPClientConfig());
            ftp.connect(this.server, this.port);
            if (!ftp.login(this.uname, this.password)) {
                ftp.logout();
                ftp = null;
                return false;
            }

            // 文件类型,默认是ASCII
            // ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //ftp.setControlEncoding("GBK");
            // 设置被动模式
            ftp.enterLocalPassiveMode();
            ftp.setConnectTimeout(2000);
            ftp.setBufferSize(1024);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // ftp.changeWorkingDirectory(workPath);
            // 响应信息
            int replyCode = ftp.getReplyCode();
            if ((!FTPReply.isPositiveCompletion(replyCode))) {
                // 关闭Ftp连接
                closeFTPClient();
                // 释放空间
                ftp = null;
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            ftp.disconnect();
            ftp = null;
            throw e;
        }
    }

    /**
     * 配置FTP连接参数
     *
     * @return
     * @throws Exception
     */
    public FTPClientConfig getFTPClientConfig() throws Exception {
        String systemKey = FTPClientConfig.SYST_NT;
        String serverLanguageCode = "zh";
        FTPClientConfig conf = new FTPClientConfig(systemKey);
        conf.setServerLanguageCode(serverLanguageCode);
        conf.setDefaultDateFormatStr("yyyy-MM-dd");
        return conf;
    }

    /**
     * 向FTP根目录上传文件
     *
     * @param localFile
     * @param newName   新文件名
     * @throws Exception
     */
    public Boolean uploadFile(String localFile, String newName)
            throws Exception {
        InputStream input = null;
        boolean success = false;
        try {
            File file = null;
            if (checkFileExist(localFile)) {
                file = new File(localFile);
            }
            input = new FileInputStream(file);
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("文件上传失败!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return success;
    }

    /**
     * 向FTP根目录上传文件
     *
     * @param input
     * @param newName 新文件名
     * @throws Exception
     */
    public Boolean uploadFile(InputStream input, String newName)
            throws Exception {
        boolean success = false;
        try {
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("文件上传失败!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return success;
    }

    /**
     * 向FTP指定路径上传文件
     *
     * @param localFile
     * @param newName        新文件名
     * @param remoteFoldPath
     * @throws Exception
     */
    public Boolean uploadFile(String localFile, String newName,
                              String remoteFoldPath) throws Exception {

        InputStream input = null;
        boolean success = false;
        try {
            File file = null;
            if (checkFileExist(localFile)) {
                file = new File(localFile);
            }
            input = new FileInputStream(file);

            // 改变当前路径到指定路径
            if (!this.changeDirectory(remoteFoldPath)) {
                LogUtil.error("服务器路径不存!");
                return false;
            }
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("文件上传失败!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return success;
    }

    /**
     * 向FTP指定路径上传文件
     *
     * @param input
     * @param newName        新文件名
     * @param remoteFoldPath
     * @throws Exception
     */
    public Boolean uploadFile(InputStream input, String newName,
                              String remoteFoldPath) throws Exception {
        boolean success = false;
        try {
            // 改变当前路径到指定路径
            if (!this.changeDirectory(remoteFoldPath)) {
                LogUtil.error("服务器路径不存!");
                return false;
            }
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("文件上传失败!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return success;
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param remotePath FTP路径(不包含文件名)
     * @param fileName   下载文件名
     * @param localPath  本地路径
     */
    public Boolean downloadFile(String remotePath, String fileName,
                                String localPath) throws Exception {

        BufferedOutputStream output = null;
        boolean success = false;
        try {
            // 检查本地路径
            this.checkFileExist(localPath);
            // 改变工作路径
            if (!this.changeDirectory(remotePath)) {
                LogUtil.error("服务器路径不存在");
                return false;
            }
            // 列出当前工作路径下的文件列表
            List<FTPFile> fileList = this.getFileList();
            if (fileList == null || fileList.size() == 0) {
                LogUtil.error("服务器当前路径下不存在文件！");
                return success;
            }
            for (FTPFile ftpfile : fileList) {
                if (ftpfile.getName().equals(fileName)) {
                    File localFilePath = new File(localPath + File.separator
                            + ftpfile.getName()+".wav");
                    output = new BufferedOutputStream(new FileOutputStream(
                            localFilePath));
                    success = ftp.retrieveFile(ftpfile.getName(), output);
                }
            }
            if (!success) {
                throw new Exception("文件下载失败!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return success;
    }

    /**
     * 从FTP服务器获取文件流
     *
     * @param remoteFilePath
     * @return
     * @throws Exception
     */
    public InputStream downloadFile(String remoteFilePath) throws Exception {

        return ftp.retrieveFileStream(remoteFilePath);
    }

    /**
     * 获取FTP服务器上指定路径下的文件列表
     *
     * @param remotePath
     * @return
     */
    public List<FTPFile> getFtpServerFileList(String remotePath)
            throws Exception {

        FTPListParseEngine engine = ftp.initiateListParsing(remotePath);
        List<FTPFile> ftpfiles = Arrays.asList(engine.getNext(25));

        return ftpfiles;
    }

    /**
     * 获取FTP服务器上[指定路径]下的文件列表
     *
     * @param remotePath
     * @return
     * @throws Exception
     */
    public List<FTPFile> getFileList(String remotePath) throws Exception {

        List<FTPFile> ftpfiles = Arrays.asList(ftp.listFiles(remotePath));

        return ftpfiles;
    }

    /**
     * 获取FTP服务器[当前工作路径]下的文件列表
     *
     * @return
     * @throws Exception
     */
    public List<FTPFile> getFileList() throws Exception {

        List<FTPFile> ftpfiles = Arrays.asList(ftp.listFiles());

        return ftpfiles;
    }

    /**
     * 改变FTP服务器工作路径
     *
     * @param remoteFoldPath
     */
    public Boolean changeDirectory(String remoteFoldPath) throws Exception {

        return ftp.changeWorkingDirectory(remoteFoldPath);
    }

    /**
     * 删除文件
     *
     * @param remoteFilePath
     * @return
     * @throws Exception
     */
    public Boolean deleteFtpServerFile(String remoteFilePath) throws Exception {

        return ftp.deleteFile(remoteFilePath);
    }

    /**
     * 创建目录
     *
     * @param remoteFoldPath
     * @return
     */
    public boolean createFold(String remoteFoldPath) throws Exception {

        boolean flag = ftp.makeDirectory(remoteFoldPath);
        if (!flag) {
            throw new Exception("创建目录失败");
        }
        return false;
    }

    /**
     * 删除目录
     *
     * @param remoteFoldPath
     * @return
     * @throws Exception
     */
    public boolean deleteFold(String remoteFoldPath) throws Exception {

        return ftp.removeDirectory(remoteFoldPath);
    }

    /**
     * 删除目录以及文件
     *
     * @param remoteFoldPath
     * @return
     */
    public boolean deleteFoldAndsubFiles(String remoteFoldPath)
            throws Exception {

        boolean success = false;
        List<FTPFile> list = this.getFileList(remoteFoldPath);
        if (list == null || list.size() == 0) {
            return deleteFold(remoteFoldPath);
        }
        for (FTPFile ftpFile : list) {

            String name = ftpFile.getName();
            if (ftpFile.isDirectory()) {
                success = deleteFoldAndsubFiles(remoteFoldPath + "/" + name);
                if (!success)
                    break;
            } else {
                success = deleteFtpServerFile(remoteFoldPath + "/" + name);
                if (!success)
                    break;
            }
        }
        if (!success)
            return false;
        success = deleteFold(remoteFoldPath);
        return success;
    }

    /**
     * 检查本地路径是否存在
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public boolean checkFileExist(String filePath) throws Exception {
        boolean flag = false;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
//            throw new Exception("本地路径不存在,请检查!");
        } else {
            flag = true;
        }
        return flag;
    }


    /**
     * 关闭FTP连接
     *
     * @param ftp
     * @throws Exception
     */
    public void closeFTPClient(FTPClient ftp) throws Exception {

        try {
            if (ftp.isConnected())
                ftp.logout();
            ftp.disconnect();
        } catch (Exception e) {
            throw new Exception("关闭FTP服务出错!");
        }
    }

    /**
     * 关闭FTP连接
     *
     * @throws Exception
     */
    public void closeFTPClient() throws Exception {

        LogUtil.info("关闭FTP连接.");
        this.closeFTPClient(this.ftp);
    }

    /**
     * Get Attribute Method
     */
    public FTPClient getFtp() {
        return ftp;
    }

    public String getServer() {
        return server;
    }

    public String getUname() {
        return uname;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    /**
     * Set Attribute Method
     */
    public void setFtp(FTPClient ftp) {
        this.ftp = ftp;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void download(String filename, OutputStream os, String key, String dir) throws IOException, FTPException {

        this.ftp.cwd(dir);
        InputStream inputStream = this.ftp.retrieveFileStream(filename);
        if (inputStream == null) {
            throw new IOException("没有发现文件");
        }
        byte[] buffer = new byte[1024 * 1024];
        int byteread = 0;
        while ((byteread = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, byteread);
            os.flush();
        }
        inputStream.close();
        os.close();

    }

    public static void setParameter() {
        Properties properties = new Properties();
        try {
            InputStream in = FTPCommonUtil_bak20160826.class.getClassLoader().getResourceAsStream("ftp.properties");
            properties.load(in);

            ip = String.valueOf(properties.getProperty("ftp.ip"));
            port = Integer.valueOf(properties.getProperty("ftp.port"));
            uname = String.valueOf(properties.getProperty("ftp.username"));
            password = String.valueOf(properties.getProperty("ftp.password"));
            remotePath = new StringBuffer(String.valueOf(properties.getProperty("ftp.download.remotePath")));
            localPath = new StringBuffer(String.valueOf(properties.getProperty("ftp.download.localPath")));
            syncTime = Integer.valueOf(properties.getProperty("ftp.syncTime"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        FTPCommonUtil_bak20160826.setParameter();
        LogUtil.info("录音文件同步程序启动...");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                FTPCommonUtil_bak20160826.setParameter();

                try {

                    LogUtil.info("########################开始同步录音文件########################");

                    // 日期格式化
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    String date = dateFormat.format(new Date());

                    // 拼接文件路径
                    remotePath.append(date);
                    localPath.append(date);

                    // 获取FTP连接
                    FTPCommonUtil_bak20160826 fu = new FTPCommonUtil_bak20160826(ip, Integer.valueOf(port), uname, password);
                    boolean check=fu.connectFTPServer();

                    if(check){
                        LogUtil.info("开启FTP连接.");
                    }else {
                        throw new Exception("开启FTP连接失败.");
                    }

                    FTPClient ftpClient = fu.getFtp();
                    ftpClient.cwd(remotePath.toString());
                    String[] fileFtp = ftpClient.listNames();

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


                    boolean flag=false;
                    // 增量的方式同步录音文件
                    for (int i = 0; i < fileFtp.length; i++) {
                        String fileName = fileFtp[i];

                        if (!filesCheckArray.contains(fileName+ ".wav")) {
                            flag=true;

                            LogUtil.info(" 开始将录音文件:[" + fileName + ".wav" + "]同步到本地文件目录[" + localPath.toString() + "]");
                            fu.downloadFile(remotePath.toString(), fileName, localPath.toString());
                        }
                    }

                    if(!flag){
                        LogUtil.info("没有新的录音文件可同步.");
                    }

                    LogUtil.info("########################结束同步录音文件########################");
                    fu.closeFTPClient();

                } catch (Exception e) {
                    LogUtil.error("录音文件同步异常，错误为:" + e);
                }

            }
        }, 1000, syncTime * 1000);
    }
}
