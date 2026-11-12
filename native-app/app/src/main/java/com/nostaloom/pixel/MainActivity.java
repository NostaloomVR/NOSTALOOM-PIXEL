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
    private int pet=0, health=87, theme=0;
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
        render();timer.postDelayed(tick,650);
    }
    @Override protected void onDestroy(){timer.removeCallbacksAndMessages(null);if(tone!=null)tone.release();super.onDestroy();}
    private final Runnable tick=new Runnable(){public void run(){
        blink=!blink;if(petArt!=null)petArt.invalidate();
        if(dotClock!=null)dotClock.setText(new SimpleDateFormat("hh:mm a",Locale.US).format(new Date()));
        timer.postDelayed(this,650);
    }};

    private void render(){
        // Fixed dock is outside scrollable home content.
        LinearLayout shell=vertical();shell.setBackgroundColor(BG);setContentView(shell);
        ScrollView home=new ScrollView(this);home.setFillViewport(false);home.setVerticalScrollBarEnabled(false);
        shell.addView(home,new LinearLayout.LayoutParams(-1,0,1));
        DottedColumn contents=new DottedColumn();contents.setPadding(dp(14),dp(9),dp(14),dp(9));
        home.addView(contents);
        LinearLayout heading=horizontal();
        TextView logo=label("▣ NOSTALOOM",18,YELLOW);heading.addView(logo,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView settings=label("⚙",25,YELLOW);heading.addView(settings,item(40,42));settings.setOnClickListener(v->showOptions());
        add(contents,heading,42);
        dotClock=label("",38,PURPLE);dotClock.setLetterSpacing(.035f);
        dotClock.setText(new SimpleDateFormat("hh:mm a",Locale.US).format(new Date()));
        add(contents,dotClock,54);
        add(contents,label("•  PIXEL PET OS  •",10,MUTED),22);

        LinearLayout game=vertical();game.setPadding(dp(10),dp(9),dp(10),dp(10));
        game.setBackground(outlined(PANEL,16,0xff563b7c));
        LinearLayout.LayoutParams gameLP=item(-1,249);gameLP.topMargin=dp(9);contents.addView(game,gameLP);
        LinearLayout gameTop=horizontal();
        TextView title=label("♥  PET",15,YELLOW);title.setGravity(Gravity.CENTER_VERTICAL);
        gameTop.addView(title,new LinearLayout.LayoutParams(0,dp(32),1));
        soundText=action(sound?"♫ ON":"♫ OFF",YELLOW,()->{sound=!sound;syncPet();});
        gameTop.addView(soundText,item(69,31));add(game,gameTop,32);
        FrameLayout petArea=new FrameLayout(this);
        petArt=new PetArt();petArea.addView(petArt,new FrameLayout.LayoutParams(-1,-1));
        add(game,petArea,112);
        petName=label(names[pet],13,LIGHT);add(game,petName,22);
        LinearLayout healthLine=horizontal();healthText=label("HEALTH "+health+"%",11,YELLOW);
        healthLine.addView(healthText,new LinearLayout.LayoutParams(0,dp(21),1));
        healthBar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);healthBar.setMax(100);
        healthBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xff87df94));
        healthBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff49315a));
        healthBar.setProgress(health);healthLine.addView(healthBar,item(138,13));
        add(game,healthLine,24);
        LinearLayout controls=horizontal();controls.setPadding(0,dp(7),0,0);
        TextView feed=action("♥ FEED",YELLOW,()->{health=Math.min(100,health+13);petSound();syncPet();});
        controls.addView(feed,new LinearLayout.LayoutParams(0,dp(37),1));
        TextView next=action("◂ PET ▸",YELLOW,()->{pet=(pet+1)%names.length;petSound();syncPet();});
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,dp(37),1);np.leftMargin=dp(9);
        controls.addView(next,np);add(game,controls,44);

        LinearLayout banner=horizontal();banner.setPadding(dp(6),0,dp(6),0);
        TextView appsCaption=label("▦  MY APPS",14,YELLOW);
        banner.addView(appsCaption,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView openApps=action("ALL APPS  ▸",YELLOW,()->showAppDrawer());
        banner.addView(openApps,item(111,34));
        add(contents,banner,50);
        LinearLayout grid=vertical();grid.setBackground(outlined(0xff201338,13,0xff473366));
        grid.setPadding(dp(7),dp(10),dp(7),dp(8));add(contents,grid,172);
        String[][] entries={{"WHATSAPP","FACEBOOK","YOUTUBE"},{"TIKTOK","BROWSER","LOCK"},{"CALC","CAMERA","APPS"}};
        for(String[] line:entries){
            LinearLayout row=horizontal();add(grid,row,49);
            for(String name:line){
                LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);cell.setOnClickListener(v->shortcut(name));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(46),1);
                row.addView(cell,cp);
                PixelGlyph glyph=new PixelGlyph(name);cell.addView(glyph,item(30,27));
                TextView cap=label(name,8,LIGHT);add(cell,cap,13);
            }
        }
        LinearLayout tray=horizontal();
        TextView privateText=label("▣  PRIVATE APPS",12,MUTED);
        tray.addView(privateText,new LinearLayout.LayoutParams(0,dp(46),1));
        TextView openDrawer=action("APP DRAWER",YELLOW,()->showAppDrawer());tray.addView(openDrawer,item(126,33));
        add(contents,tray,51);

        LinearLayout fixed=vertical();fixed.setPadding(dp(12),dp(6),dp(12),dp(9));fixed.setBackgroundColor(BG);
        add(shell,fixed,90);
        HorizontalScrollView swipe=new HorizontalScrollView(this);swipe.setHorizontalScrollBarEnabled(false);
        LinearLayout dock=horizontal();dock.setPadding(dp(8),dp(4),dp(8),dp(4));
        dock.setBackground(outlined(YELLOW,14,0xffd3a747));swipe.addView(dock);
        String[] dockNames={"PHONE","MESSAGES","BROWSER","SETTINGS","CAMERA","CALC","LOCK","APPS"};
        for(String name:dockNames){
            LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp=item(68,64);dock.addView(tile,lp);
            PixelGlyph icon=new PixelGlyph(name);tile.addView(icon,item(39,38));
            TextView cap=label(name,8,BG);add(tile,cap,16);
            tile.setOnClickListener(v->shortcut(name));
        }
        add(fixed,swipe,70);
    }
    private void syncPet(){
        if(petName!=null)petName.setText(names[pet]);
        if(healthText!=null)healthText.setText("HEALTH "+health+"%");
        if(healthBar!=null)healthBar.setProgress(health);
        if(soundText!=null)soundText.setText(sound?"♫ ON":"♫ OFF");
        if(petArt!=null)petArt.invalidate();
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
        if(name.equals("LOCK")){Toast.makeText(this,"Secure lock requires a separate reviewed implementation",Toast.LENGTH_LONG).show();return;}
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
            case "CALC":pkg="com.miui.calculator";break;
        }
        if(pkg!=null)try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);
            if(i!=null){startActivity(i);return;}}catch(Exception ignored){}
        // Fallback to label search for manufacturer-specific apps.
        String word=name.equals("CALC")?"calculator":name.equals("PHONE")?"phone":name.toLowerCase(Locale.US);
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
            float left=(getWidth()-max*unit)*.5f,top=(getHeight()-art.length*unit)*.5f+(blink?unit:0);
            for(int y=0;y<art.length;y++)for(int x=0;x<art[y].length();x++){
                char a=art[y].charAt(x);if(a=='.')continue;
                int color=a=='K'?0xff7c4db2:a=='P'?(pet==2?0xfff2e1fc:pet==1?0xffcfa0f5:0xffd9b0f9):a=='W'?(blink?0xffd7b5eb:0xff221139):0xffeb719c;
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
