package com.ljq.ftp.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * User: Larry Lai
 * Date: 2016-08-24
 * Time: 18:45
 * Version: 1.0
 */

public class FTPUtil {
    private static Logger logger = Logger.getLogger(FTPUtil.class);



    /**
     * ��ȡFTPClient����
     * @param ftpHost FTP����������
     * @param ftpPassword FTP ��¼����
     * @param ftpUserName FTP��¼�û���
     * @param ftpPort FTP�˿� Ĭ��Ϊ21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpPassword,
                                         String ftpUserName, int ftpPort) {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// ����FTP������
            ftpClient.login(ftpUserName, ftpPassword);// ��½FTP������
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.info("δ���ӵ�FTP���û������������");
                ftpClient.disconnect();
            } else {
                logger.info("FTP���ӳɹ���");
            }
        } catch (SocketException e) {
            e.printStackTrace();
            logger.info("FTP��IP��ַ���ܴ�������ȷ���á�");
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("FTP�Ķ˿ڴ���,����ȷ���á�");
        }
        return ftpClient;
    }

    public static void main(String[] args) {
//        FTPUtil.getFTPClient("192.168.1.40","qlzqbf","qlzq123",21);
//        FTPUtil.getFTPClient("192.168.50.232","CRM@2016!@#*","ftp_test",21);
        FTPUtil.getFTPClient("192.168.1.40","qlzq123","qlzqbf",21);
    }
}
