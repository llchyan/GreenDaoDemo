package com.llchyan.greendaodemo;

import android.app.Application;

import com.llchyan.utils.NoteHelper;

/**
 * Created by LinLin on 2015/12/23.
 */
public class BaseApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        NoteHelper.init(this);
    }
}
