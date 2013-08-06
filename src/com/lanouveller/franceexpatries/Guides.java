package com.lanouveller.franceexpatries;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.lanouveller.franceexpatries.CommentaireDialogFragment.CommentaireDialogListener;

/**
 * Nommage des méthodes:
 * 	'init' pour l'initialisation des différentes variables de contenus chargées via ressources externes.
 * 	'configurationLayout' pour le paramètrage de la structure de la vue.
 * 	'From{Ressources}' BDD ou SharedPreferences
 * Nommage des variables:
 * 	Exemple d'abréviation de vues:
 * 	{fonction/contenu}Ll: (LinearLayout)
 * 	variableIv: (ImageView)
 * Nommage d'un argument d'entrée dans une fonction:
 * 	_variable
 * Nommage des constantes en majuscule.
 * 
 * @author Morvan C.
 */
public class Guides extends SlidingActivity implements CommentaireDialogListener {

  	private static final int SHOW_PREFERENCES = 1;

  	private SlidingMenu slidingMenu;

  	private Intent intent;

	private String nomPaysUtilisateur;

	private TextView titreGuidesTv;

  	private float heightScreen;
  	private float widthScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Récupère les dimensions du terminal.
		initScreenSize();

		// Récupère le pays de l'utilisateur.
		initPaysFromBDD();

		// Récupère le pays de l'utilisateur.
		if(checkDataHorsLigneFromBDD() || checkDataFromBDD()) {

	  		// Configuration du Layout des Guides.
	  		configurationLayout();

	  		// Configuration du menu gauche ( Menu Sliding ).
	  		configurationMenuSliding();
	  		configurationItemsMenuSliding();
		}
		// Aucune donnée pour ce pays.
		else {
	  		// Configuration du menu gauche ( Menu Sliding ).
	  		configurationMenuSliding();
	  		configurationItemsMenuSliding();

	  		noGuides();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	if(checkDataFromBDD()) {
    		// Insertion du 'menu overflow'.
    		getMenuInflater().inflate(R.menu.menu_overflow_guides, menu);
    	}
    	else {
    		// Insertion du 'Menu overflow'. Unable to share if there is no data !
        	getMenuInflater().inflate(R.menu.menu_overflow_informations_no_data, menu);
    	}

      	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId()) {
			// Items du Menu 'overflow'.
			case android.R.id.home: {
				slidingMenu.showMenu();
			} return true;

			case R.id.item_preferences: {
				Intent intent = new Intent(this, Preferences.class);
				intent.putExtra("CALLING_CLASS", "Guides");
	    		startActivityForResult(intent, SHOW_PREFERENCES);
			} return true;

			// Intégration des commentaires utilisateur ( non opérationnel ) .
			/**case R.id.item_commentaire: {
    			DialogFragment dialog = new CommentaireDialogFragment();
    			dialog.show(getFragmentManager(), "CommentaireDialogFragment");
    		} return true;*/
    	}

