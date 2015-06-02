package de.twoid.spotifystreamer.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.twoid.spotifystreamer.Error;
import de.twoid.spotifystreamer.R;

/**
 * Created by Johannes on 01.06.2015.
 */
public class EmptyLayout extends FrameLayout {

    public static final int INVALID_STATE = -1;
    public static final int STATE_DISPLAY_CONTENT = 0;
    public static final int STATE_DISPLAY_LOADING = 1;
    public static final int STATE_DISPLAY_ERROR = 2;


    @IntDef({STATE_DISPLAY_CONTENT, STATE_DISPLAY_ERROR, STATE_DISPLAY_LOADING, INVALID_STATE})
    public @interface State {

    }

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private ProgressBar loadingProgressBar;
    private TextView tvError;

    @State
    private int currentState;
    @State
    private int pendingState = INVALID_STATE;
    private de.twoid.spotifystreamer.Error error;
    private boolean isAnimating = false;

    public EmptyLayout(Context context){
        this(context, null);
    }

    public EmptyLayout(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public EmptyLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_error, this);

        if(attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EmptyLayout);

            try{
                int state = ta.getInt(R.styleable.EmptyLayout_empty_state, STATE_DISPLAY_CONTENT);
                currentState = matchState(state);

                int messageResId = ta.getResourceId(R.styleable.EmptyLayout_error_message, -1);

                if(messageResId != -1){
                    int drawableResId = ta.getResourceId(R.styleable.EmptyLayout_error_image, -1);
                    if(drawableResId != -1){
                        error = new Error(messageResId, drawableResId);
                    }else{
                        error = new Error(messageResId);
                    }
                }

            }finally{
                ta.recycle();
            }
        }

        setState(currentState);
    }

    @State
    private static int matchState(int state){
        switch(state){
            case STATE_DISPLAY_CONTENT:
                return STATE_DISPLAY_CONTENT;
            case STATE_DISPLAY_LOADING:
                return STATE_DISPLAY_LOADING;
            case STATE_DISPLAY_ERROR:
                return STATE_DISPLAY_ERROR;
            default:
                return STATE_DISPLAY_CONTENT;
        }
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        tvError = (TextView) findViewById(R.id.tv_error);

        setError(error);
    }

    public void setError(@StringRes int errorTextResId, @DrawableRes int errorImageResId){
        setError(new Error(errorTextResId, errorImageResId));
    }

    public void setError(Error error){
        this.error = error;

        if(error == null){
            tvError.setText(null);
            tvError.setCompoundDrawables(null, null, null, null);
        }else{
            error.applyText(tvError);
            error.applyImage(tvError);
        }
    }

    public void setState(@State int state){
        setState(state, false);
    }

    public void setState(@State int state, boolean animate){
        if(currentState == state){
            return;
        }

        if(isAnimating){
            pendingState = state;
            return;
        }

        pendingState = INVALID_STATE;

        switch(state){
            case STATE_DISPLAY_ERROR:
                showView(tvError, animate);
                hideView(loadingProgressBar, animate);
                hideContent(animate);
                break;
            case STATE_DISPLAY_LOADING:
                hideView(tvError, animate);
                showView(loadingProgressBar, animate);
                hideContent(animate);
                break;
            case STATE_DISPLAY_CONTENT:
                hideView(tvError, animate);
                hideView(loadingProgressBar, animate);
                showContent(animate);
                break;
        }

        currentState = state;
    }

    private void hideView(View view, boolean animate){
        switchChildVisibility(view, GONE, animate);
    }

    private void showView(View view, boolean animate){
        switchChildVisibility(view, VISIBLE, animate);
    }

    private void switchChildVisibility(View child, int newVisibility, boolean animate){
        if(child == null){
            return;
        }else if(child.getVisibility() == newVisibility){
            return;
        }

        if(animate){
            animateViewWithEndVisibility(child, newVisibility);
        }else{
            child.setVisibility(newVisibility);
        }
    }

    private void hideContent(boolean animate){
        switchContentVisibility(View.GONE, animate);
    }

    private void showContent(boolean animate){
        switchContentVisibility(View.VISIBLE, animate);
    }

    private void switchContentVisibility(int newVisibility, boolean animate){
        View child;
        for(int i = 0; i < getChildCount(); i++){
            child = getChildAt(i);
            if(child.getId() != R.id.tv_error && child.getId() != R.id.loading_progressbar){
                switchChildVisibility(child, newVisibility, animate);
            }
        }
    }


    private void animateViewWithEndVisibility(@NonNull final View view, final int visbility){
        ViewPropertyAnimator animator = view.animate().alpha(visbility == VISIBLE ? 1f : 0f);
        animator.setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation){
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation){
                isAnimating = false;
                view.setVisibility(visbility);
                if(pendingState != INVALID_STATE){
                    setState(pendingState, true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation){

            }

            @Override
            public void onAnimationRepeat(Animator animation){

            }
        });

        animator.setInterpolator(INTERPOLATOR);
        animator.start();
    }

    @Override
    protected Parcelable onSaveInstanceState(){
        Parcelable parentState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(parentState);
        savedState.error = error;
        savedState.state = currentState;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state){
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setError(error);
        setState(savedState.state);
    }

    private static class SavedState extends BaseSavedState {

        private Error error;
        @State
        private int state;

        public SavedState(Parcel source){
            super(source);
            error = source.readParcelable(Error.class.getClassLoader());
            state = matchState(source.readInt());
        }

        public SavedState(Parcelable superState){
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags){
            super.writeToParcel(out, flags);
            out.writeParcelable(error, flags);
            out.writeInt(state);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in){
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size){
                        return new SavedState[size];
                    }
                };
    }
}
