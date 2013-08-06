package com.lanouveller.franceexpatries;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.lanouveller.franceexpatries.ChoixPaysDialogFragment.ChoixPaysDialogListener;
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
public class Accueil extends Activity implements ChoixPaysDialogListener, CommentaireDialogListener {

  	// Stocke l'activité voulue pendant l'initialisation.
	private static int RENSEIGNEMENT_ECHEC = 2;
	private static int CORRESPONDANT_ECHEC = 3;

	private String nomPaysUtilisateur = "";

	// Configuration multi-devices.
	private float heightScreen;
	private float widthScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Récupère la taille de l'écran pour configurer les layouts.
		initScreenSize();

		// ??
		setTitleColor(Color.WHITE);

		// Affichage du choix du pays ( première connexion de l'utilisateur ) et Configuration des vignettes.
		choixPaysFirstConnexion();
	}

	@Override
	protected void onResume() {
		initFromPreferences();

		if(!nomPaysUtilisateur.equalsIgnoreCase("inconnu") 
		&& !nomPaysUtilisateur.equalsIgnoreCase("")
		&& !nomPaysUtilisateur.equalsIgnoreCase(" ")
		&& !nomPaysUtilisateur.equalsIgnoreCase("Choix du pays")) {
			configurationLayoutV2(true);
		}
		else {
			configurationLayoutV2(false);
		}

		super.onResume();
	}

    private void initFromPreferences() 
    {
    	Context context = getApplicationContext();
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

  		nomPaysUtilisateur = preferences.getString(Preferences.PREFERENCES_PAYS_UTILISATEUR, "inconnu");
    }

	/**
	 *  Affichage de la vue pour le choix du pays lors de la première connexion.
	 */
	public void choixPaysFirstConnexion() 
	{
		// Vérifie l'étape de configuration. ( true : pas de configuration effectuée )
		if(checkConfigurationFromBDD()) {
			// Configuration des vignettes sur la home.
			configurationLayoutV2(false);

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			// Sauvegarde des dimensions du terminal pour l'affichage de la boîte de dialogue.
			Editor editor = preferences.edit();
			editor.putFloat(Preferences.SIZE_DEVICE, initScreenSize());
			editor.commit();

			// Configuration du pays de l'expatrié en cours..
			/**DialogFragment dialog = new ChoixPaysDialogFragment();
			dialog.show(getFragmentManager(), "ChoixPaysDialogFragment");*/

			// Configuration du pays dans les préférences.
			Intent intent = new Intent(this, Preferences.class);
			intent.setAction("initialisation");
	  		intent.putExtra("CALLING_CLASS", "Accueil");
			startActivity(intent);

			// On sait que l'initialisation lors de l'installation de l'app a eu lieu.
			if(checkSaveFromBDD()) {
				saveInitialisationToBDD();
			}
		}

		// Configuration des vignettes sur la home.
		configurationLayoutV2(true);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	super.onCreateOptionsMenu(menu);

    	// Insertion du menu overflow.
    	MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_overflow_home, menu);

      	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId()) {
			// Items du 'Menu Overflow'.
			case R.id.item_preferences: {
	    		Intent intent = new Intent(this, Preferences.class);
	    		startActivity(intent);
			} return true;

			/**case R.id.item_commentaire: {
    			DialogFragment dialog = new CommentaireDialogFragment();
    			dialog.show(getFragmentManager(), "CommentaireDialogFragment");
    		} return true;*/
    	}

    	return false;
    }

    /**
     * Vérification s'il y a eu une initalisation du pays.
     */
    public boolean checkConfigurationFromBDD() 
 	{
    	// La valeur 1 signifie que le pays est initialisé.
    	String where = UtilisateurProvider.KEY_INITIALISE + " = 1";
 		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, where, null, null);

 		// Configuration valide.
 		if(cursor.moveToFirst()) { return false; }
 		// Pas de configuration effectuée.
 		else { return true; }
 	}

    /**
     * Vérification s'il y a eu une initalisation du pays.
     */
    public boolean checkSaveFromBDD() 
 	{
    	// La valeur 1 signifie que le pays est initialisé.
    	String where = UtilisateurProvider.KEY_INITIALISE + " = 0";
 		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, where, null, null);

 		// Configuration valide.
 		if(cursor.moveToFirst()) { return false; }
 		// Pas de configuration effectuée.
 		else { return true; }
 	}

 	/**
 	 * Sauvegarde de la tentative d'initalisation réalisée Et Aucun pays sélectionné.
 	 */
    public void saveInitialisationToBDD() 
  	{
	    ContentValues values = new ContentValues();
 		values.put(UtilisateurProvider.KEY_PAYS, "inconnu");
 		values.put(UtilisateurProvider.KEY_INITIALISE, 0);

 		getContentResolver().insert(UtilisateurProvider.CONTENT_UTILISATEUR_URI, values);
	}

 	/**
 	 * Sauvegarde du pays de l'utilisateur.
 	 */
    public void savePaysToBDD(String _nomPays) 
  	{
	    ContentResolver contentResolver = getContentResolver();

	    // Vérification initialisation de l'utilisateur.
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

 		// Mise à jour du pays.
 		if(cursor.moveToFirst()) {
 			majPaysToBDD(_nomPays);
 		}
 		// Sinon Création d'un id et pays utilisateur.
 		else {
 			ContentValues values = new ContentValues();
 			values.put(UtilisateurProvider.KEY_PAYS, _nomPays);
 	 		values.put(UtilisateurProvider.KEY_INITIALISE, 1);

 			contentResolver.insert(UtilisateurProvider.CONTENT_UTILISATEUR_URI, values);
 		}
	}

 	/**
 	 * Mise à jour du pays de l'utilisateur.
 	 */
    public void majPaysToBDD(String _nomPays) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_PAYS, _nomPays);
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
     * Configuration des Items Vignettes.
     * Adapte les dimensions des vignettes au terminal.
     * Configure la vignette personnalisée Informations en fonction du pays.
     * [Design by Angélique]
     */
    public void configurationLayoutV2(boolean verificationPays) 
    {
    	if(!nomPaysUtilisateur.equalsIgnoreCase("inconnu") 
    	&& !nomPaysUtilisateur.equalsIgnoreCase("Choix du pays")
    	&& !nomPaysUtilisateur.equalsIgnoreCase("") 
    	&& !nomPaysUtilisateur.equalsIgnoreCase(" ")) {
    		setTitle("France-Expatriés : " + nomPaysUtilisateur);
    	}

  		// Adpate la taille des vignettes en fonction de la taille de l'écran.
  	    if(widthScreen > 2.5) { setContentView(R.layout.accueil_v2_tablette); }
  	    else { setContentView(R.layout.accueil_v2_smartphone); }

		ImageView imageviewRenseignements = (ImageView) findViewById(R.id.vignette_renseignements);
		ImageView imageviewCorrespondants = (ImageView) findViewById(R.id.vignette_correspondants);
		ImageView imageviewTwitter = 		(ImageView) findViewById(R.id.vignette_fluxtwitter);
		ImageView imageviewFacebook = 		(ImageView) findViewById(R.id.vignette_fluxfacebook);
		ImageView imageviewGuides = 		(ImageView)	findViewById(R.id.vignette_guides);
		//ImageView imageviewBlogFE = 		(ImageView) findViewById(R.id.vignette_blog);

		// Pays initialisé.
		if(verificationPays) {
			// -> Envoi de la vignette personnalisée en fonction du Pays et du Terminal. ( tablette )
			if(widthScreen > 2.5) {
				// Design par défaut initialisé.
				if(nomPaysUtilisateur.equalsIgnoreCase("Allemagne")) {
					// Design à mettre à jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Argentine")) {
					// Design à mettre à jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Australie")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Autriche")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Canada")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Espagne")) {
					imageviewRenseignements.setImageResource(R.drawable.vignette_v2_infos_espagne_800x1280);				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Etats-Unis")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Liban")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Luxembourg")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Mexique")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Royaume-Uni")) {
					// Design à mettre à jour..				
				}
			}
			// Version smartphone.
			else {
				if(nomPaysUtilisateur.equalsIgnoreCase("Allemagne")) {
					// Design à mettre à jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Argentine")) {
					// Design à mettre à jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Australie")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Autriche")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Canada")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Espagne")) {
					imageviewRenseignements.setImageResource(R.drawable.vignette_v2_infos_espagne_480x854);				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Etats-Unis")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Liban")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Luxembourg")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Mexique")) {
					// Design à mettre à jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Royaume-Uni")) {
					// Design à mettre à jour..				
				} 
			}

			// Configuration de l'évènement des vignettes.
			imageviewRenseignements.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Renseignements.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivity(intent);
				}
			});

			imageviewCorrespondants.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Correspondants.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivity(intent);
				}
			});

			imageviewGuides.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Guides.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivity(intent);
				}
			});
		}

		// Envoi du layout d'erreur.
		else if(!verificationPays) {
			// Envoi de la vignette non personnalisée.
			if(widthScreen > 2.5) { imageviewRenseignements.setImageResource(R.drawable.vignette_v2_defaut_800x1280); }
			else { imageviewRenseignements.setImageResource(R.drawable.vignette_v2_defaut_480x854); }

			// Configuration des vignettes.
			imageviewRenseignements.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Erreur.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivityForResult(intent, RENSEIGNEMENT_ECHEC);
				}
			});

			imageviewCorrespondants.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Erreur.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivityForResult(intent, CORRESPONDANT_ECHEC);
				}
			});

			imageviewGuides.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					Intent intent = new Intent(Accueil.this, Erreur.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
					startActivityForResult(intent, CORRESPONDANT_ECHEC);
				}
			});
		}

		imageviewTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Accueil.this, FEXTwitter.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
			}
		});

		imageviewFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Accueil.this, FEXFacebook.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
			}
		});

		/**imageviewBlogFE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Accueil.this, Blog.class);
			  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
			}
		});*/
    }

    /**
     * Récupère la taille de l'écran pour configurer les layouts.
     */
    public float initScreenSize() 
    {
    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;

  	    return widthScreen;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);

    	if(requestCode == RENSEIGNEMENT_ECHEC && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(Accueil.this, Renseignements.class);
	  		intent.putExtra("CALLING_CLASS", "Accueil");
			startActivity(intent);
    	}

    	else if(requestCode == CORRESPONDANT_ECHEC && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(Accueil.this, Correspondants.class);
	  		intent.putExtra("CALLING_CLASS", "Accueil");
			startActivity(intent);    		
    	}
    }

	/**
	 * Méthodes du choix du pays.
	 */
	@Override
	public void onChoixPaysPositiveClick(DialogFragment paysDialog) 
	{
		// Récupération du pays choisi.
		ChoixPaysDialogFragment cpdf = (ChoixPaysDialogFragment) paysDialog;
		// Mise à jour du paramètre.
		nomPaysUtilisateur = cpdf.getPays();
		// Sauvegarde BDD du pays.
		savePaysToBDD(cpdf.getPays());

		// Envoi du layout à jour.
		configurationLayoutV2(true);
	}

	@Override
	public void onChoixPaysNegativeClick(DialogFragment dialog) 
	{
		Toast.makeText(getApplication(), "Vous n'avez sélectionner aucun pays, accéder aux préférences pour le sélectionner", Toast.LENGTH_LONG).show();
		nomPaysUtilisateur = "inconnu";

		// Configuration du layout d'erreur.
		configurationLayoutV2(false);
	}

	@Override
	public void onChoixPaysFinishEditDialog(String inputText) { }

 	/**
 	 * Mise à jour du commentaire / signalement d'un bug par l'utilisateur.
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