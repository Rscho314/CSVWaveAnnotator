package artery_labeler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Trace;
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
	public Trace trace2;
	public XYGraph xyGraph;
	private static CircularBufferDataProvider trace2Provider;
	public Display display = MainWindow.display;
	private static long t;
	public Graph() {

		Color red = display.getSystemColor(SWT.COLOR_RED);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		
		xyGraph = new XYGraph();
		xyGraph.primaryXAxis.setTitle("Time");
		xyGraph.primaryYAxis.setTitle("Signal");
		xyGraph.primaryXAxis.setDateEnabled(false);
		xyGraph.primaryYAxis.setAutoScale(true);
		xyGraph.primaryXAxis.setAutoScale(true);
		xyGraph.primaryXAxis.setAutoScaleThreshold(0);
		xyGraph.primaryYAxis.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
		
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

		
		xyGraph.addTrace(trace2);

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
			
			
			for(int i = 0; i < w+1; i++){
				rs.next();
				final Sample flowsample = new Sample(i, rs.getDouble(3));
				trace2Provider.addSample(flowsample);
				trace2Provider.setCurrentYDataTimestamp(rs.getInt(1)); // ticks in seconds
			}
			t = 1250;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void update(ResultSet rs) throws SQLException {
			t+=1;
			rs.next();
			final Sample flowsample = new Sample(t, rs.getDouble(3));
			trace2Provider.addSample(flowsample);
			trace2Provider.setCurrentYDataTimestamp(rs.getInt(1));
	};
	
	public static void update10(ResultSet rs) throws SQLException {
		for(int i = 0; i<11;i++){
			t+=1;
			rs.next();
			final Sample flowsample = new Sample(t, rs.getDouble(3));
			trace2Provider.addSample(flowsample);
			trace2Provider.setCurrentYDataTimestamp(rs.getInt(1));
		}
};

}
