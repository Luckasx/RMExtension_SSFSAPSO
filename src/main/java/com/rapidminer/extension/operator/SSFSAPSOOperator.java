package com.rapidminer.extension.operator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.mail.search.DateTerm;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.extension.jswarm_pso.Particle;
import com.rapidminer.extension.utils.SwarmLogger;
import com.rapidminer.extension.utils.UtilsERM;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;


public class SSFSAPSOOperator extends OperatorChain {

	//private InputPort exampleSetInput = getInputPorts().createPort("example set");
	//private InputPort exampleSetInput = getInputPorts().createPort("example set", ExampleSet.class);
	
	
	private OutputPort exampleSetOutput = getOutputPorts().createPort("extra example set");
	
	public static final String PARTICLES_AMOUNT_PARAMETER = "Particles Amount";
	
	public static final String LOOP_NUMBER_PARAMETER = "Loops number";
	
	public static final String MAX_MIN_VELOCITY = "Max/Min Velocity";
	
	public static final String INERTIA = "Inertia";
	
	
	//portas no subprocesso
	
	
	private final PortPairExtender inputPortPairExtender = 
		    new PortPairExtender("inputPortPairExtender", getInputPorts(), getSubprocess(0).getInnerSources()
		    		, new MetaData(ExampleSet.class));
	
	private final PortPairExtender  outExtender = 
		    new PortPairExtender (
		        "outExtender", getSubprocess(0).getInnerSinks(), getOutputPorts(), new MetaData(PerformanceVector.class));
	
	//private final PortPairExtender  outExtenderEX = 
		//    new PortPairExtender (
		  //      "outExtenderEX", getSubprocess(0).getInnerSinks(), getOutputPorts(), new MetaData(ExampleSet.class));
	
	
	
	//private OutputPort exampleSetOutput3 = getSubprocess(0). createPort("example set 2");
	
	public SSFSAPSOOperator(OperatorDescription description) {
		super(description, "Operator Master");
		// TODO Auto-generated constructor stub
		 //new comment
		//exampleSetInput.addPrecondition(
			//        new SimplePrecondition( exampleSetInput, new MetaData(ExampleSet.class) ));

		
		
		inputPortPairExtender.start();
		
		outExtender.start();
		
		//exampleSetOutput.
		
		//getTransformer().addRule(outExtender.makePassThroughRule());
		getTransformer().addRule(inputPortPairExtender.makePassThroughRule());
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        getTransformer().addRule(outExtender.makePassThroughRule());
        //getTransformer().addRule(outExtenderEX.makePassThroughRule());
        

        
	}
	
