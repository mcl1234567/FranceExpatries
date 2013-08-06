package com.lanouveller.franceexpatries;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// Gestion d'une boîte de dialogue ( DialogFragment ) .
// Non implémenté - l'utilisateur configure directement le pays à l'installation dans les préférences.
public class ChoixPaysDialogFragment extends DialogFragment {

  	private SharedPreferences preferences;
  	private String selectionPays;

	/**
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. 
     */
    public interface ChoixPaysDialogListener 
    {
        public void onChoixPaysPositiveClick(DialogFragment dialog);
        public void onChoixPaysNegativeClick(DialogFragment dialog);
        public void onChoixPaysFinishEditDialog(String inputText);
    }

    // Use this instance of the interface to deliver action events.
    private ChoixPaysDialogListener mListener;

    /**
     *  Override the Fragment.onAttach() method to instantiate the 'ChoixPaysDialogFragment'.
     */
    @Override
    public void onAttach(Activity activity) 
    {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the ChoixPaysDialogListener so we can send events to the host.
            mListener = (ChoixPaysDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception.
            throw new ClassCastException(activity.toString() + " must implement ChoixPaysDialogFragment");
        }
    }

    /**
     *  Création de la boîte de dialogue.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		 /**float widthScreen = preferences.getFloat(Preferences.SIZE_DEVICE, 1);*/

        // Création des éléments.
        final Spinner spinnerPays = new Spinner(context);
        final LinearLayout ll = new LinearLayout(context);
        final TextView tv = new TextView(context);

        // Configuration du layout.
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(llp);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Configuration du Textview.
        LinearLayout.LayoutParams tvp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvp.setMargins(20, 20, 10, 20);
        tv.setLayoutParams(tvp);
        tv.setTextColor(Color.WHITE);
        tv.setText("Le pays pourra être modifié, par la suite, dans les préférences de l'application.");
        /**if(widthScreen > 2.5) { tv.setTextAppearance(context, R.style.contenuInformationTablette); } 
        else { tv.setTextAppearance(context, R.style.contenuInformationSmartphone); }*/

        ll.addView(spinnerPays);
        ll.addView(tv);

        // Configuration du spinner des pays disponibles.
    	ArrayAdapter<CharSequence> adaptPays = ArrayAdapter.createFromResource(context, R.array.pays_disponibles, android.R.layout.simple_spinner_item);
    	adaptPays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinnerPays.setAdapter(adaptPays);
    	spinnerPays.setId(0);

        builder.setView(ll)
        	   .setMessage(R.string.choix_pays)
        	   .setTitle(R.string.title_choix_pays)
               .setPositiveButton(R.string.selection_pays, new DialogInterface.OnClickListener() {

            	   public void onClick(DialogInterface dialog, int id) 
            	   {
            		   String paysUtilisateur = spinnerPays.getSelectedItem().toString();
                	   Toast.makeText(getActivity(), paysUtilisateur + " a été enregistré ! ", Toast.LENGTH_LONG).show();

                 		// Sauvegarde du pays de l'utilisateur.
                 		int paysIndex = spinnerPays.getSelectedItemPosition();

                  		// Configuration des variables de 'Préférences'.
                  		Context context = getActivity();
                  		preferences = PreferenceManager.getDefaultSharedPreferences(context);

                 		// Ecriture dans constante des préférences.
                 		Editor editor = preferences.edit();
                 		editor.putString(Preferences.PREFERENCES_PAYS_UTILISATEUR, paysUtilisateur);
                 		editor.commit();

                 		selectionPays = paysUtilisateur;

                       // Send the positive button event back to the host activity.
                       mListener.onChoixPaysPositiveClick(ChoixPaysDialogFragment.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   selectionPays = "inconnu";
                       // Send the negative button event back to the host activity.
                       mListener.onChoixPaysNegativeClick(ChoixPaysDialogFragment.this);
                   }
               });
        
        // Create the AlertDialog object and return it.
        return builder.create();
    }

    public String getPays() { return this.selectionPays; }

}