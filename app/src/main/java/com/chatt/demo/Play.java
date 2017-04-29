/**
 * Created by ankit on 22/4/17.
 */
package com.chatt.demo;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.chatt.demo.utils.Const;

public class Play extends Activity {
    VideoView v;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        String path=getIntent().getStringExtra(Const.EXTRA_DATA);
        v=(VideoView) findViewById(R.id.videoView1);
        MediaController mc=new MediaController(this);
        mc.setAnchorView(v);
        v.setMediaController(mc);
        v.setVideoPath(path);
        v.requestFocus();
        v.start();

    }

}