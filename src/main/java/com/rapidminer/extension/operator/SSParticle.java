package com.rapidminer.extension.operator;

import java.util.logging.Level;

import com.rapidminer.extension.jswarm_pso.Particle;
import com.rapidminer.tools.LogService;

/**
 * Simple particle example
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 */
public class SSParticle extends Particle {

	public SSParticle() throws Exception {
		throw new Exception("It is not possible to instantiate without the dimensions parameter yet.");
	}
	/** Default constructor */
	public SSParticle(int dimensions) {
		super(dimensions); // Create a n-dimentional particle
	}
	
	/**
	 * Initialize a particles's position and velocity vectors 
	 * @param maxPosition : Vector stating maximum position for each dimension
	 * @param minPosition : Vector stating minimum position for each dimension
	 * @param maxVelocity : Vector stating maximum velocity for each dimension
	 * @param minVelocity : Vector stating minimum velocity for each dimension
	 * @throws Exception 
	 */
	public void init(double maxPosition[], double minPosition[], double maxVelocity[], double minVelocity[])  {
		
		for (int i = 0; i < position.length; i++) {
			if (Double.isNaN(maxPosition[i])) throw new RuntimeException("maxPosition[" + i + "] is NaN!");
			if (Double.isInfinite(maxPosition[i])) throw new RuntimeException("maxPosition[" + i + "] is Infinite!");

			if (Double.isNaN(minPosition[i])) throw new RuntimeException("minPosition[" + i + "] is NaN!");
			if (Double.isInfinite(minPosition[i])) throw new RuntimeException("minPosition[" + i + "] is Infinite!");

			if (Double.isNaN(maxVelocity[i])) throw new RuntimeException("maxVelocity[" + i + "] is NaN!");
			if (Double.isInfinite(maxVelocity[i])) throw new RuntimeException("maxVelocity[" + i + "] is Infinite!");

			if (Double.isNaN(minVelocity[i])) throw new RuntimeException("minVelocity[" + i + "] is NaN!");
			if (Double.isInfinite(minVelocity[i])) throw new RuntimeException("minVelocity[" + i + "] is Infinite!");

			// Initialize using uniform distribution
			if(i > 0) {
				double newPosition = (maxPosition[i] - minPosition[i]) * Math.random() + minPosition[i];
				
				//check first whether the value is assigned to another
				int loops = 0;
				boolean containAlready = containElementAlready((int) newPosition, i ) && newPosition >= minPosition[0] && newPosition <= maxPosition[0]; 
				while( containAlready && loops <= 1000){
					
					//LogService.getRoot().log(Level.INFO, "Contain already in init :: " + newPosition + " Position.length:" + position.length);
					newPosition = (maxPosition[i] - minPosition[i]) * Math.random() + minPosition[i];
					containAlready = containElementAlready((int) newPosition, i) && newPosition >= minPosition[0] && newPosition <= maxPosition[0];
					loops++;
				}
				
				if(containAlready) {
					LogService.getRoot().log(Level.SEVERE, "It was not possible to create the particle.");
					
					for(double d : position) {
						LogService.getRoot().log(Level.WARNING, "Value in the positions::::" + d);	
					}
					
					System.exit(0);
					
				}
				
				position[i] = newPosition;			
			}
			else
			{
				position[i] = position.length - 1;
			}
			
			velocity[i] = (maxVelocity[i] - minVelocity[i]) * Math.random() + minVelocity[i];
			bestPosition[i] = Double.NaN;
			
		}
		
		this.sortPositions();
	}
	
	
	public void resize(int oldSize, int newSize, double maxPosition[], double minPosition[], double maxVelocity[], double minVelocity[]) {
		
		//LogService.getRoot().log(Level.INFO, "Resizing particle ::::... ");
		
		/*
		 * The size of position array must be at least 2
		 */
		double nuSize = newSize;
		
		boolean greaterThanMax = false;
		boolean lessThanMin = false;

		//randomize a new attribute size
		while(nuSize < 2 || nuSize > maxPosition[0]) {
			
			LogService.getRoot().log(Level.WARNING, "Nusize value::::" + nuSize + ". MaxPosition Value::: " + maxPosition[0]);
			
			 greaterThanMax = nuSize > maxPosition[0];
			 lessThanMin = nuSize < 2;
			 
			 if(greaterThanMax) {
				 position[0] -= (maxPosition[0] - minPosition[0]) * Math.random() + minPosition[0];
			 }
			 else if(lessThanMin) {
				 position[0] += (maxPosition[0] - minPosition[0]) * Math.random() + minPosition[0];
			 }
			 
			nuSize = position[0] + 1;			
		}
				
		
		
		
		newSize = (int) nuSize;
		
		int sizeDifference = newSize - oldSize;
		
		if( sizeDifference != 0) {
			
			double positionNew[] = new double[newSize];
			double velocityNew[] = new double[newSize];
			double bestPositionNew[] = new double[newSize];
			
				int qtdLoop = position.length;
				//	
				if(sizeDifference < 0) {
					qtdLoop = newSize;
				}
				
				//copy the current arrays to temp
				for(int i = 0; i < qtdLoop; i++) {
					//if(newSize >= position.length) {
						positionNew [i] = position[i];
						velocityNew [i] = velocity[i];
						bestPositionNew [i] = bestPosition[i];
					//}
				}
			
			
				double dmaxPosition = maxPosition[0];
				double dminPosition = minPosition[0];
				
			//if size difference is greater then adds a value
			for(int j = 1  ; j < newSize; j++) {
				
				//LogService.getRoot().log(Level.INFO, "Resizing particle ::::... j ::" + j);
				
					if(j >= oldSize) {
						
						positionNew [j] = (maxPosition[j] - minPosition[j]) * Math.random() + minPosition[j];
				
						velocityNew [j] = (maxVelocity[j] - minVelocity[j]) * Math.random() + minVelocity[j];
					}
					
					boolean flowSum = true;
					//check own elements to avoid repeat a value
					int loops = 0;
					boolean containAlready = containElementAlready(positionNew, (int) positionNew[j], j ) && positionNew[j] >= minPosition[0] && positionNew[j] <= maxPosition[0];
					
					while( containAlready && loops <= 1000){
						
						//LogService.getRoot().log(Level.INFO, "Contain already in resize :: " + positionNew[j] + " PositionNew.length:" + positionNew.length);
						positionNew[j] = (dmaxPosition - dminPosition) * Math.random() + dminPosition;
						
							while(positionNew[j] < dminPosition ) {
								//LogService.getRoot().log(Level.INFO, "Less than min : : " + positionNew[j] );
								positionNew[j] += (Math.abs(velocity[j]));
							}
							
							while(positionNew[j] > dmaxPosition ) {
								//LogService.getRoot().log(Level.INFO, "Greather than min : : " + positionNew[j] );
								positionNew[j] += (Math.abs(velocity[j]) * - 1);
							}
						
						
						containAlready = containElementAlready(positionNew, (int) positionNew[j], j) && positionNew[j] >= minPosition[0] && positionNew[j] <= maxPosition[0];;
						loops++;
					}
					
				
				//velocityNew [j] = (maxVelocity[j] - minVelocity[j]) * Math.random() + minVelocity[j];
				bestPositionNew[j] = Double.NaN;
			}
			
			//just for debug
			/*if(newSize < oldSize) {
				StringBuilder sb = new StringBuilder();
				sb.append("Resizing to less..\nOld array:::");
				for(double d : position)
				{
					sb.append(d).append(" " );
				}
				
				sb.append("\nNew array:::");
				for(double d : positionNew)
				{
					sb.append(d).append(" ");
				}
				LogService.getRoot().log(Level.INFO, sb.toString());
			
			}*/
			
			
						
			this.setPosition(positionNew);
			this.setVelocity(velocityNew);
			this.setBestPosition(bestPositionNew);
			
		}
		
		sortPositions();
		
	}
	
