using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Windows.Forms.DataVisualization.Charting;

namespace PDBGraph
{
    public partial class Form2 : Form
    {
        public Form2()
        {
            InitializeComponent();
            PopulateGraph();
        }
        public void PopulateGraph()
        {
            try
            {
                chart.ChartAreas[0].AxisX.Maximum = 180;
                chart.ChartAreas[0].AxisX.Minimum = -180;
                chart.ChartAreas[0].AxisY.Maximum = 180;
                chart.ChartAreas[0].AxisY.Minimum = -180;
                chart.Titles[0].Text = PlotList.Title;
                chart.ChartAreas[0].AxisX.Title = PlotList.XTitle;
                chart.ChartAreas[0].AxisY.Title = PlotList.YTitle;
                chart.Titles[1].Text = PlotList.Resolution;
                chart.Titles[2].Text = PlotList.Date;
                chart.Series["Series"].MarkerSize = PlotList.Size;
                label5.Text = "Number of points:" + PlotList.series.Count;

                foreach (Data data in PlotList.series)
                    chart.Series["Series"].Points.AddXY(data.X, data.Y);
            }
            catch (Exception e1) { MessageBox.Show("Error occured !"); }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            try { FolderBrowserDialog fbd = new FolderBrowserDialog();
                DialogResult result = fbd.ShowDialog();
                textBox1.Text = (fbd.SelectedPath);
            }
            catch (Exception e1) { MessageBox.Show("Error occured !"); }
}

        private void button2_Click(object sender, EventArgs e)
        {
            try {
                chart.SaveImage(textBox1.Text+textBox2.Text+".png", ChartImageFormat.Png);
                label2.Visible = true; label2.Text = "Graph Saved at " + textBox1.Text +textBox2.Text + ".png";
            }
            catch(Exception e1) { label2.Visible = true;label2.Text = e1.Message; MessageBox.Show("Error occured !"); }
        }
    }
}
