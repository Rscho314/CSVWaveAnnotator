package artery_labeler;

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
	public Runnable updater;
	private final CircularBufferDataProvider trace2Provider;
	public Display display = MainWindow.display;
	private long t;
	private int frameDelay = 30;
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
		trace2Provider.setBufferSize(100);
		trace2Provider.setUpdateDelay(frameDelay);

		trace2 = new Trace("Flow", xyGraph.primaryXAxis, xyGraph.primaryYAxis, trace2Provider);
		trace2.setDataProvider(trace2Provider);
		
		trace2.setTraceColor(red);
		trace2.setTraceType(TraceType.SOLID_LINE);
		trace2.setLineWidth(1);
		trace2.setBaseLine(BaseLine.NEGATIVE_INFINITY);
		trace2.setAreaAlpha(100);
		trace2.setAntiAliasing(true);

		
		xyGraph.addTrace(trace2);

		add(xyGraph);
/*
	Display.getCurrent().timerExec(100, updater);
	t = Calendar.getInstance().getTimeInMillis();
*/
	}
	
	@Override
	protected void layout() {
		xyGraph.setBounds(bounds.getCopy());
		super.layout();
	}

	public void update() {
			t+=40;
			final Sample flowsample = new Sample(t, 1);
			trace2Provider.addSample(flowsample);
			trace2Provider.setCurrentYDataTimestamp(t);
	};

}
