package com.google.android.apps.nexuslauncher.qsb;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener;
import com.android.launcher3.util.Themes;
import com.google.android.apps.nexuslauncher.search.SearchThread;

@TargetApi(24)
public class AllAppsQsbLayout extends AbstractQsbLayout implements SearchUiManager, OnChangeListener, o {
    private final k Ds;
    private final int Dt;
    private int Du;
    private Bitmap Dv;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    private float Dy;
    private TextView Dz;
    private AllAppsContainerView mAppsView;
    boolean mDoNotRemoveFallback;

    public AllAppsQsbLayout(Context context) {
        this(context, null);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.Du = 0;
        setOnClickListener(this);
        this.Ds = k.getInstance(context);
        this.Dt = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        this.Dy = getTranslationY();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        dz();
        this.Dz = findViewById(R.id.qsb_hint);
    }

    public void setInsets(Rect rect) {
        c(Utilities.getDevicePrefs(getContext()));
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
        marginLayoutParams.topMargin = Math.max((int) (-this.Dy), rect.top - this.Dt);
        requestLayout();
        if (this.mActivity.getDeviceProfile().isVerticalBarLayout()) {
            this.mActivity.mAllAppsController.setScrollRangeDelta(0.0f);
            return;
        }
//        this.mActivity.mAllAppsController.setScrollRangeDelta(0.0f);
        this.mActivity.mAllAppsController.setScrollRangeDelta(((float) HotseatQsbWidget.c(this.mActivity)) + (((float) (marginLayoutParams.height + marginLayoutParams.topMargin)) + this.Dy));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(getContext());
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        dN();
        Ds.a(this);
    }

    protected void onDetachedFromWindow() {
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        super.onDetachedFromWindow();
        Ds.b(this);
    }

    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        boolean isDarkTheme = Themes.getAttrBoolean(mActivity, R.attr.isMainColorDark);
        boolean isDarkBar = Utilities.getLawnchairPrefs(getContext()).getDarkSearchbar();
        int colorRes;
        if (isDarkBar) {
            colorRes = R.color.qsb_background_drawer_dark_bar;
        } else {
            colorRes = isDarkTheme ? R.color.qsb_background_drawer_dark : R.color.qsb_background_drawer_default;
        }
        int color = getResources().getColor(colorRes);
        ay(ColorUtils.compositeColors(ColorUtils.compositeColors(color, Themes.getAttrColor(mActivity, R.attr.allAppsScrimColor)), wallpaperColorInfo.getMainColor()));
    }

    protected final int aA(int i) {
        if (this.mActivity.getDeviceProfile().isVerticalBarLayout()) {
            return (i - this.mAppsView.getActiveRecyclerView().getPaddingLeft()) - this.mAppsView.getActiveRecyclerView().getPaddingRight();
        }
        View view = this.mActivity.getHotseat().getLayout();
        return (i - view.getPaddingLeft()) - view.getPaddingRight();
    }

    public final void initialize(AllAppsContainerView allAppsContainerView) {
        this.mAppsView = allAppsContainerView;
        int i = 0;
        mAppsView.addElevationController(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                aD(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(true);
    }

    public final void dM() {
        dN();
        invalidate();
    }

    private void dN() {
        az(this.Dc);
        h(this.Ds.ed());
        this.Dh = this.Ds.eh();
        this.Dj = this.Ds.ef();
        a(this.Ds.ee(), this.Dz);
        dH();
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view == this) {
            startSearch("", this.Di);
        }
    }

    public final void l(String str) {
        startSearch(str, 0);
    }

    @Override
    public final void startSearch(String str, int i) {
        if (!Utilities.ATLEAST_NOUGAT || !Utilities.getLawnchairPrefs(getContext()).getAllAppsGoogleSearch()) {
            searchFallback();
            return;
        }
        final ConfigBuilder f = new ConfigBuilder(this, true);
        if (!mActivity.getGoogleNow().startSearch(f.build(), f.getExtras())) {
            searchFallback();
            if (mFallback != null) {
                mFallback.setHint(null);
            }
        }
    }

    public void searchFallback() {
        ensureFallbackView();
        mFallback.showKeyboard();
    }

    public final void resetSearch() {
        aD(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    private void ensureFallbackView() {
        setOnClickListener(null);
        mFallback = (FallbackAppsSearchView) this.mActivity.getLayoutInflater().inflate(R.layout.all_apps_google_search_fallback, this, false);
        AllAppsContainerView allAppsContainerView = this.mAppsView;
        mFallback.DJ = this;
        mFallback.mApps = allAppsContainerView.getApps();
        mFallback.mAppsView = allAppsContainerView;
        mFallback.DI.initialize(new SearchThread(mFallback.getContext()), mFallback, Launcher.getLauncher(mFallback.getContext()), mFallback);
        addView(this.mFallback);
    }

    private void removeFallbackView() {
        if (mFallback != null) {
            mFallback.clearSearchResult();
            setOnClickListener(this);
            removeView(mFallback);
            mFallback = null;
        }
    }

    private void resetFallbackView() {
        if (mFallback != null) {
            mFallback.reset();
            mFallback.clearSearchResult();
        }
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        View view = (View) getParent();
        setTranslationX((float) ((view.getPaddingLeft() + ((((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - (i3 - i)) / 2)) - i));
    }

    public void draw(Canvas canvas) {
        if (this.Du > 0) {
            if (this.Dv == null) {
                this.Dv = c(getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius), getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset), 0);
            }
            this.CW.paint.setAlpha(this.Du);
            a(this.Dv, canvas);
            this.CW.paint.setAlpha(255);
        }
        super.draw(canvas);
    }

    final void aD(int i) {
        i = Utilities.boundToRange(i, 0, 255);
        if (this.Du != i) {
            this.Du = i;
            invalidate();
        }
    }

    protected final boolean dK() {
        if (this.mFallback != null) {
            return false;
        }
        return super.dK();
    }

    protected final void c(SharedPreferences sharedPreferences) {
        if (mUseFallbackSearch) {
            removeFallbackView();
            this.mUseFallbackSearch = false;
            ((ImageView) findViewById(R.id.g_icon)).setImageResource(R.drawable.ic_super_g_color);
            if (this.mUseFallbackSearch) {
                ensureFallbackView();
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent keyEvent) {

    }

    @Override
    public void startSearch() {
        post(this::performClick);
    }
}