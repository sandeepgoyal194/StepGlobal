package au.com.stepglobal.activity.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import au.com.stepglobal.presenter.IUARTBasePresenter;
import au.com.stepglobal.presenter.UARTBasePresenterImpl;

/**
 * Created by hiten.bahri on 17/06/2017.
 */

public abstract class UARTBaseActivityView extends AppCompatActivity implements IUARTBaseActivityView {
    IUARTBasePresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = getPresenter();
        presenter.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    IUARTBasePresenter getPresenter() {
        return new UARTBasePresenterImpl(this);
    }

    public void sendMessage(String message) {
        presenter.sendMessage(message);
    }

    @Override
    abstract public void onReceiveMessage(String message);
}
