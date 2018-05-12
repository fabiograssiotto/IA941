/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SoarBridge;

/**
 *
 * @author Fabio
 */
public class CommandAddMem
{
    private String thingName = null;
    private Float x = null;
    private Float y = null;

    public CommandAddMem()
    {

    }

    /**
     * @return the thingName
     */
    public String getThingName()
    {
        return thingName;
    }

    /**
     * @param thingName the thingName to set
     */
    public void setThingName(String thingName)
    {
        this.thingName = thingName;
    }

    /**
     * @return the x Position
     */
    public Float getX()
    {
        return x;
    }

    /**
     * @param xPosition the x Position to set
     */
    public void setX(Float xPosition)
    {
        this.x = xPosition;
    }

    /**
     * @return the yPosition
     */
    public Float getY()
    {
        return y;
    }

    /**
     * @param yPosition the y Position to set
     */
    public void setY(Float yPosition)
    {
        this.y = yPosition;
    }


}
