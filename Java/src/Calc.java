import javax.vecmath.Vector3d;

public class Calc {

	/**
	 * calculate distance between two atoms.
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return a double	
	 */
	public static final double getDistance(Atom a, Atom b) {
		double x = a.getX() - b.getX();
		double y = a.getY() - b.getY();
		double z = a.getZ() - b.getZ();

		double s  = x * x  + y * y + z * z;

		return Math.sqrt(s);
	}


	/**
	 * Will calculate the square of distances between two atoms. This will be
	 * faster as it will not perform the final square root to get the actual
	 * distance. Use this if doing large numbers of distance comparisons - it is
	 * marginally faster than getDistance().
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return a double
	 */
	public static double getDistanceFast(Atom a, Atom b) {
		double x = a.getX() - b.getX();
		double y = a.getY() - b.getY();
		double z = a.getZ() - b.getZ();

		return x * x  + y * y + z * z;
	}

	public static final Atom invert(Atom a) {
		double[] coords = new double[]{0.0,0.0,0.0} ;
		Atom zero = new Atom();
		zero.setCoords(coords);
		return subtract(zero, a);
	}


	/** add two atoms ( a + b).
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return an Atom object
	 */
	public static final Atom add(Atom a, Atom b){

		Atom c = new Atom();
		c.setX( a.getX() + b.getX() );
		c.setY( a.getY() + b.getY() );
		c.setZ( a.getZ() + b.getZ() );

		return c ;
	}



	/** subtract two atoms ( a - b).
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return n new Atom object representing the difference
	 */
	public static final Atom subtract(Atom a, Atom b) {
		Atom c = new Atom();
		c.setX( a.getX() - b.getX() );
		c.setY( a.getY() - b.getY() );
		c.setZ( a.getZ() - b.getZ() );

		return c ;
	}

	/** Vector product (cross product).
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return an Atom object
	 */
	public static final Atom vectorProduct(Atom a , Atom b){

		Atom c = new Atom();
		c.setX( a.getY() * b.getZ() - a.getZ() * b.getY() ) ;
		c.setY( a.getZ() * b.getX() - a.getX() * b.getZ() ) ;
		c.setZ( a.getX() * b.getY() - a.getY() * b.getX() ) ;
		return c ;

	}

	/**
	 * Scalar product (dot product).
	 *
	 * @param a an Atom object
	 * @param b an Atom object
	 * @return a double
	 */
	public static final double scalarProduct(Atom a, Atom b) {
		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
	}

	/** 
	 * Gets the length of the vector (2-norm)
	 *
	 * @param a  an Atom object
	 * @return Square root of the sum of the squared elements
	 */
	public static final double amount(Atom a){
		return Math.sqrt(scalarProduct(a,a));
	}

	/** 
	 * Gets the angle between two vectors
	 *
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @return Angle between a and b in degrees, in range [0,180]. 
	 * If either vector has length 0 then angle is not defined and NaN is returned 
	 */
	public static final double angle(Atom a, Atom b){


		Vector3d va = new Vector3d(a.getCoords());
		Vector3d vb = new Vector3d(b.getCoords());

		return Math.toDegrees(va.angle(vb));

	}

	/** 
	 * Returns the unit vector of vector a .
	 *
	 * @param a  an Atom object
	 * @return an Atom object
	 */
	public static final Atom unitVector(Atom a) {
		double amount = amount(a) ;

		double[] coords = new double[3];

		coords[0] = a.getX() / amount ;
		coords[1] = a.getY() / amount ;
		coords[2] = a.getZ() / amount ;

		a.setCoords(coords);
		return a;

	}

	/**
	 * Calculate the torsion angle, i.e. the angle between the normal vectors of the 
	 * two plains a-b-c and b-c-d.
	 * See http://en.wikipedia.org/wiki/Dihedral_angle
	 * @param a  an Atom object
	 * @param b  an Atom object
	 * @param c  an Atom object
	 * @param d  an Atom object
	 * @return the torsion angle in degrees, in range +-[0,180]. 
	 * If either first 3 or last 3 atoms are colinear then torsion angle is not defined and NaN is returned
	 */
	public static final double torsionAngle(Atom a, Atom b, Atom c, Atom d) {

		Atom ab = subtract(a,b);
		Atom cb = subtract(c,b);
		Atom bc = subtract(b,c);
		Atom dc = subtract(d,c);

		Atom abc = vectorProduct(ab,cb);
		Atom bcd = vectorProduct(bc,dc);

		double angl = angle(abc,bcd) ;

		/* calc the sign: */
		Atom vecprod = vectorProduct(abc,bcd);
		double val = scalarProduct(cb,vecprod);
		if (val<0.0) angl = -angl ;

		return angl;
	}

	/**
	 * Calculate the phi angle.
	 *
	 * @param a  an AminoAcid object
	 * @param b  an AminoAcid object
	 * @return a double
	 * @throws StructureException if aminoacids not connected or if any of the 4 needed atoms missing
	 */
	public static final double getPhi(AminoAcid a, AminoAcid b) {

		if ( ! isConnected(a,b)){
			System.out.println("can not calc Phi - AminoAcids are not connected!") ;
		}

		Atom a_C  = a.getC();
		Atom b_N  = b.getN();
		Atom b_CA = b.getCA();
		Atom b_C  = b.getC();

		// C and N were checked in isConnected already
		if (b_CA==null) System.out.println("Can not calculate Phi, CA atom is missing");

		return torsionAngle(a_C,b_N,b_CA,b_C);
	}

	/**
	 * Calculate the psi angle.
	 *
	 * @param a  an AminoAcid object
	 * @param b  an AminoAcid object
	 * @return a double
	 * @throws StructureException if aminoacids not connected or if any of the 4 needed atoms missing
	 */
	public static final double getPsi(AminoAcid a, AminoAcid b){
		if ( ! isConnected(a,b)) {
			System.out.println("can not calc Psi - AminoAcids are not connected!") ;
		}

		Atom a_N   = a.getN();
		Atom a_CA  = a.getCA();
		Atom a_C   = a.getC();
		Atom b_N   = b.getN();

		// C and N were checked in isConnected already
		if (a_CA==null) System.out.println("Can not calculate Psi, CA atom is missing");

		return torsionAngle(a_N,a_CA,a_C,b_N);

	}

	/**
	 * Test if two amino acids are connected, i.e.
	 * if the distance from C to N < 2.5 Angstrom.
	 *
	 * If one of the AminoAcids has an atom missing, returns false.
	 *
	 * @param a  an AminoAcid object
	 * @param b  an AminoAcid object
	 * @return true if ...
	 */
	public static final boolean isConnected(AminoAcid a, AminoAcid b) {
		Atom C = null ;
		Atom N = null;

		C = a.getC();
		N = b.getN();

		if ( C == null || N == null)
			return false;

		// one could also check if the CA atoms are < 4 A...
		//double distance = getDistance(C,N);
		//return distance < 2.5;
		return true;
	}

}
