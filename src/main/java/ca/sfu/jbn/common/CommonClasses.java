package ca.sfu.jbn.common;

import java.util.ArrayList;

public class CommonClasses
{
	// Returns the intersection of two arraylists in an arraylist
	public ArrayList intersect(ArrayList a1, ArrayList a2)
	{
		boolean haveIntersect = false;
		ArrayList res = new ArrayList();
		for (int i = 0; i < a1.size(); i++) {
			String str1 = (String) a1.get(i);
			if (!res.contains(a1.get(i)))
				res.add(a1.get(i));
			for (int j = 0; j < a2.size(); j++) {
				String str2 = (String) a2.get(j);
				int result = str1.compareTo(str2);
				if (!res.contains(a2.get(j)))
					res.add(a2.get(j));

				if (result == 0)
					haveIntersect = true;
			}
		}
		if (haveIntersect) {
			// so we do not return (ra,registration,ra)
			if (res.size() >= a1.size() && res.size() >= a2.size())
				return res;

		}

		return null;
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////
}
