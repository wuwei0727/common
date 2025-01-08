package com.tgy.rtls.location.netty;

import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.tgy.rtls.location.Utils.Constant.CMD_CAT1_CMD;
import static com.tgy.rtls.location.netty.MessageDecoder.*;

@Component
public class SendData {
    @Autowired
    private MapContainer mapContainer;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    int reserved=16;
    public boolean sendDate(Long bsid,byte[] CMD,byte[] data){
        Header head=new Header(PACKAGE_TAG, ByteUtils.shortToByte((short)(data.length+reserved)),RESERVED,HEADER,SRC,ByteUtils.intToBytes(bsid),CMD);
        Message msg=new Message(head,data);
       String data_string= ByteUtils.printHexString(data);
       if(CMD[0]==0x35)
       {
           System.out.println(msg);
       }
       return sendMessageToDevice(msg,bsid+"");
    }

    boolean sendMessageToDevice(Message msg, String bsid){

        Channel channel = mapContainer.all_channel.get(bsid);
        if(channel!=null&&channel.isOpen()){
            try {
                channel.write(msg);
                logger.info("write xxxx");
                channel.flush();
                return true;
            }catch (Exception e){
                logger.info("write fail"+e);
                return false;
            }
        }else{
            logger.info("write fail");
            return false;
        }

    }

    public boolean sendDataToLora(Long bsid,byte[] CMD,byte[] msgid,byte[] data){
        Header head=new Header(PACKAGE_TAG, ByteUtils.shortToByte((short)(data.length+reserved)),RESERVED,HEADER,ByteUtils.intToBytes(bsid),msgid,CMD);
        Message msg=new Message(head,data);
        String data_string= ByteUtils.printHexString(data);
        return sendMessageToDevice(msg,bsid+"");
    }

    public boolean sendDataToCAT1(Long bsid,byte[] CMD,byte[] msgid,byte[] data){
        Header head=new Header(PACKAGE_TAG, ByteUtils.shortToByte((short)(data.length+reserved)),RESERVED,HEADER,ByteUtils.intToBytes(bsid),msgid,CMD);
        Message msg=new Message(head,data);
        String data_string= ByteUtils.printHexString(data);
        return sendMessageToDevice(msg,"CAT1_"+bsid);
    }

    public boolean sendDataToScreen(Long bsid,byte[] data){
         final byte[] HEAD = { (byte) 0xC9,(byte) 0xC9};
        Header head=new Header(HEAD,null,null,null,null,null,null);
        Message msg=new Message(head,data);
        String data_string= ByteUtils.printHexString(data);
        logger.info("bsid:"+bsid);
        logger.info("data:"+data_string);
        return sendMessageToDevice(msg,"4G_"+bsid);
    }

    public Boolean  sendCat1CmdToNed(Long bsid,int  cmd,int data){
        ByteBuffer buffer= ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)cmd);
        buffer.putInt(bsid.intValue());
        buffer.putInt(data);
        byte[] msgid={0x00,0x00,0x00,0x00};
        sendDataToCAT1(bsid,CMD_CAT1_CMD,msgid,buffer.array());
        return true;
    }

 /*   public boolean sendCommandToNed(Long bsid,byte[] data){
        final byte[] HEAD = { (byte) 0x7e,(byte) 0xaa};
        Header head=new Header(HEAD,null,null,null,null,null,null);
        Message msg=new Message(head,data);
        String data_string= ByteUtils.printHexString(data);
        logger.info("bsid:"+bsid);
        logger.info("data:"+data_string);
        return sendMessageToDevice(msg,""+bsid);
    }*/


}
