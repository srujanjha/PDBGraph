using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media.Media3D;

namespace PDBGraph
{
    class Calc
    {
            public static double getDistance(Atom a, Atom b)
            {
                double x = a.getX() - b.getX();
                double y = a.getY() - b.getY();
                double z = a.getZ() - b.getZ();

                double s = x * x + y * y + z * z;

                return Math.Sqrt(s);
            }
            public static double getDistanceFast(Atom a, Atom b)
            {
                double x = a.getX() - b.getX();
                double y = a.getY() - b.getY();
                double z = a.getZ() - b.getZ();

                return x * x + y * y + z * z;
            }
            public static Atom invert(Atom a)
            {
                double[] coords = new double[] { 0.0, 0.0, 0.0 };
                Atom zero = new Atom();
                zero.setCoords(coords);
                return subtract(zero, a);
            }
            public static Atom add(Atom a, Atom b)
            {

                Atom c = new Atom();
                c.setX(a.getX() + b.getX());
                c.setY(a.getY() + b.getY());
                c.setZ(a.getZ() + b.getZ());

                return c;
            }
            public static  Atom subtract(Atom a, Atom b)
            {
                Atom c = new Atom();
                c.setX(a.getX() - b.getX());
                c.setY(a.getY() - b.getY());
                c.setZ(a.getZ() - b.getZ());

                return c;
            }
            public static Atom vectorProduct(Atom a, Atom b)
            {

                Atom c = new Atom();
                c.setX(a.getY() * b.getZ() - a.getZ() * b.getY());
                c.setY(a.getZ() * b.getX() - a.getX() * b.getZ());
                c.setZ(a.getX() * b.getY() - a.getY() * b.getX());
                return c;

            }
            public static double scalarProduct(Atom a, Atom b)
            {
                return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
            }
            public static double amount(Atom a)
            {
                return Math.Sqrt(scalarProduct(a, a));
            }
        
            public static  double angle(Atom a, Atom b)
            {
                Vector3D va = new Vector3D(a.getX(), a.getY(), a.getZ());
                Vector3D vb = new Vector3D(b.getX(), b.getY(), b.getZ());

                return Vector3D.AngleBetween(va, vb);

        }
            public static Atom unitVector(Atom a)
            {
                double amnt = amount(a);

                double[] coords = new double[3];

                coords[0] = a.getX() / amnt;
                coords[1] = a.getY() / amnt;
                coords[2] = a.getZ() / amnt;

                a.setCoords(coords);
                return a;

            }
        public static double torsionAngle(Atom a, Atom b, Atom c, Atom d)
        {

            Atom ab = subtract(a, b);
            Atom cb = subtract(c, b);
            Atom bc = subtract(b, c);
            Atom dc = subtract(d, c);

            Atom abc = vectorProduct(ab, cb);
            Atom bcd = vectorProduct(bc, dc);

            double angl = angle(abc, bcd);

            /* calc the sign: */
            Atom vecprod = vectorProduct(abc, bcd);
            double val = scalarProduct(cb, vecprod);
            if (val < 0.0) angl = -angl;

            return angl;
        }
            public static double getPhi(AminoAcid a, AminoAcid b)
            {

                if (!isConnected(a, b))
                {
                    //System.out.println("can not calc Phi - AminoAcids are not connected!");
                }

                Atom a_C = a.getC();
                Atom b_N = b.getN();
                Atom b_CA = b.getCA();
                Atom b_C = b.getC();

            // C and N were checked in isConnected already
            if (b_CA == null) { }// System.out.println("Can not calculate Phi, CA atom is missing");

                return torsionAngle(a_C, b_N, b_CA, b_C);
            }

            /**
             * Calculate the psi angle.
             *
             * @param a  an AminoAcid object
             * @param b  an AminoAcid object
             * @return a double
             * @throws StructureException if aminoacids not connected or if any of the 4 needed atoms missing
             */
            public static double getPsi(AminoAcid a, AminoAcid b)
            {
                if (!isConnected(a, b))
                {
                    //System.out.println("can not calc Psi - AminoAcids are not connected!");
                }

                Atom a_N = a.getN();
                Atom a_CA = a.getCA();
                Atom a_C = a.getC();
                Atom b_N = b.getN();

            // C and N were checked in isConnected already
            if (a_CA == null) { }// System.out.println("Can not calculate Psi, CA atom is missing");

                return torsionAngle(a_N, a_CA, a_C, b_N);

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
            public static Boolean isConnected(AminoAcid a, AminoAcid b)
            {
                Atom C = null;
                Atom N = null;

                C = a.getC();
                N = b.getN();

                if (C == null || N == null)
                    return false;

                // one could also check if the CA atoms are < 4 A...
                //double distance = getDistance(C,N);
                //return distance < 2.5;
                return true;
            }
        }
}
