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
package org.thechiselgroup.biomixer.client.workbench.ui;

import org.thechiselgroup.biomixer.client.dnd.windows.AbstractWindowContent;
import org.thechiselgroup.choosel.core.client.persistence.Memento;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetCollector;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class HelpWindowContent extends AbstractWindowContent implements
        Persistable {

    private static final String CSS_HELP_CONTENT = "Choosel-Help-Content";

    public HelpWindowContent() {
        super("Help", "help");
    }

    @Override
    public Widget asWidget() {
        /*
         * The help is injected as html, and not shown as external html file
         * within an inner frame, because window resizing was slow and
         * error-prone with inner frames.
         */
        String html = "";

        html += "<h1>Welcome to Choosel!</h1>";
        html += "<div>This help provides you with an overview of how to import data, how to configure views, and "
                + "how to work with selections.</div>";

        html += "<h1>Short Video Demonstration</h1>";
        html += "<div style=\"text-align: center;\"><iframe src=\"http://player.vimeo.com/video/16109235?portrait=0\" width=\"601\" height=\"338\" frameborder=\"0\"></iframe></div>";
        html += "<h1>Importing data</h1>";

        html += "<div>";
        html += "Choosel allows you to import data using comma separated value (CSV) files. To import a CSV file:";
        html += "<ol>";
        html += "<li>Open a previously created CSV file using a text editor (e.g., Windows Notepad) "
                + "See <b>Creating CSV files for use by Choosel</b> (below) for help on creating correctly "
                + "formatted CSV data</li>";
        html += "<li>Select <b>Import</b> under the <b>Data</b> menu to display the <b>Import CSV</b> dialog.</li>";
        html += "<li>Enter a name for the data set</li>";
        html += "<li>Copy the CSV file data in the text editor and paste this data into the Import CSV dialog paste area.</li>";
        html += "<li>Click Import to complete the import.</li>";
        html += "<li>A round cornered rectangle icon will be displayed under the Data menu area, which can now be used "
                + "to populate views with data.</li>";
        html += "</ol>";
        html += "</div>";

        html += "<h2>Preparing CSV for use by Choosel</h2>";

        html += "<div>";
        html += "Choosel supports four data types within comma separated value (CSV) data. These are:";
        html += "<ul>";
        html += "<li>Numeric values</li>";
        html += "<li>Dates</li>";
        html += "<li>Locations (latitude/longitude)</li>";
        html += "<li>Unformatted text data</li>";
        html += "</ul>";
        html += "</div>";

        html += "<div>";
        html += "The CSV import routine automatically recognizes these data types "
                + "based on the following rules:";
        html += "</div>";

        html += "<div>";
        html += "Numeric values must be expressed as decimal numbers, containing one "
                + "or more digits separated by a decimal point. E.g., 1.0, 4.726355.";
        html += "</div>";

        html += "<div>";
        html += "Dates must be expressed in the format <b>dd/MM/yyyy</b> where <b>dd</b> "
                + "is a two digit day form 1 to 31, <b>MM</b> a two digit month form "
                + "1 to 12 and <b>yyyy</b> a 4 digit year. These values must be separated "
                + "by forward slashes (\"/\"). E.g., 23/09/2010 defines a date for "
                + "the 23rd of September, 2010";
        html += "</div>";

        html += "<div>";
        html += "Locations must be expressed as latitude/longitude pairs using decimal "
                + "degrees as <b>lat/lon</b>, where <b>lat</b> and <b>lon</b> are both numeric "
                + "values as described above. E.g., 48.5/110.24 describes a location at 48 degrees "
                + "30 minutes 0 seconds North and 110 degrees, 14 minutes, 24 seconds West. To "
                + "express latitudes south of the equator or longitudes east of the prime meridian, "
                + "use negative numbers.";
        html += "</div>";

        html += "<div>";
        html += "To convert a latitude or longitude value expressed in degrees, "
                + "minutes, and seconds to decimal degrees, divide the minutes value by 60 and "
                + "add this to the degrees value, then divide the seconds value by 3600 "
                + "and add this value to get the total. E.g., 110 degrees, 14 minutes, and 24 seconds "
                + "becomes 110 + 14/60 + 24/3600 = 110.24";
        html += "</div>";

        html += "<div>";
        html += "Any values that do not conform to the formatting described for numeric values, "
                + "dates, or locations are considered to be unformatted text data.";
        html += "</div>";

        html += "<h1>Creating Views</h1>";

        html += "<div>";
        html += "To create a view:";
        html += "<ol>";
        html += "<li>Click on one of the options under the <b>Views</b> menu (e.g., <b>Text</b> or <b>Map</b>).</li>";
        html += "<li>The associated view window will be displayed in the work area.</li>";
        html += "<li><b>Drag</b> a previously created data set from the <b>Data menu</b> onto the view.</li>";
        html += "<li>If feasible, the data will be visualized based on the type of view.</li>";
        html += "</ol>";
        html += "</div>";

        html += "<h1>Working with Views</h1>";
        html += "<div>";
        html += "Once you have created and populated one or more views with data, you can explore these data by "
                + "mousing over data, creating <b>Selections</b> (selected sets of data elements) and by "
                + "configuring the views. Moving the mouse over a data element in any view highlights that "
                + "data element. If the element is visible in other views, it will also be highlighted in these "
                + "views. See <b>Working with Selections</b> for more detail on how to create sets of selected "
                + "data. See <b>Configuring Views</b> for information on how to customize each view.";
        html += "</div>";

        html += "<h1>Configuring Views</h1>";

        html += "<div>";
        html += "Each view contains a configuration area that you can select by clicking on the double "
                + "inverted triangle icon near the top right corner. This opens up a <b>Mappings</b> "
                + "pane that contains <b>Grouping</b> and <b>Label</b> fields as well as additional "
                + "view-specific fields as appropriate.";
        html += "</div>";

        html += "<div>";
        html += "The <b>Grouping</b> field contains column names for all text fields from the data set "
                + "used in this view. When you select one of these column names for a column in which "
                + "there are repeated values the view will redisplay with the data elements grouped "
                + "under each repeated value. For example, in a <b>Bar Chart</b> view that contains a "
                + "country list with a column for <b>Continent</b>, grouping by continent will aggregate "
                + "the individual country values and redisplay a bar for each continent. Selecting a "
                + "grouping in one window will select the individual elements in the group if displayed "
                + "in another window.";
        html += "</div>";

        html += "<div>";
        html += "The <b>Label</b> field determines what text label is associated with the data displayed. "
                + "For example, in the <b>Bar Chart</b> view of country data that contains country, "
                + "capital, and continent, the bars can be labeled with country name, continent name, "
                + "or capital name.";
        html += "</div>";

        html += "<h2>View-specific Fields</h2>";
        html += "<div>";
        html += "The <b>Bar Chart</b> view provides configurable <b>Value</b> fields for any numeric values "
                + "found in the data set. Select the numeric field to display from a dropdown list of "
                + "fields and specify how the numbers should be processed (as Sum, Count, Average, Minimum, "
                + "or Maximum). Note that not all processing options are applicable to all data.";
        html += "</div>";
        html += "<div>";
        html += "The <b>Text</b> view displays a tag cloud that can be configured by changing the numeric values "
                + "that determine font size. Similar to the <b>Value</b> fields in the Bar Chart view, "
                + "select the numeric field to display from a dropdown list of fields and specify how "
                + "the numbers should be processed (as Sum, Count, Average, Minimum, or Maximum). Note "
                + "that not all processing options are applicable to all data.";
        html += "</div>";
        html += "<h1>Working with Selections</h1>";
        html += "<div>";
        html += "Selections are subsets of data elements created by clicking on data elements within a view. "
                + "When the first element is selected, a new Selection is automatically created. Clicking on "
                + "additional elements will add these to the selection. You can rename or remove selections by "
                + "mousing over the selection. A dialog box will appear with a text field for the name of the "
                + "selection and a link that allows you to remove the selection from the current view.";
        html += "</div>";
        html += "<div>";
        html += "Once you have created a selection, you can use this selection in other views by dragging it to another view.";
        html += "</div>";

        HTML help = new HTML(html);
        help.setStyleName(CSS_HELP_CONTENT);
        return help;
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        return new Memento();
    }
}