	public void sortPositions() {
		
		//LogService.getRoot().log(Level.INFO, "Sorting particle ::::... ");
		//iterate sorting
		for(int k = 1; k < position.length ; k++) {
			
			//LogService.getRoot().log(Level.INFO, "Sorting particle ::::... k ::" + k);
			
			for(int j = k + 1; j < position.length ; j++) {
				
				//LogService.getRoot().log(Level.INFO, "Sorting particle ::::... j ::" + j);
					
						double current 	= position[k];
						double next		= position[j];
						
						if(current  > next) {
							position[j] = current;
							position[k] = next;
							j = k;
							//LogService.getRoot().log(Level.INFO, "Loop inside sort::: Current / Next " +  current + "/" + next);
						}
					
			
			}
		}
	}
	
	
	public boolean containElementAlready(double value, int index) {
		
		boolean contain = false;
		
		int randomized = (int) value;
		
		//0 is not a selectable attribute
		if(randomized < 1) {
			return true;
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Contain already ? " + randomized);
		
		sb.append("\nPreexistent elements::: ");
		
		for(int k = 1; k < index; k++) {
			
			int stored = (int)position[k];

			sb.append(stored + "\t");
			
			// 0.0 means the value is not initialized
			if(stored == randomized || randomized == 0) {
				contain = true;
			}
			
		}
		sb.append("Result: -> " + contain + "\n");
		
		
		//LogService.getRoot().log(Level.INFO, sb.toString());
		
		return contain;
		
	}
	
	public boolean containElementAlready(double[] positionNew, double value, int index) {
		
		boolean contain = false;
		
		int randomized = (int) value;
		
		//0 is not a selectable attribute
				if(randomized < 1) {
					return true;
				}

		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Contain already ? " + randomized);
		
		sb.append("\nPreexistent elements::: ");
		
		for(int k = 1; k < index; k++) {
			
			int stored = (int)positionNew[k];

			sb.append(stored + "\t");
			
			// 0.0 means the value is not initialized
			if(stored == randomized || randomized == 0) {
				contain = true;
			}
			
		}
		sb.append("Result: -> " + contain + "\n");
		
		//LogService.getRoot().log(Level.INFO, sb.toString());
		
		return contain;
		
	}
}