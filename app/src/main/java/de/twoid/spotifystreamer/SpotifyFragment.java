package de.twoid.spotifystreamer;

import android.os.Handler;

import de.twoid.spotifystreamer.util.NetworkUtils;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.client.Response;

/**
 * Created by Johannes on 01.06.2015.
 * <p/>
 * Offers easy access to the {@link SpotifyApi}
 */
public abstract class SpotifyFragment extends BaseFragment implements Handler.Callback {

    protected static final int MESSAGE_TYPE_ERROR = -1;
    protected SpotifyService spotify;
    protected Handler mHandler;

    private EmptyLayout emptyLayout;

    @Override
    protected void initResources(){
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
        mHandler = new Handler(this);
    }

    protected void setEmptyLayout(EmptyLayout emptyLayout){
        this.emptyLayout = emptyLayout;
    }

    @Override
    public boolean handleMessage(android.os.Message msg){
        if(msg != null){
            switch(msg.what){
                case MESSAGE_TYPE_ERROR:
                    displayMessage((Message) msg.obj);
                    break;
                default:
                    onMessageReceived(msg.what, msg);
            }
        }
        return false;
    }

    protected abstract void onMessageReceived(int type, android.os.Message message);

    protected void displayLoading(){
        if(emptyLayout != null){
            emptyLayout.setState(EmptyLayout.STATE_DISPLAY_LOADING);
        }
    }

    protected void displayMessage(Message message){
        if(emptyLayout != null){
            emptyLayout.setMessage(message);
            emptyLayout.setState(EmptyLayout.STATE_DISPLAY_MESSAGE);
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
            android.os.Message message = android.os.Message.obtain();
            message.what = getSuccessMessageType();
            message.obj = t;
            mHandler.sendMessage(message);
        }

        @Override
        public void failure(SpotifyError error){
            android.os.Message message = android.os.Message.obtain();
            message.what = MESSAGE_TYPE_ERROR;
            message.obj = resolveError(error);
            mHandler.sendMessage(message);
        }

        public abstract int getSuccessMessageType();

        public abstract Message resolveError(SpotifyError error);
    }
}
