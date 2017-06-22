package MLNExporter;
/*
 * Description: This class is MergeSort.
 *	   		   It allows the users sort the array of string in alphabetical order
 *
 * Function: Merge - merge the sub-array with the order from small to large
 *			 mSort - divided string array with recursive function and then merge them together
 * 
 * Bugs: None
 *
 * Version: 1.0
 *
 */
public class MergeSort {
	/*
	 * divided string array with recursive function and then merge them back
	 * @param array of strings  the non-sorted string array
	 * @param low  the lower position index (start point) in the array
	 * @param high  the higher position index (start point) in the array
	 */
	public static void mSort(String[] array, int low, int high){
		if(low < high){
			int mid = (low + high) / 2;
			mSort(array, low, mid);
			mSort(array, mid + 1, high);
			merge(array, low, mid, mid + 1, high);
		}
		else
			return;	
	}
	
	/*
	 * merge the sub-array with the order from small to large
	 * @param array of strings  the string array which need to be merged
	 * @param first1  the starting position of the first array which need to be merged
	 * @param last1  the ending position of the first array which need to be merged
	 * @param first2  the starting position of the second array which need to be merged
	 * @param last2  the ending position of the second array which need to be merged
	 */
	public static void merge(String[] array, int first1, int last1, int first2, int last2){
		String[] temp = new String[last2 - first1 + 1];
		int low = first1;
		int high = last2;
		int index = 0;
		for(; (first1 <= last1) && (first2 <= last2) ; index++){
			if(array[first1].toLowerCase().compareTo(array[first2].toLowerCase()) < 0){
				temp[index] = array[first1];
				first1 ++;
			}
			else{
				temp[index] = array[first2];
				first2 ++;
			}
		}
		
		for(;first1 <= last1; ++first1, ++index){
			temp[index] = array[first1];
		}
		for(;first2 <= last2; ++first2, ++index){
			temp[index] = array[first2];
		}
		
		index = 0;
		for(int i = low; i <= high; i++, index++){
			array[i] = temp[index];
		}
	}
}
