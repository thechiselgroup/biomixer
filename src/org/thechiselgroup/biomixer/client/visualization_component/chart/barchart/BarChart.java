/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.visualization_component.chart.barchart;

import java.util.Comparator;

import org.thechiselgroup.biomixer.client.visualization_component.chart.ChartViewContentDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.DecimalFormattedSlotResolver;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.TickFormatFunction;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemColorSlotAccessor;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemPredicateJsBooleanFunction;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemStringSlotAccessor;
import org.thechiselgroup.choosel.core.client.ui.Colors;
import org.thechiselgroup.choosel.core.client.ui.SidePanelSection;
import org.thechiselgroup.choosel.core.client.ui.TextBoundsEstimator;
import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.util.collections.Delta;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.ViewContentDisplayProperty;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Status;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Subset;
import org.thechiselgroup.choosel.core.client.visualization.model.comparators.VisualItemDoubleComparator;
import org.thechiselgroup.choosel.core.client.visualization.model.predicates.GreaterThanSlotValuePredicate;
import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVAlignment;
import org.thechiselgroup.choosel.protovis.client.PVBar;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVLabel;
import org.thechiselgroup.choosel.protovis.client.PVLinearScale;
import org.thechiselgroup.choosel.protovis.client.PVMark;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsBooleanFunction;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsDoubleFunction;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsStringFunction;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/* TODO refactor such that the differences between vertical and horizontal bar chart
 * are extracted and the commonalities are kept.
 */
// TODO right side ticks
// TODO leverage scales
public class BarChart extends ChartViewContentDisplay {

    private class BarSpacingProperty implements
            ViewContentDisplayProperty<Boolean> {

        @Override
        public String getPropertyName() {
            return BAR_SPACING_PROPERTY;
        }

        @Override
        public Boolean getValue() {
            return getBarSpacing();
        }

        @Override
        public void setValue(Boolean value) {
            setBarSpacing(value);
        }
    }

    private class LayoutProperty implements
            ViewContentDisplayProperty<LayoutType> {

        @Override
        public String getPropertyName() {
            return LAYOUT_PROPERTY;
        }

        @Override
        public LayoutType getValue() {
            return getLayout();
        }

        @Override
        public void setValue(LayoutType value) {
            setLayout(value);
        }
    }

    public static enum LayoutType {

        VERTICAL("Vertical"), HORIZONTAL("Horizontal"), AUTOMATIC("Automatic");

        private String name;

        LayoutType(String name) {
            this.name = name;
        }

        /**
         * @return space that is available for the bar height.
         */
        private double getBarLengthSpace(int chartHeight, int chartWidth) {
            return isVerticalBarChart(chartHeight, chartWidth) ? chartHeight
                    : chartWidth;
        }

        /**
         * @return space that is available for the bar width.
         */
        private double getBarWidthSpace(int chartHeight, int chartWidth) {
            return isVerticalBarChart(chartHeight, chartWidth) ? chartWidth
                    : chartHeight;
        }

        public String getName() {
            return name;
        }

        private boolean isVerticalBarChart(int chartHeight, int chartWidth) {
            return this == LayoutType.VERTICAL
                    || (this == LayoutType.AUTOMATIC && chartHeight < chartWidth);
        }

    }

    private class ThinPartialBarsProperty implements
            ViewContentDisplayProperty<Boolean> {

        @Override
        public String getPropertyName() {
            return THIN_PARTIAL_BARS_PROPERTY;
        }

        @Override
        public Boolean getValue() {
            return getThinPartialBars();
        }

        @Override
        public void setValue(Boolean value) {
            setThinPartialBars(value);
        }
    }

    private class ValueLabelVisibilityProperty implements
            ViewContentDisplayProperty<Boolean> {

        @Override
        public String getPropertyName() {
            return VALUE_LABEL_VISIBILITY_PROPERTY;
        }

        @Override
        public Boolean getValue() {
            return getValueLabelVisibility();
        }

        @Override
        public void setValue(Boolean value) {
            setValueLabelVisibility(value);
        }
    }

    // TODO move to protovis (events)
    public static final String ALL = "all";

    // TODO move to protovis (cursor)
    public static final String POINTER = "pointer";

    public final static String ID = "org.thechiselgroup.choosel.visualization_component.chart.BarChart";

    public static final Slot BAR_LABEL = new Slot("barLabel", "Label",
            DataType.TEXT);

    public static final Slot BAR_LENGTH = new Slot("barLength", "Bar Length",
            DataType.NUMBER);

