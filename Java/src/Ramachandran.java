import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageIO.*;

public class Ramachandran extends JPanel {
    /** This shape will represent the x and y axes. */
    Shape mAxes;
    /** Length of half of an axis (-180 to +180). */
    int mLength = 180;
    /** This variable controls the height of ticks on the
	axis. See method createAxes for details. */
    int mTick = 4;
    /** Height of the final image */
    double imageHeight;
    /** Width of the jpg image.*/
    double imageWidth;
    /** Each point stores one psi/phi pair. */
    Vector<Point2D.Double> data;
    /** Draw a single point with with this radius */
    private static final float POINT_RADIUS = 0.7f;
    private static final float POINT_DIAMETER = POINT_RADIUS * 2;

    /** The Graphics object of the main display. */
    private Graphics2D g2d;


    /** Should we draw a box in the plot? This may be used 
	to draw a box around the region of a certain beta turn class.*/
    private boolean doBox;
    /** X and Y of the angles around which to draw
	a box of 30 degrees. */
    private int boxX, boxY;

    /** Do Kleywegt style core areas of Ramachandran plot */
    private boolean kleywegt;

    final static BasicStroke stroke = new BasicStroke(2.0f);
    final static BasicStroke wideStroke = new BasicStroke(8.0f);

    final static float dash1[] = {1.0f};
    final static BasicStroke dashed = new BasicStroke(1.0f, 
                                                      BasicStroke.CAP_BUTT, 
                                                      BasicStroke.JOIN_MITER, 
                                                      2.0f, new float[] {3, 4}, 
						      0.0f);
   

    /** This function draws a point representing one psi/phi pair. */
    private void drawPoint(Graphics2D g, double x, double y) {
        Ellipse2D point = new Ellipse2D.Double(x - POINT_RADIUS, 
	      y - POINT_RADIUS, POINT_DIAMETER, POINT_DIAMETER);
        g.setStroke(new BasicStroke(0.2f));
        g.fill(point);
    }
    
   
    /** Base name of input file */
    private String filename;
 
    /** This creates the axes for the main plot and returns
	them in form of a Java Shape which is then drawn by
	a Graphics2D object. */
    public Shape createAxes() {
        GeneralPath path = new GeneralPath();
        path.moveTo(-180,-180);
        path.lineTo(-180,180);
        path.lineTo(180,180);
        path.lineTo(180,-180);
        path.lineTo(-180,-180);
        path.moveTo(0,180);
        path.lineTo(0,-180);
	path.moveTo(180,0);
        path.lineTo(-180,0);
      
        int[] tickPos = {-180,-150,-120,-90,-60,-30,0,
			 30,60,90,120,150,180 };
        for (int j = 0; j < tickPos.length; ++j) {
            path.moveTo(-180-mTick,tickPos[j]);
            path.lineTo(-180+mTick,tickPos[j]);
        }
         for (int j = 0; j < tickPos.length; ++j) {
            path.moveTo(tickPos[j],-180-mTick);
            path.lineTo(tickPos[j],-180+mTick);
        } 
        return path;
    }
    

    /** Number and label the axes. For some reason this
     function needs to be called "extra" for printing to files
    even though the code in drawPlot does in fact number the
    axes. It does work well though. */
    private void numberAxes(Graphics2D g) {
        Font font = new Font("Serif", Font.PLAIN, 10);
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        int[] numPos = {-180,-90,0,90,180 };
        for (int i = 0; i < numPos.length; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append(numPos[i] + "\u00B0");
            g.drawString(sb.toString(), (int)(-210 + imageWidth / 2),
                    (int)(imageHeight/2 - numPos[i] ) + 2);
        }
        
        g.drawString("Psi", 10,(int)(imageHeight/2.0));
          
        for (int i = 0; i < numPos.length; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append(numPos[i] + "\u00B0");
            g.drawString(sb.toString(), 
			 (int)(imageWidth/2 + numPos[i] - 5) ,
			 (int)(-50 + imageHeight ));
        }
        
        g.drawString("Phi", (int)imageWidth / 2 - 5, 
		     (int)(-35 + imageHeight));
        
    }
    
    /** The constructor creates the axes and lets the drawPlot
	function create the canvas graphic. */
    public Ramachandran(int h, int w, String args) {
	this.data = new  Vector<Point2D.Double>();
	doBox = false; // Default: Do not draw box
	processCommandLine(args);
        imageHeight = (double) h;
        imageWidth = (double) w;
        
        mAxes = createAxes();

    }


    /** Set up data for drawing a box in some area of the
	panel. */
    public void setBox(int x, int y) {
	doBox = true;
	boxX = x;
	boxY = y;
    }


