/*
package org.rockyang.blockchain.db.MySql;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.core.BitcoinSerializer;
import org.rockyang.blockchain.testing.FakeTxBuilder;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


public class testmysql {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = UnitTestParams.get();
    public static void main(String[] args) throws Exception{
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
        BitcoinSerializer deserialize = new BitcoinSerializer(params,true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Message message = tx;
        deserialize.serialize(message,out);

        System.out.println(out.toByteArray());
        //MySQL a = new MySQL();
        //Date date = new Date();
        //Timestamp nousedate = new Timestamp(date.getTime());
        //System.out.println(a.examineTime());
        //System.out.println(a.examinePeerAddress());
        //System.out.println(a.examinePeerIp());
        //System.out.println(a.examineNior());
        //List <String> list2 =a.examineNior();
        //String li = String.join("",list2);
        //System.out.println(li);
        //System.out.println(a.examine());
        //a.upnew("3","asfdasd",nousedate);
        //String aa ="          ";
        //System.out.println(aa.length());
        //if (aa.isEmpty()){
        //    System.out.println("yes");
        //}
        //a.upold(aa,nousedate);
    }
}
*/
