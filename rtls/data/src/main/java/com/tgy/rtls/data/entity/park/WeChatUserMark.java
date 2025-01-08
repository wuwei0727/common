package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WeChatUserMark {
    private Long  id;
    private Integer  userid;
    private Integer map;
    private String  x;
    private String y;
    private Short floor;
    private String fid;
    private String name;
    private Short state;
}
