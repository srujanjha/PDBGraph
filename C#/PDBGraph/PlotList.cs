using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PDBGraph
{
    class PlotList
    {
        public static List<Data> series = new List<Data>();
        public static string XTitle="PHI", YTitle="PSI", Title="Ramachandran Plot for Phi and Psi",Resolution="Less than 1 Angstorm",Date="01-01-2009 to 31-01-2010";
        public static int Size = 1;
    }
}
