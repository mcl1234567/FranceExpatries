package com.lanouveller.franceexpatries;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
public class Preferences extends Activity {

	// Stockage de la valeur numérique dans le spinner.
	public static final String PREFERENCES_POSITION_PAYS_UTILISATEUR = "PREFERENCES_POSITION_PAYS_UTILISATEUR";
	// Stockage de la valeur chainee
	public static final String PREFERENCES_PAYS_UTILISATEUR = "PREFERENCES_PAYS_UTILISATEUR";
	// Non utilisé
	public static final String SIZE_DEVICE = "SIZE_DEVICE";

	public static final String PREFERENCES_DISPONIBILITE_INFORMATION = "PREFERENCES_DISPONIBILITE_INFORMATION";
	public static final String PREFERENCES_DISPONIBILITE_CORRESPONDANT = "PREFERENCES_DISPONIBILITE_CORRESPONDANT";
	public static final String PREFERENCES_DISPONIBILITE_GUIDE = "PREFERENCES_DISPONIBILITE_GUIDE";

	private SharedPreferences preferences;

	private boolean failInstallValue = false;

	// Receivers de services.
	private InformationReceiver receiverInformation;
	private CorrespondantReceiver receiverCorrespondant;
	private GuideReceiver receiverGuide;
	private GuideCheckReceiver receiverCheckGuide;

	private Spinner spinnerChoixPays;

	// Disponibilités des fonctionnalités.
	private boolean guidesDisponiblesHs = false;
	private boolean guidesDisponibles = false;
	private ImageView etatInformationsIv;
	private ImageView etatCorrespondantsIv;
	private ImageView etatGuidesIv;
	
	// Configure le téléchargement requis.
	private boolean reload = false;
	
	// Nom du pays actuel.
	private String nomPaysUtilisateur = "";

	// Menu Item [refresh].
	private Menu optionsMenu;
	private boolean refreshing = false;

	// Navigation.
	private String callingClass = ""; 

  	// Configuration multi-écrans.
	private float heightScreen;
	private float widthScreen;

