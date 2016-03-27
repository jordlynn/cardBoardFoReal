package com.cardforeal;

import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.*;

import com.google.vrtoolkit.cardboard.Viewport;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.hardware.Camera;
import android.opengl.GLES20;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import boofcv.io.UtilIO;
import boofcv.android.gui.VideoImageProcessing;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class main extends CardboardActivity implements CardboardView.StereoRenderer, OnFrameAvailableListener{
	private static final String TAG = "====================== I got stuffs =============================>";
	private static String cameraID = new String();
	private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	private Camera camera;

	private final String vertexShaderCode =
        "attribute vec4 position;" +
                "attribute vec2 inputTextureCoordinate;" +
                "varying vec2 textureCoordinate;" +
                "void main()" +
                "{" +
                "gl_Position = position;" +
                "textureCoordinate = inputTextureCoordinate;" +
                "}";

    private final String fragmentShaderCode =
        "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 textureCoordinate;                            \n" +
                "uniform samplerExternalOES s_texture;               \n" +
                "void main(void) {" +
                "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                //"  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
                "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer, vertexBuffer2;
	private ShortBuffer drawListBuffer, buf2;
	private int mProgram;
	private int mPositionHandle, mPositionHandle2;
	private int mColorHandle;
	private int mTextureCoordHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;
	static float squareVertices[] = { // in counterclockwise order:
        -1.0f, -1.0f,   // 0.left - mid
        1.0f, -1.0f,   // 1. right - mid
        -1.0f, 1.0f,   // 2. left - top
        1.0f, 1.0f,   // 3. right - top
    };

    private short drawOrder[] = {0, 2, 1, 1, 2, 3}; // order to draw vertices
	private short drawOrder2[] = {2, 0, 3, 3, 0, 1}; // order to draw vertices

	static float textureVertices[] = {
        0.0f, 1.0f,  // A. left-bottom
        1.0f, 1.0f,  // B. right-bottom
        0.0f, 0.0f,  // C. left-top
        1.0f, 0.0f   // D. right-top
	};

	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

	private ByteBuffer indexBuffer;    // Buffer for index-array

	private int texture;
	private CardboardOverlayView mOverlayView;
	private CardboardView cardboardView;
	private SurfaceTexture surface;
	private float[] mView;
	private float[] mCamera;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.main);
	    CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
	    cardboardView.setRestoreGLStateEnabled(false);
	    cardboardView.setRenderer(this);
	    setCardboardView(cardboardView);

	    // Start Camera stuff, sorry not sorry Brett.
	    mCamera = new float[16];
	    mView = new float[16];
	    mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
	    //mOverlayView.show3DToast("Pull the magnet when you find an object.");
    }

    public void startCamera(int texture) {
    surface = new SurfaceTexture(texture);
    surface.setOnFrameAvailableListener(this);

    camera = Camera.open();
    Camera.Parameters params = camera.getParameters();
    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

    camera.setParameters(params);
    try {
        camera.setPreviewTexture(surface);
        camera.startPreview();
    } catch (IOException ioe)
    {
        Log.d(TAG, "CAM LAUNCH FAILED");
    }
	}

	static private int createTexture() {
    int[] texture = new int[1];

    GLES20.glGenTextures(1, texture, 0);
    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
    GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
    GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

    return texture[0];
	}

	private int loadGLShader(int type, String code) {
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, code);
    GLES20.glCompileShader(shader);

    // Get the compilation status.
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
        Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        shader = 0;
    }

    if (shader == 0) {
        throw new RuntimeException("Error creating shader.");
    }

    return shader;
	}

    @Override
	public void onRendererShutdown() {
    	Log.i(TAG, "onRendererShutdown");
  	}
  	@Override
	public void onFrameAvailable(SurfaceTexture arg0) {
	    //this.cardboardView.requestRender();
	}

  	@Override
  	public void onSurfaceChanged(int width, int height){

  	}

  	/***
	 *                                                                                   
	 *                                                         ,ad8888ba,   88           
	 *                                                        d8"'    `"8b  88           
	 *                                                       d8'            88           
	 *     ,adPPYba,   8b,dPPYba,    ,adPPYba,  8b,dPPYba,   88             88           
	 *    a8"     "8a  88P'    "8a  a8P_____88  88P'   `"8a  88      88888  88           
	 *    8b       d8  88       d8  8PP"""""""  88       88  Y8,        88  88           
	 *    "8a,   ,a8"  88b,   ,a8"  "8b,   ,aa  88       88   Y8a.    .a88  88           
	 *     `"YbbdP"'   88`YbbdP"'    `"Ybbd8"'  88       88    `"Y88888P"   88888888888  
	 *                 88                                                                
	 *                 88                                                                
	 */

  	@Override
	public void onSurfaceCreated(EGLConfig config) {
	    Log.i(TAG, "onSurfaceCreated");
	    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well

	    ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
	    bb.order(ByteOrder.nativeOrder());
	    vertexBuffer = bb.asFloatBuffer();
	    vertexBuffer.put(squareVertices);
	    vertexBuffer.position(0);

	    ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
	    dlb.order(ByteOrder.nativeOrder());
	    drawListBuffer = dlb.asShortBuffer();
	    drawListBuffer.put(drawOrder);
	    drawListBuffer.position(0);

	    ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
	    bb2.order(ByteOrder.nativeOrder());
	    textureVerticesBuffer = bb2.asFloatBuffer();
	    textureVerticesBuffer.put(textureVertices);
	    textureVerticesBuffer.position(0);

	    int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

	    mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
	    GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
	    GLES20.glLinkProgram(mProgram);

	    texture = createTexture();
	    startCamera(texture);
	}

	/**
	 * Prepares OpenGL ES before we draw a frame.
	 *
	 * @param headTransform The head transformation in the new frame.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {
	    float[] mtx = new float[16];
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	    surface.updateTexImage();
	    surface.getTransformMatrix(mtx);
	}


  	/**
	 * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
	 *
	 * @param func
	 */
	private static void checkGLError(String func) {
	    int error;
	    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	        Log.e(TAG, func + ": glError " + error);
	        throw new RuntimeException(func + ": glError " + error);
	    }
	}

	/**
	 * Draws a frame for an eye. The transformation for that eye (from the camera) is passed in as
	 * a parameter.
	 *
	 * @param transform The transformations to apply to render this eye.
	 */
	@Override
	public void onDrawEye(Eye transform) {
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

	    GLES20.glUseProgram(mProgram);

	    GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
	    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture);


	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
	            false, vertexStride, vertexBuffer);


	    mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
	    GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
	    GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
	            false, vertexStride, textureVerticesBuffer);

	    mColorHandle = GLES20.glGetAttribLocation(mProgram, "s_texture");


	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
	            GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	    GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

	    Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);
	}

		@Override
	public void onFinishFrame(Viewport viewport) {
	}

	@Override
	public void onCardboardTrigger() {

	}

}
