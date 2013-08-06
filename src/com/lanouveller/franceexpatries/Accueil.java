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
 * Nommage des m�thodes:
 * 	'init' pour l'initialisation des diff�rentes variables de contenus charg�es via ressources externes.
 * 	'configurationLayout' pour le param�trage de la structure de la vue.
 * 	'From{Ressources}' BDD ou SharedPreferences
 * Nommage des variables:
 * 	Exemple d'abr�viation de vues:
 * 	{fonction/contenu}Ll: (LinearLayout)
 * 	variableIv: (ImageView)
 * Nommage d'un argument d'entr�e dans une fonction:
 * 	_variable
 * Nommage des constantes en majuscule.
 * 
 * @author Morvan C.
 */
public class Accueil extends Activity implements ChoixPaysDialogListener, CommentaireDialogListener {

  	// Stocke l'activit� voulue pendant l'initialisation.
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

		// R�cup�re la taille de l'�cran pour configurer les layouts.
		initScreenSize();

		// ??
		setTitleColor(Color.WHITE);

		// Affichage du choix du pays ( premi�re connexion de l'utilisateur ) et Configuration des vignettes.
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
	 *  Affichage de la vue pour le choix du pays lors de la premi�re connexion.
	 */
	public void choixPaysFirstConnexion() 
	{
		// V�rifie l'�tape de configuration. ( true : pas de configuration effectu�e )
		if(checkConfigurationFromBDD()) {
			// Configuration des vignettes sur la home.
			configurationLayoutV2(false);

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			// Sauvegarde des dimensions du terminal pour l'affichage de la bo�te de dialogue.
			Editor editor = preferences.edit();
			editor.putFloat(Preferences.SIZE_DEVICE, initScreenSize());
			editor.commit();

			// Configuration du pays de l'expatri� en cours..
			/**DialogFragment dialog = new ChoixPaysDialogFragment();
			dialog.show(getFragmentManager(), "ChoixPaysDialogFragment");*/

			// Configuration du pays dans les pr�f�rences.
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
     * V�rification s'il y a eu une initalisation du pays.
     */
    public boolean checkConfigurationFromBDD() 
 	{
    	// La valeur 1 signifie que le pays est initialis�.
    	String where = UtilisateurProvider.KEY_INITIALISE + " = 1";
 		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, where, null, null);

 		// Configuration valide.
 		if(cursor.moveToFirst()) { return false; }
 		// Pas de configuration effectu�e.
 		else { return true; }
 	}

    /**
     * V�rification s'il y a eu une initalisation du pays.
     */
    public boolean checkSaveFromBDD() 
 	{
    	// La valeur 1 signifie que le pays est initialis�.
    	String where = UtilisateurProvider.KEY_INITIALISE + " = 0";
 		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, where, null, null);

 		// Configuration valide.
 		if(cursor.moveToFirst()) { return false; }
 		// Pas de configuration effectu�e.
 		else { return true; }
 	}

