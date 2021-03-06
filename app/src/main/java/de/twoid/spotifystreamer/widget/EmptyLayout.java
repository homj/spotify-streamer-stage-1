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

import de.twoid.spotifystreamer.Message;
import de.twoid.spotifystreamer.R;

/**
 * Created by Johannes on 01.06.2015.
 */
public class EmptyLayout extends FrameLayout {

    public static final int INVALID_STATE = -1;
    public static final int STATE_DISPLAY_CONTENT = 0;
    public static final int STATE_DISPLAY_LOADING = 1;
    public static final int STATE_DISPLAY_MESSAGE = 2;


    @IntDef({STATE_DISPLAY_CONTENT, STATE_DISPLAY_MESSAGE, STATE_DISPLAY_LOADING, INVALID_STATE})
    public @interface State {

    }

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private ProgressBar loadingProgressBar;
    private TextView tvMessage;

    @State
    private int currentState;
    @State
    private int pendingState = INVALID_STATE;
    private Message message;
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
                        message = new Message(messageResId, drawableResId);
                    }else{
                        message = new Message(messageResId);
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
            case STATE_DISPLAY_MESSAGE:
                return STATE_DISPLAY_MESSAGE;
            default:
                return STATE_DISPLAY_CONTENT;
        }
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        tvMessage = (TextView) findViewById(R.id.tv_error);
        tvMessage.setCompoundDrawablePadding(tvMessage.getResources().getDimensionPixelOffset(R.dimen.default_spacing));
        setMessage(message);
    }

    public void setMessage(@StringRes int errorTextResId, @DrawableRes int errorImageResId){
        setMessage(new Message(errorTextResId, errorImageResId));
    }

    public void setMessage(Message message){
        this.message = message;

        if(message == null){
            tvMessage.setText(null);
            tvMessage.setCompoundDrawables(null, null, null, null);
        }else{
            message.applyText(tvMessage);
            message.applyImage(tvMessage);
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
            case STATE_DISPLAY_MESSAGE:
                showView(tvMessage, animate);
                hideView(loadingProgressBar, animate);
                hideContent(animate);
                break;
            case STATE_DISPLAY_LOADING:
                hideView(tvMessage, animate);
                showView(loadingProgressBar, animate);
                hideContent(animate);
                break;
            case STATE_DISPLAY_CONTENT:
                hideView(tvMessage, animate);
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
        switchContentVisibility(View.INVISIBLE, animate);
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
        savedState.message = message;
        savedState.state = currentState;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state){
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setMessage(message);
        setState(savedState.state);
    }

    private static class SavedState extends BaseSavedState {

        private Message message;
        @State
        private int state;

        public SavedState(Parcel source){
            super(source);
            message = source.readParcelable(Message.class.getClassLoader());
            state = matchState(source.readInt());
        }

        public SavedState(Parcelable superState){
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags){
            super.writeToParcel(out, flags);
            out.writeParcelable(message, flags);
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
