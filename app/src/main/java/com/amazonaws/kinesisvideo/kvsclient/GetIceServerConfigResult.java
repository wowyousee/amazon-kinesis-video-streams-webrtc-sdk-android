package com.amazonaws.kinesisvideo.kvsclient;

import com.amazonaws.services.kinesisvideosignaling.model.IceServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetIceServerConfigResult implements Serializable {
    private List<IceServer> iceServerList = new ArrayList<IceServer>();

    /**
     * 获取ICE服务器列表
     */
    public List<IceServer> getIceServerList() {
        return iceServerList;
    }

    /**
     * 设置ICE服务器列表
     */
    public void setIceServerList(Collection<IceServer> iceServerList) {
        if (iceServerList == null) {
            this.iceServerList = null;
            return;
        }
        this.iceServerList = new ArrayList<IceServer>(iceServerList);
    }

    /**
     * 添加ICE服务器(可变参数)
     */
    public GetIceServerConfigResult withIceServerList(IceServer... iceServerList) {
        if (getIceServerList() == null) {
            this.iceServerList = new ArrayList<IceServer>(iceServerList.length);
        }
        for (IceServer value : iceServerList) {
            this.iceServerList.add(value);
        }
        return this;
    }

    /**
     * 添加ICE服务器(集合)
     */
    public GetIceServerConfigResult withIceServerList(Collection<IceServer> iceServerList) {
        setIceServerList(iceServerList);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getIceServerList() != null)
            sb.append("IceServerList: ").append(getIceServerList());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((getIceServerList() == null) ? 0 : getIceServerList().hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GetIceServerConfigResult other = (GetIceServerConfigResult) obj;
        if (other.getIceServerList() == null ^ this.getIceServerList() == null)
            return false;
        return other.getIceServerList() == null || 
               other.getIceServerList().equals(this.getIceServerList());
    }
}