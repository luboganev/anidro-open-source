package app.anidro.modules.export.files;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.anidro.BuildConfig;
import app.anidro.models.FileType;
import timber.log.Timber;

/**
 * A helper class containing methods for handling internal and external drawing files.
 * <p/>
 * Created by luboganev on 22/09/15.
 */
public class DrawingsFileHelper {
    // Has to be the same as in the file_paths.xml
    private static final String INTERNAL_DRAWINGS_DIR = "drawings";
    private static final String EXTERNAL_DRAWINGS_DIR = "Anidro";
    private static final String DRAWING_FILE_PREFIX = "anidro_";
    private static final String DRAWING_FILE_SUFFIX_GIF = ".gif";
    private static final String DRAWING_FILE_SUFFIX_IMAGE = ".jpg";
    private static final String DRAWING_FILE_SUFFIX_VIDEO = ".mp4";

    /**
     * Creates a content Uri from a file, using the Anidro {@link FileProvider}
     * @param context
     *      The context needed by the {@link FileProvider} API
     * @param file
     *      The file to be mapped to Uri
     */
    public static Uri getFileContentUri(Context context, File file) {
        return FileProvider.getUriForFile(context, BuildConfig.FILES_AUTHORITY, file);
    }

    /**
     * Cleans up the internal files folder from old drawing files
     */
    public static void deleteDrawingsOlderThanOneDay(Context context) {
        File drawingsDir = new File(context.getFilesDir() + File.separator
                + INTERNAL_DRAWINGS_DIR);

        if (drawingsDir.exists()) {
            File[] files = drawingsDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if (file.lastModified() < (System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS)) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
    }

    /**
     * Creates a new file with unique name for the input file type under the internal files folder
     */
    @NonNull
    public static File getFreshDrawingFile(Context context, FileType fileType) {
        File drawingsDir = new File(context.getFilesDir() + File.separator
                + INTERNAL_DRAWINGS_DIR);
        
        if (!drawingsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            drawingsDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = DRAWING_FILE_PREFIX + timeStamp;
        switch (fileType) {
            case IMAGE:
                fileName += DRAWING_FILE_SUFFIX_IMAGE;
                break;
            case GIF:
                fileName += DRAWING_FILE_SUFFIX_GIF;
                break;
            case VIDEO:
                fileName += DRAWING_FILE_SUFFIX_VIDEO;
                break;
        }
        
        return new File(drawingsDir, fileName);
    }

    /**
     * Call the media store to show the newly created files in the gallery
     */
    public static void mediaStorageScan(Context context, final File externalFile) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{externalFile.getAbsolutePath()},
                null,
                null);
    }

    /**
     * Copies a local file to proper external storage folder
     */
    public static File copyToExternal(@NonNull File file, FileType fileType) {
        File externalFile = getExternalFile(fileType, file.getName());

        if (externalFile == null) {
            return null;
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(externalFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            return externalFile;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if there is an available writable external storage
     */
    private static boolean isWriteableStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Creates a new external file
     */
    private static @Nullable File getExternalFile(FileType fileType, String fileName) {
        if (!isWriteableStorageAvailable()) {
            Timber.e("External directory not available for file type " + fileType);
            return null;
        }

        String publicDirType;
        switch (fileType) {
            case VIDEO:
                publicDirType = Environment.DIRECTORY_MOVIES;
                break;
            case IMAGE:
            case GIF:
            default:
                publicDirType = Environment.DIRECTORY_PICTURES;
                break;
        }

        File drawingsDir = new File(Environment.getExternalStoragePublicDirectory(publicDirType) + File.separator
                + EXTERNAL_DRAWINGS_DIR);

        if (!drawingsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            drawingsDir.mkdirs();
        }

        return new File(drawingsDir, fileName);
    }
}
