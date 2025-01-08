package com.tgy.rtls.data.enums;

/**
 * @author wuwei
 * @date 2024/2/21 - 17:12
 */
public enum IconType {
    SHOP("340834", "店铺", "800009"),

    STAIRS("200005", "步行梯", "170001"),
    ESCALATOR("340818", "手扶电梯", "170003"),
    ELEVATOR_LOBBY("340855", "电梯前室", "170006"),
    ELEVATOR("200004", "直升电梯", ""),

    MAN_LOO("100004", "男洗手间", "100004"),
    GIRL_LOO("100005", "女洗手间", "100005"),

    EMERGENCY_EXIT("200010", "安全出口", "110002"),

    CLASS_I_BUILD("340873", "一类建筑", ""),
    CLASS_II_BUILD("340874", "二类建筑", ""),
    CLASS_III_BUILD("340875", "三类建筑", ""),
    ROAD("200110", "道路", "");

    private String code;
    private String name;
    private String iconType;

    IconType(String code, String name, String iconType) {
        this.code = code;
        this.name = name;
        this.iconType = iconType;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIconType() {
        return iconType;
    }

    public static IconType matchByCode(String code) {
        for (IconType type : IconType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

//    IconType iconType = null;
//            for (IconType enumItem : IconType.values()) {
//        if (enumItem.getCode().equals(targetCode)) {
//            iconType = enumItem;
//            break;
//        }
//    }
//            if (iconType != null) {
//        String matchedIconType = iconType.getIconType();
//        System.out.println(matchedIconType);
//    }
}
