package app.anidro.modules.export;

import android.content.Intent;
import android.net.Uri;

/**
 * A helper class which builds the {@link Intent} instances for sharing and viewing an exported drawing.
 * <p/>
 * Created by luboganev on 23/10/15.
 */
public class ExportedDrawingsIntentsHelper {

    public static Intent getShareDrawingIntent(String shareText, Uri contentUri, String mimeType) {
        Intent shareIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, contentUri)
                .setType(mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(shareIntent, shareText);
    }

    public static Intent getViewDrawingIntent(Uri contentUri, String mimeType) {
        return new Intent()
                .setAction(android.content.Intent.ACTION_VIEW)
                .setType(mimeType)
                .setData(contentUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
