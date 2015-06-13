package de.twoid.spotifystreamer.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.support.annotation.IntDef;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Johannes on 04.06.2015.
 */
public class StatefulMediaPlayer extends MediaPlayer implements OnPreparedListener, OnCompletionListener, OnErrorListener {

    public static final int STATE_NONE = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_PREPARING = 2;
    public static final int STATE_PREPARED = 3;
    public static final int STATE_STARTED = 4;
    public static final int STATE_PAUSED = 5;
    public static final int STATE_STOPPED = 6;
    public static final int STATE_PLAYBACK_COMPLETED = 7;
    public static final int STATE_ERROR = 8;
    public static final int STATE_END = 9;

    @IntDef({STATE_NONE, STATE_IDLE, STATE_INITIALIZED, STATE_PREPARING, STATE_PREPARED, STATE_STARTED, STATE_PAUSED, STATE_STOPPED, STATE_PLAYBACK_COMPLETED, STATE_ERROR, STATE_END})
    public @interface State {

    }

    @State
    private int currentState = STATE_NONE;
    private OnPreparedListener wrappedOnPrepareListener;
    private OnCompletionListener wrappedOnCompletionListener;
    private OnErrorListener wrappedOnErrorListener;
    private OnStateChangeListener onStateChangeListener;

    public StatefulMediaPlayer(){
        switchToState(STATE_IDLE);
    }

    @State
    public int getCurrentState(){
        return currentState;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener){
        wrappedOnPrepareListener = listener;
        super.setOnPreparedListener(this);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener){
        wrappedOnCompletionListener = listener;
        super.setOnCompletionListener(this);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener){
        wrappedOnErrorListener = listener;
        super.setOnErrorListener(this);
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener){
        this.onStateChangeListener = onStateChangeListener;
    }

