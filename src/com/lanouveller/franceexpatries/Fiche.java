package com.lanouveller.franceexpatries;

public class Fiche {
	private String pays;
	private String guide;
	private String titre;
	private String contenu;

	public Fiche(String _pays, String _guide, String _titre, String _contenu) 
	{
		this.pays = _pays;
		this.guide = _guide;
		this.titre = _titre;
		this.contenu = _contenu;
	}

	public String toString() 
	{
		return this.guide + " - " + this.pays;
	}

	public String getPays() { return this.pays; }
	public String getGuide() { return this.guide; }
	public String getTitre() { return this.titre; }
	public String getContenu() { return this.contenu; }

}
