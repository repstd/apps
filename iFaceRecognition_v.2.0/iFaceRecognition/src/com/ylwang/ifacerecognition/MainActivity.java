package com.ylwang.ifacerecognition;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
//MainActivity
public class MainActivity extends Activity {
	private MenuItem MenuStart;
	private MenuItem MenuAdd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuStart=menu.add(R.string.menu_main_start);
		MenuAdd=menu.add(R.string.menu_main_add_person);
		return true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
		
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		if(item==MenuStart){
			Intent intent = new Intent();  
	        intent.setClass(MainActivity.this, FaceRecTestActivity.class);  
	     
	        startActivity(intent);  
		}
		else if(item==MenuAdd){
			Intent intent = new Intent();  
	        intent.setClass(MainActivity.this, AddPersonActivity.class);  
	  
	        startActivity(intent);  
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.exit(0);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	
	
}
