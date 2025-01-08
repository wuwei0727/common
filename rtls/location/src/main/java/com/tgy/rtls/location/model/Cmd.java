package com.tgy.rtls.location.model;

import static com.tgy.rtls.location.Utils.ByteUtils.bytesReverseOrder;
import static com.tgy.rtls.location.Utils.ByteUtils.printHexString;

public class Cmd {
    String cmd;
    public  Cmd(byte[] value) {
       byte[] reverseValue= bytesReverseOrder(value);
        this.cmd =printHexString(reverseValue).toUpperCase();
    }

    public String getCmd() {
        return cmd;
    }
}
