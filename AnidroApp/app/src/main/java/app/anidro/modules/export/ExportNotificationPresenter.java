package app.anidro.modules.export;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

import app.anidro.R;
import app.anidro.models.FileType;

/**
 * A helper class which manages the status bar notifications shown while drawing export is running and
 * when an export has successfully finished.
 * <p/>
 * Created by luboganev on 24/09/15.
 */
public class ExportNotificationPresenter {

    private static final String CHANNEL_ID = "export_drawing";

    private final Context context;
    private final int notificationId;
    private final NotificationCompat.Builder notifBuilder;
    private final NotificationManager notifManager;
    private final FileType fileType;

    public ExportNotificationPresenter(Context applicationContext, FileType fileType) {
        this.context = applicationContext;
        this.fileType = fileType;
        this.notificationId = new Random().nextInt();
        this.notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            buildNotificationChannel(applicationContext, this.notifManager);
            this.notifBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            this.notifBuilder = new NotificationCompat.Builder(context);
        }
        initCommonBuilderProperties();
    }

    @RequiresApi(26)
    private void buildNotificationChannel(Context applicationContext, NotificationManager notificationManager) {
        notificationManager.createNotificationChannel(
                new NotificationChannel(CHANNEL_ID,
                        applicationContext.getResources().getString(R.string.export_notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
        );
    }

    public void showExportNotificationBegin(int max) {
        updateExportNotificationProgress(0, max);
    }

    public void updateExportNotificationProgress(int current, int max) {
        switch (fileType) {
            case IMAGE:
                notifBuilder.setContentText(context.getString(R.string.export_notification_image_text))
                        .setProgress(0, 0, true);
                break;
            case GIF:
                notifBuilder.setContentText(context.getString(R.string.export_notification_gif_text))
                        .setProgress(max, current, false);
                break;
            case VIDEO:
                notifBuilder.setContentText(context.getString(R.string.export_notification_video_text))
                        .setProgress(max, current, false);
                break;
            default:
                return;
        }

        notifManager.notify(notificationId, notifBuilder.build());
    }

    public void showExportFailed() {
        notifBuilder.setContentText(context.getString(R.string.text_export_failed))
                .setProgress(0, 0, false);
        notifManager.notify(notificationId, notifBuilder.build());
    }

    public void showExportNotificationEnd(Uri fileUri, String mimeType, Bitmap thumbnail) {
        switch (fileType) {
            case IMAGE:
                notifBuilder.setContentText(context.getString(R.string.export_notification_image_done_text));
                break;
            case GIF:
                notifBuilder.setContentText(context.getString(R.string.export_notification_gif_done_text));
                break;
            case VIDEO:
                notifBuilder.setContentText(context.getString(R.string.export_notification_video_done_text));
                break;
            default:
                return;
        }

        notifBuilder.setProgress(0, 0, false);

        if (fileUri != null && thumbnail != null) {
            notifBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(thumbnail));

            Intent viewIntent = ExportedDrawingsIntentsHelper.getViewDrawingIntent(fileUri, mimeType);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, viewIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notifBuilder.setContentIntent(pendingIntent);

            Intent shareIntent = ExportedDrawingsIntentsHelper.getShareDrawingIntent(context.getString(R.string.share_to), fileUri, mimeType);
            PendingIntent sharePendingIntent = PendingIntent.getActivity(context, notificationId, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // using the vector drawable here
                notifBuilder.addAction(R.drawable.ic_share_white_24dp, context.getString(R.string.share_to), sharePendingIntent);
            } else {
                // using the standard framework icon here as fallback
                notifBuilder.addAction(android.R.drawable.ic_menu_share, context.getString(R.string.share_to), sharePendingIntent);
            }
        }

        notifManager.notify(notificationId, notifBuilder.build());
    }

    public void hideNotification() {
        notifManager.cancel(notificationId);
    }

    private void initCommonBuilderProperties() {
        notifBuilder.setContentTitle(context.getString(R.string.app_name))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_notification_export)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ResourcesCompat.getColor(context.getResources(),
                        R.color.orange_peel, null));
    }
}