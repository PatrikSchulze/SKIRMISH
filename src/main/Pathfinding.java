package main;

import main.Unit.TYPE;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
//import java.util.ArrayList;
import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.utils.Array;

public class Pathfinding
{
	private short unitArea;
	private short targetArea;
	private boolean avoidUnit = true;
	private boolean aStar = false;
	private String mapName;	
	private GameMap actualMap;
	private Unit aiUnit, actualTarget;
	
	//Constructor
	public Pathfinding (GameMap gameMap, Unit ctrlUnit, Unit aiTarget, String mapType)
	{
		actualMap = gameMap;
		aiUnit = ctrlUnit;
		actualTarget = aiTarget;
		mapName = mapType;	
	}
	
	// linear equation to get y of x
	private float aiLinearEquation(Vector2 startPoint, Vector2 endPoint, float x_check)
	{
		float m = (endPoint.y - startPoint.y)/(endPoint.x - startPoint.x); //slope
		float n = startPoint.y - (m * startPoint.x); //shift
		float y = (m * x_check) + n; //equation
		return y;
	}//aiLinearEquation() END
	
	// rotation of any point on origin point
	private Vector2 aiRotation(Vector2 originPoint, Vector2 pointToRotate, float rotationAngle)
	{
	/*	formula rotation point2 around point1
	 * 	P1 (x1|y1) -> origin
	 *	P2 (x2|y2) -> to rotate
	 * 	x2' = x1 + (x2 - x1) * cos('angle') - (y2 - y1) * sin('angle')
	 *	y2' = y1 + (x2 - x1) * sin('angle') + (y2 - y1) * cos('angle')
	 *	P2'(x2'|y2') -> rotated	*/
		Vector2 rotatedPoint = new Vector2();
		rotatedPoint.set((float) (originPoint.x + (pointToRotate.x - originPoint.x) * MathUtils.cos(rotationAngle) - (pointToRotate.y - originPoint.y) * MathUtils.sin(rotationAngle)),
				(float) (originPoint.y + (pointToRotate.x - originPoint.x) * MathUtils.sin(rotationAngle) + (pointToRotate.y - originPoint.y) * MathUtils.cos(rotationAngle)));
		return rotatedPoint;
	}//aiRotation() END
		
	// obstacle collision test from start to end point gives back first collision and point of
	private float[] aiObstTest(Vector2 unit1, Vector2 unit2)
	{
		float xToCheck = unit1.x;
		float yToCheck = -1;
		boolean obstColl = false;
		int obstNumber = -1;
		
		if(unit1.x > unit2.x)
		{
			while(xToCheck > unit2.x && obstColl == false)
			{
				yToCheck = aiLinearEquation(unit1, unit2, xToCheck);
				for(int i=0 ; i<actualMap.obstacles.size() && obstColl != true ; i++)
				{
					obstColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
					obstNumber = i;
				}
				
				xToCheck = xToCheck - 0.1f;
			}
		}
		else if(unit1.x < unit2.x)
		{
			while(xToCheck < unit2.x && obstColl == false)
			{
				yToCheck = aiLinearEquation(unit1, unit2, xToCheck);
				for(int i=0 ; i<actualMap.obstacles.size() && obstColl != true ; i++)
				{
					obstColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(xToCheck, yToCheck); //getCollRect().contains(xToCheck, yToCheck);
					obstNumber = i;
				}
				
				xToCheck = xToCheck + 0.1f;
			}
		}
		else if(unit1.x == unit2.x)
		{
			yToCheck = unit1.y;
			xToCheck = unit1.x;
			if(unit1.y > unit2.y)
			{
				while(yToCheck > unit2.y && obstColl == false)
				{
					for(int i=0; i<actualMap.obstacles.size() && obstColl != true; i++)
					{
						obstColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
						obstNumber = i;
					}
					yToCheck = yToCheck - 0.1f;
				}
			}
			else if(unit1.y < unit2.y)
			{
				while(yToCheck < unit2.y && obstColl == false)
				{
					for(int i=0; i<actualMap.obstacles.size() && obstColl != true; i++)
					{
						obstColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
						obstNumber = i;
					}
					yToCheck = yToCheck + 0.1f;
				}
			}
		}
		float obstPoint[] = new float[3];
		obstPoint[0] = obstNumber;
		obstPoint[1] = xToCheck;
		obstPoint[2] = yToCheck;
		return obstPoint;
	}//aiObstTest() END
	
