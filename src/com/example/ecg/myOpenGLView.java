package com.example.ecg;


import java.io.ByteArrayOutputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;


public class myOpenGLView extends GLSurfaceView implements SurfaceHolder.Callback{
    int length=0;
    byte[] data_old;
    public Bitmap aImage;
    
    
	public myOpenGLView(Context context){
		super(context);
		setEGLContextClientVersion(1);	
		 mRenderer = new ClearRenderer(context);
	        setRenderer(mRenderer);

	}
	public myOpenGLView(Context context, AttributeSet attrs){
		super(context, attrs);
		setEGLContextClientVersion(1);	
		 mRenderer = new ClearRenderer(context);
	        setRenderer(mRenderer);
	}
	
	public boolean onTouchEvent(final MotionEvent event) {
		queueEvent(new Runnable(){
            public void run() {
                mRenderer.moveOn();
            }});
            return true;
        }
    ClearRenderer mRenderer;
    
    /*
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		//System.out.println("DEBUG onPreviewFrame" + data[0] + "Tesüt");
		System.out.println("FRAME-RATE = "+ camera.getParameters().getPreviewFrameRate()+" fps");
		
		Camera.Parameters p = camera.getParameters();
		int xSize=p.getPreviewSize().width;
		int ySize=p.getPreviewSize().height;
		if(aImage==null){
			//create image
			//aImage=Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888);
		}

		p.setPreviewFrameRate(100);
		camera.setParameters(p);
		YuvImage yuvimage = new YuvImage(data,ImageFormat.NV21,xSize,ySize,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, xSize, ySize), 80, baos);
		byte[] jdata = baos.toByteArray();

		// Convert to Bitmap
		aImage = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

		// **** begin old code ****
		if(length==0){
			length=data.length;
			data_old=new byte[length];
			for(int i=0;i<data.length;i++){
				data_old[i]=data[i];
			}		
		}
		float val=0;
		for(int i=0;i<data.length;i++){
			val+=(data[i]-data_old[i]);
		}
		val/=data.length;
		mRenderer.moveOn2(val);
		for(int i=0;i<data.length;i++){
			data_old[i]=data[i];
		}		
		// **** end old code ****

		//		System.out.println("DEBUG onPreviewFrame");
	}*/

}