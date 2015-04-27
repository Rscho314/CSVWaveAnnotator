package artery_labeler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.figures.Trace.BaseLine;
import org.csstudio.swt.xygraph.figures.Trace.TraceType;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.draw2d.Figure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import artery_labeler.MainWindow;

public class Graph extends Figure {
	public Trace trace1;
	public Trace trace2;
	public Trace trace3;
	public Trace trace4;
	public XYGraph xyGraph;
	private static CircularBufferDataProvider trace1Provider;
	private static CircularBufferDataProvider trace2Provider;
	private static CircularBufferDataProvider trace3Provider;
	private static CircularBufferDataProvider trace4Provider;
	public Display display = MainWindow.display;
	private static long t;
	public Graph() {

		Color red = display.getSystemColor(SWT.COLOR_RED);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		Color green = display.getSystemColor(SWT.COLOR_GREEN);
		
		xyGraph = new XYGraph();
		xyGraph.primaryXAxis.setTitle("Time");
		xyGraph.primaryYAxis.setTitle("Signal");
		xyGraph.primaryXAxis.setDateEnabled(false);
		xyGraph.primaryYAxis.setAutoScale(true);
		xyGraph.primaryXAxis.setAutoScale(true);
		xyGraph.primaryXAxis.setAutoScaleThreshold(0);
		xyGraph.primaryYAxis.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
		
		trace1Provider = new CircularBufferDataProvider(true);
		trace1Provider.setBufferSize(2);
		trace1 = new Trace("Bullseye", xyGraph.primaryXAxis, xyGraph.primaryYAxis, trace1Provider);
		trace1.setDataProvider(trace1Provider);
		trace1.setTraceColor(green);
		trace1.setTraceType(TraceType.BAR);
		trace1.setLineWidth(1);
		trace1.setBaseLine(BaseLine.NEGATIVE_INFINITY);
		trace1.setAreaAlpha(100);
		trace1.setAntiAliasing(true);
		
		
		trace2Provider = new CircularBufferDataProvider(true);
		trace2Provider.setBufferSize(1250);
		trace2 = new Trace("Flow", xyGraph.primaryXAxis, xyGraph.primaryYAxis, trace2Provider);
		trace2.setDataProvider(trace2Provider);
		trace2.setTraceColor(red);
		trace2.setTraceType(TraceType.SOLID_LINE);
		trace2.setLineWidth(3);
		trace2.setBaseLine(BaseLine.NEGATIVE_INFINITY);
		trace2.setAreaAlpha(100);
		trace2.setAntiAliasing(true);
		
		trace3Provider = new CircularBufferDataProvider(true);
		trace3Provider.setBufferSize(625);
		trace3 = new Trace("systole", xyGraph.primaryXAxis, xyGraph.primaryYAxis, trace3Provider);
		trace3.setDataProvider(trace3Provider);
		trace3.setTraceColor(red);
		trace3.setTraceType(TraceType.AREA);
		trace3.setLineWidth(1);
		trace3.setBaseLine(BaseLine.ZERO);
		trace3.setAreaAlpha(50);
		trace3.setAntiAliasing(true);
		
		trace4Provider = new CircularBufferDataProvider(true);
		trace4Provider.setBufferSize(625);
		trace4 = new Trace("diastole", xyGraph.primaryXAxis, xyGraph.primaryYAxis, trace4Provider);
		trace4.setDataProvider(trace4Provider);
		trace4.setTraceColor(green);
		trace4.setTraceType(TraceType.AREA);
		trace4.setLineWidth(1);
		trace4.setBaseLine(BaseLine.ZERO);
		trace4.setAreaAlpha(50);
		trace4.setAntiAliasing(true);
		

		xyGraph.addTrace(trace1);
		xyGraph.addTrace(trace2);
		xyGraph.addTrace(trace3);
		xyGraph.addTrace(trace4);

		add(xyGraph);
	}
	
	@Override
	protected void layout() {
		xyGraph.setBounds(bounds.getCopy());
		super.layout();
	}

