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

    String dataURL = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=";

    HashMap<Integer, Coin> coins;
    int numCoins = 0;
    double profit, dayProfit;
    double gained;
    int coinsBought = 0, coinsHodl = 0, coinsSold = 0, soldLoss = 0, soldProf = 0, day = 1, totalSold = 0, dayCounter = 0;
    double averageLoss = 0, averageProf = 0, highProf = 0, highLoss = 0;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AnalyseMarkets(){
        coins = new HashMap();
    }

    public void init(){


        JSONArray resultArray = null;

        while(resultArray == null) {

            resultArray = getMarketJson();

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject marketObj = resultArray.getJSONObject(i);
                if (marketObj.getString("MarketName").contains("BTC-")) {
                    coins.put(i, new Coin(marketObj.getString("MarketName")));
                    numCoins++;
                }
            }
        }

        for(int i = 0; i < numCoins; i++){
            String ccName = coins.get(i).getName().replaceAll("BTC-","");
            JSONArray ccArray = getCCJSON(ccName);
            for(int j = 0; j < ccArray.length(); j++){
                JSONObject price = ccArray.getJSONObject(j);
                double lastPrice = price.getDouble("close");
                coins.get(j).addPrice(lastPrice, Double.NaN, Double.NaN, false);
            }
        }

        analyseMarkets();
    }

    private void analyseMarkets(){

        final Runnable printReport = new Runnable() {

            public void run() {
                System.out.println("---------- REPORT FOR PERIOD " + day + "----------");
                System.out.println("Total Profit/Loss = " + profit);
                System.out.println("Average Profit/Loss = " + profit/totalSold);
                System.out.println("-------------------------------------");
                System.out.println("Coins Bought today = " + coinsBought);
                System.out.println("Coins sold today = " + coinsSold);
                System.out.println("Coins still Hodl = " + coinsHodl);
                System.out.println("-------------------------------------");
                System.out.println("Profit/Loss for day =  " + dayProfit);
                System.out.println("Average Profit/Loss for day = " + dayProfit/coinsSold);
                System.out.println("-------------------------------------");
                System.out.println("Average Profit = " + averageProf/soldProf);
                System.out.println("Coins sold with Profit = " + soldProf);
                System.out.println("Biggest Profit = " + highProf);
                System.out.println("-------------------------------------");
                System.out.println("Average Loss = " + averageLoss/soldLoss);
                System.out.println("Coins sold with Loss = " + soldLoss);
                System.out.println("Biggest Loss = " + highLoss);
                System.out.println("-------------------------------------");
                System.out.println("");

                day++;
                dayProfit = 0; coinsSold = 0; coinsBought = 0; highLoss = 0; highProf = 0; soldLoss = 0; soldProf = 0; averageLoss = 0; averageProf = 0;

            }
        };

        final Runnable gatherData = new Runnable() {

            public void run() {

                JSONArray resultArray = getMarketJson();

                outer:
                for(int j = 0; j < numCoins; j++){
                    JSONObject marketObj = resultArray.getJSONObject(j);
                    for(int k = 0; k < numCoins; k++){
                        if(marketObj.getString("MarketName").equalsIgnoreCase(coins.get(k).getName())){

                            gained = coins.get(k).addPrice(marketObj.getDouble("Last"), marketObj.getDouble("Bid"), marketObj.getDouble("Ask"), true);

                            if(!Double.isNaN(gained)){
                                if(gained > 100.0){
                                    coinsBought++;
                                    coinsHodl++;
                                } else {
                                    coinsHodl--;
                                    profit += gained;
                                    dayProfit += gained;
                                    coinsSold++;
                                    totalSold++;
                                    if(gained < 0.00000000){
                                        if(gained < highLoss){
                                            highLoss = gained;
                                        }
                                        soldLoss++;
                                        averageLoss += gained;
                                    } else {
                                        if(gained > highProf){
                                            highProf = gained;
                                        }
                                        soldProf++;
                                        averageProf += gained;
                                    }
                                }

                            }
                            continue outer;
                        }
                    }
                }
            }
        };

        final ScheduledFuture<?> dataHandler = scheduler.scheduleAtFixedRate(gatherData, 72, 72, TimeUnit.HOURS);
        final ScheduledFuture<?> reportHandler = scheduler.scheduleAtFixedRate(printReport, 72, 72, TimeUnit.HOURS);

    }

    private JSONArray getMarketJson(){
        try{
            URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                response.append(line);
            }

            bufferedReader.close();

            JSONObject obj = new JSONObject(response.toString());
            Boolean success = obj.getBoolean("success");
            if(success) {
                JSONArray resultArray = obj.getJSONArray("result");
                return resultArray;
            } else {
                return null;
            }
        } catch (IOException e){

            System.out.println("ERROR getting JSON: " + e.getMessage());
            return getMarketJson();
        }
    }

    private JSONArray getCCJSON(String name){
        try{
            URL url = new URL("https://min-api.cryptocompare.com/data/histohour?fsym="+name+"&tsym=BTC&limit=168&aggregate=1&e=CCCAGG");
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
