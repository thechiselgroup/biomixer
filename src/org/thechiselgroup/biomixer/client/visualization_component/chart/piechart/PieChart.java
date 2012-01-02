/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.chart.piechart;

import org.thechiselgroup.biomixer.client.visualization_component.chart.ChartViewContentDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.chart.barchart.BarChart;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemColorSlotAccessor;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemPredicateJsBooleanFunction;
import org.thechiselgroup.biomixer.client.visualization_component.chart.functions.VisualItemStringSlotAccessor;
import org.thechiselgroup.choosel.core.client.ui.Colors;
import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.predicates.GreaterThanSlotValuePredicate;
import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVAlignment;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVWedge;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsDoubleFunction;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsStringFunction;

public class PieChart extends ChartViewContentDisplay {

    private static final String WEDGE_LABEL_COLOR = "#000000";

    private static final String RGBA_TRANSPARENT = "rgba(0,0,0,0)";

    public final static String ID = "org.thechiselgroup.choosel.visualization_component.chart.PieChart";

    public static final Slot VALUE = new Slot("value", "Value", DataType.NUMBER);

    public static final Slot LABEL = new Slot("label", "Label", DataType.TEXT);

    public static final Slot PARTIAL_VALUE = new Slot("partialValue",
            "Partial Value", DataType.NUMBER);

    public static final Slot COLOR = new Slot("color", "Color", DataType.COLOR);

    public static final Slot BORDER_COLOR = new Slot("borderColor",
            "Border Color", DataType.COLOR);

    public static final Slot PARTIAL_COLOR = new Slot("partialColor",
            "Partial Color", DataType.COLOR);

    public static final Slot PARTIAL_BORDER_COLOR = new Slot(
            "partialBorderColor", "Partial Border Color", DataType.COLOR);

    public static final Slot[] SLOTS = new Slot[] { LABEL, VALUE,
            PARTIAL_VALUE, COLOR, BORDER_COLOR, PARTIAL_COLOR,
            PARTIAL_BORDER_COLOR };

    private final static int WEDGE_TEXT_ANGLE = 0;

    private JsDoubleFunction partialWedgeRadius = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            VisualItem visualItem = args.getObject();

            double partialValue = visualItem.getValueAsDouble(PARTIAL_VALUE);
            double value = visualItem.getValueAsDouble(VALUE);

            // cannot divide by zero
            if (value == 0) {
                return 0;
            }

            assert 0 <= partialValue && partialValue <= value : "invalid partial value. Expected range: [0,"
                    + value + "]; got: " + partialValue;

            double partialPercentage = partialValue / value;

            assert 0 <= partialPercentage && partialPercentage <= 1 : "0 <= partialPercentage <= 1 (was: "
                    + partialPercentage + ")";

