package com.onets.test1;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 测试实体类
 */
public class Peer  {
    private static final long serialVersionUID = 1L;
    List<Object> PeerAddress;
    List<Object> PeerIp;
    List<Object> Time;

    public List<Object> getPeerAddress() {
        return PeerAddress;
    }
    public void setPeerAddress(List<Object> PeerAddress) {
        this.PeerAddress = PeerAddress;
    }
    public List<Object> getPeerIp() {
        return PeerIp;
    }
    public void setPeerIp(List<Object> PeerIp) {
        this.PeerIp = PeerIp;
    }
    public List<Object> getTime() {
        return Time;
    }
    public void setTime(List<Object> Time) {
        this.Time = Time;
    }

    /*@Override*/
    public String toString() {
        //注意这里的年龄是不会被序列化的，所以在反序列化的时候是读取不到数据的
        return "用户名:"+PeerAddress+"密 码:"+PeerIp+"年龄:"+Time;
    }
}