  	@Override
  	public void onCreate(Bundle savedInstanceState) 
  	{
  		super.onCreate(savedInstanceState);

    	// Ré-initialisation des disponibilités des fonctionnalités.
    	guidesDisponiblesHs = false;
    	guidesDisponibles = false;

  		// Initialisation de la configuration des paramètres.
  		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

  		// Récupère les dimensions du terminal.
  		initScreenSize();

    	initPaysFromBDD();

  		// Message de l'installation de l'application.
  		Intent intent = this.getIntent();
  		callingClass = intent.getStringExtra("CALLING_CLASS");

  		if(intent.getAction() != null && intent.getAction().equalsIgnoreCase("initialisation")) {
  			if(!isOnline()) {
  				Toast.makeText(getApplicationContext(), "Échec de l'installation : veuillez vérifier la connexion réseau.", Toast.LENGTH_LONG).show();
  			} else {
  				Toast.makeText(getApplicationContext(), "Veuillez sélectionner votre pays d'expatriation.", Toast.LENGTH_LONG).show();
  			}
  		}
  		// Limitation des données à un seul pays.
  		else if(!isOnline()) {
  			Toast.makeText(getApplicationContext(), "Vous êtes déconnecté. Toutes les données du pays précédemment téléchargées sont accessibles hors-ligne.", Toast.LENGTH_LONG).show();
  		}

  		configurationLayout();

  		// Lance le checking des contenus du pays choisi.
	    spinnerChoixPays.setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> _adapterView, View _view, int _arg2, long _arg3) {
	        	String mSelection = spinnerChoixPays.getSelectedItem().toString();

	        	// Si l'utilisateur est connecté, il peut télécharger un nouveau pays. ( pour le moment ce pays remplace le précédent dans la BDD ) .
	      		if(!mSelection.equalsIgnoreCase("Choisir un pays")) {
	      			// Finalisation de la configuration du pays.
	      			if(mSelection.equalsIgnoreCase(nomPaysUtilisateur)) {
	      				if(checkInformationsFromBDD()) {
	      					// Disponible.
	      					etatInformationsIv.setImageResource(R.drawable.navigation_accept);
	      				} else if(checkInitInformationsFromBDD()) {
	      					// Vide ou crash.
	      					etatInformationsIv.setImageResource(R.drawable.navigation_defaut);
	      					failInstallValue = true;
	      				} else {
	      					// Non disponible.
	      					etatInformationsIv.setImageResource(R.drawable.navigation_cancel);	      					
	      				}
	      				if(checkCorrespondantsDataFromBDD()) {
	      					// Disponible.
	      					etatCorrespondantsIv.setImageResource(R.drawable.navigation_accept);
	      				} else if(checkInitCorrespondantsDataFromBDD()) {
	      					// Vide ou crash.
	      					etatCorrespondantsIv.setImageResource(R.drawable.navigation_defaut);
	      					failInstallValue = true;
	      				} else {
	      					// Non disponible.
	      					etatCorrespondantsIv.setImageResource(R.drawable.navigation_cancel);	      					
	      				}
	      				if(checkGuidesHorsLigneFromBDD()) {
	      					// Disponible.
	      					etatGuidesIv.setImageResource(R.drawable.navigation_accept);
	      				} else if(checkInitGuidesHorsLigneFromBDD()) {
	      					// Vide ou crash.
	      					etatGuidesIv.setImageResource(R.drawable.navigation_defaut);
	      					failInstallValue = true;
	      				} else {
	      					// Non disponible.
	      					etatGuidesIv.setImageResource(R.drawable.navigation_cancel);	      					
	      				}
	      				if(failInstallValue) failInstall();
	      				failInstallValue = false;
	      			}
	      			// Chargement d'un nouveau pays.
	      			if(!mSelection.equalsIgnoreCase(nomPaysUtilisateur)) {
	      				if(isOnline()) {
			      			// Enregistre le nouveau pays.
			      			savePaysToPreferences();
			      			savePaysToBDD(mSelection);
		
					        // Réinitialisation des disponibilités des fonctionnalités.
					        guidesDisponiblesHs = false;
					        guidesDisponibles = false;
					        etatInformationsIv.setImageResource(R.drawable.navigation_defaut);
					        etatCorrespondantsIv.setImageResource(R.drawable.navigation_defaut);
					        etatGuidesIv.setImageResource(R.drawable.navigation_defaut);
	
					        // Réinitialise la BDD Utilisateur. Actuellement un seul pays enregistrable.
					        reInitBDD();
			
					        // Active la ProgressBar.
					      	setRefreshActionButtonState(true);
					      	Toast.makeText(getApplicationContext(), "Téléchargement en cours..", Toast.LENGTH_SHORT).show();
			
					        // Récupère les disponibilités des guides hors-ligne.
					        launchInformationService(mSelection);
					        launchCorrespondantService(mSelection);
					        launchGuideService("argent", "DOWNLOADING");   
		      			} else {
			      			Toast.makeText(getApplicationContext(), "Échec du changement de pays : veuillez vérifier la connexion réseau.", Toast.LENGTH_LONG).show();  			
			      		}
	      			}
	      		} else {
	      			saveFailInitialisationToBDD();
	      		}
	        }

