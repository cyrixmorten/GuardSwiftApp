package com.guardswift.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Vibrator;
import android.util.Log;

import com.guardswift.R;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;

@Singleton
public class Sounds {

	private static final String TAG = Sounds.class.getSimpleName();

	private Context context;
    private MediaPlayer alarmMediaPlayer;
    private MediaPlayer notificationMediaPlayer;
    private AudioManager mAudioManager;
	int userVolume;

    private Vibrator v;

    private static Sounds instance;

    public static Sounds getInstance(Context context) {
        if (instance == null)
            instance = new Sounds(context);

        return instance;
    }

	@Inject
	public Sounds(@ForApplication Context c) {
		// class
		this.context = c;
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		// remeber what the user's volume was set to before we change it.
		userVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

         v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		Log.e(TAG, "Sounds created");

	}

    private boolean disabled = true;

	public void playNotification(int raw) {
		Log.i(TAG, "playNotification");

        if (disabled)
            return;

        // ignore requests while playing
        if (notificationMediaPlayer != null && notificationMediaPlayer.isPlaying())
            return;

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        v.vibrate(250);

		notificationMediaPlayer = MediaPlayer.create(context,
                raw);
		notificationMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
			}
		});
		notificationMediaPlayer
				.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						Log.i(TAG, "onComplete");
                        if (notificationMediaPlayer != null)
						    notificationMediaPlayer.release();

						notificationMediaPlayer = null;

                        if (v != null)
                            v.cancel();
					}
				});

	}

	public void playAlarmSound() {

		Log.i(TAG, "playAlarmSound");

//        if (BuildConfig.DEBUG)
//            return;

//        if (BuildConfig.DEBUG) {
//            Log.i(TAG, "No sound in debug mode!");
//            return;
//        }

		if (alarmMediaPlayer != null) {
			Log.i(TAG, "Already playing!");
			return;
		}

        alarmMediaPlayer = new MediaPlayer();

        Resources res = context.getResources();
        AssetFileDescriptor afd = res.openRawResourceFd(R.raw.alarm);


        alarmMediaPlayer.reset();
		alarmMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		alarmMediaPlayer.setLooping(true);
        try {
            alarmMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            alarmMediaPlayer.prepare();
            alarmMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            // set the volume to what we want it to be.  In this case it's max volume for the alarm stream.
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
        } catch (IOException e) {
            Log.e(TAG, "prepare alarm media player", e);
        }

        long[] pattern = { 0, 500, 2000 };
        v.vibrate(pattern, 0);

	}

	public void stopAlarm() {
		Log.i(TAG, "stopAlarm");

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);

		if (alarmMediaPlayer != null) {
			alarmMediaPlayer.release();
            alarmMediaPlayer = null;
		}

        if (v != null) {
            v.cancel();
        }

	}

    public boolean isPlayingAlarmSound() {
        return alarmMediaPlayer != null;
    }
}
