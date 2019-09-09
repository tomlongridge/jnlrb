package linegenerator.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import linegenerator.core.LinePoint.Direction;
import linegenerator.core.exceptions.MethodGenerationException;

public class LineFactory extends AbstractMethodGenerator {

    /** The size of a place bell dot. */
    private float m_DotSize = 3f;
    
    /** The width of the line. */
    private float m_LineWidth = 0.75f;

    /** The amount to increase the height of a place by. */
    private float m_PlaceMultiplier = 2.0f;
    
    /** The amount to increase the row height compared to the width. */
    private float m_HeightMultiplier = 0.2f;
    
    /** The width of the line to be drawn. */
    private float m_Width = 100f;
    
    /** The color to draw the line. */
    private Color m_Color = Color.BLACK;
    
    /** The padding to the sides of the line. */
    private float m_HorizontalPadding = 10f;
    
    /** The padding above and below the line. */
    private float m_VerticalPadding = 10f;

    /** Directory in which to find TTF files. */
    private String m_FontDirectory = "C:/windows/fonts";

    private String m_LabelFont = "Arial Bold";
    private float m_LabelFontSize = 8f;

    private String m_TitleFont = "Arial";
    private float m_TitleFontSize = 10f;
    private float m_TitleLeading = 9f;

    private String m_DescriptionFont = "Arial";
    private float m_DescriptionFontSize = 8f;
    private float m_DescriptionLeading = 6f;
    private float m_DescriptionSpaceBefore = 2f;
    private float m_VerticalOffset = 0f;

    private boolean m_ShowTitle = true;
    private boolean m_ShowFullTitle = true;
    private boolean m_ShowMethodDescription = true;
    
	private HashMap<String, Integer> m_MethodTitleSizes;
	private HashMap<String, HashMap<String,LinePoint.Direction>> m_LabelPositions;
	
	private float m_CrossWidth = m_LineWidth;
	private Color m_CrossColor = Color.DARK_GRAY;
    
