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

package org.cirqwizard.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.cirqwizard.fx.contour.ContourMilling;
import org.cirqwizard.fx.contour.InsertContourMill;
import org.cirqwizard.fx.dispensing.*;
import org.cirqwizard.fx.drilling.DrillingGroup;
import org.cirqwizard.fx.misc.About;
import org.cirqwizard.fx.misc.Firmware;
import org.cirqwizard.fx.misc.ManualDataInput;
import org.cirqwizard.fx.panel.PanelController;
import org.cirqwizard.fx.pp.InsertPPHead;
import org.cirqwizard.fx.pp.MicroscopeController;
import org.cirqwizard.fx.pp.PPGroup;
import org.cirqwizard.fx.pp.PlacingOverview;
import org.cirqwizard.fx.rubout.BottomRubout;
import org.cirqwizard.fx.rubout.TopRubout;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.fx.traces.InsertTool;
import org.cirqwizard.fx.traces.ZOffset;
import org.cirqwizard.fx.traces.bottom.BottomTraceMilling;
import org.cirqwizard.fx.traces.top.PCBPlacement;
import org.cirqwizard.fx.traces.top.TopTraceMilling;
import org.cirqwizard.layers.Board;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.*;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;


public class MainApplication extends Application
{
    private Stage primaryStage;
    private Scene scene;
    private Scene wizardScene;

    private Context context = new Context();
    private SerialInterface serialInterface;
    private CNCController cncController;
    private MicroscopeController microscopeController = new MicroscopeController();

    private MainViewController mainView = (MainViewController) new MainViewController().setMainApplication(this);

    private ScreenController topTracesGroup = new OperationsScreenGroup("Isolation milling - top")
        {
            @Override
            protected boolean isEnabled()
            {
                return super.isEnabled() && getMainApplication().getContext().getPanel().getBoards().stream().
                        map(b -> b.getBoard().getLayer(Board.LayerType.TOP)).
                        anyMatch(l -> l != null);
            }
        }.setMainApplication(this).
        addChild(new PCBPlacement().setMainApplication(this)).
        addChild(new OperationsScreenGroup("Isolation milling").setMainApplication(this).
                addChild(new InsertTool().setMainApplication(this)).
                addChild(new ZOffset().setMainApplication(this)).
                addChild(new TopTraceMilling().setMainApplication(this))).
        addChild(new OperationsScreenGroup("Rub-out")
        {
            @Override
            protected boolean isMandatory()
            {
                return !SettingsFactory.getRubOutSettings().getSkipRubOut().getValue();
            }
        }.setMainApplication(this).
                        addChild(new org.cirqwizard.fx.rubout.InsertTool().setMainApplication(this)).
                        addChild(new TopRubout().setMainApplication(this)));

    private ScreenController bottomTracesGroup = new OperationsScreenGroup("Isolation milling - bottom")
        {
            @Override
            protected boolean isEnabled()
            {
                return super.isEnabled() && getMainApplication().getContext().getPanel().getBoards().stream().
                        map(b -> b.getBoard().getLayer(Board.LayerType.BOTTOM)).
                        anyMatch(l -> l != null);
            }
        }.setMainApplication(this).
            addChild(new org.cirqwizard.fx.traces.bottom.PCBPlacement().setMainApplication(this)).
            addChild(new OperationsScreenGroup("Isolation milling").setMainApplication(this).
                            addChild(new InsertTool().setMainApplication(this)).
                            addChild(new ZOffset().setMainApplication(this)).
                            addChild(new BottomTraceMilling().setMainApplication(this))
            ).
            addChild(new OperationsScreenGroup("Rub-out")
                    {
                        @Override
                        protected boolean isMandatory()
                        {
                            return !SettingsFactory.getRubOutSettings().getSkipRubOut().getValue();
                        }
                    }.setMainApplication(this).
                            addChild(new org.cirqwizard.fx.rubout.InsertTool().setMainApplication(this)).
                            addChild(new BottomRubout().setMainApplication(this)));

