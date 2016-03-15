package com.example.emindeniz.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    private final String TAG = ForecastFragment.class.getSimpleName();


    private ArrayAdapter mForecastAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the refresh option menu
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeatherForecast();
            return true;
        }


        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onStart() {
        super.onStart();
        updateWeatherForecast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);


        final String[] foreCastArray = {};
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(foreCastArray));
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                weekForecast);


        forecastListView.setAdapter(mForecastAdapter);
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String forecast = (String) mForecastAdapter.getItem(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });


        return rootView;
    }

    /**
     * Updates weather forecast
     */
    private void updateWeatherForecast(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String location = pref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        fetchWeatherTask.execute(location);
    }
    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     *
     * @param forecastJsonStr Json string which get from Open Weather API
     * @param numDays         Number of day we interested
     * @return day name + forecast description(Sunny,cloudy,etc..) + high and low temp .
     * @throws JSONException
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";


        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();


        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {

            String day; // Ex: Monday
            String description; // Ex: Sunny
            String highAndLow; // EX: 11/8

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);


            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".

            long dateTime;

            // Cheating to convert this to UTC time, which is what we want anyhow

            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            //TODO numDays should not be smaller than array length. This may need a check
            resultStrs[i] = day + " - " + description + " - " + highAndLow;


        }

        return resultStrs;
    }

    /**
     * Prepare the weather high/lows for presentation.
     *
     * @param high Highest temperature in a day
     * @param low  Lowest temperature in a day
     * @return high/low as rounded string
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.(Make it as int)
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     *
     * @param time day
     * @return String day
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.

        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }


    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params) {
            String[] weatherData = null;
            OpenWeatherURLConnection weatherURLConnection = new OpenWeatherURLConnection();
            if (params[0] == null || params[0].length() == 0)
                params[0] = "94043";
            String forecastJsonStr = weatherURLConnection.getWeatherForecastForQuery(params[0], "json", "metric", 7);

            try {
                if (forecastJsonStr == null) {
                    Log.e(TAG, "Weather data is null.");
                    return null;
                }

                weatherData = getWeatherDataFromJson(forecastJsonStr, 7);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage() + e);
            }


            return weatherData;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(result);

            }else{
                Toast.makeText(getActivity(), "We couldn't get any data from server. \n" +
                        "Did you enter the right postal code ?", Toast.LENGTH_LONG).show();
            }


        }
    }
}
