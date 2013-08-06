package com.lanouveller.franceexpatries;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.lanouveller.franceexpatries.CommentaireDialogFragment.CommentaireDialogListener;

public class Blog extends SlidingActivity implements CommentaireDialogListener {

  	private static final int SHOW_PREFERENCES = 1;

  	private BlogReceiver receiver;

  	private SlidingMenu slidingMenu;

  	// Gestion des informations de l'article.
  	private ArrayAdapter<Article> adapterArticle;
  	private ArrayList<Article> arraylistArticle;

  	private Article selection;

  	// Gestion [refresh].
	private Menu optionsMenu;

  	private float heightScreen;
  	private float widthScreen;

  	/**
  	 * [Blog] - Réception du diffuseur.
  	 */
  	public class BlogReceiver extends BroadcastReceiver {

  		public static final String BLOG_REFRESHED = "com.lanouveller.franceexpatries.BLOG_REFRESHED";

  		@Override
    	public void onReceive(Context context, Intent intent) 
    	{
    		initBlogFromBDD();
    	}
  	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Initialise la taille du terminal.
  		initScreenSize();

  		// Adapte le menu en fonction de la taille du terminal.
  		configurationMenuSliding();
  		configurationItemsMenuSliding();

  		configurationLayout();

	  	// Récupère les données du serveur.
  		launchService();
	}

    @Override
    public void onResume() 
    {
    	receiver = new BlogReceiver();
    	registerReceiver(receiver, new IntentFilter(BlogService.BLOG_REFRESHED));

    	super.onResume();
    }

    @Override
    public void onPause() 
    {
    	unregisterReceiver(receiver);
    	super.onPause();
    }

	/**
	 * [Blog] - Chargement de la BDD.
	 */
	public void initBlogFromBDD() 
	{
		arraylistArticle.clear();

		Cursor cursor = getContentResolver().query(BlogProvider.CONTENT_BLOG_URI, null, null, null, null);

		if(cursor.moveToFirst()) {
			do {
				String titre = 			cursor.getString(BlogProvider.TITRE_COLUMN);
				String description = 	cursor.getString(BlogProvider.DESCRIPTION_COLUMN);
				String contenu = 		cursor.getString(BlogProvider.CONTENU_COLUMN);
				String date = 			cursor.getString(BlogProvider.DATE_COLUMN);
				String lien = 			cursor.getString(BlogProvider.LIEN_COLUMN);

				Article article = new Article(titre, description, contenu, date, lien);

				ajoutArticle(article);

			} while(cursor.moveToNext());
		}
		else {
			Toast.makeText(getApplicationContext(), "Le temps de chargement peut prendre quelques temps,  veuillez patienter", Toast.LENGTH_LONG).show();

			// Lancement timer ? ProgressBar ?
			launchService();
		}
    }

  	/**
  	 * Blog - Ajout d'un article.
  	 */
	public void ajoutArticle(Article _article) 
	{
		arraylistArticle.add(_article);
		adapterArticle.notifyDataSetChanged();
	}