    /** THe main function for drawing the Ramachandran plot. */
    private void drawPlot(Graphics2D g2d) {
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY); 
	

	/** 1) Translate drawing surface so that we can draw
	    from -180 - +180 */
        g2d.translate(imageWidth/2.0,imageHeight/2.0); 
	/** 2) Save this transformation for drawing axes before
	    flipping coordinates */
	AffineTransform aftx = g2d.getTransform();
	/** 3) Flip coordinates to draw at "natural" y */
        g2d.scale(1.0,-1.0);  
	/** 4) Draw the preferred areas  */
	if (kleywegt) {
	    defcorKJ(g2d);
	} else {
	    defcor(g2d); 
	}
	/** 5) Draw the axes with ticks  */
        g2d.draw(mAxes);
	/** 6) If user passes x,y, draw 30 degree box representing
	    core area of a beta turn type. */
	if (doBox) {
	    g2d.setPaint(Color.BLUE);
	    g2d.setStroke(dashed);
	    Shape box = new Rectangle2D.Double(boxX-30, boxY-30,60,60);
	    g2d.draw(box);
	    g2d.setStroke(stroke);
	    g2d.setPaint(Color.BLACK);
	}

	/** 7) Draw the psi/phi data points */
        for (Point2D.Double p: data) {
            drawPoint(g2d,p.x,p.y);  
        }




	/** Draw Labels & Numbers for Axes.
	    Note that we use the translation but not the
	    "flipping" of the drawing canvas. */
	g2d.setTransform(aftx);
	Font font = new Font("Serif", Font.PLAIN, 10);
        g2d.setFont(font);
	
	/** The following two arrays are 'backwards' because of
	    the way we plot on the Y axis in Java (from the top)*/
	String[] strPos = {"-180"," -90","   0","  90"," 180" };
	int[] numPosY = {180,90,0,-90,-180 }; 
        for (int i = 0; i < numPosY.length; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append(strPos[i] + "\u00B0");
            g2d.drawString(sb.toString(), -215,
                    numPosY[i] + 5 );
        }
	g2d.drawString("Psi", -235,0);
        
	/** Note: for x axis there is no need to 'reverse' as above */
	int [] numPosX = {-180,-90,0,90,180 }; 
        for (int i = 0; i < numPosX.length; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append(strPos[i] + "\u00B0"); /* \u00B0= UniCode 'degree' */
            g2d.drawString(sb.toString(), 
			 numPosX[i] - 10,
			 (int)(-55 + imageHeight/2  ));
        }
        
