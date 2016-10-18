package com.ljq.FileTransfer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * �ļ����ݳ���
 * User: Larry Lai
 * Date: 2016-06-20
 * Time: 16:04
 * Version: 1.0
 */

public class CopyDir {

    public static File dirFrom;
    public static File dirTo;
    public static String shieldFile;
    static String originalPath;
    static String newPath;
    static String runningTime;
    static Timer timer;

    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    static{
        CopyDir.init();
    }

    public void listFileInDir(File file) {

        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // ����ʱ��

        File[] files = file.listFiles();

        // ���������ε��ļ���
        String[] shieldFileArry=shieldFile.split(",");

        boolean flag;

        for (File f : files) {

            flag=false;
            for (int i = 0; i < shieldFileArry.length && !flag; i++) {

                flag=f.getAbsolutePath().contains(shieldFileArry[i]);
            }

            if (!flag) {

                String tempfrom = f.getAbsolutePath();

                String tempto = tempfrom.replace(dirFrom.getAbsolutePath(),
                        dirTo.getAbsolutePath());

                if (f.isDirectory()) {
                    File tempFile = new File(tempto);
                    tempFile.mkdirs();
                    listFileInDir(f);
                } else {
//                    System.out.println("Դ�ļ�:" + f.getAbsolutePath());

                    int endindex = tempto.lastIndexOf("\\");

                    String mkdirPath = tempto.substring(0, endindex);

                    File tempFile = new File(mkdirPath);

                    tempFile.mkdirs();

//                    System.out.println("Ŀ���ļ�:" + tempto);

                    System.out.println(format.format(new Date())+" ��ʼ��Դ�ļ�:[" + f.getAbsolutePath() + "]ת�Ƶ�Ŀ¼�ļ�[" + tempto + "]");

                    copy(tempfrom, tempto);
                }
            }

        }
    }

    public void copy(String from, String to) {
        try {
            InputStream in = new FileInputStream(from);

            OutputStream out = new FileOutputStream(to);

            byte[] buff = new byte[1024];

            int len = 0;

            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }

            in.close();

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setParameter() {
        Properties prop = new Properties();
        try {
            InputStream in = CopyDir.class.getResourceAsStream("config.properties");
            prop.load(in);

            originalPath = prop.getProperty("originalPath").trim();
            newPath = prop.getProperty("newPath").trim();
            shieldFile = prop.getProperty("shieldFile").trim();
            runningTime = prop.getProperty("runningTime").trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ���ӻ��������
     * @param date
     * @param num
     * @return
     */
    public static Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    /**
     * ��������Ƿ����Ԫ��(ʹ��List)
     * @param arr
     * @param targetValue
     * @return
     */
    public static boolean useList(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

    public static void init(){
        CopyDir.setParameter();
        System.out.println(CopyDir.originalPath + " " + CopyDir.newPath + " " + CopyDir.shieldFile);
        System.out.println("########################��ʼ����########################");
        System.out.println("Դ�ļ�Ŀ¼��"+ CopyDir.originalPath + "\n" + "Ŀ���ļ�Ŀ¼��" + CopyDir.newPath + "\n" + "�����ļ�Ŀ¼��" + CopyDir.shieldFile);
        File fromfile = new File(CopyDir.originalPath);
        File tofile = new File(CopyDir.newPath);
        CopyDir copy = new CopyDir();

        CopyDir.dirFrom = fromfile;
        CopyDir.dirTo = tofile;

        copy.listFileInDir(fromfile);

        System.out.println("########################���ݽ���########################");
    }

    public static void main(String[] args) {
        System.out.println("�����ʼ��...");
        setParameter();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // ����ʱ��
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(runningTime));  // �賿1��
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date=calendar.getTime();  // ��һ��ִ�ж�ʱ�����ʱ��
        // �����һ��ִ�ж�ʱ�����ʱ�� С�ڵ�ǰ��ʱ��
        // ��ʱҪ�� ��һ��ִ�ж�ʱ�����ʱ���һ�죬�Ա���������¸�ʱ���ִ�С��������һ�죬���������ִ�С�
        if (date.before(new Date())) {
            date = CopyDir.addDay(date, 1);
        }
//        Timer timer = new Timer();
//        Task task = new Task();
//        //����ָ����������ָ����ʱ�俪ʼ�����ظ��Ĺ̶��ӳ�ִ�С�
//        timer.schedule(task,date,PERIOD_DAY);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                CopyDir.setParameter();
                System.out.println(CopyDir.originalPath + " " + CopyDir.newPath + " " + CopyDir.shieldFile);
                System.out.println("########################��ʼ����########################");
                System.out.println("Դ�ļ�Ŀ¼��"+ CopyDir.originalPath + "\n" + "Ŀ���ļ�Ŀ¼��" + CopyDir.newPath + "\n" + "�����ļ�Ŀ¼��" + CopyDir.shieldFile);
                File fromfile = new File(CopyDir.originalPath);
                File tofile = new File(CopyDir.newPath);
                CopyDir copy = new CopyDir();

                CopyDir.dirFrom = fromfile;
                CopyDir.dirTo = tofile;

                copy.listFileInDir(fromfile);

                System.out.println("########################���ݽ���########################");
            }
        }
                , date, PERIOD_DAY);

//        CopyDir.setParameter();
//        System.out.println(CopyDir.originalPath + " " + CopyDir.newPath + " " + CopyDir.shieldFile);
//        System.out.println("########################��ʼ����########################");
//        System.out.println("Դ�ļ�Ŀ¼��"+CopyDir.originalPath + "\n" + "Ŀ���ļ�Ŀ¼��" + CopyDir.newPath + "\n" + "�����ļ�Ŀ¼��" + CopyDir.shieldFile);
//        File fromfile = new File(CopyDir.originalPath);
//        File tofile = new File(CopyDir.newPath);
//        CopyDir copy = new CopyDir();
//
//        CopyDir.dirFrom = fromfile;
//        CopyDir.dirTo = tofile;
//
//        copy.listFileInDir(fromfile);
//
//        System.out.println("########################���ݽ���########################");
    }
}
