package de.twoid.spotifystreamer.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.StatefulMediaPlayer.OnStateChangeListener;
import de.twoid.spotifystreamer.player.StatefulMediaPlayer.State;

import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_PAUSED;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_PREPARING;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STARTED;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STOPPED;

/**
 * Created by Johannes on 03.06.2015.
 * <p/>
 * Used http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 * as a guidance on how to bind this service to the {@link PlayerFragment}
 */
public class PlayerService extends Service implements OnPreparedListener, OnCompletionListener, OnErrorListener, OnStateChangeListener {

    private static final String TAG = "SpotifyStreamingService";
    public static final String ACTION_PLAY = "de.twoid.spotifystreamer.action.PLAY";
    public static final String EXTRA_SESSION = "de.twoid.spotifystreamer.extra.SESSION";
    private final IBinder streamingBinder = new SpotifyStreamingBinder();
    private StatefulMediaPlayer mediaPlayer = null;
    private PlayerNotificationManager notificationManager;
    private PlayerSession session;
    private WifiLock wifiLock;
    private final List<Callback> callbacks = new ArrayList<>();
    private Handler mHandler = new Handler();
    private boolean watchProgressUpdate = false;
    private boolean startPlayingWhenPrepared = false;

    private Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run(){
            if(mediaPlayer != null){
                onProgressChange(mediaPlayer.getCurrentPosition());
            }
            if(watchProgressUpdate){
                mHandler.postDelayed(progressUpdateRunnable, 16);
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        initMediaPlayer();
        notificationManager = new PlayerNotificationManager(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return streamingBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        String action = intent == null ? null : intent.getAction();
        if(ACTION_PLAY.equals(action)){
            ensureMediaPlayerInIdleState();

            setSession((PlayerSession) intent.getParcelableExtra(EXTRA_SESSION));
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        releaseMediaPlayer();
        releaseWifiLock();
        notificationManager.stopNotification();
        super.onDestroy();
    }

    @Override
    public void onPrepared(MediaPlayer player){
        if(startPlayingWhenPrepared){
            play();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp){
        if(!canPlayNextTrack()){
            releaseAndStop();
        }else{
            setNextTrack();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra){
        Log.e(TAG, "An error occurred: " + what + ", " + extra);
        mediaPlayer.reset();
        wifiLock.release();
        return false;
    }

    @Override
    public void onStateChanged(@State int newState){
        onPlayerStateChanged(newState);

        if(newState == STATE_STARTED || newState == STATE_PAUSED){
            notificationManager.startNotification();
        }
    }

    /**
     * Register a {@link Callback} to this service.
     * The callback will be notified on updates such as
     * {@link Callback#onProgressChange(int)}
     * {@link Callback#onTrackChanged(SpotifyTrack)}
     * {@link Callback#onPlaybackStopped()}
     * {@link Callback#onPlayerStateChanged(int)}
     *
     * @param callback the callback that should be notified on changes
     */
    public void registerCallback(Callback callback){
        synchronized(callbacks){
            callbacks.add(callback);
        }
    }

    /**
     * Unregister a callback from this service
     *
     * @param callback the callback to be unregistered
     */
    public void unregisterCallback(Callback callback){
        synchronized(callbacks){
            callbacks.remove(callback);
        }
    }

    /**
     * Set a {@link PlayerSession} and prepare it for playback.
     * This method prepares the current track in the sessions' playlist and calls {@link #play()} if immediate playback is requested;
     *
     * @param session the session to prepare
     */
    public void setSession(PlayerSession session){
        setSession(session, false);
    }

    /**
     * Set a {@link PlayerSession} and prepare it for playback.
     * This method prepares the current track in the sessions' playlist and calls {@link #play()} if immediate playback is requested;
     *
     * @param session          the session to prepare
     * @param playWhenPrepared whether to immediately start playing once the session has been prepared
     */
    public void setSession(PlayerSession session, boolean playWhenPrepared){
        this.session = session;
        startPlayingWhenPrepared = playWhenPrepared;

        if(session == null || session.getTrackCount() == 0){
            releaseAndStop();
            return;
        }

        ensureMediaPlayerInIdleState();
        ensureWifiLockHeld();
        preparePlaying(session.getCurrentTrack());
    }

    //    /**
    //     * Prepares the song at positionInPlaylist in the playlist and calls {@link #play()} if immediate playback is requested.
    //     *
    //     * @param positionInPlaylist the position of the song in the playlist that should be prepared
    //     */
    //    private void setSongAtPositionInPlaylist(int positionInPlaylist){
    //        if(playlist == null || playlist.isEmpty() || positionInPlaylist < 0 || positionInPlaylist > playlist.size() - 1){
    //            releaseAndStop();
    //        }
    //
    //        SpotifyTrack track = playlist.get(positionInPlaylist);
    //        if(callbacks != null){
    //            for(Callback callback : callbacks){
    //                callback.onTrackChanged(track);
    //            }
    //        }
    //
    //        ensureMediaPlayerInIdleState();
    //        ensureWifiLockHeld();
    //        preparePlaying(track);
    //    }

    private void setNextTrack(){
        SpotifyTrack track = session.getNextTrack();

        ensureMediaPlayerInIdleState();
        ensureWifiLockHeld();
        preparePlaying(track);
    }

    private void setPreviousTrack(){
        SpotifyTrack track = session.getPreviousTrack();

        ensureMediaPlayerInIdleState();
        ensureWifiLockHeld();
        preparePlaying(track);
    }

    /**
     * Get the current progress of the song that is being played
     *
     * @return the progress of the song in milliseconds or -1 if the mediaplayer is invalid
     */
    public int getPlayerPosition(){
        if(mediaPlayer == null){
            return -1;
        }else{
            return mediaPlayer.getCurrentPosition();
        }
    }

    public SpotifyTrack getCurrentTrack(){
        return session == null ? null : session.getCurrentTrack();
    }

    public PlayerSession getSession(){
        return session;
    }

    /**
     * Get the current state of the {@linkplain MediaPlayer media player}
     *
     * @return the current {@linkplain State state} of the {@linkplain MediaPlayer media player}
     */
    @State
    public int getCurrentState(){
        if(mediaPlayer == null){
            return StatefulMediaPlayer.STATE_NONE;
        }

        return mediaPlayer.getCurrentState();
    }

    /**
     * Start playing the previously prepared song.
     * If no song has been prepared, this method will throw an {@link IllegalStateException}!
     */
    public void play(){
        if(mediaPlayer.getCurrentState() == STATE_STARTED){
            return;
        }else if(mediaPlayer.getCurrentState() == STATE_PREPARING){
            startPlayingWhenPrepared = true;
            return;
        }
        startPlayingWhenPrepared = true;
        ensureWifiLockHeld();
        mediaPlayer.start();
        setWatchProgress(true);
    }

    /**
     * Stop playing  the current song.
     * If no song has been prepared, this method will throw an {@link IllegalStateException}!
     */
    public void stop(){
        if(mediaPlayer.getCurrentState() == STATE_STOPPED){
            return;
        }else if(mediaPlayer.getCurrentState() == STATE_PREPARING){
            startPlayingWhenPrepared = false;
            return;
        }

        releaseAndStop();
    }


    /**
     * Pause playback of the current song.
     * If no song has been prepared, this method will throw an {@link IllegalStateException}!
     */
    public void pause(){
        if(mediaPlayer.getCurrentState() == STATE_PAUSED){
            return;
        }else if(mediaPlayer.getCurrentState() == STATE_PREPARING){
            startPlayingWhenPrepared = false;
            return;
        }
        startPlayingWhenPrepared = false;
        mediaPlayer.pause();
        setWatchProgress(false);
        releaseWifiLock();
    }

    /**
     * Switch between the play and pause state
     */
    public void togglePlayPause(){
        if(mediaPlayer.isPlaying()){
            pause();
        }else{
            play();
        }
    }

    /**
     * Skip to the next track in the playlist or start from the beginning, if the current track is the last one
     */
    public void skipToNext(){
        setNextTrack();
    }

    /**
     * Skip to the previous song in the playlist or start the current track from the beginning if the current track is the first one
     */
    public void skipToPrevious(){
        if(canPlayPreviousTrack()){
            setPreviousTrack();
        }else{
            seekTo(0);
        }
    }

    /**
     * Seek the current song to the specified milliseconds
     *
     * @param milliseconds the millisecodns to seek to
     */
    public void seekTo(int milliseconds){
        mediaPlayer.seekTo(milliseconds);
    }

    /**
     * Set whether the progress of the current song should be watched and passed to specified {@linkplain de.twoid.spotifystreamer.player.PlayerService.Callback callbacks}
     *
     * @param watchProgress whether the progress should be watched
     */
    private void setWatchProgress(boolean watchProgress){
        watchProgressUpdate = watchProgress;
        if(watchProgress){
            progressUpdateRunnable.run();
        }
    }

    /**
     * Release dependencies and stop this service
     */
    private void releaseAndStop(){
        startPlayingWhenPrepared = false;
        mediaPlayer.stop();
        onPlaybackStopped();
        setWatchProgress(false);
        releaseWifiLock();
        stopForeground(true);
        stopSelf();
    }

    /**
     * Release the {@linkplain StatefulMediaPlayer mediaplayer}
     */
    private void releaseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.setOnStateChangeListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Release the {@linkplain WifiLock wifi lock}
     */
    private void releaseWifiLock(){
        if(wifiLock == null){
            return;
        }

        if(wifiLock.isHeld()){
            wifiLock.release();
        }
    }

    /**
     * Check whether there is a next track in the playlist
     *
     * @return true if there is a next track to play, false otherwise
     */
    public boolean canPlayNextTrack(){
        if(session == null){
            return false;
        }

        final int trackCount = session.getTrackCount();
        if(trackCount <= 1){
            return false;
        }

        final int indexOfNextTrack = session.getCurrentTrackPosition() + 1;

        return indexOfNextTrack >= 0 && indexOfNextTrack < trackCount;
    }

    /**
     * Check whether there is a previous track in the playlist
     *
     * @return true if there is a previous track to play, false otherwise
     */
    public boolean canPlayPreviousTrack(){
        if(session == null){
            return false;
        }

        final int trackCount = session.getTrackCount();
        if(trackCount <= 1){
            return false;
        }

        final int indexOfPreviousTrack = session.getCurrentTrackPosition() - 1;

        return indexOfPreviousTrack >= 0 && indexOfPreviousTrack < trackCount;
    }

    /**
     * Ensures that the {@linkplain StatefulMediaPlayer mediaplayer} is in its {@linkplain StatefulMediaPlayer#STATE_IDLE idle state}
     */
    private void ensureMediaPlayerInIdleState(){
        if(mediaPlayer == null){
            initMediaPlayer();
        }else if(mediaPlayer.getCurrentState() != StatefulMediaPlayer.STATE_IDLE){
            mediaPlayer.reset();
        }
    }

    /**
     * Ensures that a {@linkplain WifiLock wifi lock} is being held
     */
    private void ensureWifiLockHeld(){
        if(wifiLock == null){
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "SpotifyStreamingLock");
            wifiLock.acquire();
        }else if(!wifiLock.isHeld()){
            wifiLock.acquire();
        }
    }

    /**
     * Preparse a track for playback
     *
     * @param track the track to prepare
     */
    private void preparePlaying(@NonNull SpotifyTrack track){
        try{
            mediaPlayer.setDataSource(track.preview_url);

            onTrackChanged(track);

            mediaPlayer.prepareAsync();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Initializes the {@linkplain StatefulMediaPlayer mediaplayer} if null
     */
    private void initMediaPlayer(){
        if(mediaPlayer == null){
            mediaPlayer = new StatefulMediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnStateChangeListener(this);
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            ensureWifiLockHeld();
        }
    }

    public class SpotifyStreamingBinder extends Binder {

        public PlayerService getService(){
            return PlayerService.this;
        }
    }

    private void onProgressChange(int progress){
        if(callbacks != null && !callbacks.isEmpty()){
            synchronized(callbacks){
                Iterator<Callback> iterator = callbacks.iterator();

                Callback callback;
                while(iterator.hasNext()){
                    callback = iterator.next();
                    callback.onProgressChange(progress);
                }
            }
        }
    }

    private void onTrackChanged(SpotifyTrack track){
        if(callbacks != null && !callbacks.isEmpty()){
            synchronized(callbacks){
                Iterator<Callback> iterator = callbacks.iterator();

                Callback callback;
                while(iterator.hasNext()){
                    callback = iterator.next();
                    callback.onTrackChanged(track);
                }
            }
        }
    }

    private void onPlaybackStopped(){
        if(callbacks != null && !callbacks.isEmpty()){
            synchronized(callbacks){
                Iterator<Callback> iterator = callbacks.iterator();

                Callback callback;
                while(iterator.hasNext()){
                    callback = iterator.next();
                    callback.onPlaybackStopped();
                }
            }
        }
    }

    private void onPlayerStateChanged(@State int newState){
        if(callbacks != null && !callbacks.isEmpty()){
            synchronized(callbacks){
                Iterator<Callback> iterator = callbacks.iterator();

                Callback callback;
                while(iterator.hasNext()){
                    callback = iterator.next();
                    callback.onPlayerStateChanged(newState);
                }
            }
        }
    }

    /**
     * A set of callback methods to watch {@linkplain PlayerService this service}
     */
    public interface Callback {

        /**
         * Called when the progress of a track has changed
         *
         * @param progress the progress of the track in milliseconds
         */
        void onProgressChange(int progress);

        /**
         * Called when a new track is set
         *
         * @param track the new track to be played
         */
        void onTrackChanged(SpotifyTrack track);

        /**
         * Called when the playlist is completed and playback stopped
         */
        void onPlaybackStopped();

        /**
         * Called when the {@linkplain StatefulMediaPlayer mediaplayer} switched to a new {{@linkplain StatefulMediaPlayer.State state}}
         *
         * @param newState the new {{@linkplain StatefulMediaPlayer.State state
         */
        void onPlayerStateChanged(@State int newState);
    }
}