        g2d.drawString("Phi", -5, (int) imageHeight/2 - 40);
        
    }


    
    public void paintComponent(Graphics g) {
        g2d = (Graphics2D) g;
	drawPlot(g2d);
      }
    
    public static void Main(String args) {
        Vector<Point2D.Double> data = new Vector<Point2D.Double>();
	boolean doBox = false;
    
	Ramachandran rama = new Ramachandran(500,500,args);

	/** Set up drawing a box of 30 degrees around X,Y angle
	    if the user enters a second argument. This can be 
	    used to show the ideal area for a given Beta Turn
	    type. */


	openInJFrame(rama, 500, 500, "Ramachandran Plot",Color.WHITE);
	int selection = JOptionPane.showConfirmDialog(null,
	     "Do you want to save this plot as \"ramachandran2.png\"?" ,
						      "Sample",
	  JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
	if (selection  ==  JOptionPane.YES_OPTION ) {
	    rama.exportToFile();
	}
	
    }

    
   
    public void exportToFile() {
	// Create an image to save
	BufferedImage  bImage =
	    new BufferedImage((int)imageWidth,(int) imageHeight, 
			      BufferedImage.TYPE_INT_RGB);
	
        // Create a graphics contents on the buffered image
        Graphics2D gtwod = bImage.createGraphics();
	
        // Draw graphics
        //gtwod.setColor(Color.white);
        gtwod.fillRect(0, 0,(int)imageWidth,(int) imageHeight);
        numberAxes(gtwod);
	drawPlot(gtwod);
	
        // Graphics context no longer needed so dispose it
        gtwod.dispose();
	// Write generated image to a file
	try {
	    // Save as PNG
	    File file = new File("ramachandran.png");
	    ImageIO.write(bImage, "png", file);
    	} catch (IOException e) {
	    System.err.println("Error saving file to png: " +e);
	}
    }
    



    /** A simplified way to see a JPanel or other Container.
     *  Pops up a JFrame with specified Container as the content pane.
     */

    public static JFrame openInJFrame(Container content,
				      int width,
				      int height,
				      String title,
				      Color bgColor) {
	JFrame frame = new JFrame(title);
	frame.setBackground(bgColor);
	content.setBackground(bgColor);
	frame.setSize(width, height);
	frame.setContentPane(content);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });
	frame.setVisible(true);
	return(frame);
    }
    
 

    /**
     * Define coordinates.
     * The following definition of the Ramachandran allowed
     * areas was taken from
     * ftp://kinemage.biochem.duke.edu/pub/kinfiles/rama/Rama500noGPc.kin
     * as described in  Lovell et al. (2003)
     * Proteins. 2003 Feb 15;50(3):437-50. 
     * Structure validation by Calpha geometry: phi,psi and Cbeta 
     * deviation.
     */

    public void defcor(Graphics2D g){
	Color black = new Color(0,0,0);
	Color orange = new Color(255,111,0);
	Color gold = new Color(255,204,0);

	// Draw the inner core region in orange (98% of residues)
	g.setColor(orange);
	GeneralPath gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(57.5f,67.5f);
	gpath.lineTo(57.5f,62.5f);
	gpath.lineTo(62.5f,62.5f);
	gpath.lineTo(62.5f,57.5f);
	gpath.lineTo(67.5f,57.5f);
	gpath.lineTo(67.5f,47.5f);
	gpath.lineTo(72.5f,47.5f);
	gpath.lineTo(72.5f,32.5f);
	gpath.lineTo(77.5f,32.5f);
	gpath.lineTo(77.5f,2.5f);
	gpath.lineTo(62.5f,2.5f);
	gpath.lineTo(62.5f,7.5f);
	gpath.lineTo(57.5f,7.5f);
	gpath.lineTo(57.5f,12.5f);
	gpath.lineTo(52.5f,12.5f);
	gpath.lineTo(52.5f,22.5f);
	gpath.lineTo(47.5f,22.5f);
	gpath.lineTo(47.5f,27.5f);
	gpath.lineTo(42.5f,27.5f);
	gpath.lineTo(42.5f,37.5f);
	gpath.lineTo(37.5f,37.5f);
	gpath.lineTo(37.5f,62.5f);
	gpath.lineTo(42.5f,62.5f);
	gpath.lineTo(42.5f,67.5f);
	gpath.lineTo(57.5f,67.5f);
	gpath.moveTo(-177.5f,-180.0f);
	gpath.lineTo(-177.5f,-177.5f);
	gpath.lineTo(-172.5f,-177.5f);
	gpath.lineTo(-172.5f,-172.5f);
	gpath.lineTo(-167.5f,-172.5f);
	gpath.lineTo(-167.5f,-167.5f);
	gpath.lineTo(-127.5f,-167.5f);
	gpath.lineTo(-127.5f,-172.5f);
	gpath.lineTo(-97.5f,-172.5f);
	gpath.lineTo(-97.5f,-167.5f);
	gpath.lineTo(-77.5f,-167.5f);
	gpath.lineTo(-77.5f,-172.5f);
	gpath.lineTo(-72.5f,-172.5f);
	gpath.lineTo(-72.5f,-177.5f);
	gpath.lineTo(-67.5f,-177.5f);
	gpath.lineTo(-67.5f,-180.0f);
	gpath.moveTo(-62.5f,180.0f);
	gpath.lineTo(-62.5f,172.5f);
	gpath.lineTo(-57.5f,172.5f);
	gpath.lineTo(-57.5f,167.5f);
	gpath.lineTo(-52.5f,167.5f);
	gpath.lineTo(-52.5f,157.5f);
	gpath.lineTo(-47.5f,157.5f);
	gpath.lineTo(-47.5f,147.5f);
	gpath.lineTo(-42.5f,147.5f);
	gpath.lineTo(-42.5f,137.5f);
	gpath.lineTo(-37.5f,137.5f);
	gpath.lineTo(-37.5f,122.5f);
	gpath.lineTo(-42.5f,122.5f);
	gpath.lineTo(-42.5f,117.5f);
	gpath.lineTo(-47.5f,117.5f);
	gpath.lineTo(-47.5f,112.5f);
	gpath.lineTo(-57.5f,112.5f);
	gpath.lineTo(-57.5f,107.5f);
	gpath.lineTo(-62.5f,107.5f);
	gpath.lineTo(-62.5f,102.5f);
	gpath.lineTo(-67.5f,102.5f);
	gpath.lineTo(-67.5f,97.5f);
	gpath.lineTo(-72.5f,97.5f);
	gpath.lineTo(-72.5f,62.5f);
	gpath.lineTo(-77.5f,62.5f);
	gpath.lineTo(-77.5f,52.5f);
	gpath.lineTo(-87.5f,52.5f);
	gpath.lineTo(-87.5f,47.5f);
	gpath.lineTo(-92.5f,47.5f);
	gpath.lineTo(-92.5f,52.5f);
	gpath.lineTo(-97.5f,52.5f);
	gpath.lineTo(-97.5f,67.5f);
	gpath.lineTo(-102.5f,67.5f);
	gpath.lineTo(-102.5f,77.5f);
	gpath.lineTo(-107.5f,77.5f);
	gpath.lineTo(-107.5f,82.5f);
	gpath.lineTo(-112.5f,82.5f);
	gpath.lineTo(-112.5f,72.5f);
	gpath.lineTo(-117.5f,72.5f);
	gpath.lineTo(-117.5f,62.5f);
	gpath.lineTo(-122.5f,62.5f);
	gpath.lineTo(-122.5f,52.5f);
	gpath.lineTo(-127.5f,52.5f);
	gpath.lineTo(-127.5f,47.5f);
	gpath.lineTo(-112.5f,47.5f);
	gpath.lineTo(-112.5f,42.5f);
	gpath.lineTo(-102.5f,42.5f);
	gpath.lineTo(-102.5f,37.5f);
	gpath.lineTo(-92.5f,37.5f);
	gpath.lineTo(-92.5f,32.5f);
	gpath.lineTo(-87.5f,32.5f);
	gpath.lineTo(-87.5f,22.5f);
	gpath.lineTo(-82.5f,22.5f);
	gpath.lineTo(-82.5f,17.5f);
	gpath.lineTo(-77.5f,17.5f);
	gpath.lineTo(-77.5f,12.5f);
	gpath.lineTo(-67.5f,12.5f);
	gpath.lineTo(-67.5f,7.5f);
	gpath.lineTo(-62.5f,7.5f);
	gpath.lineTo(-62.5f,2.5f);
	gpath.lineTo(-57.5f,2.5f);
	gpath.lineTo(-57.5f,-7.5f);
	gpath.lineTo(-52.5f,-7.5f);
	gpath.lineTo(-52.5f,-12.5f);
	gpath.lineTo(-47.5f,-12.5f);
	gpath.lineTo(-47.5f,-22.5f);
	gpath.lineTo(-42.5f,-22.5f);
	gpath.lineTo(-42.5f,-32.5f);
	gpath.lineTo(-37.5f,-32.5f);
	gpath.lineTo(-37.5f,-62.5f);
	gpath.lineTo(-42.5f,-62.5f);
	gpath.lineTo(-42.5f,-67.5f);
	gpath.lineTo(-77.5f,-67.5f);
	gpath.lineTo(-77.5f,-62.5f);
	gpath.lineTo(-117.5f,-62.5f);
	gpath.lineTo(-117.5f,-57.5f);
	gpath.lineTo(-122.5f,-57.5f);
	gpath.lineTo(-122.5f,-47.5f);
	gpath.lineTo(-127.5f,-47.5f);
	gpath.lineTo(-127.5f,-37.5f);
	gpath.lineTo(-132.5f,-37.5f);
	gpath.lineTo(-132.5f,-17.5f);
	gpath.lineTo(-137.5f,-17.5f);
	gpath.lineTo(-137.5f,2.5f);
	gpath.lineTo(-142.5f,2.5f);
	gpath.lineTo(-142.5f,32.5f);
	gpath.lineTo(-137.5f,32.5f);
	gpath.lineTo(-137.5f,52.5f);
	gpath.lineTo(-142.5f,52.5f);
	gpath.lineTo(-142.5f,57.5f);
	gpath.lineTo(-147.5f,57.5f);
	gpath.lineTo(-147.5f,67.5f);
	gpath.lineTo(-152.5f,67.5f);
	gpath.lineTo(-152.5f,77.5f);
	gpath.lineTo(-147.5f,77.5f);
	gpath.lineTo(-147.5f,87.5f);
	gpath.lineTo(-152.5f,87.5f);
	gpath.lineTo(-152.5f,97.5f);
	gpath.lineTo(-157.5f,97.5f);
	gpath.lineTo(-157.5f,112.5f);
	gpath.lineTo(-162.5f,112.5f);
	gpath.lineTo(-162.5f,122.5f);
	gpath.lineTo(-167.5f,122.5f);
	gpath.lineTo(-167.5f,132.5f);
	gpath.lineTo(-172.5f,132.5f);
	gpath.lineTo(-172.5f,142.5f);
	gpath.lineTo(-180.0f,142.5f);

	
	// Draw allowed outer region in gold
	// The first portion is in the left upper and lower quadrants
	g.draw(gpath);
	g.setColor(gold);

	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	
	gpath.moveTo(-42.5f,180.0f);
	gpath.lineTo(-42.5f,172.5f);
	gpath.lineTo(-42.5f,172.5f);
	gpath.lineTo(-37.5f,172.5f);
	gpath.lineTo(-37.5f,167.5f);
	gpath.lineTo(-32.5f,167.5f);
	gpath.lineTo(-32.5f,157.5f);
	gpath.lineTo(-27.5f,157.5f);
	gpath.lineTo(-27.5f,147.5f);
	gpath.lineTo(-22.5f,147.5f);
	gpath.lineTo(-22.5f,127.5f);
	gpath.lineTo(-17.5f,127.5f);
	gpath.lineTo(-17.5f,112.5f);
	gpath.lineTo(-22.5f,112.5f);
	gpath.lineTo(-22.5f,107.5f);
	gpath.lineTo(-27.5f,107.5f);
	gpath.lineTo(-27.5f,102.5f);
	gpath.lineTo(-32.5f,102.5f);
	gpath.lineTo(-32.5f,97.5f);
	gpath.lineTo(-47.5f,97.5f);
	gpath.lineTo(-47.5f,92.5f);
	gpath.lineTo(-52.5f,92.5f);
	gpath.lineTo(-52.5f,72.5f);
	gpath.lineTo(-57.5f,72.5f);
	gpath.lineTo(-57.5f,42.5f);
	gpath.lineTo(-62.5f,42.5f);
	gpath.lineTo(-62.5f,27.5f);
	gpath.lineTo(-57.5f,27.5f);
	gpath.lineTo(-57.5f,22.5f);
	gpath.lineTo(-52.5f,22.5f);
	gpath.lineTo(-52.5f,12.5f);
	gpath.lineTo(-47.5f,12.5f);
	gpath.lineTo(-47.5f,7.5f);
	gpath.lineTo(-42.5f,7.5f);
	gpath.lineTo(-42.5f,2.5f);
	gpath.lineTo(-37.5f,2.5f);
	gpath.lineTo(-37.5f,-7.5f);
	gpath.lineTo(-32.5f,-7.5f);
	gpath.lineTo(-32.5f,-12.5f);
	gpath.lineTo(-27.5f,-12.5f);
	gpath.lineTo(-27.5f,-27.5f);
	gpath.lineTo(-22.5f,-27.5f);
	gpath.lineTo(-22.5f,-47.5f);
	gpath.lineTo(-17.5f,-47.5f);
	gpath.lineTo(-17.5f,-67.5f);
	gpath.lineTo(-22.5f,-67.5f);
	gpath.lineTo(-22.5f,-77.5f);
	gpath.lineTo(-27.5f,-77.5f);
	gpath.lineTo(-27.5f,-82.5f);
	gpath.lineTo(-47.5f,-82.5f);
	gpath.lineTo(-47.5f,-87.5f);
	gpath.lineTo(-77.5f,-87.5f);
	gpath.lineTo(-77.5f,-92.5f);
	gpath.lineTo(-87.5f,-92.5f);
	gpath.lineTo(-87.5f,-112.5f);
	gpath.lineTo(-92.5f,-112.5f);
	gpath.lineTo(-92.5f,-122.5f);
	gpath.lineTo(-97.5f,-122.5f);
	gpath.lineTo(-97.5f,-137.5f);
	gpath.lineTo(-92.5f,-137.5f);
	gpath.lineTo(-92.5f,-142.5f);
	gpath.lineTo(-82.5f,-142.5f);
	gpath.lineTo(-82.5f,-147.5f);
	gpath.lineTo(-72.5f,-147.5f);
	gpath.lineTo(-72.5f,-152.5f);
	gpath.lineTo(-67.5f,-152.5f);
	gpath.lineTo(-67.5f,-157.5f);
	gpath.lineTo(-62.5f,-157.5f);
	gpath.lineTo(-62.5f,-162.5f);
	gpath.lineTo(-57.5f,-162.5f);
	gpath.lineTo(-57.5f,-167.5f);
	gpath.lineTo(-52.5f,-167.5f);
	gpath.lineTo(-52.5f,-172.5f);
	gpath.lineTo(-47.5f,-172.5f);
	gpath.lineTo(-47.5f,-177.5f);
	gpath.lineTo(-42.5f,-177.5f);
	gpath.lineTo(-42.5f,-180.0f);
	
	g.draw(gpath);

	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(-180.0f,-147.5f);
	gpath.lineTo(-177.5f,-147.5f);
	gpath.lineTo(-167.5f,-147.5f);
	gpath.lineTo(-167.5f,-142.5f);
	gpath.lineTo(-157.5f,-142.5f);
	gpath.lineTo(-157.5f,-137.5f);
	gpath.lineTo(-147.5f,-137.5f);
	gpath.lineTo(-147.5f,-132.5f);
	gpath.lineTo(-142.5f,-132.5f);
	gpath.lineTo(-142.5f,-127.5f);
	gpath.lineTo(-147.5f,-127.5f);
	gpath.lineTo(-147.5f,-97.5f);
	gpath.lineTo(-152.5f,-97.5f);
	gpath.lineTo(-152.5f,-92.5f);
	gpath.lineTo(-157.5f,-92.5f);
	gpath.lineTo(-157.5f,-82.5f);
	gpath.lineTo(-162.5f,-82.5f);
	gpath.lineTo(-162.5f,-52.5f);
	gpath.lineTo(-157.5f,-52.5f);
	gpath.lineTo(-157.5f,-37.5f);
	gpath.lineTo(-162.5f,-37.5f);
	gpath.lineTo(-162.5f,-7.5f);
	gpath.lineTo(-167.5f,-7.5f);
	gpath.lineTo(-167.5f,32.5f);
	gpath.lineTo(-172.5f,32.5f);
	gpath.lineTo(-172.5f,52.5f);
	gpath.lineTo(-177.5f,52.5f);
	gpath.lineTo(-177.5f,77.5f);
	gpath.lineTo(-180.0f,77.5f);
	

	g.draw(gpath);
	
	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(82.5f,57.5f);
	gpath.lineTo(87.5f,57.5f);
	gpath.lineTo(87.5f,42.5f);
	gpath.lineTo(92.5f,42.5f);
	gpath.lineTo(92.5f,22.5f);
	gpath.lineTo(97.5f,22.5f);
	gpath.lineTo(97.5f,-17.5f);
	gpath.lineTo(92.5f,-17.5f);
	gpath.lineTo(92.5f,-22.5f);
	gpath.lineTo(87.5f,-22.5f);
	gpath.lineTo(87.5f,-27.5f);
	gpath.lineTo(82.5f,-27.5f);
	gpath.lineTo(82.5f,-37.5f);
	gpath.lineTo(87.5f,-37.5f);
	gpath.lineTo(87.5f,-47.5f);
	gpath.lineTo(92.5f,-47.5f);
	gpath.lineTo(92.5f,-57.5f);
	gpath.lineTo(87.5f,-57.5f);
	gpath.lineTo(87.5f,-67.5f);
	gpath.lineTo(82.5f,-67.5f);
	gpath.lineTo(82.5f,-72.5f);
	gpath.lineTo(77.5f,-72.5f);
	gpath.lineTo(77.5f,-77.5f);
	gpath.lineTo(62.5f,-77.5f);
	gpath.lineTo(62.5f,-72.5f);
	gpath.lineTo(57.5f,-72.5f);
	gpath.lineTo(57.5f,-67.5f);
	gpath.lineTo(52.5f,-67.5f);
	gpath.lineTo(52.5f,-37.5f);
	gpath.lineTo(57.5f,-37.5f);
	gpath.lineTo(57.5f,-27.5f);
	gpath.lineTo(62.5f,-27.5f);
	gpath.lineTo(62.5f,-22.5f);
	gpath.lineTo(57.5f,-22.5f);
	gpath.lineTo(57.5f,-12.5f);
	gpath.lineTo(52.5f,-12.5f);
	gpath.lineTo(52.5f,-7.5f);
	gpath.lineTo(47.5f,-7.5f);
	gpath.lineTo(47.5f,-2.5f);
	gpath.lineTo(42.5f,-2.5f);
	gpath.lineTo(42.5f,2.5f);
	gpath.lineTo(37.5f,2.5f);
	gpath.lineTo(37.5f,12.5f);
	gpath.lineTo(32.5f,12.5f);
	gpath.lineTo(32.5f,22.5f);
	gpath.lineTo(27.5f,22.5f);
	gpath.lineTo(27.5f,32.5f);
	gpath.lineTo(22.5f,32.5f);
	gpath.lineTo(22.5f,47.5f);
	gpath.lineTo(17.5f,47.5f);
	gpath.lineTo(17.5f,67.5f);
	gpath.lineTo(22.5f,67.5f);
	gpath.lineTo(22.5f,77.5f);
	gpath.lineTo(27.5f,77.5f);
	gpath.lineTo(27.5f,82.5f);
	gpath.lineTo(32.5f,82.5f);
	gpath.lineTo(32.5f,87.5f);
	gpath.lineTo(47.5f,87.5f);
	gpath.lineTo(47.5f,92.5f);
	gpath.lineTo(67.5f,92.5f);
	gpath.lineTo(67.5f,87.5f);
	gpath.lineTo(72.5f,87.5f);
	gpath.lineTo(72.5f,82.5f);
	gpath.lineTo(77.5f,82.5f);
	gpath.lineTo(77.5f,77.5f);
	gpath.lineTo(82.5f,77.5f);
	gpath.lineTo(82.5f,57.5f);
	
	g.draw(gpath);
	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(72.5f,-102.5f);
	gpath.lineTo(72.5f,-112.5f);
	gpath.lineTo(77.5f,-112.5f);
	gpath.lineTo(77.5f,-157.5f);
	gpath.lineTo(72.5f,-157.5f);
	gpath.lineTo(72.5f,-180.0f);
       
	g.draw(gpath);

	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(57.5f,-180.0f);
	gpath.lineTo(57.5f,-167.5f);
	gpath.lineTo(52.5f,-167.5f);
	gpath.lineTo(52.5f,-162.5f);
	gpath.lineTo(47.5f,-162.5f);
	gpath.lineTo(47.5f,-157.5f);
	gpath.lineTo(42.5f,-157.5f);
	gpath.lineTo(42.5f,-152.5f);
	gpath.lineTo(37.5f,-152.5f);
	gpath.lineTo(37.5f,-142.5f);
	gpath.lineTo(32.5f,-142.5f);
	gpath.lineTo(32.5f,-107.5f);
	gpath.lineTo(37.5f,-107.5f);
	gpath.lineTo(37.5f,-102.5f);
	gpath.lineTo(42.5f,-102.5f);
	gpath.lineTo(42.5f,-97.5f);
	gpath.lineTo(52.5f,-97.5f);
	gpath.lineTo(52.5f,-92.5f);
	gpath.lineTo(62.5f,-92.5f);
	gpath.lineTo(62.5f,-97.5f);
	gpath.lineTo(67.5f,-97.5f);
	gpath.lineTo(67.5f,-102.5f);
	gpath.lineTo(72.5f,-102.5f);
	

	g.draw(gpath);
	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	gpath.moveTo(77.5f,180.0f);
	gpath.lineTo(77.5f,162.5f);
	gpath.lineTo(82.5f,162.5f);
	gpath.lineTo(82.5f,147.5f);
	gpath.lineTo(72.5f,147.5f);
	gpath.lineTo(72.5f,157.5f);
	gpath.lineTo(67.5f,157.5f);
	gpath.lineTo(67.5f,167.5f);
	gpath.lineTo(62.5f,167.5f);
	gpath.lineTo(62.5f,180.0f);
	
	//gpath.closePath();
	g.draw(gpath);
	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	
	gpath.moveTo(162.5f,180.0f);
	gpath.lineTo(162.5f,147.5f);
	gpath.lineTo(167.5f,147.5f);
	gpath.lineTo(167.5f,132.5f);
	gpath.lineTo(172.5f,132.5f);
	gpath.lineTo(172.5f,117.5f);
	gpath.lineTo(177.5f,117.5f);
	gpath.lineTo(177.5f,77.5f);
	gpath.lineTo(180.0f,77.5f);
	
	g.draw(gpath);
	gpath = new GeneralPath(GeneralPath.WIND_NON_ZERO);

	gpath.moveTo(162.5f,-180.0f);
	gpath.lineTo(162.5f,-177.5f);
	gpath.lineTo(167.5f,-177.5f);
	gpath.lineTo(167.5f,-167.5f);
	gpath.lineTo(172.5f,-167.5f);
	gpath.lineTo(172.5f,-157.5f);
	gpath.lineTo(177.5f,-157.5f);
	gpath.lineTo(177.5f,-147.5f);
	gpath.lineTo(180.0f,-147.5f);
	
	g.draw(gpath);
	
	g.setColor(black);
    }

     /**
     * Define coordinatesKJ.
     * The following definition of the Ramachandran allowed
     * areas was taken from
     * http://alpha2.bmc.uu.se/gerard/rama/ramarev.html
     * and was adapted from the awk script on that webpage.
     * See also 
     * GJ Kleywegt and TA Jones (1996). Phi/psi-chology: 
     * Ramachandran revisited. Structure 4, 1395 - 1400.
     * This definition will be used if the user passes a 'k'
     * as the third argument to this program
     */

    public void defcorKJ(Graphics2D g){
	   double x1,x2,y1,y2;
        Color green = new Color(10,220,10);
        g.setPaint(green);
        double height = 10.0;  // Height of each block is always 10.
        double width;
        Shape s;
       


        drawBlock(g,2,11,1);
        drawBlock(g,3,5,2);
        drawBlock(g,9,10,2);
        drawBlock(g,7,7,11);
        drawBlock(g,10,15,12);
        drawBlock(g, 7,15,13);
        drawBlock(g,6,15,14);
        drawBlock(g,6,14,15);
        drawBlock(g,5,14,16);
        drawBlock(g,5,13,17);
        drawBlock(g,5,12,18);
        drawBlock(g,4,11,19);
        drawBlock(g,25,26,19);
        drawBlock(g,5,11,20);
        drawBlock(g,25,26,20);
        drawBlock(g,5,10,21);
        drawBlock(g,24,26,21);
        drawBlock(g,5,8,22);
        drawBlock(g,23,25,22);
        drawBlock(g,5,7,23);
        drawBlock(g,23,25,23);
        drawBlock(g,5,6,24);
        drawBlock(g,10,11,24);
        drawBlock(g,23,25,24);
        drawBlock(g,4,6,25);
        drawBlock(g,9,11,25);
        drawBlock(g,23,24,25);
        drawBlock(g,4,6,26);
        drawBlock(g,9,11,26);
        drawBlock(g,4,11,27);
        drawBlock(g,3,11,28);
        drawBlock(g,3,12,29);
        drawBlock(g,3,14,20);
        drawBlock(g,2,14,31);
        drawBlock(g,2,14,32);
        drawBlock(g,2,14,33);
        drawBlock(g,1,13,34);
        drawBlock(g,1,13,35);
        drawBlock(g,1,12,36);
	// drawBlock(g,37,37,37);
        //drawBlock(g,2,11,37);
        g.setPaint(Color.BLACK);
    }

     
    public double defcor2degreeX(int x) {
        return (10 * (x-1)) - 180.0;  
    }
    
    /* The Y range goes from 1 to 37 in the awk coding.
     *  We need to transform this to -180 to +180 */
    public double defcor2degreeY(int y) {
        return (10 * (y-1)) - 180.0;  
    }
    
    /** Used by defcorKJ to draw 10 degree squares from 
	x1 to x2 at y level y1.
     */
    private void drawBlock (Graphics2D g, int x1i, int x2i, int y1i) {
        double height = 10.0;
	double x1 = defcor2degreeX(x1i);
	double x2 = defcor2degreeX(x2i+1); // <= Notation in awk script
	double y1 = defcor2degreeY(y1i); // 1 to 2
	double width = x2 - x1;
	Shape s = new Rectangle2D.Double(x1,y1,width,height);
	g.fill(s);
	g.draw(s);
    }


    public static  void usage() {
	System.out.println("Usage: java Ramachandran -f PSIPHI [-b x:y] [-k]\n" +
	   "\t-f PSIPHI is a file containing sets of psi/phi pairs\n" +
		  "\t(one per line, tab-separated)\n" +
		  "\t-b x:y is an optional set of angles around which\n" +
		  "\t\t to draw a box 30 degrees to each side\n" +
                          "\t\t(Can be used to display regions for \n" +
			   "\t\tvarious classes of beta turn)\n" +
			   "\t-k use Kleywegt/Jones definition of core areas\n" +
			   "\t\t(default:  Lovell et al. definition)\n"); 

	System.out.println("\tNote that the absolute value of x and y" +
			    " must be less than 150.");
	System.exit(1);
    }
 
    private void processCommandLine(String argv) {
	kleywegt = false;
	doBox = false;
	inputData(argv);
    }


    private void inputData (String folderPath) {
	
	try {
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		     try{ FileReader file1 = new FileReader(folderPath+listOfFiles[i].getName());
	    BufferedReader InputFile = new BufferedReader(file1);
	    // Get First Record
	    String currentRecord = InputFile.readLine(); 
	    int count = 1;
	    while(currentRecord != null){
		String[] stArray = currentRecord.split(",");
		if (stArray.length != 2) {
		    System.err.println("Bad input data, need 2 vals/row");
		    //continue;
		    System.exit(1);
		}
	
		double d1 = Double.parseDouble(stArray[0]);
		double d2 = Double.parseDouble(stArray[1]);
		this.data.add(new Point.Double(d1,d2));
		count++;
		currentRecord = InputFile.readLine();
	    }  // while
	    System.err.println("Read " + count + " lines of data");}catch (Exception e) {
		    e.printStackTrace();
		}
	} }
	}catch (Exception e) {
	    e.printStackTrace();
	}
	
    }

}