    public static final Slot BAR_COLOR = new Slot("barColor", "Color",
            DataType.COLOR);

    public static final Slot BAR_BORDER_COLOR = new Slot("barBorderColor",
            "Border Color", DataType.COLOR);

    public static final Slot PARTIAL_BAR_LENGTH = new Slot("partialBarLength",
            "Partial Bar Length", DataType.NUMBER);

    public static final Slot PARTIAL_BAR_COLOR = new Slot("partialBarColor",
            "Partial Bar Color", DataType.COLOR);

    public static final Slot PARTIAL_BAR_BORDER_COLOR = new Slot(
            "partialBarBorderColor", "Partial Bar Border Color", DataType.COLOR);

    public static final Slot[] SLOTS = new Slot[] { BAR_LABEL, BAR_LENGTH,
            BAR_COLOR, BAR_BORDER_COLOR, PARTIAL_BAR_LENGTH, PARTIAL_BAR_COLOR,
            PARTIAL_BAR_BORDER_COLOR };

    public static final String LAYOUT_PROPERTY = "layout";

    public static final String BAR_SPACING_PROPERTY = "barSpacing";

    public static final String VALUE_LABEL_VISIBILITY_PROPERTY = "valueLabelVisibility";

    public static final String THIN_PARTIAL_BARS_PROPERTY = "thinPartialBars";

    private static final int BORDER_BOTTOM = 35;

    private static final int BORDER_LEFT = 5;

    private static final int BORDER_TOP = 5;

    private static final int BORDER_RIGHT = 5;

    private static final String GRIDLINE_SCALE_COLOR = "rgba(255,255,255,.3)";

    private static final String AXIS_SCALE_COLOR = Colors.GRAY_1;

    private static final double BAR_STROKE_WIDTH = 0.5d;

    private static final String FONT_WEIGHT = "normal";

    private static final String FONT_SIZE = "10px";

    private static final String FONT_STYLE = "normal";

    private static final String FONT_FAMILY = "sans-serif";

    private static final String FONT = FONT_SIZE + " " + FONT_FAMILY;

    private double[] regularValues;

    private boolean valueLabelVisibility = true;

    // TODO semantic meaning (bar length etc) --> makes different settings
    // easier
    protected int chartHeight;

    protected int chartWidth;

    protected JsStringFunction partialLabelText = new DecimalFormattedSlotResolver(
            PARTIAL_BAR_LENGTH, 2);

    private JsStringFunction fullMarkLabelText = new DecimalFormattedSlotResolver(
            BAR_LENGTH, 2);

    private JsBooleanFunction showPartialBars = new VisualItemPredicateJsBooleanFunction(
            new GreaterThanSlotValuePredicate(PARTIAL_BAR_LENGTH, 0));

