package com.ljq.ftp.util;

import com.enterprisedt.net.ftp.FTPException;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ¼��ͬ������(ͨ��FTP��ʽͬ��¼���ļ������ط�����)
 * User: Larry Lai
 * Date: 2016-08-24
 * Time: 10:39
 * Version: 1.0
 */

public class FTPCommonUtil_bak20160826 {

    private FTPClient ftp = null;
    /**
     * Ftp������
     */
    private static String server;
    /**
     * IP
     */
    private static String ip;

    /**
     * �û���
     */
    private static String uname;
    /**
     * ����
     */
    private static String password;
    /**
     * ���Ӷ˿ڣ�Ĭ��21
     */
    private static int port = 21;
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


    public FTPCommonUtil_bak20160826(String server, int port, String uname, String password) {
        this.server = server;
        if (this.port > 0) {
            this.port = port;
        }
        this.uname = uname;
        this.password = password;
        //��ʼ��
        ftp = new FTPClient();
    }

    /**
     * ����FTP������
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

            // �ļ�����,Ĭ����ASCII
            // ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //ftp.setControlEncoding("GBK");
            // ���ñ���ģʽ
            ftp.enterLocalPassiveMode();
            ftp.setConnectTimeout(2000);
            ftp.setBufferSize(1024);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // ftp.changeWorkingDirectory(workPath);
            // ��Ӧ��Ϣ
            int replyCode = ftp.getReplyCode();
            if ((!FTPReply.isPositiveCompletion(replyCode))) {
                // �ر�Ftp����
                closeFTPClient();
                // �ͷſռ�
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
     * ����FTP���Ӳ���
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
     * ��FTP��Ŀ¼�ϴ��ļ�
     *
     * @param localFile
     * @param newName   ���ļ���
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
                throw new Exception("�ļ��ϴ�ʧ��!");
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
     * ��FTP��Ŀ¼�ϴ��ļ�
     *
     * @param input
     * @param newName ���ļ���
     * @throws Exception
     */
    public Boolean uploadFile(InputStream input, String newName)
            throws Exception {
        boolean success = false;
        try {
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("�ļ��ϴ�ʧ��!");
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
     * ��FTPָ��·���ϴ��ļ�
     *
     * @param localFile
     * @param newName        ���ļ���
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

            // �ı䵱ǰ·����ָ��·��
            if (!this.changeDirectory(remoteFoldPath)) {
                LogUtil.error("������·������!");
                return false;
            }
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("�ļ��ϴ�ʧ��!");
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
     * ��FTPָ��·���ϴ��ļ�
     *
     * @param input
     * @param newName        ���ļ���
     * @param remoteFoldPath
     * @throws Exception
     */
    public Boolean uploadFile(InputStream input, String newName,
                              String remoteFoldPath) throws Exception {
        boolean success = false;
        try {
            // �ı䵱ǰ·����ָ��·��
            if (!this.changeDirectory(remoteFoldPath)) {
                LogUtil.error("������·������!");
                return false;
            }
            success = ftp.storeFile(newName, input);
            if (!success) {
                throw new Exception("�ļ��ϴ�ʧ��!");
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
     * ��FTP�����������ļ�
     *
     * @param remotePath FTP·��(�������ļ���)
     * @param fileName   �����ļ���
     * @param localPath  ����·��
     */
    public Boolean downloadFile(String remotePath, String fileName,
                                String localPath) throws Exception {

        BufferedOutputStream output = null;
        boolean success = false;
        try {
            // ��鱾��·��
            this.checkFileExist(localPath);
            // �ı乤��·��
            if (!this.changeDirectory(remotePath)) {
                LogUtil.error("������·��������");
                return false;
            }
            // �г���ǰ����·���µ��ļ��б�
            List<FTPFile> fileList = this.getFileList();
            if (fileList == null || fileList.size() == 0) {
                LogUtil.error("��������ǰ·���²������ļ���");
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
                throw new Exception("�ļ�����ʧ��!");
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
     * ��FTP��������ȡ�ļ���
     *
     * @param remoteFilePath
     * @return
     * @throws Exception
     */
    public InputStream downloadFile(String remoteFilePath) throws Exception {

        return ftp.retrieveFileStream(remoteFilePath);
    }

    /**
     * ��ȡFTP��������ָ��·���µ��ļ��б�
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
     * ��ȡFTP��������[ָ��·��]�µ��ļ��б�
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
     * ��ȡFTP������[��ǰ����·��]�µ��ļ��б�
     *
     * @return
     * @throws Exception
     */
    public List<FTPFile> getFileList() throws Exception {

        List<FTPFile> ftpfiles = Arrays.asList(ftp.listFiles());

        return ftpfiles;
    }

    /**
     * �ı�FTP����������·��
     *
     * @param remoteFoldPath
     */
    public Boolean changeDirectory(String remoteFoldPath) throws Exception {

        return ftp.changeWorkingDirectory(remoteFoldPath);
    }

    /**
     * ɾ���ļ�
     *
     * @param remoteFilePath
     * @return
     * @throws Exception
     */
    public Boolean deleteFtpServerFile(String remoteFilePath) throws Exception {

        return ftp.deleteFile(remoteFilePath);
    }

    /**
     * ����Ŀ¼
     *
     * @param remoteFoldPath
     * @return
     */
    public boolean createFold(String remoteFoldPath) throws Exception {

        boolean flag = ftp.makeDirectory(remoteFoldPath);
        if (!flag) {
            throw new Exception("����Ŀ¼ʧ��");
        }
        return false;
    }

    /**
     * ɾ��Ŀ¼
     *
     * @param remoteFoldPath
     * @return
     * @throws Exception
     */
    public boolean deleteFold(String remoteFoldPath) throws Exception {

        return ftp.removeDirectory(remoteFoldPath);
    }

    /**
     * ɾ��Ŀ¼�Լ��ļ�
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
     * ��鱾��·���Ƿ����
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
//            throw new Exception("����·��������,����!");
        } else {
            flag = true;
        }
        return flag;
    }


    /**
     * �ر�FTP����
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
            throw new Exception("�ر�FTP�������!");
        }
    }

    /**
     * �ر�FTP����
     *
     * @throws Exception
     */
    public void closeFTPClient() throws Exception {

        LogUtil.info("�ر�FTP����.");
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
            throw new IOException("û�з����ļ�");
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
     * Main����
     *
     * @param args
     */
    public static void main(String[] args) {

        // ����ʱ������ֹjava��õ�ʱ����ϵͳ��ʱ����һ��
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);

        FTPCommonUtil_bak20160826.setParameter();
        LogUtil.info("¼���ļ�ͬ����������...");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                FTPCommonUtil_bak20160826.setParameter();

                try {

                    LogUtil.info("########################��ʼͬ��¼���ļ�########################");

                    // ���ڸ�ʽ��
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    String date = dateFormat.format(new Date());

                    // ƴ���ļ�·��
                    remotePath.append(date);
                    localPath.append(date);

                    // ��ȡFTP����
                    FTPCommonUtil_bak20160826 fu = new FTPCommonUtil_bak20160826(ip, Integer.valueOf(port), uname, password);
                    boolean check=fu.connectFTPServer();

                    if(check){
                        LogUtil.info("����FTP����.");
                    }else {
                        throw new Exception("����FTP����ʧ��.");
                    }

                    FTPClient ftpClient = fu.getFtp();
                    ftpClient.cwd(remotePath.toString());
                    String[] fileFtp = ftpClient.listNames();

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


                    boolean flag=false;
                    // �����ķ�ʽͬ��¼���ļ�
                    for (int i = 0; i < fileFtp.length; i++) {
                        String fileName = fileFtp[i];

                        if (!filesCheckArray.contains(fileName+ ".wav")) {
                            flag=true;

                            LogUtil.info(" ��ʼ��¼���ļ�:[" + fileName + ".wav" + "]ͬ���������ļ�Ŀ¼[" + localPath.toString() + "]");
                            fu.downloadFile(remotePath.toString(), fileName, localPath.toString());
                        }
                    }

                    if(!flag){
                        LogUtil.info("û���µ�¼���ļ���ͬ��.");
                    }

                    LogUtil.info("########################����ͬ��¼���ļ�########################");
                    fu.closeFTPClient();

                } catch (Exception e) {
                    LogUtil.error("¼���ļ�ͬ���쳣������Ϊ:" + e);
                }

            }
        }, 1000, syncTime * 1000);
    }
}
