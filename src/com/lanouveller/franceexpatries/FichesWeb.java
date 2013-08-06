package com.lanouveller.franceexpatries;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

public class FichesWeb extends SlidingActivity {

	// Retour de l'activité Préférences.
	private static final int SHOW_PREFERENCES = 1;

	private SlidingMenu slidingMenu;

	private String paysUtilisateur;
	private String guideDemande = "";
  	
  	private ShareActionProvider mShareActionProvider;

	private float heightScreen;
	private float widthScreen;

	private WebView wv;

	private String url = "";

    @Override
  	public void onCreate(Bundle savedInstanceState) 
  	{
  		super.onCreate(savedInstanceState);

  		// Initialise la taille du terminal.
  		initScreenSize();

  		// Initialise le pays de l'utilisateur.
  		initPaysFromBDD();

	  	// Récupère les données du serveur.
  		Intent intentInit = this.getIntent();
  		guideDemande = intentInit.getAction();

  		// Initialise le layout du terminal.
  		configurationLayout();

  		processusLoading();

  		// Configuration du menu gauche ( Menu Sliding ).
  		configurationMenuSliding();
  		configurationItemsMenuSliding();
  	}

    /**
     *  Fonction générique.
     */
    public void processusLoading() {
    	// Envoi le guide en ligne.
  		if(isOnline()) {
  	  		url = "http://france-expatries.com/fex_app/guides.php?pays=" + paysUtilisateur + "&guide=" + guideDemande;
  	  		wv.loadUrl(url);
  	  		launchService(guideDemande);
  		}
  		// Envoi le mode hors-ligne.// Vérifie les données en hors-ligne.
  		else if(checkDataHorsLigneFromBDD(guideDemande)) {
    		Intent intentHS = new Intent(this, Fiches.class);
    		intentHS.setAction(guideDemande);
    		startActivity(intentHS);
    		finish();
  		}
  		// Pas de connexion, et ce guide n'est pas disponible hors-ligne. Donc retour au choix des guides.
  		else {
  			Toast.makeText(getApplicationContext(), "Vous êtes déconnecté. Veuillez réessayer ultérieurement", Toast.LENGTH_LONG).show();
    		Intent intentBack = new Intent(this, Guides.class);
			// Rappel l'activité 'Guides' déjà lancée.
    		intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intentBack);
    		finish();
  		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	// Insertion du menu overflow.
    	getMenuInflater().inflate(R.menu.menu_overflow_fiches_web, menu);

    	// Icône Partage - Récupère le MenuItem avec ShareActionProvider.
      	mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.ab_item_share).getActionProvider();

