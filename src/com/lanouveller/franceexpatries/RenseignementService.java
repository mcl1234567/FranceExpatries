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

public class RenseignementService extends Service {

	public static final String CHECK_INFORMATION = "com.lanouveller.franceexpatries.CHECK_INFORMATION";
	public static final String INFORMATION_REFRESHED = "com.lanouveller.franceexpatries.INFORMATION_REFRESHED";

	private AppLookupTask lastLookup = null;

	private URL url;

    @Override
    public IBinder onBind(Intent intent) { return null; }

	/**
	 * Lancement des threads en background.
	 */
  	private class AppLookupTask extends AsyncTask<String, Void, Pays> {

  		@Override
  		protected Pays doInBackground(String... params) 
  		{
  			Pays pays = null;

  			// Récupére et analyse le flux XML de Wikipédia.
  			/**if(analyseDataWikipedia(params) != null) {
  				pays = analyseDataWikipedia(params);
  			}
  			// Récupére et analyse le flux XML de l'agence LNR.
  			else {
  				pays = analyseDataLNR(params);
  			}*/

			pays = analyseDataLNR(params);
  			if(pays != null) {
  				saveToBDD(pays);
  			}

  			return pays;
  		}

    	@Override
    	protected void onProgressUpdate(Void... values) { }

    	@Override
    	protected void onPostExecute(Pays result) 
    	{
    		Intent intent = new Intent(INFORMATION_REFRESHED);

        	// Envoi aux préférences les disponibilités - L'utilisation de la méthode setAction ne permet pas au receiver de recevoir la diffusion.
        	if(result == null) { intent.putExtra("disponibilite", "information_non_disponible");} 
        	else { intent.putExtra("disponibilite" , "information_disponible"); }

      	  	// Diffuseur de l'activité 'Préférences'.
      	  	sendBroadcast(intent);

  	  		stopSelf();
    	}
  	}

