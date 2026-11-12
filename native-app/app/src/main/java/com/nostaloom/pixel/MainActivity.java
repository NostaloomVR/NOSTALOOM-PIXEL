package com.nostaloom.pixel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** NOSTALOOM Pixel Pet OS native UI restoration preview.
 *  No notification listener, accessibility, device admin or surveillance privileges.
 */
public final class MainActivity extends Activity {
    private static final int BG=0xff120d25, PANEL=0xff22163c, PURPLE=0xffc18cff,
            LIGHT=0xfff8e8ff, MUTED=0xffae91c7, YELLOW=0xffffd65e, RED=0xfff16b89;
    private final Handler timer = new Handler();
    private final String[] names={"MOCHI CAT","PIXEL PUP","BUNNY POP"};
    private int pet=0, health=87, hunger=58, energy=80, theme=0, motionFrame=0, motionTicks=0;
    private String petAction="IDLE";
    private PetArt islandCat;
    private TextView petStats;
    private boolean sound=true, blink=false;
    private TextView healthText, soundText, petName, dotClock;
    private ProgressBar healthBar;
    private PetArt petArt;
    private ToneGenerator tone;
    private Dialog appDialog;

    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    GradientDrawable shape(int fill,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));return d;
    }
    GradientDrawable outlined(int fill,int radius,int stroke){
        GradientDrawable d=shape(fill,radius);d.setStroke(dp(2),stroke);return d;
    }
    TextView label(String text,int size,int color) {
        TextView t=new TextView(this);t.setText(text);t.setTextColor(color);
        t.setTextSize(size);t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create("monospace",Typeface.BOLD));t.setIncludeFontPadding(false);return t;
    }
    LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
    LinearLayout horizontal(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    LinearLayout.LayoutParams item(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    void add(LinearLayout parent,View v,int h){parent.addView(v,item(-1,h));}
    TextView action(String title,int color,Runnable run){
        TextView t=label(title,12,BG);t.setBackground(shape(color,9));t.setOnClickListener(v->run.run());return t;
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);
        getWindow().setStatusBarColor(0xff100a1c);getWindow().setNavigationBarColor(0xff090613);
        android.content.SharedPreferences prefs=getSharedPreferences("nostaloom_pet",MODE_PRIVATE);
        pet=prefs.getInt("pet",0)%names.length;
        health=prefs.getInt("happy",87);hunger=prefs.getInt("hunger",58);energy=prefs.getInt("energy",80);
        sound=prefs.getBoolean("sound",true);
        render();syncPet();timer.postDelayed(tick,650);
    }
    @Override protected void onDestroy(){timer.removeCallbacksAndMessages(null);if(tone!=null)tone.release();super.onDestroy();}
    private final Runnable tick=new Runnable(){public void run(){
        blink=!blink;motionFrame++;
        if(motionTicks>0){motionTicks--;if(motionTicks==0)petAction="IDLE";}
        else if(motionFrame%14==0){String[] acts={"IDLE","WALK","RUN","PLAY","REST","SLEEP"};
            petAction=acts[(motionFrame/14)%acts.length];}
        if(motionFrame%40==0){hunger=Math.max(0,hunger-1);energy=Math.max(0,energy-1);syncPet();}
        if(petArt!=null)petArt.invalidate();if(islandCat!=null)islandCat.invalidate();
        if(dotClock!=null)dotClock.setText(new SimpleDateFormat("hh:mm a",Locale.US).format(new Date()));
        timer.postDelayed(this,650);
    }};

    private void render(){
        getWindow().setStatusBarColor(0xff68b5a9);
        getWindow().setNavigationBarColor(0xffb4b8a6);
        final int BORDER=0xff8e7396,CREAM=0xfff2e4d5,INK=0xff51405e,LAVENDER=0xffc5addd;
        FrameLayout scene=new FrameLayout(this);
        scene.addView(new PixelLandscape(this),new FrameLayout.LayoutParams(-1,-1));
        setContentView(scene);
        LinearLayout screen=vertical();screen.setPadding(dp(12),dp(7),dp(12),dp(11));
        scene.addView(screen,new FrameLayout.LayoutParams(-1,-1));

        add(screen,label("✦  N O S T A L O O M  P I X E L  ✦",15,0xfff8e9e7),27);
        add(screen,label("A N D R O I D   L A U N C H E R",9,0xfff8f0e9),20);

        LinearLayout top=horizontal();
        LinearLayout timeCard=vertical();timeCard.setPadding(dp(6),dp(8),dp(6),dp(5));
        timeCard.setBackground(outlined(CREAM,18,BORDER));
        LinearLayout.LayoutParams tL=new LinearLayout.LayoutParams(0,dp(102),1.2f);tL.rightMargin=dp(8);
        top.addView(timeCard,tL);
        dotClock=label("",29,0xffdac3ec);dotClock.setBackground(shape(0xff302a30,11));
        dotClock.setText(new SimpleDateFormat("hh:mm a",Locale.US).format(new Date()));
        add(timeCard,dotClock,58);
        TextView date=label(new SimpleDateFormat("EEE dd MMM",Locale.US).format(new Date()).toUpperCase(Locale.US),11,INK);
        add(timeCard,date,28);
        LinearLayout weather=vertical();weather.setGravity(Gravity.CENTER);weather.setPadding(dp(4),0,dp(4),0);
        weather.setBackground(outlined(0xffd6c3e4,18,BORDER));
        LinearLayout.LayoutParams wL=new LinearLayout.LayoutParams(0,dp(102),.9f);top.addView(weather,wL);
        add(weather,label("☀  28°C",23,INK),47);
        add(weather,label("PARTLY CLOUDY",10,INK),18);
        add(weather,label("↑ 32°C    ↓ 24°C",10,INK),23);
        add(screen,top,111);

        // Let the fantasy island and cat remain visible between upper cards and pet panel.
        FrameLayout gap=new FrameLayout(this);screen.addView(gap,new LinearLayout.LayoutParams(-1,0,1));
        islandCat=new PetArt();
        FrameLayout.LayoutParams catLP=new FrameLayout.LayoutParams(dp(77),dp(73),Gravity.CENTER);
        catLP.topMargin=dp(16);gap.addView(islandCat,catLP);
        gap.setOnClickListener(v->showPetActions());

        LinearLayout lower=horizontal();
        LinearLayout left=vertical();
        lower.addView(left,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout petPanel=vertical();petPanel.setPadding(dp(8),dp(5),dp(8),dp(7));
        petPanel.setBackground(outlined(CREAM,13,BORDER));
        add(left,petPanel,143);
        LinearLayout petTitle=horizontal();petTitle.setBackground(shape(0xffbda9d1,7));
        TextView title=label("♡ NOSTALOOM PIXEL",11,INK);
        petTitle.addView(title,new LinearLayout.LayoutParams(0,dp(25),1));
        TextView choose=label("▣",18,INK);petTitle.addView(choose,item(30,25));
        choose.setOnClickListener(v->{pet=(pet+1)%names.length;syncPet();});
        add(petPanel,petTitle,25);
        LinearLayout petContent=horizontal();
        FrameLayout artBox=new FrameLayout(this);petArt=new PetArt();
        artBox.addView(petArt);
        artBox.setOnClickListener(v->showPetActions());
        petContent.addView(artBox,item(99,80));
        LinearLayout bars=vertical();bars.setPadding(dp(7),dp(4),0,0);
        petContent.addView(bars,new LinearLayout.LayoutParams(0,dp(80),1));
        petName=label(names[pet],13,INK);add(bars,petName,23);
        healthText=label("HAPPINESS  "+health+"%",10,INK);add(bars,healthText,17);
        healthBar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        healthBar.setMax(100);healthBar.setProgress(health);
        healthBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffa678a3));
        healthBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xffcfc3ba));
        add(bars,healthBar,13);
        petStats=label("HUNGER "+hunger+"%   ENERGY "+energy+"%",9,INK);add(bars,petStats,18);
        add(petPanel,petContent,80);
        LinearLayout actions=horizontal();
        TextView feed=action("♥ FEED",LAVENDER,()->doPetAction("EAT"));
        actions.addView(feed,new LinearLayout.LayoutParams(0,dp(30),1));
        TextView play=action("★ PLAY",LAVENDER,()->doPetAction("PLAY"));
        LinearLayout.LayoutParams playLP=new LinearLayout.LayoutParams(0,dp(30),1);playLP.leftMargin=dp(4);
        actions.addView(play,playLP);
        soundText=action("♫ ON",LAVENDER,()->{sound=!sound;syncPet();});
        LinearLayout.LayoutParams sLP=new LinearLayout.LayoutParams(0,dp(30),1);sLP.leftMargin=dp(4);
        actions.addView(soundText,sLP);
        add(petPanel,actions,30);
        petPanel.setOnLongClickListener(v->{showPetActions();return true;});
        LinearLayout iconArea=vertical();iconArea.setPadding(dp(3),dp(12),dp(3),0);
        add(left,iconArea,184);
        String[][] icons={{"CAMERA","GALLERY","MUSIC","FILES"},{"SETTINGS","BROWSER","CALCULATOR","LOCKSCREEN"}};
        for(String[] rowNames:icons){
            LinearLayout line=horizontal();
            for(String app:rowNames){
                LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(85),1);
                lp.leftMargin=dp(1);lp.rightMargin=dp(1);line.addView(tile,lp);
                LinearLayout plaque=vertical();plaque.setGravity(Gravity.CENTER);
                plaque.setBackground(outlined(0xffe7d7e5,13,BORDER));
                tile.addView(plaque,item(48,54));
                PixelGlyph glyph=new PixelGlyph(app);plaque.addView(glyph,item(36,40));
                TextView name=label(app,9,INK);name.setBackground(shape(CREAM,5));
                add(tile,name,18);
                tile.setOnClickListener(v->shortcut(app));
            }
            add(iconArea,line,89);
        }

        LinearLayout social=vertical();social.setGravity(Gravity.CENTER);
        social.setPadding(dp(3),dp(8),dp(3),dp(8));
        social.setBackground(outlined(CREAM,15,BORDER));
        LinearLayout.LayoutParams soc=new LinearLayout.LayoutParams(dp(55),-1);soc.leftMargin=dp(8);
        lower.addView(social,soc);
        for(String app:new String[]{"WHATSAPP","FACEBOOK","YOUTUBE","TIKTOK"}){
            LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);
            social.addView(tile,new LinearLayout.LayoutParams(-1,0,1));
            PixelGlyph glyph=new PixelGlyph(app);tile.addView(glyph,item(43,43));
            tile.setOnClickListener(v->shortcut(app));
        }
        add(screen,lower,327);
        LinearLayout dots=horizontal();dots.setGravity(Gravity.CENTER);
        add(screen,dots,24);
        for(int i=0;i<4;i++){TextView d=label("●",13,i==0?0xfff7e6d8:0xffc8d9d0);dots.addView(d,item(14,20));}

        LinearLayout dock=horizontal();dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(7),dp(5),dp(7),dp(5));
        dock.setBackground(outlined(CREAM,17,BORDER));
        add(screen,dock,70);
        String[] shortcuts={"PHONE","MESSAGES","APPS"};
        for(String app:shortcuts){
            LinearLayout iconSlot=horizontal();iconSlot.setGravity(Gravity.CENTER);
            dock.addView(iconSlot,new LinearLayout.LayoutParams(0,-1,1));
            PixelGlyph glyph=new PixelGlyph(app);iconSlot.addView(glyph,item(40,42));
            TextView cap=label(app.equals("APPS")?"▦":app,app.equals("APPS")?25:11,INK);
            iconSlot.addView(cap,new LinearLayout.LayoutParams(-2,-1));
            iconSlot.setOnClickListener(v->shortcut(app));
        }
    }
    private void persistPet(){
        getSharedPreferences("nostaloom_pet",MODE_PRIVATE).edit()
            .putInt("pet",pet).putInt("happy",health).putInt("hunger",hunger)
            .putInt("energy",energy).putBoolean("sound",sound).apply();
    }
    private void doPetAction(String action){
        petAction=action;motionTicks=7;
        if(action.equals("EAT")){hunger=Math.min(100,hunger+23);health=Math.min(100,health+5);}
        else if(action.equals("PLAY")){health=Math.min(100,health+19);energy=Math.max(0,energy-13);hunger=Math.max(0,hunger-6);}
        else if(action.equals("SLEEP")){energy=Math.min(100,energy+28);}
        else if(action.equals("RUN")){health=Math.min(100,health+7);energy=Math.max(0,energy-9);}
        else if(action.equals("REST")){energy=Math.min(100,energy+14);}
        petSound();syncPet();
    }
    private void showPetActions(){
        final String[] actions={"FEED","PLAY","RUN","WALK","SLEEP","REST"};
        new android.app.AlertDialog.Builder(this).setTitle(names[pet]+" • TAMAGOTCHI")
            .setItems(actions,(dialog,which)->doPetAction(
                actions[which].equals("FEED")?"EAT":actions[which]))
            .show();
    }
    private void syncPet(){
        if(petName!=null)petName.setText(names[pet]);
        if(healthText!=null)healthText.setText("HAPPINESS "+health+"%");
        if(petStats!=null)petStats.setText("HUNGER "+hunger+"%  ENERGY "+energy+"%");
        if(healthBar!=null)healthBar.setProgress(health);
        if(soundText!=null)soundText.setText(sound?"♫ ON":"♫ OFF");
        if(petArt!=null)petArt.invalidate();if(islandCat!=null)islandCat.invalidate();
        persistPet();
    }
    private void petSound(){if(!sound)return;try{
        if(tone==null)tone=new ToneGenerator(AudioManager.STREAM_MUSIC,35);
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2,100);
    }catch(Exception ignored){}}
    private void showOptions(){
        String[] options={"Purple Retro Theme","Blue Retro Theme","Pink Retro Theme","Installed Apps"};
        new android.app.AlertDialog.Builder(this).setTitle("NOSTALOOM PIXEL SETTINGS")
            .setItems(options,(d,which)->{
                if(which==3){showAppDrawer();return;}
                theme=which;Toast.makeText(this,"Theme setting preview: "+options[which],Toast.LENGTH_SHORT).show();
            }).show();
    }
    private void shortcut(String name){
        if(name.equals("APPS")){showAppDrawer();return;}
        if(name.equals("SETTINGS")){startActivity(new Intent(Settings.ACTION_SETTINGS));return;}
        if(name.equals("LOCK") || name.equals("LOCKSCREEN")){Toast.makeText(this,"Secure lock is not yet enabled in this test build",Toast.LENGTH_LONG).show();return;}
        if(name.equals("PHONE")){
            try{startActivity(new Intent(Intent.ACTION_DIAL));return;}catch(Exception ignored){}
        }
        if(name.equals("CAMERA")){
            try{startActivity(new Intent("android.media.action.IMAGE_CAPTURE"));return;}catch(Exception ignored){}
        }
        if(name.equals("BROWSER")){
            try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com")));return;}catch(Exception ignored){}
        }
        if(name.equals("MESSAGES")){
            try{startActivity(new Intent(Intent.ACTION_SENDTO,android.net.Uri.parse("smsto:")));return;}catch(Exception ignored){}
        }
        String pkg=null;
        switch(name){
            case "WHATSAPP":pkg="com.whatsapp";break;
            case "FACEBOOK":pkg="com.facebook.katana";break;
            case "YOUTUBE":pkg="com.google.android.youtube";break;
            case "TIKTOK":pkg="com.zhiliaoapp.musically";break;
            case "BROWSER":pkg="com.android.chrome";break;
            case "CAMERA":pkg="com.android.camera";break;
            case "PHONE":pkg="com.google.android.dialer";break;
            case "MESSAGES":pkg="com.google.android.apps.messaging";break;
            case "CALC":case "CALCULATOR":pkg="com.miui.calculator";break;
            case "GALLERY":pkg="com.miui.gallery";break;
            case "FILES":pkg="com.android.documentsui";break;
            case "MUSIC":pkg="com.miui.player";break;
        }
        if(pkg!=null)try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);
            if(i!=null){startActivity(i);return;}}catch(Exception ignored){}
        // Fallback to label search for manufacturer-specific apps.
        String word=name.equals("CALC")?"calculator":name.equals("FILES")?"file":name.toLowerCase(Locale.US);
        PackageManager pm=getPackageManager();
        Intent q=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo ri:pm.queryIntentActivities(q,0)){
            if(String.valueOf(ri.loadLabel(pm)).toLowerCase(Locale.US).contains(word)){
                try{Intent i=new Intent();i.setClassName(ri.activityInfo.packageName,ri.activityInfo.name);startActivity(i);return;}
                catch(Exception ignored){}
            }
        }
        Toast.makeText(this,"Application not found: "+name,Toast.LENGTH_SHORT).show();
    }
    private void showAppDrawer(){
        if(appDialog!=null&&appDialog.isShowing())return;
        appDialog=new Dialog(this);appDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout holder=vertical();holder.setPadding(dp(16),dp(16),dp(16),dp(10));holder.setBackground(shape(BG,18));
        LinearLayout head=horizontal();
        TextView title=label("▦  APPLICATION DRAWER",16,YELLOW);
        head.addView(title,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView close=action("✕",YELLOW,()->appDialog.dismiss());head.addView(close,item(43,40));
        add(holder,head,47);
        EditText filter=new EditText(this);filter.setSingleLine(true);filter.setHint("Search apps...");
        filter.setHintTextColor(MUTED);filter.setTextColor(LIGHT);filter.setTextSize(15);
        filter.setPadding(dp(14),0,dp(14),0);filter.setBackground(shape(PANEL,9));
        add(holder,filter,49);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
        LinearLayout appRows=vertical();appRows.setPadding(0,dp(7),0,0);scroll.addView(appRows);
        holder.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        PackageManager pm=getPackageManager();
        Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installed=new ArrayList<>(pm.queryIntentActivities(query,0));
        Collections.sort(installed,new ResolveInfo.DisplayNameComparator(pm));
        Runnable populate=()->{
            appRows.removeAllViews();
            String search=filter.getText().toString().trim().toLowerCase(Locale.ROOT);
            for(ResolveInfo ri:installed){
                if(ri.activityInfo.packageName.equals(getPackageName()))continue;
                String text=String.valueOf(ri.loadLabel(pm));if(!text.toLowerCase(Locale.ROOT).contains(search))continue;
                LinearLayout row=horizontal();row.setPadding(dp(11),dp(5),dp(11),dp(5));
                row.setBackground(shape(PANEL,9));
                ImageView icon=new ImageView(this);try{icon.setImageDrawable(ri.loadIcon(pm));}catch(Exception ignored){}
                row.addView(icon,item(36,36));
                TextView name=label(text,13,LIGHT);name.setGravity(Gravity.CENTER_VERTICAL);
                name.setPadding(dp(13),0,0,0);row.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
                LinearLayout.LayoutParams rlp=item(-1,51);rlp.bottomMargin=dp(5);appRows.addView(row,rlp);
                row.setOnClickListener(v->{try{Intent i=new Intent();i.setClassName(ri.activityInfo.packageName,ri.activityInfo.name);startActivity(i);appDialog.dismiss();}
                catch(Exception ex){Toast.makeText(this,"Unable to open "+text,Toast.LENGTH_SHORT).show();}});
            }
        };
        filter.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){populate.run();}
            public void afterTextChanged(Editable e){}
        });
        populate.run();
        appDialog.setContentView(holder);
        Window win=appDialog.getWindow();
        if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);
            win.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.94), (int)(getResources().getDisplayMetrics().heightPixels*.82));
            win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);}
        appDialog.show();
        win=appDialog.getWindow();
        if(win!=null)win.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.94),(int)(getResources().getDisplayMetrics().heightPixels*.82));
    }
    private final class DottedColumn extends LinearLayout{
        Paint p=new Paint(1);DottedColumn(){super(MainActivity.this);setOrientation(1);setWillNotDraw(false);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);
            p.setColor(0xff2a1c43);float step=dp(14);
            for(float x=step/2;x<getWidth();x+=step)for(float y=step/2;y<getHeight();y+=step)
                c.drawCircle(x,y,dp(1.1f),p);
        }
    }
    private final class PetArt extends View{
        Paint p=new Paint(1);
        final String[][] sprites={
            {"....KK........KK....","...KPPK......KPPK...","..KPPPPKKKKKKPPPPK..","..KPPPPPPPPPPPPPPK..","..KPPPPPPPPPPPPPPK..","..KPPWWPPPPPPWWPPK..","..KPPWWPPPPPPWWPPK..","..KPPPPPPNNPPPPPPK..","..KPPPPPNNNNPPPPPK..","..KPPPPPPPPPPPPPPK..","...KPPPPPPPPPPPPK...","....KKPPPPPPPPKK....",".....KPPPPPPPPK.....","....KPPPPPPPPPPK....","....KPPPPPPPPPPK....","....KKKKKKKKKKKK...."},
            {".....KK......KK.....","....KPPK....KPPK....","...KPPPPKKKKPPPPK...","..KPPPPPPPPPPPPPPK..","..KPPPPPPPPPPPPPPK..","..KPPWWPPPPPPWWPPK..","..KPPWWPPPPPPWWPPK..","..KPPPPPPNNPPPPPPK..","..KPPPPPNNNNPPPPPK..","...KPPPPPPPPPPPPK...","....KKPPPPPPPPKK....",".....KPPPPPPPPK.....","....KPPPPPPPPPPK....","....KPPPPPPPPPPK....","....KKKKKKKKKKKK...."},
            {"..KKKK..........KKKK..","..KPPK..........KPPK..","..KPPK..........KPPK..","..KPPPKKKKKKKKKKPPK..","...KPPPPPPPPPPPPPK...","...KPPWWPPPPWWPPK...","...KPPWWPPPPWWPPK...","...KPPPPPPNNPPPPK...","...KPPPPPNNNNPPPK...","....KPPPPPPPPPPK....",".....KKPPPPPPKK.....","......KPPPPPPK......",".....KPPPPPPPPK.....",".....KPPPPPPPPK.....",".....KKKKKKKKKK....."}
        };
        PetArt(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);
            String[] art=sprites[pet];int max=0;for(String line:art)max=Math.max(max,line.length());
            float unit=Math.min(getWidth()/(float)(max+5),getHeight()/(float)(art.length+2));
            float left=(getWidth()-max*unit)*.5f,top=(getHeight()-art.length*unit)*.5f;
            if(petAction.equals("WALK")||petAction.equals("RUN"))left+=(motionFrame%4-2)*unit*(petAction.equals("RUN")?2:1);
            if(petAction.equals("PLAY")||petAction.equals("RUN"))top-=(motionFrame%2)*unit*2;
            if(petAction.equals("SLEEP"))top+=unit*3;
            if(petAction.equals("IDLE")||petAction.equals("REST"))top+=(blink?unit:0);
            for(int y=0;y<art.length;y++)for(int x=0;x<art[y].length();x++){
                char a=art[y].charAt(x);if(a=='.')continue;
                int color=a=='K'?0xff7c4db2:a=='P'?(pet==2?0xfff2e1fc:pet==1?0xffcfa0f5:0xffd9b0f9):a=='W'?((blink||petAction.equals("SLEEP"))?0xffd7b5eb:0xff221139):0xffeb719c;
                p.setColor(color);c.drawRect(left+x*unit,top+y*unit,left+(x+1)*unit+.15f,top+(y+1)*unit+.15f,p);
            }
        }
    }
    private final class PixelGlyph extends View{
        private final String type;Paint p=new Paint(1);
        PixelGlyph(String name){super(MainActivity.this);type=name;}
        @Override protected void onDraw(Canvas c){super.onDraw(c);
            int color=type.equals("YOUTUBE")?0xffe95066:type.equals("WHATSAPP")?0xff30ba7b:
                type.equals("FACEBOOK")?0xff5d94ee:type.equals("TIKTOK")?0xffb879d9:0xff6d42ad;
            float size=Math.min(getWidth(),getHeight())/10f;
            float ox=(getWidth()-size*10)/2,oy=(getHeight()-size*10)/2;
            String[] pix={"..######..",".########.","##......##","##.####.##","##.#..#.##","##.#..#.##","##.####.##","##......##",".########.","..######.."};
            if(type.equals("LOCK"))pix=new String[]{"...####...","..##..##..","..##..##..","..######..",".########.","##......##","##..##..##","##..##..##","##......##",".########."};
            if(type.equals("BROWSER"))pix=new String[]{"..######..",".########.","###....###","##..##..##","##.####.##","##.####.##","##..##..##","###....###",".########.","..######.."};
            for(int y=0;y<pix.length;y++)for(int x=0;x<pix[y].length();x++)if(pix[y].charAt(x)=='#'){
                p.setColor(color);c.drawRect(ox+x*size,oy+y*size,ox+(x+1)*size,oy+(y+1)*size,p);
            }
        }
    }
}
