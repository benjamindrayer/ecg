package com.example.ecg;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLSurfaceView.Renderer;

public class BenjaminFirstGLActivity extends Activity {
	private camLayer mPreview;
	private GLSurfaceView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	System.out.println("Start ");
        super.onCreate(savedInstanceState);
//        mGLView = new myOpenGLView(this);
        view = new GLSurfaceView(this);
    	System.out.println("GL View ");
        ClearRenderer renderer1=new ClearRenderer(this);
    	System.out.println("Renderer ");
        mPreview = new camLayer(this, renderer1);
    	System.out.println("Cam layer ");
         view.setRenderer(renderer1);
        setContentView(view);
        addContentView(mPreview, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        System.out.println("Activity DEBUG");

    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
        System.out.println("Pause -< Exit");

        System.exit(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
        System.out.println("Resume");
    }

    //private GLSurfaceView mGLView;
   // private myOpenGLView mGLView;
}

@SuppressLint("ParserError")
class ClearRenderer extends GLSurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback, Renderer {
	public ClearRenderer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public Bitmap aImage;

	int nSamples=100;									//number of data samples
	float raw_values[] = new float[nSamples];			//raw values
	float processed_values[] = new float[nSamples];		//processed values
	
	float coordinates[] =new float[nSamples*3];
	float coord_color[]=new float[nSamples*4];
	FloatBuffer coord_color_buffer;

	/*
	 * update the values
	 */
	
	void updateValues(float newValue){
		//shift old values
		float mean=0;
		for(int i=0;i<nSamples-1;++i){
			raw_values[i]=raw_values[i+1];
			processed_values[i]=processed_values[i+1];
			mean+=raw_values[i];
		}
		mean+=newValue;
		mean=(float) mean/nSamples;
		//save new value
		raw_values[nSamples-1]=newValue;
		//compute the variance
		float var=0, diff=0;
		int k_val=50;
		for(int i=nSamples-k_val;i<nSamples;++i){
			diff=raw_values[i]-mean;
			var+=diff*diff;
		}
		var=(float) Math.sqrt((double) var/k_val);
		if(var<=0.00001) var=0.00001f;
		float temp_val=(newValue-mean)/var;
		temp_val+=0.5*processed_values[nSamples-2];
		temp_val+=0.25*processed_values[nSamples-3];
		temp_val+=0.125*processed_values[nSamples-4];
		temp_val+=0.05*processed_values[nSamples-5];
		temp_val/=(1f+0.5f+0.25f+0.125f+0.05f);
		processed_values[nSamples-1]=temp_val;
		//generate coordinates
		for(int i=0;i<nSamples;++i){
    		coordinates[3*i]=(float) i/nSamples-0.5f;
    		coordinates[3*i+1]=processed_values[i]*0.2f;
    		coordinates[3*i+2]=0;
    		//System.out.println("PROCESSEDVAL"+processed_values[i]);
    		coord_color[4*i]=0.0f;
    		coord_color[4*i+1]=0.0f;
    		coord_color[4*i+2]=1.0f;
    		coord_color[4*i+3]=1.0f;
    	}
		
      	ByteBuffer tempColorBuffer2 = ByteBuffer.allocateDirect(coord_color.length * 4);
      	tempColorBuffer2.order(ByteOrder.nativeOrder());  	
      	coord_color_buffer = tempColorBuffer2.asFloatBuffer();
      	coord_color_buffer.put(coord_color);
      	coord_color_buffer.position(0);
	}

	int vsize=100;
	float values[] = new float[vsize*3];
	float true_values[] = new float[vsize];
	float mem_values[] = new float[vsize];

	private FloatBuffer lineVB;
	
	 float n=0;
	 float nv=0;
	 boolean change=false;
	 
     public void moveOn(){ 
    	 n++;
    	// initCurve(n);
     } 
     public void moveOn2(float n){ 
    	 nv=n/100;
    	 change=true;
    	// initCurve(n);
     }

