package us.baocai.baocaishop.util;

import java.util.Random;

/**
 * Created by study on 2015/8/6.
 */
public class RandomUtils {


    public static long nextInt(int start, int end) {
        int space = end -start;
        Random random = new Random();
        return random.nextInt(start+space);
    }
}
