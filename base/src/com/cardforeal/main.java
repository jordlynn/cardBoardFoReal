package com.cardforeal;

import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;


public class main extends CardboardActivity implements CardboardView.StereoRenderer{
	private static final String TAG = "MAIN";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.main);
	    CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
	    cardboardView.setRestoreGLStateEnabled(false);
	    cardboardView.setRenderer(this);
	    setCardboardView(cardboardView);
    }

    @Override
	public void onRendererShutdown() {
    	Log.i(TAG, "onRendererShutdown");
  	}

  	@Override
  	public void onSurfaceCreated(EGLConfig config) {

  	}

  	@Override
  	public void onSurfaceChanged(int width, int height){

  	}

  	@Override
  	public void onFinishFrame(Viewport viewport){

  	}

  	@Override
  	public void onDrawEye(Eye eye){

  	}

  	@Override
  	public void onNewFrame(HeadTransform headTransform){

  	}

}
