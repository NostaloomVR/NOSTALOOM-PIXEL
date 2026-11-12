package com.nostaloom.pixel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import java.util.Random;

/** Pixel-art fantasy backdrop rendered natively without external images or permissions. */
public final class PixelLandscape extends View {
  private final Paint p=new Paint();
  private final Random rnd=new Random(71);
  public PixelLandscape(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  void rect(Canvas c,float x,float y,float w,float h,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+w,y+h,p);}
  void circle(Canvas c,float x,float y,float r,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
  void polygon(Canvas c,int col,float... xy){p.setColor(col);Path path=new Path();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);path.close();c.drawPath(path,p);}
  void cloud(Canvas c,float x,float y,float s){int light=0xfff6edec,shadow=0xffc2cadd;rect(c,x,y+8*s,69*s,13*s,shadow);rect(c,x+9*s,y,42*s,19*s,light);rect(c,x+22*s,y-10*s,26*s,15*s,light);rect(c,x+38*s,y+3*s,29*s,13*s,light);}
  void star(Canvas c,float x,float y,float size){rect(c,x,y-size,size,size*3,0xfffbe9ae);rect(c,x-size,y,size*3,size,0xfffbe9ae);}
  void tree(Canvas c,float x,float y,float s){int wood=0xff986f86,leaf=0xffd585b7,leaf2=0xffeea5c5;rect(c,x,y,7*s,34*s,wood);
     for(int i=0;i<12;i++){float cx=x+(-30+i%5*15)*s,cy=y+(-19+(i/5)*11)*s;circle(c,cx,cy,18*s,leaf);circle(c,cx-3*s,cy-5*s,12*s,leaf2);}
  }
  void island(Canvas c,float x,float y,float w,float h){
    polygon(c,0xff64516d,x,y+12,x+w,y+12,x+w*.77f,y+h,x+w*.40f,y+h*.88f);
    polygon(c,0xff89758a,x+8,y+12,x+w-9,y+12,x+w*.65f,y+h*.86f,x+w*.42f,y+h*.63f);
    rect(c,x,y,w,17,0xff568a6a);rect(c,x,y-8,w,11,0xffa2b779);
    for(float i=0;i<w;i+=17)rect(c,x+i,y-11,9,5,0xffdce6a2);
  }
  void house(Canvas c,float x,float y,float s){
    rect(c,x,y,42*s,36*s,0xffefc09b);rect(c,x+6*s,y+12*s,11*s,12*s,0xffa0d1d1);
    rect(c,x+27*s,y+14*s,10*s,22*s,0xffaa7d83);
    polygon(c,0xff925f78,x-8*s,y+3*s,x+20*s,y-20*s,x+51*s,y+3*s);
    rect(c,x+19*s,y-12*s,5*s,9*s,0xfff5d4a0);
  }
  @Override protected void onDraw(Canvas actual){super.onDraw(actual);
    float sx=getWidth()/360f,sy=getHeight()/780f;actual.save();actual.scale(sx,sy);
    rect(actual,0,0,360,780,0xff82c3b7);
    for(int i=0;i<28;i++){float x=(i*107+31)%365,y=(i*79+20)%620;star(actual,x,y,(i%3==0)?2:1);}
    cloud(actual,12,28,.58f);cloud(actual,267,36,.65f);cloud(actual,220,240,.85f);
    // layered mountains and distant village
    polygon(actual,0xffa9d4c5,0,485,70,375,133,490);
    polygon(actual,0xffafd7cd,72,480,186,354,282,486);
    polygon(actual,0xffa0c9bc,195,478,310,370,360,470);
    rect(actual,0,620,360,160,0xff94c9bc);
    for(int i=0;i<8;i++){float x=i*49-8;house(actual,x,595+(i%3)*8,.38f);}
    // floating cherry tree island
    island(actual,22,257,223,114);tree(actual,63,181,.81f);house(actual,111,221,.72f);
    // little waterfall
    polygon(actual,0xff98dcdd,143,269,171,269,182,429,164,478,151,429);
    rect(actual,151,282,6,137,0xffe6f7ef);
    // sun and clouds
    circle(actual,277,262,33,0xffecdba4);cloud(actual,238,294,.83f);
    // distant floating castle
    island(actual,289,351,66,43);house(actual,309,322,.57f);
    // windmill far away
    house(actual,263,554,.41f);rect(actual,274,524,4,23,0xff937985);
    polygon(actual,0xffe7d9d5,275,527,254,521,275,531);
    polygon(actual,0xffe7d9d5,276,527,294,510,280,530);
    polygon(actual,0xffe7d9d5,278,530,300,546,275,534);
    // lower bridge and blossoms
    rect(actual,0,684,360,96,0xff6cad9e);
    for(int i=0;i<14;i++){int x=i*27;circle(actual,x,697+(i%3)*7,10,0xff669b80);}
    for(int i=0;i<13;i++){float x=i*32;rect(actual,x,673,25,3,0xffe7dfc7);rect(actual,x+8,676,4,14,0xffe7dfc7);}
    circle(actual,18,748,35,0xffd68cb3);circle(actual,341,747,33,0xffe5a5cb);
    actual.restore();
  }
}
