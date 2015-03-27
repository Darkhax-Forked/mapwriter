package mapwriter.util;

import java.util.List;

public class Utils 
{
	public static int[] integerListToIntArray(List<Integer> list)
	{
		// convert List of integers to integer array to pass as default value
		int size = list.size();
		int[] array = new int[size];
		for (int i = 0; i < size; i++) 
		{
			array[i] = list.get(i);
		}
		
		return array;
	}
}
