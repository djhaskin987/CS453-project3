import java.util.*;
public class Utilities
{
    public static <K> void MapIncrementCount(
            Map<K,Integer> map,
            K next)
    {
        Integer Count = map.get(next);
        if (Count == null)
        {
            Count = new Integer(1);
        }
        else
        {
            Count = new Integer(
                    Count.intValue() + 1);
            map.remove(next);
        }
        map.put(next, Count);
    }

    public static <K1, K2, V2> Map<K1, Map<K2, V2>> MapMapInit(
            Set<K1> KeySet)
    {
        Map<K1, Map<K2, V2>> MapMap = new HashMap<K1, Map<K2, V2>>();
        for (K1 k : KeySet)
        {
            MapMap.put(k,
                    new HashMap<K2,V2>());
        }
        return MapMap;
    }
}
