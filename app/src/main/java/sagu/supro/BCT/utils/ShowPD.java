package sagu.supro.BCT.utils;

import android.app.AlertDialog;
import android.content.Context;

import dmax.dialog.SpotsDialog;

public class ShowPD {

    private AlertDialog progressDialog;
    private Context context;

    public ShowPD(Context context){
        this.context=context;
    }

    public void displayPD(String message){
        progressDialog = new SpotsDialog(context, message);
        progressDialog.show();
    }

    public void dismissPD(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