     private void initCurve(float shift){
		 //int vsize=100;
		 float[] values=new float[vsize*3];
		 for(int i=0;i<vsize;i++){
			 values[3*i]=(float) i/75;
		     values[3*i+1]=(float) (Math.sin( shift+(float) i/vsize*30.0)/10);
		     values[3*i+2]=0;
		     
		 }
		 ByteBuffer vbb = ByteBuffer.allocateDirect(
	                // (# of coordinate values * 4 bytes per float)
	                vsize * 3 * 4); 
	        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
	        lineVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
	        lineVB.put(values);    // add the coordinates to the FloatBuffer
	        lineVB.position(0);            // set the buffer to read the first coordinate
		 
	 }
	 
	 private void initPulse(float nv, boolean change){
		 if(change){
			 for(int i=0;i<vsize-1;i++){
				 values[3*i+1]=values[3*(i+1)+1];	
				 //true_values[i]=true_values[i+1];
				 mem_values[i]=mem_values[i+1];
		 	}
	     	mem_values[vsize-1]=nv;
            //true_values[vsize-1]=nv;
		 
		 //demean
		 float m=0;
		 for(int i=0;i<vsize;i++){
			 m+=mem_values[i];	     
	 	}
		 m/=(float) vsize;
		 for(int i=0;i<vsize;i++){
			 true_values[i]=mem_values[i]-m;	     
	 	}
		 //varianz
		 float v=(float) 0.0;
		 float v1=(float) 0.0;
		 for(int i=0;i<vsize;i++){
			 if(Math.abs(true_values[i])>v){
				 v=Math.abs(true_values[i]);
			 }
			 v1+=Math.abs(true_values[i]);
	 	  }
		 v1/=vsize;
		  if(v==0) v=1;
		 // if(v<0.1){
			//    for(int i=0;i<vsize;i++){
				//   true_values[i]/=v*10;
				  // values[3*(i)+1]=true_values[i];
		 	//    }
		 // }
		 
		    for(int i=0;i<vsize;i++){
			   true_values[i]/=v*2;
			   values[3*(i)+1]=true_values[i];
	 	    }
		   values[3*(vsize-1)+1]=true_values[vsize-1];	  
		   System.out.println("Varianz " +v+" value " + true_values[vsize-1]);
		 }
		 
		 
		 ByteBuffer vbb = ByteBuffer.allocateDirect(
	                // (# of coordinate values * 4 bytes per float)
	                vsize * 3 * 4); 
	        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
	        lineVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
	        lineVB.put(values);    // add the coordinates to the FloatBuffer
	        lineVB.position(0);            // set the buffer to read the first coordinate
	        change=false;
		 
	 }
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Do nothing special.
    	System.out.println("on surface created in renderer");
    	 gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
         for(int i=0;i<vsize;i++){
         	values[3*i]=(float) (i/80.0)-(float) (vsize/(2.0*80.0));
         	values[3*i+1]=0;
         	values[3*i+2]=0;   
         	true_values[i]=0;
         	mem_values[i]=0;
         }
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
    }

    float[] points;
    float[] colors;
    ByteBuffer tempColorBuffer;
    ByteBuffer tempByteBuffer;

    /*
     * Place the camera image into the graph
     */
    
