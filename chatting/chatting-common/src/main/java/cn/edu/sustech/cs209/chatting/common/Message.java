package cn.edu.sustech.cs209.chatting.common;

import java.awt.*;
import java.util.List;
import java.io.Serializable;

public class Message implements Serializable {

    private Long timestamp;

    private int messageType;

    private String sentBy;

    private String sendTo;

    private String data;

    private List<String> groupList;




    public Message(Long timestamp, String sentBy, String sendTo, String data, int messageType) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.messageType=messageType;
    }

    public List<String> getGroupList(){
        return groupList;
    }
    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public Long getTimestamp() {
        return timestamp;
    }
    public int getMessageType(){return messageType; }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
    public void setSendTo(String sendTo){
        this.sendTo=sendTo;
    }

}
