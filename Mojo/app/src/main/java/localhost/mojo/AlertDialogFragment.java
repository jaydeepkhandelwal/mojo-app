package localhost.mojo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jaydeep on 02/07/15.
 */
public class AlertDialogFragment extends DialogFragment
{

   //private static Context context;
    public static AlertDialogFragment newInstance(int title) {

        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();

        args.putInt("title", title);
      //  context = activityContext;
        frag.setArguments(args);
        return frag;
    }





    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
              //  .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)

                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (getActivity().getClass().equals(MainActivity.class)) {
                                    ((MainActivity) getActivity()).doPositiveClick();
                                } else if (getActivity().getClass().equals(SplashScreen.class)) {
                                    ((SplashScreen) getActivity()).doPositiveClick();
                                }
                            }
                        }
                )
//                .setNegativeButton(R.string.alert_dialog_cancel,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((MainActivity)getActivity()).doNegativeClick();
//                            }
//                        }
//                )
                .create();
    }
}