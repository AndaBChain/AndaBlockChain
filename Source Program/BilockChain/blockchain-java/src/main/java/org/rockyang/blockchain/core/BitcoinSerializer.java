package org.rockyang.blockchain.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bitcoinj.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.bitcoinj.core.Utils.*;

/**
 * <p>Methods to serialize and de-serialize messages to the Bitcoin network format as defined in
 * <a href="https://en.bitcoin.it/wiki/Protocol_specification">the protocol specification</a>.</p>
 *方法将消息序列化或反序列化为定义为的比特币网络格式
 * * < a href = " https://en.bitcoin。它/ wiki / Protocol_specification " >协议规范
 * <p>To be able to serialize and deserialize new Message subclasses the following criteria needs to be met.</p>
 *为了能够序列化和反序列化新的消息子类，需要满足以下条件。</p>
 * <ul>
 * <li>The proper Class instance needs to be mapped to its message name in the names variable below</li>
 * <li>There needs to be a constructor matching: NetworkParameters params, byte[] payload</li>
 * <li>Message.bitcoinSerializeToStream() needs to be properly subclassed</li>
 * 正确的类实例需要映射到名称变量</li> <li>，
 * 需要有一个构造函数匹配:NetworkParameters params, byte[] payload</li>
 * <li> message . bitcoinserializetostream()需要适当的子类化
 * </ul>
 *
 * @author Wang HaiTian
 */
public class BitcoinSerializer  {
    private static final Logger log = LoggerFactory.getLogger(BitcoinSerializer.class);
    private static final int COMMAND_LEN = 12;

    private final NetworkParameters params;
    private final boolean parseRetain;

    private static final Map<Class<? extends Message>, String> names = new HashMap<>();
    /**
     * Constructs a BitcoinSerializer with the given behavior.
     *
     * @param params           networkParams used to create Messages instances and determining packetMagic
     * @param parseRetain      retain the backing byte array of a message for fast reserialization.
     */
    public BitcoinSerializer(NetworkParameters params, boolean parseRetain) {
        this.params = params;
        this.parseRetain = parseRetain;
    }

    /**
     * Writes message to to the output stream.
     * 将消息写入输出流。
     * @return
     */

    public OutputStream serialize(String name, byte[] message, OutputStream out) throws IOException {
        byte[] header = new byte[4 + COMMAND_LEN + 4 + 4 /* checksum */];
        uint32ToByteArrayBE(params.getPacketMagic(), header, 0);

        // The header array is initialized to zero by Java so we don't have to worry about
        // NULL terminating the string here.
        for (int i = 0; i < name.length() && i < COMMAND_LEN; i++) {
            header[4 + i] = (byte) (name.codePointAt(i) & 0xFF);
        }

        Utils.uint32ToByteArrayLE(message.length, header, 4 + COMMAND_LEN);

        byte[] hash = Sha256Hash.hashTwice(message);
        System.arraycopy(hash, 0, header, 4 + COMMAND_LEN + 4, 4);
        out.write(header);
        out.write(message);
        if (log.isDebugEnabled())
            log.debug("Sending {} message: {}", name, HEX.encode(header) + HEX.encode(message));
        return out;
    }
    /**
     * Writes message to to the output stream.
     * 将消息写入输出流。
     */
    public OutputStream serialize(Message message, OutputStream out) throws IOException {
        String name = "tx";
        if (name == null) {
            throw new Error("BitcoinSerializer doesn't currently know how to serialize " + message.getClass());
        }
        return serialize(name, message.bitcoinSerialize(), out);
    }
    /**
     * Get the network parameters for this serializer.
     */
    public NetworkParameters getParameters() {
        return params;
    }


    /**
     * Make an alert message from the payload. Extension point for alternative
     * serialization format support.
     */
    public Message makeAlertMessage(byte[] payloadBytes) throws ProtocolException {
        return new AlertMessage(params, payloadBytes);
    }
    /**
     * Make an filter message from the payload. Extension point for alternative
     * serialization format support.
     */
    public Message makeBloomFilter(byte[] payloadBytes) throws ProtocolException {
        return new BloomFilter(params, payloadBytes);
    }

    /**
     * Make a filtered block from the payload. Extension point for alternative
     * serialization format support.
     */
    public FilteredBlock makeFilteredBlock(byte[] payloadBytes) throws ProtocolException {
        return new FilteredBlock(params, payloadBytes);
    }

    public void seekPastMagicBytes(ByteBuffer in) throws BufferUnderflowException {
        int magicCursor = 3;  // Which byte of the magic we're looking for currently.
        while (true) {
            byte b = in.get();
            // We're looking for a run of bytes that is the same as the packet magic but we want to ignore partial
            // magics that aren't complete. So we keep track of where we're up to with magicCursor.
            byte expectedByte = (byte)(0xFF & params.getPacketMagic() >>> (magicCursor * 8));
            if (b == expectedByte) {
                magicCursor--;
                if (magicCursor < 0) {
                    // We found the magic sequence.
                    return;
                } else {
                    // We still have further to go to find the next message.
                }
            } else {
                magicCursor = 3;
            }
        }
    }

    /**
     * Whether the serializer will produce cached mode Messages
     */
    public boolean isParseRetainMode() {
        return parseRetain;
    }


    public static class BitcoinPacketHeader {
        /** The largest number of bytes that a header can represent */
        public static final int HEADER_LENGTH = COMMAND_LEN + 4 + 4;

        public final byte[] header;
        public final String command;
        public final int size;
        public final byte[] checksum;

        public BitcoinPacketHeader(ByteBuffer in) throws ProtocolException, BufferUnderflowException {
            header = new byte[HEADER_LENGTH];
            in.get(header, 0, header.length);

            int cursor = 0;

            // The command is a NULL terminated string, unless the command fills all twelve bytes
            // in which case the termination is implicit.
            for (; header[cursor] != 0 && cursor < COMMAND_LEN; cursor++) ;
            byte[] commandBytes = new byte[cursor];
            System.arraycopy(header, 0, commandBytes, 0, cursor);
            command = new String(commandBytes, StandardCharsets.US_ASCII);
            cursor = COMMAND_LEN;

            size = (int) readUint32(header, cursor);
            cursor += 4;

            if (size > Message.MAX_SIZE || size < 0)
                throw new ProtocolException("Message size too large: " + size);

            // Old clients don't send the checksum.
            checksum = new byte[4];
            // Note that the size read above includes the checksum bytes.
            System.arraycopy(header, cursor, checksum, 0, 4);
            cursor += 4;
        }
    }
}
