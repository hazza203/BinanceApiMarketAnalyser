import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by harry on 2/01/2018.
 */
public class AnalyseMarkets {



    HashMap<Integer, Coin> coins;
    int numCoins = 0, iter = 1;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AnalyseMarkets(){
        coins = new HashMap();
    }


    /**
     Get all BTC pairs from binance API and store create a Coin class instances for
     each and store in an HashMap of type Coin.

     Once initialised the HashMap get the historical data for each Coin from the CryptoCompare
     API to begin analyses and ensure we have enough data to begin analysis.

     Begin getting live price data at specified intervals.
     **/

    public void init(){


        JSONArray resultArray = null;

        while(resultArray == null) {

            resultArray = getMarketJson();

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject marketObj = resultArray.getJSONObject(i);
                if (marketObj.getString("symbol").contains("BTC")) {
                    coins.put(numCoins, new Coin(marketObj.getString("symbol")));
                    numCoins++;
                }
            }
        }

        for(int i = 0; i < numCoins; i++){
            String ccName = coins.get(i).getName().replaceAll("BTC","");
            JSONArray ccArray = getCCJSON(ccName);
            for(int j = 0; j < ccArray.length(); j++){
                JSONObject price = ccArray.getJSONObject(j);
                double lastPrice = price.getDouble("close");
                coins.get(i).addPrice(lastPrice);
            }
        }

        analyseMarkets();
    }


    //Gets new price data every specified period and feeds it into the Coin class for analysing.
    private void analyseMarkets(){

        final Runnable gatherData = new Runnable() {

            public void run() {

                JSONArray resultArray = getMarketJson();
                System.out.println("Iteration #" + iter);
                iter++;
                for(int i = 0; i < numCoins; i++){
                    coins.get(i).addPrice(getPrice(coins.get(i).getName()));
                }
            }
        };

        final ScheduledFuture<?> dataHandler = scheduler.scheduleAtFixedRate(gatherData, 30, 30, TimeUnit.MINUTES);
    }

    /*

        FROM HERE ON IT IS JUST METHODS FOR GETTING JSON TYPE API DATA

        FIRST METHOD FOR BINANCE API TICKER, ONE FOR EACH COIN (Use this to avoid searching over in the second method)
        SECOND METHOD FOR BINANCE API TICKER FOR ALL COINS (Use this to initialize the coin data structure)
        THIRD METHOD FOR CRYPTOCOMPARE API TO GET HISTORICAL DATA TO AVOID ANALYSIS DELAY


     */

    private Double getPrice(String name){
        JSONObject obj = null;

        try{
            URL url = new URL("https://api.binance.com/api/v3/ticker/price?symbol="+name);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                response.append(line);
            }

            bufferedReader.close();

            obj = new JSONObject(response.toString());
            return obj.getDouble("price");

        } catch (IOException e){
            System.out.println("IO EXCEPTION IN GETPRICE METHOD: JSON CODE: " + obj.getString("code") );
        }

        return null;
    }



    private JSONArray getMarketJson(){
        try{
            URL url = new URL("https://api.binance.com/api/v3/ticker/price");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                response.append(line);
            }

            bufferedReader.close();

            JSONArray obj = new JSONArray(response.toString());
            return obj;
        } catch (IOException e){

            System.out.println("ERROR getting JSON: " + e.getMessage());
            return getMarketJson();
        }
    }

    private JSONArray getCCJSON(String name){
        try{
            URL url = new URL("https://min-api.cryptocompare.com/data/histominute?fsym="+name+"&tsym=BTC&limit=28&aggregate=30&e=CCCAGG");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                response.append(line);
            }

            bufferedReader.close();

            JSONObject obj = new JSONObject(response.toString());

            JSONArray resultArray = obj.getJSONArray("Data");
            return resultArray;
        } catch (IOException e){
            System.out.println("ERROR getting JSON: " + e.getMessage());
            return null;
        }
    }


}
