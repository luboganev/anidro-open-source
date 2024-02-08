package app.anidro.modules.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.anidro.common.DisplayMetricsConverter;
import app.anidro.common.DrawingColorsPalette;
import app.anidro.common.Navigator;
import app.anidro.common.ScreenOrientationSensor;
import app.anidro.models.FileType;
import app.anidro.models.TimedSegment;
import app.anidro.modules.export.DrawingExporter;
import app.anidro.modules.export.ExportState;
import app.anidro.modules.export.ExportedDrawingsIntentsHelper;
import app.anidro.modules.persistence.terms.AcceptTermsPersistence;
import app.anidro.renderers.FixedFrameRateRenderer;
import app.anidro.renderers.SequentialTimeNormalizer;
import app.anidro.renderers.SingleFrameTimeNormalizer;

/**
 * This class contains all the logic for managing the different states of the main UI. It also
 * communicates with any running export operation.
 * <p/>
 * Created by luboganev on 26/09/15.
 */
public class MainPresenter implements MainPresenterInput, TimedDrawingManager.TimedDrawingCallbackListener {

    /* Declarations */

    private enum UIMode {
        DRAW,
        PRESENT,
        EXPORT
    }

    /* Dependencies */

    private MainPresenterOutput view;

    private final @NonNull
    Navigator navigator;
    private final @NonNull
    AcceptTermsPersistence acceptTermsPersistence;
    private final @NonNull
    String shareIntentPickerTitle;
    private final @Nullable
    Vibrator vibrator;
    private final TimedDrawingManager timedDrawingManager;
    private final DrawingExporter drawingExporter;

    public MainPresenter(@NonNull Navigator navigator,
                         @NonNull AcceptTermsPersistence acceptTermsPersistence,
                         @NonNull String shareIntentPickerTitle,
                         @Nullable Vibrator vibrator,
                         @NonNull DrawingColorsPalette drawingColorsPalette,
                         @NonNull DisplayMetricsConverter displayMetricsConverter,
                         @NonNull DrawingExporter drawingExporter) {
        this.navigator = navigator;
        this.acceptTermsPersistence = acceptTermsPersistence;
        this.shareIntentPickerTitle = shareIntentPickerTitle;
        this.vibrator = vibrator;
        this.drawingExporter = drawingExporter;
        this.timedDrawingManager = new TimedDrawingManager(drawingColorsPalette, displayMetricsConverter);
        this.timedDrawingManager.setListener(this);
    }



    /* State */

    private UIMode uiMode = UIMode.DRAW;
    private int drawingProgress = 0;
    private FixedFrameRateRenderer exportProgressRenderer;
    private boolean isViewVisible = false;
    private Intent lastExportedFileShareIntent;
    // Used for tracking
    private int limitExceededRetryCount = 0;



    /* MainPresenterInput implementation */

    @Override
    public void setView(@NonNull MainPresenterOutput view) {
        this.view = view;

        view.attachDrawingDelegate(timedDrawingManager);
        view.notifyDrawingBrushSelected();

        if (acceptTermsPersistence.shouldAskToAcceptTerms()) {
            this.view.showAcceptTermsDialog();
        }

        drawingExporter.getExportStateLiveData().observe(view, exportState -> {
            if (exportState instanceof ExportState.InProgress) {
                if (uiMode != UIMode.EXPORT) {
                    return;
                }
                ExportState.InProgress state = (ExportState.InProgress) exportState;

                int exportProgressPercent = (int) Math.floor(((double) state.getCurrentFrameNumber()) / ((double) exportProgressRenderer.getFramesCount()) * 100.0d);
                exportProgressRenderer.renderFrame(state.getCurrentFrameNumber());
                view.updateExportProgressView(exportProgressRenderer.getCurrentFrame());
                view.updateExportProgressPercent(exportProgressPercent);
            } else if (exportState instanceof ExportState.Failed) {
                if (uiMode != UIMode.EXPORT) {
                    return;
                }

                setUIMode(UIMode.DRAW, true);
                view.showExportFailedMessage();
            } else if (exportState instanceof ExportState.Finished) {
                if (uiMode != UIMode.EXPORT) {
                    return;
                }

                setUIMode(UIMode.DRAW, true);
                ExportState.Finished state = (ExportState.Finished) exportState;
                createShareIntent(state.getUri(), state.getMimeType());
                showShareIntentIfNecessary();
            }
        });
    }

