package main;

public class Stats
{
	//how long did the round take all in all
	//in ms
	//static long time; not needed, see next one
	
	//this will differentiate P1 and P2's time
	//like the chess clock
	//so both numbers should add up to TIME
	//in hindsight, this would mean that the total time should be calculated
	//rather than measured.
	static long timeSpendP1, timeSpendP2;
	
	// who often the phase actually occured
	// however, considering that christian will add a button
	// so that one doesnt have to shoot in attack phase and can skip it
	// this stat should count how often a player calls shoot();
	int attackedP1, attackedP2;
	
	// Similar but different from previous.
	// Because not all units fire 1 projectile
	int projsFiredP1, projsFiredP2;
	
	// How far did a player move in the round ?
	// use PIXELS_PER_METER to calculate "meters"
	int metersDrivenP1, metersDrivenP2;
	
}
