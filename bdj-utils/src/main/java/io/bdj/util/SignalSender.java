package io.bdj.util;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SignalSender implements AutoCloseable{

    private static final Logger LOG = getLogger(SignalSender.class.getName());

    private final DatagramChannel channel;

    private SignalSender(DatagramChannel sender){
        this.channel = sender;
    }

    public boolean send(Signal signal, SocketAddress addr)  {

        try {
            channel.send(ByteBuffer.wrap(signal.byteSequence), addr);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not send signal", e);
            return false;
        }
    }

    public static SignalSender newSender(){
        try {
            return new SignalSender(DatagramChannel.open());
        } catch (IOException e) {
            throw new RuntimeException("Could not open datagram channel", e);
        }
    }

    @Override
    public void close() throws Exception {
        if(this.channel != null) {
            this.channel.close();
        }
    }
}
