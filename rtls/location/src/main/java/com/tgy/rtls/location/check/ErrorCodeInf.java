package com.tgy.rtls.location.check;

import java.util.concurrent.ConcurrentHashMap;

public class ErrorCodeInf /*implements AutoService*/ {
   public volatile boolean stopFlag=false;
   public volatile  int count;
   public Long bsid;
   public Thread th;
    public int messageid;
    public int interval;
    public ConcurrentHashMap<Long ,Boolean>  info_state=new ConcurrentHashMap<>();
   public ErrorCodeInf(){

    }

    public int add(int a,int b){
       return a+b;
    }

    public static void main(String[] args) {
/*        ErrorCodeInf errorCodeInf=new ErrorCodeInf();
        Proxy1 p=new Proxy1();
        p.setObject(errorCodeInf);
       *//* Object pxoy_error = p.createProxy();
        AutoService errorCodeInf1=(AutoService) pxoy_error;*//*
        ErrorCodeInf errorCodeInf2=(ErrorCodeInf) p.getProxyInstance();
        errorCodeInf2.add(1,2);*/
      /*  AutoidEntity autoidEntity=new AutoidEntity();
        autoidEntity.setId(100l);
      // errorCodeInf1.updateById(autoidEntity);
       autoidEntity.setId(1001l);
        errorCodeInf2.updateById(autoidEntity);*/
      int res= (int) Math.ceil(1.0);
        System.out.println(res);


    }

/*    @Override
    public AutoidEntity getAutoId(String key) {
        return new AutoidEntity();
    }

    @Override
    public void updateById(AutoidEntity autoid) {
        System.out.println(autoid.getId());
    }*/
}
