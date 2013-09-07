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

package org.cirqwizard.serial;

import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;


public class SerialInterfaceStub implements SerialInterface
{
    @Override
    public void setBootloaderMode(boolean bootloader) throws SerialException
    {
    }

    @Override
    public void write(int b) throws IOException
    {
    }

    @Override
    public int readByte() throws IOException
    {
        return -1;
    }

    @Override
    public void close() throws SerialException
    {
    }

    @Override
    public void send(String str, long timeout) throws SerialException
    {
        LoggerFactory.getSerialLogger().fine(str);
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            // Interrupted. That's fine
        }
    }

    @Override
    public String sendAndReadResponse(String req, long timeout) throws SerialException, ExecutionException
    {
        return "";
    }

    @Override
    public String getPortName()
    {
        return "";
    }
}
