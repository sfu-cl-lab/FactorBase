package cs.sfu.jbn.alchemy;

import java.util.Random;

public class TestMLN {
	
	public static void main(String[] args) {
		Random randomGenerator = new Random();
		StringBuffer output = new StringBuffer();
		

		for (int idx = 1; idx <= 600; ++idx) {
			int randomInt = randomGenerator.nextInt(100);
			if (randomInt < 50) {
				output.append("gender(Id" + idx + ",M)" + "\n");
				randomInt = randomGenerator.nextInt(100);
				if (randomInt < 20)
					output.append("hair(Id" + idx + ",Long)" + "\n");
				else
					output.append("hair(Id" + idx + ",Short)" + "\n");
					
			} else {
				output.append("gender(Id" + idx + ",F)" + "\n");
				randomInt = randomGenerator.nextInt(100);
				if (randomInt < 20)
					output.append("hair(Id" + idx + ",Short)" + "\n");
				else
					output.append("hair(Id" + idx + ",Long)" + "\n");

			}
			   System.out.println(output.toString());
		}
	}
}
