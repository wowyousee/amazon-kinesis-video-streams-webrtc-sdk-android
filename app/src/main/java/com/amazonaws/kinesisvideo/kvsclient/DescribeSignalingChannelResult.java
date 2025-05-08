package com.amazonaws.kinesisvideo.kvsclient;

import com.amazonaws.services.kinesisvideo.model.ChannelInfo;

public class DescribeSignalingChannelResult {
    private ChannelInfo channelInfo;

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public DescribeSignalingChannelResult withChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        return this;
    }
}