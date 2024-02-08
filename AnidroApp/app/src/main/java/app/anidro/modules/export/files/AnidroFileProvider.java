package app.anidro.modules.export.files;

import android.database.Cursor;
import android.net.Uri;
import androidx.core.content.FileProvider;

/**
 * A custom implementation of the file provider for the Anidro use case.
 * Includes some goodies from Commonsware for misbehaving apps.
 *
 * Created by luboganev on 19/11/2016.
 */
public class AnidroFileProvider extends FileProvider {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return(new LegacyCompatCursorWrapper(super.query(uri, projection, selection, selectionArgs, sortOrder)));
    }
}
