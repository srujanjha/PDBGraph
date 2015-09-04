import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class PDBGraph {

	public static void main(String[] args) {
		System.out.print("Enter a folder location(where all the PDB files are located):");
		Scanner sc=new Scanner(System.in);
		File folder = new File(sc.nextLine());
		File[] listOfFiles = folder.listFiles();
		System.out.print("Enter a folder location(where all the temporary files could be saved):");
		String tempFolder=sc.nextLine();
		for(int i=0;i<listOfFiles.length;i++){
		File listPDBs=listOfFiles[i];
		File listPDBs1=new File(tempFolder+listPDBs.getName()+".txt");
		Structure pdb=loadStructure(listPDBs);
		System.out.println(pdb.PDBCode+" "+pdb.Groups.size());
		calcPhiPsi(pdb,listPDBs1);
		}
		Ramachandran.Main(tempFolder);
		
	}
	public static void calcPhiPsi(Structure structure,File listPDBs1){
		try{
			   FileOutputStream fileout=new FileOutputStream(listPDBs1);
			   BufferedWriter wr=new BufferedWriter(new OutputStreamWriter(fileout));
			   
		for(int j=0;j<structure.Groups.size();j++)
		{
			AminoAcid a;
	        AminoAcid b;
	        AminoAcid c;
			for(int i=0;i<structure.Groups.get(j).AA.size();i++)
			{
                b = structure.Groups.get(j).AA.get(i);
                double phi =360.0;
                double psi =360.0;
                if ( i > 0) {
                        a = structure.Groups.get(j).AA.get(i-1) ;
                        try {
                                phi = Calc.getPhi(a,b);
                        } catch (Exception e){
                                e.printStackTrace();
                                phi = 360.0 ;
                        }
                }
                if ( i < structure.Groups.get(j).AA.size()-1) {
                        c = structure.Groups.get(j).AA.get(i+1) ;
                        try {
                                psi = Calc.getPsi(b,c);
                        }catch (Exception e){
                                e.printStackTrace();
                                psi = 360.0 ;
                        }
                }
                wr.write(phi+","+psi);
                wr.newLine();
        }
		}wr.close();
		}catch (Exception e) {
            e.printStackTrace();
    }
	}
	public static Structure loadStructure(File listPDBs){
		Structure structure = new Structure();
		int l=listPDBs.getName().length();
		structure.PDBCode=listPDBs.getName().substring(l-8,l-4);
        try{
        	boolean flag=true;
			   FileInputStream filein=new FileInputStream(listPDBs);
			   BufferedReader br=new BufferedReader(new InputStreamReader(filein));
			   String line=br.readLine();
			   Group g=new Group();
			   AminoAcid aa=new AminoAcid();
			   while(line!=null)
			   {   if(line.startsWith("ATOM")){
				   if(flag){g.groupID=(line.charAt(21)-65);
				   aa.aaID=Integer.parseInt(line.substring(22,26).trim());flag=false;}
				   if(aa.aaID!=(Integer.parseInt(line.substring(22,26).trim())))
				   {
					   g.AA.add(aa);
					   aa=new AminoAcid();
					   aa.aaID=Integer.parseInt(line.substring(22,26).trim());
				   }
				   if(g.groupID!=(line.charAt(21)-65))
				   {
					   structure.Groups.add(g);
					   g=new Group();
					   g.groupID=(line.charAt(21)-65);
				   }
				   Atom ob=new Atom();
				   ob.setX(Double.parseDouble(line.substring(30,38).trim()));
				   ob.setY(Double.parseDouble(line.substring(38,46).trim()));
				   ob.setZ(Double.parseDouble(line.substring(46,54).trim()));
				   ob.Name=line.substring(12,15).trim();
				   if(ob.Name.equals("N"))aa.setN(ob);
				   else if(ob.Name.equals("CA"))aa.setCA(ob);
				   else if(ob.Name.equals("CB"))aa.setCB(ob);
				   else if(ob.Name.equals("O"))aa.setO(ob);
				   else if(ob.Name.equals("C"))aa.setC(ob);				   
			   		}
			   line=br.readLine();
			   }
			   g.AA.add(aa);structure.Groups.add(g);
			   br.close();
		 } catch (Exception e) {
             e.printStackTrace();
     }
        return structure;
}

}
