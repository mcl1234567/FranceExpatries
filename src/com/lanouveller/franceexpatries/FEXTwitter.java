package com.lanouveller.franceexpatries;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class FEXTwitter extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		String url = "https://mobile.twitter.com/francexpatries";
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setData(Uri.parse(url));
		startActivity(intent);
		finish();
	}
}
