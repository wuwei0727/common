package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;
@Data
public class TagInitInf {
    public String bootLoaderVersion;
    public String hardWareVersion;
    public String firmWareVersion;
    public long locationInterval ;
    public	short   gain ;
    public  int	moveLevel;
    public	long   hr_on_ms ;
    public	long   hr_off_ms ;
    public  long tagid;
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
