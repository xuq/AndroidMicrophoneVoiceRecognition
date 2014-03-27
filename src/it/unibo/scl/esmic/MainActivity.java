package it.unibo.scl.esmic;

import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_DEFAULT;
	private final static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final static int SAMPLE_RATE = 44100;
	private final static double RMS_THRESHOLD = 60;
	private final static double MIC_CALIBRATION = 100;
	private final static int REQUEST_CODE = 1234;

	private boolean isRecording;
	final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
			CHANNEL_CONFIGURATION, ENCODING);
	final AudioRecord recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE,
			CHANNEL_CONFIGURATION, ENCODING, bufferSize);

	TextView rmsValue;
	TextView rmsResult;
	TextView activityRecTextView;
	TextView activityRecHintsTextView;
	View rlRms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rmsValue = (TextView) findViewById(R.id.tv_rms_value);
		rmsResult = (TextView) findViewById(R.id.tv_audio_value);
		activityRecTextView = (TextView) findViewById(R.id.tv_voice_recognition_main_value);
		activityRecHintsTextView = (TextView) findViewById(R.id.tv_voice_recognition_other_values);
		rlRms = findViewById(R.id.rl_mic_values);
		((TextView) findViewById(R.id.tv_threshold_rms_value))
				.setText(RMS_THRESHOLD + "");

	}

	public void start(View v) {
		isRecording = true;
		process();
	}

	public void process() {
		recorder.startRecording();
		final short[] audioData = new short[bufferSize];

		(new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();

				while (isRecording) {
					recorder.read(audioData, 0, bufferSize);
					int sum = 0;
					for (short a : audioData) {
						sum += (a * a);
					}

					System.out.println("Sum: " + sum);
					double RMS = Math.sqrt(sum / audioData.length);
					double RMSdB = 20 * Math.log10(RMS / 0.00002);
					setRMSValue(RMSdB - 80);
				}
			}
		}).start();

	}

	public void stop(View v) {
		// TODO stop lettura microfono
		isRecording = false;
		recorder.stop();
		recorder.release();
	}

	public void startVoiceRecognition(View v) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Test di Activity Recognition");
		startActivityForResult(intent, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			setActivityRecognitionResult(matches);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void setActivityRecognitionResult(List<String> list) {
		activityRecTextView.setText(list.get(0));
		list.remove(0);
		String hints = "";
		for (String result : list) {
			hints += result + ", ";
		}
		activityRecHintsTextView
				.setText(hints.substring(0, hints.length() - 2));
	}

	public void setRMSValue(final double value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rmsValue.setText(value + "");
				if (value >= RMS_THRESHOLD) {
					rmsResult.setText("Rumore");
					rlRms.setBackgroundResource(R.drawable.onnoise);
				} else {
					rmsResult.setText("Silenzio");
					rlRms.setBackgroundResource(R.drawable.border);
				}

			}
		});
	}

}
