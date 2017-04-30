package main;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class PatrikPathfinding
{
	public int stepSize = 100;
	private ArrayList<Vector2> steps;
	private Vector2 startPoint, goalPoint;
	private ArrayList<Polygon> polygons = null;
	
	public PatrikPathfinding(Vector2 start, Vector2 goal, int _stepSize, World world, ArrayList<Polygon> staticPolygons)
	{
		init(start,goal,_stepSize, world, staticPolygons);
	}
	
	public PatrikPathfinding(Element start, Element goal, World world, ArrayList<Polygon> staticPolygons)
	{
		init(new Vector2(start.centerX, start.centerY), new Vector2(goal.centerX, goal.centerY), 
				(int)(start.getWidth() > start.getHeight() ? start.getWidth() : start.getHeight()), 
				world, staticPolygons);
	}
	
	private void init(Vector2 start, Vector2 goal, int _stepSize, World world, ArrayList<Polygon> staticPolygons)
	{
		steps = new ArrayList<Vector2>();
		stepSize = _stepSize;
		stepSize*=1.3f;
		
		startPoint = start.cpy();
		goalPoint  = goal.cpy();
		
		steps.add(startPoint.cpy());
		
		//create polygons
		polygons = new ArrayList<Polygon>();
		
		for (int ip = 0;ip<staticPolygons.size();ip++)
		{
			polygons.add(staticPolygons.get(ip));
		}
		
		Iterator<Body> it = world.getBodies();
        while(it.hasNext())
        {
        	
           Body b = it.next();
           if (b.getType() == BodyType.StaticBody) continue;
           Iterator<Fixture> fxl=b.getFixtureList().iterator();
           while(fxl.hasNext())
           {
              Fixture fx = fxl.next();
              Shape s =fx.getShape();
              if(s instanceof PolygonShape)
              {     
                 int k=((PolygonShape) s).getVertexCount();
                 float[] vertices = new float[k*2];
                 for(int n=0;n<k; n++)
                 {
                    Vector2 temp = new Vector2();
                    ((PolygonShape) s).getVertex(n, temp);
                    b.getTransform().mul(temp);
                    temp.scl(Game.PIXELS_PER_METER);
                    vertices[n*2]   = temp.x;
                    vertices[(n*2)+1] = temp.y;//*Game.PIXELS_PER_METER;
                 }
                 
                 polygons.add(new Polygon(vertices));
              }
           }  
        }
	}
	
	public ArrayList<Polygon> getPolys() { return polygons; }
	
	public Vector2 getNextPoint(GameMap gameMap)
	{
		doStep(gameMap);
		Vector2 v = steps.get(steps.size()-1);

		return v;
	}
	
	public Vector2 getStartPoint() {
		return startPoint;
	}

	public Vector2 getGoalPoint() {
		return goalPoint;
	}

	public ArrayList<Vector2> getSteps() {
		return steps;
	}

	private double getDistance(Vector2 _start, Vector2 _end)
	{
		int dx = (int)(_start.x - _end.x);
		int dy = (int)(_start.y - _end.y);
		return SquareRoot.fastSqrt(dx*dx + dy*dy);
	} 
	
	private void doStep(GameMap map)
	{
		if (startPoint == null || goalPoint == null) return;
		
		System.out.println("DO STEP");
		
		Vector2 curPoint  = null;
		Vector2 nextPoint = new Vector2();
		
		curPoint = steps.get(steps.size()-1);
		
		float slopeY = (goalPoint.y - curPoint.y);
		float slopeX = (goalPoint.x - curPoint.x);
		float slope = (slopeY / slopeX);
		
//		float angle = Trigonometrics.getAngleBetweenPointsRad(curPoint.x, curPoint.y, goalPoint.x, goalPoint.y);
//		System.out.println("slope: "+slope);
//		System.out.println("angle: "+angle);
//		System.out.println("Distance: "+Trigonometrics.getDistanceFast(curPoint, goalPoint));
		
		
		Vector2 slopeVector = new Vector2(slopeX, slopeY);
		
		limitSlopeVectorToStepSize(slopeVector);
 
		boolean collision = false;
		float searchAngle = 10;
		

		do
		{
			collision = false;
			nextPoint.set(curPoint.x+(slopeVector.x), curPoint.y+(slopeVector.y));
			if (nextPoint.x < 0 ) nextPoint.x = 0;
			if (nextPoint.y < 0 ) nextPoint.y = 0;
			if (nextPoint.x > map.getWidth() ) nextPoint.x = map.getWidth();
			if (nextPoint.y > map.getHeight() ) nextPoint.y = map.getHeight();
			
			if (searchAngle > 360)
			{
				System.out.println("PATHFINDING CANNOT DO ANYTHING APPARENTLY");
				break; // CANNOT DO ANYTHING APPARENTLY
			}
			
//			Rectangle rect = new Rectangle(nextPoint.x-(stepSize/2f), nextPoint.y-(stepSize/2f), stepSize, stepSize);
			
			float[] verts = new float[8];
			verts[0] = nextPoint.x-(stepSize/2f);
			verts[1] = nextPoint.y-(stepSize/2f);
			
			verts[2] = nextPoint.x-(stepSize/2f)+stepSize;
			verts[3] = nextPoint.y-(stepSize/2f);
			
			verts[4] = nextPoint.x-(stepSize/2f)+stepSize;
			verts[5] = nextPoint.y-(stepSize/2f)+stepSize;
			
			verts[6] = nextPoint.x-(stepSize/2f);
			verts[7] = nextPoint.y-(stepSize/2f)+stepSize;
			Polygon checkPoly = new Polygon(verts);
			
			for (int i = 0; i < polygons.size(); i++)
			{
				if (Intersector.overlapConvexPolygons(checkPoly, polygons.get(i)))
				{
					System.out.println("Collision");
					collision = true;
					slopeVector.rotate(searchAngle);
					limitSlopeVectorToStepSize(slopeVector);
					System.out.println("Rotating slopeVector "+searchAngle);
					
					if (searchAngle > 0) searchAngle = -searchAngle;
					else { searchAngle = -searchAngle; searchAngle+=10; }
					
					break;
				}
			}
			
//			for (int i = 0; i < map.obstacles.size(); i++)
//			{
//				Element el = map.obstacles.get(i);
//				
//				if (el.getCollRect().overlaps(rect))
//				{
//					System.out.println("Collision");
//					collision = true;
//					slopeVector.rotate(searchAngle);
//					limitSlopeVectorToStepSize(slopeVector);
//					System.out.println("Rotating slopeVector "+searchAngle);
//					
//					if (searchAngle > 0) searchAngle = -searchAngle;
//					else { searchAngle = -searchAngle; searchAngle+=10; }
//					
//					break;
//				}
//			}
		}
		while(collision);
		
		steps.add(nextPoint);
	}
	
	private void limitSlopeVectorToStepSize(Vector2 slopeVector)
	{
		if (slopeVector.x > stepSize)
		{
			float factor = stepSize / slopeVector.x;
			slopeVector.x = stepSize;
			slopeVector.y*=factor;
		}
		else if (slopeVector.y > stepSize)
		{
			float factor = stepSize / slopeVector.y;
			slopeVector.y = stepSize;
			slopeVector.x*=factor;
		}
		
		if (slopeVector.x < -stepSize)
		{
			float factor = -stepSize / slopeVector.x;
			slopeVector.x = -stepSize;
			slopeVector.y*=factor;
		}
		else if (slopeVector.y < -stepSize)
		{
			float factor = -stepSize / slopeVector.y;
			slopeVector.y = -stepSize;
			slopeVector.x*=factor;
		}
	}
	
	public int getTotalSteps() { return steps.size(); }
}