    @Override
    public void onViewShow() {
        isViewVisible = true;
        setUIMode(uiMode, false);
        showShareIntentIfNecessary();
    }

    @Override
    public void onViewHide() {
        isViewVisible = false;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public boolean onBackPressed() {
        switch (uiMode) {
            case PRESENT:
                setUIMode(UIMode.DRAW, true);
                return true;
            case EXPORT:
                drawingExporter.cancelExport();
                setUIMode(UIMode.PRESENT, true);
                return true;
            default:
            case DRAW:
                return false;
        }
    }

    @Override
    public void onPlayButtonClicked() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        vibrateShort();
        setUIMode(UIMode.PRESENT, true);
    }

    @Override
    public void onSettingsButtonClicked() {
        navigator.navigateToSettings();
    }

    @Override
    public void onActionBackClicked() {
        if (uiMode != UIMode.PRESENT) {
            return;
        }

        vibrateShort();
        setUIMode(UIMode.DRAW, true);
    }

    @Override
    public void onActionBrushClicked() {
        if (uiMode != UIMode.DRAW) {
            return;
        }
        vibrateShort();
        this.view.showSelectBrushUI(timedDrawingManager.getCurrentStrokeDPWidth(),
                timedDrawingManager.getCurrentColorIndex(),
                timedDrawingManager.getCurrentColorLightnessIndex());
    }

