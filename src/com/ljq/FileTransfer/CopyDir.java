package com.ljq.FileTransfer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件备份程序
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
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区

        File[] files = file.listFiles();

        // 处理多个屏蔽的文件夹
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
//                    System.out.println("源文件:" + f.getAbsolutePath());

                    int endindex = tempto.lastIndexOf("\\");

                    String mkdirPath = tempto.substring(0, endindex);

                    File tempFile = new File(mkdirPath);

                    tempFile.mkdirs();

//                    System.out.println("目标文件:" + tempto);

                    System.out.println(format.format(new Date())+" 开始将源文件:[" + f.getAbsolutePath() + "]转移到目录文件[" + tempto + "]");

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
     * 增加或减少天数
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
     * 检查数组是否包含元素(使用List)
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
        System.out.println("########################开始备份########################");
        System.out.println("源文件目录："+ CopyDir.originalPath + "\n" + "目标文件目录：" + CopyDir.newPath + "\n" + "忽略文件目录：" + CopyDir.shieldFile);
        File fromfile = new File(CopyDir.originalPath);
        File tofile = new File(CopyDir.newPath);
        CopyDir copy = new CopyDir();

        CopyDir.dirFrom = fromfile;
        CopyDir.dirTo = tofile;

        copy.listFileInDir(fromfile);

        System.out.println("########################备份结束########################");
    }

    public static void main(String[] args) {
        System.out.println("程序初始化...");
        setParameter();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(runningTime));  // 凌晨1点
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date=calendar.getTime();  // 第一次执行定时任务的时间
        // 如果第一次执行定时任务的时间 小于当前的时间
        // 此时要在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(new Date())) {
            date = CopyDir.addDay(date, 1);
        }
//        Timer timer = new Timer();
//        Task task = new Task();
//        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
//        timer.schedule(task,date,PERIOD_DAY);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                CopyDir.setParameter();
                System.out.println(CopyDir.originalPath + " " + CopyDir.newPath + " " + CopyDir.shieldFile);
                System.out.println("########################开始备份########################");
                System.out.println("源文件目录："+ CopyDir.originalPath + "\n" + "目标文件目录：" + CopyDir.newPath + "\n" + "忽略文件目录：" + CopyDir.shieldFile);
                File fromfile = new File(CopyDir.originalPath);
                File tofile = new File(CopyDir.newPath);
                CopyDir copy = new CopyDir();

                CopyDir.dirFrom = fromfile;
                CopyDir.dirTo = tofile;

                copy.listFileInDir(fromfile);

                System.out.println("########################备份结束########################");
            }
        }
                , date, PERIOD_DAY);

//        CopyDir.setParameter();
//        System.out.println(CopyDir.originalPath + " " + CopyDir.newPath + " " + CopyDir.shieldFile);
//        System.out.println("########################开始备份########################");
//        System.out.println("源文件目录："+CopyDir.originalPath + "\n" + "目标文件目录：" + CopyDir.newPath + "\n" + "忽略文件目录：" + CopyDir.shieldFile);
//        File fromfile = new File(CopyDir.originalPath);
//        File tofile = new File(CopyDir.newPath);
//        CopyDir copy = new CopyDir();
//
//        CopyDir.dirFrom = fromfile;
//        CopyDir.dirTo = tofile;
//
//        copy.listFileInDir(fromfile);
//
//        System.out.println("########################备份结束########################");
    }
}
