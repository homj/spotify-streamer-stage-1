package de.twoid.spotifystreamer.player;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.MediaStyle;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.StatefulMediaPlayer.State;

import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_END;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_ERROR;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_NONE;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STARTED;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STOPPED;

/**
 * Used https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/MediaNotificationManager.java
 * as a base to create this PlayerNotificationManager
 */
public class PlayerNotificationManager extends BroadcastReceiver {
    private static final String TAG = "PlayerNotificationMngr";
    private static final int NOTIFICATION_ID = 368;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "de.twoid.spotifystreamer.action.PAUSE";
    public static final String ACTION_PLAY = "de.twoid.spotifystreamer.action.PLAY";
    public static final String ACTION_PREV = "de.twoid.spotifystreamer.action.SKIP_PREVIOUS";
    public static final String ACTION_NEXT = "de.twoid.spotifystreamer.action.SKIP_NEXT";
    public static final String ACTION_STOP = "de.twoid.spotifystreamer.action.STOP";

    private final PlayerService mService;

    private SpotifyTrack currentTrack;
    @State
    private int playerState = STATE_NONE;

    private NotificationManager mNotificationManager;

    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mStopIntent;

    private int mNotificationColor;

    private boolean mStarted = false;

    public PlayerNotificationManager(PlayerService service){
        mService = service;
        currentTrack = mService.getCurrentTrack();
        playerState = mService.getCurrentState();

        mNotificationColor = mService.getResources().getColor(R.color.branding);

        mNotificationManager = (NotificationManager) mService
                .getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification(){
        currentTrack = mService.getCurrentTrack();
        playerState = mService.getCurrentState();

        Log.d(TAG, "startNotification");
        if(!mStarted){
            Log.d(TAG, "startNotification -> not started -> try createNotification");
            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if(notification != null){
                Log.d(TAG, "startNotification -> not started -> notification created -> display");
                mService.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }else{

                Log.d(TAG, "startNotification -> not started -> notification not created!");
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification(){
        if(mStarted){
            mStarted = false;
            mService.unregisterCallback(mCb);
            try{
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            }catch(IllegalArgumentException ex){
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        final String action = intent.getAction();
        switch(action){
            case ACTION_PAUSE:
                mService.pause();
                break;
            case ACTION_PLAY:
                mService.play();
                break;
            case ACTION_NEXT:
                mService.skipToNext();
                break;
            case ACTION_PREV:
                mService.skipToPrevious();
                break;
            case ACTION_STOP:
                mService.stop();
                break;
            default:
        }
    }

    private PendingIntent createContentIntent(){
        Intent openUI = new Intent(mService, PlayerActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra(PlayerActivity.EXTRA_SESSION, mService.getSession());

        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private final PlayerService.Callback mCb = new PlayerService.Callback() {
        @Override
        public void onProgressChange(int progress){

        }

        @Override
        public void onTrackChanged(SpotifyTrack track){
            currentTrack = track;
            Notification notification = createNotification();
            if(notification != null){
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onPlaybackStopped(){

        }

        @Override
        public void onPlayerStateChanged(@State int newState){
            playerState = newState;
            if(!isValidState(newState)){
                stopNotification();
            }else{
                Notification notification = createNotification();
                if(notification != null){
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }
    };

    private static boolean isValidState(@State int state){
        switch(state){
            case STATE_STOPPED:
            case STATE_NONE:
            case STATE_END:
            case STATE_ERROR:
                return false;
            default:
                return true;
        }
    }

    private Notification createNotification(){
        if(currentTrack == null || !isValidState(playerState)){
            return null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        int playPauseButtonPosition = 0;

        // If skip to previous action is enabled
        if(mService.canPlayPreviousTrack()){
            notificationBuilder.addAction(R.drawable.ic_skip_previous_24dp,
                    mService.getString(R.string.label_previous), mPreviousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;
        }

        addPlayPauseAction(notificationBuilder);

        // If skip to next action is enabled
        if(mService.canPlayNextTrack()){
            notificationBuilder.addAction(R.drawable.ic_skip_next_24dp,
                    mService.getString(R.string.label_next), mNextIntent);
        }


        String imageUrl = currentTrack.hasImage() ? currentTrack.getMediumImage().url : null;
        Bitmap image = BitmapFactory.decodeResource(mService.getResources(), R.drawable.ic_albumart_placeholder);

        notificationBuilder
                .setStyle(new MediaStyle().setShowActionsInCompactView(playPauseButtonPosition))
                .setDeleteIntent(mStopIntent)
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_play_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(currentTrack.name)
                .setContentText(currentTrack.artists == null ? null : currentTrack.artists.get(0).name)
                .setLargeIcon(image);

        setNotificationPlaybackState(notificationBuilder);
        if(imageUrl != null){
            loadImage(imageUrl, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder){
        String label;
        int icon;
        PendingIntent intent;
        if(playerState == STATE_STARTED){
            label = mService.getString(R.string.label_pause);
            icon = R.drawable.ic_pause_24dp;
            intent = mPauseIntent;
        }else{
            label = mService.getString(R.string.label_play);
            icon = R.drawable.ic_play_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder){
        if(!isValidState(playerState) || !mStarted){
            mService.stopForeground(true);
            return;
        }
        if(playerState == STATE_STARTED && mService.getPlayerPosition() >= 0){
            builder
                    .setWhen(System.currentTimeMillis() - mService.getPlayerPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        }else{
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(playerState == STATE_STARTED);
        if(playerState == StatefulMediaPlayer.STATE_PAUSED){
            mService.stopForeground(false);
        }
    }

    private void loadImage(final String bitmapUrl, final NotificationCompat.Builder builder){

        Picasso.with(mService).load(bitmapUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from){
                builder.setLargeIcon(bitmap);
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable){

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable){

            }
        });
    }
}