    @Override
    public void onActionDeleteClicked() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        drawingProgress = 0;
        vibrateShort();
        timedDrawingManager.clear();
        this.view.notifyDrawingCleared();
    }

    @Override
    public void onActionUndoClicked() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        vibrateShort();
        timedDrawingManager.removeLastDrawingSegment();
        this.view.notifyLastDrawingSegmentRemoved(timedDrawingManager.isDrawingEmpty());
    }

    @Override
    public void onAnimationClicked() {
        if (uiMode != UIMode.PRESENT) {
            return;
        }

        vibrateShort();
        setUIMode(UIMode.DRAW, true);
    }

    @Override
    public void onShareFileTypeSelected(FileType fileType) {
        if (uiMode != UIMode.PRESENT) {
            return;
        }

        vibrateShort();
        if (exportProgressRenderer != null) {
            exportProgressRenderer.destroy();
        }

        List<TimedSegment> drawing = timedDrawingManager.getTimedSegments();

        switch (fileType) {
            case IMAGE:
                exportProgressRenderer = new FixedFrameRateRenderer(drawing,
                        timedDrawingManager.getBackgroundColor(),
                        timedDrawingManager.getCanvasWidth(),
                        timedDrawingManager.getCanvasHeight(),
                        new SingleFrameTimeNormalizer(), 100, Bitmap.Config.ARGB_8888);
                break;
            case GIF:
                exportProgressRenderer = new FixedFrameRateRenderer(drawing,
                        timedDrawingManager.getBackgroundColor(),
                        timedDrawingManager.getCanvasWidth(),
                        timedDrawingManager.getCanvasHeight(),
                        new SequentialTimeNormalizer(), FixedFrameRateRenderer.GIF_FRAME_LENGTH,
                        Bitmap.Config.RGB_565);
                break;
            case VIDEO:
                exportProgressRenderer = new FixedFrameRateRenderer(drawing,
                        timedDrawingManager.getBackgroundColor(),
                        timedDrawingManager.getCanvasWidth(),
                        timedDrawingManager.getCanvasHeight(),
                        new SequentialTimeNormalizer(), FixedFrameRateRenderer.VIDEO_FRAME_LENGTH,
                        Bitmap.Config.ARGB_8888);
                break;
        }

        if (!(drawingExporter.getExportStateLiveData().getValue() instanceof ExportState.InProgress)) {
            drawingExporter.startExport(drawing,
                    timedDrawingManager.getBackgroundColor(),
                    timedDrawingManager.getCanvasWidth(),
                    timedDrawingManager.getCanvasHeight(),
                    fileType);
            this.view.updateExportProgressPercent(0);
            setUIMode(UIMode.EXPORT, true);
        }

//        if (navigator.startExportIfNotRunning(fileType, drawing, timedDrawingManager.getBackgroundColor(),
//                timedDrawingManager.getCanvasWidth(),
//                timedDrawingManager.getCanvasHeight())) {
//
//        }
    }

    @Override
    public void onCancelExportClicked() {
        if (uiMode != UIMode.EXPORT) {
            return;
        }

        setUIMode(UIMode.PRESENT, true);
        drawingExporter.cancelExport();
//        navigator.cancelRunningExport();
    }

    @Override
    public void onBrushSelected(int size, int colorIndex, int lightnessIndex) {
        timedDrawingManager.setColor(colorIndex, lightnessIndex);
        timedDrawingManager.setStrokeWidth(size);
        this.view.notifyDrawingBrushSelected();
    }

    @Override
    public void onActionBackgroundColorClicked() {
        if (uiMode != UIMode.DRAW) {
            return;
        }
        vibrateShort();
        this.view.showSelectBackgroundColorUI(timedDrawingManager.getBackgroundColorIndex(),
                timedDrawingManager.getBackgroundColorLightnessIndex());
    }

    @Override
    public void onBackgroundColorSelected(int colorIndex, int lightnessIndex) {
        timedDrawingManager.setBackgroundColor(colorIndex, lightnessIndex);
        this.view.notifyDrawingBackgroundSelected();
    }

    @Override
    public void onCanvasChanged(int width, int height, @NonNull ScreenOrientationSensor.ScreenOrientation screenOrientation) {
        if (timedDrawingManager.getCanvasWidth() != width ||
                timedDrawingManager.getCanvasHeight() != height ||
                timedDrawingManager.getScreenOrientation() != screenOrientation) {
            timedDrawingManager.setCanvasSize(width, height, screenOrientation);
            setUIMode(uiMode, false);
        }
    }

    /* AcceptTermsCallback implementation */

    @Override
    public void onAcceptTerms() {
        acceptTermsPersistence.hasAcceptedTerms();
    }

    @Override
    public void onDeclineTerms() {
        navigator.finishCurrentActivity();
    }



    /* DrawingView.DrawingViewCallbackListener implementation */

    @Override
    public void onDrawingTimeProgressChanged(int drawingTimeProgress) {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        drawingProgress = drawingTimeProgress;
        this.view.updateDrawingTimeWarning(drawingTimeProgress, true);
    }

    @Override
    public void onDrawingLimitExceeded() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        limitExceededRetryCount++;
        this.view.showUIAfterDrawingStops(true);
        this.view.showDrawingTimeExceeded();
        vibrateLong();
    }

    @Override
    public void onDrawingStarted() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        limitExceededRetryCount = 0;
        this.view.hideUIWhenDrawingStarts();
    }

    @Override
    public void onDrawingStopped() {
        if (uiMode != UIMode.DRAW) {
            return;
        }

        this.view.showUIAfterDrawingStops(false);
    }

    /* Helper methods */

    private void setUIMode(UIMode mode, boolean animate) {
        uiMode = mode;

        switch (uiMode) {
            case DRAW:
                this.view.updateDrawingTimeWarning(drawingProgress, animate);
                this.view.updatePresentationUIVisibility(false, animate, new ArrayList<>(),
                        timedDrawingManager.getBackgroundColor());
                this.view.updateExportUIVisibility(false, animate);
                this.view.updateDrawingUIVisibility(true, timedDrawingManager.isDrawingEmpty(), animate);
                break;
            case PRESENT:
                this.view.updateDrawingUIVisibility(false, timedDrawingManager.isDrawingEmpty(), animate);
                this.view.updateExportUIVisibility(false, animate);
                this.view.updatePresentationUIVisibility(true, animate,
                        timedDrawingManager.getTimedSegments(),
                        timedDrawingManager.getBackgroundColor());
                break;
            case EXPORT:
                this.view.updateDrawingUIVisibility(false, timedDrawingManager.isDrawingEmpty(), animate);
                this.view.updatePresentationUIVisibility(false, animate, new ArrayList<>(),
                        timedDrawingManager.getBackgroundColor());
                this.view.updateExportUIVisibility(true, animate);
                break;
        }
    }

    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
    }

    private void vibrateLong() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(250);
        }
    }

    private void createShareIntent(Uri fileUri, String fileMimeType) {
        lastExportedFileShareIntent = ExportedDrawingsIntentsHelper.getShareDrawingIntent(shareIntentPickerTitle, fileUri, fileMimeType);
    }

    private void showShareIntentIfNecessary() {
        if (isViewVisible && lastExportedFileShareIntent != null) {
            navigator.startActivityWithShareIntent(lastExportedFileShareIntent);
            lastExportedFileShareIntent = null;
        }
    }
}
