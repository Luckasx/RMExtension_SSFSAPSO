package com.rapidminer.extension.operator;

import java.util.ArrayList;
import java.util.logging.Level;

import com.rapidminer.extension.jswarm_pso.Particle;
import com.rapidminer.extension.jswarm_pso.ParticleUpdate;
import com.rapidminer.extension.jswarm_pso.Swarm;
import com.rapidminer.tools.LogService;

public class ParticleSwarmUpdate extends ParticleUpdate {

	/** Random vector for local update */
	double rlocal[];
	/** Random vector for global update */
	double rglobal[];
	/** Random vector for neighborhood update */
	double rneighborhood[];

	/**
	 * Constructor 
	 * @param particle : Sample of particles that will be updated later
	 */
	public ParticleSwarmUpdate(Particle sampleParticle) {
		super(sampleParticle);
		rlocal = new double[sampleParticle.getDimension()];
		rglobal = new double[sampleParticle.getDimension()];
		rneighborhood = new double[sampleParticle.getDimension()];
	}

	/** 
	 * This method is called at the begining of each iteration
	 * Initialize random vectors use for local and global updates (rlocal[] and rother[])
	 */
	@Override
	public void begin(Swarm swarm) {
		int i, dim = swarm.getSampleParticle().getDimension();
		for (i = 0; i < dim; i++) {
			rlocal[i] = Math.random();
			rglobal[i] = Math.random();
			rneighborhood[i] = Math.random();
		}
	}

	/** This method is called at the end of each iteration */
	@Override
	public void end(Swarm swarm) {
	}

	/** Update particle's velocity and position */
	@Override
	public void update(Swarm swarm, Particle particle) {
		
		//LogService.getRoot().log(Level.INFO, "updating particle");
		
		SSParticle sparticle = (SSParticle) particle;
		
		double position[] = particle.getPosition();
		
		int sizeBeforeUpdate = ((int)position[0]) + 1;
		
		double velocity[] = particle.getVelocity();
		double globalBestPosition[] = swarm.getBestPosition();
		double particleBestPosition[] = particle.getBestPosition();
		double neighBestPosition[] = swarm.getNeighborhoodBestPosition(particle);
		
		double maxPosition = swarm.getMaxPosition()[0];
		double minPosition = swarm.getMinPosition()[0];

		// Update velocity and position
		for (int i = 0; i < position.length; i++) {
			
			//LogService.getRoot().log(Level.INFO, "Updating particle ::::... i ::" + i);
			
			if(Double.isNaN(particleBestPosition[i])) {
				
				if(!Double.isNaN(globalBestPosition[i])) {
					particleBestPosition[i] = globalBestPosition[i];
				}
			}
			
			// Update velocity with PSO
			/**
			 *
			velocity[i] = swarm.getInertia() * velocity[i] // Inertia
					+ rlocal[i] * swarm.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local best
					+ rneighborhood[i] * swarm.getNeighborhoodIncrement() * (neighBestPosition[i] - position[i]) // Neighborhood best					
					+ rglobal[i] * swarm.getGlobalIncrement() * (globalBestPosition[i] - position[i]); // Global best

			// Update position with PSO
			position[i] += velocity[i];
			
			*/
			
			
			//Update position with APSO
			double global_i  = swarm.getGlobalIncrement();
			double particle_i  = swarm.getParticleIncrement();
			
			position[i] = (1 - particle_i) * position[i] + (particle_i * globalBestPosition[i]) + (global_i * (Math.random()));
			
			double maxPos = swarm.getMaxPosition()[0];
			
			//if i == 0 then loop until be greater than 1 and less or equal max 
			while(i == 0 && (position[i] < 1 || position[i] > maxPos)) {
				
				LogService.getRoot().log(Level.INFO, "Checking position[0] " + position[0] + "Max position[0]:::" + maxPos + "Velocity[i]: " + velocity[i]);
				
						if(position[i] < 1)
						{
							position[i] = 1;
						}
						else if(position[i] > maxPos) {
							position[i] = maxPos;
						}
						
			}
			
			//iterates over the previous items to avoid repeated attributes
			//SSParticle ssparticle = (SSParticle)particle;
			
			boolean flowSum = true;  //true + false -
					
			int loops = 0;
			boolean containAlready = sparticle.containElementAlready((int) position[i], i ) && position[i] >= minPosition && position[i] <= maxPosition;
			
			while(containAlready && loops <= 1000){
				
				//LogService.getRoot().log(Level.INFO, "Contain already in update :: " + position[i] + " Position.length:" + position.length);
				position[i] = (maxPosition - minPosition) * Math.random() + minPosition;
				
					while(position[i] < minPosition ) {
						LogService.getRoot().log(Level.INFO, "Less than min : : " + position[i] );
						position[i] += (Math.abs(velocity[i]));
					}
					
					while(position[i] > maxPosition ) {
						LogService.getRoot().log(Level.INFO, "Greather than min : : " + position[i] );
						position[i] += (Math.abs(velocity[i]) * - 1);
					}
				
				
				containAlready = sparticle.containElementAlready((int) position[i], i) && position[i] >= minPosition && position[i] <= maxPosition;;
				loops++;
			}
			
		}
		
		
		validateParticle(sparticle, sizeBeforeUpdate, swarm);		
		
	}
	
	public void validateParticle(SSParticle ssparticle, int sizeBeforeUpdate, Swarm swarm) {
		
		
		double position[] = ssparticle.getPosition();
		
		//LogService.getRoot().log(Level.INFO, "Validating particle :::: Previous size: " + sizeBeforeUpdate + ". New size ::: " + (((int)position[0]) + 1) + " Position[0]:::" + position[0]);
		
		
		//if the size is different , then resize the position and velocities array 
				int sizeDifference = ((int) position[0])  + 1 - sizeBeforeUpdate;  
				
				
				
				if(sizeDifference != 0 ) {
					ssparticle.resize(sizeBeforeUpdate, ((int)position[0]) + 1, swarm.getMaxPosition(), swarm.getMinPosition(),swarm.getMaxVelocity(), swarm.getMinVelocity());
				}
				
				
		ssparticle.sortPositions();
		
	}
	
	
	
	
	
}
