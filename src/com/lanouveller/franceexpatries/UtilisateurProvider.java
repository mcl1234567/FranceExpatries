package com.lanouveller.franceexpatries;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UtilisateurProvider extends ContentProvider {

	public static final Uri CONTENT_UTILISATEUR_URI = Uri.parse("content://com.lanouveller.provider.utilisateur/utilisateur");

  	@Override
  	public boolean onCreate() 
  	{
  		DatabaseHelper dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
  		database = dbHelper.getWritableDatabase();

    	return (database == null) ? false : true;
  	}

  	/**
  	 *  Fonction de requête.
  	 */
  	@Override
  	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) 
  	{
  		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

  		qb.setTables(TABLE);

  		// S'il s'agit d'une requête sur une ligne, on limite le résultat.
  		switch(uriMatcher.match(uri)) {
      		case OBJET_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1)); break;
      		default: break;
  		}

  		// Si aucun ordre de tri n'est spécifié, tri par date/heure.
  		/**String orderBy;
  		if(TextUtils.isEmpty(sort)) {
  			orderBy = KEY_DATE;
  		}
  		else {
  			orderBy = sort;
  		}*/

  		// Applique la requête à la base.
  		Cursor cursor = qb.query(database, projection, selection, selectionArgs, null, null, null);

  		// Enregistre le ContextResolver pour qu'il soit averti si le résultat change.
  		cursor.setNotificationUri(getContext().getContentResolver(), uri);

  		// Renvoie un curseur.
  		return cursor;
  	}

  	/**
  	 *  Insertion des données.
  	 */
  	@Override
  	public Uri insert(Uri _uri, ContentValues _initialValues) 
  	{
  		// Insère la nouvelle ligne. Renvoie son numéro en cas de succès.
  		long rowID = database.insert(TABLE, "nullColumnHack", _initialValues);

  		// Renvoie l'URI de la nouvelle ligne.
  		if(rowID > 0) {
  			Uri uri = ContentUris.withAppendedId(CONTENT_UTILISATEUR_URI, rowID);
  			getContext().getContentResolver().notifyChange(uri, null);

  			return uri;
  		}

  		throw new SQLException("Echec de l'ajout d'une ligne dans " + _uri);
  	}

  	/**
  	 *  Suppression des données.
  	 */
  	@Override
  	public int delete(Uri uri, String where, String[] whereArgs) 
  	{
  		int count;

  		switch(uriMatcher.match(uri)) {
  			case OBJETS: count = database.delete(TABLE, where, whereArgs); break;
  			case OBJET_ID: {
  				String segment = uri.getPathSegments().get(1);
  				count = database.delete(TABLE, KEY_ID + "=" + segment + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
  			} break;

  			default: throw new IllegalArgumentException("URI non supportée : " + uri);
  		}

  		getContext().getContentResolver().notifyChange(uri, null);

  		return count;
  	}

  	/**
  	 *  Mise à jour des données.
  	 */
  	@Override
  	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
  	{
  		int count;

  		switch(uriMatcher.match(uri)) {
      		case OBJETS: {
      			count = database.update(TABLE, values, where, whereArgs); break;
      		}
      		case OBJET_ID: {
      			String segment = uri.getPathSegments().get(1);
                count = database.update(TABLE, values, KEY_ID + "=" + segment + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
      		} break;

      		default: throw new IllegalArgumentException("URI inconnue " + uri);
  		}

  		getContext().getContentResolver().notifyChange(uri, null);

    	return count;
  	}

  	/**
  	 *  Retourne les données sous le format spécifié.
  	 */
  	@Override
  	public String getType(Uri uri) 
  	{
  		switch(uriMatcher.match(uri)) {
  			case OBJETS: 	return "vnd.android.cursor.dir/vnd.lanouveller.franceexpatries";
  			case OBJET_ID: 	return "vnd.android.cursor.item/vnd.lanouveller.franceexpatries";
  			default: throw new IllegalArgumentException("URI non supportée : " + uri);
  		}
  	}

  	// Crée les constantes utilisées pour différencier les requêtes URI.
  	private static final int OBJETS = 1;
  	private static final int OBJET_ID = 2;

  	private static final UriMatcher uriMatcher;

  	// Alloue l'objet UriMatcher. Une URI terminée par 'utilisateur' correspondra à une requête sur tous les 'utilisateurs'.
  	// Une URI terminée par'/[rowID]' correspondra à une ligne unique.
  	static {
  		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  		uriMatcher.addURI("com.lanouveller.provider.utilisateur", "utilisateur", OBJETS);
  		uriMatcher.addURI("com.lanouveller.provider.utilisateur", "utilisateur/#", OBJET_ID);
  	}

  	// Constantes de la base de données.
  	private SQLiteDatabase database;
  	private static final String TAG = "UtilisateurProvider";
  	private static final String DATABASE_NAME = "franceexpatries_utilisateur.db";
  	private static final int DATABASE_VERSION = 1;
  	private static final String TABLE = "utilisateur";

  	// Noms des colonnes.
  	public static final String KEY_ID = "_id";
  	public static final String KEY_INITIALISE = "initialise_utilisateur";
  	public static final String KEY_PAYS = "pays_utilisateur";
  	// Disponibilté des contenus Hors-Ligne.
  	public static final String KEY_DISPO_INFOS = "disponibilite_informations";
  	public static final String KEY_DISPO_CORRESPONDANTS = "disponibilite_correspondants";
  	public static final String KEY_DISPO_GUIDES_HS = "disponibilite_guides_hs";
  	public static final String KEY_DISPO_GUIDES = "disponibilite_guides";
  	// Disponibilté des guides Hors-Ligne ( source XML ).
  	public static final String KEY_DISPO_GUIDE_ARGENT_HS = "disponibilite_guide_argent_hs";
  	public static final String KEY_DISPO_GUIDE_EMPLOI_HS = "disponibilite_guide_emploi_hs";
  	public static final String KEY_DISPO_GUIDE_PASSEPORT_HS = "disponibilite_guide_passeport_hs";
  	public static final String KEY_DISPO_GUIDE_SANTE_HS = "disponibilite_guide_sante_hs";
  	public static final String KEY_DISPO_GUIDE_TRANSPORT_HS = "disponibilite_guide_transport_hs";
  	public static final String KEY_DISPO_GUIDE_VIE_CULTURELLE_HS = "disponibilite_guide_vie_culturelle_hs";
  	public static final String KEY_DISPO_GUIDE_VIE_QUOTIDIENNE_HS = "disponibilite_vie_quotidienne_hs";
  	// Disponibilté des guides en ligne ( source html ).
  	public static final String KEY_DISPO_GUIDE_ARGENT = "disponibilite_guide_argent";
  	public static final String KEY_DISPO_GUIDE_EMPLOI = "disponibilite_guide_emploi";
  	public static final String KEY_DISPO_GUIDE_PASSEPORT = "disponibilite_guide_passeport";
  	public static final String KEY_DISPO_GUIDE_SANTE = "disponibilite_guide_sante";
  	public static final String KEY_DISPO_GUIDE_TRANSPORT = "disponibilite_guide_transport";
  	public static final String KEY_DISPO_GUIDE_VIE_CULTURELLE = "disponibilite_guide_vie_culturelle";
  	public static final String KEY_DISPO_GUIDE_VIE_QUOTIDIENNE = "disponibilite_vie_quotidienne";
  	public static final String KEY_COMMENTAIRE = "commentaire_utilisateur";

  	// Indexes.
  	public static final int INITIALISE_COLUMN = 1;
  	public static final int PAYS_COLUMN = 2;
  	// Disponibilté des contenus Hors-Ligne.
  	public static final int DISPO_INFOS_COLUMN = 3;
  	public static final int DISPO_CORRESPONDANTS_COLUMN = 4;
  	public static final int DISPO_GUIDES_HS_COLUMN = 5;
  	public static final int DISPO_GUIDES_COLUMN = 6;
	// Disponibilté des guides Hors-Ligne.
  	public static final int DISPO_GUIDE_ARGENT_HS_COLUMN = 7;
  	public static final int DISPO_GUIDE_EMPLOI_HS_COLUMN = 8;
  	public static final int DISPO_GUIDE_PASSEPORT_HS_COLUMN = 9;
  	public static final int DISPO_GUIDE_SANTE_HS_COLUMN = 10;
  	public static final int DISPO_GUIDE_TRANSPORT_HS_COLUMN = 11;
  	public static final int DISPO_GUIDE_VIE_CULTURELLE_HS_COLUMN = 12;
  	public static final int DISPO_GUIDE_VIE_QUOTIDIENNE_HS_COLUMN = 13;
	// Disponibilté des guides en ligne.
  	public static final int DISPO_GUIDE_ARGENT_COLUMN = 14;
  	public static final int DISPO_GUIDE_EMPLOI_COLUMN = 15;
  	public static final int DISPO_GUIDE_PASSEPORT_COLUMN = 16;
  	public static final int DISPO_GUIDE_SANTE_COLUMN = 17;
  	public static final int DISPO_GUIDE_TRANSPORT_COLUMN = 18;
  	public static final int DISPO_GUIDE_VIE_CULTURELLE_COLUMN = 19;
  	public static final int DISPO_GUIDE_VIE_QUOTIDIENNE_COLUMN = 20;
  	public static final int COMMENTAIRE_COLUMN = 21;

  	// Classe helper pour ouvrir, créer et gérer le contrôle de version de la base.
  	private static class DatabaseHelper extends SQLiteOpenHelper {

  		private static final String DB_CREATE = "CREATE TABLE " + TABLE + " (" 
												+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
												+ KEY_INITIALISE + " INTEGER, "
												+ KEY_PAYS + " TEXT, "
												+ KEY_DISPO_INFOS + " TEXT, "
												+ KEY_DISPO_CORRESPONDANTS + " TEXT, "
												+ KEY_DISPO_GUIDES_HS + " TEXT, "
												+ KEY_DISPO_GUIDES + " TEXT, "
												+ KEY_DISPO_GUIDE_ARGENT_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_EMPLOI_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_PASSEPORT_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_SANTE_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_TRANSPORT_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_VIE_CULTURELLE_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_VIE_QUOTIDIENNE_HS + " TEXT, "
												+ KEY_DISPO_GUIDE_ARGENT + " TEXT, "
												+ KEY_DISPO_GUIDE_EMPLOI + " TEXT, "
												+ KEY_DISPO_GUIDE_PASSEPORT + " TEXT, "
												+ KEY_DISPO_GUIDE_SANTE + " TEXT, "
												+ KEY_DISPO_GUIDE_TRANSPORT + " TEXT, "
												+ KEY_DISPO_GUIDE_VIE_CULTURELLE + " TEXT, "
												+ KEY_DISPO_GUIDE_VIE_QUOTIDIENNE + " TEXT, "
												+ KEY_COMMENTAIRE + " TEXT);";

  		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) 
  		{
  			super(context, name, factory, version);
  		}

  		@Override
  		public void onCreate(SQLiteDatabase db) 
  		{
  			db.execSQL(DB_CREATE);
  		}

  		// Appelé lors d'un changement de version.
  		@Override
  		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
  		{
  			Log.w(TAG, "Mise à jour de la version " + oldVersion + " vers la version " + newVersion + ", les anciennes données seront détruites ");

  			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
  			onCreate(db);
  		}
  	}

}
