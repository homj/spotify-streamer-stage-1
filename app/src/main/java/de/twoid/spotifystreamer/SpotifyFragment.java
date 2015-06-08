package de.twoid.spotifystreamer;

import android.os.Handler;
import android.os.Message;

import de.twoid.spotifystreamer.util.NetworkUtils;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.client.Response;

/**
 * Created by Johannes on 01.06.2015.
 *
 * Offers easy access to the {@link SpotifyApi}
 */
public abstract class SpotifyFragment extends BaseFragment implements Handler.Callback {

    protected static final int MESSAGE_TYPE_ERROR = -1;
    private SpotifyApi api;
    protected SpotifyService spotify;
    protected Handler mHandler;

    private EmptyLayout emptyLayout;

    @Override
    protected void initResources(){
        api = new SpotifyApi();
        spotify = api.getService();
        mHandler = new Handler(this);
    }

    protected void setEmptyLayout(EmptyLayout emptyLayout){
        this.emptyLayout = emptyLayout;
    }

    @Override
    public boolean handleMessage(Message msg){
        if(msg != null){
            switch(msg.what){
                case MESSAGE_TYPE_ERROR:
                    displayError((Error) msg.obj);
                    break;
                default:
                    onMessageReceived(msg.what, msg);
            }
        }
        return false;
    }

    protected abstract void onMessageReceived(int type, Message message);

    protected void displayLoading(){
        if(emptyLayout != null){
            emptyLayout.setState(EmptyLayout.STATE_DISPLAY_LOADING);
        }
    }

    protected void displayError(Error error){
        if(emptyLayout != null){
            emptyLayout.setError(error);
            emptyLayout.setState(EmptyLayout.STATE_DISPLAY_ERROR);
        }
    }

    protected void displayContent(){
        if(emptyLayout != null){
            emptyLayout.setState(EmptyLayout.STATE_DISPLAY_CONTENT);
        }
    }

    protected boolean isConnectedToInternet(){
        return NetworkUtils.isConnected(getActivity());
    }

    protected abstract class SpotifyCallback<T> extends kaaes.spotify.webapi.android.SpotifyCallback<T> {

        @Override
        public void success(T t, Response response){
            Message message = Message.obtain();
            message.what = getSuccessMessageType();
            message.obj = t;
            mHandler.sendMessage(message);
        }

        @Override
        public void failure(SpotifyError error){
            Message message = Message.obtain();
            message.what = MESSAGE_TYPE_ERROR;
            message.obj = resolveError(error);
            mHandler.sendMessage(message);
        }

        public abstract int getSuccessMessageType();

        public abstract Error resolveError(SpotifyError error);
    }
}
