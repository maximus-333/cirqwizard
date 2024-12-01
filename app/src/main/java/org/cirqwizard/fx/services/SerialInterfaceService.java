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

package org.cirqwizard.fx.services;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.fx.util.ExceptionAlert;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.ExecutionException;
import org.cirqwizard.serial.SerialException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class SerialInterfaceService extends Service
{
    private MainApplication mainApplication;
    private List<String> programLines;
    private Property<String> executionTime = new SimpleStringProperty("");
    private Property<String> responses = new SimpleStringProperty("");
    private boolean readResponses;
    private boolean suppressExceptions;

    public SerialInterfaceService(MainApplication mainApplication)
    {
        this.mainApplication = mainApplication;
    }

    public void setProgram(String program)
    {
        setProgram(program, false, false);
    }

    public void setProgram(String program, boolean readResponses, boolean suppressExceptions)
    {
        this.readResponses = readResponses;
        this.suppressExceptions = suppressExceptions;
        programLines = new ArrayList<>();
        LineNumberReader reader = new LineNumberReader(new StringReader(program));
        String str;
        try
        {
            while ((str = reader.readLine()) != null)
                programLines.add(str);
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error reading a program from StringReader", e);
        }
    }

    public Property<String> executionTimeProperty()
    {
        return executionTime;
    }

    public Property<String> responsesProperty()
    {
        return responses;
    }

    @Override
    protected Task createTask()
    {
        return new SerialInterfaceTask();
    }

    private String formatTime(long time)
    {
        return String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60);
    }

    public class SerialInterfaceTask extends Task
    {
        @Override
        protected Object call() throws Exception
        {
            try
            {
                final StringBuilder responseBuilder = new StringBuilder();
                if (readResponses)
                    Platform.runLater(() -> responses.setValue(""));

                long executionStartTime = System.currentTimeMillis();
                for (int i = 0; i < programLines.size(); i++)
                {
                    if (isCancelled())
                        throw new InterruptedException();

                    try
                    {
                        mainApplication.getSerialInterface().send(programLines.get(i), 20000000, responseBuilder, false);
                    }
                    catch (SerialException | ExecutionException e)
                    {
                        if (!suppressExceptions)
                            throw e;
                    }
                    if (readResponses)
                        Platform.runLater(() -> responses.setValue(responseBuilder.toString()));
                    updateProgress(i, programLines.size());
                    final String s = formatTime((System.currentTimeMillis() - executionStartTime) / 1000);
                    Platform.runLater(() -> executionTime.setValue(s));
                }
            }
            catch (SerialException | ExecutionException e)
            {
                LoggerFactory.logException("Error communicating with the controller", e);
                mainApplication.getCNCController().interruptProgram();
                ExceptionAlert alert = new ExceptionAlert("Oops! That's embarrassing!", "Communication error",
                        "Something went wrong while communicating with the controller. " +
                                "The most sensible thing to do now would be to close the program and start over again. Sorry about that.", e);
                alert.showAndWait();
            }
            catch (InterruptedException e)
            {
                mainApplication.getCNCController().interruptProgram();
            }
            return null;
        }
    }
}