	/**
	 * Initiates the graph on file open with the 1250 first records
	 */
	public static void init(ResultSet rs, int w) {
		try{
			
			Double max = 0D;
			for(int i = 0; i < w; i++){
				rs.next();
				final Sample flowsample = new Sample(i, rs.getDouble(3));
				trace2Provider.addSample(flowsample);
				trace2Provider.setCurrentYDataTimestamp(rs.getInt(1)); // ticks in seconds
				if(rs.getDouble(3) > max){
					max = rs.getDouble(3);
				}
			}
			t = 1250;
			final Sample beSample = new Sample(625, max);
			trace1Provider.addSample(beSample);
			final Sample beSample1 = new Sample(627, max);
			trace1Provider.addSample(beSample1);
						
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void advance(ResultSet rs) throws SQLException {
			t+=1;
			rs.next();
			final Sample flowsample = new Sample(rs.getInt(1), rs.getDouble(3));
			trace2Provider.addSample(flowsample);
			trace2Provider.setCurrentYDataTimestamp(rs.getInt(1));
			
			Double max = trace2Provider.getYDataMinMax().getUpper();
			final Sample beSample = new Sample(rs.getInt(1)-625, max);
			trace1Provider.addSample(beSample);
			final Sample beSample1 = new Sample(rs.getInt(1)-623, max);
			trace1Provider.addSample(beSample1);
			
			
			rs.absolute(rs.getRow()-1250);
			for(int i=0; i<625; i++){
				if(rs.getInt(2)==1){
					final Sample areaSsample = new Sample(rs.getInt(1), rs.getDouble(3));
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), 0);
					trace4Provider.addSample(areaDsample);
				}
				else if(rs.getInt(2)==-1){
					final Sample areaSsample = new Sample(rs.getInt(1), 0);
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), rs.getDouble(3));
					trace4Provider.addSample(areaDsample);
				}
				else{
					final Sample areaSsample = new Sample(rs.getInt(1), 0);
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), 0);
					trace4Provider.addSample(areaDsample);
				}
				rs.next();
			}
			rs.absolute(rs.getRow()+625);
			
	}
	
	public static void advance10(ResultSet rs) throws SQLException {
		for(int i = 0; i<10;i++){
			t+=1;
			rs.next();
			final Sample flowsample = new Sample(rs.getInt(1), rs.getDouble(3));
			trace2Provider.addSample(flowsample);
			trace2Provider.setCurrentYDataTimestamp(rs.getInt(1));

			rs.absolute(rs.getRow()-1250);
			for(int w=0; w<625; w++){
				if(rs.getInt(2)==1){
					final Sample areaSsample = new Sample(rs.getInt(1), rs.getDouble(3));
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), 0);
					trace4Provider.addSample(areaDsample);
				}
				else if(rs.getInt(2)==-1){
					final Sample areaSsample = new Sample(rs.getInt(1), 0);
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), rs.getDouble(3));
					trace4Provider.addSample(areaDsample);
				}
				else{
					final Sample areaSsample = new Sample(rs.getInt(1), 0);
					trace3Provider.addSample(areaSsample);
					final Sample areaDsample = new Sample(rs.getInt(1), 0);
					trace4Provider.addSample(areaDsample);
				}
				rs.next();
			}
			rs.absolute(rs.getRow()+625);
		}
			Double max = trace2Provider.getYDataMinMax().getUpper();
			final Sample beSample = new Sample(rs.getInt(1)-625, max);
			trace1Provider.addSample(beSample);
			final Sample beSample1 = new Sample(rs.getInt(1)-623, max);
			trace1Provider.addSample(beSample1);
	}
		
		public static void retreat(ResultSet rs, int w) throws SQLException {
			try{
				
				rs.absolute(rs.getRow()-1251);
				Double max = 0D;
				for(int i = 0; i < w; i++){
					rs.next();
					final Sample flowsample = new Sample(rs.getRow(), rs.getDouble(3));
					trace2Provider.addSample(flowsample);
					trace2Provider.setCurrentYDataTimestamp(rs.getRow()); // ticks in seconds
					if(rs.getDouble(3) > max){
						max = rs.getDouble(3);
					}
				}
				
				final Sample beSample = new Sample(rs.getInt(1)-625, max);
				trace1Provider.addSample(beSample);
				final Sample beSample1 = new Sample(rs.getInt(1)-623, max);
				trace1Provider.addSample(beSample1);
				
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	public static void retreat10(ResultSet rs, int w) throws SQLException {
		try{
			
			rs.absolute(rs.getRow()-1260);
			Double max = 0D;
			for(int i = 0; i < w; i++){
				rs.next();
				final Sample flowsample = new Sample(rs.getRow(), rs.getDouble(3));
				trace2Provider.addSample(flowsample);
				trace2Provider.setCurrentYDataTimestamp(rs.getRow()); // ticks in seconds
				if(rs.getDouble(3) > max){
					max = rs.getDouble(3);
				}
			}
			
			final Sample beSample = new Sample(rs.getInt(1)-625, 0);
			trace1Provider.addSample(beSample);
			final Sample beSample1 = new Sample(rs.getInt(1)-623, max);
			trace1Provider.addSample(beSample1);
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