    	return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);

    	if(requestCode == SHOW_PREFERENCES && resultCode == Activity.RESULT_OK) {
      		setTitle("Guides - " + nomPaysUtilisateur);

    		// Enregistrement du pays de l'utilisateur.
			initPaysFromBDD();

    		if(checkDataFromBDD()) {
    	  		// Configuration du Layout des Guides.
    	  		configurationLayout(); 
    	  	}
    		else {
    			noGuides();
    		}
    	}
    }

    public void configurationLayout() 
    {
  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
	    /**ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);*/

  		setTitle("Guides - " + nomPaysUtilisateur);

		// Configuration de l'intent pour l'accès au guide demandé.
		intent = new Intent(Guides.this, FichesWeb.class);
		intent.putExtra("CALLING_CLASS", "Guides");

		// Configuration des paramètres.
  		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
  		LinearLayout.LayoutParams tvLlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  		LinearLayout.LayoutParams titreTvLlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

  		tvLlp.setMargins(0, 10, 0, 0);
  		titreTvLlp.setMargins(20, 10, 0, 10);

  		LinearLayout ll = new LinearLayout(this);
  		ll.setLayoutParams(llp);
  		ll.setOrientation(LinearLayout.VERTICAL);
  		ll.setBackgroundColor(Color.WHITE);

  		titreGuidesTv = new TextView(this);
  		TextView itemArgentTv = new ItemGuides(this);
  		TextView itemEmploiTv = new ItemGuides(this);
  		TextView itemPasseportTv = new ItemGuides(this);
  		TextView itemSanteTv = new ItemGuides(this);
  		TextView itemTransportTv = new ItemGuides(this);
  		TextView itemVieCulturelleTv = new ItemGuides(this);
  		TextView itemVieQuotidienneTv = new TextView(this);

  		itemArgentTv.setId(0);
		itemEmploiTv.setId(1);
		itemPasseportTv.setId(2);
		itemSanteTv.setId(3);
		itemTransportTv.setId(4);
		itemVieCulturelleTv.setId(5);
		itemVieQuotidienneTv.setId(6);

		titreGuidesTv.setLayoutParams(titreTvLlp);
  		itemArgentTv.setLayoutParams(tvLlp);
		itemEmploiTv.setLayoutParams(tvLlp);
		itemPasseportTv.setLayoutParams(tvLlp);
		itemSanteTv.setLayoutParams(tvLlp);
		itemTransportTv.setLayoutParams(tvLlp);
		itemVieCulturelleTv.setLayoutParams(tvLlp);
		itemVieQuotidienneTv.setLayoutParams(tvLlp);

		String[] guidesDisponibles = getResources().getStringArray(R.array.guides_disponibles);

		titreGuidesTv.setText("Guides");
  		itemArgentTv.setText(guidesDisponibles[0]);
		itemEmploiTv.setText(guidesDisponibles[1]);
		itemPasseportTv.setText(guidesDisponibles[2]);
		itemSanteTv.setText(guidesDisponibles[3]);
		itemTransportTv.setText(guidesDisponibles[4]);
		itemVieCulturelleTv.setText(guidesDisponibles[5]);
		itemVieQuotidienneTv.setText(guidesDisponibles[6]);

		//titreGuidesTv.setGravity(Gravity.CENTER);
		itemArgentTv.setGravity(Gravity.CENTER);
		itemEmploiTv.setGravity(Gravity.CENTER);
		itemPasseportTv.setGravity(Gravity.CENTER);
		itemSanteTv.setGravity(Gravity.CENTER);
		itemTransportTv.setGravity(Gravity.CENTER);
		itemVieCulturelleTv.setGravity(Gravity.CENTER);
		itemVieQuotidienneTv.setGravity(Gravity.CENTER);
		
		// Décalage par rapport à la ligne de séparation.
		itemArgentTv.setPadding(0, 0, 0, 10);
		itemEmploiTv.setPadding(0, 0, 0, 10);
		itemPasseportTv.setPadding(0, 0, 0, 10);
		itemSanteTv.setPadding(0, 0, 0, 10);
		itemTransportTv.setPadding(0, 0, 0, 10);
		itemVieCulturelleTv.setPadding(0, 0, 0, 10);
		//itemVieQuotidienneTv.setPadding(0, 0, 0, 10);

		titreGuidesTv.setTextColor(getResources().getColor(R.color.orange));

		// Application du style aux textviews.
		if(widthScreen > 2.5) {
			titreGuidesTv.setTextAppearance(this, R.style.titreInformationTablette);
			itemArgentTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemEmploiTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemPasseportTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemSanteTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemTransportTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemVieCulturelleTv.setTextAppearance(this, R.style.contenuGuidesTablette);
			itemVieQuotidienneTv.setTextAppearance(this, R.style.contenuGuidesTablette);
		}
		else {
			titreGuidesTv.setTextAppearance(this, R.style.titreInformationSmartphone);
			itemArgentTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemEmploiTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemPasseportTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemSanteTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemTransportTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemVieCulturelleTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
			itemVieQuotidienneTv.setTextAppearance(this, R.style.contenuGuidesSmartphone);
		}
		
		// Ajout du titre de l'activité.
		ll.addView(titreGuidesTv);

		// Configuration Guide 'Argent'.
		if(checkDataGuideArgentFromBDD()) {
			itemArgentTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("argent");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemArgentTv);
		}

		// Configuration Guide 'Emploi'.
		if(checkDataGuideEmploiFromBDD()) {
			itemEmploiTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					intent.setAction("emploi");
					startActivity(intent);
				}
			});

			ll.addView(itemEmploiTv);
		}

		// Configuration Guide 'Passeport'.
		if(checkDataGuidePasseportFromBDD()) {
			itemPasseportTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("passeport");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemPasseportTv);
		}

		// Configuration Guide 'Santé'.
 		if(checkDataGuideSanteFromBDD()) {
			itemSanteTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("sante");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemSanteTv);
 		}

		// Configuration Guide 'Transport'.
		if(checkDataGuideTransportFromBDD()) {
			itemTransportTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("transport");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemTransportTv);
		}

		// Configuration Guide 'Vie Culturelle'.
		if(checkDataGuideVieCulturelleFromBDD()) {
			itemVieCulturelleTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("vie_culturelle");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemVieCulturelleTv);
		}

		// Configuration Guide 'Vie Quotidienne'.
		if(checkDataVieQuotidienneFromBDD()) {
			itemVieQuotidienneTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		  			intent.setAction("vie_quotidienne");
		  			startActivity(intent);
		  		}
			});

			ll.addView(itemVieQuotidienneTv);
		}

		// Mise en place du layout.
  		setContentView(ll);
    }

	public boolean checkDataFromBDD() 
	{
		try {
			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDES_COLUMN).equalsIgnoreCase("guides_disponibles")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			Toast.makeText(getApplicationContext(), 
  					"La configuration a été interrompu. Veuillez la relancer en accédant aux Préférences dans le menu de l'application.", 
  					Toast.LENGTH_LONG).show();
  			finish();
  		}

		return false;
	}

	public boolean checkDataHorsLigneFromBDD() 
	{
  		try {
  			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

  			while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDES_HS_COLUMN).equalsIgnoreCase("guides_disponibles_hs")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			Toast.makeText(getApplicationContext(), 
  					"La configuration a été interrompu. Veuillez la relancer en accédant aux Préférences dans le menu de l'application.", 
  					Toast.LENGTH_LONG).show();
  			finish();
  		}

		return false;
	}

	public boolean checkDataGuideArgentHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_ARGENT_HS_COLUMN).equalsIgnoreCase("guide_argent_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideEmploiHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_EMPLOI_HS_COLUMN).equalsIgnoreCase("guide_emploi_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuidePasseportHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_PASSEPORT_HS_COLUMN).equalsIgnoreCase("guide_passeport_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideSanteHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_SANTE_HS_COLUMN).equalsIgnoreCase("guide_sante_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideTransportHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_TRANSPORT_HS_COLUMN).equalsIgnoreCase("guide_transport_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideVieCulturelleHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_CULTURELLE_HS_COLUMN).equalsIgnoreCase("guide_vie_culturelle_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataVieQuotidienneHsFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_QUOTIDIENNE_HS_COLUMN).equalsIgnoreCase("guide_vie_quotidienne_disponible_hs")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideArgentFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_ARGENT_COLUMN).equalsIgnoreCase("guide_argent_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideEmploiFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_EMPLOI_COLUMN).equalsIgnoreCase("guide_emploi_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuidePasseportFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_PASSEPORT_COLUMN).equalsIgnoreCase("guide_passeport_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideSanteFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_SANTE_COLUMN).equalsIgnoreCase("guide_sante_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideTransportFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_TRANSPORT_COLUMN).equalsIgnoreCase("guide_transport_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataGuideVieCulturelleFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_CULTURELLE_COLUMN).equalsIgnoreCase("guide_vie_culturelle_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

	public boolean checkDataVieQuotidienneFromBDD() 
	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_QUOTIDIENNE_COLUMN).equalsIgnoreCase("guide_vie_quotidienne_disponible")) {
  				return true;
  			}
  		}
		return false;
	}

  	public void initPaysFromBDD() 
  	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
  		while(cursor.moveToNext()) {
  			nomPaysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
  		}
  	}

    /**
     * Guides - Récupère les dimensions de l'écran.
     */
    public void initScreenSize() 
    {
    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;
    }

    /**
     * Guides - Layout affichant aucun guide.
     */
    public void noGuides() 
    {
  		setTitle("Guides - " + nomPaysUtilisateur);

    	LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 50, 50, 50);

    	TextView tv = new TextView(getApplication());
        tv.setLayoutParams(llp);
        tv.setTextColor(Color.BLACK);
    	tv.setText("Aucun guide n'est disponible pour ce pays.");
    	tv.setGravity(Gravity.CENTER);

    	if(widthScreen > 2.5) { tv.setTextAppearance(getApplicationContext(), R.style.contenuGuidesTablette); } 
    	else { tv.setTextAppearance(getApplicationContext(), R.style.contenuGuidesSmartphone); }

    	LinearLayout ll = new LinearLayout(getApplication());
    	ll.setBackgroundColor(Color.WHITE);
    	ll.addView(tv);

    	setContentView(ll);
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
        	return true;
        }

        return false;
    }

    /**
     * Adapte la taille du menu sliding en fonction de la taille de l'écran.
     */
    public void configurationMenuSliding() 
    {
  		setBehindContentView(R.layout.menu_sliding);

		// Configuration du 'Menu Sliding'.
		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		// Dimensions du 'Menu Sliding'.
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		//slidingMenu.setFadeEnabled(true);
		//slidingMenu.setFadeDegree(0.35f);

  	    if(widthScreen > 2.5) { slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_tablette); }
  	    else { slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_smartphone); }

		ActionBar actionBar = getActionBar();
	  	actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Configuration des items du Menu Sliding.
     */
    public void configurationItemsMenuSliding() 
    {
    	TextView itemAccueil = (TextView) findViewById(R.id.item_accueil);
    	TextView itemInformation = (TextView) findViewById(R.id.item_informations);
    	TextView itemCorrespondant = (TextView) findViewById(R.id.item_correspondants);
    	TextView itemTwitter = (TextView) findViewById(R.id.item_twitter);
    	TextView itemFacebook = (TextView) findViewById(R.id.item_facebook);
    	TextView itemBlogFE = (TextView) findViewById(R.id.item_blog);

  	    // Adapter la taille du texte en fonction de la taille de l'écran.
  	    if(widthScreen > 2.5) { 
  	    	itemAccueil.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemInformation.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
			itemCorrespondant.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
			itemTwitter.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
			itemFacebook.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
			itemBlogFE.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
		} 
  	    else { 
  	    	itemAccueil.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemInformation.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemCorrespondant.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemTwitter.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemFacebook.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemBlogFE.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    }

  		itemAccueil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, Accueil.class);
				// Rappel l'activité 'Accueil' déjà lancée.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

  	    itemInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, Renseignements.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, Correspondants.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, FEXTwitter.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, FEXFacebook.class);
				startActivity(intent);
				finish();
			}
		});

  	  itemBlogFE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Guides.this, Blog.class);
				startActivity(intent);
				finish();
			}
		});
    }

 	/**
 	 * Mise à jour du commentaire de l'utilisateur.
 	 */
    public void majCommentaireToBDD(String _ancienCommentaire, String _nouveauCommentaire) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

		Calendar calendar = Calendar.getInstance(); 
		String dateCommentaire = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
								+ String.valueOf(calendar.get(Calendar.MONTH)) + "-"
								+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " "
								+ String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
								+ String.valueOf(calendar.get(Calendar.MINUTE)) + ":"
								+ String.valueOf(calendar.get(Calendar.SECOND));

	    values.put(UtilisateurProvider.KEY_COMMENTAIRE, _ancienCommentaire + " #" + dateCommentaire + " / " + _nouveauCommentaire);
	    getContentResolver().update(uri, values, null, null);
	}

    /**
 	 * Sauvegarde du commentaire de l'utilisateur.
 	 */
    public void saveCommentaireToBDD(String _commentaire) 
  	{
	    ContentResolver contentResolver = getContentResolver();
    	String whereArgs = UtilisateurProvider.KEY_COMMENTAIRE + " = ' '" + " OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " = '' OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " is null";

	    // Vérification initialisation.
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, whereArgs, null, null);

 		// Sinon Création de la ligne utilisateur.
 		if(cursor.moveToFirst()) {
 			ContentValues values = new ContentValues();
			values.put(UtilisateurProvider.KEY_PAYS, "inconnu");

			Calendar calendar = Calendar.getInstance(); 
			String dateCommentaire = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
									+ String.valueOf(calendar.get(Calendar.MONTH)) + "-"
									+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " "
									+ String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
									+ String.valueOf(calendar.get(Calendar.MINUTE)) + ":"
									+ String.valueOf(calendar.get(Calendar.SECOND));

			values.put(UtilisateurProvider.KEY_COMMENTAIRE, "#" + dateCommentaire + " / " + _commentaire);
			contentResolver.update(UtilisateurProvider.CONTENT_UTILISATEUR_URI, values, null, null);
 		}
 		// Si des commentaires sont déjà enregistrés, on l'ajoute aux commentaires précédents.
 		else {
 			cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
 			cursor.moveToFirst();
			String ancienCommentaire = cursor.getString(UtilisateurProvider.COMMENTAIRE_COLUMN);
 			majCommentaireToBDD(ancienCommentaire, _commentaire);		
 		}
	}

	/**
	 * Méthodes du commentaire / signalement d'un bug de l'utilisateur à enregistrer.
	 */
	@Override
	public void onCommentairePositiveClick(DialogFragment dialog) 
	{
 	   Toast.makeText(getApplication(), "Merci, le commentaire a été envoyé ! ", Toast.LENGTH_LONG).show();
 	   CommentaireDialogFragment cdf = (CommentaireDialogFragment) dialog;
 	   saveCommentaireToBDD(cdf.getCommentaire());
	}

	@Override
	public void onCommentaireNegativeClick(DialogFragment dialog) { }

	@Override
	public void onCommentaireFinishEditDialog(String inputText) { }	
	
}
