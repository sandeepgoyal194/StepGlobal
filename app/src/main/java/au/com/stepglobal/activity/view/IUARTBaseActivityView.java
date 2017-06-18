package au.com.stepglobal.activity.view;

import android.content.Context;

/**
 * Created by hiten.bahri on 17/06/2017.
 */

public interface IUARTBaseActivityView {
    Context getApplicationContext();
    void onReceiveMessage(String message);
}