	// unit collision test checks units on way from start to end point, gives back index -1 if no collisions detected
	private float[] aiUnitTest(Vector2 startPoint, Vector2 endPoint)
	{
		float xToCheck = startPoint.x;
		float yToCheck = -1;
		boolean unitColl = false;
		short unitNumber = -1;
		
		if(startPoint.x > endPoint.x)
		{
			while(xToCheck > endPoint.x && unitColl == false)
			{
				yToCheck = aiLinearEquation(startPoint, endPoint, xToCheck);
				for(short i=0 ; i<actualMap.units.size() && unitColl != true ; i++)
				{
					unitColl = actualMap.units.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
					if (actualMap.units.get(i).equals(aiUnit) || actualMap.units.get(i).equals(actualTarget) || actualMap.units.get(i).type == TYPE.COPTER)
					{
						unitColl = false;
					}
					unitNumber = i;
				}
				xToCheck = xToCheck - 0.1f;
			}
		}
		else if(startPoint.x < endPoint.x)
		{
			while(xToCheck < endPoint.x && unitColl == false)
			{
				yToCheck = aiLinearEquation(startPoint, endPoint, xToCheck);
				for(short i=0 ; i<actualMap.units.size() && unitColl != true ; i++)
				{
					unitColl = actualMap.units.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
					if (actualMap.units.get(i).equals(aiUnit) || actualMap.units.get(i).equals(actualTarget) || actualMap.units.get(i).type == TYPE.COPTER)
					{
						unitColl = false;
					}
					unitNumber = i;
				}
				xToCheck = xToCheck + 0.1f;
			}
		}
		else if(startPoint.x == endPoint.x)
		{
			yToCheck = startPoint.y;
			xToCheck = startPoint.x;
			if(startPoint.y > endPoint.y)
			{
				while(yToCheck > endPoint.y && unitColl == false)
				{
					for(short i=0; i<actualMap.units.size() && unitColl != true; i++)
					{
						unitColl = actualMap.units.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
						if (actualMap.units.get(i).equals(aiUnit) || actualMap.units.get(i).equals(actualTarget) || actualMap.units.get(i).type == TYPE.COPTER)
						{
							unitColl = false;
						}
						unitNumber = i;
					}
					yToCheck = yToCheck - 0.1f;
				}
			}
			else if(startPoint.y < endPoint.y)
			{
				while(yToCheck < endPoint.y && unitColl == false)
				{
					for(short i=0; i<actualMap.units.size() && unitColl != true; i++)
					{
						unitColl = actualMap.units.get(i).getBoundingRectangle().contains(xToCheck, yToCheck);
						if (actualMap.units.get(i).equals(aiUnit) || actualMap.units.get(i).equals(actualTarget) || actualMap.units.get(i).type == TYPE.COPTER)
						{
							unitColl = false;
						}
						unitNumber = i;
					}
					yToCheck = yToCheck + 0.1f;
				}
			}
		}
		float unitPoint[] = new float[3];
		if (unitColl == true)
		{
//System.out.println("unit on way " + unitColl + unitNumber);
			unitPoint[0] = unitNumber;
		}
		else
		{
//System.out.println("NO unit on way");
			unitPoint[0] = -1.0f;		
		}
		unitPoint[1] = xToCheck;
		unitPoint[2] = yToCheck;
		return unitPoint;
	}//aiUnitTest() END
	
