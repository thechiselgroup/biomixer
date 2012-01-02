/*************************************************************import org.thechiselgroup.choosel.dnd.client.windows.AbstractWindowContent;

import com.google.gwt.user.client.ui.Widget;
); 
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
package org.thechiselgroup.biomixer.client.dnd.windows;

import org.thechiselgroup.choosel.core.client.persistence.Memento;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.choosel.core.client.util.Disposable;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.user.client.ui.Widget;

public class ViewWindowContent implements WindowContent, Persistable,
        Disposable {

    private View view;

    public ViewWindowContent(View view) {
        this.view = view;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void dispose() {
        view.dispose();
    }

    @Override
    public String getContentType() {
        return view.getContentType();
    }

    @Override
    public String getLabel() {
        return view.getLabel();
    }

    public View getView() {
        return view;
    }

    @Override
    public void init() {
        view.init();
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        view.restore(state, restorationService, accessor);
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        return view.save(resourceSetCollector);
    }

    @Override
    public void setLabel(String label) {
        view.setLabel(label);
    }

}
