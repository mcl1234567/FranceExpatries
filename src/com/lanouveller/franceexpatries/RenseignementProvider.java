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

public class RenseignementProvider extends ContentProvider {

	public static final Uri CONTENT_RENSEIGNEMENT_URI = Uri.parse("content://com.lanouveller.provider.pays/pays");

  	@Override
  	public boolean onCreate() 
  	{
  		DatabaseHelper dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
  		database = dbHelper.getWritableDatabase();

    	return (database == null) ? false : true;
  	}

  	// Fonction de requête.
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

  	// Insertion des données.
  	@Override
  	public Uri insert(Uri _uri, ContentValues _initialValues) 
  	{
  		// Insère la nouvelle ligne. Renvoie son numéro en cas de succès.
  		long rowID = database.insert(TABLE, "nullColumnHack", _initialValues);

  		// Renvoie l'URI de la nouvelle ligne.
  		if(rowID > 0) {
  			Uri uri = ContentUris.withAppendedId(CONTENT_RENSEIGNEMENT_URI, rowID);
  			getContext().getContentResolver().notifyChange(uri, null);

  			return uri;
  		}

  		throw new SQLException("Echec de l'ajout d'une ligne dans " + _uri);
  	}

  	// Suppression des données.
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

  	// Mise à jour des données.
  	@Override
  	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
  	{
  		int count;

  		switch(uriMatcher.match(uri)) {
      		case OBJETS: count = database.update(TABLE, values, where, whereArgs); break;
      		case OBJET_ID: {
      			String segment = uri.getPathSegments().get(1);
                count = database.update(TABLE, values, KEY_ID + "=" + segment + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
      		} break;

      		default: throw new IllegalArgumentException("URI inconnue " + uri);
  		}

  		getContext().getContentResolver().notifyChange(uri, null);

    	return count;
  	}

  	// Retourne les données sous le format spécifié.
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

  	// Alloue l'objet UriMatcher. Une URI terminée par 'pays' correspondra à une requête sur tous les renseignements.
  	// Une URI terminée par'/[rowID]'  correspondra à une ligne unique.
  	static {
  		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  		uriMatcher.addURI("com.lanouveller.provider.pays", "pays", OBJETS);
  		uriMatcher.addURI("com.lanouveller.provider.pays", "pays/#", OBJET_ID);
  	}

  	// La base de données.
  	private SQLiteDatabase database;
  	private static final String TAG = "RenseignementProvider";
  	private static final String DATABASE_NAME = "franceexpatries_renseignements.db";
  	private static final int DATABASE_VERSION = 1;
  	private static final String TABLE = "pays";

  	// Noms des colonnes.
  	public static final String KEY_ID = "_id";
  	public static final String KEY_NOM = "nom";
  	public static final String KEY_MONNAIE = "monnaie";
  	public static final String KEY_POPULATION = "population";
  	public static final String KEY_FORMEETAT = "formeetat";
  	public static final String KEY_ROI = "roi";
  	public static final String KEY_PRESIDENT_GOUVERNEMENT = "president_gouvernement";
  	public static final String KEY_LANGUE = "langue";
  	public static final String KEY_CAPITALE = "capitale";
  	public static final String KEY_GOUVERNEMENT = "gouvernement";
  	public static final String KEY_PREMIER_MINISTRE = "premier_ministre";
  	public static final String KEY_PRESIDENT_REPUBLIQUE = "president_republique";
  	public static final String KEY_CLIMAT = "climat";
  	public static final String KEY_SUPERFICIE = "superficie";
  	public static final String KEY_DENSITE = "densite";
  	public static final String KEY_RELIGION = "religion";
  	public static final String KEY_PIB = "pib";
  	public static final String KEY_NOMBRE_EXPATRIES = "nombre_expatries";
  	public static final String KEY_TAUX_CHOMAGE = "taux_chomage";
  	public static final String KEY_INDICATIF_TEL = "indicatif_tel";
  	public static final String KEY_FUSEAU_HORAIRE = "fuseau_horaire";

  	// Indexes.
  	public static final int NOM_COLUMN = 1;
  	public static final int MONNAIE_COLUMN = 2;
  	public static final int POPULATION_COLUMN = 3;
  	public static final int FORMEETAT_COLUMN = 4;
  	public static final int ROI_COLUMN = 5;
  	public static final int PRESIDENT_GOUVERNEMENT_COLUMN = 6;
  	public static final int LANGUE_COLUMN = 7;
  	public static final int CAPITALE_COLUMN = 8;
  	public static final int GOUVERNEMENT_COLUMN = 9;
  	public static final int PREMIER_MINISTRE_COLUMN = 10;
  	public static final int PRESIDENT_REPUBLIQUE_COLUMN = 11;
  	public static final int CLIMAT_COLUMN = 12;
  	public static final int SUPERFICIE_COLUMN = 13;
  	public static final int DENSITE_COLUMN = 14;
  	public static final int RELIGION_COLUMN = 15;
  	public static final int PIB_COLUMN = 16;
  	public static final int NOMBRE_EXPATRIES_COLUMN = 17;
  	public static final int TAUX_CHOMAGE_COLUMN = 18;
  	public static final int INDICATIFT_TEL_COLUMN = 19;
  	public static final int FUSEAU_HORAIRE_COLUMN = 20;

  	// Classe helper pour ouvrir, créer et gérer le contrôle de version de la base.
  	private static class DatabaseHelper extends SQLiteOpenHelper {

  		private static final String DB_CREATE = "CREATE TABLE " + TABLE + " (" 
																+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
																+ KEY_NOM + " TEXT, "
																+ KEY_MONNAIE + " TEXT, "
																+ KEY_POPULATION + " TEXT, "
																+ KEY_FORMEETAT + " TEXT, "
																+ KEY_ROI + " TEXT, "
																+ KEY_PRESIDENT_GOUVERNEMENT + " TEXT, "
																+ KEY_LANGUE + " TEXT, "
																+ KEY_CAPITALE + " TEXT, "
																+ KEY_GOUVERNEMENT + " TEXT, "
																+ KEY_PREMIER_MINISTRE + " TEXT, "
																+ KEY_PRESIDENT_REPUBLIQUE + " TEXT, "
																+ KEY_CLIMAT + " TEXT, "
																+ KEY_SUPERFICIE + " TEXT, "
																+ KEY_DENSITE + " TEXT, "
																+ KEY_RELIGION + " TEXT, "
																+ KEY_PIB + " TEXT, "
																+ KEY_NOMBRE_EXPATRIES + " TEXT, "
																+ KEY_TAUX_CHOMAGE + " TEXT, "
																+ KEY_INDICATIF_TEL + " TEXT, "
																+ KEY_FUSEAU_HORAIRE + " TEXT);";

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
