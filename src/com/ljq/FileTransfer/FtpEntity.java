package com.ljq.FileTransfer;

/**
 * User: Larry Lai
 * Date: 2016-06-20
 * Time: 16:05
 * Version: 1.0
 */

public class FtpEntity {
    private String ipAddr;
    private Integer port;
    private String userName;
    private String pwd;
    private String path;

    public String getIpAddr()
    {
        return this.ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
