package com.starsriver.ftx;

import android.content.Context;

import com.tencent.vasdolly.helper.ChannelReaderUtil;

public class ChannelUtil {
    public static String channel(Context context) {
        String channel = ChannelReaderUtil.getChannel(context);
        return channel == null ? "default" : channel;
    }
}
