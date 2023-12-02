package org.superchat.server.common.utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyUtil {

    public static final AttributeKey<String> TOKEN = AttributeKey.newInstance("token");
    public static final AttributeKey<String> IP = AttributeKey.newInstance("ip");
    public static final AttributeKey<Long> UID = AttributeKey.newInstance("uid");

    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T text) {
        Attribute<T> attribute = channel.attr(attributeKey);
        attribute.set(text);
    }

    public static <T> T getAttr(Channel channel, AttributeKey<T> attributeKey) {
        Attribute<T> attribute = channel.attr(attributeKey);
        return attribute.get();
    }
}
