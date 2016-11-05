package com.haier.uhome.usend.events;

/**
 * @Author: majunling
 * @Data: 2016/10/29
 * @Description:
 */
public class EventSendStatus {
    public enum SendStatus {
        UNSEND,
        SEND_START,
        SEND_PROGRESS,
        SEND_DONE,
        SEND_CANCLE
    }

    public class SendCount{
        //成功条数
        int sucessCount;
        //失败条数
        int failCount;

        public int getSucessCount() {
            return sucessCount;
        }

        public int getFailCount() {
            return failCount;
        }
    }

    private SendStatus sendStatus = SendStatus.UNSEND;

    private SendCount sendCount;

    public EventSendStatus(SendStatus sendStatus, int sucessCount, int failCount){
        this.sendStatus = sendStatus;
        sendCount = new SendCount();
        sendCount.sucessCount = sucessCount;
        sendCount.failCount = failCount;
    }

    public SendStatus getSendStatus() {
        return sendStatus;
    }


    public SendCount getSendCount() {
        return sendCount;
    }
}
