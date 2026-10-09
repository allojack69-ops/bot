package com.example.compositioncamera;
import android.view.*;import android.graphics.*;import android.content.*;
public class CompositionOverlayView extends View { Paint p=new Paint(3); int type;String name="Золоте січення";float tx=.5f,ty=.5f;
 public CompositionOverlayView(Context c){super(c);p.setTypeface(Typeface.create("sans",Typeface.BOLD));setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 public void setData(int t,String n,float x,float y){type=t;name=n;tx=x;ty=y;invalidate();}
 protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(Color.argb(220,255,215,80));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);
  if(type==0){RectF r=new RectF(w*.08f,h*.08f,w*.92f,h*.92f);for(int i=0;i<5;i++){c.drawArc(r,0,90,true,p);r.inset(w*.09f,h*.09f);}}
  else if(type==1){c.drawLine(w/3,0,w/3,h,p);c.drawLine(w*2/3,0,w*2/3,h,p);c.drawLine(0,h/3,w,h/3,p);c.drawLine(0,h*2/3,w,h*2/3,p);}
  else if(type==2){Path q=new Path();q.moveTo(w*.08f,h*.9f);q.lineTo(w*.92f,h*.1f);q.moveTo(w*.08f,h*.9f);q.lineTo(w*.92f,h*.9f);q.moveTo(w*.92f,h*.1f);q.lineTo(w*.92f,h*.9f);c.drawPath(q,p);}
  else if(type==3){c.drawLine(0,0,w,h,p);c.drawLine(w,0,0,h,p);}
  else{c.drawLine(w/2,0,w/2,h,p);c.drawLine(0,h/2,w,h/2,p);c.drawCircle(w/2,h/2,Math.min(w,h)*.34f,p);}
  p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(220,0,0,0));c.drawRoundRect(24,28,250,78,25,25,p);p.setColor(Color.WHITE);p.setTextSize(18);c.drawText(name,40,60,p);
  float ax=tx*w,ay=ty*h;p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawCircle(ax,ay,11,p);c.drawLine(w/2,h/2,ax,ay,p);
  String msg=(Math.abs(tx-.5f)<.06&&Math.abs(ty-.5f)<.06)?"✓ КОМПОЗИЦІЯ ГОТОВА":"ПІДВЕДІТЬ КАМЕРУ ДО МІТКИ";p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(225,0,0,0));c.drawRoundRect(w/2-145,h-115,w/2+145,h-65,25,25,p);p.setColor(Color.WHITE);p.setTextSize(16);c.drawText(msg,w/2-p.measureText(msg)/2,h-83,p);
  if(!msg.startsWith("✓")){String a=Math.abs(tx-.5f)>.06?(tx<.5f?"←":"→"):(ty<.5f?"↑":"↓");p.setTextSize(28);c.drawText(a,w/2-p.measureText(a)/2,h-30,p);}
 }
}