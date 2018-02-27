package com.rapidminer.extension.operator;

import java.util.Date;
import java.util.logging.Level;

import com.rapidminer.extension.jswarm_pso.Particle;
import com.rapidminer.extension.jswarm_pso.Swarm;
import com.rapidminer.extension.utils.SwarmLogger;
import com.rapidminer.extension.utils.UtilsERM;
import com.rapidminer.tools.LogService;

public class SwarmSearch extends Swarm{

	/** the number of attributes of the example set except the label attribute **/ 
	int numberOfAttributes;
	
	long swarmTimeStamp;
	
	SwarmLogger slo = new SwarmLogger(String.valueOf(new Date().getTime()), true);
	
	/** Fitness function for this swarm */
	protected SwarmFSFunction fitnessFunction;
	
	public SwarmSearch(int numberOfParticles, Particle sampleParticle, SwarmFSFunction fitnessFunction, int numberOfAttributes) {
		super(numberOfParticles, sampleParticle, fitnessFunction);
		this.numberOfAttributes = numberOfAttributes;
		// TODO Auto-generated constructor stub
		this.swarmTimeStamp = new Date().getTime();
		this.particleUpdate = new ParticleSwarmUpdate(sampleParticle);
		this.fitnessFunction = fitnessFunction;
	}
	
	public double getMinPositionZero() {
		return this.minPosition[0];
	}

	@Override
	public void init() {
		// Init particles
		particles = new Particle[numberOfParticles];

		// Check constraints (they will be used to initialize particles)
		if (maxPosition == null) throw new RuntimeException("maxPosition array is null!");
		if (minPosition == null) throw new RuntimeException("maxPosition array is null!");
		if (maxVelocity == null) {
			// Default maxVelocity[]
			int dim = sampleParticle.getDimension();
			maxVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				maxVelocity[i] = (maxPosition[i] - minPosition[i]) / 2.0;
		}
		if (minVelocity == null) {
			// Default minVelocity[]
			int dim = sampleParticle.getDimension();
			minVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				minVelocity[i] = -maxVelocity[i];
		}

		/** min 2 max numberOfattributes **/
		int min = 2;
		int range = numberOfAttributes - min + 1 ;
		// Init each particle
		for (int i = 0; i < numberOfParticles; i++) {
			
			int rand = (int) (Math.random() * range + min);
				
			
			particles[i] = (Particle) new SSParticle((int)rand); // Create a new particles (using 'sampleParticle' as reference)
			particles[i].init(maxPosition, minPosition, maxVelocity, minVelocity); // Initialize it
		}

		// Init neighborhood
		if (neighborhood != null) neighborhood.init(this);
		
		//logParticles();
	}
	
	
	/**
	 * Update every particle's position and velocity, also apply position and velocity constraints (if any)
	 * Warning: Particles must be already evaluated
	 */
	public void update() {
		// Initialize a particle update iteration
		particleUpdate.begin(this);

		// For each particle...
		for (int i = 0; i < particles.length; i++) {
			// Update particle's position and speed
			particleUpdate.update(this, particles[i]);

			// Apply position and velocity constraints
			particles[i].applyConstraints(minPosition, maxPosition, minVelocity, maxVelocity);
		}

		// Finish a particle update iteration
		particleUpdate.end(this);
		
		//log the modified particles
		//logParticles();
		
	}
	
	
	/**
	 * Evaluate fitness function for every particle 
	 * Warning: particles[] must be initialized and fitnessFunction must be set
	 */
	public void evaluate() {
		if (particles == null) throw new RuntimeException("No particles in this swarm! May be you need to call Swarm.init() method");
		if (fitnessFunction == null) throw new RuntimeException("No fitness function in this swarm! May be you need to call Swarm.setFitnessFunction() method");

		// Initialize
		if (Double.isNaN(bestFitness)) {
			bestFitness = (fitnessFunction.isMaximize() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			bestParticleIndex = -1;
		}

		//---
		// Evaluate each particle (and find the 'best' one)
		//---
		for (int i = 0; i < particles.length; i++) {
			
			
			//log particle before evaluation
			StringBuilder sb = new StringBuilder();
				sb.append("Particle(" + i +") size: "+  particles[i].getDimension());
				sb.append("\nParticle(" + i + ") toString: " + particles[i].toString());
			this.log(sb.toString(), false);
			
			
			// Evaluate particle
			double fit = fitnessFunction.evaluate(particles[i], this.slo);

			numberOfEvaliations++; // Update counter

			// Update 'best global' position
			if (fitnessFunction.isBetterThan(bestFitness, fit)) {
				bestFitness = fit; // Copy best fitness, index, and position vector
				bestParticleIndex = i;
				if (bestPosition == null) bestPosition = new double[sampleParticle.getDimension()];
				particles[bestParticleIndex].copyPosition(bestPosition);
			}

			// Update 'best neighborhood' 
			if (neighborhood != null) {
				neighborhood.update(this, particles[i]);
			}

		}
	}
	
	
	public void logParticles() 
	{
		
		Particle[] allParticles = this.getParticles();
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < allParticles.length; i++) {
			
			sb.append("Particle(" + i +") size: "+  allParticles[i].getDimension());
			sb.append("\nParticle(" + i + ") toString: " + allParticles[i].toString());
		}
		
		this.log(sb.toString(), false);
	}
	
	public void log(String msg, boolean forced) {
		try 
		{
			slo.log(msg, forced);
		}
		catch(Exception e)
		{
			LogService.getRoot().log(Level.SEVERE,"Catched error:: " + UtilsERM.getCustomStackTrace(e));
		}
	}

}
