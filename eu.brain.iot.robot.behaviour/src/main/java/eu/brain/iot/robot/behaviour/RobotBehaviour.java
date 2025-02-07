package eu.brain.iot.robot.behaviour;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest.State;
import eu.brain.iot.service.robotic.door.api.DoorStatusResponse;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.api.Command;
import eu.brain.iot.robot.api.Coordinate;
import eu.brain.iot.robot.events.*;
import eu.brain.iot.robot.events.QueryStateValueReturn.CurrentState;
import eu.brain.iot.robot.events.RobotCommand;
import eu.brain.iot.warehouse.events.CartMovedNotice;
import eu.brain.iot.warehouse.events.CartNoticeResponse;
import eu.brain.iot.warehouse.events.DockingRequest;
import eu.brain.iot.warehouse.events.DockingResponse;
import eu.brain.iot.warehouse.events.NewPickPointRequest;
import eu.brain.iot.warehouse.events.NewPickPointResponse;
import eu.brain.iot.warehouse.events.NewStoragePointRequest;
import eu.brain.iot.warehouse.events.NewStoragePointResponse;
import eu.brain.iot.warehouse.events.NoCartNotice;
import compound.sim07;

@Component(
		configurationPid= "eu.brain.iot.robot.behaviour.Behaviour",
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		service = {SmartBehaviour.class})

@SmartBehaviourDefinition(
		consumed = {MarkerReturn.class, QueryStateValueReturn.class, RobotReadyResponse.class, AvailabilityReturn.class, DoorStatusResponse.class, // AvailabilityReturn event will not be used in current workflow, it's for future use
				NewPickPointResponse.class, NewStoragePointResponse.class, CartNoticeResponse.class, DockingResponse.class },  // the events in this line are used to communicate with warehouse backend component  
		author = "UGA", name = "Robot Behavior",
		description = "Implements a Robot Behavior.")

public class RobotBehaviour implements SmartBehaviour<BrainIoTEvent> {

	public static int robotID;
    public boolean isRobotReady=false;
	//public static QueryStateValueReturn queryReturn;
	public int markerID = 0;
	public static NewPickPointResponse pickResponse = null;
	public static NewStoragePointResponse storageResponse = null;
	public static DockingResponse dockingResponse = null;
	public static CartNoticeResponse cartNoticeResponse = null;
	public static RobotReadyResponse rbc ;
	public static QueryStateValueReturn qs ;
	public static MarkerReturn cvr ;
	public static DoorStatusResponse response ;

	
	@ObjectClassDefinition
	public static @interface Config {
		
		@AttributeDefinition(description = "The name of the robot behaviour")
		String name();

		@AttributeDefinition(description = "The identifier for the robot behaviour")
		int id();

	}

	private Config config;
	/*updater of deliverables*/
	private final ScheduledExecutorService worker0 = Executors.newSingleThreadScheduledExecutor();
	
	private ExecutorService worker;
	private ServiceRegistration<?> reg;

	@Reference
	private EventBus eventBus;
	
    @Activate
	void activate(BundleContext context, Config config, Map<String,Object> props){
    	
	    this.config=config;
	    System.out.println("\n Hello!  I am robotBehavior : "+config.id()+ "  name = "+config.name());
	    
	    worker = Executors.newFixedThreadPool(10);

	    Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
				.filter(e -> !e.getKey().startsWith("."))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			
			serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter", 
		    String.format("(|(robotID=%s)(robotID=%s))", config.id(), RobotCommand.ALL_ROBOTS));
			
			System.out.println("+++++++++ filter = "+serviceProps.get(SmartBehaviourDefinition.PREFIX_ + "filter"));
			reg = context.registerService(SmartBehaviour.class, this, serviceProps);

		this.robotID=config.id();
		
