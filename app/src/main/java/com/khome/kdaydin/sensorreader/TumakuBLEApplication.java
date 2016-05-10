package com.khome.kdaydin.sensorreader;

/**
 * Created by kdaydin on 21.4.2016.
 */
import android.app.Application;
import android.content.Context;

public class TumakuBLEApplication extends Application {

    static TumakuBLE mTumakuBLE;

    public void resetTumakuBLE(){
        TumakuBLE.resetTumakuBLE();
    }

    public TumakuBLE getTumakuBLEInstance(Context context){
        mTumakuBLE=TumakuBLE.getInstance(context);
        return mTumakuBLE;
    }

}