    private ScreenController contourMillingGroup = new OperationsScreenGroup("Contour milling")
        {
            @Override
            protected boolean isEnabled()
            {
                return super.isEnabled() && getMainApplication().getContext().getPanel().getBoards().stream().
                        map(b -> b.getBoard().getLayer(Board.LayerType.MILLING)).
                        anyMatch(Objects::nonNull);
            }
        }.setMainApplication(this).
            addChild(new org.cirqwizard.fx.drilling.PCBPlacement().setMainApplication(this)).
            addChild(new InsertContourMill().setMainApplication(this)).
            addChild(new ContourMilling().setMainApplication(this));

    private ScreenController topDispensingGroup = new OperationsScreenGroup("Dispensing - top")
        {
            @Override
            protected boolean isEnabled()
            {
                return super.isEnabled() && getMainApplication().getContext().getPanel().getBoards().stream().
                        map(b -> b.getBoard().getLayer(Board.LayerType.SOLDER_PASTE_TOP)).
                        anyMatch(Objects::nonNull);
            }
        }.setMainApplication(this).
            addChild(new PCBPlacement().setMainApplication(this)).
            addChild(new InsertSyringe().setMainApplication(this)).
            addChild(new SyringeBleeding().setMainApplication(this)).
            addChild(new TopDispensing().setMainApplication(this));

    private ScreenController bottomDispensingGroup = new OperationsScreenGroup("Dispensing - bottom")
        {
            @Override
            protected boolean isEnabled()
            {
                return super.isEnabled() && getMainApplication().getContext().getPanel().getBoards().stream().
                        map(b -> b.getBoard().getLayer(Board.LayerType.SOLDER_PASTE_BOTTOM)).
                        anyMatch(Objects::nonNull);
            }
        }.setMainApplication(this).
            addChild(new org.cirqwizard.fx.traces.bottom.PCBPlacement().setMainApplication(this)).
            addChild(new InsertSyringe().setMainApplication(this)).
            addChild(new SyringeBleeding().setMainApplication(this)).
            addChild(new BottomDispensing().setMainApplication(this));

    private PPGroup topPpGroup = (PPGroup)(new PPGroup(Board.LayerType.PLACEMENT_TOP,"Pick and place - top").setMainApplication(this).
            addChild(new PCBPlacement().setMainApplication(this)).
            addChild(new InsertPPHead().setMainApplication(this)).
            addChild(new PlacingOverview(Board.LayerType.PLACEMENT_TOP).setMainApplication(this)));

    private PPGroup bottomPpGroup = (PPGroup)(new PPGroup(Board.LayerType.PLACEMENT_BOTTOM, "Pick and place - bottom").setMainApplication(this).
            addChild(new org.cirqwizard.fx.traces.bottom.PCBPlacement().setMainApplication(this)).
            addChild(new InsertPPHead().setMainApplication(this)).
            addChild(new PlacingOverview(Board.LayerType.PLACEMENT_BOTTOM).setMainApplication(this)));

    private ScreenController root = new Welcome().setMainApplication(this).
            addChild(new PanelController().setMainApplication(this)).
            addChild(new Homing().setMainApplication(this)).
            addChild(topTracesGroup).
            addChild(bottomTracesGroup).
            addChild(new DrillingGroup("Drilling").setMainApplication(this).
                    addChild(new org.cirqwizard.fx.drilling.PCBPlacement().setMainApplication(this))).
            addChild(contourMillingGroup).
            addChild(topDispensingGroup).
            addChild(bottomDispensingGroup).
            addChild(topPpGroup).
            addChild(bottomPpGroup).
            addChild(new Terminal().setMainApplication(this)).
            addChild(new ScreenGroup("Misc").setVisible(false).setMainApplication(this).
                    addChild(new SettingsEditor().setMainApplication(this)).
                    addChild(new Firmware().setMainApplication(this)).
                    addChild(new About()).setMainApplication(this).
                    addChild(new ManualDataInput().setMainApplication(this)));

