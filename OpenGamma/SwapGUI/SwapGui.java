/*SWAP PRICER by Richard Ruthberg 2018
 * Using OpenGamma copyrighted open source package.
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.examples.gui;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.FxConvertible;
import com.opengamma.strata.basics.date.*;
import com.opengamma.strata.basics.schedule.*;
import com.opengamma.strata.calc.CalculationRunner;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.swap.Swap;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Callback;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;

//Other imports from examples and plotting
import com.opengamma.strata.examples.SwapPricingExample;
import com.opengamma.strata.examples.SingleSwapExample;

/**
 * A simple GUI demonstration of swap pricing
 */
public class SwapGui extends Application {

  // the reference data to use
  private static final ReferenceData REF_DATA = ReferenceData.standard();

  /**
   * Launch GUI, no arguments needed.
   * 
   * @param args  no arguments needed
   */
  public static void main(String[] args) {
    launch(args);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start(Stage primaryStage) {

    //Generate swap






    //----------


    LocalDate today = LocalDate.now(ZoneId.systemDefault());

    // setup GUI elements
    Label headerLbl = new Label("Swap trade details");

    Label startLbl = new Label("Start date:");
    DatePicker startInp = new DatePicker(LocalDate.of(2014, 8, 27));
    startLbl.setLabelFor(startInp);
    startInp.setShowWeekNumbers(false);

    Label endLbl = new Label("End date:");
    DatePicker endInp = new DatePicker(LocalDate.of(2024, 8, 27));
    endLbl.setLabelFor(endInp);
    endInp.setShowWeekNumbers(false);

    Label bsLbl = new Label("Buy/Sell:");
    ChoiceBox<BuySell> bsInp = new ChoiceBox<>(
            FXCollections.observableArrayList(
                    BuySell.BUY, BuySell.SELL
            )
    );
    bsLbl.setLabelFor(bsInp);
    bsInp.setValue(BuySell.BUY);

    Label notLbl = new Label("Notional amount:");
    ChoiceBox<Integer> notInp = new ChoiceBox<>(
            FXCollections.observableArrayList(
                    100_000, 500_000, 1_000_000, 5_000_000, 10_000_000,
                    50_000_000, 100_000_000, 150_000_000,
                    200_000_000, 250_000_000, 500_000_000, 1_000_000_000
            )
    );
    notLbl.setLabelFor(notInp);
    notInp.setValue(100_000_000);

    Label coupLbl = new Label("Fixed coupon:");
    ChoiceBox<Double> coupInp = new ChoiceBox<>(
            FXCollections.observableArrayList(
                    0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0, 3.25, 3.5, 3.75, 4.0,
                    5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.5, 15.0, 17.5, 20.0, 25.0, 30.0
            )
    );
    coupLbl.setLabelFor(coupInp);
    coupInp.setValue(3.0);


    Label freqLbl = new Label("Frequency:");
    ChoiceBox<Frequency> freqInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            Frequency.P1M, Frequency.P2M, Frequency.P3M, Frequency.P4M, Frequency.P6M, Frequency.P12M));
    freqLbl.setLabelFor(freqInp);
    freqInp.setValue(Frequency.P3M);

    Label stubLbl = new Label("Stub:");
    ObservableList<StubConvention> stubOptions = FXCollections.observableArrayList(StubConvention.values());
    stubOptions.add(0, null);
    ChoiceBox<StubConvention> stubInp = new ChoiceBox<>(stubOptions);
    stubLbl.setLabelFor(stubInp);
    stubInp.setValue(StubConvention.SHORT_INITIAL);

