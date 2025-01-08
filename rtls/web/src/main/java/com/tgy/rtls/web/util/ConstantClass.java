package com.tgy.rtls.web.util;

/**
 * jdk:1.8
 * 通过子类引用父类的静态字段,不会导致子类初始化
 */
class  SuperClass{

    static {
        System.out.println("SuperClass init!");
    }
    public  static  int value=123;

}
class  SubClass extends  SuperClass{

    static {
        System.out.println("SubClass init!");
    }

}

 class Test {
    public static void main(String[] args)throws  Exception{
        System.out.println(new SubClass());
        //输出:
        //SuperClass init!
        //123

    }
}
