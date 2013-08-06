package com.lanouveller.franceexpatries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class BlogService extends Service {

	public static final String NOUVEAU_OBJ_INSERE = "Nouveau blog enregistré";
	public static final String BLOG_REFRESHED = "com.lanouveller.franceexpatries.BLOG_REFRESHED";

	private AppLookupTask lastLookup = null;

	/**
	 * Lancement des threads en background.
	 */
  	private class AppLookupTask extends AsyncTask<Void, Void, Void> {

  		@Override
  		protected Void doInBackground(Void... params) 
  		{
  			Log.i("TAG", "doInBackground");
  			// Récupére le XML.
  			URL url;

  			try {
  				String feed = "http://www.france-expatries-blog.fr/?feed=rss2";
  				url = new URL(feed);

  				URLConnection connection;
  				connection = url.openConnection();

  				HttpURLConnection httpConnection = (HttpURLConnection) connection;
  				int responseCode = httpConnection.getResponseCode();

  				if(responseCode == HttpURLConnection.HTTP_OK) {
  					InputStream in = httpConnection.getInputStream();

  					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  					DocumentBuilder db = dbf.newDocumentBuilder();

  					// Parse le flux.
  					Document dom = db.parse(in);
  					Element docEle = dom.getDocumentElement();

  					// Récupère une liste de chaque entrée.
  					NodeList nl = docEle.getElementsByTagName("item");

  					if(nl != null && nl.getLength() > 0) {
  						for(int i=0; i<nl.getLength(); i++) {
  							Element entry = (Element) nl.item(i);

  							String titre = 			entry.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
  							String description = 	entry.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
  							String contenu = 		entry.getElementsByTagName("content:encoded").item(0).getFirstChild().getNodeValue();
  							String date = 			entry.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
  							String lien = 			entry.getElementsByTagName("link").item(0).getFirstChild().getNodeValue();

  							Article article = new Article(titre, description, contenu, date, lien);

  							// Traite le nouveau obj. ajouté.
  							ajoutArticle(article);
  						}
  					}
  				}
  			} catch (MalformedURLException e) {
  				e.printStackTrace();
  			} catch (IOException e) {
  				e.printStackTrace();
  			} catch (ParserConfigurationException e) {
  				e.printStackTrace();
  			} catch (SAXException e) {
  				e.printStackTrace();
  			}
  			finally { }

  			return null;
  		}

    	@Override
    	protected void onProgressUpdate(Void... values) 
    	{    		
    		// Récupération d'une notification.
    		/**NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    		Context context = getApplicationContext();
    		String expandedText =""; //values[0].getT();
    		String expandedTitle = "M: taratata";

    		// Démarre une nouvelle activité.
    		Intent BlogsIntent = new Intent(BlogService.this, Blog.class);
    		PendingIntent launchIntent = PendingIntent.getActivity(context, 0, BlogsIntent, 0);

    		// Configuration d'une notification.
    		nouvelleNotification.setLatestEventInfo(context, expandedTitle, expandedText, launchIntent);
    		nouvelleNotification.when = java.lang.System.currentTimeMillis();

    		// Envoi d'une notification.
    		notificationManager.notify(NOTIFICATION_ID, nouvelleNotification);

    		Toast.makeText(context, expandedTitle, Toast.LENGTH_SHORT).show();*/
    	}

    	@Override
    	protected void onPostExecute(Void result) 
    	{
			Log.i("TAG", "onPostExecute");
    		// Appel des receivers.
  	  		sendBroadcast(new Intent(BLOG_REFRESHED));
  	  		stopSelf();
    	}
  	}

    /**
     * Insertion / Mise à jour de la base de données.
     */
    private void ajoutArticle(Article _article) 
    {
		Log.i("TAG", "ajoutArticle");
    	ContentResolver contentResolver = getContentResolver();

    	// Construit une clause where pour vérifier que ce pays n'est pas déjà dans le provider.
    	String where = BlogProvider.KEY_TITRE + " = '" + _article.getTitre() + "'";

    	if(contentResolver.query(BlogProvider.CONTENT_BLOG_URI, null, where, null, null).getCount() == 0) {

    		ContentValues values = new ContentValues();
		    values.put(BlogProvider.KEY_TITRE, _article.getTitre());
		    values.put(BlogProvider.KEY_DESCRIPTION, _article.getDescription());
		    values.put(BlogProvider.KEY_CONTENU, _article.getContenu());
		    values.put(BlogProvider.KEY_DATE, _article.getDate());
		    values.put(BlogProvider.KEY_LIEN, _article.getLien());

    		contentResolver.insert(BlogProvider.CONTENT_BLOG_URI, values);

    		/**annonceArticle(_article);*/
    	}
    }

  	/**
  	 * Lancement du service via l'intent.
  	 */
  	@Override
  	public int onStartCommand(Intent intent, int flags, int startId) 
  	{
		refresh();

  		return Service.START_NOT_STICKY;
    };

    @Override
    public IBinder onBind(Intent intent) 
    {
    	return null;
    }

    /**
    private void annonceArticle(Article _article) 
    {
    	Intent intent = new Intent(NOUVEAU_OBJ_INSERE);
    	intent.putExtra("titre", _article.getTitre());
    	sendBroadcast(intent);
    }*/

    /**
     * Lance un thread en background.
     */
    private void refresh() {
    	if(lastLookup == null || lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
    		lastLookup = new AppLookupTask();
    		lastLookup.execute((Void[]) null);
    	}
    }

}
