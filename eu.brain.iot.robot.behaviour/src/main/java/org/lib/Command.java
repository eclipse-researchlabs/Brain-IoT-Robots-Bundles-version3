package org.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import eu.brain.iot.robot.api.Coordinate;
import eu.brain.iot.robot.behaviour.RobotBehaviour;
import eu.brain.iot.robot.events.QueryStateValueReturn;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest.State;




public class Command {

	
	int generate(){
		Random rand = new Random();
		return rand.nextInt() % 2;
	}
	
	public void finish(){

		System.out.println("         ************************************");
		System.out.println("         ****************  END  *************");
		System.out.println("         ************************************");

	}
	
	public void waitForTask(){
		int seconds=10;
		System.out.println("         ************************************");
		System.out.println("         *******  WAIT FOR 10 SECNODS  ******");
		System.out.println("         ************************************");
		try
		{ 
		    Thread.sleep(1000 * seconds);
		}
		catch(InterruptedException ex)
		{
		    Thread.currentThread().interrupt();
		}
	}
	
	void wait(int time){
		try {	
			Thread.sleep(time);
		} 
		catch (InterruptedException e) { }
	}
	
	/**
	 * Check robot if it is ready
	 * @param req
	 * @param robotID
	 * @return
	 */
	public int checkRobotReadyRequest(int req, int robotID){
		
		RobotBehaviour.sendCheckRobotReadyRequest();
		return 1;
		
	}
	/**
	 * check if the robot answer for ready
	 * @param req
	 * @param robotID
	 * @return
	 */
	public int robotReadyResponse(int req, int robotID){
		int isReady = 0;
		
		    int i=0;
			while(i==0) {
				if(RobotBehaviour.rbc!=null) {
					i=1;	
					System.out.print("--- WATING QueryState loop--- " + i);
				}
				wait(10);
			}
			if(RobotBehaviour.rbc.isReady){isReady=1;} 
			
			RobotBehaviour.rbc=null;
			
		return isReady;
	}
	
	/**
	 * send a pick point request
	 * @param req
	 * @param robotID
	 */
	public void sendNewPickPointRequest(int req, int robotID){

		RobotBehaviour.sendNewPickPointRequest();

	}
	
