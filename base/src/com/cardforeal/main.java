package com.cardforeal;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;



public class main extends Activity
{

    private GLSurfaceView mGLView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGLView = new glView(this);
        setContentView(mGLView);
    }
}
