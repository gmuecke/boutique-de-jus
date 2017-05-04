package io.bdj.util.signals;

/**
 * Created on 04.05.2017.
 */
public final class Payloads {

    private Payloads(){}
    public static String[] nameValuePair(byte[] payload){
        return new String(payload).split("=|:");
    }

}
