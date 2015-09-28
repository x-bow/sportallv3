package dekirasoft.sportmeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by prateekarora on 27/09/15.
 */
public class ViewUtils {

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static void showToast(String message, Context ctx) {
        WeakReference<Context> weakReference = new WeakReference<Context>(ctx);
        Toast.makeText(weakReference.get(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorAlert(String message, Context ctx) {
        WeakReference<Context> weakReference = new WeakReference<Context>(ctx);
        new AlertDialog.Builder(weakReference.get())
                .setTitle("Error!")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showSuccessAlert(String message, Context ctx) {
        WeakReference<Context> weakReference = new WeakReference<Context>(ctx);
        new AlertDialog.Builder(weakReference.get())
                .setTitle("Success!")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
