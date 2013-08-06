package com.lanouveller.franceexpatries;

import java.util.ArrayList;

public class Pays {

	private String nom;
	private String monnaie;
	private String population;
	private String formeEtat;
	private String roi;
	private String presidentGouvernement;
	private String langue;
	private String capitale;
	private String gouvernement;
	private String premierMinistre;
	private String presidentRepublique;
	private String climat;
	private String superficie;
	private String densite;
	private String religion;
	private String pib;
	private String nombreExpatries;
	private String tauxChomage;
	private String indicatifTel;
	private String fuseauHoraire;

	private ArrayList<String> telUrgences;
	private ArrayList<String> ambassades;
	private ArrayList<String> ecoles;
	private ArrayList<String> sorties;
	private ArrayList<String> moyensLogement;
	private ArrayList<String> alertesSanitaire;

	public Pays() {  }

	public Pays(String nomPays, String monnaie, String population, String formeEtat, String roi,  String presidentGouv, String langue, String capitale, 
			String gouvernement, String premierMinistre, String presidentRepublique, String climat, String superficie, String densite, String religion, 
			String pib, String nombreExpatries, String tauxChomage, String indicatifTel, String fuseauHoraire) 
	{
		this.nom = nomPays;
		this.monnaie = monnaie;
		this.population = population;
		this.formeEtat = formeEtat;
		this.roi = roi;
		this.presidentGouvernement = presidentGouv;
		this.langue = langue;
		this.capitale = capitale;
		this.gouvernement = gouvernement;
		this.premierMinistre = premierMinistre;
		this.presidentRepublique = presidentRepublique;
		this.climat = climat;
		this.superficie = superficie;
		this.densite = densite;
		this.religion = religion;
		this.pib = pib;
		this.nombreExpatries = nombreExpatries;
		this.tauxChomage = tauxChomage;
		this.indicatifTel = indicatifTel;
		this.fuseauHoraire = fuseauHoraire;

		this.telUrgences = new ArrayList<String>();
		this.ambassades = new ArrayList<String>();
		this.ecoles = new ArrayList<String>();
		this.sorties = new ArrayList<String>();
		this.moyensLogement = new ArrayList<String>();
		this.alertesSanitaire = new ArrayList<String>();
	}

	public void saveBDD() { }

	public String getNomPays() { return this.nom; }
	public String getMonnaie() { return this.monnaie; }
	public String getPopulation() { return this.population; }
	public String getFormeEtat() { return this.formeEtat; }
	public String getRoi() { return this.roi; }
	public String getPresidentGouvernement() { return this.presidentGouvernement; }
	public String getLangue() { return this.langue; }
	public String getCapitale() { return this.capitale; }
	public String getGouvernement() { return this.gouvernement; }
	public String getPremierMinistre() { return this.premierMinistre; }
	public String getPresidentRepublique() { return this.presidentRepublique; }
	public String getClimat() { return this.climat; }
	public String getSuperficie() { return this.superficie; }
	public String getDensite() { return this.densite; }
	public String getReligion() { return this.religion; }
	public String getPIB() { return this.pib; }
	public String getNombreExpatries() { return this.nombreExpatries; }
	public String getTauxChomage() { return this.tauxChomage; }
	public String getIndicatifTel() { return this.indicatifTel; }
	public String getFuseauHoraire() { return this.fuseauHoraire; }

	public ArrayList<String> getTelUrgences() { return this.telUrgences; }
	public ArrayList<String> getAmbassades() { return this.ambassades; }
	public ArrayList<String> getEcoles() { return this.ecoles; }
	public ArrayList<String> getSorties() { return this.sorties; }
	public ArrayList<String> getMoyensLogement() { return this.moyensLogement; }
	public ArrayList<String> getAlertesSanitaire() { return this.alertesSanitaire; }

}
