package com.tgy.rtls.data;

import com.tgy.rtls.data.service.common.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DataApplicationTests {
    @Autowired(required = false)
  /*  BserrorcodetestDao bserrorcodetestDao;*/
   /* AutoidDao autoidDao;*/
   /* BsfirmwareDao bsfirmwareDao;
*/
            MailService mailService;
   // TagcheckbsidDao tagcheckbsidDao;

   public   void contextLoads() {
/*        bserrorcodetestDao.getByTagCheckId(142l);
        bserrorcodetestDao.getByTagCheckId(142l);
        bserrorcodetestDao.selectById(111l);
        bserrorcodetestDao.selectById(111l);*/

       /* autoidDao.getIdByRedisKey("redis");
        autoidDao.getIdByRedisKey("redis");
        AutoidEntity autoidEntity=new AutoidEntity();
        autoidEntity.setRedisvalue(12l);

        autoidDao.insert(autoidEntity);*/
     /*   BsfirmwareEntity bsfirmwareEntity=new BsfirmwareEntity();
        bsfirmwareEntity.setBsid(100l);
        bsfirmwareEntity.setCore("core");
        bsfirmwareDao.insert(bsfirmwareEntity);*/
     mailService.sendAttachmentsMail(null,null,null,null,null);
    /*    String start1="2021-03-23 17:46:";
        String start2="2021-03-23 17:47:";
        String start3="2021-03-23 17:48:";
        String start4="2021-03-23 17:49:";
        List<String> sdas=new ArrayList<>();
        sdas.add(start1);
        sdas.add(start2);
        sdas.add(start3);
        sdas.add(start4);
        for(String date:sdas) {
            for (int i = 10; i < 59; ) {
                int k=i+2;
                String start=start1 + i;
                String end=start1 + k;
                List<TagcheckbsidEntity> res = tagcheckbsidDao.getLackTagid(start,end);
                System.out.println(start+"====="+end+":size"+res.size());
                i=i+2;
            }
        }*/

        




    }

 /*   public static void main(String[] args) {
        String startTime="01:04";
        String endTime="07:07";
        startTime=startTime.replace(":","");
        System.out.println(Integer.valueOf(startTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
        Integer currentTime = Integer.valueOf(dateFormat.format(new Date()));
        System.out.println(currentTime);

    }*/

}
