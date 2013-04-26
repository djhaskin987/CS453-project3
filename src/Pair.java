
public class Pair<K extends Comparable<K>,V>
    implements Comparable<Pair<K,V>>
{
	private K first;
	private V second;

	private void init(K first, V second)
	{
        this.first = first;
        this.second = second;
	}

	public Pair()
	{
        init(null,null);
	}

	public Pair(K first, V second)
	{
		init(first,second);
	}

	public K First()
	{
		return first;
	}

	public V Second()
	{
		return second;
	}

	public boolean equals(Pair<K,V> o)
	{
		if ((o == null) ||
            ((first == null || o.first == null) &&
                (first != o.first)) ||
            ((second == null || o.second == null) &&
                (second != o.second)))
        {
            return false;
        }
		return (first == o.first || first.equals(o.first)) &&
            (second == o.second || second.equals(o.second));
	}

	public int hashCode()
	{
		return first.hashCode() + second.hashCode() * 7;
	}

    public int compareTo(Pair<K,V> other)
    {
        return first.compareTo(other.first);
    }

	public String toString()
	{
		return "(" + first + "," + second + ")";
	}
}
