package com.tgy.rtls.web.controller.test.nacostest;

import com.tgy.rtls.data.entity.equip.InfraredOrigin;
import com.tgy.rtls.data.entity.view.PFindCar;
import com.tgy.rtls.data.mapper.view.FindCarMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.test.nacostest
 * @Author: wuwei
 * @CreateTime: 2023-08-09 18:42
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class test {
    @Autowired
    private FindCarMapper findcarMapper;


    @RequestMapping("/test1")
    public void test1(){
        PFindCar findcar = new PFindCar();
        findcar.setMap(75L);
        findcar.setPlace(1111L);
        findcar.setPlaceName("E11920");
        findcar.setTimestamp(LocalDateTime.parse("2023-07-16T17:28:57.185"));
        int i = LocalDateTime.now().hashCode()%5;
        log.error("-----"+i);
        findcarMapper.insertFindCar(findcar);
    }


    @RequestMapping("/test2")
    public void test2(){
        for (long i = 1; i < 5; i++){
            PFindCar findcar = new PFindCar();
            findcar.setMap(74L);
            findcar.setTimestamp(LocalDateTime.parse("2023-08-16T17:28:57.185"));
            log.error("TimestampTimestamp="+findcar.getTimestamp());
            findcarMapper.insert(findcar);
        }

        for (long i = 5; i < 9; i++){
            PFindCar findcar = new PFindCar();
            findcar.setMap(75L);
            findcar.setTimestamp(LocalDateTime.parse("2023-09-16T17:28:57.185"));
            log.error("TimestampTimestamp="+findcar.getTimestamp());
            findcarMapper.insertFindCar(findcar);
        }

    }

    @RequestMapping("/test3")
    public String test3() throws UnknownHostException {
        // List<PFindCar> findcars = findcarMapper.selectList((PFindCar) null);
        // findcars.forEach(System.out::println);
        InetAddress addr = InetAddress.getLocalHost();
    return addr+"HostAddress:"+Inet4Address.getLocalHost().getHostAddress()+"----"+"HostName"+Inet4Address.getLocalHost().getHostName();
    }
    @RequestMapping("/test4")
    public void test4(){
        PFindCar pFindCar = new PFindCar();
        pFindCar.setMap(75L);
        List<PFindCar> findcars = findcarMapper.selectList(pFindCar);
        findcars.forEach(System.out::println);
    }


    @RequestMapping("/test5")
    public void test5(){
        InfraredOrigin origin = new InfraredOrigin();
        origin.setGatewaynum(101);
        origin.setTimestamp(LocalDateTime.parse("2023-08-16T17:28:57.185"));
        origin.setInfrarednum(1);
        int i = origin.getGatewaynum()%2;
        log.error("-----"+i);
        findcarMapper.insertInfraredOrigin(origin);
    }

    @RequestMapping("/test6")
    public void test6() {
        for (long i = 1; i < 5; i++) {
            // 创建InfraredOrigin对象
            InfraredOrigin origin = new InfraredOrigin();
            // 设置Gatewaynum
            origin.setGatewaynum(Math.toIntExact(101 + i));
            // 设置时间戳
            origin.setTimestamp(LocalDateTime.parse("2023-09-16T17:28:57.185"));
            // 设置Infrarednum
            origin.setInfrarednum(1);
            // 获取Gatewaynum的奇偶性
            int i1 = origin.getGatewaynum()%2;
            log.error("-----"+i1);
            // 插入InfraredOrigin
            findcarMapper.insertInfraredOrigin(origin);
        }

        for (long i = 5; i < 9; i++) {
            // 创建InfraredOrigin对象
            InfraredOrigin origin = new InfraredOrigin();
            // 设置Gatewaynum
            origin.setGatewaynum(Math.toIntExact(102 + i));
            // 设置时间戳
            origin.setTimestamp(LocalDateTime.parse("2023-08-16T17:28:57.185"));
            // 设置Infrarednum
            origin.setInfrarednum(1);
            // 获取Gatewaynum的奇偶性
            int i1 = origin.getGatewaynum()%2;
            log.error("-----"+i1);
            // 插入InfraredOrigin
            findcarMapper.insertInfraredOrigin(origin);
        }
    }
    private static final DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    //这是一个main方法，程序的入口
    //注释
    public static void main(String[] args){
        //循环输出1到5的整数
        for (long i = 1; i < 5; i++) {
            //创建InfraredOrigin对象
            InfraredOrigin origin = new InfraredOrigin();
            //设置网关号
            origin.setGatewaynum(Math.toIntExact(101 + i));
            //输出网关号
            System.out.println("origin = " + origin.getGatewaynum());
        }

    }
}
