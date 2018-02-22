import java.util.ArrayList;

/**
 * Created by harry on 2/01/2018.
 */
public class Coin {

    String name;
    ArrayList<Double> last12, last26, last9MACD, sma7day;
    int RSIcount = 0, count = 0, ma7day = 0, hrNum = 0;
    double ma12, ma26, ema12 = 101, ema26, MACD, signalLine, buyPrice, hodl, gains;
    double avLoss, avGain, RSI, highPrice = 0;
    boolean wasBelow = false, ma12Crossedma26 = false, ema12Crossed26 = false;
    boolean wasAbove = false, ma26Crossedma12 = false, ema26Crossed12 = false;
    boolean hasBought = false;
    boolean firstDrop = false;
    boolean holdOff = true, holdOff1 = true;
    boolean tradable = false;
    double bid, ask;



    public Coin(String name){
        this.name = name;
        last12 = new ArrayList();
        last26 = new ArrayList();
        last9MACD = new ArrayList();
        sma7day = new ArrayList();
    }

    public double addPrice(Double price, Double bid, Double ask, boolean condition){

        tradable = condition;
        this.bid = bid;
        this.ask = ask;
        last12.add(price);
        last26.add(price);

        if(last12.size() > 12){
            last12.remove(0);
        }

        if(last26.size() > 26) {
            last26.remove(0);
            calculateRSI();
            calculateEMA(price);
            // calculateMA();
            if(buyTrigger()){
                buyCoin(price);
                return 121.01;
            }


            if(hasBought && sellTrigger(price)){
                sellCoin(price);
                return gains;
            }
        }


        return Double.NaN;
    }

    private void buyCoin(double price){

        hodl = (0.001 / ask);



        hasBought = true;

    }

    private void sellCoin(double bid) {
        hasBought = false;
        gains = hodl * bid - 0.001005;

    }

    public boolean buyTrigger() {
        //if it 7 hr av was previously below 25 hr av and has now crossed the 25 hr av
        //we should buy, set was above boolean to true and the conditions for buying to false;

        if (wasBelow && ma12Crossedma26) {
            wasBelow = false;
            ma12Crossedma26 = false;
            wasAbove = true;
            return true;
        }


        return false;
    }

    public boolean sellTrigger(double price) {
        //if it wasAbove (only set when to true if we bought) and the 25 hr av has gone below the
        // 7 hr av then we should sell, set was below to true as now below and the sell conditions back to false

        if (wasAbove && ma26Crossedma12) {
            wasBelow = true;
            ma26Crossedma12 = false;
            wasAbove = false;
            return true;
        }

        return false;
    }

    private void calculateMA() {
        double ma12Total = 0, ma26Total = 0;
        for (int i = 0; i < last12.size(); i++) {
            ma12Total += last12.get(i);
        }

        for (int i = 0; i < last26.size(); i++) {
            ma26Total += last26.get(i);
        }

        ma26 = ma26Total / 26;
        ma12 = ma12Total / 12;

        if (hrNum == 0) {
            hrNum = 23;
            ma7day++;
            sma7day.add(last12.get(last12.size() - 1));
        }
        hrNum--;


        if (sma7day.size() == 8) {
            sma7day.remove(0);
        }


        //If 7 hour average is less than 25 hour average
        if (ma12 < ma26) {
            wasBelow = true;
        }

        //If 7 hour average has moved above 25 hour average
        if (ma12 > ma26 && wasBelow) {
            ma12Crossedma26 = true;
        }

        //if 7 hour average has moved below 25 hour average from previously being above
        if (ma12 < ma26 && wasAbove) {
            ma26Crossedma12 = true;
        }

    }

    public void calculateEMA(double price) {
        if (holdOff) {
            holdOff = false;
            return;
        }

        if (ema12 > 100) {
            ema12 = ma12;
            ema26 = ma26;
            return;
        } else {
            ema12 = (price - ema12) * 0.1538 + ema12;
            ema26 = (price - ema26) * 0.07407 + ema26;
        }

        MACD = ema12 - ema26;
        last9MACD.add(MACD);

        if (last9MACD.size() > 9) {
            last9MACD.remove(0);

            if (holdOff1) {
                for (int i = 0; i < 9; i++) {
                    signalLine += last9MACD.get(i);
                }
                signalLine = signalLine / 9;
                holdOff1 = false;
            } else {
                signalLine = (MACD - signalLine) * 0.2 + signalLine;
                //If 7 hour average is less than 25 hour average

                if(tradable){
                    if (MACD < signalLine) {
                        wasBelow = true;
                    }

                    //If 7 hour average has moved above 25 hour average
                    if (MACD > signalLine && wasBelow) {
                        ma12Crossedma26 = true;
                    }

                    //if 7 hour average has moved below 25 hour average from previously being above
                    if (MACD < signalLine && wasAbove) {
                        ma26Crossedma12 = true;
                    }
                }

            }


        }


    }

    public void calculateRSI() {
        double RS;
        if (RSIcount == 0) {
            for (int i = 1; i < last26.size(); i++) {
                if (last26.get(i) < last26.get(i - 1)) {
                    avLoss = avLoss + (last26.get(i - 1) - last26.get(i));
                } else {
                    avGain = avGain + (last26.get(i) - last26.get(i - 1));
                }
            }
            avLoss = avLoss / 24;
            avGain = avGain / 24;
            RSIcount = 1;
        } else {
            if (last26.get(last26.size() - 1) < last26.get(last26.size() - 2)) {
                avLoss = ((avLoss * 25) + (last26.get(last26.size() - 2) - last26.get(last26.size() - 1))) / 25;
            } else {
                avGain = ((avGain * 25) + (last26.get(last26.size() - 1) - last26.get(last26.size() - 2))) / 25;
            }


        }

        RS = avGain / avLoss;

        RSI = 100 - (100 / (1 + RS));


    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    public double get7day() {
        return sma7day.get(sma7day.size() - 1) - sma7day.get(0);
    }

    public String getName() {
        return name;
    }

}
