package org.videolan.vlc.gui;

import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.vlc.AudioService;
import org.videolan.vlc.AudioServiceController;
import org.videolan.vlc.R;
import org.videolan.vlc.Util;
import org.videolan.vlc.VLCCallbackTask;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final static String TAG = "VLC/MainActivity";

	public static final String MEDIA_URL="rtsp://172.16.18.224/480x320.264";
	
    protected static final String ACTION_SHOW_PROGRESSBAR = "org.videolan.vlc.gui.ShowProgressBar";
    protected static final String ACTION_HIDE_PROGRESSBAR = "org.videolan.vlc.gui.HideProgressBar";
    protected static final String ACTION_SHOW_TEXTINFO = "org.videolan.vlc.gui.ShowTextInfo";

    private static final String PREF_SHOW_INFO = "show_info";
    private static final String PREF_FIRST_RUN = "first_run";

    private static final int ACTIVITY_RESULT_PREFERENCES = 1;
    private static final int ACTIVITY_SHOW_INFOLAYOUT = 2;


    private AudioServiceController mAudioController;

    private View mInfoLayout;
    private ProgressBar mInfoProgress;
    private TextView mInfoText;
    private String mCurrentFragment;

    private SharedPreferences mSettings;

    private int mVersionNumber = -1;
    private boolean mFirstRun = false;
    private boolean mScanNeeded = true;

    private Button playButton;
    private EditText meidaUrl;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(!LibVlcUtil.hasCompatibleCPU(this)){
			Log.e(TAG, LibVlcUtil.getErrorMsg());
			finish();
		}
		PackageInfo pinfo=null;
		try {
			pinfo=getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "packet info not found.");
		}
		if (pinfo != null)
            mVersionNumber = pinfo.versionCode;
		
		 /* Get settings */
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        /* Check if it's the first run */
        mFirstRun = mSettings.getInt(PREF_FIRST_RUN, -1) != mVersionNumber;
        if (mFirstRun) {
            Editor editor = mSettings.edit();
            editor.putInt(PREF_FIRST_RUN, mVersionNumber);
            editor.commit();
        }
        
        try {
			Util.getLibVlcInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
			finish();
			super.onCreate(savedInstanceState);
			return;
		}
        
		super.onCreate(savedInstanceState);
		
		/*** Start initializing the UI ***/

        /* Enable the indeterminate progress feature */
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
		setContentView(R.layout.activity_main);
		
		playButton=(Button) findViewById(R.id.play_button2);
		meidaUrl=(EditText) findViewById(R.id.media_url);
		
		playButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				VLCCallbackTask task=new VLCCallbackTask(MainActivity.this) {
					
					@Override
					public void run() {
						Log.d(TAG, "run");
						AudioServiceController c=AudioServiceController.getInstance();
						
						c.append(meidaUrl.getText().toString());
					}
				};
				
				task.execute();
			}
		});
		
		 /* Reload the latest preferences */
        reloadPreferences();
	}

	 @Override
	    protected void onResume() {
	        super.onResume();
//	        mAudioController.addAudioPlayer(mAudioPlayer);
	        AudioServiceController.getInstance().bindAudioService(this);	//此处绑定音频服务

	        /* FIXME: this is used to avoid having MainActivity twice in the backstack */
	        if (getIntent().hasExtra(AudioService.START_FROM_NOTIFICATION))
	            getIntent().removeExtra(AudioService.START_FROM_NOTIFICATION);

	        /* Load media items from database and storage */
//	        if (mScanNeeded)
//	            MediaLibrary.getInstance(this).loadMediaItems(this);
	    }
	    /**
	     * Stop audio player and save opened tab
	     */
	    @Override
	    protected void onPause() {
	        super.onPause();

	        /* Check for an ongoing scan that needs to be resumed during onResume */
//	        mScanNeeded = MediaLibrary.getInstance(this).isWorking();
	        /* Stop scanning for files */
//	        MediaLibrary.getInstance(this).stop();
	        /* Save the tab status in pref */
	        SharedPreferences.Editor editor = getSharedPreferences("MainActivity", MODE_PRIVATE).edit();
	        editor.putString("fragment", mCurrentFragment);
	        editor.commit();

//	        mAudioController.removeAudioPlayer(mAudioPlayer);
	        AudioServiceController.getInstance().unbindAudioService(this);
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();

//	        try {
//	            unregisterReceiver(messageReceiver);
//	        } catch (IllegalArgumentException e) {}
	    }

	    @Override
	    protected void onRestart() {
	        super.onRestart();
	        /* Reload the latest preferences */
	        reloadPreferences();
	    }
	 private void reloadPreferences() {
	        SharedPreferences sharedPrefs = getSharedPreferences("MainActivity", MODE_PRIVATE);
	        mCurrentFragment = sharedPrefs.getString("fragment", "video");
	 }
}