    private JsDoubleFunction barStart = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return calculateBarStart(args.<PVMark> getThis().index());
        }
    };

    private JsDoubleFunction partialBarStart = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            PVMark _this = args.getThis();
            double regularBarStart = calculateBarStart(_this.index());
            return (partialBarThinner) ? regularBarStart + calculateBarWidth()
                    * 0.33 : regularBarStart;
        }
    };

    /**
     * Calculates the length of the highlighted bar.
     */
    private JsDoubleFunction partialBarLength = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return calculatePartialBarLength(args.<VisualItem> getObject())
                    - BAR_STROKE_WIDTH; // subtract initial offset
        }
    };

    private JsDoubleFunction regularBarLength = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            PVMark _this = args.getThis();
            return calculateBarLength(regularValues[_this.index()])
                    - BAR_STROKE_WIDTH; // subtract initial offset
        }
    };

    private JsDoubleFunction barWidth = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return calculateBarWidth();
        }

    };

    private JsDoubleFunction partialBarWidth = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return partialBarThinner ? 0.33 * calculateBarWidth()
                    : calculateBarWidth();
        }
    };

    private JsStringFunction scaleStrokeStyle = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            double d = args.getDouble();
            return d == 0 ? AXIS_SCALE_COLOR : GRIDLINE_SCALE_COLOR;
        }
    };

    private PVBar regularBar;

    private PVBar partialBar;

    private JsDoubleFunction baselineLabelStart = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            PVMark _this = args.getThis();
            return calculateBarStart(_this.index()) + calculateBarWidth() / 2;
        }
    };

    private final static String BAR_TEXT_BASELINE = PVAlignment.TOP;

    private LayoutType layout = LayoutType.HORIZONTAL;

    protected JsStringFunction barValueLabelTextStyle = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            VisualItem visualItem = args.<VisualItem> getObject();
            PVMark _this = args.getThis();

            if (Status.FULL.equals(visualItem.getStatus(Subset.HIGHLIGHTED))) {
                return Colors.BLACK;
            }

            // XXX calculate label size instead of using 60px
            if (calculateBarLength(regularValues[_this.index()]) < 60) {
                return Colors.GRAY_2;
            }

            return Colors.WHITE;
        }
    };

    private String valueAxisLabel;

    private JsStringFunction valueAxisLabelFunction = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            return valueAxisLabel;
        }
    };

    private JsStringFunction valueLabelAlignment = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            // XXX pre-calculation should be done by methods in
            // chart..
            // XXX calculate label size instead of taking 60px
            PVMark _this = args.getThis();
            if (calculateBarLength(regularValues[_this.index()]) < 60) {
                return PVAlignment.LEFT;
            }
            return PVAlignment.RIGHT;
        }
    };

    protected JsStringFunction regularMarkLabelText = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            VisualItem visualItem = args.getObject();
            // TODO separate visibility determination
            return visualItem.getValueAsDouble(BAR_LENGTH)
                    - visualItem.getValueAsDouble(PARTIAL_BAR_LENGTH) < 1 ? null
                    : Double.toString(visualItem.getValueAsDouble(BAR_LENGTH)
                            - visualItem.getValueAsDouble(PARTIAL_BAR_LENGTH));
        }
    };

    protected JsStringFunction partialBarValueLabelText = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            VisualItem visualItem = args.getObject();
            // TODO separate visibility determination
            return visualItem.getValueAsDouble(PARTIAL_BAR_LENGTH) <= 0 ? null
                    : Double.toString(visualItem
                            .getValueAsDouble(PARTIAL_BAR_LENGTH));
        }
    };

    protected double maxChartItemValue;

    private boolean barSpacing = true;

    private Comparator<VisualItem> visualItemComparator = new VisualItemDoubleComparator(
            BAR_LENGTH);

    private PVLabel barLabel;

    private boolean partialBarThinner = false;

    /**
     * We use an invisible interaction bar to capture mouse events over the
     * bars. Without this bar, the grid lines and the partial bars lead to
     * flickering and inconsistencies due to automatically fired events.
     */
    private PVPanel invisibleInteractionBar;

    public BarChart() {
        registerProperty(new LayoutProperty());
        registerProperty(new BarSpacingProperty());
        registerProperty(new ValueLabelVisibilityProperty());
        registerProperty(new ThinPartialBarsProperty());
    }

    @Override
    protected void beforeRender() {
        super.beforeRender();

        visualItemsJsArray.sortStable(getVisualItemComparator());

        calculateMaximumChartItemValue();

        if (visualItemsJsArray.length() == 0) {
            return;
        }

        regularValues = new double[visualItemsJsArray.length()];
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            regularValues[i] = visualItemsJsArray.get(i).getValueAsDouble(
                    BAR_LENGTH);
        }
    }

    @Override
    public void buildChart() {
        assert visualItemsJsArray.length() >= 1;

        // TODO do we need sorting?
        // Collections.sort(chartItems, new ChartItemComparator(
        // SlotResolver.CHART_LABEL_SLOT));

        calculateChartVariables();
        calculateMaximumChartItemValue();

        if (layout.isVerticalBarChart(chartHeight, chartWidth)) {
            getChart().left(BORDER_LEFT + 40).bottom(BORDER_BOTTOM);
            // TODO axis label
            drawVerticalBarChart();
            drawVerticalBarScales();
        } else {
            drawHorizontalBarChart();

        }
        getChart().add(PV.Rule).bottom(0).left(0).width(chartWidth)
                .strokeStyle(AXIS_SCALE_COLOR).lineWidth(BAR_STROKE_WIDTH);
        getChart().add(PV.Rule).left(0).bottom(0).height(chartHeight)
                .strokeStyle(AXIS_SCALE_COLOR).lineWidth(BAR_STROKE_WIDTH);
    }

    private double calculateBarLength(double value) {
        return (value * getBarLengthSpace() / maxChartItemValue);
    }

    private double calculateBarStart(int index) {
        double barAreaStart = index * getBarWidthSpace()
                / visualItemsJsArray.length();
        double barOffset = barSpacing ? calculateBarWidth() / 2 : 0;
        return barAreaStart + barOffset;
    }

    private double calculateBarWidth() {
        double spacePerBar = getBarWidthSpace() / visualItemsJsArray.length();

        if (barSpacing) {
            spacePerBar /= 2;
        }

        return spacePerBar;
    }

    private void calculateChartVariables() {
        if (layout.isVerticalBarChart(chartHeight, chartWidth)) {
            chartWidth = width - BORDER_LEFT - 40 - BORDER_RIGHT;
        } else {
            chartWidth = width - BORDER_LEFT - BORDER_RIGHT
                    - calculateHorizontalLabelSpace();
        }

        chartHeight = height - BORDER_BOTTOM - BORDER_TOP;
    }

    private int calculateHorizontalLabelSpace() {
        TextBoundsEstimator estimator = new TextBoundsEstimator();
        estimator.applyFontSettings(FONT_FAMILY, FONT_STYLE, FONT_WEIGHT,
                FONT_SIZE);

        // max over widths for labels
        int maxWidth = 0;
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            String label = visualItemsJsArray.get(i).getValue(BAR_LABEL);
            estimator.setText(label);
            int width = estimator.getWidth();

            if (maxWidth < width) {
                maxWidth = width;
            }
        }

        return maxWidth;
    }

    protected void calculateMaximumChartItemValue() {
        maxChartItemValue = 0;
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            double currentItemValue = visualItemsJsArray.get(i).getValueAsDouble(
                    BAR_LENGTH);
            if (maxChartItemValue < currentItemValue) {
                maxChartItemValue = currentItemValue;
            }
        }
    }

    private double calculatePartialBarLength(VisualItem d) {
        return calculateBarLength(d.getValueAsDouble(PARTIAL_BAR_LENGTH));
    }

    private void drawHorizontalBarChart() {
        getChart().left(BORDER_LEFT + calculateHorizontalLabelSpace()).bottom(
                BORDER_BOTTOM);

        drawHorizontalMeasurementAxis();

        /*
         * The stroke gets added to the length, but is part of the visible
         * appearance. We thus have to adjust the bar length and position for
         * the stroke width.
         * 
         * The regular bar starts after the partial bar. Otherwise there are
         * color differences if the partial bar is semi-transparent.
         */
        regularBar = getChart().add(PV.Bar).data(visualItemsJsArray)
                .left(BAR_STROKE_WIDTH).width(regularBarLength)
                .bottom(barStart).height(barWidth)
                .fillStyle(new VisualItemColorSlotAccessor(BAR_COLOR))
                .strokeStyle(new VisualItemColorSlotAccessor(BAR_BORDER_COLOR))
                .lineWidth(BAR_STROKE_WIDTH);

        if (valueLabelVisibility) {
            regularBar.anchor(PVAlignment.RIGHT).add(PV.Label)
                    .textBaseline(PVAlignment.MIDDLE).text(fullMarkLabelText)
                    .textStyle(barValueLabelTextStyle)
                    .textAlign(valueLabelAlignment);
        }

        // TODO negative bars (in opposite direction)
        /*
         * Partial bars have a white bar below them to prevent the regular bar
         * from affecting a semi-transparent partial bar.
         */
        getChart().add(PV.Bar).data(visualItemsJsArray).left(BAR_STROKE_WIDTH)
                .width(partialBarLength).bottom(partialBarStart)
                .height(partialBarWidth).fillStyle(Colors.WHITE)
                .strokeStyle(Colors.WHITE).lineWidth(BAR_STROKE_WIDTH)
                .visible(showPartialBars);
        partialBar = getChart()
                .add(PV.Bar)
                .data(visualItemsJsArray)
                .left(BAR_STROKE_WIDTH)
                .width(partialBarLength)
                .bottom(partialBarStart)
                .height(partialBarWidth)
                .fillStyle(new VisualItemColorSlotAccessor(PARTIAL_BAR_COLOR))
                .strokeStyle(
                        new VisualItemColorSlotAccessor(PARTIAL_BAR_BORDER_COLOR))
                .lineWidth(BAR_STROKE_WIDTH).visible(showPartialBars);

        if (valueLabelVisibility) {
            partialBar.anchor(PVAlignment.RIGHT).add(PV.Label)
                    .textBaseline(BAR_TEXT_BASELINE).text(partialLabelText)
                    .textStyle(Colors.BLACK).textBaseline(PVAlignment.MIDDLE);
        }

        drawHorizontalGridLines();

        invisibleInteractionBar = getChart().add(PV.Panel)
                .data(visualItemsJsArray).left(BAR_STROKE_WIDTH)
                .width(regularBarLength).bottom(barStart).height(barWidth)
                .lineWidth(BAR_STROKE_WIDTH).cursor(POINTER).events(ALL);

        barLabel = getChart().add(PV.Label).data(visualItemsJsArray)
                .bottom(baselineLabelStart).textAlign(PVAlignment.RIGHT)
                .left(0).font(FONT)
                .text(new VisualItemStringSlotAccessor(BAR_LABEL))
                .textBaseline(PVAlignment.MIDDLE).events(ALL).cursor(POINTER);
    }

    private void drawHorizontalGridLines() {
        PVLinearScale scale = PV.Scale.linear(0, maxChartItemValue).range(0,
                chartWidth);
        getChart().add(PV.Rule).data(scale.ticks(5)).left(scale).bottom(0)
                .strokeStyle(scaleStrokeStyle).height(chartHeight)
                .anchor(PVAlignment.BOTTOM).add(PV.Label)
                .text(new TickFormatFunction(scale));
    }

    public void drawHorizontalMeasurementAxis() {
        getChart().add(PV.Label).bottom(-BORDER_BOTTOM + 5)
                .left(chartWidth / 2).text(valueAxisLabelFunction)
                .textAlign(PVAlignment.CENTER);
    }

    private void drawVerticalBarChart() {
        /*
         * The regular bar starts after the partial bar. Otherwise there are
         * color differences if the partial bar is semi-transparent.
         */
        regularBar = getChart().add(PV.Bar).data(visualItemsJsArray)
                .bottom(BAR_STROKE_WIDTH).height(regularBarLength)
                .left(barStart).width(barWidth)
                .fillStyle(new VisualItemColorSlotAccessor(BAR_COLOR))
                .strokeStyle(new VisualItemColorSlotAccessor(BAR_BORDER_COLOR))
                .lineWidth(BAR_STROKE_WIDTH);

        if (valueLabelVisibility) {
            regularBar.anchor(PVAlignment.TOP).add(PV.Label)
                    .textAngle(-Math.PI / 2).textBaseline(PVAlignment.MIDDLE)
                    .textAlign(valueLabelAlignment)
                    .textStyle(barValueLabelTextStyle).text(fullMarkLabelText);
        }

        /*
         * Partial bars have a white bar below them to prevent the regular bar
         * from affecting a semi-transparent partial bar.
         */
        getChart().add(PV.Bar).data(visualItemsJsArray).bottom(BAR_STROKE_WIDTH)
                .height(partialBarLength).left(partialBarStart)
                .width(partialBarWidth).fillStyle(Colors.WHITE)
                .strokeStyle(Colors.WHITE).lineWidth(BAR_STROKE_WIDTH)
                .visible(showPartialBars);
        partialBar = getChart()
                .add(PV.Bar)
                .data(visualItemsJsArray)
                .bottom(BAR_STROKE_WIDTH)
                .height(partialBarLength)
                .left(partialBarStart)
                .width(partialBarWidth)
                .fillStyle(new VisualItemColorSlotAccessor(PARTIAL_BAR_COLOR))
                .strokeStyle(
                        new VisualItemColorSlotAccessor(PARTIAL_BAR_BORDER_COLOR))
                .lineWidth(BAR_STROKE_WIDTH).visible(showPartialBars);

        if (valueLabelVisibility) {
            partialBar.anchor(PVAlignment.TOP).add(PV.Label)
                    .textBaseline(PVAlignment.MIDDLE)
                    .textAlign(PVAlignment.RIGHT)
                    .text(partialBarValueLabelText).textAngle(-Math.PI / 2);
        }

        invisibleInteractionBar = getChart().add(PV.Panel)
                .data(visualItemsJsArray).bottom(BAR_STROKE_WIDTH)
                .height(regularBarLength).left(barStart).width(barWidth)
                .lineWidth(BAR_STROKE_WIDTH).cursor(POINTER).events(ALL);

        getChart().add(PV.Label).data(visualItemsJsArray)
                .left(baselineLabelStart).textAlign(PVAlignment.CENTER)
                .bottom(new JsDoubleFunction() {
                    @Override
                    public double f(JsArgs args) {
                        PVMark _this = args.getThis();
                        // TODO dynamic positioning depending on label size
                        if (chartWidth / regularValues.length > 60) {
                            return -10;
                        }
                        return _this.index() % 2 == 0 ? -10 : -25;
                    }
                }).text(new VisualItemStringSlotAccessor(BAR_LABEL))
                .textBaseline(PVAlignment.MIDDLE);

    }

    // TODO extract scale ticks # as property
    protected void drawVerticalBarScales() {
        PVLinearScale scale = PV.Scale.linear(0, maxChartItemValue).range(0,
                chartHeight);
        getChart().add(PV.Rule).data(scale.ticks(5)).left(0).bottom(scale)
                .strokeStyle(scaleStrokeStyle).width(chartWidth)
                .anchor(PVAlignment.LEFT).add(PV.Label)
                .text(new TickFormatFunction(scale));
    }

    private double getBarLengthSpace() {
        return layout.getBarLengthSpace(chartHeight, chartWidth);
    }

    private boolean getBarSpacing() {
        return barSpacing;
    }

    private double getBarWidthSpace() {
        return layout.getBarWidthSpace(chartHeight, chartWidth);
    }

    public LayoutType getLayout() {
        return layout;
    }

    @Override
    public String getName() {
        return "Bar Chart";
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        FlowPanel settingsPanel = new FlowPanel();

        {
            settingsPanel.add(new Label("Chart orientation"));
            final ListBox layoutBox = new ListBox(false);
            layoutBox.setVisibleItemCount(1);
            for (LayoutType layout : LayoutType.values()) {
                layoutBox.addItem(layout.getName(), layout.toString());
            }
            layoutBox.setSelectedIndex(1);
            layoutBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    setLayout(LayoutType.valueOf(layoutBox.getValue(layoutBox
                            .getSelectedIndex())));
                }
            });
            settingsPanel.add(layoutBox);
        }
        {
            settingsPanel.add(new Label("Bar spacing"));

            CheckBox checkBox = new CheckBox();
            checkBox.setText("separate");
            checkBox.setValue(barSpacing);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    setBarSpacing(event.getValue());
                }
            });
            settingsPanel.add(checkBox);
        }
        {
            settingsPanel.add(new Label("Value labels"));

            CheckBox checkBox = new CheckBox();
            checkBox.setText("visible");
            checkBox.setValue(valueLabelVisibility);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    setValueLabelVisibility(event.getValue());
                }
            });
            settingsPanel.add(checkBox);
        }
        {
            settingsPanel.add(new Label("Partial bar width"));

            CheckBox checkBox = new CheckBox();
            checkBox.setText("thinner");
            checkBox.setValue(partialBarThinner);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    setThinPartialBars(event.getValue());
                }
            });
            settingsPanel.add(checkBox);
        }
        return new SidePanelSection[] { new SidePanelSection("Settings",
                settingsPanel), };
    }

    @Override
    public Slot[] getSlots() {
        return SLOTS;
    }

    public boolean getThinPartialBars() {
        return partialBarThinner;
    }

    public boolean getValueLabelVisibility() {
        return valueLabelVisibility;
    }

    public Comparator<VisualItem> getVisualItemComparator() {
        return visualItemComparator;
    }

    @Override
    protected void registerEventHandler(String eventType, PVEventHandler handler) {
        invisibleInteractionBar.event(eventType, handler);
        barLabel.event(eventType, handler);
    }

    public void setBarSpacing(boolean barSpacing) {
        if (this.barSpacing == barSpacing) {
            return;
        }

        this.barSpacing = barSpacing;
        updateChart(true);
    }

    public void setLayout(LayoutType layout) {
        assert layout != null;

        if (this.layout.equals(layout)) {
            return;
        }

        this.layout = layout;
        updateChart(true);
    }

    public void setThinPartialBars(boolean thinner) {
        this.partialBarThinner = thinner;
    }

    public void setValueLabelVisibility(boolean valueLabelVisibility) {
        if (this.valueLabelVisibility == valueLabelVisibility) {
            return;
        }

        this.valueLabelVisibility = valueLabelVisibility;
        updateChart(true);
    }

    public void setVisualItemComparator(Comparator<VisualItem> visualItemComparator) {
        if (this.visualItemComparator == visualItemComparator) {
            return;
        }

        this.visualItemComparator = visualItemComparator;
        updateChart(true);
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> changedSlots) {

        // TODO re-enable - might be wrong for initial configuration...
        // if (!changedSlots.isEmpty()) {
        valueAxisLabel = callback.getSlotResolverDescription(BAR_LENGTH);
        // }

        super.update(delta, changedSlots);
    }

}