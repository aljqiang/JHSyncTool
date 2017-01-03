package com.ljq.FileTransfer;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

import java.io.*;

/**
 * User: Larry Lai
 * Date: 2016-06-20
 * Time: 16:05
 * Version: 1.0
 */

public class FtpUtil {
    private static Logger logger = Logger.getLogger(FtpUtil.class);
    private static FTPClient ftp;

    public static boolean connectFtp(FtpEntity f)
            throws Exception {
        ftp = new FTPClient();
        boolean flag = false;

        if (f.getPort() == null)
            ftp.connect(f.getIpAddr(), 21);
        else {
            ftp.connect(f.getIpAddr(), f.getPort().intValue());
        }
        ftp.login(f.getUserName(), f.getPwd());
        ftp.setFileType(2);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            return flag;
        }
        ftp.changeWorkingDirectory(f.getPath());
        flag = true;
        return flag;
    }

    public static void closeFtp() {
        if ((ftp != null) && (ftp.isConnected()))
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void upload(File f)
            throws Exception {
        if (f.isDirectory()) {
            ftp.makeDirectory(f.getName());
            ftp.changeWorkingDirectory(f.getName());
            String[] files = f.list();
            for (String fstr : files) {
                File file1 = new File(f.getPath() + "/" + fstr);
                if (file1.isDirectory()) {
                    upload(file1);
                    ftp.changeToParentDirectory();
                } else {
                    File file2 = new File(f.getPath() + "/" + fstr);
                    FileInputStream input = new FileInputStream(file2);
                    ftp.storeFile(file2.getName(), input);
                    input.close();
                }
            }
        } else {
            File file2 = new File(f.getPath());
            FileInputStream input = new FileInputStream(file2);
            ftp.storeFile(file2.getName(), input);
            input.close();
        }
    }

    public static void startDown(FtpEntity f, String localBaseDir, String remoteBaseDir)
            throws Exception {
        if (connectFtp(f))
            try {
                FTPFile[] files = (FTPFile[]) null;
                boolean changedir = ftp.changeWorkingDirectory(remoteBaseDir);
                if (!changedir) return;
                ftp.setControlEncoding("GBK");
                files = ftp.listFiles();
                for (int i = 0; i < files.length; i++)
                    try {
                        downloadFile(files[i], localBaseDir, remoteBaseDir);
                    } catch (Exception e) {
                        logger.error(e);
                        logger.error("<" + files[i].getName() + ">???????");
                    }
            } catch (Exception e) {
                logger.error(e);
                logger.error("???????��?????");
            }
        else
            logger.error("????????");
    }

    private static void downloadFile(FTPFile ftpFile, String relativeLocalPath, String relativeRemotePath) {
        if (ftpFile.isFile()) {
            if (ftpFile.getName().indexOf("?") == -1) {
                OutputStream outputStream = null;
                try {
                    File locaFile = new File(relativeLocalPath + ftpFile.getName());

                    if (locaFile.exists()) {
                        return;
                    }
                    outputStream = new FileOutputStream(relativeLocalPath + ftpFile.getName());
                    ftp.retrieveFile(ftpFile.getName(), outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    logger.error(e);
                    try {
                        if (outputStream == null) return;
                        outputStream.close();
                    } catch (IOException ex) {
                        logger.error("??????????");
                    }
                } finally {
                    try {
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException e) {
                        logger.error("??????????");
                    }
                }
                try {
                    if (outputStream == null) return;
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("??????????");
                }
            }
        } else {
            String newlocalRelatePath = relativeLocalPath + ftpFile.getName();
            String newRemote = new String(relativeRemotePath + ftpFile.getName().toString());
            File fl = new File(newlocalRelatePath);
            if (!fl.exists())
                fl.mkdirs();
            try {
                newlocalRelatePath = newlocalRelatePath + '/';
                newRemote = newRemote + "/";
                String currentWorkDir = ftpFile.getName().toString();
                boolean changedir = ftp.changeWorkingDirectory(currentWorkDir);
                if (changedir) {
                    FTPFile[] files = (FTPFile[]) null;
                    files = ftp.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        downloadFile(files[i], newlocalRelatePath, newRemote);
                    }
                }
                if (changedir)
                    ftp.changeToParentDirectory();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        FtpEntity f = new FtpEntity();
        f.setIpAddr("192.168.7.27");
        f.setPort(Integer.valueOf(21));
        f.setUserName("hanxiaoyude27");
        f.setPwd("my13145920");
        f.setPath("E:\\x");
        System.out.println(connectFtp(f));
        File file = new File("E:\\test\\1\\111.txt");
        upload(file);

        System.out.println("ok");
        getButton_actionPerformed();
    }

    static void ftpList_actionPerformed() {
        String server = "192.168.7.27";
        String user = "hanxiaoyude27";
        String password = "my13145920";
        String path = "";
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.openServer(server);
            ftpClient.login(user, password);
            if (path.length() != 0)
                ftpClient.cd(path);
            TelnetInputStream is = ftpClient.list();
            int c;
            while ((c = is.read()) != -1) {
                System.out.print((char) c);
            }
            is.close();
            ftpClient.closeServer();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    static void getButton_actionPerformed() {
        String server = "192.168.7.27";
        String user = "hanxiaoyude27";
        String password = "my13145920";
        String path = "";
        String filename = "111.txt";
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.openServer(server);
            ftpClient.login(user, password);

            if (path.length() != 0) {
                ftpClient.cd(path);
            }
            ftpClient.binary();
            TelnetInputStream is = ftpClient.get(filename);
            File file_out = new File("E:/x/12.txt");
            FileOutputStream os = new FileOutputStream(file_out);
            byte[] bytes = new byte[1024];
            int c;
            while ((c = is.read(bytes)) != -1) {

                os.write(bytes, 0, c);
            }
            is.close();
            os.close();
            ftpClient.closeServer();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
