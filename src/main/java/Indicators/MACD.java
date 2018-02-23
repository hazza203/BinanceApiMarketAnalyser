package Indicators;

import java.util.ArrayList;

public class MACD {

    ArrayList<Double> last12, last26, last9MACD;
    double ema12 = 101, ema26, MACD, signalLine;
    boolean holdOff = true, holdOff1 = true, belowSignal = false, aboveSignal = false, convergence = false, divergence = false ;
    int result = 0;

    public MACD(){
        last12 = new ArrayList();
        last26 = new ArrayList();
        last9MACD = new ArrayList();
    }

    public int addPrice(double price){
        last12.add(price);
        last26.add(price);

        if(last12.size() > 12){
            last12.remove(0);
        }

        if(last26.size() > 26) {
            last26.remove(0);
            result = calculateMACD(price);
            return result;
        } else {
            return 0;
        }
    }

    public int calculateMACD(double price) {
        //First ema's are just the standard MA, calculate them and then return
        if (holdOff) {
            double ma12Total = 0, ma26Total = 0;
            for (int i = 0; i < last12.size(); i++) {
                ma12Total += last12.get(i);
            }

            for (int i = 0; i < last26.size(); i++) {
                ma26Total += last26.get(i);
            }

            ema12 = ma26Total / 26;
            ema26 = ma12Total / 12;
            holdOff = false;
            return 0;
        }

        //Calculate the new ema's
        ema12 = (price - ema12) * 0.1538 + ema12;
        ema26 = (price - ema26) * 0.07407 + ema26;


        //Calculate the MACD
        MACD = ema12 - ema26;
        last9MACD.add(MACD);

        //Calculate the signal line,
        if (last9MACD.size() > 9) {
            last9MACD.remove(0);

            //If signal line has not been calculate yet it is simply the SMA of the 9 period MACD.
            if (holdOff1) {
                for (int i = 0; i < 9; i++) {
                    signalLine += last9MACD.get(i);
                }
                signalLine = signalLine / 9;
                holdOff1 = false;
                return 0;
                //Calculate 9 period EMA for the signal line
            } else {
                signalLine = (MACD - signalLine) * 0.2 + signalLine;

                //Finding out convergence/divergence points
                if (MACD < signalLine) {
                    belowSignal = true;
                }

                if (MACD > signalLine && belowSignal) {
                    convergence = true;
                    aboveSignal = true;
                    belowSignal = false;
                    return 1;
                }

                if (MACD < signalLine && aboveSignal) {
                    divergence = true;
                    aboveSignal = false;
                    belowSignal = true;
                    return -1;
                }
            }
        }
        return 0;
    }

}
