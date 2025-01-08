package com.tgy.rtls.location.Utils;

import io.netty.util.AttributeKey;

public interface ServerConstant {

    public static final AttributeKey<String> NETTY_CHANNEL_DEVID = AttributeKey.valueOf("netty.channel.devId");
}
