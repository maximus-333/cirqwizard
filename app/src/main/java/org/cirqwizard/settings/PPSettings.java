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

package org.cirqwizard.settings;

public class PPSettings extends SettingsGroup
{
    public final static String NO_CAMERA_STRING = "No camera assistance";

    @PersistentPreference
    @PreferenceGroup(name =  "Camera assistance")
    private UserPreference<String> usbCamera = new UserPreference<>("USB camera", NO_CAMERA_STRING, "", PreferenceType.USB_CAMERA);

    @PersistentPreference
    @PreferenceGroup(name =  "Heights")
    private UserPreference<Integer> pickupHeight = new UserPreference<>("Pickup height", -14_200, "mm");

    @PersistentPreference
    @PreferenceGroup(name =  "Heights")
    private UserPreference<Integer> moveHeight = new UserPreference<>("Move height", 0, "mm");

    @PersistentPreference
    @PreferenceGroup(name =  "Heights")
    private UserPreference<Integer> pcbOffset = new UserPreference<>("PCB surface offset", -3500, "mm");

    @PersistentPreference
    @PreferenceGroup(name =  "Rotation")
    private UserPreference<Integer> rotationFeed = new UserPreference<>("Rotation feed", 100_000, "mm/min");

    @Override
    public String getName()
    {
        return "Pick & Place";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "pp";
    }

    public UserPreference<String> getUsbCamera()
    {
        return usbCamera;
    }

    public void setUsbCamera(UserPreference<String> usbCamera)
    {
        this.usbCamera = usbCamera;
    }

    public UserPreference<Integer> getPickupHeight()
    {
        return pickupHeight;
    }

    public void setPickupHeight(UserPreference<Integer> pickupHeight)
    {
        this.pickupHeight = pickupHeight;
    }

    public UserPreference<Integer> getMoveHeight()
    {
        return moveHeight;
    }

    public void setMoveHeight(UserPreference<Integer> moveHeight)
    {
        this.moveHeight = moveHeight;
    }

    public UserPreference<Integer> getPcbOffset()
    {
        return pcbOffset;
    }

    public void setPcbOffset(UserPreference<Integer> pcbOffset)
    {
        this.pcbOffset = pcbOffset;
    }

    public UserPreference<Integer> getRotationFeed()
    {
        return rotationFeed;
    }

    public void setRotationFeed(UserPreference<Integer> rotationFeed)
    {
        this.rotationFeed = rotationFeed;
    }
}
