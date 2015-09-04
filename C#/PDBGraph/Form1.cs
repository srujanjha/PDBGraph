using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace PDBGraph
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }
        private void backgroundTask()
        {
            foreach (string fileName in Directory.EnumerateFiles(FolderPath.Text, "*.pdb"))
            {
                this.Invoke((MethodInvoker)delegate {
                    label7.Text = "Reading " + count + " of " + fCount + " files...";
                    progressBar.Value = count;
                });
                Structure pdb = loadStructure(fileName);
                calcPhiPsi(pdb,0);

            }
            this.Invoke((MethodInvoker)delegate {
                label7.Text = "Loading completed.";
                button1.Enabled = true;
                progressBar.Value = progressBar.Maximum;
                var newForm = new Form2(); //create your new form.
                newForm.Show(); //show the new form.
            });             
        }
        private int fCount = 0, count = 0;
        private int[][] angles = new int[361][];
        private void differencePlotTask()
        {
            for (int i = 0; i < 361; i++) angles[i] = new int[361];
            foreach (string fileName in Directory.EnumerateFiles(textBox6.Text, "*.pdb"))
            {
                this.Invoke((MethodInvoker)delegate {
                    label7.Text = "Reading " + count + " of " + fCount + " files...";
                    progressBar.Value = count;
                });
                Structure pdb = loadStructure(fileName);
                calcPhiPsi(pdb,1);
            }
            fCount = 0; count = 0;
            fCount = Directory.GetFiles(FolderPath.Text, "*.pdb").Length;
            this.Invoke((MethodInvoker)delegate {
                progressBar.Maximum = fCount;
            }); 
            foreach (string fileName in Directory.EnumerateFiles(FolderPath.Text, "*.pdb"))
            {
                this.Invoke((MethodInvoker)delegate {
                    label7.Text = "Reading " + count + " of " + fCount + " files...";
                    progressBar.Value = count;
                });
                Structure pdb = loadStructure(fileName);
                calcPhiPsi(pdb,2);
            }
            this.Invoke((MethodInvoker)delegate {
                label7.Text = "Loading completed.";
                button2.Enabled = true;
                progressBar.Value = progressBar.Maximum;
                var newForm = new Form2();
                newForm.Show();
            });
        }
        private void button1_Click(object sender, EventArgs e)
        {
            button1.Enabled = false;
            PlotList.Title = textBox1.Text;
            PlotList.XTitle = textBox2.Text;
            PlotList.YTitle = textBox3.Text;
            PlotList.Date = textBox5.Text;
            PlotList.Resolution = textBox4.Text;
            if (!Int32.TryParse(textBox7.Text, out PlotList.Size))
                PlotList.Size = 1;
            label7.Visible = true;
            fCount = 0; count = 0;
            fCount = Directory.GetFiles(FolderPath.Text, "*.pdb").Length;
            progressBar.Maximum = fCount;
            Thread background = new Thread(new ThreadStart(backgroundTask));
            background.Start();
        }
        private void button2_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog fbd = new FolderBrowserDialog();
            DialogResult result = fbd.ShowDialog();
            FolderPath.Text=(fbd.SelectedPath);
        }
        private void calcPhiPsi(Structure structure,int flag)
        {
            try
            {
                for (int j = 0; j < structure.Groups.Count(); j++)
                {
                    AminoAcid a;
                    AminoAcid b;
                    AminoAcid c;
                    for (int i = 0; i < structure.Groups.ElementAt(j).AA.Count(); i++)
                    {
                        b = structure.Groups.ElementAt(j).AA.ElementAt(i);
                        double phi = 360.0;
                        double psi = 360.0;
                        if (i > 0)
                        {
                            a = structure.Groups.ElementAt(j).AA.ElementAt(i - 1);
                            try
                            {
                                phi = Calc.getPhi(a, b);
                            }
                            catch (Exception e)
                            {
                                phi = 360.0;
                            }
                        }
                        if (i < structure.Groups.ElementAt(j).AA.Count() - 1)
                        {
                            c = structure.Groups.ElementAt(j).AA.ElementAt(i + 1);
                            try
                            {
                                psi = Calc.getPsi(b, c);
                            }
                            catch (Exception e)
                            {
                                
                                psi = 360.0;
                            }
                        }
                        if (phi == 360.0 || psi == 360.0) continue;
                        if (flag==1) angles[(int)phi+180][(int)psi+180] = 1;
                        else if(flag==2 && angles[(int)phi+180][(int)psi+180] != 1) PlotList.series.Add(new Data(phi, psi));
                        else if (flag == 0)PlotList.series.Add(new Data(phi,psi));
                    }
                }
            }
            catch (Exception e)
            {
               
            }
        }
        private void button3_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog fbd = new FolderBrowserDialog();
            DialogResult result = fbd.ShowDialog();
            textBox6.Text = (fbd.SelectedPath);
        }

        private void button4_Click(object sender, EventArgs e)
        {
            button4.Enabled = false;
            PlotList.Title = textBox1.Text;
            PlotList.XTitle = textBox2.Text;
            PlotList.YTitle = textBox3.Text;
            PlotList.Date = textBox5.Text;
            PlotList.Resolution = textBox4.Text;
            label7.Visible = true;
            fCount = 0; count = 0;
            fCount = Directory.GetFiles(textBox6.Text, "*.pdb").Length;
            progressBar.Maximum = fCount;
            Thread background = new Thread(new ThreadStart(differencePlotTask));
            background.Start(); 
        }

        private Structure loadStructure(string fileName)
        {
            Structure structure = new Structure();
            int l = fileName.Length;
            structure.PDBCode = fileName.Substring(l-8,4);
                bool flag = false;
                string line = "";
                StreamReader file = new StreamReader(fileName);
                Group g = new Group();
                AminoAcid aa = new AminoAcid();
            while ((line = file.ReadLine()) != null)
            {

                if (line.StartsWith("ATOM"))
                {
                    try{
                        if (flag)
                    {
                        g.groupID = (line.ElementAt(21) - 65);
                        aa.aaID = Convert.ToInt16(line.Substring(22, 4).Trim()); flag = false;
                    }
                    if (aa.aaID != (Convert.ToInt16(line.Substring(22, 4).Trim())))
                    {
                        g.AA.Add(aa);
                        aa = new AminoAcid();
                        aa.aaID = Convert.ToInt16(line.Substring(22, 4).Trim());
                    }
                    if (g.groupID != (line.ElementAt(21) - 65))
                    {
                        structure.Groups.Add(g);
                        g = new Group();
                        g.groupID = (line.ElementAt(21) - 65);
                    }
                    Atom ob = new Atom();
                    
                        ob.setX(Convert.ToDouble(line.Substring(30, 8).Trim()));
                        ob.setY(Convert.ToDouble(line.Substring(38, 8).Trim()));
                        ob.setZ(Convert.ToDouble(line.Substring(46, 8).Trim()));
                        ob.Name = line.Substring(12, 3).Trim();
                        if (ob.Name.Equals("N")) aa.setN(ob);
                        else if (ob.Name.Equals("CA")) aa.setCA(ob);
                        else if (ob.Name.Equals("CB")) aa.setCB(ob);
                        else if (ob.Name.Equals("O")) aa.setO(ob);
                        else if (ob.Name.Equals("C")) aa.setC(ob);
                    }
                    catch (Exception) { }
                }
            }
                    g.AA.Add(aa); structure.Groups.Add(g);
                   
                file.Close();
                count++; 
            return structure;
        }
    }
}
