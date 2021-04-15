package com.dailyyoga.cn.media.example;


import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 2018/05/04 10:29
 * @description:
 */
public class BaseFragment extends Fragment  {

    public final String TAG = getClass().getName();
    private Toast mToast;
    protected Fragment mFragment;
    private boolean mFirst = true;
    public void requestFocus() {}

    /**
     * 神策采集Fragment需重写方法,勿删除
     * <p>
     * 此方法创建Fragment时会先于onCreateView()调用一次,在展示时还会调用一次,所有会先调用两次....然后展示/隐藏分别调用
     * 但是当fragment位于第一位时,fragment直接展示故之在创建时调用一次,所以我们需要在onActivityCreated时手动调用一次
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        boolean firstTemp = false;
        if (isVisibleToUser && getView() != null && mFirst) {
            firstTemp = true;
            showUserVisibleHint();
            mFirst = false;
        }
        if (isVisibleToUser && getView() != null) {
            if (!firstTemp) setUserVisibleHint();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserVisibleHint(getUserVisibleHint());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) return;
        setUserVisibleHint(getUserVisibleHint());
    }

    /** fragment初始化完成时并且可见触发,只调用一次 */
    public void showUserVisibleHint() {
    }

    /** fragment可见就会触发包括onResume(),但是showUserVisibleHint()调用时不会触发 */
    public void setUserVisibleHint() {
    }

    @Deprecated
    public void showUserVisibleHint(boolean first) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public void showToast(String message) {
        if (message == null || message.trim().isEmpty() || "null".equals(message) || getActivity() == null)
            return;
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.cancel();
            mToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public void showKeyboard(EditText editText) {
        if (editText == null) return;
        editText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    protected void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }
}
