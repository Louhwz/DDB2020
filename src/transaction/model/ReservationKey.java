/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction.model;

import java.io.Serializable;

/**
 * @author RAdmin
 *
 */
public class ReservationKey implements Serializable
{
    protected String custName;

    protected int resvType;

    protected String resvKey;

    public ReservationKey(String custName, int resvType, String resvKey)
    {
        this.custName = custName;
        this.resvKey = resvKey;
        this.resvType = resvType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof ReservationKey))
            return false;
        if (this == o)
            return true;
        ReservationKey k = (ReservationKey) o;
        if (k.custName.equals(custName) && k.resvKey.equals(resvKey) && k.resvType == resvType)
            return true;
        return false;
    }

    @Override
    public int hashCode()
    {
        return custName.hashCode() + resvType + resvKey.hashCode();
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer("[");
        buf.append("customer name=");
        buf.append(custName);
        buf.append(";");
        buf.append("resvKey=");
        buf.append(resvKey);
        buf.append(";");
        buf.append("resvType=");
        buf.append(resvType);
        buf.append("]");

        return buf.toString();
    }
}