	/**
	 * Send a new pick point response 
	 * @param req
	 * @param robotID
	 * @return
	 */
	public int sendNewPickPointResponse(int req, int robotID){
		
		double x = 0;
		double y = 0;
		double z = 0;

		int isReady = 0;
		
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.pickResponse!=null) {
				i=1;	
				System.out.print("--- WATING PickResponse loop--- " + i);
			}
			wait(10);
		}
		
		if(RobotBehaviour.pickResponse.hasNewPoint){
			
			
			pickpoint=RobotBehaviour.pickResponse.pickPoint; hasnewtask=1; 
			
			coordinate=pickpoint;
			
			return 1;
			} 
		hasnewtask=0;
		RobotBehaviour.pickResponse=null; 
		
		return 0;//generate();
	}
	
	int hasnewtask;
	
	Coordinate coordinate;
	Coordinate coordinate2ND;
	
	Coordinate pickpoint;
	Coordinate storagePoint;
	
	Coordinate storageAuxilaryPoint;
	
	Coordinate coordinateDockingAuxilatyPoint;
	Coordinate coordinateDockingPoint;
	
	/**
	 * Send a goto to the robot
	 * @param r
	 * @param data the destination of the robot
	 * @return
	 */
	public int sendGoto(int r, int data){
		if(pickpoint!=null && data==1) {
			RobotBehaviour.sendGoto(pickpoint.getX(),pickpoint.getY(),pickpoint.getZ());
			
			//return 1;
		}
		if(storagePoint!=null && data==2) {
			RobotBehaviour.sendGoto(storagePoint.getX(),storagePoint.getY(),storagePoint.getZ());
			
			//return 1;
		}
		if(storageAuxilaryPoint!=null && data==3) {
			RobotBehaviour.sendGoto(storageAuxilaryPoint.getX(),storageAuxilaryPoint.getY(),storageAuxilaryPoint.getZ());
			
			//return 1;
		}
		if(coordinateDockingPoint!=null && data==4) {
			RobotBehaviour.sendGoto(coordinateDockingPoint.getX(),coordinateDockingPoint.getY(),coordinateDockingPoint.getZ());
			
			//return 1;
		}		
		if(coordinateDockingAuxilatyPoint!=null && data==5) {
			RobotBehaviour.sendGoto(coordinateDockingAuxilatyPoint.getX(),coordinateDockingAuxilatyPoint.getY(),coordinateDockingAuxilatyPoint.getZ());
			
			//return 1;
		}		
	
		int i=0;
		while(i==0) {
			if(RobotBehaviour.qs!=null) {
				i=1;	
				System.out.print("--- WATING QUERY STATE VALUE--- " + i);
			}
			wait(10);
		}
		if(RobotBehaviour.qs.currentState==QueryStateValueReturn.CurrentState.finished ) {
                    return 1;
		}
		
		return 0;//generate();
		
	}
	
	public int hasNewTask(int req, int robotID){
		
		return hasnewtask;//generate();
		
	}
	
	/**
	 * Check the marker in front of the robot
	 * @param req
	 * @param robotID
	 * @return
	 */
	public int checkMerker(int req, int robotID){
		RobotBehaviour.checkMerker();
		
		return markerReturn(req, robotID);
		
	}
	/**
	 * Wait for the marker return 
	 * @param req
	 * @param robotID
	 * @return
	 */
	public int markerReturn(int req, int robotID){
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.cvr!=null) {
				i=1;	
				System.out.print("--- WATING Marker Return loop--- " + i);
			}
			wait(10);
		}
		
		return RobotBehaviour.cvr.markerID;
	}
	
	public int cartMovedNotice(int req, int robotID){
		RobotBehaviour.cartMovedNotice(robotID);
		
		return 1;//generate();
		
	}
	public int noCartNotice(double x,double y,double z){
		RobotBehaviour.noCartNotice(x,y,z);
		return 1;//generate();
		
	}
	public int cartNoticeResponse( ){
		
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.cartNoticeResponse!=null) {
			
				i=1;	
				System.out.print("--- WATING Marker Return loop--- " + i);
			}
			wait(10);
		}
		
		return 1;//generate();
		
	}
	
	/**
	 * Pick cart order
	 * @param req
	 * @param MarkerID
	 * @return
	 */
	public int pickcart(int req, int MarkerID){
		
		RobotBehaviour.pickcart(MarkerID);
		
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.qs!=null) {
				i=1;	
				System.out.print("--- WATING Marker Return loop--- " + i);
			}
			wait(10);
		}
		if(RobotBehaviour.qs.currentState==QueryStateValueReturn.CurrentState.finished ) {
			
			//Get the storage coordinate points
			i=0;
			while(i==0) {
				if(RobotBehaviour.storageResponse!=null) {
					storagePoint = RobotBehaviour.storageResponse.storagePoint;
					
					storageAuxilaryPoint = RobotBehaviour.storageResponse.storageAuxliaryPoint;
					
					i=1;	
					System.out.print("--- WATING Marker Return loop--- " + i);
				}
				wait(10);
			}

			//set auxiliary points
			//coordinate =storagePoint;
			//coordinate2ND =storageAuxilaryPoint;
			
			return 1;
		}
		return 0;
		
	}

	/**
	 * Open a door request 
	 * @param req
	 * @param MarkerID
	 * @return
	 */
	public int openDoor(int req, int MarkerID){
		RobotBehaviour.sendOpenDoorRequest();
		
		RobotBehaviour.sendStateDoorRequest();
		
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.response!=null) {
				
				i=1;	
				System.out.print("--- WATING DOOR Return STATE--- " + i);
				
				if(RobotBehaviour.response.currentState == State.OPEN) {
					return 1;
				}
			}
			wait(10);
		}
		
		return 1;//generate();
		
	}
	
	public void pickCart(){
		
	}
	
	public void openDoor(){
		
	}
	
	/**
	 * place a cart and wait for the status
	 * @param req
	 * @param MarkerID
	 * @return
	 */
	public int placeCart(int req, int MarkerID){
		RobotBehaviour.placeCart();
		int i=0;
		while(i==0) {
			if(RobotBehaviour.qs!=null) {
				i=1;	
				System.out.print("--- WATING QUERY STATE VALUE--- " + i);
			}
			wait(10);
		}
		if(RobotBehaviour.qs.currentState==QueryStateValueReturn.CurrentState.finished ) {
                    return 1;
		}
		return 0;//generate();
		
	}
	
	/**
	 * send docking request
	 * @param req
	 * @param MarkerID
	 * @return
	 */
	public int dockingRequest(int req, int MarkerID){
		
		RobotBehaviour.sendDockingRequest();
		return MarkerID;
		
	}
	
	/**
	 * Get the docking response 
	 * @param req
	 * @param MarkerID
	 * @return
	 */
	public int dockingResponse(int req, int MarkerID){
		
	    int i=0;
		while(i==0) {
			if(RobotBehaviour.dockingResponse!=null) {
				
				coordinateDockingAuxilatyPoint= RobotBehaviour.dockingResponse.dockAuxliaryPoint;
				
				coordinateDockingPoint= RobotBehaviour.dockingResponse.dockingPoint;
				
				//coordinate = coordinateDockingPoint;
				
				//coordinate2ND = coordinateDockingAuxilatyPoint;

				
				i=1;	
				System.out.print("--- WATING DOCKING RESPONSE--- " + i);
			}
			wait(10);
		}
		return 1;//generate();
	}
	
	public  void write(int robot_id, String str){
		
		System.out.println("ROBOT :  "+ robot_id+ " "+str);
	}
	
	public  void writePosition(){
		
		System.out.println("ROBOT : SEND POSITION ");
		

	}
	
	public  void writeMarker(){
		
		System.out.println("ROBOT : SEND MARKER IN SIGHT ");
		
	}
	
	public  void writeAvailability( ){
		
		System.out.println("ROBOT : SEND AVAILABILITY ");
		
		
	}
	
	public  void writeGotoAdd( ){
		
		System.out.println("ROBOT : SEND GOTO ADD ");
	
		
	}
	
	public  void writeGotoCancel( ){
		
		System.out.println("ROBOT : SEND GOTO CANCEL ");

		
	}
	
	public  void writeGotoQueryState( ){
		
		System.out.println("ROBOT : SEND GOTO QUERY STATE ");

		
	}
	
	public  void writePickAdd( ){
		
		System.out.println("ROBOT : SEND PICK ADD ");

		
	}
	
	public static void writePickCancel( ){
		
		System.out.println("ROBOT : SEND PICK CANCEL ");

		
	}
	
	public  void writePickQueryState( ){
		
		System.out.println("ROBOT : SEND PICK QUERY STATE ");

		
	}
	

	public  void writePlaceAdd( ){
		
		System.out.println("ROBOT : SEND PLACE ADD ");

		
	}
	
	public  void writePlaceCancel( ){
		
		
		System.out.println("ROBOT : SEND PLCAE CANCEL ");

		
	}
	
	public  void writePlaceQueryState( ){
		
		System.out.println("ROBOT : SEND PLACE QUERY STATE ");

		
	}

	

	public  void writeChargeAdd( ){
		
		System.out.println("ROBOT : SEND CHARGE ADD ");

		
	}
	
	public  void writeChargeCancel( ){
		
		System.out.println("ROBOT : SEND CHARGE CANCEL ");
	
		
	}
}
