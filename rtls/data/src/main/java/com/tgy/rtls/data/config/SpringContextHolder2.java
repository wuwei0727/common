package com.tgy.rtls.data.config;

import com.tgy.rtls.data.entity.park.ParkingPlace;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 * 
 */
@Component
public class SpringContextHolder2 implements ApplicationContextAware {

	private static ApplicationContext applicationContext = null;
    public static ConcurrentHashMap<Integer,ParkingPlace>  parkingPlaceConcurrentHashMap=new ConcurrentHashMap<>();
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(SpringContextHolder2.applicationContext == null){
			SpringContextHolder2.applicationContext  = applicationContext;
		}
	}


	//获取applicationContext
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	//通过name获取 Bean.
	public static Object getBean(String name){
		return getApplicationContext().getBean(name);
	}

	//通过class获取Bean.
	public static <T> T getBean(Class<T> clazz){
		return getApplicationContext().getBean(clazz);
	}

	//通过name,以及Clazz返回指定的Bean
	public static <T> T getBean(String name,Class<T> clazz){
		return getApplicationContext().getBean(name, clazz);
	}
}
