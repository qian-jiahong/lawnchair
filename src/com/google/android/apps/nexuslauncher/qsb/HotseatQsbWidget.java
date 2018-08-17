package com.google.android.apps.nexuslauncher.qsb;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import ch.deletescape.lawnchair.LawnchairPreferences;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HotseatQsbWidget extends AbstractQsbLayout implements o, LawnchairPreferences.OnPreferenceChangeListener {
    public static final String KEY_DOCK_COLORED_GOOGLE = "pref_dockColoredGoogle";
    private final BroadcastReceiver DK;
    private boolean mIsGoogleColored;
    private final k Ds;

    static /* synthetic */ void a(HotseatQsbWidget hotseatQsbWidget) {
        if (hotseatQsbWidget.mIsGoogleColored != hotseatQsbWidget.isGoogleColored()) {
            hotseatQsbWidget.mIsGoogleColored = !hotseatQsbWidget.mIsGoogleColored;
            hotseatQsbWidget.dM();
        }
    }

    public HotseatQsbWidget(Context context) {
        this(context, null);
    }

    public HotseatQsbWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HotseatQsbWidget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.DK = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setColors();
            }
        };
        this.Ds = k.getInstance(context);
        setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        Utilities.getLawnchairPrefs(getContext()).addOnPreferenceChangeListener(KEY_DOCK_COLORED_GOOGLE, this);
        dW();
        super.onAttachedToWindow();
        getContext().registerReceiver(this.DK, new IntentFilter("android.intent.action.WALLPAPER_CHANGED"));
        this.Ds.a((o) this);
        dH();
        setOnFocusChangeListener(this.mActivity.mFocusHandler);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Utilities.getLawnchairPrefs(getContext()).removeOnPreferenceChangeListener(KEY_DOCK_COLORED_GOOGLE, this);
        getContext().unregisterReceiver(this.DK);
        this.Ds.b((o) this);
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull LawnchairPreferences prefs, boolean force) {
        mIsGoogleColored = isGoogleColored();
        dM();
    }

    public final void dM() {
        removeAllViews();
        setColors();
        dW();
        dy();
        dH();
    }

    private void dW() {
        y(false);
    }

    private void y(boolean z) {
        View findViewById = findViewById(R.id.g_icon);
        if (findViewById != null) {
            findViewById.setAlpha(1.0f);
        }
    }

    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i != 0) {
            y(false);
        }
    }

    public final int dL() {
        return 0;
    }

    private void setColors() {
        Dj = Ds.ef();
        View.inflate(new ContextThemeWrapper(getContext(), mIsGoogleColored ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme), R.layout.qsb_hotseat_content, this);
        ay(getResources().getColor(mIsGoogleColored ? R.color.qsb_background_hotseat_white : R.color.qsb_background_hotseat_default));
        az(ColorUtils.setAlphaComponent(Dc, Ds.ec()));
    }

    private boolean isGoogleColored() {
        if (Utilities.getLawnchairPrefs(getContext()).getDockColoredGoogle()) {
            return true;
        }
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(getContext()).getWallpaperInfo();
        return wallpaperInfo != null && wallpaperInfo.getComponent().flattenToString().equals(getContext().getString(R.string.default_live_wallpaper));
    }

    protected final int aA(int i) {
        View view = this.mActivity.getHotseat().getLayout();
        return (i - view.getPaddingLeft()) - view.getPaddingRight();
    }

    public final boolean dI() {
        return false;
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setTranslationY((float) (-c(this.mActivity)));
    }

    public void setInsets(Rect rect) {
        super.setInsets(rect);
        setVisibility(mActivity.getDeviceProfile().isVerticalBarLayout() ? View.GONE : View.VISIBLE);
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view == this) {
            startSearch("", this.Di);
        }
    }

    protected final Intent createSettingsIntent() {
        Intent addFlags = new Intent("com.google.android.apps.gsa.nowoverlayservice.PIXEL_DOODLE_QSB_SETTINGS").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456);
        int i = 0;
        List queryBroadcastReceivers = getContext().getPackageManager().queryBroadcastReceivers(addFlags, 0);
        if (!(queryBroadcastReceivers == null || queryBroadcastReceivers.isEmpty())) {
            i = 1;
        }
        return i != 0 ? addFlags : null;
    }

    public final void l(String str) {
        startSearch(str, 0);
    }

    private Intent getSearchIntent() {
        int[] array = new int[2];
        getLocationInWindow(array);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        rect.offset(array[0], array[1]);
        rect.inset(getPaddingLeft(), getPaddingTop());
        return ConfigBuilder.getSearchIntent(rect, findViewById(R.id.g_icon), mMicIconView);
    }

    @Override
    public final void startSearch(String str, int i) {
        final ConfigBuilder f = new ConfigBuilder(this, false);
        if (mActivity.getGoogleNow().startSearch(f.build(), f.getExtras())) {
            SharedPreferences devicePrefs = Utilities.getDevicePrefs(getContext());
            devicePrefs.edit().putInt("key_hotseat_qsb_tap_count", devicePrefs.getInt("key_hotseat_qsb_tap_count", 0) + 1).apply();
            mActivity.playQsbAnimation();
        } else {
            getContext().sendOrderedBroadcast(getSearchIntent(), null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (getResultCode() == 0) {
                                fallbackSearch("com.google.android.googlequicksearchbox.TEXT_ASSIST");
                            } else {
                                mActivity.playQsbAnimation();
                            }
                        }
                    }, null, 0, null, null);
        }
    }

    static int c(Launcher launcher) {
        Resources resources = launcher.getResources();
        DeviceProfile profile = launcher.getDeviceProfile();
        Rect rect = profile.getInsets();
        Rect hotseatLayoutPadding = profile.getHotseatLayoutPadding();
        float f = (((float) (((profile.hotseatBarSizePx + rect.bottom) - hotseatLayoutPadding.top) - hotseatLayoutPadding.bottom)) + (((float) profile.iconSizePx) * 0.92f)) / 2.0f;
        float f2 = ((float) rect.bottom) * 0.67f;
        return Math.round(f2 + (((((((float) (profile.hotseatBarSizePx + rect.bottom)) - f2) - f) - resources.getDimension(R.dimen.qsb_widget_height)) - ((float) profile.verticalDragHandleSizePx)) / 2.0f));
    }
}