package com.tgy.rtls.location.model;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingDeque;

public class Proxy1 implements InvocationHandler, MethodInterceptor {

Object object;
    public void setObject(Object obj){
        this.object=obj;

    }
    //jdk 动态代理，被代理对象必须实现接口
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("代理开始");
        Object result = method.invoke(this.object, args);
        System.out.println("代理结束");
        return result;
    }
   public Object createProxy(){
        return        Proxy.newProxyInstance(this.object.getClass().getClassLoader(),this.object.getClass().getInterfaces(),this);
    }

    // cglib 动态代理，被代理对象不用实现接口
    public Object getProxyInstance(){
        //1.工具类
        Enhancer en = new Enhancer();
        //2.设置父类
        en.setSuperclass(this.object.getClass());
        //3.设置回调函数
        en.setCallback(this);
        //4.创建子类(代理对象)
        return en.create();

    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        //执行目标对象的方法
        System.out.println("cglib代理开始");
        Object result = method.invoke(this.object, objects);
        System.out.println("cglib代理结束");
        return result;
    }

    public static void main(String[] args) {
         LinkedBlockingDeque<LocFiterRes> filterRes=new LinkedBlockingDeque<>();//存储区域判定数据
        filterRes.push(new LocFiterRes("sad",1,1) );
        filterRes.push(new LocFiterRes("s1d",1,1) );
        filterRes.push(new LocFiterRes("s2d",1,1) );
        System.out.println(filterRes.size());
        filterRes.pollLast();
        System.out.println(filterRes.size());
    }
}
