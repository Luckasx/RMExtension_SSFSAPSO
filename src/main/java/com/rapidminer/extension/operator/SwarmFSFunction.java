package com.rapidminer.extension.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.extension.jswarm_pso.FitnessFunction;
import com.rapidminer.extension.jswarm_pso.Particle;
import com.rapidminer.extension.utils.SwarmLogger;
import com.rapidminer.extension.utils.UtilsERM;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.tools.LogService;

public class SwarmFSFunction  extends FitnessFunction{

	ExecutionUnit euni;
	ExampleSet exampleSetOriginal;
	PortPairExtender inputPortPairExtenderF;
	PortPairExtender outExtenderF;
	PerformanceVector bestPC = null;
	
	int indexAttributeLabel = 0;

	ExampleSet bestExampleSet = null ;
	
	Attributes attsOriginal; 
	List<Attribute> latt;
	
	public SwarmFSFunction(ExecutionUnit eu, ExampleSet exOriginal, PortPairExtender inputPortPairExtender, 	PortPairExtender outExtender) {
		
		this.euni = eu;
		this.exampleSetOriginal = exOriginal;
		this.inputPortPairExtenderF = inputPortPairExtender;
		this.outExtenderF = outExtender;
		
		this.attsOriginal = exampleSetOriginal.getAttributes();
		
		Iterator<Attribute> iatt = attsOriginal.allAttributes();
		
		latt = new ArrayList<Attribute>(); 
		
		int k = 0;
		
		while(iatt.hasNext())
		{
			Attribute atemp = iatt.next();
			AttributeRole  amdtemp = attsOriginal.findRoleByName(atemp.getName()); 
			latt.add(atemp);
			
			String att_role = amdtemp.getSpecialName();
					
			LogService.getRoot().log(Level.INFO, String.format("Original attribute Position/Role/Name::::\t%d - %s -%s", k, att_role,  atemp.getName() ));
			
			if(att_role != null &&  att_role.equalsIgnoreCase("label")) {
				indexAttributeLabel = k;
				LogService.getRoot().log(Level.SEVERE, String.format("LABEL INDEX:: %d" , k));
			}
			
			k++;
		}
	}
	
	/**
	 * Evaluates a particles 
	 * @param particle : Particle to evaluate
	 * @return Fitness function for a particle
	 */
	public double evaluate(Particle particle, SwarmLogger slo) {
		double position[] = particle.getPosition();
		double fit = evaluate(position, slo);
		particle.setFitness(fit, this.isMaximize());
		return fit;
	}
	
	public double evaluate(double[] position) {
		
		return 0;
	}
	
	public double evaluate(double[] position,SwarmLogger slo) {
		
		try 
		{
		List<ExampleSet> lext = new ArrayList<ExampleSet>();
		
		ExampleSet exTemp = getExampleSet(position);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append( "---------------Temp Attributes List--------------------------\n");
		
		
		
		for(Attribute a : exTemp.getAttributes()) {
			sb.append(" | ").append(a.getName()).append(" | ");
		}
		slo.log(sb.toString(), false);
		//slo.log("---------------Temp Attributes List--------------------------\n");
		
		
		lext.clear();
		
		lext.add(exTemp);
	
		
		inputPortPairExtenderF.deliver(lext);
		
		euni.execute();
		
		  outExtenderF.passDataThrough();
		    
		    List<IOObject> lpfv = outExtenderF.getOutputData(IOObject.class);
		    
		    /**
		     * for(IOObject io : lpfv) {
		     
		    	LogService.getRoot().log(Level.INFO,"IOObject result :: " + io.toString());
		    }
		    */
		    
		    PerformanceVector pfv = (PerformanceVector) UtilsERM.getIOObjectFromIOList(lpfv, PerformanceVector.class);
		    
		    
		    sb.setLength(0);
		    
		    sb.append("---------------Performance Vector--------------------------" + "\n");
		    
		    
		    
		    for(int k = 0; k < pfv.size(); k++) {
		    	
		    	sb.append(pfv.getCriterion(k).getName()).append(" -> ").append(pfv.getCriterion(k).getAverage()).append("\n ");
		    			    	
		    }
		    slo.log( sb.toString(), false);
		    //LogService.getRoot().log(Level.SEVERE, "---------------Performance Vector--------------------------");
		
		    if(bestPC == null || bestPC.compareTo(pfv) < 0) {
		    	
	    		
	    		bestPC = pfv;
	    		bestExampleSet = exTemp;
	    	
		    }
		    
		    	return pfv.getCriterion("accuracy").getAverage();
		}
		catch(Exception e)
		{
			LogService.getRoot().log(Level.SEVERE,"Catched error:: " + UtilsERM.getCustomStackTrace(e));
		}

		return 0;
	}
	
	public ExampleSet getExampleSet(double[] position) {
		
		
		ExampleSet tempExSet = (ExampleSet)exampleSetOriginal.copy();
		
		Attributes atts = tempExSet.getAttributes();
		
		ArrayList<Integer> iposition = new ArrayList<Integer>();
		
		//the first position is the array size
		for(int i = 1; i< position.length; i++) {
			iposition.add((int)position[i]);
		}
		
		
		int qtd_atts_original = attsOriginal.allSize();
		
		//LogService.getRoot().log(Level.SEVERE, String.format("QTD ATRIBS ORIGINAL:: %d" , qtd_atts_original));
				
		for(int i = 1; i < qtd_atts_original ; i++) {
			
			int indexToRemove = i;
			
			if(indexAttributeLabel == indexToRemove) {
				//LogService.getRoot().log(Level.SEVERE, String.format("Pulando Index do Label:: %d" , indexToRemove));
				continue;
			}
			
			//if the label is the last, then remove the (i-1) attribute
			if(indexAttributeLabel == qtd_atts_original - 1)
			{
				indexToRemove = i-1;
			}
			//else it is not the first (0) attribute
			else if(indexAttributeLabel > 0) {
				
				indexToRemove = (i-1);
				
				if(indexAttributeLabel == indexToRemove) {
					indexToRemove = i;
				}
				
			}
				
			
			//LogService.getRoot().log(Level.WARNING, String.format("Loop to Remove attribute : %d - %d ", indexToRemove, qtd_atts_original));
			
			int index = iposition.indexOf(i);
			
			if(index == -1) {
				
				
			
				Attribute atemp = latt.get(indexToRemove);
			
				//
				//LogService.getRoot().log(Level.WARNING, String.format("to Removing attribute : %d - %s ", indexToRemove, atemp.getName()));
			
				tempExSet.getAttributes().remove(atemp);
				
			}
			else {
				
				Attribute atemp = latt.get(indexToRemove);
				//LogService.getRoot().log(Level.WARNING, String.format("NOT Removing attribute : %d - %s ", indexToRemove, atemp.getName()));
			}
		}
		
		//LogService.getRoot().log(Level.WARNING, "Attributes contain LABEL ??: " + tempExSet.getAttributes().getLabel());
		//tempExSet.
		return tempExSet;
		
	}
	
	public PerformanceVector getBestPC() {
		return bestPC;
	}

	public ExampleSet getBestExampleSet() {
		return bestExampleSet;
	}

}
