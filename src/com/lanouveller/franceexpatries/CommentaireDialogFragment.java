package com.lanouveller.franceexpatries;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Gestion d'une boite de dialogue ( DialogFragment ) .
 * Non implémenté . La plateforme de visualisation des messages / remarques des utilisateurs par les admins n'est pas développé.
 */
public class CommentaireDialogFragment extends DialogFragment {
	private String commentaire;

	/**
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. 
     */
    public interface CommentaireDialogListener {
        public void onCommentairePositiveClick(DialogFragment dialog);
        public void onCommentaireNegativeClick(DialogFragment dialog);
        public void onCommentaireFinishEditDialog(String inputText);
    }

    // Use this instance of the interface to deliver action events.
    private CommentaireDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the 'CommentaireDialogFragment'.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the DialogListener so we can send events to the host.
            mListener = (CommentaireDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CommentaireDialogListener");
        }
    }

    // Création de la boîte de dialogue.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Création du layout et de l'EditText'.
   		final EditText input = new EditText(getActivity());
   		LayoutParams params = new LayoutParams(50, 30);
        LinearLayout layout = new LinearLayout(getActivity());
   		input.setId(0); 
   		input.setLayoutParams(params);
   		layout.addView(input);

        builder.setView(layout)
        	   .setMessage(R.string.dialog_message)
        	   .setTitle(R.string.dialog_title)
               .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   commentaire = input.getText().toString();
                       // Send the positive button event back to the host activity.
                       mListener.onCommentairePositiveClick(CommentaireDialogFragment.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the negative button event back to the host activity.
                       mListener.onCommentaireNegativeClick(CommentaireDialogFragment.this);
                   }
               });

        // Create the AlertDialog object and return it.
        return builder.create();
    }
    
    public String getCommentaire() { return this.commentaire; }
}