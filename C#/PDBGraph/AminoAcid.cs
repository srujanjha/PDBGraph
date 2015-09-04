using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PDBGraph
{
    class AminoAcid
    {
        public int aaID = 1;
        private Atom N, CA, C, O, CB;

        public Atom getCB()
        {
            return CB;
        }

        public void setCB(Atom cB)
        {
            CB = cB;
        }

        public Atom getO()
        {
            return O;
        }

        public void setO(Atom o)
        {
            O = o;
        }

        public Atom getC()
        {
            return C;
        }

        public void setC(Atom c)
        {
            C = c;
        }

        public Atom getN()
        {
            return N;
        }

        public void setN(Atom n)
        {
            N = n;
        }

        public Atom getCA()
        {
            return CA;
        }

        public void setCA(Atom cA)
        {
            CA = cA;
        }
    }
}
