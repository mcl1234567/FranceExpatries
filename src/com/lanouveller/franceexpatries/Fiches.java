package com.lanouveller.franceexpatries;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.lanouveller.franceexpatries.CommentaireDialogFragment.CommentaireDialogListener;

public class Fiches extends SlidingActivity implements CommentaireDialogListener {

		// Retour de l'activité Préférences.
	  	private static final int SHOW_PREFERENCES = 1;

	  	private SlidingMenu slidingMenu;

	  	// Gestion des fiches des guides du pays.
		private ArrayList<Fiche> arraylistGuide;
		private FichesAdapter adapterGuide;

	  	private String nomPaysUtilisateur = "";
	  	private String guideDemande = "";
	  	
	  	private ShareActionProvider mShareActionProvider;

	  	private float heightScreen;
	  	private float widthScreen;

	    @Override
	  	public void onCreate(Bundle savedInstanceState) 
	  	{
	  		super.onCreate(savedInstanceState);

	  		// Récupère la taille dedu terminal.
	  		initScreenSize();

	  		// Initialise le pays de l'utilisateur.
	  		initPaysFromBDD();

		  	// Récupère les données du serveur.
	  		Intent intent = this.getIntent();
	  		guideDemande = intent.getAction();

	  		// Initialise le layout du terminal.
	  		configurationLayout();

	  		// Configuration du menu gauche ( Menu Sliding ).
	  		configurationMenuSliding();
	  		configurationItemsMenuSliding();

  		  	// Transfert les données hors-ligne vers le Layout.
	  		initGuideFromBDD(nomPaysUtilisateur, guideDemande);

	  		Toast.makeText(getApplicationContext(), "Vous êtes actuellement en mode hors-ligne.", Toast.LENGTH_LONG).show();
	  	}

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) 
	    {
	    	// Insertion du menu overflow.
	    	getMenuInflater().inflate(R.menu.menu_overflow_fiches_hors_ligne, menu);

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

				case R.id.item_go_online: {
		    		if(isOnline()) {
		    			Intent intent = new Intent(this, FichesWeb.class);
			    		intent.setAction(guideDemande);
			    		startActivity(intent);
			    		finish();
		    		}
		    		else {
		    	  		Toast.makeText(getApplicationContext(), "Vous êtes déconnecté. Veuillez vous reconnecter auparavant.", Toast.LENGTH_LONG).show();
		    		}
	    		} return true;
	    	}

	    	return false;
	    }

	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	    {
	    	super.onActivityResult(requestCode, resultCode, intent);

	    	if(requestCode == SHOW_PREFERENCES && resultCode == Activity.RESULT_OK) {
	    		// Relance des guides pour le pays mis à jour.
	    		Intent intentBack = new Intent(this, Guides.class);
	    		startActivity(intentBack);
	    		// Fin de l'activité 'guide'.
	    		finish();
	    	}
	    }

	  	/**
	  	 * Initialisation du pays.
	  	 */
	  	public void initPaysFromBDD() {
	  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			nomPaysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
	  		}
	  	}

	  	/**
	  	 * Ajout d'une fiche.
	  	 */
		public void ajoutFiches(Fiche _fiche) 
		{
			arraylistGuide.add(_fiche);
			adapterGuide.notifyDataSetChanged();
		}

		/**
		 * Chargement des fiches du pays via la BDD.
		 */
		public void initGuideFromBDD(String _pays, String _guide) 
		{
			arraylistGuide.clear();

	    	String where = FicheProvider.KEY_PAYS + " = " + '"' + _pays + '"' + " AND " +
	    					FicheProvider.KEY_GUIDE + " = " + '"' + _guide + '"';

			Cursor cursor = getContentResolver().query(FicheProvider.CONTENT_FICHE_URI, null, where, null, null);

			while(cursor.moveToNext()) {
				String titre = cursor.getString(FicheProvider.TITRE_COLUMN);
				String contenu = cursor.getString(FicheProvider.CONTENU_COLUMN);

				// Lancement du mode hors-ligne du guide.
				if(titre.equalsIgnoreCase("") || titre.equalsIgnoreCase(" ") && contenu.equalsIgnoreCase("") || contenu.equalsIgnoreCase(" ")) {
					Intent intent = new Intent(this, FichesWeb.class);
					startActivity(intent);
					finish();
				}
				// Ligne valide et complète insérée.
				else {
					Fiche fiche = new Fiche(nomPaysUtilisateur, guideDemande, titre, contenu);
					ajoutFiches(fiche);
				}
			}
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

	    	String continent = getContinent(nomPaysUtilisateur);

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

	    	String url = "http://france-expatries.com/" + continent + "/" + nomPaysUtilisateur + "/" + nomPaysUtilisateur + "-" + guideUrl;

	    	// Add data to the intent, the receiving app will decide what to do with it.
	    	intent.putExtra(Intent.EXTRA_SUBJECT, "France-Expatriés: " + nomPaysUtilisateur + " - Guide " + guideDemandeTitre);
	    	intent.putExtra(Intent.EXTRA_TEXT, "France-Expatriés. " + nomPaysUtilisateur + " - Guide " + guideDemandeTitre + "." + url);

	    	// Lancement du partage via 'mini menu apps'.
	    	/**startActivity(Intent.createChooser(intent, "Continuer avec"));*/
	    	// Lancement du partage via 'dialog menu apps'.
	        mShareActionProvider.setShareIntent(intent);
	    }

		private String getContinent(String nomPays) {
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

	    public boolean isOnline() {
	        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

	        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
	        	return true;
	        }

	        return false;
	    }

	    /**
	     * Configuration du layout par défaut.
	     */
	    public void configurationLayout() 
	    {
	  		setContentView(R.layout.fiches_fragment);

	  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
		    /**ActionBar actionBar = getActionBar();
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setDisplayShowHomeEnabled(false);*/

	  		String guideDemandeTitre = "";
	  		if(guideDemande.equalsIgnoreCase("argent")) 				{ guideDemandeTitre = "Argent et Fiscalité"; }
	  		else if(guideDemande.equalsIgnoreCase("emploi")) 			{ guideDemandeTitre = "Emploi et Contrat de Travail"; }
	  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Passeport et Visa"; }
	  		else if(guideDemande.equalsIgnoreCase("passeport")) 		{ guideDemandeTitre = "Santé et Protection Sociale"; }
	  		else if(guideDemande.equalsIgnoreCase("transport")) 		{ guideDemandeTitre = "Transport"; }
	  		else if(guideDemande.equalsIgnoreCase("vie_culturelle")) 	{ guideDemandeTitre = "Vie Culturelle"; }
	  		else if(guideDemande.equalsIgnoreCase("vie_quotidienne")) 	{ guideDemandeTitre = "Vie Quotidienne"; }

	  		setTitle("Guides - " + nomPaysUtilisateur + " - Guide : " + guideDemandeTitre);

	  		// Configuration de la ListView de fiches.
	    	FragmentManager fragmentManager = getFragmentManager();
	    	FicheFragment ficheFragment = (FicheFragment) fragmentManager.findFragmentById(R.id.fiches_listview);
	    	arraylistGuide = new ArrayList<Fiche>();
	  		adapterGuide = new FichesAdapter(this, R.layout.fiches_listitems, arraylistGuide);
	  		ficheFragment.setListAdapter(adapterGuide);
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
					Intent intent = new Intent(Fiches.this, Accueil.class);
					// Rappel l'activité 'Accueil' déjà lancée.
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			});

	  	    itemInformation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Fiches.this, Renseignements.class);
					startActivity(intent);
					finish();
				}
			});

	  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Fiches.this, Correspondants.class);
					startActivity(intent);
					finish();
				}
			});

	  	    itemTwitter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Fiches.this, FEXTwitter.class);
					startActivity(intent);
					finish();
				}
			});

	  	    itemFacebook.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Fiches.this, FEXFacebook.class);
					startActivity(intent);
					finish();
				}
			});

	  	    itemBlogFE.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Fiches.this, Blog.class);
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

	    /**
	 	 * Sauvegarde du commentaire / critique de l'utilisateur.
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
	 			//contentResolver = getContentResolver();
	 			cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
	 			cursor.moveToFirst();
				String ancienCommentaire = cursor.getString(UtilisateurProvider.COMMENTAIRE_COLUMN);
	 			majCommentaireToBDD(ancienCommentaire, _commentaire);		
	 		}
		}

	 	/**
	 	 * Mise à jour des commentaires / critiques de l'utilisateur.
	 	 */
	    public void majCommentaireToBDD(String _ancienCommentaire, String _nouveauCommentaire) 
	  	{
	    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		    ContentResolver contentResolver = getContentResolver();
			ContentValues values = new ContentValues();

			Calendar calendar = Calendar.getInstance(); 
			String dateCommentaire = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
									+ String.valueOf(calendar.get(Calendar.MONTH)) + "-"
									+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " "
									+ String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
									+ String.valueOf(calendar.get(Calendar.MINUTE)) + ":"
									+ String.valueOf(calendar.get(Calendar.SECOND));

		    values.put(UtilisateurProvider.KEY_COMMENTAIRE, _ancienCommentaire + " #" + dateCommentaire + " / " + _nouveauCommentaire);
			contentResolver.update(uri, values, null, null);
		}

		/**
		 * Méthodes du commentaire / signalement d'un bug de l'utilisateur à enregistrer.
		 */
		@Override
		public void onCommentairePositiveClick(DialogFragment dialog) {
			Toast.makeText(getApplication(), "Merci, le commentaire a été envoyé ! ", Toast.LENGTH_LONG).show();
	 	    CommentaireDialogFragment cdf = (CommentaireDialogFragment) dialog;
	 	    saveCommentaireToBDD(cdf.getCommentaire());
		}

		@Override
		public void onCommentaireNegativeClick(DialogFragment dialog) { }

		@Override
		public void onCommentaireFinishEditDialog(String inputText) { }

}
