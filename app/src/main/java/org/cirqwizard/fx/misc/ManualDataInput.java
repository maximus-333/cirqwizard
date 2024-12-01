/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.fx.misc;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.services.SerialInterfaceService;


public class ManualDataInput extends ScreenController
{
    @FXML private Region veil;
    @FXML private Button executeGCodeButton;
    @FXML private TextArea gCodeInputTextArea;
    @FXML private TextArea responseTextArea;

    @FXML private VBox executionPane;
    @FXML private ProgressBar executionProgressBar;
    @FXML private Label timeElapsedLabel;

    private SerialInterfaceService serialService;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/misc/ManualDataInput.fxml";
    }

    @Override
    protected String getName()
    {
        return "Direct GCode";
    }

    @Override
    public void refresh()
    {
        boolean noMachineConnected = getMainApplication().getCNCController() == null;
        executeGCodeButton.setDisable(noMachineConnected);

        serialService = new SerialInterfaceService(getMainApplication());
        executionProgressBar.progressProperty().bind(serialService.progressProperty());
        timeElapsedLabel.textProperty().bind(serialService.executionTimeProperty());
        executionPane.visibleProperty().bind(serialService.runningProperty());
        responseTextArea.textProperty().bind(serialService.responsesProperty());
        veil.visibleProperty().bind(serialService.runningProperty());
    }

    public void executeGCode()
    {
        serialService.setProgram(gCodeInputTextArea.getText(), true, true);
        serialService.restart();
    }

    public void stopExecution()
    {
        serialService.cancel();
    }
}
