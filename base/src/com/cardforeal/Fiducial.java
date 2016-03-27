package com.cardforeal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.hardware.camera2.*;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import boofcv.abst.fiducial.BaseSquare_FiducialDetector;
import boofcv.abst.fiducial.calib.CalibrationDetectorChessboard;
import boofcv.abst.fiducial.calib.CalibrationDetectorSquareGrid;
import boofcv.abst.fiducial.calib.ConfigChessboard;
import boofcv.abst.fiducial.CalibrationFiducialDetector;
import boofcv.abst.fiducial.FiducialDetector;
import boofcv.abst.geo.calibration.CalibrationDetector;
import boofcv.alg.geo.PerspectiveOps;
import boofcv.android.ConvertBitmap;
import boofcv.android.gui.VideoImageProcessing;
import boofcv.android.VisualizeImageData;
import boofcv.core.image.ConvertImage;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.io.UtilIO;
import boofcv.struct.calib.IntrinsicParameters;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import java.io.*;
import java.nio.*;
import boofcv.android.BoofAndroidFiles;
import android.content.res.Resources;

public class Fiducial{
    IntrinsicParameters params;
    FiducialDetector<ImageFloat32> detector =
            FactoryFiducial.calibChessboard(
                new ConfigChessboard(4, 5, 0.03),
                ImageFloat32.class);

    public Fiducial(int intrinsicFile, Resources source){
        InputStream is = null;
        Reader reader = null;
        try{
            is = source.openRawResource(intrinsicFile);
            reader = new InputStreamReader(is);
            params = BoofAndroidFiles.readIntrinsic(reader);
            detector.setIntrinsic(params);
        } catch (Exception e) {
            System.err.println("Failed to import intrinsic File");
            System.err.println(e);
        } finally {
            try{
                if(is!=null) is.close();
                if(reader!=null) reader.close();
            } catch (Exception e){
                System.err.println(e);
            }
        }
    }

    public Fiducial(){
    }

    public synchronized void process(ImageFloat32 input){
        detector.detect(input);
        int tf = detector.totalFound();
        Se3_F64 sixDof = new Se3_F64();
        //List<Matrix> result = new ArrayList<Matrix>(tf);
        for(int i=0; i<tf; i++){
            detector.getFiducialToCamera(i, sixDof);
            System.out.println("Found "+sixDof);
            //result.add(fromSixDof(sixDof));
        }
    }

/*

            double r = width/2.0;
            Point3D_F64 corners[] = new Point3D_F64[8];
            corners[0] = new Point3D_F64(-r,-r,0);
            corners[1] = new Point3D_F64( r,-r,0);
            corners[2] = new Point3D_F64( r, r,0);
            corners[3] = new Point3D_F64(-r, r,0);
            corners[4] = new Point3D_F64(-r,-r,r);
            corners[5] = new Point3D_F64( r,-r,r);
            corners[6] = new Point3D_F64( r, r,r);
            corners[7] = new Point3D_F64(-r, r,r);

            Point2D_F32 pixel[] = new Point2D_F32[8];
            Point2D_F64 p = new Point2D_F64();
            for (int i = 0; i < 8; i++) {
                Point3D_F64 c = corners[i];
                SePointOps_F64.transform(targetToCamera, c, c);
                PerspectiveOps.convertNormToPixel(intrinsic, c.x / c.z, c.y / c.z, p);
                pixel[i] = new Point2D_F32((float)p.x,(float)p.y);
            }


 */

/*    private Matrix fromSixDof(Se3_F64 transform){
        Matrix result = new Matrix();

        Point3D_F64 topLeft = new Point3D_F64(, , 0);

        RectF start = new RectF(0.0, 1.0, 1.0, 0.0); //left, top, right, bottom

        return result;
    }*/
}
