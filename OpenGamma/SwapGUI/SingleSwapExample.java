/*SWAP PRICER by Richard Ruthberg 2018
 * Using OpenGamma copyrighted open source package.
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.examples;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DayCounts;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendarIds;
import com.opengamma.strata.basics.index.IborIndices;
import com.opengamma.strata.basics.index.OvernightIndices;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.PeriodicSchedule;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.basics.value.ValueSchedule;
import com.opengamma.strata.calc.*;
import com.opengamma.strata.calc.runner.CalculationFunctions;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.data.*;
import com.opengamma.strata.examples.marketdata.ExampleData;
import com.opengamma.strata.examples.marketdata.ExampleMarketData;
import com.opengamma.strata.examples.marketdata.ExampleMarketDataBuilder;
import com.opengamma.strata.market.amount.CashFlow;
import com.opengamma.strata.market.amount.CashFlows;
import com.opengamma.strata.market.curve.CurveId;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.param.*;
import com.opengamma.strata.measure.AdvancedMeasures;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.StandardComponents;
import com.opengamma.strata.product.AttributeType;
import com.opengamma.strata.product.Trade;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.swap.*;
import com.opengamma.strata.product.swap.type.FixedIborSwapConventions;
import com.opengamma.strata.product.swap.type.FixedOvernightSwapConventions;
import com.opengamma.strata.report.ReportCalculationResults;
import com.opengamma.strata.report.trade.TradeReport;
import com.opengamma.strata.report.trade.TradeReportTemplate;
import sun.awt.geom.Curve;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;

import java.time.LocalDate;
import java.util.*;

import static com.opengamma.strata.basics.date.BusinessDayConventions.MODIFIED_FOLLOWING;

/**
 * Example to illustrate using the calculation API to price a swap.
 * <p>
 * This makes use of the example market data environment.
 */
public class SingleSwapExample {

  /**
   * Runs the example, pricing the instruments, producing the output as an ASCII table.
   * 
   * @param args  ignored
   */
  //vars
  public static BuySell buy_or_sell = BuySell.BUY;
  public static int notional_amt = 100_000_000;
  public static double fixed_rate = 0.15;
  public static String repTemplate = "swap-report-templateTEST";
  public static Map<String,DoubleArray> curve_points;
  public static Map<String,ArrayList<String>> curve_buckets;
  public static Map<String,Double> cash_flows;
  public static Map<String,Double> discount_delta;
  public static Map<String,Double> forward_delta;

  public static Map<String, DoubleArray> getCurve_points() {
    return curve_points;
  }

  public static Map<String, ArrayList<String>> getCurve_buckets() {
    return curve_buckets;
  }

  public static Map<String, Double> getCash_flows() {
    return cash_flows;
  }

  public static Map<String, Double> getDiscount_delta() {
    return discount_delta;
  }

  public static Map<String, Double> getForward_delta() {
    return forward_delta;
  }



  public static void main(String[] args) {
    // setup calculation runner component, which needs life-cycle management
    // a typical application might use dependency injection to obtain the instance
    /*
    try (CalculationRunner runner = CalculationRunner.ofMultiThreaded()) {
      calculate(runner);
    }
    */
  }

  public SingleSwapExample(BuySell buyorsell, int notional_amt, double fixed_rate, String report_template){
    this.buy_or_sell=buyorsell;
    this.notional_amt = notional_amt;
    this.fixed_rate = fixed_rate;
    if(report_template != null) {
      this.repTemplate = report_template;
    } else{
      this.repTemplate = "swap-report-templateTEST";
    }
  }



