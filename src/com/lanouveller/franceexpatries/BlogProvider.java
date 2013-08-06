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

public class BlogProvider extends ContentProvider {

	public static final Uri CONTENT_BLOG_URI = Uri.parse("content://com.lanouveller.provider.blog/blog");

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

  		// Si aucun ordre de tri n'est spécifié, tri par date/heure
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
  			Uri uri = ContentUris.withAppendedId(CONTENT_BLOG_URI, rowID);
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

  	// Alloue l'objet UriMatcher. Une URI terminée par 'pays' correspondra à une requête sur tous les Blogs.
  	// Une URI terminée par'/[rowID]'  correspondra à une ligne unique.
  	static {
  		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  		uriMatcher.addURI("com.lanouveller.provider.Blogs", "blog", OBJETS);
  		uriMatcher.addURI("com.lanouveller.provider.Blogs", "blog/#", OBJET_ID);
  	}

  	// La base de données.
  	private SQLiteDatabase database;
  	private static final String TAG = "BlogProvider";
  	private static final String DATABASE_NAME = "franceexpatries_blog.db";
  	private static final int DATABASE_VERSION = 1;
  	private static final String TABLE = "blog";

  	// Noms des colonnes.
  	public static final String KEY_ID = "_id";
  	public static final String KEY_TITRE = "titre";
  	public static final String KEY_DESCRIPTION = "description";
  	public static final String KEY_CONTENU = "contenu";
  	public static final String KEY_DATE = "date";
  	public static final String KEY_LIEN = "lien";

  	// Indexes.
  	public static final int TITRE_COLUMN = 1;
  	public static final int DESCRIPTION_COLUMN = 2;
  	public static final int CONTENU_COLUMN = 3;
  	public static final int DATE_COLUMN = 4;
  	public static final int LIEN_COLUMN = 5;

  	// Classe helper pour ouvrir, créer et gérer le contrôle de version de la base.
  	private static class DatabaseHelper extends SQLiteOpenHelper {

  		private static final String DB_CREATE = "CREATE TABLE " + TABLE + " (" 
																+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
																+ KEY_TITRE + " TEXT, "
																+ KEY_DESCRIPTION + " TEXT, "
																+ KEY_CONTENU + " TEXT, "
																+ KEY_DATE + " TEXT, "
																+ KEY_LIEN + " TEXT);";

  		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) 
  		{
  			super(context, name, factory, version);
  		}

  		@Override
  		public void onCreate(SQLiteDatabase db) 
  		{
  			db.execSQL(DB_CREATE);
  		}

  		// Initialisé lors d'un changement de version.
  		@Override
  		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
  		{
  			Log.w(TAG, "Mise à jour de la version " + oldVersion + " vers la version " + newVersion + ", les anciennes données seront détruites ");

  			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
  			onCreate(db);
  		}
  	}

}
