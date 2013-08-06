package com.lanouveller.franceexpatries;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FichesAdapter extends ArrayAdapter<Fiche> {

	int resource;

	public FichesAdapter(Context context, int resource, List<Fiche> items) 
	{
		super(context, resource, items);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LinearLayout linearLayout;

		Fiche fiche = this.getItem(position);

		String titre = fiche.getTitre();
		String contenu = fiche.getContenu();

		if(convertView == null) {
			linearLayout = new LinearLayout(getContext());
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutInflater.inflate(resource, linearLayout, true);
		}
		else {
			linearLayout = (LinearLayout) convertView;
		}

    	TextView titreFicheTv = (TextView) linearLayout.findViewById(R.id.tv_titre_fiche);
    	TextView contenuTv = (TextView) linearLayout.findViewById(R.id.tv_contenu);
    	ImageView iconGuideIv = (ImageView) linearLayout.findViewById(R.id.iv_icon_guide);

    	// Configuration de l'icone des fiches selon le type de guide.
		if(fiche.getGuide().equalsIgnoreCase("argent")) {
			iconGuideIv.setImageResource(R.drawable.icon_fiches_finance);
		}
		else if(fiche.getGuide().equalsIgnoreCase("emploi")) {
			iconGuideIv.setImageResource(R.drawable.icon_fiches_emploi);
		}
		else if(fiche.getGuide().equalsIgnoreCase("passeport")) {
			// Mise à jour design..
		}
		else if(fiche.getGuide().equalsIgnoreCase("sante")) {
			iconGuideIv.setImageResource(R.drawable.icon_fiches_sante);
		}
		else if(fiche.getGuide().equalsIgnoreCase("transport")) {
			iconGuideIv.setImageResource(R.drawable.icon_fiches_transport);
		}
		else if(fiche.getGuide().equalsIgnoreCase("vie_culturelle")) {
			// Mise à jour design..
		}
		else if(fiche.getGuide().equalsIgnoreCase("vie_quotidienne")) {
			// Mise à jour design..
		}

		titreFicheTv.setText(titre);
		contenuTv.setText(contenu);

    	return linearLayout;
  	}

}