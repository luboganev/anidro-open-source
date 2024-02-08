package app.anidro.modules.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.koin.core.module.Module;
import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.List;

import app.anidro.R;
import app.anidro.common.BaseAnidroActivity;
import app.anidro.common.ScreenOrientationFlipSensor;
import app.anidro.common.ScreenOrientationSensor;
import app.anidro.databinding.ActivityMainBinding;
import app.anidro.models.FileType;
import app.anidro.models.TimedSegment;
import app.anidro.modules.main.terms.AcceptTermsCallback;
import app.anidro.modules.main.terms.AcceptTermsDialogFragment;
import app.anidro.modules.main.views.ShakeAnimator;
import app.anidro.modules.main.views.SharePopupAdapter;
import app.anidro.modules.main.views.ShowHideAnimator;
import app.anidro.modules.persistence.settings.SettingsPersistence;

/**
 * This {@link android.app.Activity} represents the main UI of the application. It contains all views
 * related to the 3 different states:
 * <ul>
 * <li>Draw</li>
 * <li>Preview</li>
 * <li>Export</li>
 * </ul>
 * <p/>
 * Most of the presentation logic is located the presenter for this screen.
 * This class calls the presenter through the {@link MainPresenterInput} and receives callbacks from
 * it by implementing the {@link MainPresenterOutput} interface.
 * <p/>
 * Created by luboganev on 24/07/15.
 */
