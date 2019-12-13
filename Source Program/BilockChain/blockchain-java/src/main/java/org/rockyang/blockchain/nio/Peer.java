package org.rockyang.blockchain.nio;
import java.io.Serializable;

public class Peer implements Serializable {
    //private static final long serialVersionUID = 1L;
    private static String AGE = "269";
    private String PeerAddress;
    private String PeerIp;
    private String Time;

    //private String addTip;

    public String getPeerAddress() {
        return PeerAddress;
    }

    public void setPeerAddress(String PeerAddress) {
        this.PeerAddress = PeerAddress;
    }

    public String getPeerIp() {
        return PeerIp;
    }

    public void setPeerIp(String PeerIp) {
        this.PeerIp = PeerIp;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String Time) {
        this.Time = Time;
    }

    //public String getAddTip() {
    //    return addTip;
    //}
    //
    //public void setAddTip(String addTip) {
    //    this.addTip = addTip;
    //}

    @Override
    public String toString() {
        return "FlyPig{" +
                "name='" + PeerAddress + '\'' +
                ", color='" + PeerIp + '\'' +
                ", car='" + Time + '\'' +
                ", AGE='" + AGE + '\'' +
                //", addTip='" + addTip + '\'' +
                '}';
    }
}