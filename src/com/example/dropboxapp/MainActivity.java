package com.example.dropboxapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.dropbox.sync.android.DbxAccountManager;

public class MainActivity extends Activity {

	private static final int REQUEST_LINK_TO_DBX = 0;
	private DbxAccountManager mDbxAcctMgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "l59nplqoyij0rwg" , "y17w2sbno1c1c1p");
	if(mDbxAcctMgr.hasLinkedAccount())
	    {
		Intent intent = new Intent(this,PostLink.class);
	    startActivity(intent);
	    }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Button to start the linking process..
	public void onClickLinkToDropbox(View view) {
	    mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
	}
	
	//Overriding the onActvityResult method to use the linked account for further actions..
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            Log.i("Dropbox account","Linked for user");
	        	Intent intent = new Intent(this,PostLink.class);
	    	    startActivity(intent);
	        } else {
	            // ... Link failed or was cancelled by the user.
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}

}