    Label rollLbl = new Label("Roll:");
    ChoiceBox<RollConvention> rollInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            null,
            RollConventions.NONE,
            RollConventions.EOM,
            RollConventions.IMM,
            RollConventions.IMMAUD,
            RollConventions.IMMNZD,
            RollConventions.SFE));
    rollLbl.setLabelFor(rollInp);
    rollInp.setValue(RollConventions.NONE);

    Label bdcLbl = new Label("Adjust:");
    ChoiceBox<BusinessDayConvention> bdcInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            BusinessDayConventions.NO_ADJUST,
            BusinessDayConventions.FOLLOWING,
            BusinessDayConventions.MODIFIED_FOLLOWING,
            BusinessDayConventions.PRECEDING,
            BusinessDayConventions.MODIFIED_PRECEDING,
            BusinessDayConventions.MODIFIED_FOLLOWING_BI_MONTHLY,
            BusinessDayConventions.NEAREST));
    bdcLbl.setLabelFor(bdcInp);
    bdcInp.setValue(BusinessDayConventions.MODIFIED_FOLLOWING);

    Label holidayLbl = new Label("Holidays:");
    ChoiceBox<HolidayCalendarId> holidayInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            HolidayCalendarIds.CHZU,
            HolidayCalendarIds.GBLO,
            HolidayCalendarIds.EUTA,
            HolidayCalendarIds.FRPA,
            HolidayCalendarIds.JPTO,
            HolidayCalendarIds.NYFD,
            HolidayCalendarIds.NYSE,
            HolidayCalendarIds.USNY,
            HolidayCalendarIds.USGS,
            HolidayCalendarIds.NO_HOLIDAYS,
            HolidayCalendarIds.SAT_SUN));
    holidayLbl.setLabelFor(holidayInp);
    holidayInp.setValue(HolidayCalendarIds.GBLO);


    //-----Charting
    //Run pricer
    //SingleSwapExample sse = new SingleSwapExample(BuySell.BUY,100_000_000,0.03,"swap-report-templateTEST");
    SingleSwapExample sse = new SingleSwapExample(bsInp.getValue(),notInp.getValue(),coupInp.getValue()/100,"swap-report-templateTEST");
    try (CalculationRunner runner = CalculationRunner.ofMultiThreaded()){
      sse.calculate(runner);
    }

    //Load data from swap
    Map<String,Double> forward_delta = sse.getForward_delta();
    Map<String,Double> discount_delta = sse.getDiscount_delta();
    Map<String,DoubleArray> curve_points = sse.getCurve_points();
    Map<String,ArrayList<String>> curve_buckets = sse.getCurve_buckets();
    Map<String,Double> cash_flows = sse.getCash_flows();
    DoubleArray curve_usd_disc = curve_points.get("USD-Disc");
    ArrayList<String> buckets_usd_disc = curve_buckets.get("USD-Disc");
    DoubleArray curve_usd_3m = curve_points.get("USD-3ML");
    ArrayList<String> buckets_usd_3m = curve_buckets.get("USD-3ML");
    DoubleArray curve_usd_6m = curve_points.get("USD-6ML");
    ArrayList<String> buckets_usd_6m = curve_buckets.get("USD-6ML");

    //Plot curves chart
    //Label plotLbl = new Label("Curves");

    final NumberAxis yAxis = new NumberAxis();
    final CategoryAxis xAxis = new CategoryAxis();
    final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
    yAxis.setAutoRanging(true);
    yAxis.setLabel("Rate");
    xAxis.setLabel("Point");
    lineChart.setTitle("Forward Curve");

    XYChart.Series series = new XYChart.Series();
    XYChart.Series seriesA = new XYChart.Series();
    XYChart.Series seriesB = new XYChart.Series();

    series.setName("USD-Disc");
    seriesA.setName("USD Libor 3M");
    seriesB.setName("USD Libor 6M");
    int Adiff = 0;
    int Bdiff = 0;

    for(int i = 0; i < curve_usd_disc.size(); i++){
      String axisname = buckets_usd_disc.get(i);
      series.getData().add(new XYChart.Data(axisname,curve_usd_disc.get(i)*100));
      if(buckets_usd_3m.get(i-Adiff).equals(axisname)) {
        seriesA.getData().add(new XYChart.Data(axisname,curve_usd_3m.get(i-Adiff)*100));
      } else {
        Adiff++;
      }
      if(buckets_usd_6m.get(i-Bdiff).equals(axisname)) {
        seriesB.getData().add(new XYChart.Data(axisname, curve_usd_6m.get(i-Bdiff) * 100));
      } else {
        Bdiff++;
      }
    }
    lineChart.getData().addAll(series,seriesA,seriesB);

    //Plot cashflow chart
    //Label plotLbl2 = new Label("Cash flows");
    final NumberAxis yAxis2 = new NumberAxis();
    final CategoryAxis xAxis2 = new CategoryAxis();
    final BarChart<String, Number> barChart = new BarChart<>(xAxis2, yAxis2);
    yAxis2.setAutoRanging(true);
    yAxis2.setLabel("Cash flow");
    xAxis2.setLabel("Time");
    barChart.setTitle("Future cash flows");

    XYChart.Series cf_series = new XYChart.Series();

    cf_series.setName("Future CF");

    cash_flows.forEach((k,v) -> {
      String axisname2 = k.toString();
      cf_series.getData().add(new XYChart.Data(axisname2,v.doubleValue()));
    });

    barChart.getData().addAll(cf_series);

    //Plot deltas
    //Label plotLbl2 = new Label("Cash flows");
    //Forward delta
    final NumberAxis yAxis3 = new NumberAxis();
    final CategoryAxis xAxis3 = new CategoryAxis();
    final BarChart<String, Number> barChart2 = new BarChart<>(xAxis3, yAxis3);
    yAxis3.setAutoRanging(true);
    yAxis3.setLabel("Forward Delta, $/bp");
    xAxis3.setLabel("Time buckets");
    barChart2.setTitle("Forward Curve Sensitivities (PV01BP)");

    XYChart.Series fwd_series = new XYChart.Series();
    //XYChart.Series disc_series = new XYChart.Series();

    fwd_series.setName("Forward delta, USD 3M Libor");
    forward_delta.forEach((k,v) -> {
      //if(v.doubleValue() != 0.0) {
        String axisname3 = k.toString();
        fwd_series.getData().add(new XYChart.Data(axisname3,v.doubleValue()));
      //}
    });

    barChart2.getData().addAll(fwd_series);
    //Double fwd_max = (Double) fwd_series.getData().filtered(.);
    //Double fwd_min = (Double) fwd_series.getData().sorted().get(fwd_series.getData().size());

    //Discount delta
    final NumberAxis yAxis4 = new NumberAxis();
    final CategoryAxis xAxis4 = new CategoryAxis();
    final BarChart<String, Number> barChart3 = new BarChart<>(xAxis4, yAxis4);
    yAxis4.setAutoRanging(true);
    yAxis4.setLabel("Discount Delta, $/bp");
    xAxis4.setLabel("Time buckets");
    barChart3.setTitle("Discount Curve Sensitivities (PV01BP)");

    XYChart.Series disc_series = new XYChart.Series();

    disc_series.setName("Discount delta, Fed funds");


    discount_delta.forEach((k,v) -> {
      //if(v.doubleValue() != 0.0){
        String axisname4 = k.toString();
        disc_series.getData().add(new XYChart.Data(axisname4,v.doubleValue()));
      //}

    });

    barChart3.getData().addAll(disc_series);
    //-------------




    // setup generation button
    Button btn = new Button();
    btn.setText("Generate");
    btn.setOnAction(event -> {
      
      SingleSwapExample sse2 = new SingleSwapExample(bsInp.getValue(),notInp.getValue(),coupInp.getValue()/100,"swap-report-templateTEST");
      try (CalculationRunner runner = CalculationRunner.ofMultiThreaded()){

        //Price new swap
        sse2.calculate(runner);

        //Plot cashflow chart
        barChart.getData().clear();
        Map<String,Double> cash_flows2 = sse2.getCash_flows();
        XYChart.Series cf_series2 = new XYChart.Series();
        cf_series2.setName("Future CF");
        cash_flows2.forEach((k,v) -> {
          String axisname2 = k.toString();
          cf_series2.getData().add(new XYChart.Data(axisname2,v.doubleValue()));
        });
        barChart.getData().addAll(cf_series2);

        //Plot new fwd chart
        barChart2.getData().clear();
        Map<String,Double> forward_delta2 = sse2.getForward_delta();
        XYChart.Series fwd_series2 = new XYChart.Series();
        fwd_series2.setName("Forward delta, USD 3M Libor");
        forward_delta2.forEach((k,v) -> {
          String axisname3 = k.toString();
          fwd_series2.getData().add(new XYChart.Data(axisname3,v.doubleValue()));
        });
        barChart2.getData().addAll(fwd_series2);

        //Plot new dd chart
        barChart3.getData().clear();
        Map<String,Double> discount_delta2 = sse2.getDiscount_delta();
        XYChart.Series disc_series2 = new XYChart.Series();
        disc_series2.setName("Discount delta, Fed funds");
        discount_delta2.forEach((k,v) -> {
          String axisname4 = k.toString();
          disc_series2.getData().add(new XYChart.Data(axisname4,v.doubleValue()));
        });
        barChart3.getData().addAll(disc_series2);
        
      } catch (Exception e){
        System.out.print(e.getMessage());
      }

      //}
    //});
    });





    // layout the components
    GridPane gp = new GridPane();
    gp.setHgap(10);
    gp.setVgap(10);
    gp.setPadding(new Insets(0, 10, 0, 10));
    gp.add(headerLbl,1,1,2,1);
    gp.add(startLbl, 1, 2);
    gp.add(startInp, 2, 2);
    gp.add(endLbl, 1, 3);
    gp.add(endInp, 2, 3);
    gp.add(notLbl,1,4);
    gp.add(notInp,2,4);
    gp.add(bsLbl, 1, 5);
    gp.add(bsInp, 2, 5);
    gp.add(coupLbl,1,6);
    gp.add(coupInp,2,6);
    gp.add(bdcLbl, 3, 2);
    gp.add(bdcInp, 4, 2);
    gp.add(holidayLbl, 3, 3);
    gp.add(holidayInp, 4, 3);
    gp.add(stubLbl, 3, 4);
    gp.add(stubInp, 4, 4);
    gp.add(rollLbl, 3, 5);
    gp.add(rollInp, 4, 5);
    //gp.add(new Label(""),1,7,2,2);
    gp.add(btn, 3, 6, 2, 1);
    gp.add(barChart, 10, 1,5,3); //CFS
    gp.add(barChart3,10,4,5,3); //DDelta
    gp.add(lineChart, 5, 1,5,3); //Curves
    gp.add(barChart2, 5, 4,5,3); //FwdDelta

    BorderPane bp = new BorderPane(gp);
    Scene scene = new Scene(bp, 1200, 800);

    // launch
    primaryStage.setTitle("Swap Pricer");
    primaryStage.setScene(scene);
    primaryStage.show();


  }

  //-------------------------------------------------------------------------

}
