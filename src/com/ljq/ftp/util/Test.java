package com.ljq.ftp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Larry Lai
 * Date: 2016-11-02
 * Time: 10:37
 * Version: 1.0
 */

public class Test {
    public static void main(String[] args) throws ParseException {
        Date startDate = new SimpleDateFormat("yyyyMMdd").parse("20160801");  // 定义起始日期
        Date endDate = new SimpleDateFormat("yyyyMMdd").parse("20161102");  // 定义结束日期

        Calendar calendar = Calendar.getInstance();  // 定义日期实例
        calendar.setTime(startDate);  // 设置日期起始时间
        while (calendar.getTime().before(endDate)) {  // 判断是否到结束日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String date = sdf.format(calendar.getTime());
            System.out.println(date);  // 输出日期结果
            calendar.add(Calendar.DAY_OF_YEAR, 1);  // 进行当前日期加1
        }
    }
}
