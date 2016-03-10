package com.example.emindeniz.sunshine;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Created by emindeniz on 02/03/16.
 */


public class OpenWeatherURLConnection {

    private final String TAG =OpenWeatherURLConnection.class.getSimpleName();
    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    private HttpURLConnection urlConnection;
    private BufferedReader reader;

    // Will contain the raw JSON response as a string.
    private String forecastJsonStr;

    public String getWeatherForecastForQuery(String queryParam, String format, String unit, int numDays){
        try {

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERRY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "appid";

            final String appID = "44db6a862fba0b067b1930da0d769e98";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERRY_PARAM,queryParam)
                    .appendQueryParameter(FORMAT_PARAM,format)
                    .appendQueryParameter(UNITS_PARAM,unit)
                    .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM,appID).build();



            Log.v(TAG,"Built uri : " + builtUri.toString());

            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
      //      URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Istanbul&mode=json&units=metric&cnt=7&appid=44db6a862fba0b067b1930da0d769e98");

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.i(TAG,"URL Connection created");

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder stringBuffer = new StringBuilder();

            if(inputStream == null){
                Log.e(TAG,"InputStream is null, no data received");
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine() )!= null){
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                stringBuffer.append(line + "\n");
            }

            if (stringBuffer.length() == 0){
                Log.e(TAG,"Weather data is empty");
                // Stream was empty.  No point in parsing.
                return null;
            }

            forecastJsonStr = stringBuffer.toString();

            Log.v(TAG,"Forecast json string : " + forecastJsonStr);

        } catch (IOException e) {
            Log.e(TAG,e.toString());
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        }

        finally {
            if(urlConnection != null){
                urlConnection.disconnect();
                Log.i(TAG,"URL connection closed");
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return forecastJsonStr;
    }


}
