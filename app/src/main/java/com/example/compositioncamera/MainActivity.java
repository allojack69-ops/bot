package com.example.compositioncamera;
import android.Manifest; import android.app.*; import android.widget.FrameLayout; import android.os.*; import android.content.pm.PackageManager; import android.graphics.*; import android.hardware.camera2.*; import android.media.*; import android.view.*; import java.nio.*; import java.util.*;

public class MainActivity extends Activity {
 FrameLayout root; TextureView preview; CompositionOverlayView overlay; CameraDevice camera; CameraCaptureSession session; ImageReader reader;
 int template=1, sensorOrientation=90; float tx=.5f,ty=.5f; int lastClass=-1,stableFrames=0;
 final String[] names={"Золоте січення","Правило третин","Золотий трикутник","Діагоналі","Центральна симетрія"};

 public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.BLACK);
  root=new FrameLayout(this); preview=new TextureView(this); overlay=new CompositionOverlayView(this);
  root.addView(preview); root.addView(overlay); setContentView(root);
  if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.CAMERA},7);
  preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
   public void onSurfaceTextureAvailable(SurfaceTexture s,int w,int h){open();}
   public void onSurfaceTextureSizeChanged(SurfaceTexture s,int w,int h){applyTransform(w,h);}
   public boolean onSurfaceTextureDestroyed(SurfaceTexture s){return true;} public void onSurfaceTextureUpdated(SurfaceTexture s){}
  });
 }
 void open(){try{
  CameraManager cm=(CameraManager)getSystemService(CAMERA_SERVICE); String id=cm.getCameraIdList()[0];
  CameraCharacteristics ch=cm.getCameraCharacteristics(id); Integer so=ch.get(CameraCharacteristics.SENSOR_ORIENTATION); if(so!=null)sensorOrientation=so;
  reader=ImageReader.newInstance(320,240,ImageFormat.YUV_420_888,2);
  reader.setOnImageAvailableListener(r->{Image im=r.acquireLatestImage();if(im!=null){analyze(im);im.close();}},null);
  if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
   cm.openCamera(id,new CameraDevice.StateCallback(){public void onOpened(CameraDevice c){camera=c;start();}public void onDisconnected(CameraDevice c){c.close();}public void onError(CameraDevice c,int e){c.close();}},null);
 }catch(Exception e){}}
 void applyTransform(int vw,int vh){if(preview.getSurfaceTexture()==null)return;
  int rot=getWindowManager().getDefaultDisplay().getRotation(); int degrees=rot==Surface.ROTATION_90?90:rot==Surface.ROTATION_180?180:rot==Surface.ROTATION_270?270:0;
  int relative=(sensorOrientation-degrees+360)%360; Matrix m=new Matrix();
  float sw=320,sh=240; RectF src=new RectF(0,0,sw,sh),dst=new RectF(0,0,vw,vh);
  if(relative==90||relative==270){src=new RectF(0,0,sh,sw);}
  m.setRectToRect(src,dst,Matrix.ScaleToFit.CENTER);
  if(relative==90)m.postRotate(90,vw/2f,vh/2f); else if(relative==270)m.postRotate(-90,vw/2f,vh/2f); else if(relative==180)m.postRotate(180,vw/2f,vh/2f);
  preview.setTransform(m);
 }
 void start(){try{applyTransform(preview.getWidth(),preview.getHeight()); Surface ps=new Surface(preview.getSurfaceTexture());
  camera.createCaptureSession(Arrays.asList(ps,reader.getSurface()),new CameraCaptureSession.StateCallback(){
   public void onConfigured(CameraCaptureSession s){session=s;try{CaptureRequest.Builder b=camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);b.addTarget(ps);b.addTarget(reader.getSurface());b.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);s.setRepeatingRequest(b.build(),null,null);}catch(Exception e){}}
   public void onConfigureFailed(CameraCaptureSession s){}
  },null);}catch(Exception e){}}
 void analyze(Image im){
  ByteBuffer y=im.getPlanes()[0].getBuffer(); byte[] a=new byte[y.remaining()];y.get(a);
  int w=im.getWidth(),h=im.getHeight(),rs=im.getPlanes()[0].getRowStride(); double sx=0,sy=0,sum=0,axis=0,diag=0;
  double[] q4=new double[4];
  for(int yy=5;yy<h-5;yy+=5)for(int xx=5;xx<w-5;xx+=5){int i=yy*rs+xx;if(i+rs*5>=a.length||i+5>=a.length)continue;
   int v=a[i]&255; int gx=Math.abs(v-(a[i+5]&255)); int gy=Math.abs(v-(a[i+rs*5]&255));
   int d1=Math.abs(v-(a[Math.min(a.length-1,i+rs*5+5)]&255)); int d2=Math.abs(v-(a[Math.max(0,i+rs*5-5)]&255));
   double q=gx+gy+2; sx+=q*xx; sy+=q*yy; sum+=q; axis+=gx+gy; diag+=d1+d2;
   int qi=(yy<h/2?0:2)+(xx<w/2?0:1);q4[qi]+=q;
  }
  float rawX=sum>0?(float)(sx/sum/w):.5f, rawY=sum>0?(float)(sy/sum/h):.5f;
  int rel=(sensorOrientation-getWindowManager().getDefaultDisplay().getRotation()*90+360)%360;
  if(rel==90){tx=1-rawY;ty=rawX;} else if(rel==270){tx=rawY;ty=1-rawX;} else if(rel==180){tx=1-rawX;ty=1-rawY;} else {tx=rawX;ty=rawY;}
  tx=Math.max(.10f,Math.min(.90f,tx)); ty=Math.max(.10f,Math.min(.90f,ty));
  double total=q4[0]+q4[1]+q4[2]+q4[3]+1; double sym=Math.abs((q4[0]+q4[2])-(q4[1]+q4[3]))/total;
  int cls; if(sym<.10)cls=4; else if(diag>axis*1.12)cls=3; else if(axis>sum*1.8)cls=1; else cls=0;
  if(cls!=lastClass){stableFrames=0;lastClass=cls;} else stableFrames++;
  if(stableFrames>8)template=cls;
  final int t=template; final float x=tx,yy=ty; runOnUiThread(()->overlay.setData(t,names[t],x,yy));
 }
 @Override protected void onDestroy(){super.onDestroy();try{if(session!=null)session.close();if(camera!=null)camera.close();}catch(Exception e){}}
}