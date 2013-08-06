package com.lanouveller.franceexpatries;

public class Article {
	private String titre;
	private String description;
	private String contenu;
	private String date;
	private String lien;

	public Article(String titre, String description, String contenu, String date, String lien) {
		this.titre = titre;
		this.description = description;
		this.contenu = contenu;
		this.date = date;
		this.lien = lien;
	}
	
	public String toString() {
		return this.titre;
	}

	public String getTitre() { return titre; }
	public String getDescription() { return description; }
	public String getContenu() { return contenu; }
	public String getDate() { return date; }
	public String getLien() { return lien; }
}