      	sharing();

      	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId()) {
			// Items du 'Menu overflow'.
			case android.R.id.home: {
				slidingMenu.showMenu();
			} return true;

			case R.id.item_preferences: {
				Intent intent = new Intent(this, Preferences.class);
				intent.putExtra("CALLING_CLASS", "FichesWeb");
	    		startActivityForResult(intent, SHOW_PREFERENCES);
			} return true;

			case R.id.item_go_hors_ligne: {
				if(checkDataHorsLigneFromBDD(guideDemande)) {
		    		Intent intentHS = new Intent(this, Fiches.class);
		    		intentHS.setAction(guideDemande);
		    		startActivity(intentHS);
		    		finish();
		  		}
		  		// Pas de connexion, et ce guide n'est pas disponible hors-ligne. Donc retour au choix des guides.
		  		else {
		  			Toast.makeText(getApplicationContext(), "Vous êtes déconnecté. Veuillez réessayer ultérieurement", Toast.LENGTH_LONG).show();
		    		Intent intentBack = new Intent(this, Guides.class);
					// Rappel l'activité 'Guides' déjà lancée.
		    		intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    		startActivity(intentBack);
		    		finish();
		  		}
    		} return true;

			case R.id.item_refresh: {
				processusLoading();
    		} return true;
    	}

    	return false;
    }

  	/**
  	 * Initialisation du pays.
  	 */
  	public void initPaysFromBDD() 
  	{
  		ContentResolver contentResolver = getContentResolver();
  		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

  		while(cursor.moveToNext()) {
  			paysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
  		}
  	}

    /**
     * Configuration du layout par défaut.
     */
    public void configurationLayout() 
    {
    	setContentView(R.layout.fiches_web);

  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
    	/**ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);*/

    	wv = (WebView) findViewById(R.id.fiches_webview);

    	String guideDemandeTitre = "";
  		if(guideDemande.equalsIgnoreCase("argent")) 				{ guideDemandeTitre = "Argent et Fiscalité"; }
  		else if(guideDemande.equalsIgnoreCase("emploi")) 			{ guideDemandeTitre = "Emploi et Contrat de Travail"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Passeport et Visa"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Santé et Protection Sociale"; }
  		else if(guideDemande.equalsIgnoreCase("transport")) 		{ guideDemandeTitre = "Transport"; }
  		else if(guideDemande.equalsIgnoreCase("vie_culturelle")) 	{ guideDemandeTitre = "Vie Culturelle"; }
  		else if(guideDemande.equalsIgnoreCase("vie_quotidienne")) 	{ guideDemandeTitre = "Vie Quotidienne"; }

  		setTitle("Guides - " + paysUtilisateur + " - Guide : " + guideDemandeTitre);
    }

    public boolean isOnline() 
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
        	return true;
        }

        return false;
    }

    /**
     * Lance le service avec le guide demandé.
     */
  	private void launchService(String nomGuide) 
  	{
  		Intent intent = new Intent(this, FicheService.class);
  		intent.setAction(nomGuide);
  		startService(intent);
  	}

    /**
     * Fonction gérant le partage.
     */
    private void sharing() 
    {
        // This line chooses a custom shared history xml file. Omit the line if using the default share history file is desired.
        //mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");

    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType("text/plain");
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

    	String continent = getContinent(paysUtilisateur);

  		String guideUrl = "";
  		if(guideDemande.equalsIgnoreCase("argent")) 				{ guideUrl = "argent-et-fiscalite"; }
  		else if(guideDemande.equalsIgnoreCase("emploi")) 			{ guideUrl = "emploi-et-contrat-de-travail"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideUrl = "passeport-et-visa"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideUrl = "sante-et-protection-sociale"; }
  		else if(guideDemande.equalsIgnoreCase("transport")) 		{ guideUrl = "transport"; }
  		else if(guideDemande.equalsIgnoreCase("vie_culturelle")) 	{ guideUrl = "vie-culturelle"; }
  		else if(guideDemande.equalsIgnoreCase("vie_quotidienne")) 	{ guideUrl = "vie-quotidienne"; }

  		String guideDemandeTitre = "";
  		if(guideDemande.equalsIgnoreCase("argent")) 				{ guideDemandeTitre = "Argent et Fiscalité"; }
  		else if(guideDemande.equalsIgnoreCase("emploi")) 			{ guideDemandeTitre = "Emploi et Contrat de Travail"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Passeport et Visa"; }
  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Santé et Protection Sociale"; }
  		else if(guideDemande.equalsIgnoreCase("transport")) 		{ guideDemandeTitre = "Transport"; }
  		else if(guideDemande.equalsIgnoreCase("vie_culturelle")) 	{ guideDemandeTitre = "Vie Culturelle"; }
  		else if(guideDemande.equalsIgnoreCase("vie_quotidienne")) 	{ guideDemandeTitre = "Vie Quotidienne"; }

    	String url = "http://france-expatries.com/" + continent + "/" + paysUtilisateur + "/" + paysUtilisateur + "-" + guideUrl;

    	// Add data to the intent, the receiving app will decide what to do with it.
    	intent.putExtra(Intent.EXTRA_SUBJECT, "France-Expatriés : " + paysUtilisateur + " - Guide " + guideDemandeTitre);
    	intent.putExtra(Intent.EXTRA_TEXT, "France-Expatriés - Portail privé de l'expatriation : " + paysUtilisateur + " - Guide " + guideDemandeTitre + ". " + url);

    	// Lancement du partage via 'mini menu apps'.
    	/**startActivity(Intent.createChooser(intent, "Continuer avec"));*/
    	// Lancement du partage via 'dialog menu apps'.
        mShareActionProvider.setShareIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
    	super.onActivityResult(requestCode, resultCode, intent);

    	if(requestCode == SHOW_PREFERENCES && resultCode == Activity.RESULT_OK) {
    		Intent intentBack = new Intent(this, Guides.class);
    		startActivity(intentBack);
    		finish();
    	}
    }

	private String getContinent(String nomPays) 
	{
		String[] paysAfrique = getResources().getStringArray(R.array.pays_disponibles_afrique);
		String[] paysAmerique = getResources().getStringArray(R.array.pays_disponibles_amerique);
		String[] paysAsie = getResources().getStringArray(R.array.pays_disponibles_asie);
		String[] paysEurope = getResources().getStringArray(R.array.pays_disponibles_europe);
		String[] paysOceanie = getResources().getStringArray(R.array.pays_disponibles_oceanie);

		for(String pays : paysAfrique) {
			if(nomPays.equalsIgnoreCase(pays)) return "Afrique";
		}
		for(String pays : paysAmerique) {
			if(nomPays.equalsIgnoreCase(pays)) return "Amerique";
		}
		for(String pays : paysAsie) {
			if(nomPays.equalsIgnoreCase(pays)) return "Asie";
		}
		for(String pays : paysEurope) {
			if(nomPays.equalsIgnoreCase(pays)) return "Europe";
		}
		for(String pays : paysOceanie) {
			if(nomPays.equalsIgnoreCase(pays)) return "Oceanie";
		}

		return "";
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
    	TextView itemAccueil = 			(TextView) findViewById(R.id.item_accueil);
    	TextView itemInformation = 		(TextView) findViewById(R.id.item_informations);
    	TextView itemCorrespondant = 	(TextView) findViewById(R.id.item_correspondants);
    	TextView itemTwitter = 			(TextView) findViewById(R.id.item_twitter);
    	TextView itemFacebook = 		(TextView) findViewById(R.id.item_facebook);
    	TextView itemBlogFE = 			(TextView) findViewById(R.id.item_blog);

    	// Configuration des Items :

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
				Intent intent = new Intent(FichesWeb.this, Accueil.class);
				// Rappel l'activité 'Accueil' déjà lancée.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

  	    itemInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(FichesWeb.this, Renseignements.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(FichesWeb.this, Correspondants.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(FichesWeb.this, FEXTwitter.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(FichesWeb.this, FEXFacebook.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemBlogFE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(FichesWeb.this, Blog.class);
				startActivity(intent);
				finish();
			}
		});
    }

    /**
     * Mesure la taille de l'écran.
     */
    public void initScreenSize() 
    {
  	    // Tablette : Hauteur: 6, largeur: 4
  	    // Smartphone : Hauteur: 3.5, largeur: 2

    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;
    }

	public boolean checkDataHorsLigneFromBDD(String _guideDemande) 
	{
		try {
			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDES_HS_COLUMN).equalsIgnoreCase("guides_disponibles_hs")) {
	  				if(_guideDemande.equalsIgnoreCase("argent") 
	  				&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_ARGENT_HS_COLUMN).equalsIgnoreCase("guide_argent_disponible_hs")) {
	  		  			return true;
	  				}
	  	  			if(_guideDemande.equalsIgnoreCase("emploi") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_EMPLOI_HS_COLUMN).equalsIgnoreCase("guide_emploi_disponible_hs")) {
	  	  				return true;
	  	  			}
	  	  			if(_guideDemande.equalsIgnoreCase("passeport") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_PASSEPORT_HS_COLUMN).equalsIgnoreCase("guide_passeport_disponible_hs")) {
	  	  				return true;
	  	  			}
	  	  			if(_guideDemande.equalsIgnoreCase("sante") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_SANTE_HS_COLUMN).equalsIgnoreCase("guide_sante_disponible_hs")) {
	  	  				return true;
	  	  			}
	  	  			if(_guideDemande.equalsIgnoreCase("transport") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_TRANSPORT_HS_COLUMN).equalsIgnoreCase("guide_transport_disponible_hs")) {
	  	  				return true;
	  	  			}
	  	  			if(_guideDemande.equalsIgnoreCase("vie_culturelle") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_CULTURELLE_HS_COLUMN).equalsIgnoreCase("guide_vie_culturelle_disponible_hs")) {
	  	  				return true;
	  	  			}
	  	  			if(_guideDemande.equalsIgnoreCase("vie_quotidienne") 
	  	    		&& cursor.getString(UtilisateurProvider.DISPO_GUIDE_VIE_QUOTIDIENNE_HS_COLUMN).equalsIgnoreCase("guide_vie_quotidienne_disponible_hs")) {
	  	  				return true;
	  	  			}
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

}
