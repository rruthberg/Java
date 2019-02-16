package FinApps;
import BaseStats.Gaussian;

import java.util.HashMap;
import java.util.Map;

public class BlackScholes {
    //Simple class for calculating Black-Scholes-Merton-based option price and greeks

    //PROB
    private double d1 = 0.0;
    private double d2 = 0.0;
    private double p1 = 0.0;
    private double p2 = 0.0;
    private double pdf1 = 0.0;
    private double pdf2 = 0.0;

    //OPTION PRICE
    private double callprice = 0.0;
    private double putprice = 0.0;

    //CARRY
    private double crate = 0.0; //carry rate r
    private double brate = 0.0; //cost of carry

    //GREEKS
    private double deltac = 0.0;
    private double deltap = 0.0;
    private double gamma = 0.0;
    private double vega = 0.0;
    private double thetac = 0.0; //TBD
    private double thetap = 0.0; //TBD
    private double rhoc = 0.0; //TBD
    private double rhop = 0.0; //TBD

    public BlackScholes(){}
    public BlackScholes(double carry_rate){
        crate = carry_rate;
    }

    private void calcProbValues(
            double price_underlying,
            double strike,
            double volatility,
            double time,
            double rate
    ){
        //Calculate probability parameters
        if(volatility*time != 0.0){
            d1 = ( Math.log(price_underlying/strike) + time*(brate + (Math.pow(volatility,2))/2) )/(volatility*Math.sqrt(time));
            d2 = d1 - volatility*Math.sqrt(time);
        }
        p1 = Gaussian.cdf(d1);
        p2 = Gaussian.cdf(d2);
        pdf1 = Gaussian.pdf(d1);
        pdf2 = Gaussian.pdf(d2);
    }

    public void calculate(
            double price_underlying,
            double strike,
            double volatility,
            double time,
            double rate
    ){
        //Cost of carry:
        brate = (crate==0.0?0.0:(
                brate =crate!=rate?(rate-crate):rate
        ));

        //Probcalcs
        calcProbValues(price_underlying,strike,volatility,time,rate);

        //Price calculations using standard Black-Scholes-Merton
        callprice = price_underlying*Math.exp(time*(brate-crate))*p1 - strike*Math.exp(-rate*time)*p2;
        putprice = strike*Math.exp(-rate*time)*Gaussian.cdf(-d2) - price_underlying*Math.exp(time*(brate-crate))*Gaussian.cdf(-d1);

        //Greeks calculation
        deltac = Math.exp(time*(brate-crate))*p1;
        deltap = Math.exp(time*(brate-crate))*(p1-1);
        gamma = pdf1*Math.exp(time*(brate-crate))/(price_underlying*volatility*Math.sqrt(time));
        vega = price_underlying*Math.exp(time*(brate-crate))*pdf1*Math.sqrt(time);

    }


    //GETTER
    public Map<String,String> getValues(){
        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("call_price",Double.toString(callprice));
        returnMap.put("put_price",Double.toString(putprice));
        returnMap.put("call_delta",Double.toString(deltac));
        returnMap.put("put_delta",Double.toString(deltap));
        returnMap.put("gamma",Double.toString(gamma));
        returnMap.put("vega",Double.toString(vega));
        return returnMap;
    }
}
