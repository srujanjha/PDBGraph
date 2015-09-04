
public class Atom {
	public String Name="N";
	double[] coords =new double[3];
	public void     setCoords( double[] c ) { coords = c   ; }
	public double[] getCoords()            { return coords ; }
	public void setX(double x) {
		coords[0] = x ;
	}
	public void setY(double y) {
		coords[1] = y ;
	}
	public void setZ(double z) {
		coords[2] = z ;
	}
	public double getX() { return coords[0]; }
	public double getY() { return coords[1]; }
	public double getZ() { return coords[2]; }


}