	// gives Point ahead of another point
	private Vector2 aiGivePointAhead(Vector2 startPoint, Vector2 endPoint, float distanceAhead)
	{
		float xToCheck = startPoint.x, yToCheck = -1.0f;
		boolean pointFound = false;
		Vector2 toCheck = new Vector2();
		
		if(startPoint.x > endPoint.x)
		{
			while(xToCheck > endPoint.x && pointFound == false)
			{
				yToCheck = aiLinearEquation(startPoint, endPoint, xToCheck);
				toCheck.set(xToCheck, yToCheck);
				if(aiGetDistance(toCheck, endPoint) > distanceAhead)
				{
					xToCheck = xToCheck - 0.1f;
				}
				else
					pointFound = true;
			}
		}
		else if(startPoint.x < endPoint.x)
		{
			while(xToCheck < endPoint.x && pointFound == false)
			{
				yToCheck = aiLinearEquation(startPoint, endPoint, xToCheck);
				toCheck.set(xToCheck, yToCheck);
				if(aiGetDistance(toCheck, endPoint) > distanceAhead)
				{
					xToCheck = xToCheck + 0.1f;
				}
				else
					pointFound = true;
			}
		}
		else if(startPoint.x == endPoint.x)
		{
			yToCheck = startPoint.y;
			xToCheck = startPoint.x;
			if(startPoint.y > endPoint.y)
			{
				while(yToCheck > endPoint.y && pointFound == false)
				{
					toCheck.set(xToCheck, yToCheck);
					if(aiGetDistance(toCheck, endPoint) > distanceAhead)
					{
						yToCheck = yToCheck - 0.1f;
					}
					else
						pointFound = true;
				}
			}
			else if(startPoint.y < endPoint.y)
			{
				while(yToCheck < endPoint.y && pointFound == false)
				{
					toCheck.set(xToCheck, yToCheck);
					if(aiGetDistance(toCheck, endPoint) > distanceAhead)
					{
						yToCheck = yToCheck + 0.1f;
					}
					else
						pointFound = true;
				}
			}
		}
		if(pointFound)
		{
			Vector2 aheadPoint = new Vector2(xToCheck, yToCheck);
			//System.out.println("aheadPoint found" + aheadPoint.x);
			return aheadPoint;
		}
		else
		{
			Vector2 aheadPoint = new Vector2(startPoint.x + 77.0f, startPoint.y + 77.0f);
			//System.out.println("NO aheadPoint found" + aheadPoint.x);
			return aheadPoint;
		}
		
	}//aiGivePointAhead() END

	// definition of map areas 
	private short aiGiveArea(Unit _Unit, String _map)
	{
		short area = 0;
		
		if(_map == "field")
		{
			if(_Unit.centerX <= 750 && _Unit.centerY <= 800)
				area = 1;
			else if(_Unit.centerX <= 1600 && _Unit.centerY <= 800)
				area = 2;
			else if(_Unit.centerX <= 2500 && _Unit.centerY <= 800)
				area = 3;
			else if(_Unit.centerX <= 750 && _Unit.centerY > 800)
				area = 4;
			else if(_Unit.centerX <= 1600 && _Unit.centerY > 800)
				area = 5;
			else if(_Unit.centerX <= 2500 && _Unit.centerY > 800)
				area = 6;
		}
		
		else if(_map == "snow")
		{
			if(_Unit.centerX <= 1200 && _Unit.centerY <= 825)
				area = 1;
			else if(_Unit.centerX <= 2500 && _Unit.centerY <= 825)
				area = 2;
			else if(_Unit.centerX <= 1200 && _Unit.centerY <= 1250)
				area = 3;
			else if(_Unit.centerX <= 2500 && _Unit.centerY <= 1250)
				area = 4;
			else if(_Unit.centerX <= 1200 && _Unit.centerY <= 2000)
				area = 5;
			else if(_Unit.centerX <= 2500 && _Unit.centerY <= 2000)
				area = 6;
		}
		
		else if(_map == "desert")
		{
			if(_Unit.centerX <= 575 && _Unit.centerY <= 1000)
				area = 1;
			else if(_Unit.centerX <= 1800 && _Unit.centerY <= 1000)
				area = 2;
			else if(_Unit.centerX <= 2500 && _Unit.centerY <= 1000)
				area = 3;
			else if(_Unit.centerX <= 575 && _Unit.centerY > 1000)
				area = 4;
			else if(_Unit.centerX <= 1800 && _Unit.centerY > 1000)
				area = 5;
			else if(_Unit.centerX <= 2500 && _Unit.centerY > 1000)
				area = 6;
		}
		
		return area;
	} //aiGiveArea() END
	
