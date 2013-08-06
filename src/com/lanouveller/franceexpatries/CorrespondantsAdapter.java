package com.lanouveller.franceexpatries;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lanouveller.franceexpatries.Correspondants.ImageGestion;

public class CorrespondantsAdapter extends ArrayAdapter<Correspondant> {

	int resource;

	public CorrespondantsAdapter(Context context, int resource, List<Correspondant> items) {
		super(context, resource, items);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout;

		Correspondant correspondant = this.getItem(position);

		String sexe = correspondant.getSexe();

		if(convertView == null) {
			linearLayout = new LinearLayout(getContext());
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutInflater.inflate(resource, linearLayout, true);
		}
		else {
			linearLayout = (LinearLayout) convertView;
		}

    	// Initialisation des vues [layout principal]. La configuration se situe dans les XML.
		TextView nomTv = (TextView) linearLayout.findViewById(R.id.liste_correspondant_presentation);
    	ImageView imageIv = (ImageView) linearLayout.findViewById(R.id.liste_correspondant_image);

    	// Initialisation du nom ou prénom du correspondant.
    	nomTv.setText(correspondant.toString());

    	if(!correspondant.getNom().equalsIgnoreCase("Nom du correspondant")) {
    		nomTv.setTextColor(Color.BLACK);
    	}

    	// Initialisation de l'image du correspondant.
		if(ImageGestion.getImagesGestion() != null) {
			boolean hasImage = false;

			for(int i=0; i<ImageGestion.getImagesGestion().size(); i++) {
				if(ImageGestion.getNomImagesGestion().get(i).equalsIgnoreCase(correspondant.getNomImage())) {
		    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		    		imageParams.setMargins(20, 0, 0, 0);
					imageIv.setImageBitmap(Bitmap.createScaledBitmap(ImageGestion.getImagesGestion().get(i), 90, 120, true));
					hasImage = true;
				}
			}

    		if(!hasImage && sexe.equalsIgnoreCase("homme")) {
	    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    		imageParams.setMargins(20, 0, 0, 0);
	    		imageIv.setImageResource(R.drawable.icon_fiche_correspondant_homme);
    		}
    		else if(!hasImage && sexe.equalsIgnoreCase("femme")) {
	    		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    		imageParams.setMargins(20, 0, 0, 0);
	    		imageIv.setImageResource(R.drawable.icon_fiche_correspondant_femme);
    		}
		}

    	return linearLayout;
  	}

}