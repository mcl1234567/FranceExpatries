package com.lanouveller.franceexpatries;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ScrollView;
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
public class Renseignements extends SlidingActivity implements CommentaireDialogListener {

	// Constantes - Retour de l'activité Préférences.
  	private static final int SHOW_PREFERENCES = 1;

  	private SlidingMenu slidingMenu;

  	private String nomPaysUtilisateur;
  	private Pays paysObjUtilisateur;

  	private InformationReceiver receiver;

  	private String noCorrespondantValue = "";

  	// Gestion [refresh].
	private Menu optionsMenu;

	// Titre de l'activité.
	private TextView titreInformationsTv;

  	// Configuration multi-écrans.
  	private float heightScreen;
  	private float widthScreen;

  	// Taille du nom pays. ( tablet )
  	private float tailleTitreTexteT = 35;
  	// Taille du nom pays. ( smartphone )
  	private float tailleTitreTexteSP = 22;

  	/**
  	 * Gestion du service.
  	 */
  	public class InformationReceiver extends BroadcastReceiver {
  		public static final String INFORMATION_REFRESHED = "com.lanouveller.franceexpatries.INFORMATION_REFRESHED";

  		@Override
    	public void onReceive(Context context, Intent intent) 
    	{
  			// Chargement des données.
  			initInformationFromBDD(nomPaysUtilisateur);

  			// Chargement du layout.
			launchNewLayout();

			// Désactive la ProgressBar.
      		setRefreshActionButtonState(false);
    	}
  	}

    @Override
  	public void onCreate(Bundle savedInstanceState) 
  	{
  		super.onCreate(savedInstanceState);

  		// Récupère les dimensions de l'écran du terminal.
  		initScreenSize();

  		// Récupère le pays de l'expatrié.
  		initPaysFromBDD();

  		if(checkDataFromBDD()) {
  	  		// Configure le Menu Sliding.
  	  		configurationMenuSliding();
  	  		configurationItemsMenuSliding();

  	  		// Configure le layout.
  	  		configurationLayout();

  	  		// Récupère les données du serveur.
  	  		//launchService();

  		  	// Transfert les données hors-ligne vers le Layout.
  		  	initInformationFromBDD(nomPaysUtilisateur);

  			// Chargement du layout.
			launchNewLayout();
  		}
  		// Aucune donnée pour ce pays.
  		else {
  	  		// Configure le Menu Sliding.
  	  		configurationMenuSliding();
  	  		configurationItemsMenuSliding();

  	  		noInformation();
  			noCorrespondantValue = "aucun";
  		}
  	}

    @Override
    public void onResume() 
    {
    	receiver = new InformationReceiver();
    	registerReceiver(receiver, new IntentFilter(RenseignementService.INFORMATION_REFRESHED));

    	super.onResume();
    }

    @Override
    public void onPause() 
    {
    	unregisterReceiver(receiver);
    	super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
  	    this.optionsMenu = menu;

    	/**if(checkDataFromBDD()) {
    		// Insertion du 'Menu overflow'.
        	getMenuInflater().inflate(R.menu.menu_overflow_informations, menu);

        	// Icône Partage - Récupère le MenuItem avec ShareActionProvider. Intégré dans le menu overflow.
        	//mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
          	//sharing();
    	}
    	else {
    		// Insertion du 'Menu overflow' adapté. Unable to share if no data !
        	getMenuInflater().inflate(R.menu.menu_overflow_informations_no_data, menu);
    	}*/

      	return super.onCreateOptionsMenu(menu);
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	   if(checkDataFromBDD()) {
	   		// Insertion du 'Menu overflow'.
	       	getMenuInflater().inflate(R.menu.menu_overflow_informations, menu);
	
	       	// Icône Partage - Récupère le MenuItem avec ShareActionProvider. Intégré dans le menu overflow.
	       	/**mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
	         	sharing();*/
	   	}
	   	else {
	   		// Insertion du 'Menu overflow' adapté. Unable to share if no data !
	       	getMenuInflater().inflate(R.menu.menu_overflow_informations_no_data, menu);
	   	}

	   return super.onPrepareOptionsMenu(menu);
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

			case R.id.item_share: {
				// Sharing dialog.
				if(checkDataFromBDD()) {
					if(isOnline()) {
						startActivity(Intent.createChooser(sharing(), "Continuer avec"));
					} else {
						Toast.makeText(getApplicationContext(), "Échec de l'actualisation : veuillez vérifier votre connexion réseau.", Toast.LENGTH_LONG).show();
					}
		  		}
    		} return true;

			case R.id.item_preferences: {
				Intent intent = new Intent(this, Preferences.class);
		  		intent.putExtra("CALLING_CLASS", "Informations générales");
	    		startActivityForResult(intent, SHOW_PREFERENCES);
			} return true;

			// Inactif car les messages utilisateur doivent être lus ultérieurement sur une plateforme non développé.
			/**case R.id.item_commentaire: {
    			DialogFragment dialog = new CommentaireDialogFragment();
    			dialog.show(getFragmentManager(), "CommentaireDialogFragment");
    		} return true;*/

    		// Menu overflow : Informations.
			case R.id.ab_item_refresh: {
				if(checkDataFromBDD()) {
					if(isOnline()) {
						// Active la ProgressBar.
		    			setRefreshActionButtonState(true);
	
						// Récupère les données du serveur.
			  	  		launchService();
					} else {
						Toast.makeText(getApplicationContext(), "Échec de l'actualisation : veuillez vérifier votre connexion réseau.", Toast.LENGTH_LONG).show();
					}
		  		}
    		} return true;
    	}

