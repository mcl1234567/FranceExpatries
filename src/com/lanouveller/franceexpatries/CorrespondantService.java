package com.lanouveller.franceexpatries;

import java.io.ByteArrayOutputStream;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;

import com.lanouveller.franceexpatries.Correspondants.ImageGestion;

public class CorrespondantService extends Service {

	public static final String CHECK_CORRESPONDANT = "com.lanouveller.franceexpatries.CHECK_CORRESPONDANT";
	public static final String CORRESPONDANT_REFRESHED = "com.lanouveller.franceexpatries.CORRESPONDANT_REFRESHED";
	public static final String CORRESPONDANT_IMAGES = "com.lanouveller.franceexpatries.CORRESPONDANT_IMAGES";

	public static final String IMAGES = "IMAGES";

	private String adresseImage = "";

	private AppLookupTask lastLookup = null;

	private URL url;
	
	String modeRequest = "";

	static int imageWidth;
	static int imageHeight;

    @Override
    public IBinder onBind(Intent intent) { return null; }

	public class AppLookupTask extends AsyncTask<String, Void, String> {

  		@Override
  		protected String doInBackground(String... params) 
  		{
  			// Récupére le XML.
  			try {
  				ArrayList<Correspondant> correspondants = new ArrayList<Correspondant>();
  				String feed = "http://france-expatries.com/fex_app/correspondants.php?pays=" + params[0];

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
  					NodeList nl = docEle.getElementsByTagName("correspondant");

  					if(nl != null && nl.getLength() > 0) {
  						for(int i=0; i<nl.getLength(); i++) {
  							Element entry = (Element) nl.item(i);

  							int numeroValue = 		Integer.parseInt(entry.getElementsByTagName("numero").item(0).getFirstChild().getNodeValue());
  							//String sexe = 		entry.getElementsByTagName("sexe").item(0).getFirstChild().getNodeValue();
  							String nom = 			entry.getElementsByTagName("nom").item(0).getFirstChild().getNodeValue();
  							String prenom = 		entry.getElementsByTagName("prenom").item(0).getFirstChild().getNodeValue();
  							String email =			entry.getElementsByTagName("email").item(0).getFirstChild().getNodeValue();
  							String pays = 			entry.getElementsByTagName("pays").item(0).getFirstChild().getNodeValue();
  							String ville = 			entry.getElementsByTagName("ville").item(0).getFirstChild().getNodeValue();
  							String dateNaissance = 	entry.getElementsByTagName("datenaissance").item(0).getFirstChild().getNodeValue();
  							String profession =  	entry.getElementsByTagName("profession").item(0).getFirstChild().getNodeValue();
  							adresseImage =  		entry.getElementsByTagName("image").item(0).getFirstChild().getNodeValue();
  							String idImage =  		entry.getElementsByTagName("image").item(0).getFirstChild().getNodeValue();

  							String nomImage = idImage.substring(idImage.indexOf("files/") + "files/".length());

  							Correspondant correspondant = new Correspondant(numeroValue, "", nom, prenom, email, pays, ville, dateNaissance, profession, nomImage);

  							// Traite le nouveau correspondant ajouté.
  							addCorrespondant(correspondant);

  							// Enregistre le bitmap.
  				      		ImageGestion.setImageGestion(nomImage, dlImage());
  				      		
  				      		// Non fonctionnel.
  				      		ImageGestion.setImageDimensions(imageWidth, imageHeight);

  				      		correspondants.add(correspondant);
  						}
  					}

  					if(correspondants.size() > 0) {
  						return "ok";
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
    	protected void onProgressUpdate(Void... values) { }

    	@Override
    	protected void onPostExecute(String result) 
    	{
    		if(modeRequest.equalsIgnoreCase(IMAGES)) {
    			sendBroadcast(new Intent(CORRESPONDANT_IMAGES));  
      	  	}
    		else {
    			Intent intent = new Intent(CORRESPONDANT_REFRESHED);       		

            	// Envoi aux préférences les disponibilités - L'utilisation de la méthode setAction ne permet pas au receiver de recevoir la diffusion.
            	if(result == null) { intent.putExtra("disponibilite", "correspondant_non_disponible"); } 
            	else { intent.putExtra("disponibilite" , "correspondant_disponible"); }

          	  	// Diffuseur de l'activité 'Préférences'.
          	  	sendBroadcast(intent);
      	  	}

  	  		stopSelf();
    	}
  	}

	/**
	 * Téléchargement d'images.
	 */
  	protected static byte[] imageByter(Context context, String urlValue) 
  	{
  		try {
  			URL url = new URL(urlValue);
  		    InputStream is = (InputStream) url.getContent();
  		    byte[] buffer = new byte[8192];
  		    int bytesRead;
  		    ByteArrayOutputStream output = new ByteArrayOutputStream();

  		    // Récupère la taille du Bitmap.
  		    /**BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	  		bitmapOptions.inJustDecodeBounds = true;
	  		BitmapFactory.decodeStream(is, null, bitmapOptions);
	  		imageWidth = bitmapOptions.outWidth;
	  		imageHeight = bitmapOptions.outHeight;*/

  		    while((bytesRead = is.read(buffer)) != -1) {
  		    	output.write(buffer, 0, bytesRead);
  		    }

  		    return output.toByteArray();
  		} catch(MalformedURLException e) {
  			e.printStackTrace();
  			return null;
  		} catch (IOException e) {
  		    e.printStackTrace();
  		    return null;
  		}
  	}

  	public Bitmap dlImage() 
  	{
  		byte[] bytes = imageByter(this, adresseImage);
		Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		return bm;
  	}

    private void addCorrespondant(Correspondant _correspondant) 
    {
    	ContentResolver contentResolver = getContentResolver();
    	// Construit une clause where pour vérifier que ce correspondant n'est pas déjà dans le provider.
    	String where = CorrespondantProvider.KEY_NUMERO + " = " + _correspondant.getNumero() + " AND " +
    					CorrespondantProvider.KEY_NOM + " = '" + _correspondant.getNom() + "' AND " +
    					CorrespondantProvider.KEY_PRENOM + " = '" + _correspondant.getPrenom() + "'";

    	// Si le correspondant est nouveau, on l'insère.
    	if(contentResolver.query(CorrespondantProvider.CONTENT_CORRESPONDANT_URI, null, where, null, null).getCount() == 0) {
    		ContentValues values = new ContentValues();

    		values.put(CorrespondantProvider.KEY_NUMERO, _correspondant.getNumero());
    		values.put(CorrespondantProvider.KEY_NOM, _correspondant.getNom());
    		values.put(CorrespondantProvider.KEY_PRENOM, _correspondant.getPrenom());
    		values.put(CorrespondantProvider.KEY_EMAIL, _correspondant.getEmail());
    		values.put(CorrespondantProvider.KEY_PAYS, _correspondant.getPays());
    		values.put(CorrespondantProvider.KEY_VILLE, _correspondant.getVille());
    		values.put(CorrespondantProvider.KEY_DATE_NAISSANCE, _correspondant.getDateNaissance());
    		values.put(CorrespondantProvider.KEY_PROFESSION, _correspondant.getProfession());
    		values.put(CorrespondantProvider.KEY_NOM_IMAGE, _correspondant.getNomImage());

    		contentResolver.insert(CorrespondantProvider.CONTENT_CORRESPONDANT_URI, values);
    	}
    }

  	@Override
  	public int onStartCommand(Intent intent, int flags, int startId) 
  	{
  		if(intent != null) { 
  			refresh(intent.getAction());
  			if(intent.getStringExtra(IMAGES) != null && intent.getStringExtra(IMAGES).equalsIgnoreCase(IMAGES)) {
  				modeRequest = IMAGES;
  			}
  		}

  		return Service.START_NOT_STICKY;
    }

    private void refresh(String selectionPays) 
    {
    	if(lastLookup == null || lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
    		lastLookup = new AppLookupTask();
    		lastLookup.execute(selectionPays);
    	}
    }

}