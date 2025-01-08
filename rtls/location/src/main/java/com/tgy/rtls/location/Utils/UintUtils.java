package com.tgy.rtls.location.Utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UintUtils {

    /**
     * uint8_t
     * @param s
     * @return
     */
    public static int getUint8(byte s){
        return s & 0x0FF;
    }

    /**
     * uint16_t
     * @param i
     * @return
     */
    public static int getUint16(short i){
        return i & 0x0FFFF;
    }

    /**
     * uint32_t
     * @param l
     * @return
     */
    public static long getUint32(int i){
        return i & 0x0FFFFFFFF;
    }

    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        List<T> result = new ArrayList<T>();
        if(list != null && list.size() > 0){
            int allCount = list.size();
            int pageCount = (allCount + pageSize-1) / pageSize;
            if(pageNo >= pageCount){
                pageNo = pageCount;
            }
            int start = (pageNo-1) * pageSize;
            int end = pageNo * pageSize;
            if(end >= allCount){
                end = allCount;
            }
            for(int i = start; i < end; i ++){
                result.add(list.get(i));
            }
        }
        return (result != null && result.size() > 0) ? result : null;
    }
    //方法:
//datas是查询出来的数据,sort是升还是降排序,order是对某个数据排序
	/*public List<T> builderDatas(List<T> datas,Integer currentPage, Integer pageSize, String sort, String order){
		Stream<T> streDatas = datas.stream();
		Long skipNumber = (currentPage - 1) * pageSize.longValue();
		//利用了Comparator, 因为不知道要排序的数据是什么类型的,所以都弄了,如果还有其他的类型,可以考虑
		//Comparator<Long> longComparator = Comparator.nullsLast(Comparator.naturalOrder());
		//Comparator<Integer> integerComparator = Comparator.nullsLast(Comparator.naturalOrder());
		ProjectUserUseCountVo obj=new ProjectUserUseCountVo();
		//降序判断
		if (StringUtils.isBlank(order) || "desc".equals(order)) {
			longComparator = longComparator.reversed();
			integerComparator = integerComparator.reversed();
		}

		//根据传过来的数据种类,对单一种类进行排序
		switch(order){
			case "Name":
				streDatas = streDatas .sorted(Comparator.comparing(obj.getA(), longComparator));
				break;
			case "desc":
				streDatas = streDatas .sorted(Comparator.comparing(T::getB, integerComparator ));
				break;
			default:
				break;
		}
		//最后返回分页的数据.
		return streDatas.skip(skipNumber).limit(pageSize).collect(Collectors.toList());

	}*/
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
    /**
     * 合并多个list
     * @param lists
     * @param <T>
     * @return
     */
    public static <T> List<T> mergeLists(List<T>... lists) {
        Class clazz = lists[0].getClass();
        List<T> list = null;
        try {
            list = (List<T>) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0, len = lists.length; i < len; i++) {
            list.addAll(lists[i]);
        }
        return list;
    }
    public static long getMessageHash(byte[] data) {
        int[] dataUint8 = new int[data.length];
        for(int i = 0;i < data.length;i++) {
            dataUint8[i] = UintUtils.getUint8(data[i]);
        }

        int seed = 131;
        int hash = 0;
        int i = 0;
        for(i = 0;i < data.length;i++) {
            //hash = (seed * hash) + dataUint8[i];
            hash = hash + dataUint8[i];
        }
        return UintUtils.getUint32(hash);
    }
}
