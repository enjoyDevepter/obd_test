package com.mapbar.adas;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PageFragment extends BaseFragment {

    private FragmentPage page;

    public PageFragment() {
    }

    @Override
    protected View realCreateView(LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        Log.e("page", "realCreateView=--page===" + page);
        if (page == null) {
            return container;
        }
        View view = page.onCreateView();
        return view;
    }

    @Override
    protected void realDestroyView() {
        page.onDestroyView();
    }

    public void setPage(FragmentPage page) {
        Log.e("page", "setPage=" + page.getClass().getName());
        this.page = page;
        setTransparent(page.isTransparent());
        registLifeCycleListener(page);
    }

}
