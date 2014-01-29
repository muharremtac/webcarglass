package com.muharremtac.android.glass.webcar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.google.glass.samples.compass.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class WebcarMainActivity extends Activity {

	private Sensor accelerometer;
	private SensorManager sensorManager;

	DownloadWebPageTask downloadWebPageTask;
	private TextView accelerometerText;
	ImageView img;
	int direction = 5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webcar_main);
		img = (ImageView) findViewById(R.id.imageView);
		accelerometerText = (TextView) findViewById(R.id.textViewX);
       
		downloadWebPageTask=new DownloadWebPageTask();
		
		callAsynchronousTask();
		
		if(sensorManager==null){
        	sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        }
        if(accelerometer==null){
        	accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        
        sensorManager.registerListener(sensorEventListener, accelerometer,SensorManager.SENSOR_DELAY_UI);
        
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		downloadWebPageTask.cancel(true);
		sensorManager.unregisterListener(sensorEventListener);
	}
	
	
	 private final SensorEventListener sensorEventListener = new SensorEventListener() {

			public void onSensorChanged(SensorEvent event) {
				
				synchronized (this) {
		            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
		
		            {
		               int xAcceleration=(int) (-event.values[0] * 10);
		               int yAcceleration=(int) (-event.values[1] * 10);
		               int zAcceleration=(int) (-event.values[2] * 10);
		            	
		               
		               
		               if(zAcceleration>40 && direction != 1){
		            	   direction = 1;
		            	   sendData("1");//forward
		               }else if(zAcceleration<-40 && direction != 2){
		            	   direction = 2;
		            	   sendData("2");//backward
		               }else if(xAcceleration>40 && direction != 3){
		            	   direction = 3;
		            	   sendData("3");//right
		               }else if(xAcceleration<-40 && direction != 4){
		            	   direction = 4;
		            	   sendData("4");//left
		               }else if(zAcceleration>=-40 && zAcceleration<=40 && xAcceleration>=-40 && xAcceleration<=40 && direction != 5){
		            	   direction = 5;
		            	   sendData("5");//stop
		               }
		            }
		        }
			}
			
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				
			}
		};
		
		private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
//			String command = "do nothing!";

			@Override
			protected String doInBackground(String... urls) {

				String command = urls[0];
				
				getHTML("http://192.168.1.17/arduino/digital/13/"+command);

				return command;
			}

			@Override
			protected void onPostExecute(String result) {
				accelerometerText.setText(result);
			}
		}
		
		private void sendData(final String method) {
			downloadWebPageTask.cancel(true);
			downloadWebPageTask = new DownloadWebPageTask();
			downloadWebPageTask.execute(method);
		}
		
	public String getHTML(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public void callAsynchronousTask() {
	    final Handler handler = new Handler();
	    Timer timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                    	 new WebStreamTask().execute();
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0,500); //execute in every 50000 ms
	}
	
	private class WebStreamTask extends AsyncTask<String, Void, String> {
		Bitmap bitmap = null;

		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			URL url = null;

			try {
				url = new URL("http://192.168.1.17:8090/?action=snapshot");
				bitmap = BitmapFactory.decodeStream(url.openConnection()
						.getInputStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			img.setImageBitmap(bitmap);
		}
	}
	

}
