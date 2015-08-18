package us.baocai.baocaishop.util;

import java.math.BigDecimal;

/**
 * 实现double的常用操作
 *
 * Created by young on 15-5-5.
 */
public class DoubleUtil {

    private static final int DEF_DIV_SCALE = 100;

    private DoubleUtil(){}

    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double minus(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double multi(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后100位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1,double v2){
        return div(v1,v2,DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1,double v2,int scale){
        if(scale<0){
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }

        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v,int scale){
        if(scale<0){
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }

        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");

        return b.divide(one,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 将num放大per倍并转换成String类型（只留整数位）
     * @param num 要放大的数
     * @param per 放大倍数
     * @return
     * @throws IllegalArgumentException
     */
    public static String zoomInHundred(String num, int per) throws IllegalArgumentException {
        BigDecimal a = new BigDecimal(num);
        BigDecimal b = new BigDecimal(per);
        BigDecimal mul = new BigDecimal(a.multiply(b).doubleValue());
        String d = mul.toString();
        StringBuffer result = new StringBuffer();

        for(char ch : d.toCharArray()) {
            if('.' == ch)
                break;

            result.append(ch);
        }

        return result.toString();
    }

    /**
     * 将num缩小per倍并转换成String类型，保留scale位小数
     * @param num 原始数
     * @param scale 要保留的小数位数
     * @param per 缩小的倍数
     * @return String
     * @throws IllegalArgumentException
     */
    public static BigDecimal zoomOutWithHundred(String num, int scale, int per) throws IllegalArgumentException {
        if(scale <= 0) {
            throw new IllegalArgumentException("被除数不能小于或等于零！");
        }

        BigDecimal a = new BigDecimal(num);
        BigDecimal b = new BigDecimal(per);
        return a.divide(b, scale, BigDecimal.ROUND_HALF_UP);
    }
}
