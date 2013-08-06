package com.lanouveller.franceexpatries;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
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
public class Correspondants extends SlidingActivity implements CommentaireDialogListener {

	// Retour de l'activité Préférences.
  	private static final int SHOW_PREFERENCES = 1;
	// Réception de la fenêtre de la fiche du correspondant.
  	private static final int CORRESPONDANT_FICHE_DIALOG = 2;

  	private SlidingMenu slidingMenu;

  	private CorrespondantReceiver receiverCorrespondant;
  	private CorrespondantImagesReceiver receiverCorrespondantImages;

  	// Pays de l'utilisateur.
  	private String nomPaysUtilisateur;

  	private int nbCorrespondantsFictifs;
  	private int nbCorrespondants;

  	// Configuration des données des correspondants du pays.
	private CorrespondantsAdapter adapterCorrespondant;
  	private ArrayList<Correspondant> arraylistCorrespondant;

  	// Correspondant sélectionné lors de l'affichage de sa fiche.
  	private Correspondant selectionCorrespondant;
  	private String noCorrespondantValue = "";

  	// Sharing.
  	private ShareActionProvider mShareActionProvider;

  	// Gestion [refresh].
	private Menu optionsMenu;

  	// Variables de vues [layout principal].
  	private TextView titreCorrespondantsTv;

  	// Variables de vues [layout Dialog].
  	private ScrollView sV;

	private LinearLayout mainLl;
	private LinearLayout titreLl;

	private LinearLayout prenomLl;
	private LinearLayout emailLl;
	private LinearLayout paysLl;
	private LinearLayout villeLl;
	private LinearLayout dateNaissanceLl;
	private LinearLayout professionLl;

	private ImageView iV;

  	private TextView nomTv;
  	private TextView prenomTv;
  	private TextView emailTv;
  	private TextView paysTv;
  	private TextView villeTv;
  	private TextView dateNaissanceTv;
	private TextView professionTv;

	private TextView libellePrenomTv ;
	private TextView libelleEmailTv ;
	private TextView libellePaysTv ;
	private TextView libelleVilleTv ;
	private TextView libelleDateNaissanceTv;
	private TextView libelleProfessionTv;

  	// Configuration multi-écrans.
  	private float heightScreen;
  	private float widthScreen;

  	/**
  	 * Gestion du service.
  	 */
  	public class CorrespondantReceiver extends BroadcastReceiver {
  		public static final String CORRESPONDANT_REFRESHED = "com.lanouveller.franceexpatries.CORRESPONDANT_REFRESHED";

    	@Override
    	public void onReceive(Context context, Intent intent) 
    	{
        	initCorrespondantsFromBDD(nomPaysUtilisateur);

			// Désactive la ProgressBar.
      		setRefreshActionButtonState(false);
    	}
   	}

  	public class CorrespondantImagesReceiver extends BroadcastReceiver {
  		public static final String CORRESPONDANT_IMAGES = "com.lanouveller.franceexpatries.CORRESPONDANT_IMAGES";

    	@Override
    	public void onReceive(Context context, Intent intent) 
    	{
        	initCorrespondantsFromBDD(nomPaysUtilisateur);

			// Désactive la ProgressBar.
      		setRefreshActionButtonState(false);
    	}
   	}

  	/**
  	 * Image du correspondant.
  	 */
  	public static class ImageGestion {
  		private static ArrayList<Bitmap> bms = new ArrayList<Bitmap>();
  		private static ArrayList<String> nomBms = new ArrayList<String>();
  		private static ArrayList<Integer> imagesWidth = new ArrayList<Integer>();
  		private static ArrayList<Integer> imagesHeight = new ArrayList<Integer>();

  		public static void setImageGestion(String _nomImage, Bitmap _bm) 
  		{
  			nomBms.add(_nomImage);
  			bms.add(_bm);
  		}

  		public static void setImageDimensions(int _width, int _height) {
  			imagesWidth.add(_width);
  			imagesHeight.add(_height);
  		}

  		public static void deleteImagesGestion() 
  		{
  			ImageGestion.bms.removeAll(bms);
  		}

  		public static ArrayList<String> getNomImagesGestion() { return ImageGestion.nomBms; }
  		public static ArrayList<Bitmap> getImagesGestion() { return ImageGestion.bms; }
  		public static ArrayList<Integer> getImagesWidth() { return ImageGestion.imagesWidth; }
  		public static ArrayList<Integer> getImagesHeight() { return ImageGestion.imagesHeight; }
  	}

