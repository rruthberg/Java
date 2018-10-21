/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.examples.gui;

import com.opengamma.strata.basics.ReferenceData;
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
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;

//Other imports from examples and plotting
import com.opengamma.strata.examples.SwapPricingExample;
import com.opengamma.strata.examples.SingleSwapExample;

/**
 * A simple GUI demonstration of schedule generation.
 * <p>
 * This provides a GUI based on {@link PeriodicSchedule} and {@link Schedule}.
 * <p>
 * This GUI exists for demonstration purposes to aid with understanding schedule generation.
 * It is not intended to be used in a production environment.
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

    SingleSwapExample sse = new SingleSwapExample(BuySell.BUY,200_000_000,0.03,"swap-report-templateTEST");
    try (CalculationRunner runner = CalculationRunner.ofMultiThreaded()){
      sse.calculate(runner);
    }
    Map<String,Double> forward_delta = sse.getForward_delta();
    Map<String,Double> discount_delta = sse.getDiscount_delta();
    Map<String,DoubleArray> curve_points = sse.getCurve_points();
    Map<String,ArrayList<String>> curve_buckets = sse.getCurve_buckets();
    DoubleArray curve_usd_disc = curve_points.get("USD-Disc");
    ArrayList<String> buckets_usd_disc = curve_buckets.get("USD-Disc");
    DoubleArray curve_usd_3m = curve_points.get("USD-3ML");
    ArrayList<String> buckets_usd_3m = curve_buckets.get("USD-3ML");
    DoubleArray curve_usd_6m = curve_points.get("USD-6ML");
    ArrayList<String> buckets_usd_6m = curve_buckets.get("USD-6ML");
    System.out.println(curve_usd_disc);
    System.out.println(curve_usd_3m);
    System.out.println(curve_usd_6m);
    System.out.println(buckets_usd_disc);
    System.out.println(buckets_usd_3m);
    System.out.println(buckets_usd_6m);

    Map<String,Double> cash_flows = sse.getCash_flows();
    System.out.println(cash_flows.toString());

    //sse.getForwardDelta();
    //----------


    LocalDate today = LocalDate.now(ZoneId.systemDefault());

    // setup GUI elements
    Label startLbl = new Label("Start date:");
    DatePicker startInp = new DatePicker(today);
    startLbl.setLabelFor(startInp);
    startInp.setShowWeekNumbers(false);

    Label endLbl = new Label("End date:");
    DatePicker endInp = new DatePicker(today.plusYears(1));
    endLbl.setLabelFor(endInp);
    endInp.setShowWeekNumbers(false);

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

    TableView<SchedulePeriod> resultGrid = new TableView<>();
    TableColumn<SchedulePeriod, LocalDate> unadjustedCol = new TableColumn<>("Unadjusted dates");
    TableColumn<SchedulePeriod, LocalDate> adjustedCol = new TableColumn<>("Adjusted dates");

    TableColumn<SchedulePeriod, LocalDate> resultUnadjStartCol = new TableColumn<>("Start");
    resultUnadjStartCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().unadjustedStartDate()));
    TableColumn<SchedulePeriod, LocalDate> resultUnadjEndCol = new TableColumn<>("End");
    resultUnadjEndCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().unadjustedEndDate()));
    TableColumn<SchedulePeriod, Period> resultUnadjLenCol = new TableColumn<>("Length");
    resultUnadjLenCol.setCellValueFactory(ReadOnlyCallback.of(
        sch -> Period.between(sch.getUnadjustedStartDate(), sch.getUnadjustedEndDate())));

    TableColumn<SchedulePeriod, LocalDate> resultStartCol = new TableColumn<>("Start");
    resultStartCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().startDate()));
    TableColumn<SchedulePeriod, LocalDate> resultEndCol = new TableColumn<>("End");
    resultEndCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().endDate()));
    TableColumn<SchedulePeriod, Period> resultLenCol = new TableColumn<>("Length");
    resultLenCol.setCellValueFactory(ReadOnlyCallback.of(sch -> sch.length()));

    unadjustedCol.getColumns().add(resultUnadjStartCol);
    unadjustedCol.getColumns().add(resultUnadjEndCol);
    unadjustedCol.getColumns().add(resultUnadjLenCol);
    adjustedCol.getColumns().add(resultStartCol);
    adjustedCol.getColumns().add(resultEndCol);
    adjustedCol.getColumns().add(resultLenCol);
    resultGrid.getColumns().add(unadjustedCol);
    resultGrid.getColumns().add(adjustedCol);
    resultGrid.setPlaceholder(new Label("Schedule not yet generated"));

    unadjustedCol.prefWidthProperty().bind(resultGrid.widthProperty().divide(2));
    adjustedCol.prefWidthProperty().bind(resultGrid.widthProperty().divide(2));
    resultUnadjStartCol.prefWidthProperty().bind(unadjustedCol.widthProperty().divide(3));
    resultUnadjEndCol.prefWidthProperty().bind(unadjustedCol.widthProperty().divide(3));
    resultUnadjLenCol.prefWidthProperty().bind(unadjustedCol.widthProperty().divide(3));
    resultStartCol.prefWidthProperty().bind(adjustedCol.widthProperty().divide(3));
    resultEndCol.prefWidthProperty().bind(adjustedCol.widthProperty().divide(3));
    resultLenCol.prefWidthProperty().bind(adjustedCol.widthProperty().divide(3));

    // setup generation button
    // this uses the GUI thread which is not the best idea
    Button btn = new Button();
    btn.setText("Generate");
    btn.setOnAction(event -> {
      LocalDate start = startInp.getValue();
      LocalDate end = endInp.getValue();
      Frequency freq = freqInp.getValue();
      StubConvention stub = stubInp.getValue();
      RollConvention roll = rollInp.getValue();
      HolidayCalendarId holCal = holidayInp.getValue();
      BusinessDayConvention bdc = bdcInp.getValue();
      BusinessDayAdjustment bda = BusinessDayAdjustment.of(bdc, holCal);
      PeriodicSchedule defn = PeriodicSchedule.builder()
          .startDate(start)
          .endDate(end)
          .frequency(freq)
          .businessDayAdjustment(bda)
          .stubConvention(stub)
          .rollConvention(roll)
          .build();
      try {
        Schedule schedule = defn.createSchedule(REF_DATA);
        System.out.println(schedule);
        resultGrid.setItems(FXCollections.observableArrayList(schedule.getPeriods()));
      } catch (ScheduleException ex) {
        resultGrid.setItems(FXCollections.emptyObservableList());
        resultGrid.setPlaceholder(new Label(ex.getMessage()));
        System.out.println(ex.getMessage());
      }
    });

    //Plot curves chart
    //Label plotLbl = new Label("Curves");

    final NumberAxis yAxis = new NumberAxis(-0.25, 3.5, 1);
    final CategoryAxis xAxis = new CategoryAxis();
    final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
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

    final NumberAxis yAxis2 = new NumberAxis(-2000000, 2000000, 1000000);
    final CategoryAxis xAxis2 = new CategoryAxis();
    final BarChart<String, Number> barChart = new BarChart<>(xAxis2, yAxis2);
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

    final NumberAxis yAxis3 = new NumberAxis(-150000, 150000, 1000);
    final CategoryAxis xAxis3 = new CategoryAxis();
    final BarChart<String, Number> barChart2 = new BarChart<>(xAxis3, yAxis3);
    yAxis3.setLabel("Delta (PV01BP)");
    xAxis3.setLabel("Time buckets");
    barChart2.setTitle("Sensitivities");

    XYChart.Series fwd_series = new XYChart.Series();
    XYChart.Series disc_series = new XYChart.Series();

    fwd_series.setName("Forward delta, USD 3M Libor");
    disc_series.setName("Discount delta, Fed funds");

    forward_delta.forEach((k,v) -> {
      if(v.doubleValue() != 0.0) {
        String axisname3 = k.toString();
        fwd_series.getData().add(new XYChart.Data(axisname3,v.doubleValue()));
      }

    });

    discount_delta.forEach((k,v) -> {
      if(v.doubleValue() != 0.0){
        String axisname3 = k.toString();
        disc_series.getData().add(new XYChart.Data(axisname3,v.doubleValue()));
      }

    });

    barChart2.getData().addAll(fwd_series,disc_series);




    // layout the components
    GridPane gp = new GridPane();
    gp.setHgap(10);
    gp.setVgap(10);
    gp.setPadding(new Insets(0, 10, 0, 10));
    gp.add(startLbl, 1, 1);
    gp.add(startInp, 2, 1);
    gp.add(endLbl, 1, 2);
    gp.add(endInp, 2, 2);
    gp.add(freqLbl, 1, 3);
    gp.add(freqInp, 2, 3);
    gp.add(bdcLbl, 3, 1);
    gp.add(bdcInp, 4, 1);
    gp.add(holidayLbl, 3, 2);
    gp.add(holidayInp, 4, 2);
    gp.add(stubLbl, 3, 3);
    gp.add(stubInp, 4, 3);
    gp.add(rollLbl, 3, 4);
    gp.add(rollInp, 4, 4);
    gp.add(new Label(""),4,5);
    //gp.add(btn, 3, 5, 2, 1);
    //gp.add(resultGrid, 1, 7, 4, 1);
    //gp.add(plotLbl, 5, 1);
    gp.add(lineChart, 5, 1);
    //gp.add(plotLbl2, 5, 3);
    gp.add(barChart, 5, 2);
    gp.add(barChart2, 5, 3);
    BorderPane bp = new BorderPane(gp);
    Scene scene = new Scene(bp, 1000, 600);

    // launch
    primaryStage.setTitle("Swap Pricer");
    primaryStage.setScene(scene);
    primaryStage.show();


  }

  //-------------------------------------------------------------------------
  // link Joda-Bean meta property to JavaFX
  static class TableCallback<S extends Bean, T> implements Callback<CellDataFeatures<S, T>, ObservableValue<T>> {
    private final MetaProperty<T> property;

    public TableCallback(MetaProperty<T> property) {
      this.property = property;
    }

    @Override
    public ObservableValue<T> call(CellDataFeatures<S, T> param) {
      return getCellDataReflectively(param.getValue());
    }

    private ObservableValue<T> getCellDataReflectively(S rowData) {
      if (property == null || rowData == null) {
        return null;
      }
      T value = property.get(rowData);
      if (value == null) {
        return null;
      }
      return new ReadOnlyObjectWrapper<T>(value);
    }
  }

  // allow simpler way to define a callback
  static interface ReadOnlyCallback<S, T> extends Callback<CellDataFeatures<S, T>, ObservableValue<T>> {

    public static <S, T> Callback<CellDataFeatures<S, T>, ObservableValue<T>> of(ReadOnlyCallback<S, T> underlying) {
      return underlying;
    }

    @Override
    public default ObservableValue<T> call(CellDataFeatures<S, T> param) {
      return new ReadOnlyObjectWrapper<T>(callValue(param.getValue()));
    }

    public abstract T callValue(S value);
  }

}
