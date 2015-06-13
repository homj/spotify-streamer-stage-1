package de.twoid.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.widget.TextView;

import de.twoid.spotifystreamer.util.UiUtils;

/**
 * Created by Johannes on 02.06.2015.
 */
public class Message implements Parcelable {

    private static final int NOT_SET = -1;
    private static final int BY_RES_ID = 0;
    private static final int BY_OBJECT = 1;

    @IntDef({NOT_SET, BY_RES_ID, BY_OBJECT})
    private @interface TextState {

    }

    @IntDef({NOT_SET, BY_RES_ID})
    private @interface ImageState {

    }

    @StringRes
    private int errorTextResId;
    @DrawableRes
    private int errorImageResId;
    private String errorText;
    @TextState
    private int textState = NOT_SET;
    @ImageState
    private int imageState = NOT_SET;

    public Message(@StringRes int errorTextResId, @DrawableRes int errorImageResId){
        this.errorTextResId = errorTextResId;
        this.errorImageResId = errorImageResId;
        textState = BY_RES_ID;
        imageState = BY_RES_ID;
    }

    public Message(@StringRes int errorTextResId){
        this.errorTextResId = errorTextResId;
        textState = BY_RES_ID;
    }

    public Message(String errorText){
        this.errorText = errorText;
        textState = BY_OBJECT;
    }

    private Message(Parcel in){
        errorTextResId = in.readInt();
        errorImageResId = in.readInt();
        errorText = in.readString();
        textState = matchTextState(in.readInt());
        imageState = matchImageState(in.readInt());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in){
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size){
            return new Message[size];
        }
    };

    public void setText(@StringRes int errorTextResId){
        this.errorTextResId = errorTextResId;
        textState = BY_RES_ID;
    }

    public void setText(String errorText){
        this.errorText = errorText;
        textState = BY_OBJECT;
    }

    public void setImage(@DrawableRes int errorImageResId){
        this.errorImageResId = errorImageResId;
        imageState = BY_RES_ID;
    }

    public void applyText(TextView textView){
        if(textView != null){
            switch(textState){
                case BY_RES_ID:
                    textView.setText(errorTextResId);
                    break;
                case BY_OBJECT:
                    textView.setText(errorText);
                    break;
                default:
                    textView.setText(null);
                    break;
            }
        }
    }

    public void applyImage(TextView textView){
        if(textView != null){
            switch(imageState){
                case BY_RES_ID:
                    textView.setCompoundDrawablesWithIntrinsicBounds(null, UiUtils.getDrawable(textView.getResources(), errorImageResId), null, null);
                    break;
                default:
                    textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    break;
            }
        }
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(errorTextResId);
        dest.writeInt(errorImageResId);
        dest.writeString(errorText);
        dest.writeInt(textState);
        dest.writeInt(imageState);
    }

    @TextState
    private static int matchTextState(int state){
        switch(state){
            case NOT_SET:
                return NOT_SET;
            case BY_RES_ID:
                return BY_RES_ID;
            case BY_OBJECT:
                return BY_OBJECT;
            default:
                return NOT_SET;
        }
    }

    @ImageState
    private static int matchImageState(int state){
        switch(state){
            case NOT_SET:
                return NOT_SET;
            case BY_RES_ID:
                return BY_RES_ID;
            default:
                return NOT_SET;
        }
    }
}