	// boolean check of any obstacles between unit and point
	private boolean aiCheckObst(Unit aiUnit, Vector2 shift)
	{
		boolean obst;
		Vector2 unit = new Vector2(aiUnit.centerX, aiUnit.centerY);
		if(aiObstTest(unit, shift)[0] < actualMap.obstacles.size()-1)
			obst = true;
		else
			obst = false;
		
		return obst;
	} //aiCheckObst END
	
	// checks if way to shift point is free, if not gives back best point
	private Vector2 aiInnerFree(Unit _aiUnit, Vector2 _shift, Vector2 _best)
	{
		Vector2 _freedom = new Vector2();
		Vector2 _unit = new Vector2(_aiUnit.centerX, _aiUnit.centerY);
		if(aiCheckObst(_aiUnit, _shift) && aiGetDistance(_unit, _best) > 5)
		{
			_freedom = _best;
			//System.out.println("WAY to shift is blocked go to best " + _best.x);
		}
		else
		{
			_freedom = _shift;
			//System.out.println("WAY to shift is free " + _shift.x);
		}
		return _freedom;
	}
	
	// find way around other units
	private Vector2 aiIsAnyBodyOutThere(Vector2 _point)
	{
		Unit unitOnWay = null;
		Vector2 _onWayU = new Vector2();
		Vector2 _freepoint = new Vector2();
		Vector2 _aheadPoint = new Vector2();
		Vector2 _ctrlU = new Vector2(aiUnit.centerX, aiUnit.centerY);
		int unitNumber = -1;
		//iterate way from ctrlUnit to free point and gives number of next unit on this way
		unitNumber = (int)aiUnitTest(_ctrlU, _point)[0];
		if (unitNumber==-1 || unitNumber >= actualMap.units.size()) // no unit on way
		{
			unitOnWay = null;
			//System.out.println("no other unit on way...");
			_freepoint.set(_point);
		}
		else //unit on way
		{
			unitOnWay = actualMap.units.get(unitNumber);
			_onWayU.set(unitOnWay.centerX, unitOnWay.centerY);
			//System.out.println("next unit on way is a " + unitOnWay.type);			
			// point 150 in front of unit on way
			_aheadPoint.set(aiGivePointAhead(_ctrlU, _onWayU, 150.0f));		
			// move to point aheadXY if distance bigger than 150
			if (aiGetDistance(_ctrlU, _onWayU) > 150.0f)
			{
				_freepoint.set(_aheadPoint);
			}
			else
			{
				// case - point rotated 90
				_freepoint.set(aiRotation(_onWayU, _aheadPoint, 1.5707f));
				if (aiIsObst(_freepoint.x, _freepoint.y)) // point on obstacle? -> rotate in other direction
				{
					_freepoint.set(aiRotation(_onWayU, _aheadPoint, -1.5707f));
					if (aiIsObst(_freepoint.x, _freepoint.y)) // both +&- 90 degree angle is on obstacle 
					{
						_freepoint.set(aiRotation(_onWayU, _aheadPoint, 3.14f));
						for (float angle = 1.6f; !aiIsObst(_freepoint.x, _freepoint.y) && angle >= 3.1f; angle += 0.1f)
						{
							_freepoint.set(aiRotation(_onWayU, _aheadPoint, angle));
							if (aiIsObst(_freepoint.x, _freepoint.y))
							{
								_freepoint.set(aiRotation(_onWayU, _aheadPoint, -angle));
							}
						} 
					}
				}
			}	
		}
		// is unit on point?
		if(aiIsUnit(_freepoint.x, _freepoint.y))
		{
			_freepoint.set(actualTarget.centerX , actualTarget.centerY);		
			//System.out.println("finish point is bloKKed... " + _freepoint.x);
		}
		//System.out.println("freepoint: " + _freepoint.x);
		return _freepoint;
	} // aiAnyBodyOutThere() END