    	return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);

    	if(requestCode == SHOW_PREFERENCES && resultCode == Activity.RESULT_OK) {
  			// Active la ProgressBar.
  			setRefreshActionButtonState(true);

    		// Chargement du pays à jour.
    		initPaysFromBDD();

      		// Configuration de la navigation.
    		setTitle("Informations - " + nomPaysUtilisateur);

      		// Configuration du titre.
        	//titreInformationsTv.setText("Informations générales");

      		invalidateOptionsMenu();

    		// Recharge les données et le layout.
    		if(checkDataFromBDD() && noCorrespondantValue.equalsIgnoreCase("aucun")) {
    			configurationLayout();

    			// MAJ DATA.
        		//launchService();

      			// Chargement des données.
      			initInformationFromBDD(nomPaysUtilisateur);

      			// Chargement du layout.
    			launchNewLayout();
    		}
    		// Recharge uniquement les données.
    		else if(checkDataFromBDD()) {
    			// MAJ DATA.
        		//launchService();

      			// Chargement des données.
      			initInformationFromBDD(nomPaysUtilisateur);

      			// Chargement du layout.
    			launchNewLayout();
    			
    		}
    		else {
    			noInformation();
      			noCorrespondantValue = "aucun";
    		}

    		// Désactive la ProgressBar.
        	setRefreshActionButtonState(false);
    	}
    }

    /**
     * Vérifie la disponibilité des données du pays.
     */
	public boolean checkDataFromBDD() 
	{
  		try {
	  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
	
	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_INFOS_COLUMN).equalsIgnoreCase("information_disponible")) {
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

  	private void launchService() 
  	{
  		if(isOnline()) {
  	  		Intent intent = new Intent(this, RenseignementService.class);
  	  		intent.setAction(getPaysFormate(nomPaysUtilisateur));
  			startService(new Intent(intent));
  		} 
  		else {
  			Toast.makeText(getApplicationContext(), "Mode hors-ligne activé.", Toast.LENGTH_LONG).show();
  		}
  	}

  	public void initPaysFromBDD() 
  	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

  		while(cursor.moveToNext()) {
  			nomPaysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
  		}
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
	 * Layout dynamique des Informations générales du pays.
	 */
	public void launchNewLayout() 
	{
		ScrollView sV = new ScrollView(this);
		sV.setBackgroundColor(Color.WHITE);

		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		LinearLayout mainLl = new LinearLayout(this);
		mainLl.setBackgroundColor(Color.WHITE);
		mainLl.setOrientation(LinearLayout.VERTICAL);
		mainLl.setLayoutParams(llp);
		sV.addView(mainLl);

		// TextView : Titre de l'activité.
		titreInformationsTv = new TextView(this);
    	titreInformationsTv.setTextColor(getResources().getColor(R.color.orange));
    	titreInformationsTv.setText("Informations générales");
		titreInformationsTv.setGravity(Gravity.CENTER_HORIZONTAL);
		titreInformationsTv.setLayoutParams(llp);
		if(widthScreen > 2.5) { titreInformationsTv.setTextAppearance(this, R.style.titreInformationTablette); }
		else { titreInformationsTv.setTextAppearance(this, R.style.titreInformationSmartphone); }
		mainLl.addView(titreInformationsTv);

		// Layout : Nom du pays.
		LinearLayout titreLl = new LinearLayout(this);
		titreLl.setOrientation(LinearLayout.HORIZONTAL);
		titreLl.setGravity(Gravity.CENTER_HORIZONTAL);
		titreLl.setLayoutParams(llp);
		mainLl.addView(titreLl);

		// Traitement des contenus et libellés indépendemment.
		ArrayList<String> infos = new ArrayList<String>();
		ArrayList<String> libelleInfos = new ArrayList<String>();

		infos.add(paysObjUtilisateur.getNomPays());
		infos.add(paysObjUtilisateur.getMonnaie());
		infos.add(paysObjUtilisateur.getFormeEtat());
		infos.add(paysObjUtilisateur.getPresidentRepublique());
		infos.add(paysObjUtilisateur.getPresidentGouvernement());
		infos.add(paysObjUtilisateur.getPremierMinistre());
		infos.add(paysObjUtilisateur.getGouvernement());
		infos.add(paysObjUtilisateur.getClimat());
		infos.add(paysObjUtilisateur.getSuperficie());
		infos.add(paysObjUtilisateur.getDensite());
		infos.add(paysObjUtilisateur.getReligion());
		infos.add(paysObjUtilisateur.getPIB());
		infos.add(paysObjUtilisateur.getPopulation());
		infos.add(paysObjUtilisateur.getNombreExpatries());
		infos.add(paysObjUtilisateur.getTauxChomage());
		infos.add(paysObjUtilisateur.getIndicatifTel());
		infos.add(paysObjUtilisateur.getFuseauHoraire());

		libelleInfos.add("Monnaie");
		libelleInfos.add("Forme de l'État");
		libelleInfos.add("Président de la République");
		libelleInfos.add("Président du gouvernement");
		libelleInfos.add("Premier ministre");
		libelleInfos.add("Gouvernement");
		libelleInfos.add("Climat");
		libelleInfos.add("Superficie");
		libelleInfos.add("Densité");
		libelleInfos.add("Religion");
		libelleInfos.add("PIB");
		libelleInfos.add("Population");
		libelleInfos.add("Nombre d'expatriés");
		libelleInfos.add("Taux de chômage");
		libelleInfos.add("Indicatif téléphonique");
		libelleInfos.add("Fuseau Horaire");

		// Configuration des libellés du layout.
		LinearLayout.LayoutParams nomPaysParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		nomPaysParams.setMargins(10, 10, 0, 10);

		LinearLayout.LayoutParams libelleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		libelleParams.setMargins(10, 10, 0, 0);

		LinearLayout.LayoutParams contenuParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		contenuParams.setMargins(10, 0, 10, 10);
		contenuParams.gravity = Gravity.RIGHT;

		// Filtrage et Ajout des contenus au layout.
		for(int i=0, j=0; i<infos.size(); i++) {
			// Contenu non valide.
			if(infos.get(i).equalsIgnoreCase("") || infos.get(i).equalsIgnoreCase(" ")) { j++; }
			// Affichage du contenu 'nom pays'.
			else if(i == 0) {
				TextView nomTv = new TextView(this);
				nomTv.setLayoutParams(nomPaysParams);
				nomTv.setTextColor(Color.BLACK);
				nomTv.setText(infos.get(i));
				if(widthScreen > 2.5) { nomTv.setTextSize(tailleTitreTexteT); }
				else { nomTv.setTextSize(tailleTitreTexteSP); }
				titreLl.addView(nomTv);
			}
			// Affichage des éléments, (l'ordre des addView est important).
			else {
				// Affichage des Libellés.
				if(i > 0) {
					TextView ltv = new TextView(this);
					ltv.setLayoutParams(libelleParams);
					ltv.setTextColor(getResources().getColor(R.color.gray));
					ltv.setText(libelleInfos.get(j));
					if(widthScreen > 2.5) { ltv.setTextAppearance(this, R.style.libellesInformationTablette); } 
					else { ltv.setTextAppearance(this, R.style.libellesInformationSmartphone); }

					mainLl.addView(ltv);

					// MAJ de l'id libellé.
					j++;
				}
				// i = 0, correspond au contenu 'nom du pays' sans libellé.
				else {
					j = 0;
				}

				// Affichage des Contenus.
				TextView tv = new TextView(this);		
				tv.setLayoutParams(contenuParams);
				tv.setText(infos.get(i));
				if(widthScreen > 2.5) { tv.setTextAppearance(this, R.style.contenuInformationsTablette); } 
				else { tv.setTextAppearance(this, R.style.contenuInformationsSmartphone); }

				mainLl.addView(tv);
			}
		}

		setContentView(sV);
	}

	public void initInformationFromBDD(String _nomPays) 
	{
    	String where = RenseignementProvider.KEY_NOM + " = '" + _nomPays + "'";

		Cursor cursor = getContentResolver().query(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, null, where, null, null);

		if(cursor.moveToFirst()) {
			do {
				String nomPays = 				cursor.getString(RenseignementProvider.NOM_COLUMN);
				String monnaie = 				cursor.getString(RenseignementProvider.MONNAIE_COLUMN);
				String population = 			cursor.getString(RenseignementProvider.POPULATION_COLUMN);
				String formeEtat = 				cursor.getString(RenseignementProvider.FORMEETAT_COLUMN);
				String roi = 					cursor.getString(RenseignementProvider.ROI_COLUMN);
				String presidentGouvernement = 	cursor.getString(RenseignementProvider.PRESIDENT_GOUVERNEMENT_COLUMN);
				String langue = 				cursor.getString(RenseignementProvider.LANGUE_COLUMN);
				String capitale = 				cursor.getString(RenseignementProvider.CAPITALE_COLUMN);
				String gouvernement = 			cursor.getString(RenseignementProvider.GOUVERNEMENT_COLUMN);
				String premierMinistre = 		cursor.getString(RenseignementProvider.PREMIER_MINISTRE_COLUMN);
				String presidentRepublique = 	cursor.getString(RenseignementProvider.PRESIDENT_REPUBLIQUE_COLUMN);
				String climat = 				cursor.getString(RenseignementProvider.CLIMAT_COLUMN);
				String superficie = 			cursor.getString(RenseignementProvider.SUPERFICIE_COLUMN);
				String densite = 				cursor.getString(RenseignementProvider.DENSITE_COLUMN);
				String religion = 				cursor.getString(RenseignementProvider.RELIGION_COLUMN);
				String pib = 					cursor.getString(RenseignementProvider.PIB_COLUMN);
				String nombreExpatries = 		cursor.getString(RenseignementProvider.NOMBRE_EXPATRIES_COLUMN);
				String tauxChomage = 			cursor.getString(RenseignementProvider.TAUX_CHOMAGE_COLUMN);
				String indicatifTel = 			cursor.getString(RenseignementProvider.INDICATIFT_TEL_COLUMN);
				String fuseauHoraire = 			cursor.getString(RenseignementProvider.FUSEAU_HORAIRE_COLUMN);

				paysObjUtilisateur = new Pays(nomPays, monnaie, population, formeEtat, roi, presidentGouvernement, langue, capitale, gouvernement, premierMinistre, 
											presidentRepublique, climat, superficie, densite, religion, pib, nombreExpatries, tauxChomage, indicatifTel, fuseauHoraire);

			} while(cursor.moveToNext());
		}
    }

  	public void setRefreshActionButtonState(final boolean _refreshing) 
  	{
  	    if(optionsMenu != null) {
  	        final MenuItem refreshItem = optionsMenu.findItem(R.id.item_refresh);

  	        if(refreshItem != null) {
  	            if(_refreshing) { refreshItem.setActionView(R.layout.indeterminate); }
  	            else { refreshItem.setActionView(null); }
  	        }
  	    }
  	}

    /**
     * Fonction gérant le partage.
     */
    private Intent sharing() 
    {
        // This line chooses a custom shared history xml file. Omit the line if using the default share history file is desired.
        //mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");

    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType("text/plain");
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

    	String url = "http://france-expatries.com/" + getContinent(nomPaysUtilisateur) + "/" + getPaysFormate(nomPaysUtilisateur);

    	// Add data to the intent, the receiving app will decide what to do with it.
    	intent.putExtra(Intent.EXTRA_SUBJECT, "France-Expatriés: " + nomPaysUtilisateur + " - Informations générales");
    	intent.putExtra(Intent.EXTRA_TEXT, "France-Expatriés. " + nomPaysUtilisateur + " - Informations générales. " + url);

    	// Lancement du partage via 'dialog menu apps'.
    	/**mShareActionProvider.setShareIntent(intent);*/

    	return intent;
    }

  	/**
  	 * Retourne le nom formaté.
	*  Les accents et les espaces sont non autorisés dans l'url de requête HTTP.
  	 */
	private String getPaysFormate(String _nomPays) 
	{
		String nomPaysFormate = _nomPays;

		if(_nomPays.equalsIgnoreCase("Algérie")) {
			nomPaysFormate = "Algerie";
		}
		else if(_nomPays.equalsIgnoreCase("Bénin")) {
			nomPaysFormate = "Benin";
		}
		else if(_nomPays.equalsIgnoreCase("Brésil")) {
			nomPaysFormate = "Bresil";
		}
		// Très bientôt disponible.
		else if(_nomPays.equalsIgnoreCase("Corée du Nord")) {
			nomPaysFormate = "Coree-du-nord";
		}
		else if(_nomPays.equalsIgnoreCase("Corée du Sud")) {
			nomPaysFormate = "Coree-du-sud";
		}
		else if(_nomPays.equalsIgnoreCase("Costa Rica")) {
			nomPaysFormate = "Costa-Rica";
		}
		else if(_nomPays.equalsIgnoreCase("Grèce")) {
			nomPaysFormate = "Grece";
		}
		else if(_nomPays.equalsIgnoreCase("Indonésie")) {
			nomPaysFormate = "Indonesie";
		}
		else if(_nomPays.equalsIgnoreCase("Népal")) {
			nomPaysFormate = "Nepal";
		}
		else if(_nomPays.equalsIgnoreCase("Norvège")) {
			nomPaysFormate = "Norvege";
		}
		else if(_nomPays.equalsIgnoreCase("Israël")) {
			nomPaysFormate = "Israel";
		}
		else if(_nomPays.equalsIgnoreCase("Suède")) {
			nomPaysFormate = "Suede";
		}
		else if(_nomPays.equalsIgnoreCase("Vénézuéla")) {
			nomPaysFormate = "Venezuela";
		}

		return nomPaysFormate;
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

    /**
     * Informations - Configuration du layout par défaut - A insérer une 'Progress Bar' lors du chargement des données.
    */
    public void configurationLayout() 
    {
		setTitle("Informations - " + nomPaysUtilisateur);

  		setContentView(R.layout.renseignements);

  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
	    /**ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);*/
    }

    /**
     * Récupère les dimensions de l'écran.
     */
    public void initScreenSize() 
    {
    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;
    }

    /**
     * Informations - Layout affichant aucune informations.
     */
    private void noInformation() 
    {
		setTitle("Informations - " + nomPaysUtilisateur);

    	LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 50, 50, 50);

    	TextView tv = new TextView(getApplication());
        tv.setLayoutParams(llp);
        tv.setTextColor(Color.BLACK);
    	tv.setText("Aucune information n'est disponible pour ce pays.");
    	tv.setGravity(Gravity.CENTER);

    	if(widthScreen > 2.5) { tv.setTextAppearance(getApplicationContext(), R.style.contenuInformationsTablette); } 
    	else { tv.setTextAppearance(getApplicationContext(), R.style.contenuInformationsSmartphone); }

    	LinearLayout ll = new LinearLayout(getApplication());
    	ll.setBackgroundColor(Color.WHITE);
    	ll.addView(tv);

    	setContentView(ll);
    }

    /**
     * Informations - Adapte la taille du menu sliding en fonction de la taille de l'écran.
     */
    public void configurationMenuSliding() 
    {
  		setBehindContentView(R.layout.menu_sliding);

		// Configuration du 'Menu Sliding'.
		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		//slidingMenu.setFadeEnabled(true);
		//slidingMenu.setFadeDegree(0.35f);

		// Dimensions du 'Menu Sliding'.
  	    if(widthScreen > 2.5) { slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_tablette); }
  	    else { slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_smartphone); }

		ActionBar actionBar = getActionBar();
	  	actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Informations - Configuration des items du Menu Sliding.
     */
    public void configurationItemsMenuSliding() 
    {
    	TextView itemAccueil = 			(TextView) findViewById(R.id.item_accueil);
    	TextView itemInformation = 		(TextView) findViewById(R.id.item_informations);
    	TextView itemCorrespondant = 	(TextView) findViewById(R.id.item_correspondants);
    	TextView itemTwitter = 			(TextView) findViewById(R.id.item_twitter);
    	TextView itemFacebook = 		(TextView) findViewById(R.id.item_facebook);
    	TextView itemBlogFE = 			(TextView) findViewById(R.id.item_blog);

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
				Intent intent = new Intent(Renseignements.this, Accueil.class);
				// Rappel l'activité 'Accueil' déjà lancée.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// Pas de navigation disponible, l'activité Accueil est la seule exécutée à ce moment.
				startActivity(intent);
				finish();
			}
		});

  	    itemInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Renseignements.this, Renseignements.class);
				// Le but ici est d'afficher la navigation, le bouton retour affichera l'accueil, donc : Accueil.
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Renseignements.this, Correspondants.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Renseignements.this, FEXTwitter.class);
				// Pas de navigation, lancement du navigateur.
				startActivity(intent);
				finish();
			}
		});

  	    itemFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Renseignements.this, FEXFacebook.class);
				// Pas de navigation, lancement du navigateur.
				startActivity(intent);
				finish();
			}
		});

  	    itemBlogFE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Renseignements.this, Blog.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});
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
 	 * Mise à jour du commentaire de l'utilisateur.
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