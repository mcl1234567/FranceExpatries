package com.lanouveller.franceexpatries;

import java.util.Calendar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lanouveller.franceexpatries.ChoixPaysDialogFragment.ChoixPaysDialogListener;
import com.lanouveller.franceexpatries.CommentaireDialogFragment.CommentaireDialogListener;

/**
 * Si l'utilisateur n'a pas configuré un pays alors redirection.
 */
public class Erreur extends Activity implements ChoixPaysDialogListener, CommentaireDialogListener {

	boolean hasPays = false;

	// Configuration multi-devices.
	private float heightScreen;
	private float widthScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		initScreenSize();

		// Configuration du layout.
		setContentView(R.layout.erreur);

		TextView tv = (TextView) findViewById(R.id.erreur_tv);
		if(widthScreen > 2.5) { tv.setTextAppearance(this, R.style.contenuErreurTablette); }
		else { tv.setTextAppearance(this, R.style.contenuErreurSmartphone); }
		tv.setText("Pour configurer un pays, accéder au menu dans la barre d\'action, et sélectionner Préférences.");

		final Intent intent = new Intent(this, Preferences.class);
		intent.putExtra("CALLING_CLASS", "Accueil");

		Button bouton = (Button) findViewById(R.id.erreur_bouton);
		bouton.setTextColor(getResources().getColor(R.color.black));
		bouton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View view) {
				/**configurePays();*/
				startActivity(intent);
				finish();
			}
		});

		/**configurePays();*/
	}

	/**
	 *  Affichage de la fenêtre pour le choix du pays si aucun pays n'a été initialisé.
	 */
	public void configurePays() 
	{
		// Vérifie l'initialisation du pays.
		if(!hasPaysUtilisateur()) {
			// Aucun pays initialisé.
			DialogFragment dialog = new ChoixPaysDialogFragment();
			dialog.show(getFragmentManager(), "ChoixPaysDialogFragment");
		} 
		else {
			Toast.makeText(getApplication(), "Pays déjà initialisé", Toast.LENGTH_LONG).show();
		}
	}

    /**
     * Vérification de l'initalisation du pays.
     */
    public boolean hasPaysUtilisateur() 
 	{
 		ContentResolver contentResolver = getContentResolver();
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

 		if(cursor.moveToFirst()) {
 			do {
 				String paysUtilisateur = cursor.getString(UtilisateurProvider.PAYS_COLUMN);

 				// Pas de pays
 				if(paysUtilisateur.equalsIgnoreCase("inconnu") 
 				|| paysUtilisateur.equalsIgnoreCase("") 
 				|| paysUtilisateur.equalsIgnoreCase(" ")) {
 					return false;
 				}
 				// Pays enregistré.
 				else { 
 					return true; 
 				}
 			} while(cursor.moveToNext());
 		}

		// Pas de pays
 		return false;
 	}

    /**
     * Récupère la taille de l'écran pour configurer les layouts.
     */
    public void initScreenSize() 
    {
    	DisplayMetrics metrics = new DisplayMetrics();
  	    getWindowManager().getDefaultDisplay().getMetrics(metrics);

  	    heightScreen = metrics.heightPixels / metrics.xdpi;
  	    widthScreen = metrics.widthPixels / metrics.ydpi;
    }

 	/**
 	 * Sauvegarde du pays de l'utilisateur.
 	 */
    public void sauvegardePaysToBDD(String _nomPays) 
  	{
	    ContentResolver contentResolver = getContentResolver();

	    // Vérification initialisation
 		Cursor cursor = contentResolver.query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

 		// Si la donnée est déjà enregistrée, on l'écrase par la nouvelle.
 		if(cursor.moveToFirst()) {
 			majPaysToBDD(_nomPays);
 		}
 		// Sinon Création de la ligne utilisateur.
 		else {
 			ContentValues values = new ContentValues();
 			values.put(UtilisateurProvider.KEY_PAYS, _nomPays);

 			contentResolver.insert(UtilisateurProvider.CONTENT_UTILISATEUR_URI, values);
 		}
	}

 	/**
 	 * Mise à jour du pays de l'utilisateur.
 	 */
    public void majPaysToBDD(String _nomPays) 
  	{
    	Uri uri = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur/1");
	    ContentResolver contentResolver = getContentResolver();
		ContentValues values = new ContentValues();

	    values.put(UtilisateurProvider.KEY_PAYS, _nomPays);
		contentResolver.update(uri, values, null, null);
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
	 * Méthodes du choix du pays.
	 */
	@Override
	public void onChoixPaysPositiveClick(DialogFragment dialog) 
	{
		// Récupération du pays choisi.
		ChoixPaysDialogFragment cpdf = (ChoixPaysDialogFragment) dialog;
		sauvegardePaysToBDD(cpdf.getPays());
 		setResult(RESULT_OK);
 		finish();
	}

	@Override
	public void onChoixPaysNegativeClick(DialogFragment dialog) 
	{
		Toast.makeText(getApplication(), "Aucun pays selectionné", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onChoixPaysFinishEditDialog(String inputText) { }

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