		trigerRobotBehaviour();

	}
    
    /*Consuming notification*/
    
	@Override
	public void notify(BrainIoTEvent event) {
		
		if (event instanceof RobotReadyResponse) {
			rbc = (RobotReadyResponse) event;
			worker.execute(() -> {
				System.out.println("-->RB" + robotID + " received RobotReady event");
				isRobotReady = rbc.isReady;
			});

		} else if (event instanceof NewPickPointResponse) {
			    pickResponse = (NewPickPointResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive NewPickPointResponse ");
				});

			} else if (event instanceof NewStoragePointResponse) {
				storageResponse = (NewStoragePointResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive NewStoragePointResponse ");
				});

			} else if (event instanceof DockingResponse) {
				dockingResponse = (DockingResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive DockingResponse ");
				});

			} else if (event instanceof CartNoticeResponse) {
				cartNoticeResponse = (CartNoticeResponse) event;


			} else if (event instanceof QueryStateValueReturn) {
				qs = (QueryStateValueReturn) event;


			} else if (event instanceof MarkerReturn) {
				cvr = (MarkerReturn) event;
				

			} else if (event instanceof DoorStatusResponse) {
						response = (DoorStatusResponse) event;
						if(response.currentState == State.OPEN) {
							System.out.println("-->RB" + robotID + " door is opened successfully!!!!");
						}
					

		} 	
	}
	
	
	
	public void trigerRobotBehaviour(){
		new sim07();
    	worker.execute(this::update);
	}
	
	
	private void update() {

		if(writeGoTo!=null) {
			eventBus.deliver(writeGoTo);
			writeGoTo=null;
		}
		if(pc!=null) {
			eventBus.deliver(pc);
			pc=null;
		}	
		if(placeCart!=null) {
			eventBus.deliver(placeCart);
			placeCart=null;
		}		
		if(cancel!=null) {
			eventBus.deliver(cancel);
			cancel=null;
		}		
		if(checkMarker!=null) {
			eventBus.deliver(checkMarker);
			checkMarker=null;
		}	
		if(cartMovedNotice!=null) {
			eventBus.deliver(cartMovedNotice);
			cartMovedNotice=null;
		}
		if(noCartNotice!=null) {
			eventBus.deliver(noCartNotice);
			noCartNotice=null;
		}
		if(pickpointrequest!=null) {
			eventBus.deliver(pickpointrequest);
			pickpointrequest=null;
		}
		if(doorStatusRequest!=null) {
			eventBus.deliver(doorStatusRequest);
			doorStatusRequest=null;
		}
		if(dockingRequest!=null) {
			eventBus.deliver(dockingRequest);
			dockingRequest=null;
		}		
		worker0.schedule(this::update, 1, TimeUnit.MILLISECONDS);
	}
	
	/* Delivered services*/
	static WriteGoTo writeGoTo;
	
	static PickCart pc;
	
	static PlaceCart placeCart;
	
	static Cancel cancel;
	
	static CheckMarker checkMarker;
	
	static CartMovedNotice cartMovedNotice;
	
	static NoCartNotice noCartNotice;
	
    static NewPickPointRequest pickpointrequest;
	
    static CheckRobotReadyRequest checkrobotreadyrequest;
    
    static DoorStatusRequest doorStatusRequest;
    
    static DockingRequest dockingRequest;
    
    
    /* send docking request*/
	public static void sendDockingRequest( ) {
		
		dockingRequest = new DockingRequest();
		
	}
	//=============================================    	    
    /* send open door*/
	public static void sendOpenDoorRequest( ) {
		
		doorStatusRequest = new DoorStatusRequest();
		doorStatusRequest.targetState = DoorStatusRequest.State.OPEN;
	}
	//=============================================    	
    /* send state door*/
	public static void sendStateDoorRequest( ) {
		
		doorStatusRequest = new DoorStatusRequest();
		doorStatusRequest.targetState = DoorStatusRequest.State.QUERY;
	}
	
	//=============================================    
    
	/* send pick point response*/
	public static void sendNewPickPointRequest( ) {
		
		pickpointrequest = new NewPickPointRequest();
		
		
	}
	//=============================================
	
	/* send check robot request*/
	public static void sendCheckRobotReadyRequest( ) {
		
		checkrobotreadyrequest = new CheckRobotReadyRequest();
		
	}
	//=============================================
	
	public static void sendGoto(double x,double y,double z) {
		
		Coordinate coordinate =new Coordinate(x,y,z);
		
		createWriteGoTo(coordinate); 
	}

	public static WriteGoTo createWriteGoTo(Coordinate coordinate) {
		writeGoTo= new WriteGoTo();
		writeGoTo.robotID=robotID;
		writeGoTo.coordinate = coordinate;
		return writeGoTo;
	}
	//==========================================================
	/*pick cart*/
	public static int pickcart(int markerID) {
		createPickCart(markerID);
		return 1;
	}
	public static  PickCart createPickCart(int markerID) {
		pc= new PickCart();
		pc.robotID=robotID;
		pc.markerID=markerID;
		return pc;
	}
	//=============================================================
	
	public static int placeCart( ) {
		createPlaceCart();
		
		return 1;
	}
	public static  PlaceCart createPlaceCart() {
		placeCart=new PlaceCart();
		placeCart.robotID=robotID;
		return placeCart;
	}
	
	
	public static  Cancel createCancel(Command command) {
		cancel=new Cancel();
		cancel.robotID=robotID;
		return cancel;
	}
	
	public static int checkMerker() {
		createCheckMarker() ;
		return 1;
	}
	public static  CheckMarker createCheckMarker() {
		checkMarker=new CheckMarker();
		checkMarker.robotID=robotID;
		return checkMarker;
	}
	
	public static int cartMovedNotice(int robotID) {
		createCartMovedNotice();
		return 1;
	}
	public static  CartMovedNotice createCartMovedNotice() {
		cartMovedNotice=new CartMovedNotice();
		cartMovedNotice.robotID=robotID;
		cartMovedNotice.pickPoint = pickResponse.pickPoint;
		return cartMovedNotice;
	}
	
	public  static int noCartNotice(double x,double y,double z){
		createNoCartNotice(x,y,z);
		return 1;
	}
	
	public static  NoCartNotice createNoCartNotice(double x,double y,double z) {
		noCartNotice=new NoCartNotice();
		noCartNotice.robotID=robotID;
		Coordinate coordinate =new Coordinate(x,y,z);
		noCartNotice.pickPoint = pickResponse.pickPoint;
		return noCartNotice;
	}
	
   
    
    

	


}
