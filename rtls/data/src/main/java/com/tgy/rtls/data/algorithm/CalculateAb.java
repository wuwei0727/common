package com.tgy.rtls.data.algorithm;

public class CalculateAb {


    public static void main(String[] args) {

        double[] x={157.4,
                163.46,
                171.4,
                185.57

        };
        double[] y={4,10,17,33};

        getAb(x,y);
    }

    /**
     *
     * @param x  原始距离
     * @param y  真实距离
     * @return  返回double 数组  a=res[0]  b=res[1]  R2=res[2]
     */
  public static   double[] getAb(double[] x,double[] y){

        try {
            int length = x.length;
            double[][] x_matrix = new double[length][2];
            double[][] y_matrix = new double[length][1];
            for (int i = 0; i < length; i++) {
                x_matrix[i][0] = x[i];
                x_matrix[i][1] = 1;
                y_matrix[i][0] = y[i];
            }
            //AX=B  A=((XT*X)逆矩阵)*XT*Y
            double[][] x_t = Hilen.transpose(x_matrix);
            double[][] x_tx = Hilen.matrix(x_t, x_matrix);
            Hilen.mrinv(x_tx, 2);
            double[][] res = Hilen.matrix(Hilen.matrix(x_tx, x_t), y_matrix);


            double sum = 0;
            for (int i = 0; i < length; i++) {
                sum = sum + y_matrix[i][0];
            }

            double y_mean = sum / length;
            double sum_tot = 0;
            double sum_reg = 0;

            for (int i = 0; i < length; i++) {
                sum_tot = (y_mean - y_matrix[i][0]) * (y_mean - y_matrix[i][0]) + sum_tot;
                sum_reg = sum_reg + (y_mean - (res[0][0] * x_matrix[i][0] + res[1][0])) * (y_mean - (res[0][0] * x_matrix[i][0] + res[1][0]));
            }

            double r = sum_reg / sum_tot;
            double[] finaleres = {res[0][0], res[1][0], r};
            return finaleres;
        }catch (Exception e){
            return null;
        }

    }
}
