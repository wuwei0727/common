package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;

@Data
public class HaoXiang {
    public String data_type;//event
    public String stream_id;//数据流
    public JSONObject data;//数据流


    @Override
    public String toString() {
        return StringEscapeUtils.unescapeJavaScript(JSONObject.fromObject(this).toString());
    }

}
