package Indicators;

import java.util.ArrayList;

public class RSI {

    ArrayList<Double> last12, last26;
    int RSIcount = 0;
    double avLoss, avGain, rsi = 0;


    public RSI(){
        last12 = new ArrayList();
        last26 = new ArrayList();
    }

    public double addPrice(double price){
        last12.add(price);
        last26.add(price);

        if(last12.size() > 12){
            last12.remove(0);
        }

        if(last26.size() > 26) {
            last26.remove(0);
            calculateRSI();
        }
        return rsi;
    }

    private void calculateRSI() {
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

        rsi = 100 - (100 / (1 + RS));

    }

}