  	@Override
  	public void onCreate(Bundle savedInstanceState) 
  	{	
  		super.onCreate(savedInstanceState);

  		// Récupère le pays de l'utilisateur.
  		initPaysFromBDD();

  		// Récupère la taille du terminal.
  		initScreenSize();

  		// Menu gauche.
  	    configurationMenuSliding();
		configurationItemsMenuSliding();

  		if(checkDataFromBDD()) {
  	  		configurationLayout();

  	  		// Récupère les données (image et autres) du serveur.
  	  		launchServiceImages();
  		}
  		else {
  			noCorrespondants();
  			noCorrespondantValue = "aucun";
  		}
  	}

    @Override 
    public void onResume() 
    {
    	receiverCorrespondant = new CorrespondantReceiver();
    	registerReceiver(receiverCorrespondant, new IntentFilter(CorrespondantService.CORRESPONDANT_REFRESHED));

    	receiverCorrespondantImages = new CorrespondantImagesReceiver();
    	registerReceiver(receiverCorrespondantImages, new IntentFilter(CorrespondantService.CORRESPONDANT_IMAGES));

    	super.onResume();
    }

    @Override
    public void onPause() 
    {
    	unregisterReceiver(receiverCorrespondant);
    	unregisterReceiver(receiverCorrespondantImages);
    	super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
  	    this.optionsMenu = menu;

      	return super.onCreateOptionsMenu(menu);
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
    	 if(checkDataFromBDD()) {
     		// Insertion du 'Menu overflow'.
         	getMenuInflater().inflate(R.menu.menu_overflow_correspondants, menu);

         	// Icône Partage - Récupère le MenuItem avec ShareActionProvider.
         	mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.ab_item_share).getActionProvider();

         	// Initialise le partage.
           	//sharing();

			// Active la ProgressBar.
			setRefreshActionButtonState(true);
     	}
     	else {
     		// Insertion du 'Menu overflow' no data.
         	getMenuInflater().inflate(R.menu.menu_overflow_correspondants_no_data, menu);
     	}

