package us.baocai.baocaishop.net;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 *
 *
 * @TODO Volley管理类
 */
public class VolleyManager {

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static  VolleyManager mVolleyManager;
    private ImageLoader.ImageCache imageCache;

    private VolleyManager(Context context){
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue,imageCache);

    }


    /**
     *
     * @param context
     * @return
     */
    public  static VolleyManager getInstance(Context context){

            if (mVolleyManager==null){ //双重检查
                synchronized (VolleyManager.class){
                    if (mVolleyManager==null){
                        mVolleyManager = new VolleyManager(context);
                    }
                }
            }
        return  mVolleyManager;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