 	/**
 	 * Sauvegarde de la tentative d'initalisation r�alis�e Et Aucun pays s�lectionn�.
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

	    // V�rification initialisation de l'utilisateur.
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

 		// Mise � jour du pays.
 		if(cursor.moveToFirst()) {
 			majPaysToBDD(_nomPays);
 		}
 		// Sinon Cr�ation d'un id et pays utilisateur.
 		else {
 			ContentValues values = new ContentValues();
 			values.put(UtilisateurProvider.KEY_PAYS, _nomPays);
 	 		values.put(UtilisateurProvider.KEY_INITIALISE, 1);

 			contentResolver.insert(UtilisateurProvider.CONTENT_UTILISATEUR_URI, values);
 		}
	}

 	/**
 	 * Mise � jour du pays de l'utilisateur.
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

	    // V�rification initialisation.
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, whereArgs, null, null);

 		// Sinon Cr�ation de la ligne utilisateur.
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
 		// Si des commentaires sont d�j� enregistr�s, on l'ajoute aux commentaires pr�c�dents.
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
     * Configure la vignette personnalis�e Informations en fonction du pays.
     * [Design by Ang�lique]
     */
    public void configurationLayoutV2(boolean verificationPays) 
    {
    	if(!nomPaysUtilisateur.equalsIgnoreCase("inconnu") 
    	&& !nomPaysUtilisateur.equalsIgnoreCase("Choix du pays")
    	&& !nomPaysUtilisateur.equalsIgnoreCase("") 
    	&& !nomPaysUtilisateur.equalsIgnoreCase(" ")) {
    		setTitle("France-Expatri�s : " + nomPaysUtilisateur);
    	}

  		// Adpate la taille des vignettes en fonction de la taille de l'�cran.
  	    if(widthScreen > 2.5) { setContentView(R.layout.accueil_v2_tablette); }
  	    else { setContentView(R.layout.accueil_v2_smartphone); }

		ImageView imageviewRenseignements = (ImageView) findViewById(R.id.vignette_renseignements);
		ImageView imageviewCorrespondants = (ImageView) findViewById(R.id.vignette_correspondants);
		ImageView imageviewTwitter = 		(ImageView) findViewById(R.id.vignette_fluxtwitter);
		ImageView imageviewFacebook = 		(ImageView) findViewById(R.id.vignette_fluxfacebook);
		ImageView imageviewGuides = 		(ImageView)	findViewById(R.id.vignette_guides);
		//ImageView imageviewBlogFE = 		(ImageView) findViewById(R.id.vignette_blog);

		// Pays initialis�.
		if(verificationPays) {
			// -> Envoi de la vignette personnalis�e en fonction du Pays et du Terminal. ( tablette )
			if(widthScreen > 2.5) {
				// Design par d�faut initialis�.
				if(nomPaysUtilisateur.equalsIgnoreCase("Allemagne")) {
					// Design � mettre � jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Argentine")) {
					// Design � mettre � jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Australie")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Autriche")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Canada")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Espagne")) {
					imageviewRenseignements.setImageResource(R.drawable.vignette_v2_infos_espagne_800x1280);				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Etats-Unis")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Liban")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Luxembourg")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Mexique")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Royaume-Uni")) {
					// Design � mettre � jour..				
				}
			}
			// Version smartphone.
			else {
				if(nomPaysUtilisateur.equalsIgnoreCase("Allemagne")) {
					// Design � mettre � jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Argentine")) {
					// Design � mettre � jour..
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Australie")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Autriche")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Canada")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Espagne")) {
					imageviewRenseignements.setImageResource(R.drawable.vignette_v2_infos_espagne_480x854);				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Etats-Unis")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Liban")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Luxembourg")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Mexique")) {
					// Design � mettre � jour..				
				} else if(nomPaysUtilisateur.equalsIgnoreCase("Royaume-Uni")) {
					// Design � mettre � jour..				
				} 
			}

			// Configuration de l'�v�nement des vignettes.
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
			// Envoi de la vignette non personnalis�e.
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
     * R�cup�re la taille de l'�cran pour configurer les layouts.
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
	 * M�thodes du choix du pays.
	 */
	@Override
	public void onChoixPaysPositiveClick(DialogFragment paysDialog) 
	{
		// R�cup�ration du pays choisi.
		ChoixPaysDialogFragment cpdf = (ChoixPaysDialogFragment) paysDialog;
		// Mise � jour du param�tre.
		nomPaysUtilisateur = cpdf.getPays();
		// Sauvegarde BDD du pays.
		savePaysToBDD(cpdf.getPays());

		// Envoi du layout � jour.
		configurationLayoutV2(true);
	}

	@Override
	public void onChoixPaysNegativeClick(DialogFragment dialog) 
	{
		Toast.makeText(getApplication(), "Vous n'avez s�lectionner aucun pays, acc�der aux pr�f�rences pour le s�lectionner", Toast.LENGTH_LONG).show();
		nomPaysUtilisateur = "inconnu";

		// Configuration du layout d'erreur.
		configurationLayoutV2(false);
	}

	@Override
	public void onChoixPaysFinishEditDialog(String inputText) { }

 	/**
 	 * Mise � jour du commentaire / signalement d'un bug par l'utilisateur.
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
	 * M�thodes du commentaire / signalement d'un bug de l'utilisateur � enregistrer.
	 */
	@Override
	public void onCommentairePositiveClick(DialogFragment dialog) {
 	   Toast.makeText(getApplication(), "Merci, le commentaire a �t� envoy� ! ", Toast.LENGTH_LONG).show();
 	   CommentaireDialogFragment cdf = (CommentaireDialogFragment) dialog;
 	   saveCommentaireToBDD(cdf.getCommentaire());
	}

	@Override
	public void onCommentaireNegativeClick(DialogFragment dialog) { }

	@Override
	public void onCommentaireFinishEditDialog(String inputText) { }

}