	// aStar grid
	private Rectangle[][] aStarGrid()
	{
		Rectangle grid[][] = new Rectangle[25][21];
		float x=0.0f, y=0.0f;
		int iX=0,iY=0;
		for(iX=0;iX<25;iX++)
		{
			grid[iX][iY] = new Rectangle(x, y, 100.0f, 100.0f);
			//System.out.println("x: " + grid[iX][iY].x + " y: " + grid[iX][iY].y);
			for(iY=1;iY<20;iY++)
			{
				grid[iX][iY] = new Rectangle(x, y+100.0f, 100.0f, 100.0f);
				//System.out.println("x: " + grid[iX][iY].x + " y: " + grid[iX][iY].y);
				y+=100.0f;
			}
			x+=100.0f;
			y=0.0f;
		}
		return grid;
	}
			
	// gives back free way point
	public Vector2 aiGiveFreePoint()
	{
		if(aStar)
		{
			aStarGrid();
		}
		Vector2 free = new Vector2(-1.0f, -1.0f);
		boolean targetOnObst = false;
		unitArea = aiGiveArea(aiUnit, mapName);
		targetArea = aiGiveArea(actualTarget, mapName);
		Vector2 ctrlU = new Vector2(aiUnit.centerX, aiUnit.centerY); 
		Vector2 targetU = new Vector2(actualTarget.centerX, actualTarget.centerY);
		// flying target over obstacle?
		if(aiObstTest(ctrlU,targetU)[0] <= actualMap.obstacles.size()-1 && Math.abs(aiObstTest(ctrlU,targetU)[1]) - Math.abs(actualTarget.centerX) <= 25 
				&& Math.abs(aiObstTest(ctrlU,targetU)[2]) - Math.abs(actualTarget.centerY) <= 25 && actualTarget.type == TYPE.COPTER)
		{
			targetOnObst = true;
			//System.out.println("COPTER on obst");
		}
		
		Vector2 shift11 = new Vector2(), shift12 = new Vector2(), shift13 = new Vector2(), shift14 = new Vector2(), shift15 = new Vector2(), shift16 = new Vector2();
		Vector2 shift21 = new Vector2(), shift22 = new Vector2(), shift23 = new Vector2(), shift24 = new Vector2(), shift25 = new Vector2(), shift26 = new Vector2();
		Vector2 shift31 = new Vector2(), shift32 = new Vector2(), shift33 = new Vector2(), shift34 = new Vector2(), shift35 = new Vector2(), shift36 = new Vector2();
		Vector2 shift41 = new Vector2(), shift42 = new Vector2(), shift43 = new Vector2(), shift44 = new Vector2(), shift45 = new Vector2(), shift46 = new Vector2();
		Vector2 shift51 = new Vector2(), shift52 = new Vector2(), shift53 = new Vector2(), shift54 = new Vector2(), shift55 = new Vector2(), shift56 = new Vector2();
		Vector2 shift61 = new Vector2(), shift62 = new Vector2(), shift63 = new Vector2(), shift64 = new Vector2(), shift65 = new Vector2(), shift66 = new Vector2();

		if (mapName.equals("field"))
		{
			shift11.set(500.0f, 500.0f);
			shift12.set(760.0f, 650.0f);
			shift14.set(300.0f, 810.0f);
			shift13.set(shift12);
			shift15.set(shift12);
			shift16.set(shift12);
			
			shift21.set(740.0f, 650.0f);
			shift22.set(1350.0f, 500.0f);
			shift23.set(1610.0f, 300.0f);
			shift25.set(1150.0f, 810.0f);
			shift24.set(shift21);
			shift26.set(shift25);
			
			shift32.set(1590.0f, 300.0f);
			shift33.set(2350.0f, 400.0f);
			shift36.set(2400.0f, 810.0f);
			shift31.set(shift32);
			shift34.set(shift32);
			shift35.set(shift32);
			
			shift41.set(300.0f, 790.0f);
			shift44.set(300.0f, 1650.0f);
			shift45.set(760.0f, 1850.0f);
			shift42.set(shift41);
			shift43.set(shift41);
			shift46.set(shift45);
			
			shift52.set(1150.0f, 790.0f);
			shift54.set(740.0f, 1850.0f);
			shift55.set(1350.0f, 1650.0f);
			shift56.set(1610.0f, 1650.0f);
			shift51.set(shift52);
			shift53.set(shift52);
			
			shift63.set(2400.0f, 790.0f);
			shift65.set(1590.0f, 1650.0f);
			shift66.set(2300.0f, 1350.0f);
			shift61.set(shift65);
			shift62.set(shift65);
			shift64.set(shift65);
		}
		else if (mapName.equals("snow"))
		{
			shift11.set(500.0f, 400.0f);
			shift12.set(1210.0f, 500.0f);
			shift13.set(shift12);
			shift14.set(shift12);
			shift15.set(shift12);
			shift16.set(shift12);
			
			shift21.set(1190.0f, 500.0f);
			shift22.set(1750.0f, 400.0f);
			shift24.set(1550.0f, 850.0f);
			shift23.set(shift24);
			shift25.set(shift24);
			shift26.set(shift24);
			
			shift33.set(1050.0f, 1150.0f);
			shift34.set(1210.0f, 1050.0f);
			shift35.set(900.0f,	1260.0f);
			shift36.set(1210.0f, 1450.0f);
			shift31.set(shift34);
			shift32.set(shift34);
			
			shift42.set(1550.0f, 800.0f);
			shift43.set(1190.0f, 1050.0f);
			shift44.set(1800.0f, 1000.0f);
			shift46.set(2050.0f, 1260.0f);
			shift41.set(shift42);
			shift45.set(shift43);
			
			shift53.set(900.0f, 1240.0f);
			shift54.set(1210.0f, 1050.0f);
			shift55.set(700.0f, 1600.0f);
			shift56.set(1210.0f, 1450.0f);
			shift51.set(shift53);
			shift52.set(shift53);
			
			shift64.set(2050.0f, 1240.0f);
			shift65.set(1190.0f, 1450.0f);
			shift66.set(1950.0f, 1600.0f);
			shift61.set(shift64);
			shift62.set(shift64);
			shift63.set(shift65);
		}
		else if (mapName.equals("desert"))
		{
			shift11.set(300.0f,	450.0f);
			shift12.set(585.0f,	800.0f);
			shift14.set(350.0f,	1010.0f);
			shift13.set(shift12);
			shift15.set(shift14);
			shift16.set(shift12);
			
			shift21.set(565.0f,	800.0f);
			shift22.set(850.0f,	400.0f);
			shift23.set(1810.0f, 300.0f);
			shift25.set(1700.0f, 1010.0f);
			shift24.set(shift21);
			shift26.set(shift25);
			
			shift32.set(1790.0f, 300.0f);
			shift33.set(2300.0f, 750.0f);
			shift36.set(2200.0f, 1010.0f);
			shift31.set(shift32);
			shift34.set(shift32);
			shift35.set(shift36);
			
			shift41.set(350.0f,	990.0f);
			shift44.set(350.0f,	1350.0f);
			shift45.set(585.0f,	1650.0f);
			shift42.set(shift41);
			shift43.set(shift45);
			shift46.set(shift45);
			
			shift52.set(1700.0f, 990.0f);
			shift54.set(565.0f,	1650.0f);
			shift55.set(1300.0f, 1700.0f);
			shift56.set(1810.0f, 1800.0f);
			shift51.set(shift54);
			shift53.set(shift56);
			
			shift63.set(2200.0f, 990.0f);
			shift65.set(1790.0f, 1800.0f);
			shift66.set(2100.0f, 1400.0f);
			shift61.set(shift65);
			shift62.set(shift65);
			shift64.set(shift65);
		}
		
		// obstacle collision detected -> calculate avoid-point & move to
		if (aiObstTest(ctrlU,targetU)[0] < actualMap.obstacles.size() && aiUnit.type != TYPE.COPTER) //size -1???
		{	
			//System.out.println("obstacle detected...");
			// node system to give next logical point
			if (unitArea == 6)
			{
				if (targetArea == 6) // 6->6
				{
					free.set(shift66);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift66.x, shift66.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
				else if (targetArea == 5) // 6->5
				{						
					free.set(aiInnerFree(aiUnit, shift65, shift66));
				}
				else if (targetArea == 4) // 6->4
				{						
					free.set(aiInnerFree(aiUnit, shift64, shift66));
				}
				else if (targetArea == 3) // 6->3
				{						
					free.set(aiInnerFree(aiUnit, shift63, shift66));
				}
				else if (targetArea == 2) // 6->2
				{						
					free.set(aiInnerFree(aiUnit, shift62, shift66));
				}
				else if (targetArea == 1) // 6->1
				{						
					free.set(aiInnerFree(aiUnit, shift61, shift66));
				}
			}
			if (unitArea == 5)
			{
				if (targetArea == 6) // 5->6
				{
					free.set(aiInnerFree(aiUnit, shift56, shift55));	
				}
				else if (targetArea == 5) // 5->5
				{						
					free.set(shift55);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift55.x, shift55.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
				else if (targetArea == 4) // 5->4
				{						
					free.set(aiInnerFree(aiUnit, shift54, shift55));
				}
				else if (targetArea == 3) // 5->3
				{						
					free.set(aiInnerFree(aiUnit, shift53, shift55));
				}
				else if (targetArea == 2) // 5->2
				{						
					free.set(aiInnerFree(aiUnit, shift52, shift55));
					//System.out.println("5-2 " + shift52.x);
				}
				else if (targetArea == 1) // 5->1
				{						
					free.set(aiInnerFree(aiUnit, shift51, shift55));
				}
			}
			if (unitArea == 4)
			{
				if (targetArea == 6) // 4->6
				{
					free.set(aiInnerFree(aiUnit, shift46, shift44));
				}
				else if (targetArea == 5) // 4->5
				{						
					free.set(aiInnerFree(aiUnit, shift45, shift44));
				}
				else if (targetArea == 4) // 4->4
				{						
					free.set(shift44);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift44.x, shift44.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
				else if (targetArea == 3) // 4->3
				{						
					free.set(aiInnerFree(aiUnit, shift43, shift44));
				}
				else if (targetArea == 2) // 4->2
				{						
					free.set(aiInnerFree(aiUnit, shift42, shift44));
				}
				else if (targetArea == 1) // 4->1
				{						
					free.set(aiInnerFree(aiUnit, shift41, shift44));
				}
			}
			if (unitArea == 3)
			{
				if (targetArea == 6) // 3->6
				{
					free.set(aiInnerFree(aiUnit, shift36, shift33));
				}
				else if (targetArea == 5) // 3->5
				{						
					free.set(aiInnerFree(aiUnit, shift35, shift33));
				}
				else if (targetArea == 4) // 3->4
				{						
					free.set(aiInnerFree(aiUnit, shift34, shift33));
				}
				else if (targetArea == 3) // 3->3
				{						
					free.set(shift33);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift33.x, shift33.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
				else if (targetArea == 2) // 3->2
				{						
					free.set(aiInnerFree(aiUnit, shift32, shift33));
				}
				else if (targetArea == 1) // 3->1
				{						
					free.set(aiInnerFree(aiUnit, shift31, shift33));
				}
			}
			if (unitArea == 2)
			{
				if (targetArea == 6) // 2->6
				{
					free.set(aiInnerFree(aiUnit, shift26, shift22));
				}
				else if (targetArea == 5) // 2->5
				{						
					free.set(aiInnerFree(aiUnit, shift25, shift22));
				}
				else if (targetArea == 4) // 2->4
				{						
					free.set(aiInnerFree(aiUnit, shift24, shift22));
				}
				else if (targetArea == 3) // 2->3
				{						
					free.set(aiInnerFree(aiUnit, shift23, shift22));
				}
				else if (targetArea == 2) // 2->2
				{						
					free.set(shift22);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift22.x, shift22.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
				else if (targetArea == 1) // 2->1
				{						
					free.set(aiInnerFree(aiUnit, shift21, shift22));
				}
			}
			if (unitArea == 1)
			{
				if (targetArea == 6) // 1->6
				{
					free.set(aiInnerFree(aiUnit, shift16, shift11));
				}
				else if (targetArea == 5) // 1->5
				{						
					free.set(aiInnerFree(aiUnit, shift15, shift11));
				}
				else if (targetArea == 4) // 1->4
				{						
					free.set(aiInnerFree(aiUnit, shift14, shift11));
				}
				else if (targetArea == 3) // 1->3
				{						
					free.set(aiInnerFree(aiUnit, shift13, shift11));
				}
				else if (targetArea == 2) // 1->2
				{						
					free.set(aiInnerFree(aiUnit, shift12, shift11));
				}
				else if (targetArea == 1) // 1->1
				{						
					free.set(shift11);
					if(targetOnObst || (aiGetUnitPointDist(aiUnit, shift11.x, shift11.y) <= 5))
					{free.set(actualTarget.centerX, actualTarget.centerY);}	
				}
			}
				
			
		}
		else //target point is free
		{
			free.set(actualTarget.centerX, actualTarget.centerY);
		}
		Vector2 freePoint = new Vector2();
		// check if another unit is on the way to freepoint if not flying
		if (avoidUnit && !aiUnit.flying)
		{
			//System.out.println("free... " + free.x);
			freePoint.set(aiIsAnyBodyOutThere(free));
			//System.out.println("new free... " + freePoint.x);
		}
		else
		{
			//System.out.println("free way to next point... " + free.x);
			freePoint.set(free);
		}
		//System.out.println("map is " + mapName + " moving from area " + unitArea + " to " + targetArea + " x:" + freePoint.x + " y:" + freePoint.y);
		if(aiGetUnitPointDist(aiUnit, freePoint.x, freePoint.y) <= 50.0f)
		{
			freePoint.set(actualTarget.centerX, actualTarget.centerY);
		}
		return freePoint;
	} //aiGiveFreePoint() END
	
	// is point on obstacle?
	private boolean aiIsObst(float _x, float _y)
	{
		boolean obstColl = false;
		for(short i=0 ; i<actualMap.obstacles.size() && obstColl != true ; i++)
		{
			obstColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(_x, _y);
		}
		return obstColl;
	} //aiIsObst() END
	
	// is point on unit?
	private boolean aiIsUnit(float _x, float _y)
	{
		boolean unitColl = false;
		for(short i=0 ; i<actualMap.units.size() && unitColl != true ; i++)
		{
			unitColl = actualMap.obstacles.get(i).getBoundingRectangle().contains(_x, _y);
		}
		return unitColl;
	} //aiIsUnit() END
	
	// distance between unit and point
	private double aiGetUnitPointDist(Unit unit, float obst_x, float obst_y)
	{
		int dx = (int)(unit.centerX - obst_x);
		int dy = (int)(unit.centerY - obst_y);
		return SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
	} //aiGetObstDist() END
	
	// distance between point1 and point2
	private double aiGetDistance(Vector2 start, Vector2 end)
	{
		int dx = (int)(start.x - end.x);
		int dy = (int)(start.y - end.y);
		return SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
	} //aiGetDistance() END
}