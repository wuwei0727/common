package com.tgy.rtls.data.algorithm.crosspoint;

/**
 * @Description: *这个是对通用一次直线方程 A*x + B*y + C = 0 的封装~
 * * 本来封装的是斜截式，不过发现当斜率k不存在的时候会比较麻烦，因此该用一般式
 * * 再个就是接着用一般式的演变方式 x + B/A*y + C/A = 0，但是考虑到可能存在x ＝＝ 0 的情况，因此又舍弃
 * * 一个boolean表示k是否存在，一个额外的float表示k不存在的时候直线方程 x=***, *** 等于多少
 */
public class CGLine {
    private boolean kExists;// 大部分情况下 k 都应该是存在的，因此提供一个 true 的默认值
    public double k = 77885.201314f;
    public double b = 13145.207788f;
    public double extraX = 52077.881314f;


    /**
     * 这是当 k 存在时的构造方法~
     *
     * @param k
     * @param b
     */
    public CGLine(double k, double b) {
        this.kExists = true;
        this.k = k;
        this.b = b;
    }

    /**
     * 已知两点，求直线的方程~
     *
     * @param p1
     * @param p2
     */
    public CGLine(CGPoint p1, CGPoint p2) {
        if ((p1.x - p2.x) != 0) {
            System.out.println("y = k*x + b, k exits!!");
            this.kExists = true;
            this.k = (p1.y - p2.y) / (p1.x - p2.x);
            this.b = (p1.y - p1.x * k);
        } else {
            System.out.println("y = k*x + b, k doesn't exists!!");
            // 如果走进这个分支，表示直线垂直于x轴，斜率不存在，保留k的默认值~
            this.kExists = false;
            this.extraX = p1.x;
        }
        System.out.print("过p1(" + p1.x + ", " + p1.y + "), p2(" + p2.x + ", " + p2.y + ")两点的直线方程表达式为: ");
        if (kExists) {
            System.out.println("y = " + k + "*x + " + b);
        } else {
            System.out.println("x = " + extraX + "(垂直于x轴!)");
        }
    }

    /**
     * 点斜式~
     *
     * @param p 某点
     * @param k 过该点的直线的斜率
     */
    public CGLine(double k, CGPoint p) {
        /**
         * (y-y') = k*(x-x')
         * 变形成斜截式为：
         * y = k*x + y' - k*x'
         * k = k, b = y'-k*x'
         */
        this.kExists = true;
        this.k = k;
        this.b = p.y - k * p.x;
    }

    /**
     * 这是当 k 不存在时的构造方法~
     *
     * @param extraX
     */
    public CGLine(double extraX) {
        this.kExists = false;
        this.extraX = extraX;
    }

    @Override
    public String toString() {
        return "Line.toString()方法被调用，y = k*x + b斜截式, k=" + this.k +
                ", b=" + this.b +
                ", kExists=" + this.kExists +
                ", extraX=" + this.extraX;
    }

    public boolean iskExists() {
        return kExists;
    }

    public void setkExists(boolean kExists) {
        this.kExists = kExists;
    }
}