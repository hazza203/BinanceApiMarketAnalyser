public class Main {

    public static void main(String[] args) {

        //************NOTE**************//
        /*
        Change to binacnes api
        Analyse binances total volume over a 25hr MA to a 7hr MA
        Upon convergence analyse if market in in a green trend
        If market is bullish begin trading with data already attained
        Look into trading in shorter term with coins that have been doing well the last 2hrs;
        */

        System.out.println("Hello");
        AnalyseMarkets analyseMarkets = new AnalyseMarkets();
        analyseMarkets.init();
    }
}