  // obtains the data and calculates the grid of results
  public static void calculate(CalculationRunner runner) {
    // the trades that will have measures calculated
    List<Trade> trades = createSwapTrades();

    // the columns, specifying the measures to be calculated
    List<Column> columns = ImmutableList.of(
        Column.of(Measures.LEG_INITIAL_NOTIONAL),
        Column.of(Measures.PRESENT_VALUE),
        Column.of(Measures.LEG_PRESENT_VALUE),
        Column.of(Measures.CASH_FLOWS),
        Column.of(Measures.PV01_CALIBRATED_SUM),
        Column.of(Measures.PAR_RATE),
        Column.of(Measures.ACCRUED_INTEREST),
        Column.of(Measures.PV01_CALIBRATED_BUCKETED));

    // use the built-in example market data
    LocalDate valuationDate = LocalDate.of(2014, 1, 22);
    ExampleMarketDataBuilder marketDataBuilder = ExampleMarketData.builder();
    MarketData marketData = marketDataBuilder.buildSnapshot(valuationDate);

    // the complete set of rules for calculating measures
    CalculationFunctions functions = StandardComponents.calculationFunctions();
    CalculationRules rules = CalculationRules.of(functions, marketDataBuilder.ratesLookup(valuationDate));

    // the reference data, such as holidays and securities
    ReferenceData refData = ReferenceData.standard();

    // calculate the results
    Results results = runner.calculate(rules, trades, columns, marketData, refData);

    // use the report runner to transform the engine results into a trade report
    ReportCalculationResults calculationResults =
        ReportCalculationResults.of(valuationDate, trades, columns, results, functions, refData);

    TradeReportTemplate reportTemplate = ExampleData.loadTradeReportTemplate(repTemplate);
    TradeReport tradeReport = TradeReport.of(calculationResults, reportTemplate);
    tradeReport.writeAsciiTable(System.out);


    //GETTING CURVES, CFS, SENSITIVITIES:
    System.out.println("==================================================================================");
    System.out.println("Retrieving available curves, and generated cash flows and sensitivities for trade.");
    System.out.println("==================================================================================");
    //Obtaining curves from calculation
    System.out.println("RETRIEVING CURVES");
    CurveId cid; //curve id
    InterpolatedNodalCurve inc; //interpolated nodal curve, a "curve" in OG
    CurveMetadata cmd; //metadata for the curve
    Map<String,ArrayList<String>> curveBuckets = new HashMap<String,ArrayList<String>>(); //Buckets on curve for each corresponding curve
    Map<String,DoubleArray> curvePoints = new HashMap<String,DoubleArray>(); //points on curve for each corresponding curve

    String cname = new String();



    for (MarketDataId s : marketData.getIds()) {
      //System.out.println(s.toString());
      //System.out.println(s.getMarketDataType());
      if(s.getMarketDataType().toString().equals("interface com.opengamma.strata.market.curve.Curve")){
        //Only market data of type Curve is fetched
        cid = (CurveId) s;
        cname = ((CurveId) s).getCurveName().toString();
        ArrayList<String> tempList = new ArrayList<String>();
        //txt = marketData.getValue(cid).;
        inc = (InterpolatedNodalCurve) marketData.getValue(cid);
        cmd = (CurveMetadata) marketData.getValue(cid).getMetadata();
        //lpmd = (LabelParameterMetadata) marketData.getValue(cid).getMetadata().getParameterMetadata();

        //System.out.println(cid);
        //System.out.println(s.toString());
        //System.out.println("hey");
        //System.out.println(inc.getXValues());
        //System.out.println(inc.getYValues());
        //cmd.getParameterMetadata().get().forEach( System.out.println );
        for(ParameterMetadata l : cmd.getParameterMetadata().get()){
          //System.out.println(l.property("date").get());
          //System.out.println(l.getLabel());
          tempList.add(l.getLabel());
        }
        System.out.println(">>> Fetching curve points for " + cname + ", values: " + inc.getYValues());
        System.out.println(">>> " + cname + " curve buckets: " + tempList);
        System.out.println("");
        curvePoints.put(cname,inc.getYValues());
        curveBuckets.put(cname,tempList);
        curve_buckets = curveBuckets;
        curve_points = curvePoints;
        //System.out.println(cmd.getParameterMetadata().get().get());

        //System.out.println(marketData.getTimeSeries(s).values().toArray()[0]);
        //System.out.println(marketData.getTimeSeries(s).dates().toArray()[0]);
      }

    }
    //System.out.println(txt);
    //System.out.println(tradeReport.getData().values().asList());
    //System.out.println(results.toString());
    //System.out.println(marketData.getIds().toString());
    //System.out.println(curveBuckets.get("USD-3ML").toString());
    //System.out.println(curvePoints.get("USD-3ML").toString());
    //System.out.println(tradeReport.getData().values().asList());
    //System.out.println(calculationResults.getColumns());

    //Obtain cashflows
    CashFlows cfs;
    cfs = (CashFlows) calculationResults.getCalculationResults().get(0,3).getValue();
    Map<String,Double> cashflows = new HashMap<String,Double>();
    System.out.println("RETRIEVING CASH FLOWS");
    System.out.println(">>> Fetching cash flows for swap trade, values: ");
    for (CashFlow cf : cfs.getCashFlows()) {
      System.out.println(">>> Date = " + cf.getPaymentDate().toString() + "  |  forecast value = " + cf.getForecastValue().toString());
      //System.out.println(cf.getForecastValue().getAmount());
      cashflows.put(cf.getPaymentDate().toString(),cf.getForecastValue().getAmount());
    }
    System.out.println(">>> Cash flows saved.");
    System.out.println("");
    Map<String, Double> cashTree = new TreeMap<>(cashflows);
    cash_flows = cashTree;

    //Obtain sensitivities
    System.out.println("RETRIEVING SENSITIVITIES");
    Map<String,Double> disc_delta_temp = new HashMap<String,Double>();
    Map<String,Double> fwd_delta_temp = new HashMap<String,Double>();
    CurrencyParameterSensitivities deltas;
    deltas = (CurrencyParameterSensitivities) calculationResults.getCalculationResults().get(0,7).getValue();
    //System.out.println(deltas.toString());
    CurrencyParameterSensitivity delta_usd_3m;
    CurrencyParameterSensitivity delta_usd_disc;
    delta_usd_3m = (CurrencyParameterSensitivity) deltas.getSensitivities().get(0);
    delta_usd_disc = (CurrencyParameterSensitivity) deltas.getSensitivities().get(1);
    //DoubleArray fwd_values = delta_usd_3m.getSensitivity();
    //DoubleArray disc_values = delta_usd_disc.getSensitivity();

    delta_usd_3m.sensitivities().toMap().forEach((k,v) -> {
      fwd_delta_temp.put(k.property("date").get().toString(),v.doubleValue());
    });

    delta_usd_disc.sensitivities().toMap().forEach((k,v) -> {
      disc_delta_temp.put(k.property("date").get().toString(),v.doubleValue());
    });

    /*
    int i = 0;
    for (ParameterMetadata pm : delta_usd_3m.sensitivities().toMap().keySet().asList()){
      fwd_delta.put(pm..toString(), fwd_values.get(i));
      i++;
    }

    for(int j=0; j < fwd_values.size(); j++){
      fwd_delta.put(delta_usd_3m.sensitivities().toMap().keySet().asList().get(j).getLabel().toString(),fwd_values.get(j));
    }
    */
    Map<String, Double> fwdTree = new TreeMap<>(fwd_delta_temp);
    forward_delta = fwdTree;
    System.out.println(">>> Forward curve sensitivities on " + delta_usd_3m.getMarketDataName().getName().toString() + " obtained, values: " + forward_delta.toString());
    Map<String, Double> ddTree = new TreeMap<>(disc_delta_temp);
    discount_delta = ddTree;
    System.out.println(">>> Discount curve sensitivities on " + delta_usd_disc.getMarketDataName().getName().toString() + " obtained, values: " + discount_delta.toString());
    System.out.println(">>> Sensitivities saved.");
    System.out.println("");

    //System.out.println(marketData.findIds "Default/USD-3ML").toString());
    System.out.println("==================================================================================");
    System.out.println("Done.");
    System.out.println("==================================================================================");

  }

