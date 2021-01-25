package compound;

import org.lib.Command;
import atom.*;
import port.*;
import type.*;

import task.*;

import connector.*;

// Compound Definition;

@Task(time=5)
public class sim07 extends Compound { 

org.lib.Command Command = new org.lib.Command() ;
public sim07( ){ 	
 start();
 } 
@CompoundMethodName(name="init")
public void init( ){
robot_1= new Behavior(new Type<Integer>(1)) ; 

}
  public Behavior robot_1; 

}