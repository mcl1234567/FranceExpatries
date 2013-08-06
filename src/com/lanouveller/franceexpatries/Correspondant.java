package com.lanouveller.franceexpatries;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class Correspondant {
	private int numero;
	private String sexe;
	private String nom;
	private String prenom;
	private String email;
	private String pays;
	private String ville;
	private String dateNaissance;
	private String profession;
	private String nomImage;

	public Correspondant(int _numero, String _sexe, String _nom, String _prenom, String _email, String _pays, String _ville, String _dateNaissance, String _profession, String _nomImage) 
	{
		this.numero = _numero;
		this.sexe = _sexe;
		if(_sexe.equalsIgnoreCase("") || _sexe.equalsIgnoreCase(" ")) {
			this.sexe = "homme";
		}
		this.nom = _nom;
		this.prenom = _prenom;
		this.email = _email;
		this.pays = _pays;
		this.ville = _ville;
		this.dateNaissance = _dateNaissance;
		this.profession = _profession;
		this.nomImage = _nomImage;
	}

	@Override
	public String toString() 
	{
		if(this.nom.equalsIgnoreCase("Nom du correspondant")) {
			return "Correspondant #" + String.valueOf(numero);
		}
		else return this.prenom.substring(0, 1).toUpperCase() + this.prenom.substring(1).toLowerCase();
	}

	public int getNumero() { return this.numero; }
	public String getSexe() { return this.sexe; }
	public String getNom() { return this.nom; }
	public String getPrenom() { return this.prenom; }
	public String getEmail() { return this.email; }
	public String getPays() { return this.pays; }
	public String getVille() { return this.ville; }
	public String getDateNaissance() { return this.dateNaissance; }
	public String getProfession() { return this.profession; }
	public String getNomImage() { return this.nomImage; }

}