    @Override
    public void start(Stage primaryStage)
    {
        new Settings(Preferences.userRoot().node("org.cirqwizard")).export();
        LoggerFactory.getApplicationLogger().setLevel(SettingsFactory.getApplicationSettings().getLogLevel().getValue());
        connectSerialPort(SettingsFactory.getApplicationSettings().getSerialPort().getValue());

        this.primaryStage = primaryStage;

        if (isFirstLaunch())
            showFirstLaunchWizard();
        else
            showMainApplication();
    }

    public void showMainApplication()
    {
        // recreate scene again. fix for weird bug in mac os when links turn into buttons
        if (scene != null)
            primaryStage.close();

        mainView = (MainViewController) new MainViewController().setMainApplication(this);
        scene = new Scene(mainView.getView(), 800, 600);
        scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard.css");
        if (System.getProperty("os.name").startsWith("Linux"))
            scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard-linux.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("cirQWizard");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/application.png")));
        mainView.setScreen(root);
        primaryStage.show();
    }

    public void showFirstLaunchWizard()
    {
        if (scene != null)
            primaryStage.close();

        ScreenController firstRunWizard = new FirstRunWizard().setMainApplication(this);
        wizardScene = new Scene(firstRunWizard.getView(), 800, 600);
        wizardScene.getStylesheets().add("org/cirqwizard/fx/cirqwizard.css");
        if(System.getProperty("os.name").startsWith("Linux"))
            wizardScene.getStylesheets().add("org/cirqwizard/fx/cirqwizard-linux.css");
        primaryStage.setScene(wizardScene);
        primaryStage.setTitle("Cirqoid Wizard");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/application.png")));
        primaryStage.show();
    }

    public ScreenController getScreen(Class clazz)
    {
        return getScreen(root, clazz);
    }

    private ScreenController getScreen(ScreenController root, Class clazz)
    {
        if (clazz.equals(root.getClass()))
            return root;
        if (root.getChildren() != null)
        {
            for (ScreenController ctrl : root.getChildren())
            {
                ScreenController c = getScreen(ctrl, clazz);
                if (c != null)
                    return c;
            }
        }
        return null;
    }

    public void resetContext()
    {
        topPpGroup.resetDynamicChildren();
        context = new Context();
    }

    public ScreenController getCurrentScreen()
    {
        return mainView.getCurrentScreen();
    }

    public void setCurrentScreen(ScreenController screen)
    {
        mainView.setScreen(screen);
    }

    public MainViewController getMainView()
    {
        return mainView;
    }

    public void connectSerialPort(String port)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                mainView.disableManualControl();
                try
                {
                    if (serialInterface != null)
                        serialInterface.close();
                    if (port != null && port.length() > 0)
                        serialInterface = new SerialInterfaceImpl(port, 38400);
                    else
                        serialInterface = SerialInterfaceFactory.autodetect();
                }
                catch (SerialException e)
                {
                    LoggerFactory.logException("Can't connect to selected serial port - " + port, e);
                    try
                    {
                        serialInterface = SerialInterfaceFactory.autodetect();
                    }
                    catch (SerialException e1)
                    {
                        LoggerFactory.logException("Can't connect to any serial port", e);
                        serialInterface = null;
                    }
                }

                if (serialInterface == null)
                    cncController = null;
                else
                {
                    Platform.runLater(mainView::enableManualControl);
                    cncController = new CNCController(serialInterface, MainApplication.this);
                }
            }
        }.start();
    }

    private boolean isFirstLaunch()
    {
        Integer currentYAxisDifference = SettingsFactory.getMachineSettings().getYAxisDifference().getValue();
        return currentYAxisDifference == null;
    }

    @Override
    public void stop() throws Exception
    {
        if (serialInterface != null)
            serialInterface.close();
        super.stop();
    }

    public Context getContext()
    {
        return context;
    }

    public SerialInterface getSerialInterface()
    {
        return serialInterface;
    }

    public CNCController getCNCController()
    {
        return cncController;
    }

    public MicroscopeController getMicroscopeController()
    {
        return microscopeController;
    }

    public List<ScreenController> getSiblings(ScreenController scene)
    {
        return scene.getParent() == null ? null : scene.getParent().getChildren();
    }

    public Stage getPrimaryStage()
    {
        return primaryStage;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