	        @Override
	        public void onNothingSelected(AdapterView<?> _adapterView) { }
	    });
  	}

  	@Override 
    public void onResume() 
    {  		
  		receiverInformation = new InformationReceiver();
    	registerReceiver(receiverInformation, new IntentFilter(RenseignementService.INFORMATION_REFRESHED));

    	receiverCorrespondant = new CorrespondantReceiver();
    	registerReceiver(receiverCorrespondant, new IntentFilter(CorrespondantService.CORRESPONDANT_REFRESHED));

    	receiverGuide = new GuideReceiver();
    	registerReceiver(receiverGuide, new IntentFilter(FicheService.GUIDE_REFRESHED));

    	receiverCheckGuide = new GuideCheckReceiver();
    	registerReceiver(receiverCheckGuide, new IntentFilter(FicheService.CHECK_GUIDE));

    	super.onResume();
    }

    @Override
    public void onPause() 
    {
    	unregisterReceiver(receiverInformation);
    	unregisterReceiver(receiverCorrespondant);
    	unregisterReceiver(receiverGuide);
    	unregisterReceiver(receiverCheckGuide);

    	super.onPause();
    }

  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) 
  	{
  	    this.optionsMenu = menu;
  	    getMenuInflater().inflate(R.menu.menu_progress_preferences, menu);

  	    return super.onCreateOptionsMenu(menu);
  	}

  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) 
  	{
    	switch(item.getItemId()) {
    		// Touche actionbar retour.
    		case android.R.id.home: {
    			if(!refreshing) {
    				//savePaysToPreferences();
    		 		setResult(RESULT_OK);
    		 		finish();
    			}
        		// Téléchargement et installation du nouveau pays interrompu ( redirigé à l'Accueil ).
    			else if(reload) {
    				//savePaysToPreferences();
    				startActivity(new Intent(this, Accueil.class));
    				Toast.makeText(getApplicationContext(), "Le téléchargement des données a été interrompu.", Toast.LENGTH_SHORT).show();
    		 		finish();
    			}

    			return true;
	  		}
    	}

  		return super.onOptionsItemSelected(item);
  	}

  	/**
  	 * En cas d'échec de connexion réseau ou annulation du téléchargement des données du pays, lors 
  	 * de la prochaine configuration, relancement du processus. 
  	 */
  	public void failInstall() {
  		if(isOnline()) {
  			// Réinitialise la BDD Utilisateur. Actuellement un seul pays enregistrable.
	    	reInitBDD();
	
	    	// Active la ProgressBar.
	  		setRefreshActionButtonState(true);
	  		Toast.makeText(getApplicationContext(), "Téléchargement en cours..", Toast.LENGTH_SHORT).show();

	    	// Récupère les disponibilités des guides hors-ligne.
	    	launchInformationService(nomPaysUtilisateur);
	    	launchCorrespondantService(nomPaysUtilisateur);
	    	launchGuideService("argent", "DOWNLOADING"); 
    	}
  	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
		// Touche retour.
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		// Téléchargement et installation finis.
			if(!refreshing) {
				//savePaysToPreferences();
		 		setResult(RESULT_OK);
		 		finish();
			}
    		// Téléchargement et installation du nouveau pays en cours ( redirigé à l'Accueil ).
			else if(reload) {
				//savePaysToPreferences();
				startActivity(new Intent(this, Accueil.class));
				Toast.makeText(getApplicationContext(), "Le téléchargement des données a été interrompu.", Toast.LENGTH_SHORT).show();
		 		finish();
			}
    	}

		return false;
    }

  	public class InformationReceiver extends BroadcastReceiver {
  		public static final String INFORMATION_REFRESHED = "com.lanouveller.franceexpatries.INFORMATION_REFRESHED";

  		@Override
    	public void onReceive(Context context, Intent intent) 
    	{
  	  		String disponibilite = (String) intent.getCharSequenceExtra("disponibilite");

  			if(disponibilite.equalsIgnoreCase("information_non_disponible")) {
  				etatInformationsIv.setImageResource(R.drawable.navigation_cancel);
  			} 
  			else if(disponibilite.equalsIgnoreCase("information_disponible")) {
  				etatInformationsIv.setImageResource(R.drawable.navigation_accept);
  			}

  			// Enregistrement de la disponiblité.
  			saveDisponibilitesInfosToBDD(disponibilite);
    	}
  	}

  	public class CorrespondantReceiver extends BroadcastReceiver {
  		public static final String CORRESPONDANT_REFRESHED = "com.lanouveller.franceexpatries.CORRESPONDANT_REFRESHED"; 

    	@Override
    	public void onReceive(Context context, Intent intent) 
    	{
  	  		String disponibilite = (String) intent.getCharSequenceExtra("disponibilite");

  			if(disponibilite.equalsIgnoreCase("correspondant_non_disponible")) {
  				etatCorrespondantsIv.setImageResource(R.drawable.navigation_cancel);
  			}
  			else if(disponibilite.equalsIgnoreCase("correspondant_disponible")) {
  				etatCorrespondantsIv.setImageResource(R.drawable.navigation_accept);
  			}

  			// Enregistrement de la disponiblité.
  			saveDisponibilitesCorrespondantsToBDD(disponibilite);
    	}
   	}

  	public class GuideReceiver extends BroadcastReceiver {
  		public static final String GUIDE_REFRESHED = "com.lanouveller.franceexpatries.GUIDE_REFRESHED";

  		@Override
    	public void onReceive(Context context, Intent intent) 
    	{
  	  		String disponibilite = (String) intent.getCharSequenceExtra("disponibilite");

  	  		// Guide 'argent' Hors-Ligne.
  	  		if(disponibilite.equalsIgnoreCase("guide_argent_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibilitesGuideArgentHsToBDD(disponibilite);
	        	launchGuideService("emploi", "DOWNLOADING");
			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_argent_non_disponible_hs")) {
	  			saveDisponibilitesGuideArgentHsToBDD(disponibilite);
	        	launchGuideService("emploi", "DOWNLOADING");
  			}
  	  		// Guide 'emploi' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_emploi_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuideEmploiHsToBDD(disponibilite);
	        	launchGuideService("passeport", "DOWNLOADING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_emploi_non_disponible_hs")) {
	  			saveDisponibiliteGuideEmploiHsToBDD(disponibilite);
	        	launchGuideService("passeport", "DOWNLOADING");
  			}
  	  		// Guide 'passeport' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_passeport_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuidePasseportHsToBDD(disponibilite);
	        	launchGuideService("sante", "DOWNLOADING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_passeport_non_disponible_hs")) {
	  			saveDisponibiliteGuidePasseportHsToBDD(disponibilite);
	        	launchGuideService("sante", "DOWNLOADING");
  			}
  	  		// Guide 'santé' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_sante_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuideSanteHsToBDD(disponibilite);
	        	launchGuideService("transport", "DOWNLOADING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_sante_non_disponible_hs")) {
	  			saveDisponibiliteGuideSanteHsToBDD(disponibilite);
	        	launchGuideService("transport", "DOWNLOADING");
  			}
  	  		// Guide 'transport' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_transport_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuideTransportHsToBDD(disponibilite);
	        	launchGuideService("vie_culturelle", "DOWNLOADING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_transport_non_disponible_hs")) {
	  			saveDisponibiliteGuideTransportHsToBDD(disponibilite);
	        	launchGuideService("vie_culturelle", "DOWNLOADING");
  			}
  	  		// Guide 'vie culturelle' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_culturelle_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuideVieCulturelleHsToBDD(disponibilite);
	        	launchGuideService("vie_quotidienne", "DOWNLOADING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_culturelle_non_disponible_hs")) {
	  			saveDisponibiliteGuideVieCulturelleHsToBDD(disponibilite);
	        	launchGuideService("vie_quotidienne", "DOWNLOADING");
  			}
  	  		// Guide 'vie quotidienne' Hors-Ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_quotidienne_disponible_hs")) {
  	  			guidesDisponiblesHs = true;
	  			saveDisponibiliteGuideVieQuotidienneHsToBDD(disponibilite);
	        	launchGuideService("argent", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_quotidienne_non_disponible_hs")) {
	  			saveDisponibiliteGuideVieQuotidienneHsToBDD(disponibilite);
	        	launchGuideService("argent", "CHECKING");
  			}

  	  		if(guidesDisponiblesHs) { saveDisponibilitesGuidesHsToBDD("guides_disponibles_hs"); }
  	  		else { saveDisponibilitesGuidesHsToBDD("guides_non_disponibles_hs"); }
    	}
  	}

  	public class GuideCheckReceiver extends BroadcastReceiver {
  		public static final String CHECK_GUIDE = "com.lanouveller.franceexpatries.CHECK_GUIDE";

  		@Override
    	public void onReceive(Context context, Intent intent) 
    	{
  	  		String disponibilite = (String) intent.getCharSequenceExtra("disponibilite");

  	  		// Guide 'argent' En ligne.
  	  		if(disponibilite.equalsIgnoreCase("guide_argent_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibilitesGuideArgentToBDD(disponibilite);
	        	launchGuideService("emploi", "CHECKING");
			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_argent_non_disponible")) {
	  			saveDisponibilitesGuideArgentToBDD(disponibilite);
	        	launchGuideService("emploi", "CHECKING");
  			}
  	  		// Guide 'emploi' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_emploi_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuideEmploiToBDD(disponibilite);
	        	launchGuideService("passeport", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_emploi_non_disponible")) {
	  			saveDisponibiliteGuideEmploiToBDD(disponibilite);
	        	launchGuideService("passeport", "CHECKING");
  			}
  	  		// Guide 'passeport' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_passeport_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuidePasseportToBDD(disponibilite);
	        	launchGuideService("sante", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_passeport_non_disponible")) {
	  			saveDisponibiliteGuidePasseportToBDD(disponibilite);
	        	launchGuideService("sante", "CHECKING");
  			}
  	  		// Guide 'santé' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_sante_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuideSanteToBDD(disponibilite);
	        	launchGuideService("transport", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_sante_non_disponible")) {
	  			saveDisponibiliteGuideSanteToBDD(disponibilite);
	        	launchGuideService("transport", "CHECKING");
  			}
  	  		// Guide 'transport' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_transport_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuideTransportToBDD(disponibilite);
	        	launchGuideService("vie_culturelle", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_transport_non_disponible")) {
	  			saveDisponibiliteGuideTransportToBDD(disponibilite);
	        	launchGuideService("vie_culturelle", "CHECKING");
  			}
  	  		// Guide 'vie culturelle' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_culturelle_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuideVieCulturelleToBDD(disponibilite);
	        	launchGuideService("vie_quotidienne", "CHECKING");
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_culturelle_non_disponible")) {
	  			saveDisponibiliteGuideVieCulturelleToBDD(disponibilite);
	        	launchGuideService("vie_quotidienne", "CHECKING");
  			}
  	  		// Guide 'vie quotidienne' En ligne.
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_quotidienne_disponible")) {
  	  			guidesDisponibles = true;
	  			saveDisponibiliteGuideVieQuotidienneToBDD(disponibilite);
	  			// Permets d'informer l'utilisateur une seule fois sur l'état du téléchargement.
	      		Toast.makeText(getApplicationContext(), "Téléchargement terminé.", Toast.LENGTH_SHORT).show();
  			}
  	  		else if(disponibilite.equalsIgnoreCase("guide_vie_quotidienne_non_disponible")) {
	  			saveDisponibiliteGuideVieQuotidienneToBDD(disponibilite);
	  			// Permets d'informer l'utilisateur une seule fois sur l'état du téléchargement.
	      		Toast.makeText(getApplicationContext(), "Téléchargement terminé.", Toast.LENGTH_SHORT).show();
  			}

  	  		if(guidesDisponibles) {
				etatGuidesIv.setImageResource(R.drawable.navigation_accept);
  	  			saveDisponibilitesGuidesToBDD("guides_disponibles");
  	  		}
  	  		else {
  				etatGuidesIv.setImageResource(R.drawable.navigation_cancel);
	  			saveDisponibilitesGuidesToBDD("guides_non_disponibles");
  	  		}

  	  		// Désactive la ProgressBar.
  	  		setRefreshActionButtonState(false);
    	}
  	}
    
    public void reInitBDD() 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();
		reload = true;

	    values.put(UtilisateurProvider.KEY_DISPO_INFOS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_CORRESPONDANTS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDES_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDES, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_ARGENT_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_EMPLOI_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_PASSEPORT_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_SANTE_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_TRANSPORT_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_CULTURELLE_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_QUOTIDIENNE_HS, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_ARGENT, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_EMPLOI, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_PASSEPORT, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_SANTE, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_TRANSPORT, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_CULTURELLE, "");
	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_QUOTIDIENNE, "");

	    getContentResolver().update(uri, values, null, null);
    }

  	/**
  	 * Retourne le nom formaté.
	*  Accents et espaces non autorisés dans l'url de requête HTTP.
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

    /**
     * Vérifie la disponibilité des informations générales du pays.
     */
	public boolean checkInformationsFromBDD() 
	{
  		try {
	  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
	
	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_INFOS_COLUMN).equalsIgnoreCase("information_disponible")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

    /**
     * Vérifie la disponibilité des correspondats du pays.
     */
	public boolean checkCorrespondantsDataFromBDD() 
	{
  		try {
  			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_CORRESPONDANTS_COLUMN).equalsIgnoreCase("correspondant_disponible")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

	public boolean checkGuidesHorsLigneFromBDD() 
	{
		try {
			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDES_HS_COLUMN).equalsIgnoreCase("guides_disponibles_hs")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

	/**
     * Vérifie la disponibilité des informations générales du pays.
     */
	public boolean checkInitInformationsFromBDD() 
	{
  		try {
	  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);
	
	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_INFOS_COLUMN).equalsIgnoreCase("")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

    /**
     * Vérifie la disponibilité des correspondats du pays.
     */
	public boolean checkInitCorrespondantsDataFromBDD() 
	{
  		try {
  			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_CORRESPONDANTS_COLUMN).equalsIgnoreCase("")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

	public boolean checkInitGuidesHorsLigneFromBDD() 
	{
		try {
			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_GUIDES_HS_COLUMN).equalsIgnoreCase("")) {
	  				return true;
	  			}
	  		}
  		} catch (NullPointerException e) {
  			// Tentative de relancement de la configuration du pays.
  			failInstallValue = true;
  		}

		return false;
	}

  	private void launchInformationService(String _mSelection) 
  	{
  		Intent intent = new Intent(this, RenseignementService.class);
  		intent.setAction(getPaysFormate(_mSelection));
  		intent.putExtra("DOWNLOADING", "DOWNLOADING");
  		startService(intent);
  	}

  	private void launchCorrespondantService(String _mSelection) 
  	{
  		Intent intent = new Intent(this, CorrespondantService.class);
  		intent.setAction(getPaysFormate(_mSelection));
  		intent.putExtra("DOWNLOADING", "DOWNLOADING");
  		startService(intent);
  	}

  	private void launchGuideService(String _typeGuide, String _mode) 
  	{
  		Intent intent = new Intent(this, FicheService.class);
  		intent.setAction(_typeGuide);
  		if(_mode.equalsIgnoreCase("CHECKING")) intent.putExtra("CHECKING", "CHECKING");
  		else if(_mode.equalsIgnoreCase("DOWNLOADING")) intent.putExtra("DOWNLOADING", "DOWNLOADING");
  		startService(intent);
  	}

  	public void setRefreshActionButtonState(final boolean _refreshing) 
  	{
  	    if(optionsMenu != null) {
  	    	// Re-initialise l'item refresh.
  	        final MenuItem refreshItem = optionsMenu.findItem(R.id.item_refresh);

  	        if(refreshItem != null) {
  	            if(_refreshing) {
  	            	// Reconfigure la vue de l'item.
  	            	refreshItem.setActionView(R.layout.indeterminate);
  	            	refreshing = true;
  	            }
  	            else {
  	            	refreshItem.setActionView(null); 
  	            	refreshing = false;
	            }
  	        }
  	    }
  	}

    /**
     *  [Préférences] Chargement des pays dans le 'Spinner'.
     */
  	private void configurationLayout() 
  	{
  		setContentView(R.layout.preferences);

  		// Navigation.
  		/**setTitle(callingClass);*/
  		setTitle("Préférences");

  		// Configuration du titre.
  		String[] paysDisponibles = getResources().getStringArray(R.array.pays_disponibles);
    	TextView titrePreferencesTv = (TextView) findViewById(R.id.titre_preferences);
    	titrePreferencesTv.setText("Préférences");

    	// Configuration des TextViews.
    	TextView libelleChoixPaysTv = (TextView) findViewById(R.id.libelle_choix_pays);
    	libelleChoixPaysTv.setText("Sélectionnez votre pays parmis les " + String.valueOf(paysDisponibles.length-1) + 
    								" pays disponibles dans la liste déroulante ci-dessous : ");
    	libelleChoixPaysTv.setTextColor(getResources().getColor(R.color.gray));

  		// Configuration des disponibilités.
    	TextView dispoInfosTv = 			(TextView) findViewById(R.id.disponibilite_informations);
    	TextView dispoCorrespondantsTv = 	(TextView) findViewById(R.id.disponibilite_correspondants);
    	TextView dispoGuidesTv = 			(TextView) findViewById(R.id.disponibilite_guides);
    	// Configuration des ImageViews.
	    etatInformationsIv = 	(ImageView) findViewById(R.id.disponibilite_informations_image);
	    etatCorrespondantsIv = 	(ImageView) findViewById(R.id.disponibilite_correspondants_image);
	    etatGuidesIv = 			(ImageView) findViewById(R.id.disponibilite_guides_image);

	    etatInformationsIv.setImageResource(R.drawable.navigation_defaut);
		etatCorrespondantsIv.setImageResource(R.drawable.navigation_defaut);
		etatGuidesIv.setImageResource(R.drawable.navigation_defaut);

    	// Configuration des TextViews.
    	if(widthScreen > 2.5) { 
    		titrePreferencesTv.setTextAppearance(this, R.style.titreInformationTablette);
    		libelleChoixPaysTv.setTextSize(20);
    		dispoInfosTv.setTextSize(20);
    		dispoCorrespondantsTv.setTextSize(20);
    		dispoGuidesTv.setTextSize(20);
    	}
    	else {
    		titrePreferencesTv.setTextAppearance(this, R.style.titreInformationSmartphone);
    		libelleChoixPaysTv.setTextSize(11); 
    		dispoInfosTv.setTextSize(11); 
    		dispoCorrespondantsTv.setTextSize(11);
    		dispoGuidesTv.setTextSize(11);
   		}

  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
	    /**ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);*/

	    // Configuration du Spinner ( pays ).
	    spinnerChoixPays = (Spinner) findViewById(R.id.spinner_choix_pays);
  		/**int layoutID = android.R.layout.simple_spinner_item;*/
    	ArrayAdapter<CharSequence> adapterPays = ArrayAdapter.createFromResource(this, R.array.pays_disponibles, R.layout.preferences_spinner);
    	adapterPays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinnerChoixPays.setAdapter(adapterPays);

    	// [Positionnement du pays en fonction de son état précédent]: Soit lors de l'installation de l'application: Chaîne par défaut ( choix du pays ) ,
    	// Soit dernier pays enregistré de l'utilisateur.
  		spinnerChoixPays.setSelection(preferences.getInt(PREFERENCES_POSITION_PAYS_UTILISATEUR, 0));

  		// Vérification de la sélection via variable globale.
  		if(spinnerChoixPays.getSelectedItemPosition() == 0) {
    		if(!nomPaysUtilisateur.equalsIgnoreCase("inconnu")) {    	
		    	for(int i=0; i<spinnerChoixPays.getAdapter().getCount(); i++) {
		    		if(spinnerChoixPays.getAdapter().getItem(i).toString().equalsIgnoreCase(nomPaysUtilisateur)) {
		    			spinnerChoixPays.setSelection(i);
		    		}
		    	}
	    	}
    	}

  		// Récupération du nom du pays dans la variable globale.
  		/**nomPaysUtilisateur = spinnerChoixPays.getSelectedItem().toString();

    	// Enregistrement du nom du pays en cours d'utilisation.
  		savePaysToBDD(nomPaysUtilisateur);*/

  		/**if(isOnline()) {
  			// Récupère les disponibilités des infos, correspondants et guides hors-ligne et en ligne.
  			launchInformationService(nomPaysUtilisateur);
  			launchCorrespondantService(nomPaysUtilisateur);
  			launchGuideService("argent", "DOWNLOADING");
  			// A la suite, lancement dans le receiver des services pour avoir la disponibilité de chaque guide hors-ligne et en ligne !
  		}*/
  	}

  	/**
  	 *  Préférences - Enregistre les préférences.
  	 */
  	private void savePaysToPreferences() 
  	{
  		// Sauvegarde du pays.
		nomPaysUtilisateur = spinnerChoixPays.getSelectedItem().toString();

  		// Ecriture dans les Constantes de l'application.
  		Editor editor = preferences.edit();
  		editor.putInt(PREFERENCES_POSITION_PAYS_UTILISATEUR, spinnerChoixPays.getSelectedItemPosition());
  		editor.commit();

  		editor = preferences.edit();
  		editor.putString(PREFERENCES_PAYS_UTILISATEUR, nomPaysUtilisateur);
  		editor.commit();
  	}

  	/**
  	 * Initialisation du pays.
  	 */
  	public void initPaysFromBDD() 
  	{
  		try {
  			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			nomPaysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
	  		}
  		} catch (NullPointerException e) {
  		}
  	}

 	/**
 	 * Sauvegarde de la tentative d'initalisation réalisée Et Aucun pays sélectionné.
 	 */
    public void saveFailInitialisationToBDD() 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	 	values.put(UtilisateurProvider.KEY_INITIALISE, 0);
	    getContentResolver().update(uri, values, null, null);
	}

 	/**
 	 * Préférences - Mise à jour du pays de l'utilisateur.
 	 */
    public void savePaysToBDD(String _nomPays) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_PAYS, _nomPays);
	 	values.put(UtilisateurProvider.KEY_INITIALISE, 1);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibilitesInfosToBDD(String _dispoInfos) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_INFOS, _dispoInfos);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibilitesCorrespondantsToBDD(String _dispoCorrespondants) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_CORRESPONDANTS, _dispoCorrespondants);
	    getContentResolver().update(uri, values, null, null);
	}

    // Non utilisé dans les préférences, l'utilisateur pourra seulement savoir si les guides en ligne sont disponibles.
    public void saveDisponibilitesGuidesHsToBDD(String _dispoGuidesHs) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDES_HS, _dispoGuidesHs);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibilitesGuidesToBDD(String _dispoGuides) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDES, _dispoGuides);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibilitesGuideArgentHsToBDD(String _dispoGuideArgentHs) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_ARGENT_HS, _dispoGuideArgentHs);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibiliteGuideEmploiHsToBDD(String _dispoGuideEmploiHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_EMPLOI_HS, _dispoGuideEmploiHs);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuidePasseportHsToBDD(String _dispoGuidePasseportHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_PASSEPORT_HS, _dispoGuidePasseportHs);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideSanteHsToBDD(String _dispoGuideSanteHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_SANTE_HS, _dispoGuideSanteHs);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideTransportHsToBDD(String _dispoGuideTransportHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_TRANSPORT_HS, _dispoGuideTransportHs);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideVieCulturelleHsToBDD(String _dispoGuideVieCulturelleHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_CULTURELLE_HS, _dispoGuideVieCulturelleHs);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideVieQuotidienneHsToBDD(String _dispoGuideVieQuotidienneHs) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_QUOTIDIENNE_HS, _dispoGuideVieQuotidienneHs);
	    getContentResolver().update(uri, values, null, null);
    } 

    public void saveDisponibilitesGuideArgentToBDD(String _dispoGuideArgent) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_ARGENT, _dispoGuideArgent);
	    getContentResolver().update(uri, values, null, null);
	}

    public void saveDisponibiliteGuideEmploiToBDD(String _dispoGuideEmploi) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_EMPLOI, _dispoGuideEmploi);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuidePasseportToBDD(String _dispoGuidePasseport) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_PASSEPORT, _dispoGuidePasseport);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideSanteToBDD(String _dispoGuideSante) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_SANTE, _dispoGuideSante);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideTransportToBDD(String _dispoGuideTransport) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_TRANSPORT, _dispoGuideTransport);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideVieCulturelleToBDD(String _dispoGuideVieCulturelle) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_CULTURELLE, _dispoGuideVieCulturelle);
	    getContentResolver().update(uri, values, null, null);
    }

    public void saveDisponibiliteGuideVieQuotidienneToBDD(String _dispoGuideVieQuotidienne) 
    {
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_DISPO_GUIDE_VIE_QUOTIDIENNE, _dispoGuideVieQuotidienne);
	    getContentResolver().update(uri, values, null, null);
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
     * Récupère les dimensions de l'écran.
     */
    public void initScreenSize() 
    {
    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;
    }

}