package com.lanouveller.franceexpatries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;

public class FicheService extends Service {

	public static final String CHECK_GUIDE_HS = "com.lanouveller.franceexpatries.CHECK_GUIDE_HS";
	public static final String CHECK_GUIDE = "com.lanouveller.franceexpatries.CHECK_GUIDE";
	public static final String GUIDE_REFRESHED = "com.lanouveller.franceexpatries.GUIDE_REFRESHED";

	public static final String CHECKING = "CHECKING";
	public static final String DOWNLOADING = "DOWNLOADING";

	private AppLookupTask lastLookup = null;

	private String modeRequest = "";

    @Override
    public IBinder onBind(Intent intent) { return null; }

  	private class AppLookupTask extends AsyncTask<String, Void, ArrayList<Fiche>> {
  		String pays = "";
  		String guide = "";
  		String mode = "";

  		@Override
  		protected ArrayList<Fiche> doInBackground(String... _params) 
  		{
  			// Récupére le XML.
  			URL url;
  			
  			ArrayList<Fiche> fiches = new ArrayList<Fiche>();

  	  		pays = _params[0];
  			guide = _params[1];
  			mode = _params[2];

  			try {
  				String feed = "";

  				if(mode.equalsIgnoreCase(DOWNLOADING)) {
  	  				feed = "http://france-expatries.com/fex_app/guides_horsligne.php?pays=" + pays + "&guide=" + guide;
  	    	    	//Log.i("feed", "feed : " + feed);
  					url = new URL(feed);

  		  			URLConnection connection;
  		  			connection = url.openConnection();

  		  			HttpURLConnection httpConnection = (HttpURLConnection)connection; 
  		  			int responseCode = httpConnection.getResponseCode(); 
  		
  		  			if(responseCode == HttpURLConnection.HTTP_OK) {
  		  				InputStream in = httpConnection.getInputStream();
  		
  		  				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  		  				DocumentBuilder db = dbf.newDocumentBuilder();
  		
  		  				// Parse le flux.
  		  				Document dom = db.parse(in);
  		  				Element docEle = dom.getDocumentElement();
  		
  		  				// Récupère une liste de chaque entrée.
  		  				NodeList nl = docEle.getElementsByTagName("fiche");
  		
  		  				if(nl != null && nl.getLength() > 0) {
  		  					for(int i=0; i<nl.getLength(); i++) {
  		  						Element entry = (Element) nl.item(i);
  		
  		  						String titre = entry.getElementsByTagName("titre_fiche").item(0).getFirstChild().getNodeValue();
  		  						String contenu = entry.getElementsByTagName("contenu").item(0).getFirstChild().getNodeValue();
  		
  		  		  				Fiche fiche = new Fiche(pays, guide, titre, contenu);
  		
  		  		  				fiches.add(fiche);
  		
  		  						// Traite le nouveau correspondant ajouté.
  		  		  				saveToBDD(fiche);
  		  					}
  		  				}
  		
  			  			if(fiches.size() > 0) {
  			  				return fiches;
  			  			}
  		  			}
  				}
  				else if(mode.equalsIgnoreCase(CHECKING)) {
  	  				feed = "http://france-expatries.com/fex_app/guides.php?pays=" + pays + "&guide=" + guide;
  	    	    	//Log.i("feed", "feed : " + feed);
  					url = new URL(feed);

  		  			URLConnection connection;
  		  			connection = url.openConnection();

  		  			HttpURLConnection httpConnection = (HttpURLConnection)connection; 
  		  			int responseCode = httpConnection.getResponseCode(); 
  		
  		  			if(responseCode == HttpURLConnection.HTTP_OK) {
  		  				InputStream in = httpConnection.getInputStream();
  		
  		  				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  		  				DocumentBuilder db = dbf.newDocumentBuilder();
  		
  		  				// Parse le flux.
  		  				Document dom = db.parse(in);
  		  				Element docEle = dom.getDocumentElement();

  		  				if(docEle.getElementsByTagName("div").getLength() > 0) {
  		  					return new ArrayList<Fiche>();
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
  			// Cette exception est créé par la syntaxe du script HTML, cela signifie que le document est présent.
  			catch (DOMException e) {
  				e.printStackTrace();
				return new ArrayList<Fiche>();
  			}
  			finally { }

  			return null;
  		}

    	@Override
    	protected void onProgressUpdate(Void... values)  { }

    	@Override
    	protected void onPostExecute(ArrayList<Fiche> result) 
    	{
    		// Envoi des disponiblités de tous les guides stockés hors-ligne.
    		if(modeRequest.equalsIgnoreCase(DOWNLOADING)) {
    			Intent intent = new Intent(GUIDE_REFRESHED);

        		// Envoi aux préférences les disponibilités - L'utilisation de la méthode setAction ne permet au receiver de recevoir la diffusion.
        		if(result == null) { intent.putExtra("disponibilite", "guide_" + guide + "_non_disponible_hs"); }
        		else { intent.putExtra("disponibilite" , "guide_" + guide + "_disponible_hs"); }

      	  		// Diffuseur de l'activité 'Préférences'.
      	  		sendBroadcast(intent);
    		}
    		// Envoi des disponiblités de tous les guides accessibles en ligne.
    		else if(modeRequest.equalsIgnoreCase(CHECKING)) {
    			Intent intent = new Intent(CHECK_GUIDE);

        		// Envoi aux préférences les disponibilités - L'utilisation de la méthode setAction ne permet pas au receiver de recevoir la diffusion.
        		if(result == null) { intent.putExtra("disponibilite", "guide_" + guide + "_non_disponible"); }
        		else { intent.putExtra("disponibilite" , "guide_" + guide + "_disponible"); }

      	  		// Diffuseur pour l'activité 'Préférences'.
      	  		sendBroadcast(intent);
    		}

  	  		stopSelf();
    	}
  	}

    private void saveToBDD(Fiche _fiche) 
    {
    	ContentResolver contentResolver = getContentResolver();

    	// Construit une clause where pour vérifier que ce correspondant n'est pas déjà dans le provider.
    	String where = FicheProvider.KEY_PAYS + " = " + '"' + _fiche.getPays() + '"' + " AND " + 
    					FicheProvider.KEY_TITRE + " = " + '"' + _fiche.getTitre() + '"' + " AND " +
    					FicheProvider.KEY_GUIDE + " = " + '"' + _fiche.getGuide() + '"';

    	// Si le correspondant est nouveau, on l'insère.
    	if(contentResolver.query(FicheProvider.CONTENT_FICHE_URI, null, where, null, null).getCount() == 0) {
    		ContentValues values = new ContentValues();

    		values.put(FicheProvider.KEY_PAYS, _fiche.getPays());
    		values.put(FicheProvider.KEY_GUIDE, _fiche.getGuide());
    		values.put(FicheProvider.KEY_TITRE, _fiche.getTitre());
    		values.put(FicheProvider.KEY_CONTENU, _fiche.getContenu());

    		contentResolver.insert(FicheProvider.CONTENT_FICHE_URI, values);
    	}
    }

    /**
     * Création du Service.
     */
  	@Override
  	public int onStartCommand(Intent intent, int flags, int startId) 
  	{
  		String pays = "inconnu";
  		if(getPaysFromBDD() != null) {
  			pays = getPaysFromBDD();
  		}

  		if(intent != null) {
  			// Récupère le type de demande ( check de contenus en ligne )
  	  		if(intent.getCharSequenceExtra(CHECKING) != null 
  	  		&& ((String) intent.getCharSequenceExtra(CHECKING)).equalsIgnoreCase(CHECKING)) {
  	  			modeRequest = CHECKING;
  	  		}
  			// Récupère le type de demande ( check et maj de la bdd )
  	  		else if(intent.getCharSequenceExtra(DOWNLOADING) != null 
  	  		&& ((String) intent.getCharSequenceExtra(DOWNLOADING)).equalsIgnoreCase(DOWNLOADING)) {
  	  			modeRequest = DOWNLOADING;
  	  		}

  	  		refresh(pays, intent.getAction(), modeRequest);
  		}

  		return Service.START_NOT_STICKY;
    }

    /**
     * Méthode appelant le thread en arrière-plan.
     */
    private void refresh(String _paysDemande, String _guideDemande, String _mode) 
    {
  	    if(lastLookup == null || lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
  	    	lastLookup = new AppLookupTask();
  	    	/*Log.i("_paysDemande", "_paysDemande : " + _paysDemande);
  	    	Log.i("_guideDemande", "_guideDemande : " + _guideDemande);
  	    	Log.i("_mode", "_mode : " + _mode);*/
  	    	lastLookup.execute(_paysDemande, _guideDemande, _mode);
  	    }
    }

  	/**
  	 * Initialisation du pays.
  	 */
  	public String getPaysFromBDD() 
  	{
  		Cursor cursor = getContentResolver().query(UtilisateurProvider.CONTENT_UTILISATEUR_URI, null, null, null, null);

  		while(cursor.moveToNext()) {
  			return getPaysFormate(cursor.getString(UtilisateurProvider.PAYS_COLUMN));
  		}
  		return null;
  	}

  	/**
  	 * Retourne le nom formaté.
  	 */
	private String getPaysFormate(String _nomPays) {
		String nomPaysFormate = _nomPays;

		if(_nomPays.equalsIgnoreCase("Costa Rica") || _nomPays.equalsIgnoreCase("CostaRica")) {
			nomPaysFormate = "Costa-Rica";
		}
		else if(_nomPays.equalsIgnoreCase("Coree du nord") || _nomPays.equalsIgnoreCase("Corée du nord")) {
			nomPaysFormate = "Coree-du-Nord";
		}
		else if(_nomPays.equalsIgnoreCase("Coree du sud") || _nomPays.equalsIgnoreCase("Corée du sud")) {
			nomPaysFormate = "Coree-du-sud";
		}

		return nomPaysFormate;
	}

}