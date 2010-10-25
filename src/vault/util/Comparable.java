package vault.util;
public class Comparable
{
	public String parentlabel;
	public String parentvalue;
	public int compareTo(Comparable c)
	{
		if(this.parentvalue.compareTo(c.parentvalue)<0)
			return -1;
		else if(this.parentvalue.compareTo(c.parentvalue)>0)
			return 1;
		return 0;
	}
}
