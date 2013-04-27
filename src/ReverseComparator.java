import java.util.Comparator;
public class ReverseComparator<T extends Comparable<T>> implements Comparator<T>
{
    public ReverseComparator()
    {
    }

    public int compare(T o1, T o2)
    {
        if (o1 == null || o2 == null)
        {
            throw new IllegalArgumentException();
        }
        return -o1.compareTo(o2);
    }
}