  //-----------------------------------------------------------------------  
  // create swap trades
  private static List<Trade> createSwapTrades() {
    return ImmutableList.of(
        createVanillaFixedVsLibor3mSwap(buy_or_sell,notional_amt,fixed_rate)
    );
  }

  //-----------------------------------------------------------------------  
  // create a vanilla fixed vs libor 3m swap
  private static Trade createVanillaFixedVsLibor3mSwap(BuySell buyorsell, int notional_amt, double fixed_rate) {
    TradeInfo tradeInfo = TradeInfo.builder()
        .id(StandardId.of("example", "1"))
        .addAttribute(AttributeType.DESCRIPTION, "Fixed vs Libor 3m")
            .tradeDate(LocalDate.of(2014, 1, 22))
        .counterparty(StandardId.of("example", "A"))
        .settlementDate(LocalDate.of(2014, 9, 12))
        .build();
    return FixedIborSwapConventions.USD_FIXED_6M_LIBOR_3M.toTrade(
        tradeInfo,
        LocalDate.of(2016, 9, 12), // the start date
        LocalDate.of(2020, 9, 12), // the end date
        buyorsell,               // indicates wheter this trade is a buy or sell
        notional_amt,               // the notional amount
        fixed_rate);                    // the fixed interest rate
  }

}