	   return super.onPrepareOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item);

    	switch (item.getItemId()) {
			// Items du 'Menu Overflow'.
			case android.R.id.home: {
				slidingMenu.showMenu();
			} return true;

			case R.id.item_preferences: {
	    		Intent intent = new Intent(this, Preferences.class);
		  		intent.putExtra("CALLING_CLASS", "Correspondants");
	    		startActivityForResult(intent, SHOW_PREFERENCES);
			} return true;

			// Contrainte de devlpt: Créer une plateforme web de collectes des messages utilisateurs.
			/**case R.id.item_commentaire: {
    			DialogFragment dialog = new CommentaireDialogFragment();
    			dialog.show(getFragmentManager(), "CommentaireDialogFragment");
    		} return true;*/

			case R.id.item_refresh: {
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

			case R.id.ab_item_share: {
				// Sharing dialog.
				if(checkDataFromBDD()) {
					if(isOnline()) {
						startActivity(Intent.createChooser(sharing(), "Continuer avec"));
					} else {
						Toast.makeText(getApplicationContext(), "Échec de l'actualisation : veuillez vérifier votre connexion réseau.", Toast.LENGTH_LONG).show();
					}
		  		}
    		} return true;
    	}

    	return false;
    }

    /**
  	 *  Correspondants - Renvoi les données de retour de l'activité lancée.
  	 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);

    	// Activité 'Préférences'.
    	if(requestCode == SHOW_PREFERENCES && resultCode == Activity.RESULT_OK) {
        	// Active la ProgressBar.
      		setRefreshActionButtonState(true);

    		// Chargement du pays à jour.
    		initPaysFromBDD();

      		// Configuration de la navigation.
    		setTitle("Correspondants - " + nomPaysUtilisateur);

      		// Configuration du titre.
    		//titreCorrespondantsTv.setText("Correspondants");

      		invalidateOptionsMenu();

    		// Recharge les données et le layout.
    		if(checkDataFromBDD() && noCorrespondantValue.equalsIgnoreCase("aucun")) {
    			// Remplace le layout précédent. 
      	  		configurationLayout();

    			// MAJ DATA.
    			//launchService();

    			// Chargement des données..
            	initCorrespondantsFromBDD(nomPaysUtilisateur);
    		}
    		// Recharge uniquement les données.
    		else if(checkDataFromBDD()) {
    			// MAJ DATA.
    			//launchService();

    			// Chargement des données..
            	initCorrespondantsFromBDD(nomPaysUtilisateur);
    		}
    		else {
    			noCorrespondants();
      			noCorrespondantValue = "aucun";
    		}

        	// Désactive la ProgressBar.
      		setRefreshActionButtonState(false);
    	}
    }

  	public void initPaysFromBDD() 
  	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

  		while(cursor.moveToNext()) {
  			nomPaysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);
  		}
  	}

  	private void launchService() 
  	{
  		if(isOnline()) {
			Intent intent = new Intent(this, CorrespondantService.class);
			intent.setAction(getPaysFormate(nomPaysUtilisateur));
  			startService(intent);
  		} 
  		else {
  			Toast.makeText(getApplicationContext(), "Vous êtes déconnecté. Veuillez réessayer ultérieurement.", Toast.LENGTH_LONG).show();
  		}
  	}

  	private void launchServiceImages() 
  	{
  		if(isOnline()) {
			Intent intent = new Intent(this, CorrespondantService.class);
			intent.setAction(getPaysFormate(nomPaysUtilisateur));
			intent.putExtra("IMAGES", "IMAGES");
  			startService(intent);
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

  	/**
  	 *  Correspondants - Chargement des données des objets dans l'interface.
  	 */
	private void addCorrespondant(Correspondant _correspondant) 
	{
		arraylistCorrespondant.add(_correspondant);
		adapterCorrespondant.notifyDataSetChanged();
	}

	public void initCorrespondantsFromBDD(String _pays) 
	{
		arraylistCorrespondant.clear();

		// Numero d'identification propre du correspondant par défaut.
		int numero = 0;

		String[] sexes = {"homme", "femme"};
		// Ajout de volume.
		nbCorrespondantsFictifs = 10;
		// Réels.
		nbCorrespondants = 0;

    	String whereArgs = CorrespondantProvider.KEY_PAYS + " = '" + _pays + "'";

		Cursor cursor = getContentResolver().query(CorrespondantProvider.CONTENT_CORRESPONDANT_URI, null, whereArgs, null, null);

		if(cursor.moveToFirst()) {
			do {
				numero = 				cursor.getInt(CorrespondantProvider.NUMERO_COLUMN);
				//String sexe = 		cursor.getString(CorrespondantProvider.SEXE_COLUMN);
				String nom = 			cursor.getString(CorrespondantProvider.NOM_COLUMN);
				String prenom = 		cursor.getString(CorrespondantProvider.PRENOM_COLUMN);
				String email = 			cursor.getString(CorrespondantProvider.EMAIL_COLUMN);
				String pays = 			cursor.getString(CorrespondantProvider.PAYS_COLUMN);
				String ville = 			cursor.getString(CorrespondantProvider.VILLE_COLUMN);
				String dateNaissance = 	cursor.getString(CorrespondantProvider.DATE_NAISSANCE_COLUMN);
				String profession = 	cursor.getString(CorrespondantProvider.PROFESSION_COLUMN);
				String nomImage = 		cursor.getString(CorrespondantProvider.NOM_IMAGE_COLUMN);

				Correspondant correspondant = new Correspondant(numero, "", nom, prenom, email, pays, ville, dateNaissance, profession, nomImage);

				addCorrespondant(correspondant);

			} while(cursor.moveToNext());

			// Remplissage, correspondants fictifs.
			for(int i=0; i<nbCorrespondantsFictifs; i++) {
				Correspondant correspondantsFictifs = new Correspondant((i+1), sexes[i%2], "Nom du correspondant", "Prénom du correspondant", "email@isp.com", 
																"Pays du correspondant", "Ville du correspondant", "01/12/1980", "Profession du correspondant", "");
				addCorrespondant(correspondantsFictifs);
			}

			nbCorrespondants++;
		}
		// Aucun correspondant - Défaut.
		else {
			// Remplissage, correspondants fictifs.
			for(int i=0; i<nbCorrespondantsFictifs; i++) {
				Correspondant correspondantsFictifs = new Correspondant((i+1), sexes[i%2], "Nom du correspondant", "Prénom du correspondant", "email@isp.com", 
																"Pays du correspondant", "Ville du correspondant", "01/12/1980", "Profession du correspondant", "");
				addCorrespondant(correspondantsFictifs);
			}
		}

		// Compteur de correspondants intégré au titre.
		nbCorrespondants += nbCorrespondantsFictifs;

		// Configuration du titre de l'activité.
		String titreCorrespondants = nomPaysUtilisateur + " - " + String.valueOf(nbCorrespondants) + " correspondants";
		titreCorrespondantsTv.setText(titreCorrespondants);

		// Configuration de la navigation.
		setTitle("Correspondants - " + nomPaysUtilisateur);
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
     * Correspondants - Création de la fenêtre de la fiche du correspondant.
     */
    @Override
    public Dialog onCreateDialog(int id) 
    {
    	switch(id) {
    		// Création de la fiche du correspondant dans une fenêtre Dialog.
    		case CORRESPONDANT_FICHE_DIALOG: {
        		AlertDialog.Builder correspondantDialog = new AlertDialog.Builder(this);
        		correspondantDialog.setTitle("Fiche du correspondant");

    			sV = new ScrollView(this);
        		correspondantDialog.setView(sV);
        		sV.addView(mainLl);

        		prenomLl = new LinearLayout(this);
        		emailLl = new LinearLayout(this);
        		paysLl = new LinearLayout(this);
        		villeLl = new LinearLayout(this);
        		dateNaissanceLl = new LinearLayout(this);
       			professionLl = new LinearLayout(this);

        		return correspondantDialog.create();
    		}
    	}

    	return null;
    }

    /**
     * Correspondants - Configuration de la fenêtre de la fiche du correspondant - Construction du layout intégrée au code pour afficher que des éléments remplis.
     * La construction du layout principal est intégrée dans le CorrespondantAdapter.
     */
	@Override
    public void onPrepareDialog(int id, Dialog _dialog) 
    {
    	switch(id) {
    		// Configuration de la fenêtre sur les informations du correspondant [fiche].
    		case CORRESPONDANT_FICHE_DIALOG: {
	    		AlertDialog ficheDialog = (AlertDialog) _dialog;
	    		ficheDialog.setTitle("Détails du correspondant");

	    		// Réinitialise les vues.
	    		cleanLayouts();

	    		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

	    		// Configuration icône de la fenêtre Dialog. - non fonctionnel
	    		if(selectionCorrespondant.getSexe().equalsIgnoreCase("homme")) { ficheDialog.setIcon(R.drawable.icon_fiche_correspondant_homme); }
	    		else { ficheDialog.setIcon(R.drawable.icon_fiche_correspondant_femme); }

	    		// Configuration image du correspondant [fiche].
	    		if(ImageGestion.getImagesGestion() != null) {
	    			boolean hasImage = false;
	    			for(int i=0; i<ImageGestion.getImagesGestion().size(); i++) {
	    				if(ImageGestion.getNomImagesGestion().get(i).equalsIgnoreCase(selectionCorrespondant.getNomImage())) {
	    		    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    		    		imageParams.setMargins(20, 0, 0, 0);
	    		    		iV = new ImageView(this);
	    		    		/**Toast.makeText(getApplicationContext(), "Width : " + String.valueOf(ImageGestion.getImagesWidth().get(i)) + 
	    		    												"Height : " + String.valueOf(ImageGestion.getImagesHeight().get(i)), Toast.LENGTH_LONG).show();*/
	    					if(widthScreen > 2.5) { iV.setImageBitmap(Bitmap.createScaledBitmap(ImageGestion.getImagesGestion().get(i), 300, 400, true)); }
	    					else { iV.setImageBitmap(Bitmap.createScaledBitmap(ImageGestion.getImagesGestion().get(i), 180, 240, true)); }
	    					hasImage = true;
	    				}
	    			}

		    		if(!hasImage && selectionCorrespondant.getSexe().equalsIgnoreCase("homme")) {
			    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			    		imageParams.setMargins(20, 0, 0, 0);
			    		iV = new ImageView(this);
			    		iV.setImageResource(R.drawable.icon_fiche_correspondant_homme);
		    		}
		    		else if(!hasImage && selectionCorrespondant.getSexe().equalsIgnoreCase("femme")) {
			    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			    		imageParams.setMargins(20, 0, 0, 0);
			    		iV = new ImageView(this);
		    			iV.setImageResource(R.drawable.icon_fiche_correspondant_femme);
		    		}
		    		// Contient le nom et l'image du correspondant [fiche].
		    		titreLl.addView(iV);
	    		}

	    		// Configuration du nom du correspondant [fiche].
	    		if(selectionCorrespondant.getNom().equalsIgnoreCase("") || selectionCorrespondant.getNom().equalsIgnoreCase(" ")) ;
	    		else {
		    		nomTv.setText("Nom : " + selectionCorrespondant.getNom());
		    		titreLl.addView(nomTv);
	    		}
	    		// Ajout du premier sous-Layout : nom + photo du correspondant [fiche].
	    		mainLl.addView(titreLl);

	    		// Ajout du 2° sous-Layout : prénom [fiche].
	    		if(selectionCorrespondant.getPrenom().equalsIgnoreCase("") || selectionCorrespondant.getPrenom().equalsIgnoreCase(" ")) ;
	    		else {
	    			libellePrenomTv.setText("Prénom: ");
	    			prenomTv.setText(selectionCorrespondant.getPrenom());

	    			prenomLl.setOrientation(LinearLayout.HORIZONTAL);
	    			prenomLl.setLayoutParams(llp);
	    			prenomLl.addView(libellePrenomTv);
	    			prenomLl.addView(prenomTv);
	    			if(prenomLl.getParent() == null) mainLl.addView(prenomLl);	
	    		}

	    		// Ajout du 3° sous-Layout : email [fiche].
	    		if(selectionCorrespondant.getEmail().equalsIgnoreCase("") || selectionCorrespondant.getEmail().equalsIgnoreCase(" ")) ;
	    		else {
		    		libelleEmailTv.setText("Email: ");
		    		emailTv.setText(selectionCorrespondant.getEmail());

		    		emailLl.setOrientation(LinearLayout.HORIZONTAL);
		    		emailLl.setLayoutParams(llp);
		    		emailLl.addView(libelleEmailTv);
		    		emailLl.addView(emailTv);
		    		Linkify.addLinks(emailTv, Linkify.EMAIL_ADDRESSES);
		    		if(emailLl.getParent() == null) mainLl.addView(emailLl);
	    		}

	    		// Ajout du 4° sous-Layout : pays [fiche].
	    		if(selectionCorrespondant.getPays().equalsIgnoreCase("") || selectionCorrespondant.getPays().equalsIgnoreCase(" ")) ;
	    		else {
		    		libellePaysTv.setText("Pays: ");
		    		paysTv.setText(selectionCorrespondant.getPays());

		    		paysLl.setOrientation(LinearLayout.HORIZONTAL);
		    		paysLl.setLayoutParams(llp);
		    		paysLl.addView(libellePaysTv);
		    		paysLl.addView(paysTv);
		    		if(paysLl.getParent() == null) mainLl.addView(paysLl);
	    		}

	    		// Ajout du 5° sous-Layout : ville [fiche].
	    		if(selectionCorrespondant.getVille().equalsIgnoreCase("") || selectionCorrespondant.getVille().equalsIgnoreCase(" ")) ;
	    		else {
		    		libelleVilleTv.setText("Ville: ");
		    		villeTv.setText(selectionCorrespondant.getVille());

		    		villeLl.setOrientation(LinearLayout.HORIZONTAL);
		    		villeLl.setLayoutParams(llp);
		    		villeLl.addView(libelleVilleTv);
		    		villeLl.addView(villeTv);
		    		if(villeLl.getParent() == null) mainLl.addView(villeLl);
	    		}

	    		// Ajout du 6° sous-Layout : date naissance [fiche].
	    		if(selectionCorrespondant.getDateNaissance().equalsIgnoreCase("") || selectionCorrespondant.getDateNaissance().equalsIgnoreCase(" ")) ;
	    		else {
		    		libelleDateNaissanceTv.setText("Date de naissance: ");
		    		dateNaissanceTv.setText(selectionCorrespondant.getDateNaissance());

		    		dateNaissanceLl.setOrientation(LinearLayout.HORIZONTAL);
		    		dateNaissanceLl.setLayoutParams(llp);
		    		dateNaissanceLl.addView(libelleDateNaissanceTv);
		    		dateNaissanceLl.addView(dateNaissanceTv);
		    		if(dateNaissanceLl.getParent() == null) mainLl.addView(dateNaissanceLl);
	    		}

	    		// Ajout du 7° sous-Layout : profession [fiche].
	    		if(selectionCorrespondant.getProfession().equalsIgnoreCase("") || selectionCorrespondant.getProfession().equalsIgnoreCase(" ")) ;
	    		else {
		    		libelleProfessionTv.setText("Profession: ");
		    		professionTv.setText(selectionCorrespondant.getProfession());

		    		professionLl.setOrientation(LinearLayout.HORIZONTAL);
		    		professionLl.setLayoutParams(llp);
		    		professionLl.addView(libelleProfessionTv);
		    		professionLl.addView(professionTv);
		    		if(professionLl.getParent() == null) mainLl.addView(professionLl);
	    		}

    		} break;
    	}
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public int sizeOf(Bitmap data) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        }
        else {
            return data.getByteCount();
        }
    }

    public void cleanLayouts() 
    {
		mainLl.removeAllViews();
		titreLl.removeAllViews();
		prenomLl.removeAllViews();
		emailLl.removeAllViews();
		paysLl.removeAllViews();
		villeLl.removeAllViews();
		dateNaissanceLl.removeAllViews();
		professionLl.removeAllViews();

		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		imageParams.setMargins(20, 0, 0, 0);
		iV = new ImageView(this);
		iV.setLayoutParams(imageParams);

		nomTv.setText("");
		prenomTv.setText("");
		emailTv.setText("");
		paysTv.setText("");
		villeTv.setText("");
		dateNaissanceTv.setText("");
		professionTv.setText("");	
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

    	String url = "http://france-expatries.com/liste_correspondants";

    	// Add data to the intent, the receiving app will decide what to do with it.
    	intent.putExtra(Intent.EXTRA_SUBJECT, "France-Expatriés: " + nomPaysUtilisateur + " - Correspondants");
    	intent.putExtra(Intent.EXTRA_TEXT, "France-Expatriés. " + nomPaysUtilisateur + " - Correspondants. " + url);

    	// Lancement du partage via 'dialog menu apps'.
    	/**mShareActionProvider.setShareIntent(intent);*/

		return intent;
    }

    /**
     * Correspondants - Layout par défaut.
     */
    public void configurationLayout() 
    {
    	setContentView(R.layout.correspondants_fragment);

  		// Active, affiche de la touche 'retour' dans l'Actionbar et supprime l'icône.
	    /**ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);*/

		// Configuration de la navigation.
  		setTitle("Correspondants - " + nomPaysUtilisateur);

		// Layout principal de la fenêtre Dialog.
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		mainLl = new LinearLayout(this);
		mainLl.setBackgroundColor(Color.WHITE);
		mainLl.setOrientation(LinearLayout.VERTICAL);
		mainLl.setLayoutParams(llp);

		// Premier sous-Layout : nom + photo du correspondant.
		titreLl = new LinearLayout(this);

		// Configuration des paramètres des vues.
		LinearLayout.LayoutParams llTitrep = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		llTitrep.setMargins(10, 20, 0, 10);

		LinearLayout.LayoutParams nomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		nomParams.setMargins(60, 10, 0, 0);

		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		imageParams.setMargins(20, 0, 0, 0);

		LinearLayout.LayoutParams libelleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		libelleParams.setMargins(10, 10, 0, 10);

		LinearLayout.LayoutParams contenuParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		contenuParams.setMargins(20, 10, 0, 10);

		// Configuration du sous-layout ( image + nom ) de la fenêtre Dialog d'une fiche d'un correspondant.
		titreLl.setOrientation(LinearLayout.HORIZONTAL);
		titreLl.setLayoutParams(llTitrep);

		// Configuration du titre de l'activité.
  		titreCorrespondantsTv = (TextView) findViewById(R.id.titre_correspondants);
  		//titreCorrespondantsTv.setText("Correspondants");

		// Configuration de l'image du correspondant.
		iV = new ImageView(this);

		// Configuration des TextViews.
		nomTv = new TextView(this);
		prenomTv = new TextView(this);
		emailTv = new TextView(this);
		paysTv = new TextView(this);
		villeTv = new TextView(this);
		dateNaissanceTv = new TextView(this);
		professionTv = new TextView(this);

		libellePrenomTv = new TextView(this);
		libelleEmailTv = new TextView(this);
		libellePaysTv = new TextView(this);
		libelleVilleTv = new TextView(this);
		libelleDateNaissanceTv = new TextView(this);
		libelleProfessionTv = new TextView(this);

		// Configuration du titre [layout principal].
		titreCorrespondantsTv.setLayoutParams(libelleParams);
		
		// Configuration de l'image du correspondant [fiche].
		iV.setLayoutParams(imageParams);

		// Configuration des TextViews [fiche].
		nomTv.setLayoutParams(nomParams);
		nomTv.setTextColor(getResources().getColor(R.color.black));

		// Initialisation des paramètres.
		libellePrenomTv.setLayoutParams(libelleParams);
		libelleEmailTv.setLayoutParams(libelleParams);
		libellePaysTv.setLayoutParams(libelleParams);
		libelleVilleTv.setLayoutParams(libelleParams);
		libelleDateNaissanceTv.setLayoutParams(libelleParams);
		libelleProfessionTv.setLayoutParams(libelleParams);
		prenomTv.setLayoutParams(contenuParams);
		emailTv.setLayoutParams(contenuParams);
		paysTv.setLayoutParams(contenuParams);
		villeTv.setLayoutParams(contenuParams);
		dateNaissanceTv.setLayoutParams(contenuParams);
		professionTv.setLayoutParams(contenuParams);

		// Couleur.
		libellePrenomTv.setTextColor(getResources().getColor(R.color.black));
		libelleEmailTv.setTextColor(getResources().getColor(R.color.black));
		libellePaysTv.setTextColor(getResources().getColor(R.color.black));
		libelleVilleTv.setTextColor(getResources().getColor(R.color.black));
		libelleDateNaissanceTv.setTextColor(getResources().getColor(R.color.black));
		libelleProfessionTv.setTextColor(getResources().getColor(R.color.black));
		prenomTv.setTextColor(getResources().getColor(R.color.black));
		emailTv.setTextColor(getResources().getColor(R.color.black));
		paysTv.setTextColor(getResources().getColor(R.color.black));
		villeTv.setTextColor(getResources().getColor(R.color.black));
		dateNaissanceTv.setTextColor(getResources().getColor(R.color.black));
		professionTv.setTextColor(getResources().getColor(R.color.black));

		// Optimisé selon la taille du device.
		if(widthScreen > 2.5) {
	  		titreCorrespondantsTv.setTextAppearance(this, R.style.titreInformationTablette);
			libellePrenomTv.setTextSize(20);
			libelleEmailTv.setTextSize(20);
			libellePaysTv.setTextSize(20);
			libelleVilleTv.setTextSize(20);
			libelleDateNaissanceTv.setTextSize(20);
			libelleProfessionTv.setTextSize(20);
			nomTv.setTextSize(20);
			prenomTv.setTextSize(20);
			emailTv.setTextSize(20);
			paysTv.setTextSize(20);
			villeTv.setTextSize(20);
			dateNaissanceTv.setTextSize(20);
			professionTv.setTextSize(20);
		}
		else {
	  		titreCorrespondantsTv.setTextAppearance(this, R.style.titreInformationSmartphone);
			libellePrenomTv.setTextSize(10);
			libelleEmailTv.setTextSize(10);
			libellePaysTv.setTextSize(10);
			libelleVilleTv.setTextSize(10);
			libelleDateNaissanceTv.setTextSize(10);
			libelleProfessionTv.setTextSize(10);
			nomTv.setTextSize(10);
			prenomTv.setTextSize(10);
			emailTv.setTextSize(10);
			paysTv.setTextSize(10);
			villeTv.setTextSize(10);
			dateNaissanceTv.setTextSize(10);
			professionTv.setTextSize(10);
		}

  		// Configuration de la ListView de Correspondants [layout principal].
    	FragmentManager fragmentManager = getFragmentManager();
    	CorrespondantFragment correspondantFragment = (CorrespondantFragment) fragmentManager.findFragmentById(R.id.correspondants_listview);
  		arraylistCorrespondant = new ArrayList<Correspondant>();
  		if(widthScreen > 2.5) { adapterCorrespondant = new CorrespondantsAdapter(this, R.layout.correspondants_listitems_tablette, arraylistCorrespondant); }
  		else { adapterCorrespondant = new CorrespondantsAdapter(this, R.layout.correspondants_listitems, arraylistCorrespondant); }
  		correspondantFragment.setListAdapter(adapterCorrespondant);

  		// Configuration d'un item 'Correspondants' cliquable.
  		correspondantFragment.getListView().setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> _adapterView, View _view, int _position, long _arg) {
				// obj selectionné initialisé. 
				selectionCorrespondant = arraylistCorrespondant.get(_position);
				// Envoi de la fenêtre Dialog avec le correspondant demandé.
				showDialog(CORRESPONDANT_FICHE_DIALOG);
			}
		});
    }

    /**
     * Correspondants - [Optimisation devices] Récupère les dimensions de l'écran.
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

	public boolean checkDataFromBDD() 
	{
  		try {
  			Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

	  		while(cursor.moveToNext()) {
	  			if(cursor.getString(UtilisateurProvider.DISPO_CORRESPONDANTS_COLUMN).equalsIgnoreCase("correspondant_disponible")) {
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

	public ImageView getImageCorrespondant() { return iV; }

    /**
     * Correspondants - Configuration des items du 'Menu Sliding'.
     */
    public void configurationItemsMenuSliding() 
    {
    	TextView itemAccueil = (TextView) findViewById(R.id.item_accueil);
    	TextView itemInformation = (TextView) findViewById(R.id.item_informations);
    	TextView itemCorrespondant = (TextView) findViewById(R.id.item_correspondants);
    	TextView itemTwitter = (TextView) findViewById(R.id.item_twitter);
    	TextView itemFacebook = (TextView) findViewById(R.id.item_facebook);
    	TextView itemBlog = (TextView) findViewById(R.id.item_blog);

    	// Configuration des items.

  	    // Adapter la taille du texte en fonction de la taille de l'écran.
  	    if(widthScreen > 2.5) {
  	    	itemAccueil.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemInformation.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemCorrespondant.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemTwitter.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemFacebook.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    	itemBlog.setTextAppearance(this, R.style.itemsMenuSlidingTablette);
  	    }
  	    else {
  	    	itemAccueil.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemInformation.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemCorrespondant.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemTwitter.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemFacebook.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    	itemBlog.setTextAppearance(this, R.style.itemsMenuSlidingSmartphone);
  	    }

  		itemAccueil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, Accueil.class);
				// Rappel l'activité 'Accueil' déjà lancée.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, Renseignements.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, Correspondants.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, FEXTwitter.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, FEXFacebook.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemBlog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Correspondants.this, Blog.class);
		  		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});
    }

    /**
     * Correspondants - Configuration du 'Menu Sliding'. A redefinir par la librairie native du SDK Androïd.
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

    	// Affichage bouton home.
		ActionBar actionBar = getActionBar();
	  	actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
 	 * Correspondants - [Option] Sauvegarde du commentaire de l'utilisateur.
 	 */
    public void saveCommentaireToBDD(String _commentaire) 
  	{
	    ContentResolver contentResolver = getContentResolver();
    	String whereArgs = UtilisateurProvider.KEY_COMMENTAIRE + " = ' '" + " OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " = '' OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " is null";

	    // Vérification initialisation des données utilisateur.
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, whereArgs, null, null);

 		// Sinon Création de l'id de l'utilisateur.
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
 	 * Correspondants - [Option] Envoi d'un commentaire - Mise à jour du commentaire de l'utilisateur.
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
     * Correspondants - [Option] Envoi d'un commentaire - Réception de l'événement.  
     */
	@Override
	public void onCommentairePositiveClick(DialogFragment commentaireDialog) 
	{
 	   Toast.makeText(getApplication(), "Merci, le commentaire a été envoyé ! ", Toast.LENGTH_LONG).show();
 	   CommentaireDialogFragment cdf = (CommentaireDialogFragment) commentaireDialog;
 	   saveCommentaireToBDD(cdf.getCommentaire());
	}

	@Override
	public void onCommentaireNegativeClick(DialogFragment dialog) { }

	@Override
	public void onCommentaireFinishEditDialog(String inputText) { }

    /**
     * Correspondants - Layout affichant aucun correspondant.
     */
    public void noCorrespondants() 
    {
		setTitle("Correspondants - " + nomPaysUtilisateur);

    	LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 50, 50, 50);

    	TextView tv = new TextView(getApplication());
        tv.setLayoutParams(llp);
        tv.setTextColor(Color.BLACK);
    	tv.setText("Aucun correspondant n'est disponible pour ce pays.");
    	tv.setGravity(Gravity.CENTER);

    	if(widthScreen > 2.5) { tv.setTextAppearance(getApplicationContext(), R.style.contenuCorrespondantsTablette); } 
    	else { tv.setTextAppearance(getApplicationContext(), R.style.contenuCorrespondantsSmartphone); }

    	LinearLayout ll = new LinearLayout(getApplication());
    	ll.setBackgroundColor(Color.WHITE);
    	ll.addView(tv);

    	setContentView(ll);
    }

}