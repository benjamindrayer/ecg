package com.example.ecg;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class camLayer extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	 Camera.PreviewCallback callback;
    Camera mCamera;
    boolean isPreviewRunning = false;
	 
	 camLayer(BenjaminFirstGLActivity context, Camera.PreviewCallback callback) {
	        super(context);
	        this.callback=callback;
	        
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        SurfaceHolder mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }
	 
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
	    	if (callback!=null)
	    		callback.onPreviewFrame(data, camera);        	
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("Surface Created - CAM");
		mCamera = Camera.open();
		
    	Camera.Parameters p = mCamera.getParameters();  
    	p.setPreviewSize(240, 160);
//    	p.setPreviewSize(24, 16);
    	
    	mCamera.setParameters(p);
    	
    	try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e("Camera", "mCamera.setPreviewDisplay(holder);");
		}
		
    	mCamera.startPreview();
    	
		mCamera.setPreviewCallback(this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		 // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	synchronized(this) {
	    	try {
		    	if (mCamera!=null) {
		    		mCamera.stopPreview();  
		    		isPreviewRunning=false;
		    		mCamera.release();
		    	}
	    	} catch (Exception e) {
				Log.e("Camera", e.getMessage());
	    	}
    	}
		
	}

}