  	/**
  	 * Fonction de test - non appelée.
  	 * Les valeurs analysent sont exploitables, seulement les autres informations tel que le président ou la capitale entre autres 
  	 * sont difficilement analysables, via une simple requête vers l'api.
  	 */
  	public Pays analyseDataWikipedia(String... params) 
  	{
  		try {
  			String paysDemande = params[0];
			String feed = "http://fr.wikipedia.org/w/api.php?format=xml&action=query&titles=" + paysDemande + "&prop=revisions&rvprop=content";
  	    	//Log.i("feed", "feed : " + feed);

			// Analyse des données de Wikipédia.
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
				NodeList nl = docEle.getElementsByTagName("rev");

				if(nl != null && nl.getLength() > 0) {
						Element entry = (Element) nl.item(0);

						String content = "";

						// Tests des contenus d'entrée du xml.
						content = entry.getTextContent();

						// Récupère le PIB du pays.
						String pib = "";
						String indexPIB = "PIB=", indexFinPIB = "<ref>";
						int posPIB = content.indexOf(indexPIB) + indexPIB.length();
						int posFinPIB = content.indexOf(indexFinPIB, posPIB);
						if(posFinPIB > posPIB+200 || posFinPIB < posPIB) {
							posFinPIB = content.indexOf("| PIB", posPIB);
							pib = content.substring(posPIB, posFinPIB);
						} else {
							pib = content.substring(posPIB, posFinPIB);
						}
						pib = pib.replace("}}", "");
						pib = pib.replace("{{unité|", "");
						pib = pib.replace("|", " ");

						// Récupère les monnaies du pays.
						String monnaie = "";
						String indexMonnaie = "monnaie", indexDebutMonnaie = "[[", indexfinMonnaie = "]]";
						int posMonnaie = content.indexOf(indexMonnaie) + indexMonnaie.length();
						int posDebutMonnaie = content.indexOf(indexDebutMonnaie, posMonnaie) + indexDebutMonnaie.length();
						int posFinMonnaie = content.indexOf(indexfinMonnaie, posMonnaie);
						monnaie = content.substring(posDebutMonnaie, posFinMonnaie);
						// Recherche des monnaies suivantes.
						while(content.substring(posFinMonnaie+indexfinMonnaie.length(), posFinMonnaie+indexfinMonnaie.length()+1).equalsIgnoreCase(",") 
						|| content.substring(posFinMonnaie+indexfinMonnaie.length(), posFinMonnaie+indexfinMonnaie.length()+2).equalsIgnoreCase(" ,")) {
							// Position de la monnaie suivante.
							posMonnaie = posFinMonnaie + indexfinMonnaie.length();
							posDebutMonnaie = content.indexOf(indexDebutMonnaie, posMonnaie) + indexDebutMonnaie.length();
							posFinMonnaie = content.indexOf(indexfinMonnaie, posMonnaie);
							monnaie += ", " + content.substring(posDebutMonnaie, posFinMonnaie);
						}

						// Récupère le fuseau horaire du pays.
						String fuseauHoraire = "";
						String indexFuseauHoraire = "fuseau_horaire=", indexFinFuseauHoraire = "hymne";
						int posDebutFuseauHoraire = content.indexOf(indexFuseauHoraire) + indexFuseauHoraire.length();
						int posFinFuseauHoraire = content.indexOf(indexFinFuseauHoraire, posDebutFuseauHoraire) - 2;
						fuseauHoraire = content.substring(posDebutFuseauHoraire, posFinFuseauHoraire);
						// Suppression des séparateurs.
						fuseauHoraire = fuseauHoraire.replace("[[heure d'été|été]]", "été");
						fuseauHoraire = fuseauHoraire.replace("[[Central European Time|CET]]", "CET");
						fuseauHoraire = fuseauHoraire.replace("[[Temps universel coordonné|UTC]]", "UTC");
						fuseauHoraire = fuseauHoraire.replace("([[CEST]])", "CEST");
						fuseauHoraire = fuseauHoraire.replace("<br/>", "");
						fuseauHoraire = fuseauHoraire.replace("<br />", "");
						fuseauHoraire = "UTC " + fuseauHoraire;
						if(posFinFuseauHoraire > posDebutFuseauHoraire + 2000) {
							// Erreur de traitement du xml.
							fuseauHoraire = "";
						}

						// Récupère la population du pays.
						String population = "";
						String indexDebutPopulation = "population_totale={{formatnum:", indexFinPopulation = "}}";
						int posDebutPopulation = content.indexOf(indexDebutPopulation) + indexDebutPopulation.length();
						int posFinPopulation = content.indexOf(indexFinPopulation, posDebutPopulation);
						String populationNonType = content.substring(posDebutPopulation, posFinPopulation);
						// Récupère l'année du relevé de la population du pays.
						String relevePopulation = "";
						String indexDebutRelevePopulation = "population_année=";
						int posDebutRelevePopulation = content.indexOf(indexDebutRelevePopulation) + indexDebutRelevePopulation.length();
						relevePopulation = content.substring(posDebutRelevePopulation, posDebutRelevePopulation+4);
						// Modification : Population typé -> 3400900 hab. -> 3 400 900 hab.
						String populationType = "";
						if(populationNonType.length() > 3) {
							int nombreSeparateur = populationNonType.length() % 3;
							int decalageDebut = 0;
							int decalageFin = 0;
							for(int j=0; j<nombreSeparateur; j++) {
								decalageDebut = 3*(j+1);
								decalageFin = 3*j;
								populationType = " " + populationNonType.substring(populationNonType.length()-decalageDebut, populationNonType.length()-decalageFin) + populationType;
							}
							populationType = populationNonType.substring(0, populationNonType.length()-decalageDebut) + populationType;
						}
						population = populationType + " hab. (" + relevePopulation + ")";

						// Récupère la superficie du pays. 	a tester..
						String superficie = "";
						String indexDebutSuperficie = "superficie_totale={{formatnum:", indexFinSuperficie = "}}";
						int posDebutSuperficie = content.indexOf(indexDebutSuperficie) + indexDebutSuperficie.length();
						int posFinSuperficie = content.indexOf(indexFinSuperficie, posDebutSuperficie);
						String superficieNonType = content.substring(posDebutSuperficie, posFinSuperficie);
						// Modification : Superficie typé -> 340900 km². -> 340 900 km²
						String superficieType = "";
						if(superficieNonType.length() > 3) {
							int nombreSeparateur = superficieNonType.length() / 3;
							int decalageDebut = 0;
							int decalageFin = 0;
							for(int j=0; j<nombreSeparateur; j++) {
								decalageDebut = 3*(j+1);
								decalageFin = 3*j;
								superficieType = " " + superficieNonType.substring(superficieNonType.length()-decalageDebut, superficieNonType.length()-decalageFin) + superficieType;
							}
							superficieType = superficieNonType.substring(0, superficieNonType.length()-decalageDebut) + superficieType;
						}
						superficie = superficieType + " km²";

						// Récupère les langues du pays.
						String langue = "";
						String indexLangue = "langues_officielles", indexDebutLangue = "[[", indexfinLangue = "]]";
						int posLangue = content.indexOf(indexLangue) + indexLangue.length();
						int posDebutLangue = content.indexOf(indexDebutLangue, posLangue) + indexDebutLangue.length();
						int posFinLangue = content.indexOf(indexfinLangue, posLangue);
						langue = content.substring(posDebutLangue, posFinLangue);
						// Recherche des langues suivantes.
						while(content.substring(posFinLangue+indexfinLangue.length(), posFinLangue+indexfinLangue.length()+1).equalsIgnoreCase(",") 
						|| content.substring(posFinLangue+indexfinLangue.length(), posFinLangue+indexfinLangue.length()+2).equalsIgnoreCase(" ,")) {
							// Position de la monnaie suivante.
							posLangue = posFinLangue + indexfinLangue.length();
							posDebutLangue = content.indexOf(indexDebutLangue, posLangue) + indexDebutLangue.length();
							posFinLangue = content.indexOf(indexfinLangue, posLangue);
							langue += ", " + content.substring(posDebutLangue, posFinLangue);
						}

						// Récupère la densité du pays.
						String densite = "";
						int posDebutDensite = content.indexOf("densité=") + "densité=".length();
						int posFinDensite = content.indexOf("|", posDebutDensite);
						densite = content.substring(posDebutDensite, posFinDensite-2) + " hab./km²";
						
						
						String nom = paysDemande;
						String formeEtat = "";
						String roi = "";
						String presidentGouv = "";
						String capitale = "";
						String gouvernement = "";
						String premierMinistre = "";
						String presidentRepublique = "";
						String climat = "";
						String religion = "";
						String nombreExpatries = "";
						String tauxChomage = "";
						String indicatifTel = "";

						Pays pays = new Pays(nom, monnaie, population, formeEtat, roi, presidentGouv, langue, capitale, gouvernement, premierMinistre, 
											presidentRepublique, climat, superficie, densite, religion, pib, nombreExpatries, tauxChomage, indicatifTel, fuseauHoraire);

						return pays;
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

  	public Pays analyseDataLNR(String... params) 
  	{
  		try {
  			String paysDemande = params[0];
			String feed = "http://france-expatries.com/fex_app/pays.php?pays=" + paysDemande;
			//Log.i("feed_infos", "feed_infos : " + feed);

			// Analyse des données de l'agence LNR.
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
				NodeList nl = docEle.getElementsByTagName("pays");

				if(nl != null && nl.getLength() > 0) {
					Element entry = (Element) nl.item(0);

						String nomPays = "";
						String monnaie = "";
						String population = "";
						String formeEtat = "";
						String roi = "";
						String presidentGouv = "";
						String langue = "";
						String capitale = "";
						String gouvernement = "";
						String premierMinistre = "";
						String presidentRepublique = "";
						String climat = "";
						String superficie = "";
						String densite = "";
						String religion = "";
						String pib = "";
						String nombreExpatries = "";
						String tauxChomage = "";
						String indicatifTel = "";
						String fuseauHoraire = "";

						// Tests des contenus d'entrée du xml.
						if(entry.getElementsByTagName("nom_pays").item(0).getFirstChild().getNodeValue() != null) {
							nomPays = entry.getElementsByTagName("nom_pays").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("monnaie").item(0).getFirstChild().getNodeValue() != null) {
							monnaie = entry.getElementsByTagName("monnaie").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("population").item(0).getFirstChild().getNodeValue() != null) {
							population = entry.getElementsByTagName("population").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("formeetat").item(0).getFirstChild().getNodeValue() != null) {
							formeEtat = entry.getElementsByTagName("formeetat").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("roi").item(0).getFirstChild().getNodeValue() != null) {
							roi = entry.getElementsByTagName("roi").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("presidentgouvernement").item(0).getFirstChild().getNodeValue() != null) {
							presidentGouv = entry.getElementsByTagName("presidentgouvernement").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("langue").item(0).getFirstChild().getNodeValue() != null) {
							langue = entry.getElementsByTagName("langue").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("capitale").item(0).getFirstChild().getNodeValue() != null) {
							capitale = entry.getElementsByTagName("capitale").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("gouvernement").item(0) != null) {
							gouvernement = entry.getElementsByTagName("gouvernement").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("premierministre").item(0) != null) {
							premierMinistre = entry.getElementsByTagName("premierministre").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("presidentrepublique").item(0).getFirstChild().getNodeValue() != null) {
							presidentRepublique = entry.getElementsByTagName("presidentrepublique").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("climat").item(0).getFirstChild().getNodeValue() != null) {
							climat = entry.getElementsByTagName("climat").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("superficie").item(0).getFirstChild().getNodeValue() != null) {
							superficie = entry.getElementsByTagName("superficie").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("densite").item(0).getFirstChild().getNodeValue() != null) {
							densite = entry.getElementsByTagName("densite").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("religion").item(0).getFirstChild().getNodeValue() != null) {
							religion = entry.getElementsByTagName("religion").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("pib").item(0).getFirstChild().getNodeValue() != null) {
							pib = entry.getElementsByTagName("pib").item(0).getFirstChild().getNodeValue();  
						}

						if(entry.getElementsByTagName("nombreexpatries").item(0).getFirstChild().getNodeValue() != null) {
							nombreExpatries = entry.getElementsByTagName("nombreexpatries").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("tauxchomage").item(0).getFirstChild().getNodeValue() != null) {
							tauxChomage = entry.getElementsByTagName("tauxchomage").item(0).getFirstChild().getNodeValue(); 
						}

						if(entry.getElementsByTagName("indicatiftel").item(0).getFirstChild().getNodeValue() != null) {
							indicatifTel = entry.getElementsByTagName("indicatiftel").item(0).getFirstChild().getNodeValue();
						}

						if(entry.getElementsByTagName("fuseau").item(0).getFirstChild().getNodeValue() != null) {
							fuseauHoraire = entry.getElementsByTagName("fuseau").item(0).getFirstChild().getNodeValue();
						}
						//Log.i("nomPays", "nomPays : " + nomPays);

						Pays pays = new Pays(nomPays, monnaie, population, formeEtat, roi, presidentGouv, langue, capitale, gouvernement, premierMinistre, presidentRepublique, climat, superficie, 
											densite, religion, pib, nombreExpatries, tauxChomage, indicatifTel, fuseauHoraire);

						return pays;
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

  	/**
  	 * Lancement du service.
  	 */
  	@Override
  	public int onStartCommand(Intent intent, int flags, int startId) 
  	{
  		if(intent != null) { refresh(intent.getAction()); }

  		return Service.START_NOT_STICKY;
    };

    /**
     * Exécute le thread en background.
     */
    private void refresh(String _paysUtilisateur) 
    {
    	if(lastLookup == null || lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
    		lastLookup = new AppLookupTask();
    		lastLookup.execute(_paysUtilisateur);
    	}
    }

    /**
     * Insertion / Mise à jour de la base de données.
     */
    private void saveToBDD(Pays _pays) 
    {
    	ContentResolver contentResolver = getContentResolver();

		// Construit une clause where pour vérifier si c'est un nouveau pays.
		String where = RenseignementProvider.KEY_NOM + " = '" + _pays.getNomPays() + "'";

		// Construit une clause whereArgs pour vérifier les maj. du pays ( maj de la bdd ).
    	String whereArgs = RenseignementProvider.KEY_NOM + " = " + '"' + _pays.getNomPays() + '"' + " AND " + 
							RenseignementProvider.KEY_MONNAIE + " = " + '"' + _pays.getMonnaie() + '"' + " AND " + 
							RenseignementProvider.KEY_POPULATION + " = " + '"' + _pays.getPopulation() + '"' + " AND " + 
							RenseignementProvider.KEY_FORMEETAT + " = " + '"' + _pays.getFormeEtat() + '"' + " AND " + 
							RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT + " = " + '"' + _pays.getPresidentGouvernement() + '"' + " AND " + 
							RenseignementProvider.KEY_LANGUE + " = " + '"' + _pays.getLangue() + '"' + " AND " + 
							RenseignementProvider.KEY_CAPITALE + " = " + '"' + _pays.getCapitale() + '"' + " AND " + 
							RenseignementProvider.KEY_GOUVERNEMENT + " = " + '"' + _pays.getGouvernement() + '"' + " AND " + 
							RenseignementProvider.KEY_PREMIER_MINISTRE + " = " + '"' + _pays.getPremierMinistre() + '"' + " AND " + 
							RenseignementProvider.KEY_PRESIDENT_REPUBLIQUE + " = " + '"' + _pays.getPresidentRepublique() + '"' + " AND " + 
							RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT + " = " + '"' + _pays.getPresidentGouvernement() + '"' + " AND " + 
							RenseignementProvider.KEY_CLIMAT + " = " + '"' + _pays.getClimat() + '"' + " AND " + 
							RenseignementProvider.KEY_SUPERFICIE + " = " + '"' + _pays.getSuperficie() + '"' + " AND " + 
							RenseignementProvider.KEY_DENSITE + " = " + '"' + _pays.getDensite() + '"' + " AND " + 
							RenseignementProvider.KEY_RELIGION + " = " + '"' + _pays.getReligion() + '"' + " AND " + 
							RenseignementProvider.KEY_PIB + " = " + '"' + _pays.getPIB() + '"' + " AND " + 
							RenseignementProvider.KEY_NOMBRE_EXPATRIES + " = " + '"' + _pays.getNombreExpatries() + '"' + " AND " + 
							RenseignementProvider.KEY_TAUX_CHOMAGE + " = " + '"' + _pays.getTauxChomage() + '"' + " AND " + 
							RenseignementProvider.KEY_INDICATIF_TEL + " = " + '"' + _pays.getIndicatifTel() + '"' + " AND " + 
							RenseignementProvider.KEY_FUSEAU_HORAIRE + " = " + '"' + _pays.getFuseauHoraire() + '"';

		// Si le pays est nouveau, on l'insère ( clause 'where' ).
		if(contentResolver.query(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, null, where, null, null).getCount() == 0) {
	
			ContentValues values = new ContentValues();
	
		    values.put(RenseignementProvider.KEY_NOM, 						_pays.getNomPays());
		    values.put(RenseignementProvider.KEY_MONNAIE, 					_pays.getMonnaie());
		    values.put(RenseignementProvider.KEY_POPULATION,				_pays.getPopulation());
		    values.put(RenseignementProvider.KEY_FORMEETAT, 				_pays.getFormeEtat());
		    values.put(RenseignementProvider.KEY_ROI, 						_pays.getRoi());
		    values.put(RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT, 	_pays.getPresidentGouvernement());
		    values.put(RenseignementProvider.KEY_LANGUE, 					_pays.getLangue());
		    values.put(RenseignementProvider.KEY_CAPITALE, 					_pays.getCapitale());
		    values.put(RenseignementProvider.KEY_GOUVERNEMENT, 				_pays.getGouvernement());
		    values.put(RenseignementProvider.KEY_PREMIER_MINISTRE,			_pays.getPremierMinistre());
		    values.put(RenseignementProvider.KEY_PRESIDENT_REPUBLIQUE,		_pays.getPresidentRepublique());
		    values.put(RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT,	_pays.getPresidentGouvernement());
		    values.put(RenseignementProvider.KEY_CLIMAT,					_pays.getClimat());
		    values.put(RenseignementProvider.KEY_SUPERFICIE,				_pays.getSuperficie());
		    values.put(RenseignementProvider.KEY_DENSITE,					_pays.getDensite());
		    values.put(RenseignementProvider.KEY_RELIGION,					_pays.getReligion());
		    values.put(RenseignementProvider.KEY_PIB,						_pays.getPIB());
		    values.put(RenseignementProvider.KEY_NOMBRE_EXPATRIES,			_pays.getNombreExpatries());
		    values.put(RenseignementProvider.KEY_TAUX_CHOMAGE,				_pays.getTauxChomage());
		    values.put(RenseignementProvider.KEY_INDICATIF_TEL,				_pays.getIndicatifTel());
		    values.put(RenseignementProvider.KEY_FUSEAU_HORAIRE,			_pays.getFuseauHoraire());

			contentResolver.insert(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, values);
		}

		// Si le pays est à mettre à jour, on l'update. ( clause 'whereArgs' ).
		else if(contentResolver.query(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, null, whereArgs, null, null).getCount() == 0) {
    		String whereDelete = "nom = ?";
    		// exemple de conditions :
    			/**+ " AND value2 = ?" + " AND value3 = ?";*/

    		String[] whereArgsDelete = {_pays.getNomPays()};
			// exemple  de conditions:
    			/**, string2, string3 };*/

    		contentResolver.delete(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, whereDelete, whereArgsDelete);

    		ContentValues values = new ContentValues();

		    values.put(RenseignementProvider.KEY_NOM, 						_pays.getNomPays());
		    values.put(RenseignementProvider.KEY_MONNAIE, 					_pays.getMonnaie());
		    values.put(RenseignementProvider.KEY_POPULATION,				_pays.getPopulation());
		    values.put(RenseignementProvider.KEY_FORMEETAT, 				_pays.getFormeEtat());
		    values.put(RenseignementProvider.KEY_ROI, 						_pays.getRoi());
		    values.put(RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT, 	_pays.getPresidentGouvernement());
		    values.put(RenseignementProvider.KEY_LANGUE, 					_pays.getLangue());
		    values.put(RenseignementProvider.KEY_CAPITALE, 					_pays.getCapitale());
		    values.put(RenseignementProvider.KEY_GOUVERNEMENT, 				_pays.getGouvernement());
		    values.put(RenseignementProvider.KEY_PREMIER_MINISTRE,			_pays.getPremierMinistre());
		    values.put(RenseignementProvider.KEY_PRESIDENT_REPUBLIQUE,		_pays.getPresidentRepublique());
		    values.put(RenseignementProvider.KEY_PRESIDENT_GOUVERNEMENT,	_pays.getPresidentGouvernement());
		    values.put(RenseignementProvider.KEY_CLIMAT,					_pays.getClimat());
		    values.put(RenseignementProvider.KEY_SUPERFICIE,				_pays.getSuperficie());
		    values.put(RenseignementProvider.KEY_DENSITE,					_pays.getDensite());
		    values.put(RenseignementProvider.KEY_RELIGION,					_pays.getReligion());
		    values.put(RenseignementProvider.KEY_PIB,						_pays.getPIB());
		    values.put(RenseignementProvider.KEY_NOMBRE_EXPATRIES,			_pays.getNombreExpatries());
		    values.put(RenseignementProvider.KEY_TAUX_CHOMAGE,				_pays.getTauxChomage());
		    values.put(RenseignementProvider.KEY_INDICATIF_TEL,				_pays.getIndicatifTel());
		    values.put(RenseignementProvider.KEY_FUSEAU_HORAIRE,			_pays.getIndicatifTel());

    		contentResolver.insert(RenseignementProvider.CONTENT_RENSEIGNEMENT_URI, values);
    	}

    }

}