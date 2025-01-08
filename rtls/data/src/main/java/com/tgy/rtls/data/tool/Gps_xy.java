package com.tgy.rtls.data.tool;

public class Gps_xy {

    //经纬度转墨卡托
    public static double[] lonLat2Mercator(double lng, double lat)
    {

        double x = lng * 20037508.34 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;
        double[] xy= {x,y};
        // System.out.println("xy"+xy[0]+"xy"+xy[1]);

        return xy;
    }

    //墨卡托转经纬度
    public static double[] Mercator2lonLat(double X, double Y)
    {

        double x = X / 20037508.34 * 180;
        double y = Y / 20037508.34 * 180;
        y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
        double[] lnglat= {x,y};
        System.out.println("lnglat:"+x+"lnglat:"+y);
        return lnglat;

    }
}