    /**
     * [Blog] - Configuration du layout et des variables.
     */
    public void configurationLayout() 
    {
  		setContentView(R.layout.blog);

  		// Navigation.
  		setTitle("France-Expatriés - Blog");

		// Configuration du titre de l'activité.
		TextView titreBlogTv = (TextView) findViewById(R.id.titre_blog);
		titreBlogTv.setText("Articles du blog - France-Expatriés");

		arraylistArticle = new ArrayList<Article>();

		// Configuration de la ListView et du titre.
  		if(widthScreen > 2.5) {
  			titreBlogTv.setTextAppearance(this, R.style.titreInformationTablette);
  	  		adapterArticle = new ArrayAdapter<Article>(this, R.layout.list_items_blog_tablette, arraylistArticle);			
  		}
  		else {
  			titreBlogTv.setTextAppearance(this, R.style.titreInformationSmartphone);
  	  		adapterArticle = new ArrayAdapter<Article>(this, R.layout.list_items_blog_smartphone, arraylistArticle);	
  		}

		// Configuration de la ListView.
		ListView listview = (ListView) findViewById(R.id.listview);
  		listview.setAdapter(adapterArticle);  	

  		// Configuration d'un item ( article ) cliquable.
  		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> _adapterView, View _view, int _index, long _arg) {
				selection = arraylistArticle.get(_index);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(selection.getLien())));
			}
		});
    }

    /**
     * Configuration du layout et des variables.
     
    public void configurationLayoutAdapter() 
    {
  		setContentView(R.layout.blog);
  		
  		String[] fromColumns = {BlogProvider.KEY_TITRE, BlogProvider.KEY_DESCRIPTION};
  		int[] toViews = {R.id.t, R.id.phone_number};

  		// Configuration de la ListView.
  		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.person_name_and_number, cursor, fromColumns, toViews, 0);
  		ListView listView = getListView();
  		listView.setAdapter(adapter);

  		ListView listview = (ListView) findViewById(R.id.listview);
  		adapter = new ArrayAdapter<Article>(this, R.layout.list_items_blog, arraylist);
    }*/

  	private void launchService() 
  	{
  		startService(new Intent(this, BlogService.class));
  	}

    /**
     * [Blog] - Initialise la taille de l'écran.
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
     * [Blog] - Adapte la taille du 'Menu Sliding' en fonction de la taille de l'écran.
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
     * [Blog] - Configuration des items du Menu Sliding.
     */
    public void configurationItemsMenuSliding() 
    {
    	TextView itemAccueil = (TextView) findViewById(R.id.item_accueil);
    	TextView itemInformation = (TextView) findViewById(R.id.item_informations);
    	TextView itemCorrespondant = (TextView) findViewById(R.id.item_correspondants);
    	TextView itemTwitter = (TextView) findViewById(R.id.item_twitter);
    	TextView itemFacebook = (TextView) findViewById(R.id.item_facebook);
    	TextView itemBlog = (TextView) findViewById(R.id.item_blog);

    	// Configuration des Items:

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
				Intent intent = new Intent(Blog.this, Accueil.class);
				// Rappel l'activité 'Accueil' déjà lancée.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

  	    itemInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Blog.this, Renseignements.class);
	    		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemCorrespondant.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Blog.this, Correspondants.class);
	    		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});

  	    itemTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Blog.this, FEXTwitter.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Blog.this, FEXFacebook.class);
				startActivity(intent);
				finish();
			}
		});

  	    itemBlog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Blog.this, Blog.class);
	    		intent.putExtra("CALLING_CLASS", "Accueil");
				startActivity(intent);
				finish();
			}
		});
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
	 * [Blog] - Création du menu.
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
  	    this.optionsMenu = menu;

    	// Insertion du 'Menu overflow'.
    	getMenuInflater().inflate(R.menu.menu_overflow_blog, menu);

      	return super.onCreateOptionsMenu(menu);
    }

    /**
     * [Blog] - Sélection d'un item du menu overflow.
     */
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId()) {
			// Items du 'Menu Overflow'.
			case android.R.id.home: {
				slidingMenu.showMenu();
			} return true;

			case R.id.item_preferences: {
	    		Intent intent = new Intent(this, Preferences.class);
	    		intent.putExtra("CALLING_CLASS", "Blog");
	    		startActivityForResult(intent, SHOW_PREFERENCES);
			} return true;

			/**case R.id.item_commentaire: {
    			DialogFragment dialog = new CommentaireDialogFragment();
    			dialog.show(getFragmentManager(), "CommentaireDialogFragment");
    		} return true;*/

			case R.id.item_refresh: {
	    		Intent intent = new Intent(this, Blog.class);
	    		intent.putExtra("CALLING_CLASS", "Accueil");
	    		startActivity(intent);
	    		finish();
    		} return true;
    	}

    	return false;
    }

 	/**
 	 * [Blog] - [Option] Envoi d'un commentaire - Mise à jour du commentaire de l'utilisateur.
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
 	 * [Blog] - [Option] Sauvegarde du commentaire de l'utilisateur.
 	 */
    public void saveCommentaireToBDD(String _commentaire) 
  	{
	    ContentResolver contentResolver = getContentResolver();
    	String whereArgs = UtilisateurProvider.KEY_COMMENTAIRE + " = ' '" + " OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " = '' OR " + 
    						UtilisateurProvider.KEY_COMMENTAIRE + " IS NULL";

	    // Vérification de l'initialisation de l'id de l'utilisateur.
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
     * [Blog] - [Option] Envoi d'un commentaire - Réception de l'événement.  
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

}