	public void doWork() throws OperatorException{
		
		int particles_amount = getParameterAsInt(PARTICLES_AMOUNT_PARAMETER);
		
		int loop_number = getParameterAsInt(LOOP_NUMBER_PARAMETER);
		
		double max_min_velocity = getParameterAsDouble(MAX_MIN_VELOCITY);
		
		double inertia_param = getParameterAsDouble(INERTIA);
		
		LogService.getRoot().log(Level.INFO,"Doing something..");
		LogService.getRoot().log(Level.INFO,"Supplied value for particles amount is: .." + particles_amount);
		
		
		ExampleSet exampleSetOriginal = null ;
		
		PerformanceVector bestPC = null;
		ExampleSet bestExampleSet = null ;
		
		try {
			
			
			List<ExampleSet> lex = inputPortPairExtender.getData(ExampleSet.class);
			exampleSetOriginal = lex.get(0);
			
			bestExampleSet = exampleSetOriginal;
			
			getProgress().setTotal(exampleSetOriginal.size()); 
			
			getProgress().setCompleted(0);
			
			int qtd_att_ex_original = exampleSetOriginal.getAttributes().allSize(); 
			
			double qtd_reg_attr_ex_original = qtd_att_ex_original - 1; //only regular attributes
			
			LogService.getRoot().log(Level.SEVERE, "Qtd de atributos:: " + qtd_att_ex_original);
			
			/*
			 * create the fitness function
			 */
			ExecutionUnit eu = getSubprocess(0);
			SwarmFSFunction ssFuncaoFitness = new SwarmFSFunction(eu , exampleSetOriginal,  inputPortPairExtender, outExtender);
			ssFuncaoFitness.setMaximize(true);
			
			//Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new MyParticle(), minhaFuncaoFitness);
			SwarmSearch swarm = new SwarmSearch(particles_amount, new SSParticle(qtd_att_ex_original), ssFuncaoFitness, (qtd_att_ex_original));
			swarm.setInertia(inertia_param);
			swarm.setMaxPosition(qtd_reg_attr_ex_original + 0.9999999999999);

			//at least 1 attribute
			swarm.setMinPosition(1);
			
			swarm.setMaxMinVelocity(max_min_velocity);
			
			swarm.setParticleIncrement(0.6); // beta learning parameter
			swarm.setGlobalIncrement(0.7); //alfa  learning parameter
			
			swarm.init();
			
			
			
			
			
			double previousBest = swarm.getBestFitness();
			
			swarm.log("The process started at: " + new Date().toString() + "\n", false);
			
			for(int i = 0; i <  loop_number ; i++) 
			{
				swarm.evolve();
				previousBest = swarm.getBestFitness();
			}
			
		
		    
			
			//get the best performance from 'myfunction' and deliver
				List<PerformanceVector> lbpfv = new ArrayList<PerformanceVector>();
				
				lbpfv.add(ssFuncaoFitness.getBestPC());
				
				outExtender.deliver(lbpfv);
				
				List<ExampleSet> lbpex = new ArrayList<ExampleSet>();
				
				
				LogService.getRoot().log(Level.SEVERE,"ssFuncaoFitness.getBestExampleSet() == null:: " +  (ssFuncaoFitness.getBestExampleSet() == null));
				
				
				exampleSetOutput.deliver(ssFuncaoFitness.getBestExampleSet());
				
				swarm.log("\nThe process ended at : " + new Date().toString(), true);
			
			}
		    catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LogService.getRoot().log(Level.SEVERE,"Catched error:: " + UtilsERM.getCustomStackTrace(e));
		}
		finally
		{
			if(exampleSetOriginal != null) {
				//exampleSetOutput.deliver(exampleSet);
			}
			
			
		}

		
	}
	
	/*
	 * Return an example set with a new attribute, just for tests
	 */
	public ExampleSet modifyExampleSet(ExampleSet exampleSetP)
	{
		
		ExampleSet exampleSetN = exampleSetP;
		// get attributes from example set
					Attributes attributes = exampleSetN.getAttributes();
					
					// create a new attribute
					String newName = "newAttribute";
					
					// define the name and the type of the new attribute
					// valid types are 
					// - nominal (sub types: binominal, polynominal, string, file_path)
					// - date_time (sub types: date, time)
					// - numerical (sub types: integer, real)
					Attribute targetAttribute = AttributeFactory.createAttribute(newName, Ontology.REAL);			
					targetAttribute.setTableIndex(attributes.size());
					exampleSetN.getExampleTable().addAttribute(targetAttribute);
					attributes.addRegular(targetAttribute);
					
					for(Example example:exampleSetN){
					    example.setValue(targetAttribute, Math.round(Math.random()*10+0.5));
					}
					
					return exampleSetN;
		
	}
	
	@Override
	public List<ParameterType> getParameterTypes(){
		List<ParameterType> types = super.getParameterTypes();

	    types.add(new ParameterTypeInt(
	        PARTICLES_AMOUNT_PARAMETER,
	        "This parameter defines the amount of particles to search for the best subset.",
	        1, 1000
	        ));
	    
	    types.add(new ParameterTypeInt(
		        LOOP_NUMBER_PARAMETER,
		        "This parameter defines the number of loops til stop.",
		        1, 100000
		        ));
	    
	    types.add(new ParameterTypeDouble(
		        MAX_MIN_VELOCITY,
		        "This parameter defines the velocity of the particle's change.",
		        0.001, 100000
		        ));
	    
	    types.add(new ParameterTypeDouble(
		        INERTIA,
		        "This parameter defines the inertia that affects velocity of the particle's change.",
		        0.0001, 1
		        ));
	    
	    
	    return types;
	}
}