    @Override
    public void onPrepared(MediaPlayer mp){
        switchToState(STATE_PREPARED);
        if(wrappedOnPrepareListener != null){
            wrappedOnPrepareListener.onPrepared(mp);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp){
        switchToState(isLooping() ? STATE_STARTED : STATE_PLAYBACK_COMPLETED);

        if(wrappedOnCompletionListener != null){
            wrappedOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra){
        switchToState(STATE_ERROR);
        return wrappedOnErrorListener != null && wrappedOnErrorListener.onError(mp, what, extra);
    }

    @Override
    public void start(){
        if(canSwitchToState(STATE_STARTED)){
            super.start();
            setState(STATE_STARTED);
        }else{
            throwStateSwitchError(STATE_STARTED);
        }
    }

    @Override
    public void pause(){
        if(canSwitchToState(STATE_PAUSED)){
            super.pause();
            setState(STATE_PAUSED);
        }else{
            throwStateSwitchError(STATE_PAUSED);
        }
    }

    @Override
    public void stop(){
        if(canSwitchToState(STATE_STOPPED)){
            super.stop();
            setState(STATE_STOPPED);
        }else{
            throwStateSwitchError(STATE_STOPPED);
        }
    }

    @Override
    public void seekTo(final int milliseconds){
        if(canSwitchToState(currentState)){
            super.seekTo(milliseconds);
        }else{
            throwStateSwitchError(currentState);
        }
    }

    @Override
    public void prepare() throws IOException, IllegalStateException{
        if(canSwitchToState(STATE_PREPARED)){
            super.prepare();
            setState(STATE_PREPARED);
        }else{
            throwStateSwitchError(STATE_PREPARED);
        }
    }

    @Override
    public void prepareAsync(){
        if(canSwitchToState(STATE_PREPARING)){
            super.prepareAsync();
            setState(STATE_PREPARING);
        }else{
            throwStateSwitchError(STATE_PREPARING);
        }
    }

    @Override
    public void release(){
        if(canSwitchToState(STATE_END)){
            super.release();
            setState(STATE_END);
            setOnPreparedListener(null);
            setOnErrorListener(null);
            setOnCompletionListener(null);
            setOnStateChangeListener(null);
        }else{
            throwStateSwitchError(STATE_END);
        }
    }

    @Override
    public void reset(){
        if(canSwitchToState(STATE_IDLE)){
            super.reset();
            setState(STATE_IDLE);
        }else{
            throwStateSwitchError(STATE_IDLE);
        }
    }

    @Override
    public void setDataSource(final String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException{
        if(canSwitchToState(STATE_INITIALIZED)){
            super.setDataSource(path);
            setState(STATE_INITIALIZED);
        }else{
            throwStateSwitchError(STATE_INITIALIZED);
        }
    }

    @Override
    public void setDataSource(final Context context, final Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException{
        if(canSwitchToState(STATE_INITIALIZED)){
            super.setDataSource(context, uri);
            setState(STATE_INITIALIZED);
        }else{
            throwStateSwitchError(STATE_INITIALIZED);
        }
    }

    @Override
    public void setDataSource(final Context context, final Uri uri, final Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException{
        if(canSwitchToState(STATE_INITIALIZED)){
            super.setDataSource(context, uri, headers);
            setState(STATE_INITIALIZED);
        }else{
            throwStateSwitchError(STATE_INITIALIZED);
        }
    }

    @Override
    public void setDataSource(final FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException{
        if(canSwitchToState(STATE_INITIALIZED)){
            super.setDataSource(fd);
            setState(STATE_INITIALIZED);
        }else{
            throwStateSwitchError(STATE_INITIALIZED);
        }
    }

    @Override
    public void setDataSource(final FileDescriptor fd, final long offset, final long length) throws IOException, IllegalArgumentException, IllegalStateException{
        if(canSwitchToState(STATE_INITIALIZED)){
            super.setDataSource(fd, offset, length);
            setState(STATE_INITIALIZED);
        }else{
            throwStateSwitchError(STATE_INITIALIZED);
        }
    }

    private boolean switchToState(@State int newState){
        if(canSwitchToState(currentState, newState)){
            setState(newState);
            return true;
        }

        return false;
    }

    private void setState(@State int newState){
        currentState = newState;
        if(onStateChangeListener != null){
            onStateChangeListener.onStateChanged(newState);
        }
    }

    private void throwStateSwitchError(@State int newState){
        throw new IllegalStateException("Can't switch to state " + newState + " from " + currentState);
    }

    private boolean canSwitchToState(@State int newState){
        return canSwitchToState(currentState, newState);
    }

    private static boolean canSwitchToState(@State int oldState, @State int newState){
        switch(newState){
            case STATE_IDLE:
                return true;
            case STATE_INITIALIZED:
                return isOneOf(oldState, STATE_IDLE);
            case STATE_PREPARING:
                return isOneOf(oldState, STATE_INITIALIZED, STATE_STOPPED);
            case STATE_PREPARED:
                return isOneOf(oldState, STATE_INITIALIZED, STATE_PREPARED, STATE_PREPARING, STATE_STOPPED);
            case STATE_STARTED:
                return isOneOf(oldState, STATE_PREPARED, STATE_STARTED, STATE_PAUSED, STATE_PLAYBACK_COMPLETED);
            case STATE_PAUSED:
                return isOneOf(oldState, STATE_STARTED, STATE_PAUSED);
            case STATE_STOPPED:
                return isOneOf(oldState, STATE_PREPARED, STATE_STARTED, STATE_PAUSED, STATE_PLAYBACK_COMPLETED, STATE_STOPPED);
            case STATE_PLAYBACK_COMPLETED:
                return isOneOf(oldState, STATE_STARTED, STATE_PLAYBACK_COMPLETED);
            case STATE_ERROR:
                return true;
            case STATE_END:
                return true;
            default:
                return false;
        }
    }

    private static boolean isOneOf(@State int state, @State int... states){
        if(states == null || states.length == 0){
            return false;
        }

        final int count = states.length;

        if(count == 1){
            return state == states[0];
        }

        for(int state1 : states){
            if(state == state1){
                return true;
            }
        }

        return false;
    }

    public interface OnStateChangeListener {

        void onStateChanged(@State int newState);
    }
}
