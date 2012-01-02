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
package org.thechiselgroup.biomixer.client.workbench;

import org.thechiselgroup.choosel.dnd.client.windows.Branding;

public class DefaultBranding implements Branding {

    private static final String DISCLAIMER = "THIS SOFTWARE IS PROVIDED \"AS IS\" AND ANY EXPRESSED OR "
            + "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF "
            + "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO"
            + "EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, "
            + "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT "
            + "LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR"
            + " PROFITS; OR BUSINESS INTERRUPTION) "
            + "HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, "
            + "STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING "
            + "IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY "
            + "OF SUCH DAMAGE.";

    private static final String MESSAGE = "<p><i>\"To understand is hard. Once one understands, "
            + "action is easy.\"</i></p><br/>"
            + "<p>(C) Copyright 2009, 2010 "
            + "The CHISEL Group, University of Victoria<br/>"
            + "(<a href=\"http://www.thechiselgroup.org/\" "
            + "target=\"_blank\">www.thechiselgroup.org</a>)<p><br/>"
            + "<p>A demo of Choosel is available at:<br/><a href=\"http://choosel-mashups.appspot.com\" "
            + "target=\"_blank\">choosel-mashups.appspot.com</a></p><br/>"
            + "<p>If you have any questions, please visit our user forum:<br/><a href=\"http://groups.google.com/group/choosel/topics\" "
            + "target=\"_blank\">groups.google.com/group/choosel</a></p><br/>"
            + "<p>For more information about the Choosel framework, visit<br/><a href=\"http://code.google.com/p/choosel/\" "
            + "target=\"_blank\">code.google.com/p/choosel/</a></p><br/>"
            + "<p>For updates on Choosel, you can also follow us on "
            + "<br/><a target=\"_blank\" href=\"http://twitter.com/chooselmashups\">Twitter (@chooselmashups)</a>"
            + " or <a target=\"_blank\" href=\"http://www.facebook.com/pages/Choosel-Data-Visualization/170642902949853\">Facebook</a>"
            + "</p><br/>"
            + "<p><b>Disclaimer</b></p>"
            + "<p>"
            + DISCLAIMER
            + "</p>"
            + "<br/><br/><b>We appreciate your ideas and comments:</b>";

    @Override
    public String getAboutDialogContentHTML() {
        return MESSAGE;
    }

    @Override
    public String getApplicationTitle() {
        return "Choosel";
    }

    @Override
    public String getCopyright() {
        return "(C) 2010 The CHISEL Group (www.thechiselgroup.org)";
    }

    @Override
    public String getMinorApplicationTitle() {
        return "demo";
    }

}