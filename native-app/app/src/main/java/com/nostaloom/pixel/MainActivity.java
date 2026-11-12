package com.nostaloom.pixel;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Native launcher preview. No sensitive privileges, network access, or hidden monitoring. */
public final class MainActivity extends Activity {
  private final int BG=0xff100b1d, PANEL=0xff241437, PURPLE=0xffba88ff, YELLOW=0xffffd767;
  private final String[] petNames={"MOCHI CAT","PIXEL DOG","BUNNY"};
  private final String[] petFaces={"=^.^=","(•ᴥ•)","(•ㅅ•)"};
  private int petIndex=0, health=90;
  private boolean sound=true;
  private TextView clock, petFace, petHealth, soundButton;
  private final Handler handler=new Handler();
  private GradientDrawable background(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
  private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);v.setGravity(Gravity.CENTER);v.setTextColor(color);return v;}
  private void height(LinearLayout parent,View child,int h){parent.addView(child,new LinearLayout.LayoutParams(-1,h));}
  private TextView button(String s,Runnable run){TextView b=text(s,13,BG);b.setPadding(12,8,12,8);b.setBackground(background(YELLOW,12));b.setOnClickListener(v->run.run());return b;}
  @Override public void onCreate(Bundle state){super.onCreate(state);render();}
  @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
  private void render(){
    ScrollView scrolling=new ScrollView(this);scrolling.setFillViewport(true);scrolling.setBackgroundColor(BG);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,24,18,22);scrolling.addView(root);setContentView(scrolling);
    height(root,text("▣ NOSTALOOM PIXEL ▣",23,YELLOW),56);
    clock=text("",40,PURPLE);height(root,clock,64);updateClock();
    height(root,text("DOT MATRIX  •  RETRO PET OS",11,0xff9678b1),34);
    LinearLayout petPanel=new LinearLayout(this);petPanel.setOrientation(1);petPanel.setPadding(14,10,14,15);petPanel.setBackground(background(PANEL,18));height(root,petPanel,264);
    LinearLayout title=new LinearLayout(this);title.setGravity(Gravity.CENTER_VERTICAL);petPanel.addView(title);
    TextView label=text("♥ PET",18,YELLOW);title.addView(label,new LinearLayout.LayoutParams(0,48,1));
    soundButton=button(sound?"SOUND ON":"SOUND OFF",()->{sound=!sound;updatePet();});title.addView(soundButton);
    petFace=text("",57,PURPLE);height(petPanel,petFace,82);
    petHealth=text("",13,YELLOW);height(petPanel,petHealth,34);
    LinearLayout actions=new LinearLayout(this);petPanel.addView(actions);
    TextView feed=button("+ FEED",()->{health=Math.min(100,health+15);updatePet();});actions.addView(feed,new LinearLayout.LayoutParams(0,49,1));
    TextView select=button("◀ PET ▶",()->{petIndex=(petIndex+1)%petNames.length;updatePet();});LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,49,1);sp.leftMargin=12;actions.addView(select,sp);updatePet();
    height(root,text("▦ INSTALLED APPS",17,YELLOW),55);
    LinearLayout apps=new LinearLayout(this);apps.setOrientation(1);root.addView(apps);
    PackageManager pm=getPackageManager();Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> found=pm.queryIntentActivities(query,0);Collections.sort(found,new ResolveInfo.DisplayNameComparator(pm));
    int count=0;
    for(ResolveInfo ri:found){
      if(ri.activityInfo.packageName.equals(getPackageName()) || count++>=70)continue;
      TextView tile=text("▦  "+ri.loadLabel(pm),13,0xffecd8ff);tile.setGravity(Gravity.CENTER_VERTICAL);tile.setPadding(15,4,10,4);tile.setBackground(background(PANEL,9));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,47);lp.bottomMargin=6;apps.addView(tile,lp);
      tile.setOnClickListener(v->{try{Intent launch=new Intent();launch.setClassName(ri.activityInfo.packageName,ri.activityInfo.name);startActivity(launch);}catch(Exception ex){Toast.makeText(this,"Unable to open app",Toast.LENGTH_SHORT).show();}});
    }
    height(root,text("◀ RETRO YELLOW DOCK • SWIPE ▶",13,PURPLE),46);
    HorizontalScrollView swipe=new HorizontalScrollView(this);swipe.setHorizontalScrollBarEnabled(false);
    LinearLayout dock=new LinearLayout(this);dock.setPadding(8,9,8,9);dock.setBackground(background(YELLOW,16));swipe.addView(dock);height(root,swipe,83);
    String[] names={"PHONE","CAMERA","MESSAGES","BROWSER","SETTINGS","CALC","LOCK","PET"};
    String[] icons={"▣","▤","✉","◉","⚙","▦","▥","♥"};
    for(int i=0;i<names.length;i++){String name=names[i];TextView item=text(icons[i]+"\n"+name,10,BG);item.setPadding(13,2,13,2);dock.addView(item);item.setOnClickListener(v->launch(name));}
    height(root,text("NATIVE PREVIEW • NO SENSITIVE PERMISSIONS",10,0xff9275ac),44);
  }
  private void updatePet(){if(petFace==null)return;petFace.setText(petFaces[petIndex]);petHealth.setText(petNames[petIndex]+"  ♥ "+health+"%");soundButton.setText(sound?"SOUND ON":"SOUND OFF");}
  private void updateClock(){if(clock==null)return;clock.setText(new SimpleDateFormat("hh:mm a",Locale.US).format(new Date()));handler.removeCallbacksAndMessages(null);handler.postDelayed(this::updateClock,30000);}
  private void launch(String item){
    if(item.equals("PET")){petIndex=(petIndex+1)%petNames.length;updatePet();return;}
    if(item.equals("SETTINGS")){startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));return;}
    if(item.equals("LOCK")){Toast.makeText(this,"Secure lock unavailable in preview",Toast.LENGTH_SHORT).show();return;}
    String[] keywords;
    switch(item){case "PHONE":keywords=new String[]{"phone","dialer"};break;case "CAMERA":keywords=new String[]{"camera"};break;case "MESSAGES":keywords=new String[]{"messages","sms"};break;case "BROWSER":keywords=new String[]{"chrome","browser"};break;default:keywords=new String[]{"calculator"};}
    PackageManager pm=getPackageManager();Intent q=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
    for(ResolveInfo ri:pm.queryIntentActivities(q,0)){String label=String.valueOf(ri.loadLabel(pm)).toLowerCase(Locale.ROOT);
      for(String key:keywords)if(label.contains(key)){try{Intent launch=new Intent();launch.setClassName(ri.activityInfo.packageName,ri.activityInfo.name);startActivity(launch);return;}catch(Exception ignored){}}
    }
    Toast.makeText(this,"App not found",Toast.LENGTH_SHORT).show();
  }
}