    public void placeImage(GL10 gl){
    	//draw the f image
        if(aImage==null)	return;
        int xSize=aImage.getWidth();
        int ySize=aImage.getHeight();
        xSize=20;
        ySize=20;
        int aSize=xSize*ySize;
        if(points==null)points=new float[aSize*3];
        if(colors==null)colors=new float[aSize*4];
     //   if(points.length!=aSize*3)	points=new float[aSize*3];
      //  if(colors.length!=aSize*4)	colors=new float[aSize*4];
        
    	for(int ax=0;ax<xSize;ax++){
	    	for(int ay=0;ay<ySize;ay++){
//        aSize=100;
//	        	for(int ax=0;ax<10;ax++){
//	    	    	for(int ay=0;ay<10;ay++){	    		
	    		int pos=3*(ay*xSize+ax);
	    		points[pos]=(float)ax/((float)xSize*10);
	    		points[pos+1]=(float)ay/((float)ySize*10);
	    		points[pos+2]=0f;
	    		//System.out.println(points[pos]+" "+points[pos+1]+" "+points[pos+2]);
	    		pos=4*(ay*xSize+ax);
	    		int c_val=aImage.getPixel(ax, ay);
	    		colors[pos+0]=(float)(c_val&255*256*256)/((float)255*256*256);
	    		colors[pos+1]=(float)(c_val&255*256)/((float)255*256);
	    		colors[pos+2]=(float)(c_val&255)/255f;
	    		colors[pos+3]=(float)(c_val&255*256*256*256)/((float)255*256*256*256);
	    	}
    	}
    	FloatBuffer colorBuffer;
      	//ByteBuffer tempColorBuffer = ByteBuffer.allocateDirect(colors.length * 4);
      	if(tempColorBuffer==null){ 
      		tempColorBuffer = ByteBuffer.allocateDirect(colors.length * 4);
          	tempColorBuffer.order(ByteOrder.nativeOrder());  	

      	}
      	colorBuffer = tempColorBuffer.asFloatBuffer();
      	colorBuffer.put(colors);
      	colorBuffer.position(0);

      	FloatBuffer tempBuffer;
    	if(tempByteBuffer==null){
        	tempByteBuffer = ByteBuffer.allocateDirect(points.length * 4);
            tempByteBuffer.order(ByteOrder.nativeOrder());    		
    	}
        tempBuffer = tempByteBuffer.asFloatBuffer();
        tempBuffer.put(points);
        tempBuffer.position(0);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);   
        
          gl.glPointSize(2f);
   ///     gl.glLoadIdentity();
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, tempBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
        //gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_POINTS, 0, points.length/3);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    
    
 
    
    public void onDrawFrame(GL10 gl) {
    	gl.glClearColor(coordinates[1]*coordinates[1], 0.0f, 0.0f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        //draw grid

        //draw o
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);   

        /*
         * draw Curve
         */
        gl.glPointSize(2f);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 0.0f);
    	ByteBuffer vbb = ByteBuffer.allocateDirect(nSamples * 3 * 4);   // (# of coordinate values * 4 bytes per float)
        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
        lineVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
        lineVB.put(coordinates);    		// add the coordinates to the FloatBuffer
        lineVB.position(0);            // set the buffer to read the first coordinate
        if(lineVB!=null && coord_color_buffer!=null){
        	//System.out.println(" values in drwaw "+lineVB.get(10));
        	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVB);
       	gl.glColorPointer(4, GL10.GL_FLOAT, 0, coord_color_buffer);
        	gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, nSamples);     
        	
        }
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        /*
         * draw image
         */
        placeImage(gl);
        //Disable the client state before leaving

    }
    
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		System.out.println("Callback onPreviewFrame of renderer");
		System.out.println("FRAME-RATE = "+ camera.getParameters().getPreviewFrameRate()+" fps");
		
		Camera.Parameters p = camera.getParameters();
		int xSize=p.getPreviewSize().width;
		int ySize=p.getPreviewSize().height;
		if(aImage==null){
			//create image
			//aImage=Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888);
		}

		//p.setPreviewFrameRate(10);
		//camera.setParameters(p);
		YuvImage yuvimage = new YuvImage(data,ImageFormat.NV21,xSize,ySize,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, xSize, ySize), 80, baos);
		byte[] jdata = baos.toByteArray();

		// Convert to Bitmap
		aImage = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
		change=true;
        float val_n=0;
        if(aImage!=null)
	        for(int x=0;x<10;++x)
	        	for(int y=0;y<10;++y){
	        		val_n+=aImage.getPixel(x, y);
	        	}
        val_n/=100f;
        updateValues(val_n);
       // initPulse(val_n, change);
       // initPulse(0, true);

        System.out.println(aImage);
	}
    
}