package com.cardforeal;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;


public class glView extends GLSurfaceView {
    private final glRender mRenderer;

    public glView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        mRenderer = new glRender();

        setRenderer(mRenderer);
    }
}