package ca.sfu.Evaluation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AddConst {

	public static void add(){
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		String string ="";

			try {
				string = reader.readLine();
				
				
				while(string !=null){
					String output="0	";
					String[] sections = string.split("[(]");
					output+=sections[0]+"(";
					String[] atoms=sections[1].split("[)]")[0].split(",");
					int idCounter=0;
					for(int atomNumber=0;atomNumber<atoms.length;atomNumber++){
						if(atoms[atomNumber].contains("ID")|| atoms[atomNumber].contains("id")){
							output+="a"+idCounter;
							idCounter++;
						}
						else{
							output+=atoms[atomNumber];
						}
						output+=",";
					}
					output=output.substring(0, output.length()-1)+")";
					
					System.out.println(output);
					string = reader.readLine();
				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
			
	}
public static void main(String[] args){
	AddConst.add();
}
}
