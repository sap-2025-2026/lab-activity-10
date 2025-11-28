package ttt_api_gateway.infrastructure;

import common.exagonal.Adapter;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

@Adapter
public class PrometheusControllerObserver implements ControllerObserver {

	private Counter nTotalNumberOfRESTRequests;
	private HTTPServer promServer;
	
	public PrometheusControllerObserver(int port) throws ObsMetricServerException {
		JvmMetrics.builder().register(); 
		
		/* total number of REST requests received  */
		
		nTotalNumberOfRESTRequests = Counter.builder()
	    		    .name("apigat_num_rest_requests_total")
	    		    .help("Total number of REST requests received")
	    		    .register();

	    try {
	    	promServer = HTTPServer.builder()
		    .port(port)
		    .buildAndStart();
	    } catch (Exception ex) {
	    	throw new ObsMetricServerException();
	    }
	}

	@Override
	public void notifyNewRESTRequest() {
		nTotalNumberOfRESTRequests.inc();		
	}
	
	private void log(String msg) {
		System.out.println("[PROM] " + msg);
	}

}