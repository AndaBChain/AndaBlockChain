package org.rockyang.blockchain.nio.Serializable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 测试实体类
 * @author Wang HaiTian
 */
public class Peer  {
    private static final long serialVersionUID = 1L;
    List<Object> PeerAddress;
    List<Object> PeerIp;
    List<Object> Time;



    /*public Peer(String PeerAddress, String PeerIp, String Time) {
        super();
        this.PeerAddress = PeerAddress;
        this.PeerIp = PeerIp;
        this.Time = Time;
    }


    public Peer() {
        super();
    }*/


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

    /**
     * 序列化操作的扩展类
     *//*
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //增加一个新的对象
        Date date=new Date();
        out.writeObject(PeerAddress);
        out.writeObject(PeerIp);
        out.writeObject(Time);
        out.writeObject(date);
    }
*/
    /**
     * 反序列化的扩展类
     *//*
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        //注意这里的接受顺序是有限制的哦，否则的话会出错的
        // 例如上面先write的是A对象的话，那么下面先接受的也一定是A对象...
        PeerAddress=(String) in.readObject();
        PeerIp=(String) in.readObject();
        Time=(String) in.readObject();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date=(Date)in.readObject();
        System.out.println("反序列化后的日期为:"+sdf.format(date));

    }*/
    /*@Override*/
    public String toString() {
        return "钱包地址:"+PeerAddress+"钱包IP:"+PeerIp+"最近使用时间:"+Time;
    }
}