    public LineFactory(final File pr_OutputDir, final String pr_PropertiesFile) {
        
        super(pr_OutputDir);
        
        Properties properties = new Properties();
        File propertiesFile = new File(pr_PropertiesFile);
        if (propertiesFile.exists()) {
            try {
                final FileInputStream propertiesIn = new FileInputStream(propertiesFile);
                properties.load(propertiesIn);
                propertiesIn.close();
            } catch (IOException e) {
            	System.err.println("Error occurred reading properties, using defaults");
            }
        } else {
        	System.err.println("Warning: no line properties file found, using defaults");
        }
        m_DotSize = Float.parseFloat(properties.getProperty("DotSize", String.valueOf(m_DotSize)));
        m_LineWidth = Float.parseFloat(properties.getProperty("LineWidth", String.valueOf(m_LineWidth)));
        m_PlaceMultiplier = Float.parseFloat(properties.getProperty("PlaceMultiplier", String.valueOf(m_PlaceMultiplier)));
        m_HeightMultiplier = Float.parseFloat(properties.getProperty("HeightMultiplier", String.valueOf(m_HeightMultiplier)));
        m_Width = Float.parseFloat(properties.getProperty("Width", String.valueOf(m_Width)));
        m_HorizontalPadding = Float.parseFloat(properties.getProperty("HorizontalPadding", String.valueOf(m_HorizontalPadding)));
        m_VerticalPadding = Float.parseFloat(properties.getProperty("VerticalPadding", String.valueOf(m_VerticalPadding)));
        m_FontDirectory = properties.getProperty("FontDirectory", m_FontDirectory);
        m_LabelFont = properties.getProperty("LabelFont", m_LabelFont);
        m_LabelFontSize = Float.parseFloat(properties.getProperty("LabelFontSize", String.valueOf(m_LabelFontSize)));
        m_TitleFont = properties.getProperty("TitleFont", m_TitleFont);
        m_TitleFontSize = Float.parseFloat(properties.getProperty("TitleFontSize", String.valueOf(m_TitleFontSize)));
        m_TitleLeading = Float.parseFloat(properties.getProperty("TitleLeading", String.valueOf(m_TitleLeading)));
        m_DescriptionFont = properties.getProperty("DescriptionFont", m_DescriptionFont);
        m_DescriptionFontSize = Float.parseFloat(properties.getProperty("DescriptionFontSize", String.valueOf(m_DescriptionFontSize)));
        m_DescriptionLeading = Float.parseFloat(properties.getProperty("DescriptionLeading", String.valueOf(m_DescriptionLeading)));
        m_DescriptionSpaceBefore = Float.parseFloat(properties.getProperty("DescriptionSpaceBefore", String.valueOf(m_DescriptionSpaceBefore)));
        m_VerticalOffset = Float.parseFloat(properties.getProperty("VerticalOffset", String.valueOf(m_VerticalOffset)));
        
        m_ShowTitle = Boolean.parseBoolean(properties.getProperty("ShowTitle", String.valueOf(m_ShowTitle)));
        m_ShowFullTitle = Boolean.parseBoolean(properties.getProperty("ShowFullTitle", String.valueOf(m_ShowFullTitle)));
        m_ShowMethodDescription = Boolean.parseBoolean(properties.getProperty("ShowMethodDescription", String.valueOf(m_ShowMethodDescription)));
        
        String lineColor = properties.getProperty("Color");
        if (lineColor != null) {
        	String[] lineColours = lineColor.split(",");
        	if (lineColours.length == 3) {
        		try {
	        		m_Color = new Color(Integer.valueOf(lineColours[0]),
						        			Integer.valueOf(lineColours[1]),
						        			Integer.valueOf(lineColours[2]));
        		} catch (NumberFormatException e) {
        			System.err.println("Illegal format of color in properties: " + lineColours);
        		}
        	}
        }
        
        m_CrossWidth = Float.parseFloat(properties.getProperty("CrossWidth", String.valueOf(m_CrossWidth)));
        String crossColor = properties.getProperty("CrossColor");
        if (crossColor != null) {
        	String[] crossColors = crossColor.split(",");
        	if (crossColors.length == 3) {
        		try {
	        		m_CrossColor = new Color(Integer.valueOf(crossColors[0]),
						        			Integer.valueOf(crossColors[1]),
						        			Integer.valueOf(crossColors[2]));
        		} catch (NumberFormatException e) {
        			System.err.println("Illegal format of cross color in properties: " + crossColor);
        		}
        	}
        }
        
        m_MethodTitleSizes = new HashMap<String,Integer>();
        m_LabelPositions = new HashMap<String, HashMap<String,LinePoint.Direction>>();
        properties = new Properties();
        propertiesFile = new File("FineControl.properties");
        if (propertiesFile.exists()) {
            try {
                final FileInputStream propertiesIn = new FileInputStream(propertiesFile);
                properties.load(propertiesIn);
                propertiesIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        for (Object method : properties.keySet()) {
        	String methodStr = (String) method;
        	String controlElementsStr = properties.getProperty(methodStr);
        	String[] controlElements = controlElementsStr.split("\\|");
        	if (!controlElements[0].isEmpty()) {
        		try {
        			m_MethodTitleSizes.put(methodStr, Integer.parseInt(controlElements[0]));
        		} catch (NumberFormatException e) {
            		System.err.println("Illegal format in fine control properties (text size NaN): " + methodStr);
            		continue;
        		}
        	}
        	if (controlElements.length >= 2 && !controlElements[1].isEmpty()) {
        		m_LabelPositions.put(methodStr, new HashMap<String,LinePoint.Direction>());
        		for (String position : controlElements[1].split(",")) {
        			String[] labelPosition = position.split(":");
        			if (labelPosition.length != 2) {
                		System.err.println("Illegal format in fine control properties (direction name-pair incorrect): " + position);
                		continue;
                	}
        			try {
        				m_LabelPositions.get(methodStr).put(labelPosition[0], LinePoint.Direction.valueOf(labelPosition[1]));
        			} catch (IllegalArgumentException e) {
        				System.err.println("Illegal format in fine control properties (not a direction): " + position);
                		continue;
        			}
        		}
        	}
        }
    }
    
    public boolean generate(final Method pr_Method, final boolean pr_Overwrite) throws FileNotFoundException, MethodGenerationException
    {
    	final String methodIdentifier = pr_Method.getFileIdentifier();
        final File outputFile = new File(getOutputDirectory().getAbsolutePath() + 
                                         File.separator + 
                                         methodIdentifier + 
                                         ".pdf");
        if (!pr_Overwrite && outputFile.exists()) {
            return false;
        }
        
        final Notation notation = new Notation();
        for (int i = 0; i < pr_Method.getPlaceNotation().length; i++) {
            notation.addAll(pr_Method.getLeadNotation(LeadType.PLAIN, i));
        }
        
        final Grid grid = new Grid(pr_Method);
        grid.add(notation, LeadType.PLAIN, pr_Method, pr_Method.getStartOffset());
        grid.add(notation, LeadType.PLAIN, pr_Method, 0, pr_Method.getStartOffset() - 1);
        
        int startBell = pr_Method.getStartBell();
        final ArrayList<LinePoint> points = new ArrayList<LinePoint>();
        
        int curPos = startBell - 1;
        int lastPos = curPos;
        int treblePos;
        LinePoint point;
        float columnWidth = m_Width / (pr_Method.getStage().getBells() - 1);
        float rowHeight = columnWidth * m_HeightMultiplier;
        float x = m_HorizontalPadding + curPos * columnWidth;
        
        float y;
        if (m_ShowTitle) {
        	y = 0;
        } else {
        	y = m_VerticalPadding;
        }
    
        points.add(new LinePoint(x, y, "" + startBell));
        
        do
        {
            for (int i = 1; i < grid.size(); i++) {
                
                curPos = grid.getRow(i).indexOf("" + Stage.getLabelAtPosition(startBell));
                treblePos = grid.getRow(i).indexOf("" + Stage.getLabelAtPosition(1));
                x = m_HorizontalPadding + curPos * columnWidth;
                
                if (curPos == lastPos) {
                    y += rowHeight * m_PlaceMultiplier;
                } else {
                    y += rowHeight;
                }
                
                if (grid.isLabel(i)) {
                	point = new LinePoint(x, y, "" + (curPos + 1));
                } else {
                	point = new LinePoint(x, y);
                }
                
                if (Math.abs(curPos - treblePos) == 1) {
                	point.setTrebleCross(columnWidth * (curPos > treblePos ? -1 : 1));
                }
                
                points.add(point);
                lastPos = curPos;
                    
            }
            
            startBell = lastPos + 1;
        }   
        while (startBell != pr_Method.getStartBell());

        // Don't show last crossing point (this doesn't look correct for 2nd's place methods)
        points.get(points.size() - 1).setTrebleCross(0d);
        
        Font placeBellFont;
        BaseFont titleBaseFont;
        BaseFont descriptonBaseFont;
        try {
            placeBellFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(m_FontDirectory + "/" + m_LabelFont));
            titleBaseFont = BaseFont.createFont(m_FontDirectory + "/" + m_TitleFont, BaseFont.CP1252, BaseFont.EMBEDDED);
            descriptonBaseFont = BaseFont.createFont(m_FontDirectory + "/" + m_DescriptionFont, BaseFont.CP1252, BaseFont.EMBEDDED);
        } catch (FontFormatException e) {
            throw new MethodGenerationException("Unable to load font: " + e.getMessage());
        } catch (IOException e) {
            throw new MethodGenerationException("Unable to load font: " + e.getMessage());
        } catch (DocumentException e) {
            throw new MethodGenerationException("Unable to load font: " + e.getMessage());
		}

        float titleSize;
        if (m_MethodTitleSizes.containsKey(methodIdentifier)) {
        	titleSize = m_MethodTitleSizes.get(methodIdentifier);
        } else {
        	titleSize = m_TitleFontSize;
        }
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(titleBaseFont, titleSize);
        com.itextpdf.text.Font descriptionFont = new com.itextpdf.text.Font(descriptonBaseFont, m_DescriptionFontSize);
        
        Rectangle pageSize;
        Document document;
        PdfWriter writer;
        try {
        	pageSize = new Rectangle(m_Width + (2 * m_HorizontalPadding), y + m_VerticalPadding);
        	document = new Document(pageSize, 0.0f, 0.0f, m_VerticalOffset, 0.0f);
            writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            
            if (m_ShowTitle || m_ShowMethodDescription) {
            
            	Paragraph methodTitle = null;
            	if (m_ShowTitle) {
		            methodTitle = new Paragraph((m_ShowFullTitle ? pr_Method.toString() : pr_Method.getName()).toUpperCase(), titleFont);
		            methodTitle.setAlignment(Element.ALIGN_LEFT);
		            methodTitle.setLeading(m_TitleLeading);
		            methodTitle.setSpacingBefore(m_VerticalOffset);
		            document.add(methodTitle);
            	}
            	
            	Paragraph descriptionText;
            	if (m_ShowMethodDescription) {
		            descriptionText = new Paragraph(pr_Method.getDescription(), descriptionFont);
		            descriptionText.setAlignment(Element.ALIGN_LEFT);
		            descriptionText.setLeading(m_DescriptionLeading);
		            descriptionText.setSpacingBefore(m_DescriptionSpaceBefore);
		            document.add(descriptionText);
            	} else {
            		descriptionText = null;
            	}
            	
	            float yOffset = pageSize.getHeight() - writer.getVerticalPosition(true) + m_VerticalPadding;
	            for (LinePoint p : points) {
	            	y = (float) p.getY() + yOffset;
	            	p.setLocation(p.getX(), y);
	            }
	            
	            document.close();
	            
	        	pageSize = new Rectangle(m_Width + (2 * m_HorizontalPadding), y + m_VerticalPadding);
	        	document = new Document(pageSize, 0.0f, 0.0f, m_VerticalOffset, 0.0f);
	            writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
	            document.open();
	            
	            if (methodTitle != null) {
	            	document.add(methodTitle);
	            }
	            if (descriptionText != null) {
	            	document.add(descriptionText);
	            }
            
            }
            
        } catch (DocumentException e) {
            throw new MethodGenerationException("Unable to create PDF file: " + e.getMessage());
        }
        
        
        Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        
        map.put(TextAttribute.SIZE, m_LabelFontSize);
        placeBellFont = placeBellFont.deriveFont( map );
        
        Graphics2D g = writer.getDirectContent().createGraphicsShapes(pageSize.getWidth(),
                                                                      pageSize.getHeight());
        g.setFont(placeBellFont);
        
        boolean intersects = false;
        double distance;
        
        LinePoint labelPoint = null;
        Rectangle2D labelPosition = null;
        
        FontMetrics metrics = g.getFontMetrics(placeBellFont);
        Rectangle2D labelBounds;
        LineMetrics lineMetrics;
        
        int startPoint = -1;
        int endPoint = -1;
        
        Ellipse2D.Float dot;
        double mypad = 0.5d;
        
        /* Loop through the points and plot the line, dot and labels */
        GeneralPath path = null;
        GeneralPath trebleCrossPath = null;
        
        // List to store dots - drawn at the end to ensure they are on top
        ArrayList<Ellipse2D.Float> dots = new ArrayList<Ellipse2D.Float>();
        
        g.setStroke(new BasicStroke(m_CrossWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (LinePoint p : points) {
            
            if (path == null) {
                path = new GeneralPath();
                path.moveTo((float) p.getX(), (float) p.getY());
            } else {
                path.lineTo((float) p.getX(), (float) p.getY());
            }
            
            if (p.getTrebleCross() != 0d) {
            	if (trebleCrossPath == null) {
            		trebleCrossPath = new GeneralPath();
            		trebleCrossPath.moveTo((float) p.getX() + p.getTrebleCross(), (float) p.getY());
	            } else {
	            	trebleCrossPath.lineTo((float) p.getX() + p.getTrebleCross(), (float) p.getY());
	            }
            } else if (trebleCrossPath != null) {
                g.setColor(m_CrossColor);
            	g.draw(trebleCrossPath);
            	trebleCrossPath = null;
            }
            
            if (p.isLeadEnd()) {
                
                dot = new Ellipse2D.Float((float) (p.getX() - (0.5f * m_DotSize)),
                        (float) (p.getY() - (0.5f * m_DotSize)),
                        m_DotSize,
                        m_DotSize);
                dots.add(dot);
    
                labelBounds = metrics.getStringBounds(p.getLabel(), g);
                lineMetrics = metrics.getLineMetrics(p.getLabel(), g);
                intersects = false;
                distance = m_DotSize * 0.5f;
                
                do {
                    
                    for (LinePoint.Direction direction : LinePoint.Direction.values()) {
                    	
                    	if (m_LabelPositions.containsKey(methodIdentifier)) {
                    		HashMap<String, Direction> positionsMap = m_LabelPositions.get(methodIdentifier);
                    		if (positionsMap.containsKey(p.getLabel())) {
                    			if (positionsMap.get(p.getLabel()) != direction) {
                    				continue;
                    			}
                    		}
                    	}
                        
                        labelPoint = new LinePoint(p, distance, direction);                 
                        labelPosition = new Rectangle2D.Double(labelPoint.getX() - (0.5d * labelBounds.getWidth()) - mypad,
                                                               labelPoint.getY() - (0.5d * (lineMetrics.getHeight() + lineMetrics.getBaselineOffsets()[Font.CENTER_BASELINE])) - mypad,
                                                               labelBounds.getWidth() + (2 * mypad),
                                                               lineMetrics.getHeight() + lineMetrics.getBaselineOffsets()[Font.CENTER_BASELINE] + (2 * mypad));
                        
                        if (labelPosition.intersects(dot.getBounds2D())) {
                            intersects = true;
                        } else if (labelPosition.getX() < pageSize.getLeft()) {
                        	intersects = true;
                        } else if (labelPosition.getX() + labelPosition.getWidth() > pageSize.getRight()) {
                        	intersects = true;
                        } else if (labelPosition.getY() < pageSize.getBottom()) {
                        	intersects = true;
                        } else if (labelPosition.getY() + labelPosition.getHeight() > pageSize.getTop()) {
                        	intersects = true;
                        } else {
                        
	                        startPoint = -1;
	                        endPoint = -1;
	    
	                        for (int i = 0; i < points.size(); i++) {
	                            if (startPoint == -1) {
	                                if (points.get(i).getY() > labelPosition.getY()) {
	                                    startPoint = Math.max(i - 1, 0);
	                                }
	                            }
	                            
	                            if (points.get(i).getY() > labelPosition.getMaxY()) {
	                                endPoint = Math.min(i, points.size() - 1);
	                                break;
	                            }
	                        }
	                        
	                        intersects = false;
	                        for (int i = startPoint; i < endPoint; i++) {
	                            if (labelPosition.intersectsLine(new Line2D.Double(points.get(i), points.get(i + 1)))) {
	                                intersects = true;
	                                break;
	                            }
	                        }
                        }
                        if (!intersects) {
                            break;
                        }   
                    
                    }
                    
                    distance += 1.0f;
                    
                } while (intersects);

                g.setColor(m_Color);
                g.drawString(p.getLabel(),
                             (float) labelPosition.getX() + (float) mypad,
                             (float) (labelPosition.getY() + labelPosition.getHeight() - mypad));
            }
            
        }

        // Finish off last cross
    	if (trebleCrossPath != null) {
            g.setColor(m_CrossColor);
        	g.draw(trebleCrossPath);
        }

        g.setColor(m_Color);
        g.setStroke(new BasicStroke(m_LineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(path);

        // Draw dots last
        for (Ellipse2D.Float d : dots) {
        	g.fill(d);
        }
        
        g.dispose();
        document.close();
        return true;
    }
    
}
