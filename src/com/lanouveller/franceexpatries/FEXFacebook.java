package com.lanouveller.franceexpatries;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class FEXFacebook extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		String url = "https://fr-fr.facebook.com/pages/France-Expatries/381485448534385";
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setData(Uri.parse(url));
		startActivity(intent);
		finish();
	}
}