            /*
             * This was found to be a visually more accurate solution for both
             * small and large percentages compared to 'partialPercentage *
             * outerRadius' and to 'Math.sqrt(partialPercentage) * outerRadius'
             */
            return ((Math.sqrt(partialPercentage) + partialPercentage) / 2)
                    * outerRadius;
        }
    };

    /**
     * For each {@link VisualItem} index, it return the sum of the current and
     * all previous view items. This is required to calculate the start angle,
     * which we need for the partial wedges (because Protovis only automatically
     * calculates the correct start index if the sibling wedges are visible,
     * which is not the case for the partial wedges).
     */
    private double[] aggregatedValues;

    private JsDoubleFunction outerRadiusFunction = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return outerRadius;
        }
    };

    private int outerRadius;

    private PVWedge mainWedge;

    private PVWedge partialWedge;

    private JsDoubleFunction wedgeLeft = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return width / 2;
        }
    };

    private JsDoubleFunction wedgeBottom = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            return height / 2;
        }
    };

    private JsDoubleFunction wedgeAngle = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            VisualItem visualItem = args.getObject();
            return visualItem.getValueAsDouble(VALUE) * 2 * Math.PI
                    / getValueSum();
        }

    };

    private JsStringFunction regularMarkLabelText = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            VisualItem visualItem = args.getObject();
            return Double.toString(visualItem.getValueAsDouble(VALUE)
                    - visualItem.getValueAsDouble(PARTIAL_VALUE));
        }
    };

    private JsStringFunction highlightedMarkLabelText = new JsStringFunction() {
        @Override
        public String f(JsArgs args) {
            VisualItem visualItem = args.getObject();
            return Double.toString(visualItem.getValueAsDouble(PARTIAL_VALUE));
        }
    };

    /**
     * The wedge position calculation using angle() requires the sibling wedges
     * to be visible, which is not the case for partial wedges. We thus need to
     * use startAngle to specify the position.
     */
    private JsDoubleFunction startAngle = new JsDoubleFunction() {
        @Override
        public double f(JsArgs args) {
            int index = args.<PVWedge> getThis().index();
            if (index == 0) {
                /*
                 * -Math.PI / 2 rotates the wedges so the 1st one starts at 12
                 * o'clock
                 */
                return -Math.PI / 2;
            }
            return aggregatedValues[index - 1] * 2 * Math.PI / getValueSum()
                    - Math.PI / 2;
        }
    };

    private PVWedge labelWedge;

    private PVWedge invisibleInteractionWedge;

    @Override
    protected void beforeRender() {
        super.beforeRender();

        calculateAggregatedValues();
        calculateRegularWedgeOuterRadius();
    }

    @Override
    public void buildChart() {
        assert visualItemsJsArray.length() >= 1;

        mainWedge = getChart().add(PV.Wedge).data(visualItemsJsArray)
                .left(wedgeLeft).bottom(wedgeBottom).startAngle(startAngle)
                .innerRadius(partialWedgeRadius)
                .outerRadius(outerRadiusFunction).angle(wedgeAngle)
                .fillStyle(new VisualItemColorSlotAccessor(COLOR))
                .strokeStyle(new VisualItemColorSlotAccessor(BORDER_COLOR));

        mainWedge.anchor(PVAlignment.CENTER).add(PV.Label)
                .textAngle(WEDGE_TEXT_ANGLE).text(regularMarkLabelText)
                .textStyle(Colors.WHITE);

        partialWedge = mainWedge
                .add(PV.Wedge)
                .visible(
                        new VisualItemPredicateJsBooleanFunction(
                                new GreaterThanSlotValuePredicate(
                                        PARTIAL_VALUE, 0)))
                .innerRadius(0)
                .outerRadius(partialWedgeRadius)
                .fillStyle(new VisualItemColorSlotAccessor(PARTIAL_COLOR))
                .strokeStyle(
                        new VisualItemColorSlotAccessor(PARTIAL_BORDER_COLOR));

        partialWedge.anchor(PVAlignment.CENTER).add(PV.Label)
                .textAngle(WEDGE_TEXT_ANGLE).text(highlightedMarkLabelText);

        labelWedge = getChart().add(PV.Wedge).data(visualItemsJsArray)
                .left(wedgeLeft).bottom(wedgeBottom).startAngle(startAngle)
                .innerRadius(0).outerRadius(outerRadiusFunction)
                .angle(wedgeAngle).fillStyle(RGBA_TRANSPARENT)
                .strokeStyle(RGBA_TRANSPARENT);

        labelWedge.anchor("start").add(PV.Label)
                .text(new VisualItemStringSlotAccessor(LABEL))
                .textStyle(WEDGE_LABEL_COLOR);

        /*
         * XXX we use alpha 0.0001 because Protovis removes the invisible
         * interaction wedge if alpha is 0. This should be changed when using
         * something other than Protovis for the rendering. (o")9
         */
        invisibleInteractionWedge = getChart().add(PV.Wedge)
                .data(visualItemsJsArray).left(wedgeLeft).bottom(wedgeBottom)
                .startAngle(startAngle).innerRadius(0)
                .outerRadius(outerRadiusFunction).angle(wedgeAngle)
                .cursor(BarChart.POINTER).events(BarChart.ALL)
                .fillStyle(PV.rgb(255, 255, 255).alpha(0.00001))
                .strokeStyle(PV.rgb(255, 255, 255).alpha(0.00001));

    }

    private void calculateAggregatedValues() {
        aggregatedValues = new double[visualItemsJsArray.length()];
        double sum = 0;
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            sum += visualItemsJsArray.get(i).getValueAsDouble(VALUE);
            aggregatedValues[i] = sum;
        }
    }

    private void calculateRegularWedgeOuterRadius() {
        outerRadius = Math.min(height, width) / 2 - 5;
    }

    @Override
    public String getName() {
        return "Pie Chart";
    }

    @Override
    public Slot[] getSlots() {
        return SLOTS;
    }

    /**
     * @return sum of all values
     */
    private double getValueSum() {
        return aggregatedValues[aggregatedValues.length - 1];
    }

    @Override
    protected void registerEventHandler(String eventType, PVEventHandler handler) {
        invisibleInteractionWedge.event(eventType, handler);
    }

}