public class MainActivity extends BaseAnidroActivity implements MainPresenterOutput,
        AcceptTermsCallback, BrushSetupDialogFragment.BrushSetupCallback,
        BackgroundColorDialogFragment.BackgroundColorCallback {

    /* Constants */
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 42;
    private static final long DELAY_SHOW_DRAWING_UI = 500L;

    /* Views */
    private ActivityMainBinding viewBinding;

    /* Common */
    private ListPopupWindow sharePopupWindow;

    /* Animators */
    private ShowHideAnimator actionDeleteAnimator;
    private ShowHideAnimator actionSetupBrushAnimator;
    private ShowHideAnimator actionSetupBackgroundAnimator;
    private ShowHideAnimator actionUndoAnimator;
    private ShowHideAnimator actionBackAnimator;
    private ShowHideAnimator actionSettingsAnimator;
    private ShowHideAnimator maxDrawingTimeWarningAnimator;
    private ShakeAnimator maxDrawingTimeWarningShakeAnimator;

    /* Dependencies */
    private MainPresenterInput presenter;
    private SettingsPersistence settingsPersistence;
    private ScreenOrientationFlipSensor screenOrientationFlipSensor;

    /* State */
    private FileType selectedExportFileType = FileType.NONE;

    /* Activity methods */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            presenter = KoinJavaComponent.get(MainPresenterInput.class);
        } catch (Exception e) {
            KoinJavaComponent.getKoin().loadModules(getScopedModules());
            presenter = KoinJavaComponent.get(MainPresenterInput.class);
        }
        settingsPersistence = KoinJavaComponent.get(SettingsPersistence.class);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        setupClickListeners();

        actionDeleteAnimator = new ShowHideAnimator(viewBinding.actionDelete);
        actionSetupBrushAnimator = new ShowHideAnimator(viewBinding.actionSetupBrush);
        actionSetupBackgroundAnimator = new ShowHideAnimator(viewBinding.actionSetupBackground);
        actionUndoAnimator = new ShowHideAnimator(viewBinding.actionUndo);
        actionBackAnimator = new ShowHideAnimator(viewBinding.actionBack);
        actionSettingsAnimator = new ShowHideAnimator(viewBinding.actionSettings);
        maxDrawingTimeWarningAnimator = new ShowHideAnimator(viewBinding.maxDrawingTimeWarning);
        maxDrawingTimeWarningShakeAnimator = new ShakeAnimator(viewBinding.maxDrawingTimeWarning);
        setupSharePopupWindow();

        // Setup orientation changes sensing
        screenOrientationFlipSensor = new ScreenOrientationFlipSensor(this);
        screenOrientationFlipSensor.enableSensor(onScreenOrientationFlippedListener);
        viewBinding.drawingView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        viewBinding.animationView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        viewBinding.exportProgressView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        presenter.setView(this);
    }

    // Notify the presenter for any changes in the canvas size
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener = this::handleCanvasChanged;

    // Notify the presenter for any changes in the canvas size
    private ScreenOrientationFlipSensor.OnScreenOrientationFlippedListener onScreenOrientationFlippedListener = this::handleCanvasChanged;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.drawingView.removeCallbacks(mDelayedShowDrawingUIRunnable);
        viewBinding.animationView.stopAnimation();
        screenOrientationFlipSensor.disableSensor();
        if (isFinishing()) {
            presenter.onDestroy();
            KoinJavaComponent.getKoin().unloadModules(getScopedModules());
        }
    }

    @Override
    public void onBackPressed() {
        if (presenter.onBackPressed()) {
            // If presenter handles the back press, do not call the super
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onViewShow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onViewHide();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle dynamic permission result in android M
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                settingsPersistence.setCopyToExternal(false);
                Toast.makeText(MainActivity.this, R.string.settings_storage_permission_denied, Toast.LENGTH_LONG).show();
            }

            continueExport();
        }
    }



    /* MainPresenterOutput implementation */

    @Override
    public void attachDrawingDelegate(@NonNull DrawingDelegate drawingDelegate) {
        viewBinding.drawingView.setDrawingDelegate(drawingDelegate);
    }

    @Override
    public void showAcceptTermsDialog() {
        if (!AcceptTermsDialogFragment.isAlreadyShown(getSupportFragmentManager())) {
            AcceptTermsDialogFragment fragment = new AcceptTermsDialogFragment();
            fragment.show(getSupportFragmentManager(), AcceptTermsDialogFragment.TAG);
        }
    }

    @Override
    public void updateDrawingUIVisibility(boolean isDrawing, boolean isDrawingEmpty, boolean animate) {
        if (isDrawing) {
            viewBinding.drawingView.redraw();
            viewBinding.drawingView.setVisibility(View.VISIBLE);
            if (!isDrawingEmpty) {
                showNonEmptyDrawingButtons(animate);
            } else {
                hideNonEmptyDrawingButtons(false);
            }
            showDrawingButtons(animate);
        } else {
            viewBinding.drawingView.setVisibility(View.INVISIBLE);
            hideDrawingButtons(false);
            hideNonEmptyDrawingButtons(false);
            maxDrawingTimeWarningAnimator.hide(false);
        }
    }

    @Override
    public void updatePresentationUIVisibility(boolean isPresenting, boolean animate, @NonNull List<TimedSegment> segments, @ColorInt int backgroundColor) {
        if (isPresenting) {
            viewBinding.animationView.stopAnimation();
            viewBinding.animationView.startAnimation(segments, backgroundColor);
            viewBinding.animationView.setVisibility(View.VISIBLE);
            actionBackAnimator.show(animate);
            viewBinding.fabShareButton.show(animate);
        } else {
            viewBinding.animationView.stopAnimation();
            viewBinding.animationView.setVisibility(View.INVISIBLE);
            actionBackAnimator.hide(false);
            viewBinding.fabShareButton.hide(false);
        }
    }

    @Override
    public void updateExportUIVisibility(boolean isExporting, boolean animate) {
        if (isExporting) {
            viewBinding.exportProgressView.setVisibility(View.VISIBLE);
            viewBinding.exportProgessBar.setVisibility(View.VISIBLE);
            viewBinding.fabCancelExport.show(animate);
            viewBinding.dimmedBackground.setVisibility(View.VISIBLE);
        } else {
            viewBinding.exportProgressView.setImageBitmap(null);
            viewBinding.exportProgressView.setVisibility(View.INVISIBLE);
            viewBinding.exportProgessBar.setVisibility(View.INVISIBLE);
            viewBinding.fabCancelExport.hide(false);
            viewBinding.dimmedBackground.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void notifyDrawingCleared() {
        hideNonEmptyDrawingButtons(true);
        viewBinding.drawingView.clearCanvas();
    }

    @Override
    public void notifyLastDrawingSegmentRemoved(boolean isDrawingEmpty) {
        viewBinding.drawingView.redraw();
        if (isDrawingEmpty) {
            hideNonEmptyDrawingButtons(true);
        }
    }

    @Override
    public void updateDrawingTimeWarning(int drawingTimeProgress, boolean animate) {
        viewBinding.drawingProgress.setCurrentPercent(drawingTimeProgress);
        if (drawingTimeProgress > 60) {
            maxDrawingTimeWarningAnimator.show(animate);
        } else {
            maxDrawingTimeWarningAnimator.hide(animate);
        }
    }

    @Override
    public void showDrawingTimeExceeded() {
        maxDrawingTimeWarningShakeAnimator.shake();
    }

    @Override
    public void showUIAfterDrawingStops(boolean immediately) {
        if (viewBinding.fabPlayButton.getVisibility() != View.VISIBLE) {
            viewBinding.drawingView.removeCallbacks(mDelayedShowDrawingUIRunnable);
            if (immediately) {
                mDelayedShowDrawingUIRunnable.run();
            } else {
                viewBinding.drawingView.postDelayed(mDelayedShowDrawingUIRunnable, DELAY_SHOW_DRAWING_UI);
            }
        }
    }

    @Override
    public void hideUIWhenDrawingStarts() {
        viewBinding.drawingView.removeCallbacks(mDelayedShowDrawingUIRunnable);
        hideNonEmptyDrawingButtons(true);
        hideDrawingButtons(true);
    }

    @Override
    public void updateExportProgressView(@NonNull Bitmap frame) {
        viewBinding.exportProgressView.setImageBitmap(frame);
    }

    @Override
    public void updateExportProgressPercent(int exportProgressPercent) {
        viewBinding.exportProgessBar.setCurrentPercent(exportProgressPercent);
    }

    @Override
    public void showExportFailedMessage() {
        Toast.makeText(this, R.string.text_export_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSelectBrushUI(int size, int colorIndex, int lightnessIndex) {
        if (!BrushSetupDialogFragment.isAlreadyShown(getSupportFragmentManager())) {
            BrushSetupDialogFragment fragment = BrushSetupDialogFragment.getInstance(size, colorIndex, lightnessIndex);
            fragment.show(getSupportFragmentManager(), BrushSetupDialogFragment.TAG);
        }
    }

    @Override
    public void notifyDrawingBrushSelected() {
        viewBinding.drawingView.onBrushChanged();
    }

    @Override
    public void showSelectBackgroundColorUI(int colorIndex, int lightnessIndex) {
        if (!BackgroundColorDialogFragment.isAlreadyShown(getSupportFragmentManager())) {
            BackgroundColorDialogFragment fragment = BackgroundColorDialogFragment.getInstance(colorIndex, lightnessIndex);
            fragment.show(getSupportFragmentManager(), BackgroundColorDialogFragment.TAG);
        }
    }

    @Override
    public void notifyDrawingBackgroundSelected() {
        viewBinding.drawingView.redraw();
    }



    /* Click listeners */

    private void setupClickListeners() {
        viewBinding.fabPlayButton.setOnClickListener(view -> onFabPlayClick());
        viewBinding.fabShareButton.setOnClickListener(view -> onFabShare());
        viewBinding.actionSettings.setOnClickListener(view -> onActionAboutClick());
        viewBinding.actionBack.setOnClickListener(view -> onActionBackClick());
        viewBinding.animationView.setOnClickListener(view -> onAnimationClick());
        viewBinding.actionDelete.setOnClickListener(view -> onActionDeleteClick());
        viewBinding.actionSetupBrush.setOnClickListener(view -> onSetupBrushClick());
        viewBinding.actionSetupBackground.setOnClickListener(view -> onSetupBackgroundClick());
        viewBinding.actionUndo.setOnClickListener(view -> onActionUndoClick());
        viewBinding.fabCancelExport.setOnClickListener(view -> onFabCancelExport());
    }

    private void onFabPlayClick() {
        presenter.onPlayButtonClicked();
    }

    private void onFabShare() {
        if (!sharePopupWindow.isShowing()) {
            viewBinding.dimmedBackground.setVisibility(View.VISIBLE);
            sharePopupWindow.show();
        }
    }

    private void onActionAboutClick() {
        presenter.onSettingsButtonClicked();
    }

    private void onActionBackClick() {
        presenter.onActionBackClicked();
    }

    private void onAnimationClick() {
        presenter.onAnimationClicked();
    }

    private void onActionDeleteClick() {
        presenter.onActionDeleteClicked();
    }

    private void onSetupBrushClick() {
        presenter.onActionBrushClicked();
    }

    private void onSetupBackgroundClick() {
        presenter.onActionBackgroundColorClicked();
    }

    private void onActionUndoClick() {
        presenter.onActionUndoClicked();
    }

    private void onFabCancelExport() {
        presenter.onCancelExportClicked();
    }

    /* AcceptTermsCallback implementation */

    @Override
    public void onAcceptTerms() {
        presenter.onAcceptTerms();
    }

    @Override
    public void onDeclineTerms() {
        presenter.onDeclineTerms();
    }

    /* Helper methods */

    private void showDrawingButtons(boolean animate) {
        actionSettingsAnimator.show(animate);
        actionSetupBrushAnimator.show(animate);
        actionSetupBackgroundAnimator.show(animate);
    }

    private void hideDrawingButtons(boolean animate) {
        actionSettingsAnimator.hide(animate);
        actionSetupBrushAnimator.hide(animate);
        actionSetupBackgroundAnimator.hide(animate);
    }

    private void showNonEmptyDrawingButtons(boolean animate) {
        actionDeleteAnimator.show(animate);
        actionUndoAnimator.show(animate);
        viewBinding.fabPlayButton.show(animate);
    }

    private void hideNonEmptyDrawingButtons(boolean animate) {
        actionDeleteAnimator.hide(animate);
        actionUndoAnimator.hide(animate);
        viewBinding.fabPlayButton.hide(animate);
    }

    private void setupSharePopupWindow() {
        sharePopupWindow = new ListPopupWindow(this);
        sharePopupWindow.setAnchorView(viewBinding.sharePopupAnchor);
        sharePopupWindow.setAdapter(new SharePopupAdapter());
        sharePopupWindow.setModal(true);
        sharePopupWindow.setOnDismissListener(() -> viewBinding.dimmedBackground.setVisibility(View.INVISIBLE));
        sharePopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            sharePopupWindow.dismiss();
            selectedExportFileType = (FileType) view.getTag();

            // If copy to external setting is on, then we need to ask
            // permission on Android M and newer versions
            if (settingsPersistence.shouldCopyToExternal()
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                return;
            }

            continueExport();
        });

    }

    private void continueExport() {
        switch (selectedExportFileType) {
            case IMAGE:
            case GIF:
            case VIDEO:
                //noinspection ResourceType
                presenter.onShareFileTypeSelected(selectedExportFileType);
                break;
            default:
                break;
        }
        selectedExportFileType = FileType.NONE;
    }

    private Runnable mDelayedShowDrawingUIRunnable = () -> {
        showDrawingButtons(true);
        showNonEmptyDrawingButtons(true);
    };

    @Override
    public void onSetupBrush(int size, int colorIndex, int lightnessIndex) {
        presenter.onBrushSelected(size, colorIndex, lightnessIndex);
    }

    @Override
    public void onSelectBackgroundColor(int colorIndex, int lightnessIndex) {
        presenter.onBackgroundColorSelected(colorIndex, lightnessIndex);
    }

    private void handleCanvasChanged() {
        final ScreenOrientationSensor.ScreenOrientation screenOrientation =
                ScreenOrientationSensor.getOrientation(MainActivity.this);

        if (viewBinding.drawingView.getWidth() > 0 && viewBinding.drawingView.getHeight() > 0) {
            presenter.onCanvasChanged(viewBinding.drawingView.getWidth(), viewBinding.drawingView.getHeight(), screenOrientation);
            return;
        }

        if (viewBinding.animationView.getWidth() > 0 && viewBinding.animationView.getHeight() > 0) {
            presenter.onCanvasChanged(viewBinding.animationView.getWidth(), viewBinding.animationView.getHeight(), screenOrientation);
            return;
        }

        if (viewBinding.exportProgressView.getWidth() > 0 && viewBinding.exportProgressView.getHeight() > 0) {
            presenter.onCanvasChanged(viewBinding.exportProgressView.getWidth(), viewBinding.exportProgressView.getHeight(), screenOrientation);
        }
    }

    private List<Module> getScopedModules() {
        List<Module> modules = new ArrayList<>(1);
        modules.add(MainModuleKt.getMainPresenterModule());
        return modules;
    }
}
