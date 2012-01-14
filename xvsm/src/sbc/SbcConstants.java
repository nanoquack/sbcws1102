package sbc;

public class SbcConstants {

	public static final String PRODUCERCONTAINER="sbc_producer";
	public static final String STORAGECONTAINER="sbc_storage"; //not used anymore
	public static final String CONSTRUCTIONCONTAINER="sbc_construction";
	public static final String TESTERCONTAINER="sbc_tester";
	public static final String LOGISTICCONTAINER="sbc_logistic";
	public static final String NOTIFICATIONCONTAINER="sbc_notification";
	public static final String SALESCONTAINER = "sbc_sales";
	public static final String RECYCLINGCONTAINER = "sbc_recycling";
	public static final String LOADBALANCERCONTAINER = "sbc_loadbalancer";
	
	public static final String ProducerUrl="xvsm://localhost:12345";	//not used
	public static final String StorageContainerUrl="xvsm://localhost:12346"; //not used anymore
//	public static final String TesterContainerUrl="xvsm://localhost:12347";
//	public static final String LogisticContainerUrl="xvsm://localhost:12348";
//	public static final String SalesContainerUrl = "xvsm://localhost:12349";
//	public static final String RecyclingContainerUrl = "xvsm://localhost:12349";
//	public static final String NotificationUrl="xvsm://localhost:12350";
	public static final int LoadBalancerPort=11222;
	public static final String LoadBalancerUrl="xvsm://localhost:11222";
	
	public static final int PRODUCERPORTOFFSET=0;
	public static final int STORAGEPORTOFFSET=1;	//not used anymore
	public static final int CONSTRUCTIONPORTOFFSET=2;
	public static final int TESTERPORTOFFSET=3;
	public static final int LOGISTICPORTOFFSET=4;
	public static final int LOGGERPORTOFFSET=5;
	
	public static int MAINPORT=-1;	//overwritten at setup
}
