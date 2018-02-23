import Indicators.MACD;
import Indicators.RSI;

/**
 * Created by harry on 2/01/2018.
 */
public class Coin {

    String name;

    private Indicators.MACD macd;
    private Indicators.RSI rsi;

    private double rsiResult;
    private int macdResult;



    public Coin(String name){
        this.name = name;
        macd = new MACD();
        rsi = new RSI();

    }

    public void addPrice(Double price){
        macdResult = macd.addPrice(price);
        rsiResult = rsi.addPrice(price);
        if(macdResult == 1){
            System.out.println(name + " has just crossed over the signal line");
            System.out.println("RSI = " + rsiResult);
        }


    }

    public String getName(){return name;}
}
