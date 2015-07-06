package localhost.mojo;

import android.content.Context;
import android.app.ProgressDialog;
import android.content.DialogInterface;

/**
 * Created by jaydeep on 04/07/15.
 */
public class LoadProgressDialog {
    private ProgressDialog dialog = null;


    public void createProgressDialog(final Context context, String title, String message) {


           // dialog = GoogleApiClientSingleton.getDialog();
            dialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_DARK);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
//        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//            public void onCancel(DialogInterface dialog) {
//                if (context.getClass() == MainActivity.class) {
//                    ((MainActivity) context).showAlertDialog(R.string.loading_abort);
//                }
//                dialog = null;
//            }
  //      });


    }
    public void showProgressDialog(){
        if(dialog != null){
            dialog.show();
        }
    }

    public void closeProgressDialog(){
        if(dialog != null){
            dialog.dismiss();
        }
    }
    public ProgressDialog getDialog(){
        return dialog;
    }

}
