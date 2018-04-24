package com.fg.franco.i_agro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class ResultDialog extends DialogFragment {

    Analyzer analyzer;

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                analyzer.analize()?
                R.string.result_dialog_message_good : R.string.result_dialog_message_bad)
                .setTitle(R.string.result_dialog_title);
        // Create the AlertDialog object and return it
        return builder.create();
    }

}