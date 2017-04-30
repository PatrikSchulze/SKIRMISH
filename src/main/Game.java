package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import main.Unit.TYPE;

import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Game implements ApplicationListener
{
	public static final int PIXELS_PER_METER = 16;
	private static final long timeToWaitForSelect = 1400;
	private static final boolean SHOWFPS = false;
	private static final boolean FREECAMERA = false;
	private static final boolean PLAYAUDIO = true;
	private static final boolean TEST_AI_ONLY = false;
	private static String JUMP_INTO_MAP_DIRECTLY = null;//"field_1";
	private static final boolean DEVKIT = true;
    private static final float MIN_ZOOM = 0.8f;
    private static final float BUTTON_FADE_SPEED = 0.04f;
    private static Color designColor = new Color(0.4f, 0.4f, 1f, 0.7f);
    
	static enum PHASE { PLAYER1_CHOOSE, PLAYER1_MOVE, PLAYER1_ATTACK, PLAYER2_CHOOSE, PLAYER2_MOVE, PLAYER2_ATTACK; }
	static enum GAMEMODE { LOCAL_2PLAYER, AI_ENEMY, NETWORK; }
	static enum GAMESTATE { STARTMENU, INGAME, MAPCHOICE; }
	
	private GAMEMODE gameMode 	= GAMEMODE.AI_ENEMY;
    private GAMESTATE gameState = GAMESTATE.STARTMENU;
    private GAMEMODE gotoMode 	= GAMEMODE.AI_ENEMY;

	private boolean loopingEngineSound, enemyDidShoot, enemyDidAim, gameOver, drawChooseConfirm, actionEmpty, assetsFullyLoaded;
	private boolean howToScreen = false;
	private boolean showPauseMenu = false;
	private boolean fps60 = false;
	private boolean alreadyRendered, renderBox2D, debug_mapdesign;
	private float movieBorderAlpha = 0;
	private float titleScreenScale = 1.0f;
	private float clearMenuBgAlpha = 1.0f;
	private float titleAlpha = 0.0f;
	private float targetZoom;
	private float scaleAmount;
	private long lastDebugReloadTime = 0;
	private long selectWait = 0;
	private long aiWait = -1;
	private long engineSndId = -1;
	private long timeToNextPhase = -1;
	private long quakeTimer = System.currentTimeMillis()-100;
	private long timeMeasurePoint;
	private int menuPreLoadFrame = 5; // Show loading screen, wait 5 frames, start loading
	private int quakeCamOffsetX, quakeCamOffsetY;
	private int debugSndDelay = 0;
	private int player1UnitAmount, player2UnitAmount;
	private int hud_boxHeight, hud_phaseBoxWidth;
	private float actionButtonFadeIn = 1.0f;
	private boolean actionButtonFading = false;
	
	private BitmapFont font;
	private BitmapFont smallFont;
	private CameraController controller;
	private Color tempColor = new Color(1,1,1,1);
	private Color tempColor2 = new Color(1,1,1,1);
	private Element debugMapDesignElement = null;
	private FrameSkipper skip;
	private GameMap gameMap;
	private HashMap<String, Sound> sounds = new HashMap<String, Sound>();
	private Music music;
	private Music titleMusic;
	private MyGestureDetector gestureDetector;
	private OrthographicCamera camera;
	private PHASE phase = PHASE.PLAYER1_CHOOSE;
	private Rectangle tempRect = new Rectangle(0,0,0,0);
	private Sprite startbg;
	private Sprite startbgClear;
	private SpriteBatch spriteBatch;
	private SpriteBatch hudSpriteBatch;
	private Sprite sprActionButton;
    private Sprite sprCancelButton;
	private String winner;
	private TextureAtlas allAtlas;
	private TextureAtlas menuAtlas;
	private Texture startbgtex;
	private Texture startbgtexClear;	
	private TextureRegion alchTempTitle;
	private TextureRegion menuTitle;
	private Unit ctrlUnit, aiTarget;
	private Vector2 moveToCam = null;
	// Box2D stuff
	private World mWorld;
	private Box2DDebugRenderer debugRenderer;
	private ArrayList<Polygon> staticPolygons;
		
	private ShapeRenderer shaper;
	
	private Texture blackPreLoadTex;
	private HashMap<String, OurBodyEditorLoader> mapBox2DLoaders;
    
	//UI
	private MyStage uiStage;
	private Button buttonSingle;
	private Button buttonLocal2;
	
	// Pathfinding
	private int offSet;
	private Vector2 pathPoint = null;
//	private Pathfinding pathF;
	private PatrikPathfinding pathP;
	
	public Game(float fps)
	{
		super();
    	fps60 = (fps > 51) ? true : false;
	}
	
	@Override
	public void create()
	{
		System.out.println("#########################");
		System.out.println("SKIRMISH");
		System.out.println("#########################\n");
		System.out.println("OS: "+Gdx.app.getType());
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		
		if (isMobilePlatform()) System.out.println("Screen Size: "+Gdx.graphics.getWidth()+" x "+Gdx.graphics.getHeight());
		
		if (!fps60) skip = new FrameSkipper(30);
		camera 			= new OrthographicCamera();
		camera.setToOrtho(true);
		controller 		= new CameraController();
        gestureDetector = new MyGestureDetector(20, 0.5f, 2, 0.15f, controller);
        Gdx.input.setInputProcessor(gestureDetector);
		spriteBatch 	= new SpriteBatch();
		hudSpriteBatch 	= new SpriteBatch();
		font 			= new BitmapFont(Gdx.files.internal("content/sans_20.fnt"), false);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		if (debug_mapdesign) lastDebugReloadTime = 1;
		
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.BLACK);
		pixmap.fill();
		blackPreLoadTex = new Texture(pixmap);
		
		debugRenderer = new Box2DDebugRenderer();
		shaper = new ShapeRenderer();
		shaper.setColor(Color.BLUE);
		

//		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
//		Gdx.gl.glCullFace(GL10.GL_BACK);
		/*Lets say
		 * The given font is perfect for 800x480
		 * so lets scale from this 
		 */
		//scale to lower one
		float timesWidth  = Gdx.graphics.getWidth()/800.0f;
		float timesHeight = Gdx.graphics.getHeight()/480.0f;
		scaleAmount = timesWidth;
		if (timesHeight > timesWidth) scaleAmount = timesHeight;
		font.setScale(scaleAmount);
		System.out.println("Scaling font to "+scaleAmount);
		
		alchTempTitle = new TextureRegion(new Texture(Gdx.files.internal("content/alch-title.png"))); //  menu/alch-title
		alchTempTitle.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
//		OrthographicCamera stageCam 			= new OrthographicCamera();
//		stageCam.setToOrtho(true);
		
		camera.zoom = 1f+(2f-scaleAmount);
		targetZoom = camera.zoom;
	}
	
	private void clearBox2DBodies()
	{
		if (mWorld != null)
		{
			Body tempB;
			Iterator<Body> itb = mWorld.getBodies();
			while (itb.hasNext())
			{
				tempB = itb.next();
				if(tempB != null)mWorld.destroyBody(tempB);
			}
		}
		mWorld = new World(new Vector2(0,0), true);
	}
	
	private void startNewGame(GameMap map)
	{
		ctrlUnit = null;
		if (PLAYAUDIO)
		{
			titleMusic.stop();
			
			for (Sound snd : sounds.values())
			{
				snd.stop();
			}
			
			music.setLooping(true);
			music.setVolume(0.8f);
			music.play();
		}
		
		gameOver = false;
		gameMap = map;
		showPauseMenu = false;
		gameOver = false;
		
		player1UnitAmount = 0;
		player2UnitAmount = 0;
		
		for (int i = gameMap.units.size() - 1; i >= 0; i--)
        {
			Unit unit = gameMap.units.get(i);
			if (unit != null && !unit.dead)
			{
				if (unit.owner == Unit.OWNER.PLAYER1)
					player1UnitAmount++;
				else if (unit.owner == Unit.OWNER.PLAYER2)
					player2UnitAmount++;
			}
        }
		
		if (!debug_mapdesign && !FREECAMERA)
		{
			float tx = gameMap.units.get(0).centerX;
			float ty = gameMap.units.get(0).centerY;
		
			moveToCam = null;
			camera.position.x = tx;
			camera.position.y = ty;
			keepCameraInBounds();
		}
		
		long be = System.nanoTime()/1000L/1000L;
		//create polygons
		staticPolygons = new ArrayList<Polygon>();
		Iterator<Body> it = mWorld.getBodies();
        while(it.hasNext())
        {
           Body b = it.next();
           if (b.getType() != BodyType.StaticBody) continue;
           Iterator<Fixture> fxl=b.getFixtureList().iterator();
           while(fxl.hasNext())
           {
              Fixture fx = fxl.next();
              Shape s 	 = fx.getShape();
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
                    vertices[(n*2)+1] = temp.y;
                 }
                 staticPolygons.add(new Polygon(vertices));
              }
           }           
        }
        System.out.println("Static Polygons took: "+((System.nanoTime()/1000L/1000L)-be)+"ms");
		
		phase = PHASE.PLAYER1_CHOOSE;
		if (TEST_AI_ONLY) phase = PHASE.PLAYER2_CHOOSE;
		timeMeasurePoint = System.currentTimeMillis(); // this is for P1's round
	}
	
	private void traceThis()
	{
		new Exception("Trace").printStackTrace();
	}
	
	private void nextPhase()
	{
		//play sound
		//valkyria has also different music for player1 and player2 phases
		
		actionEmpty = false;
		
		if (phase == PHASE.PLAYER1_CHOOSE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_CHOOSE))
		{			
			if (ctrlUnit == null)
			{
				System.out.println("Error:  ctrlUnit NULL after CHOOSE Phase.");
				return;
			}
			if (phase == PHASE.PLAYER1_CHOOSE)
				phase = PHASE.PLAYER1_MOVE;
			else if (phase == PHASE.PLAYER2_CHOOSE)
				phase = PHASE.PLAYER2_MOVE;
			ctrlUnit.refillAction();
			moveToCam = new Vector2();
			moveToCam.x = ctrlUnit.centerX;
			moveToCam.y = ctrlUnit.centerY;
			return;
		}
		else if (phase == PHASE.PLAYER1_MOVE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_MOVE))
		{
			if (phase == PHASE.PLAYER1_MOVE)
				phase = PHASE.PLAYER1_ATTACK;
			else if (phase == PHASE.PLAYER2_MOVE)
				phase = PHASE.PLAYER2_ATTACK;
			ctrlUnit.stopMovement();
			if (PLAYAUDIO)
			{
				if (loopingEngineSound)
				{
					if (ctrlUnit.type == TYPE.LIGHT_TANK)  		sounds.get("light_tank_engine").stop();
					else if (ctrlUnit.type == TYPE.COPTER) 		sounds.get("copter_engine").stop();
					else if (ctrlUnit.type == TYPE.HEAVY_TANK) 	sounds.get("light_tank_engine").stop();
					else if (ctrlUnit.type == TYPE.BUGGY) 		sounds.get("light_tank_engine").stop();
					loopingEngineSound = false;
				}
			}
			return;
		}
		else if (phase == PHASE.PLAYER1_ATTACK || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_ATTACK))
		{
			if (ctrlUnit != null && ctrlUnit.projs != null && ctrlUnit.projs.size() > 0) return;
			if (ctrlUnit != null)
			{
				ctrlUnit.targeting = false;
				ctrlUnit.shooting = false;
				ctrlUnit.doneShooting = false;
				ctrlUnit.projs.clear();
				ctrlUnit.shotPoint.clear();
				ctrlUnit.shotPointSpeed.clear();
				ctrlUnit.distanceToShoot = 0;
				ctrlUnit 		= null;
			}
			
			if (phase == PHASE.PLAYER1_ATTACK)
			{
				phase = PHASE.PLAYER2_CHOOSE;

				if (timeMeasurePoint > 0)	Stats.timeSpendP1+= System.currentTimeMillis()-timeMeasurePoint;
				timeMeasurePoint = System.currentTimeMillis();
			}
			else if (phase == PHASE.PLAYER2_ATTACK)
			{
				phase = PHASE.PLAYER1_CHOOSE;
				
				if (timeMeasurePoint > 0)	Stats.timeSpendP2+= System.currentTimeMillis()-timeMeasurePoint;
				timeMeasurePoint = System.currentTimeMillis();
			}
			aiTarget 		= null;
			enemyDidShoot 	= false;
			enemyDidAim 	= false;
			return;
		}
		
		
		if (gameMode == GAMEMODE.AI_ENEMY)
		{
			if (phase == PHASE.PLAYER2_CHOOSE)
			{
				if (ctrlUnit == null)
				{
					System.out.println("Error:  ctrlUnit NULL after CHOOSE Phase.");
					
					return;
				}
				phase = PHASE.PLAYER2_MOVE;
				ctrlUnit.refillAction();
				return;
			}
			else if (phase == PHASE.PLAYER2_MOVE)
			{
				phase = PHASE.PLAYER2_ATTACK;
				return;
			}
			else if (phase == PHASE.PLAYER2_ATTACK)
			{
				if (ctrlUnit != null && ctrlUnit.projs != null && ctrlUnit.projs.size() > 0) return;
				if (ctrlUnit != null)
				{
					ctrlUnit.targeting = false;
					ctrlUnit.shooting = false;
					ctrlUnit.doneShooting = false;
					ctrlUnit.projs.clear();
					ctrlUnit.shotPoint.clear();
					ctrlUnit.shotPointSpeed.clear();
					ctrlUnit.distanceToShoot = 0;
					ctrlUnit 		= null;
				}
				phase = PHASE.PLAYER1_CHOOSE;
				if (timeMeasurePoint > 0)	Stats.timeSpendP2+= System.currentTimeMillis()-timeMeasurePoint;
				timeMeasurePoint = System.currentTimeMillis();
				aiTarget = null;
				if (TEST_AI_ONLY) phase = PHASE.PLAYER2_CHOOSE;
				return;
			}
		}
	}
	
	private void adjustCamera()
	{
		/*
		 * if the difference is negligible, its considere equal
		 * negligible is defined here as 0.01f
		 */
		
		if (Math.abs(targetZoom-camera.zoom) > 0.01f)
		{
			camera.zoom+=(targetZoom-camera.zoom)/20.0f;
		}
		
		if (!FREECAMERA)
		{
			if (moveToCam != null)
			{
				int dx = (int)(moveToCam.x - camera.position.x);
				int dy = (int)(moveToCam.y - camera.position.y);
				double distance = SquareRoot.fastSqrt((dx*dx) + (dy*dy));
				float cameraSpeed = 10.0f;
	
			    if ( distance > cameraSpeed )
			    { 
			    	camera.position.x+= (float)(cameraSpeed*dx/distance); 
			    	camera.position.y+= (float)(cameraSpeed*dy/distance); 
			    }
			    else
			    {
			    	moveToCam = null;
			    }
			}
			else
			{
				if (phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER2_MOVE)
				{
					if (ctrlUnit != null)
					{
						camera.position.x = ctrlUnit.centerX;
						camera.position.y = ctrlUnit.centerY;
					} 
				}
			}
		}
		
		if (quakeTimer > System.currentTimeMillis())
		{
			camera.position.x+=getRandom(-quakeCamOffsetX,quakeCamOffsetX);
			camera.position.y+=getRandom(-quakeCamOffsetY,quakeCamOffsetY);
		}
		
		keepCameraInBounds();
	}
	
	private void quake(int _x, int _y, int time_in_ms)
	{
		quakeCamOffsetX = _x;
		quakeCamOffsetY = _y;
		quakeTimer = System.currentTimeMillis()+time_in_ms;
	}
	
	public static final float getPercentageValue(long all, long yourValue)
	{
		return (float)((int)MathUtils.round((float)((100.0d/(double)all)*(double)yourValue)))/100.0f;
	}
	
	public static final float getSmallFloatPercentageValue(int all, int yourValue)
	{
		return (float)getIntPercentage(all, yourValue)/100.0f;
	}
	
	public static final int getIntPercentage(int all, int yourValue)
	{
		return (int)MathUtils.round((float)((100.0d/(double)all)*(double)yourValue));
	}
	
	private void holdUnitInMap(Unit unit)
	{
		if (unit.getX() < 0) unit.setX(0);
		if (unit.getY() < 0) unit.setY(0);
		if (unit.getX()+unit.getWidth()  > gameMap.getWidth())  unit.setX(gameMap.getWidth() -unit.getWidth() );
		if (unit.getY()+unit.getHeight() > gameMap.getHeight()) unit.setY(gameMap.getHeight()-unit.getHeight());
	}
	
// --- AI ---
		public void aiWait(int ms)
		{
			aiWait = System.currentTimeMillis()+ms;
		}
		
		public boolean aiHaveToWait()
		{
			return (aiWait != -1 && aiWait > System.currentTimeMillis()); //if waiting point is in the future
		}
		
		// distance between unit and point
		public int aiGetUnitPointDist(Unit unit, Vector2 _obst)
		{
			int dx = (int)(unit.centerX - _obst.x);
			int dy = (int)(unit.centerY - _obst.y);
			return SquareRoot.fastSqrt(dx*dx + dy*dy);
		} //aiGetObstDist() END
		
		private double aiGetDistance(Vector2 _start, Vector2 _end)
		{
			int dx = (int)(_start.x - _end.x);
			int dy = (int)(_start.y - _end.y);
			return SquareRoot.fastSqrt(dx*dx + dy*dy);
		} //aiGetDistance() END
			
		// move unit to point
		private void aiMove(Vector2 _pathPoint)
		{
			if (PLAYAUDIO)
			{
				if (!loopingEngineSound)
				{
					if (ctrlUnit.type == TYPE.LIGHT_TANK)  		engineSndId = sounds.get("light_tank_engine").loop();
					else if (ctrlUnit.type == TYPE.COPTER) 		engineSndId = sounds.get("copter_engine").loop();
					else if (ctrlUnit.type == TYPE.HEAVY_TANK) 	engineSndId = sounds.get("light_tank_engine").loop();
					else if (ctrlUnit.type == TYPE.BUGGY) 		engineSndId = sounds.get("light_tank_engine").loop();
					loopingEngineSound = true;
				}
			}
			moveUnitToHere(_pathPoint.x, _pathPoint.y);
//			aiWait(1500);
		}
		
		// stop moving
		private void aiStop()
		{
			ctrlUnit.stopMovement();
			//aiWait(1500);
			if (PLAYAUDIO)
			{
				if (loopingEngineSound)
				{
					if (ctrlUnit.curDriveSpeed == 0.0f)
					{
						if (ctrlUnit.type == TYPE.LIGHT_TANK)   	sounds.get("light_tank_engine").stop();
						else if (ctrlUnit.type == TYPE.COPTER)  	sounds.get("copter_engine").stop();
						else if (ctrlUnit.type == TYPE.HEAVY_TANK)  sounds.get("light_tank_engine").stop();
						else if (ctrlUnit.type == TYPE.BUGGY)  		sounds.get("light_tank_engine").stop();
						loopingEngineSound = false;
					}
				}
			}
		}
		
		private Unit aiGetFarEnemy(Unit baseUnit)
		{
			Unit r = null;
			double distance = -1;
			for (int ui = 0; ui<gameMap.units.size(); ui++)
	        {
				Unit enemy = gameMap.units.get(ui);
				if (baseUnit != enemy && enemy.owner != baseUnit.owner)
				{
					double calc_distance = getUnitDistance(baseUnit, enemy);
					
					if (distance == -1)
					{
						distance = calc_distance;
						r = enemy;
					}
					else if (calc_distance > distance)
					{
						distance = calc_distance;
						r = enemy;
					}
				}
			}
			
			return r;
		}
		
		// ai run
		private void aiRun()
		{
			// ai CHOOSE unit phase
			if (phase == PHASE.PLAYER2_CHOOSE) // chooses random unit
			{
				if (ctrlUnit == null)
				{
					int ui = getRandom(0, gameMap.units.size() - 1);
					if (gameMap.units.get(ui) != null && !gameMap.units.get(ui).dead && gameMap.units.get(ui).owner == Unit.OWNER.PLAYER2)
					{
						chooseUnit(gameMap.units.get(ui));
						// offset to target point
						if(ctrlUnit.type == TYPE.LIGHT_TANK)
							offSet = 25;
						else if(ctrlUnit.type == TYPE.HEAVY_TANK)
							offSet = 15;
						else if(ctrlUnit.type == TYPE.BUGGY)
							offSet = 35;
						else
							offSet = 5;
					}
					aiWait(3000);
				}

				if (ctrlUnit != null && !aiHaveToWait())
				{
					nextPhase();
				}
			}
	// ai MOVE phase
			else if (phase == PHASE.PLAYER2_MOVE)
			{
				// choose nearest enemy as target
				if (aiTarget == null)
				{
//					System.out.println("select target...");
					aiTarget = getNearestEnemy(ctrlUnit);
					if(ctrlUnit.type == TYPE.COPTER || ctrlUnit.type == TYPE.BUGGY)
					{
						if(getUnitDistance(ctrlUnit, aiTarget) < ctrlUnit.maxShotDistance - 45.0f)
						aiTarget = aiGetFarEnemy(ctrlUnit);
					}
					// generate new pathfinding cause of new target
//					pathF = new Pathfinding(gameMap, ctrlUnit, aiTarget, gameMap.getName());
					pathP = new PatrikPathfinding(ctrlUnit, aiTarget, mWorld, staticPolygons);
//					aiTarget.getOrbit Radius winkel bla
//					System.out.println("new pathfinding...");
				}
				// ai movement
				if (ctrlUnit.getActionValue() > 0.0f && getUnitDistance(ctrlUnit, aiTarget) > ctrlUnit.maxShotDistance - 25.0f)
				{
					// first call in round
					if(pathPoint == null)
					{
//						System.out.println("get new point because pathPoint null...");
//						pathPoint = pathF.aiGiveFreePoint();
						if (!ctrlUnit.flying)
							pathPoint = pathP.getNextPoint(gameMap);
						else
							pathPoint = pathP.getGoalPoint();
					}
					// moving until point reached
					else if (aiGetUnitPointDist(ctrlUnit, pathPoint) > offSet)
					{
						aiMove(pathPoint);
						aiWait(1500);
					}
					// stop if point reached
					else
					{
//						System.out.println("point reached...stop");
						aiStop();
						aiWait(1500);
						pathPoint = null;
					}
				}
				// ai stop moving and go to attack phase if all movement is done
				else
				{
					pathPoint = null;
					aiStop();
					if (!aiHaveToWait())
					{
						nextPhase();
						pathP = null;
					}
				}
			}
	// ai ATTACK phase
			else if (phase == PHASE.PLAYER2_ATTACK)
			{
				if(ctrlUnit != null && aiTarget != null)
				{
					Vector2 ctrlU = new Vector2(ctrlUnit.centerX, ctrlUnit.centerY), targetU = new Vector2(aiTarget.centerX, aiTarget.centerY);
					if(aiGetDistance(ctrlU, targetU) > ctrlUnit.maxShotDistance - 25.0f)
					{
						enemyDidShoot = true;
					}
					else if(ctrlUnit.type == TYPE.COPTER || ctrlUnit.type == TYPE.BUGGY)
					{
						if(aiGetDistance(ctrlU, targetU) < ctrlUnit.maxShotDistance - 75.0f)
						{
							enemyDidShoot = true;
						}
					}
				}
				else
				{
					enemyDidShoot = true;
				}
				if (!enemyDidShoot)
				{
					if (!enemyDidAim)
					{
						// turn to target if no movement before
//						ctrlUnit.turnTo(aiTarget.centerX, aiTarget.centerY, gameMode);
						prepareShooting();
						enemyDidAim = true;
					}
					else if (enemyDidAim && (ctrlUnit.distanceToShoot >= ctrlUnit.maxShotDistance || aiTarget.getCollRect().contains((int)ctrlUnit.shotPoint.get(0).x, (int)ctrlUnit.shotPoint.get(0).y)))
					{
						shoot();
						pathPoint = null;
						enemyDidShoot = true;
						aiWait(1500);
					}
				}
				else if (enemyDidShoot)
				{				
					if (!aiHaveToWait())
					{
						enemyDidShoot = false;
						enemyDidAim = false;
						nextPhase();
					}
				}
			}
		}
	// --- AI END ---

	private void shoot()
	{
		if (ctrlUnit.type == Unit.TYPE.LIGHT_TANK)
		{
			ctrlUnit.shoot(allAtlas.findRegion("shot1"));
			if(PLAYAUDIO) sounds.get("light_tank_shot").play();
		}
		else if (ctrlUnit.type == Unit.TYPE.COPTER)
		{
			ctrlUnit.shoot(allAtlas.findRegion("rocket1"));
			if(PLAYAUDIO) sounds.get("light_tank_shot").play();
		}
		else if (ctrlUnit.type == Unit.TYPE.HEAVY_TANK)
		{
			ctrlUnit.shoot(allAtlas.findRegion("heavy-shot"));
			if(PLAYAUDIO) sounds.get("heavy_canon").play();
		}
		else if (ctrlUnit.type == Unit.TYPE.BUGGY)
		{
			ctrlUnit.shoot(allAtlas.findRegion("buggy-shot"));
			if(PLAYAUDIO) sounds.get("gatling-laser").play();
		}
	}
	
	private void keepCameraInBounds()
	{
		if (camera.position.x < (camera.zoom*(camera.viewportWidth/2.0f))){
			camera.position.x = (camera.zoom*(camera.viewportWidth/2.0f));
			if (moveToCam != null && !FREECAMERA) moveToCam.x = camera.position.x;
		}
		
		if (gameMap == null)
			System.out.println("STATE: "+gameState);
		
		if (camera.position.y > ((gameMap.getHeight()-((camera.zoom*camera.viewportHeight)/2.0f)))){
			camera.position.y = ((gameMap.getHeight()-((camera.zoom*camera.viewportHeight)/2.0f)));
			if (moveToCam != null && !FREECAMERA) moveToCam.y = camera.position.y;
		}
		if (camera.position.y < (camera.zoom*(camera.viewportHeight/2.0f))){
			camera.position.y = (camera.zoom*camera.viewportHeight/2.0f);
			if (moveToCam != null && !FREECAMERA) moveToCam.y = camera.position.y;
		}
		if (camera.position.x > ((gameMap.getWidth()-((camera.zoom*camera.viewportWidth)/2.0f)))){
			camera.position.x = ((gameMap.getWidth()-((camera.zoom*camera.viewportWidth)/2.0f)));
			if (moveToCam != null && !FREECAMERA) moveToCam.x = camera.position.x;
		}
	}
	
	private void renderStats()
	{
		float timeP1_p = getPercentage(Stats.timeSpendP1+Stats.timeSpendP2, Stats.timeSpendP1);
		float timeP2_p = getPercentage(Stats.timeSpendP1+Stats.timeSpendP2, Stats.timeSpendP2);
		
		font.draw(hudSpriteBatch, "Total time: "+(int)((Stats.timeSpendP1+Stats.timeSpendP2)/1000.0f)+" seconds", 100, Gdx.graphics.getHeight()*0.9f);
		font.draw(hudSpriteBatch, "Time Player 1 took: "+(int)(Stats.timeSpendP1/1000.0f)+" seconds"+"  ("+(int)timeP1_p+"%)", 100, Gdx.graphics.getHeight()*0.7f);
		font.draw(hudSpriteBatch, "Time Player 2 took: "+(int)(Stats.timeSpendP2/1000.0f)+" seconds"+"  ("+(int)timeP2_p+"%)", 100, Gdx.graphics.getHeight()*0.65f);
	}
	
	public static final float getPercentage(float all, float yourValue)
	{
		return ((100.0f/all)*yourValue);
	}
	
	private void renderActionBar(float percen)
	{
		if (percen < 1.0f)
			hudSpriteBatch.draw(menuAtlas.findRegion("action-bar-bg-trans"), (int)(Gdx.graphics.getWidth()*0.1f), hud_boxHeight*3, (int)(Gdx.graphics.getWidth()*0.8f), 12);
		
		if (percen > 0.0f)
			hudSpriteBatch.draw(menuAtlas.findRegion("action-bar-bg"), (int)(Gdx.graphics.getWidth()*0.1f), hud_boxHeight*3, (int)((Gdx.graphics.getWidth()*0.8f)*percen), 12);
		
		
		hudSpriteBatch.draw(menuAtlas.findRegion("action-bar-line"), (int)(Gdx.graphics.getWidth()*0.1f)-1, hud_boxHeight*3, 1, 12);
		hudSpriteBatch.draw(menuAtlas.findRegion("action-bar-line"), (int)(Gdx.graphics.getWidth()*0.1f)+(int)(Gdx.graphics.getWidth()*0.8f), hud_boxHeight*3, 1, 12);
	}
	
	private void renderHPBars()
	{
		for (int i = gameMap.units.size() - 1; i >= 0; i--)
        {
			Unit unit = gameMap.units.get(i);
			if (unit != null && unit.getHPChangeDisplayTime() > System.currentTimeMillis())
			{
				long dif = unit.getHPChangeDisplayTime() - System.currentTimeMillis(); 
				if (dif < 1000)
				{
					float alpha = (dif/1000.0f);
					
					Color color = spriteBatch.getColor();
			        float oldAlpha = color.a;
			        color.a = alpha;
			        
			        spriteBatch.setColor(color);
			        renderHPBar(unit);
			        
			        color.a = oldAlpha;
			        spriteBatch.setColor(color);
				}
				else
				{
					renderHPBar(unit);
				}
			}
        }
	}
	
	private void renderSegmentSelector(Unit unit)
	{
		if (selectWait > 0)
		{
			float p = getPercentageValue(timeToWaitForSelect, System.currentTimeMillis() - selectWait);
			
			if (p > 0.01f)
			{
				TextureRegion tr = allAtlas.findRegion("selector-segment");
				for (int i = 180;i<180+(360*p);i+=8)
				{
					spriteBatch.setColor(Color.WHITE);
					spriteBatch.draw(tr, unit.centerX+4, unit.getY()-unit.getHeight()+4, 0, 0, 
							tr.getRegionWidth()/2f, tr.getRegionHeight()/2f, 
							1f, 1f, i);
					
					spriteBatch.setColor(Color.BLACK);
					spriteBatch.draw(tr, unit.centerX, unit.getY()-unit.getHeight(), 0, 0, 
							tr.getRegionWidth()/2f, tr.getRegionHeight()/2f, 
							1f, 1f, i);
					
				}
				spriteBatch.setColor(1, 1, 1, 1);
			}
			
			//fix for sliding offtarget
			if (p >= 0.85f || System.currentTimeMillis() - selectWait >= timeToWaitForSelect)
			{
				nextPhase();
				selectWait = 0;
			}
		}
	}
	
	private void renderHPBar(Unit unit)
	{
		if (unit == null) return;
		float percen = unit.getHP() / unit.getMaxHP();
		float yp = unit.getHeight();
		if (unit.getWidth() < unit.getHeight()) yp = unit.getWidth();
		float _y = unit.getY()+yp+10;
		
		if (percen < 1.0f)
			spriteBatch.draw(menuAtlas.findRegion("hp-bar-bg-trans"), unit.getX(), _y, (unit.getWidth()), 8);
		
		if (percen > 0.0f)
		{
			if (percen > 0.4f)
				spriteBatch.draw(menuAtlas.findRegion("hp-bar-bg"), unit.getX(), _y, (unit.getWidth())*percen, 8);
			else
				spriteBatch.draw(menuAtlas.findRegion("hp-bar-bg-red"), unit.getX(), _y, (unit.getWidth())*percen, 8);
		}

		spriteBatch.draw(menuAtlas.findRegion("hp-bar-line"), unit.getX()-1, 					_y);
		spriteBatch.draw(menuAtlas.findRegion("hp-bar-line"), (unit.getX()+unit.getWidth())-1,  _y);
	}
	
	private void addSmoke(float _x, float _y, float _size)
	{
		if (PLAYAUDIO) sounds.get("explosion_big").play();
//		SimpleAnimation sa = new SimpleAnimation(smoke_ani_1, _x, _y, 2, SimpleAnimation.ONCE, _size);
//		sa.x+= 20;
//		sa.y-= 15;
//		ani.add(sa);
		quake(7, 7, 200);
	}
	
//	private void addExplo(float _x, float _y, float _size)
//	{
//		if (PLAYAUDIO) sounds.get("explosion").play();
////		SimpleAnimation sa = new SimpleAnimation(explo_ani_1, _x, _y, 2, SimpleAnimation.ONCE, _size);
////		ani.add(sa);
//		quake(20, 20, 440);
//	}
	
	private void chooseUnit(Unit in_unit)
	{
		if(PLAYAUDIO) sounds.get("choose").play();
		ctrlUnit = in_unit;
		moveToCam = new Vector2();
		moveToCam.x = ctrlUnit.centerX;
		moveToCam.y = ctrlUnit.centerY;
		keepCameraInBounds();
		//if single player game
		if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
		{
			drawChooseConfirm = true;
		}
		
		if (gameMode == GAMEMODE.LOCAL_2PLAYER)
		{
			if (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK)
			{
				drawChooseConfirm = true;
			}
		}
	}
	
	private int getRelativeMouseX()
	{
		return (int)(Gdx.input.getX()*camera.zoom)+(int)(camera.position.x)-(int)((camera.viewportWidth*camera.zoom)/2.0f);
	}
	
	private int getRelativeMouseY()
	{
		return (int)(Gdx.input.getY()*camera.zoom)+(int)(camera.position.y)-(int)((camera.viewportHeight*camera.zoom)/2.0f);
	}
	
	private void moveUnitToHere(float _x, float _y)
	{
		ctrlUnit.moveTo(_x, _y, gameMap, camera);
		ctrlUnit.turnTo(_x, _y, gameMode);
		holdUnitInMap(ctrlUnit);
	}
	
	private void goBack()
	{
		if (gameState == GAMESTATE.INGAME)
		{
			if (!showPauseMenu)
			{
				showPauseMenu = true;
				if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
				{
					if (timeMeasurePoint > 0)	Stats.timeSpendP1+= System.currentTimeMillis()-timeMeasurePoint;
				}
				else
				{
					if (timeMeasurePoint > 0)	Stats.timeSpendP2+= System.currentTimeMillis()-timeMeasurePoint;
				}
			}
			else
			{
				showPauseMenu = false;
			}
		}
		else if (gameState == GAMESTATE.MAPCHOICE)
		{
			gameState = GAMESTATE.STARTMENU;
		}
		else if (gameState == GAMESTATE.STARTMENU)
		{
			Gdx.app.exit();
		}
	}
	
	private void goMenuButton()
	{
		if (gameState == GAMESTATE.INGAME)
		{
			if 		(gameMode == GAMEMODE.AI_ENEMY) gameMode = GAMEMODE.LOCAL_2PLAYER;
			else if (gameMode == GAMEMODE.LOCAL_2PLAYER) gameMode = GAMEMODE.AI_ENEMY;
		}
	}
	
	private void clearGame()
	{
		if (PLAYAUDIO)
		{
			music.stop();
			for (Sound snd : sounds.values())
			{
				snd.stop();
			}
		}
		ctrlUnit = null;

		gameOver = false;
		showPauseMenu = false;
		gameOver = false;
		
		player1UnitAmount = 0;
		player2UnitAmount = 0;

		
	}
	
	private void addParticleEffect(String in, float x, float y)
	{
		//lets try not to screw up various additive layers on normal additive maps
		if (!gameMap.useAdditiveBlending)
		{
			ParticleManager.addEffect(in, x, y, false);
		}
		else
		{
			ParticleManager.addEffect(in, x, y);
		}
	}
	
	private void checkInputIngame()
	{
		if (Gdx.input.isKeyPressed(Keys.F))
		{
			addParticleEffect("rocketfireball", getRelativeMouseX(), getRelativeMouseY());
		}
		
		if (Gdx.input.isKeyPressed(Keys.R))
		{
			System.out.println("RAM: "+((int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024)+"MB / "
					+(int)(Runtime.getRuntime().totalMemory()/1024/1024)+"MB");
		}
		
		if (Gdx.input.isKeyPressed(Keys.Q))
		{
//			camera.rotate(1, 0, 0, 2);
			quake(5, 5, 1000);
		}
		
		if (!debug_mapdesign && Gdx.input.isTouched())
		{
			if (!gameOver && !showPauseMenu)
    		{
				if (phase == PHASE.PLAYER1_CHOOSE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_CHOOSE))
				{
					Unit.OWNER leOwner = (phase == PHASE.PLAYER1_CHOOSE) ? Unit.OWNER.PLAYER1 : Unit.OWNER.PLAYER2;
					for (int ui = 0; ui<gameMap.units.size(); ui++)
			        {
						Unit unit = gameMap.units.get(ui);
						if (unit.owner == leOwner && unit.getCollRect().contains(getRelativeMouseX(), getRelativeMouseY()))
						{
							if (ctrlUnit != null && ctrlUnit == unit)
							{
								drawChooseConfirm = false;
								if (selectWait == 0) selectWait = System.currentTimeMillis();
								if (System.currentTimeMillis() - selectWait >= timeToWaitForSelect)
								{
									nextPhase();
									selectWait = 0;
								}
							}
							break;
						}
					}
				}
			}
			
			
			if (phase == PHASE.PLAYER1_MOVE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_MOVE))
			{
				if (ctrlUnit != null)
				{
					if (attackButtonTouched())
					{
						prepareShooting();
						if(phase == PHASE.PLAYER1_MOVE)phase = PHASE.PLAYER1_ATTACK;
						if(phase == PHASE.PLAYER2_MOVE)phase = PHASE.PLAYER2_ATTACK;
						return;
					}
					else if(cancelButtonTouched())
					{
						nextPhase();
						nextPhase();
						timeToNextPhase = System.currentTimeMillis()+2500;
					}
				}
			}
		}

		if (!showPauseMenu)
		{
			if (phase == PHASE.PLAYER1_MOVE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_MOVE))
			{
				if (ctrlUnit != null)
				{
					if (ctrlUnit.getActionValue() > 0.0f && Gdx.input.isTouched() && !ctrlUnit.getCollRect().contains(getRelativeMouseX(), getRelativeMouseY()))
					{
						if (PLAYAUDIO)
						{
							if (!loopingEngineSound)
							{
								if (ctrlUnit.type == TYPE.LIGHT_TANK)   	engineSndId = sounds.get("light_tank_engine").loop();
								else if (ctrlUnit.type == TYPE.COPTER)  	engineSndId = sounds.get("copter_engine")	 .loop();
								else if (ctrlUnit.type == TYPE.HEAVY_TANK)  engineSndId = sounds.get("light_tank_engine").loop();
								else if (ctrlUnit.type == TYPE.BUGGY)  		engineSndId = sounds.get("light_tank_engine").loop();
								loopingEngineSound = true;
							}
						}
						moveUnitToHere(getRelativeMouseX(), getRelativeMouseY());
					}
					else
					{
						ctrlUnit.stopMovement();
						if (PLAYAUDIO)
						{
							if (loopingEngineSound)
							{
								if (ctrlUnit.curDriveSpeed == 0.0f)
								{
									if (ctrlUnit.type == TYPE.LIGHT_TANK)   	sounds.get("light_tank_engine").stop();
									else if (ctrlUnit.type == TYPE.COPTER)  	sounds.get("copter_engine")	   .stop();
									else if (ctrlUnit.type == TYPE.HEAVY_TANK)  sounds.get("light_tank_engine").stop();
									else if (ctrlUnit.type == TYPE.BUGGY)  		sounds.get("light_tank_engine").stop();
									loopingEngineSound = false;
								}
							}
						}
					}
					
					if (ctrlUnit.getActionValue() <= 0.0f)
					{
						if (!actionEmpty && !Gdx.input.isTouched())
						{
//							System.out.println("action empty");
							actionEmpty = true;
							timeToNextPhase = System.currentTimeMillis()+400;
						}
					}
				}
			}
		}

		if (phase == PHASE.PLAYER1_ATTACK || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_ATTACK))
		{
			if (timeToNextPhase == -1)
			{
				if (ctrlUnit != null)
				{
					if (ctrlUnit.doneShooting && ctrlUnit.projs.size() == 0)
					{
						timeToNextPhase = System.currentTimeMillis()+2500;
						ctrlUnit.doneShooting = false;
					}
					
					if (!ctrlUnit.doneShooting)
					{
						if (!ctrlUnit.targeting && !ctrlUnit.shooting)
						{
							if(attackButtonTouched())prepareShooting();
							if(cancelButtonTouched())nextPhase();
						}else if (ctrlUnit.targeting && !ctrlUnit.shooting)
						{
							if (ctrlUnit.distanceToShoot >= ctrlUnit.maxShotDistance)
							{
								shoot();
							}
						}
					}
				}
				else
				{
					// ctrlUnit is null
					// I guess it did prematurely
					//lets switch phases anyway
					timeToNextPhase = System.currentTimeMillis()+2500;
				}
			}
			else
			{
				//do nothing, will be changing phase
			}
		}
	}
	
	private boolean attackButtonTouched()
	{
		if(Gdx.input.justTouched() && sprActionButton.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY()))
		{
			return true;
		}
		return false;
	}
	
	private boolean cancelButtonTouched()
	{
		if(Gdx.input.justTouched() && sprCancelButton.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY()))
		{
			return true;
		}
		return false;
	}
	
	private void prepareShooting()
	{
		if (ctrlUnit.type == Unit.TYPE.LIGHT_TANK)
			ctrlUnit.setShootingDirectionPoint(1);
		else if (ctrlUnit.type == Unit.TYPE.COPTER)
			ctrlUnit.setShootingDirectionPoint(1);
		else if (ctrlUnit.type == Unit.TYPE.HEAVY_TANK)
			ctrlUnit.setShootingDirectionPoint(1);
		else if (ctrlUnit.type == Unit.TYPE.BUGGY)
			ctrlUnit.setShootingDirectionPoint(1);
	}
	
	private void computeIngame()
	{
		if (gameMode == GAMEMODE.AI_ENEMY)
		{
			if (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK)
			{
				if (movieBorderAlpha < 1.0f)
					movieBorderAlpha +=0.01f;
				if (movieBorderAlpha > 1.0f) movieBorderAlpha = 1.0f;
			}
			else if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
			{
				if (movieBorderAlpha > 0.0f)
					movieBorderAlpha -=0.01f;
				if (movieBorderAlpha < 0.0f) movieBorderAlpha = 0.0f;
			}
		}

		
		if (phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER2_MOVE)
		{
			if (ctrlUnit != null)
			{
				if (loopingEngineSound && engineSndId != -1)
				{
					if (ctrlUnit.type == TYPE.LIGHT_TANK)
						sounds.get("light_tank_engine").setPitch(engineSndId, ctrlUnit.curDriveSpeed);
					else if (ctrlUnit.type == TYPE.COPTER)
						sounds.get("copter_engine").setPitch(engineSndId, ctrlUnit.curDriveSpeed);
					else if (ctrlUnit.type == TYPE.HEAVY_TANK)
						sounds.get("light_tank_engine").setPitch(engineSndId, ctrlUnit.curDriveSpeed);
					else if (ctrlUnit.type == TYPE.BUGGY)
						sounds.get("light_tank_engine").setPitch(engineSndId, ctrlUnit.curDriveSpeed);
				}	
			}
		}
		else if (phase == PHASE.PLAYER1_ATTACK || phase == PHASE.PLAYER2_ATTACK)
		{
			if (ctrlUnit != null && ctrlUnit.shooting && ctrlUnit.projs.size() > 0)
			{
				for (int pid = 0; pid < ctrlUnit.projs.size(); pid++)
				{
					if (ctrlUnit.projs.get(pid) == null) continue;
					float tx = ctrlUnit.projs.get(pid).centerX;
					float ty = ctrlUnit.projs.get(pid).centerY;
					
					moveToCam = null;
					if (!FREECAMERA)
					{
						camera.position.x = tx;
						camera.position.y = ty;
					}
					
					Vector2 projectileImpactPoint = ctrlUnit.projectileReachedTarget(pid);
					
					if (projectileImpactPoint != null)
					{
						if (ctrlUnit.type == Unit.TYPE.LIGHT_TANK)
						{
							if(PLAYAUDIO) sounds.get("explosion").play();
							addParticleEffect("expl04",tx,ty);
							addSmoke(tx, ty, 1.0f);
						}
						else if (ctrlUnit.type == Unit.TYPE.COPTER)
						{
							if(PLAYAUDIO) sounds.get("explosion").play();
							addParticleEffect("expl03",tx,ty);
							addSmoke(tx, ty, 1.0f);
						}
						else if (ctrlUnit.type == Unit.TYPE.BUGGY)
						{
							if(PLAYAUDIO) sounds.get("ricochet").play();
							addParticleEffect("spark",tx,ty);
						}
						else if (ctrlUnit.type == Unit.TYPE.HEAVY_TANK)
						{
							if(PLAYAUDIO) sounds.get("explosion").play();
							if(PLAYAUDIO) sounds.get("explosion_big").play();
							addParticleEffect("expl01", tx, ty);
							addSmoke(tx, ty, 1.0f);
						}
						
						for (int i = gameMap.units.size() - 1; i >= 0; i--)
				        {
							Unit unit = gameMap.units.get(i);
							Rectangle checkie = new Rectangle(projectileImpactPoint.x-(ctrlUnit.damageRange/2.0f), projectileImpactPoint.y-(ctrlUnit.damageRange/2.0f), ctrlUnit.damageRange, ctrlUnit.damageRange);
							
							if (unit.getCollRect().overlaps(checkie)) // too precise
							{
								float dx = tx - unit.centerX;
								float dy = ty - unit.centerY;
								double distance = SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
								float dmg = (float)(ctrlUnit.damageRange-distance)*ctrlUnit.damageMultipl;
								unit.changeHP(-dmg, allAtlas);
								
								//if (unit == ctrlUnit && unit.getHP() == 0) do something()
								//actually, doesnt matter if you kill yourself, when you can only attack once anyway until the phase changes anyway.
							}
				        }
					}
				}
			}
		}
		
		if (debugSndDelay > 0)
		{
			debugSndDelay++;
			if (debugSndDelay > 40) debugSndDelay = 0;
		}
		
		adjustCamera();
		if (timeToNextPhase != -1)
		{
			if (System.currentTimeMillis() >= timeToNextPhase)
			{
				nextPhase();
				timeToNextPhase = -1;
			}
		}
	}
	
	private boolean checkGameOver()
	{
		if (player1UnitAmount == 0)
		{
			if (player2UnitAmount == 0)  winner = "DRAW";
			else   winner = "WINNER:  PLAYER 2";
		}
		else if (player2UnitAmount == 0)  winner = "WINNER:  PLAYER 1";
		
		if (player1UnitAmount == 0 || player2UnitAmount == 0) return true;
		else return false;
	}
	
	private double getUnitDistance(Unit one, Unit two)
	{
		int dx = (int)(one.centerX-two.centerX);
		int dy = (int)(one.centerY-two.centerY);
		return SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
	}
	
	private Unit getNearestEnemy(Unit baseUnit)
	{
		Unit r = null;
		double distance = -1;
		for (int ui = 0; ui<gameMap.units.size(); ui++)
        {
			Unit enemy = gameMap.units.get(ui);
			if (baseUnit != enemy && enemy.owner != baseUnit.owner)
			{
				double calc_distance = getUnitDistance(baseUnit, enemy);
				
				if (distance == -1)
				{
					distance = calc_distance;
					r = enemy;
				}
				else if (calc_distance < distance)
				{
					distance = calc_distance;
					r = enemy;
				}
			}
		}
		
		return r;
	}
	
	public static final int getRandom(int minimum, int maximum)
    {
        return (int)(Math.random()*((maximum+1)-minimum)+minimum);
    }
	
	public static final float getFloatRandom(float minimum, float maximum)
    {
        return ((float)Math.random()*((maximum+1.0f)-minimum)+minimum);
    }
	
	@Override
	public void resize(int width, int height)
	{
		
	}
	
	public void runIngame()
	{
		float delta = Gdx.graphics.getDeltaTime();
		
		if (!gameOver)
		{
			if(debug_mapdesign && lastDebugReloadTime > 0)
			{
				if (System.currentTimeMillis() - lastDebugReloadTime > 800)
				{
					debugReloadMap();
					lastDebugReloadTime = System.currentTimeMillis();
				}
			}
			
			checkInputIngame();
			if (gameMode == GAMEMODE.AI_ENEMY && (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK))
			{
				aiRun();
			}
			computeIngame();
		}
		
//		
//		if (_delta < 0.001f) _delta = 0.016f;
//        mWorld.step(1.0f/Gdx.graphics.getFramesPerSecond(), 1, 1);
//		mWorld.step(_delta, 1, 1);
		

//		controller.update();
		// update camera
		

//		//RENDERING
		if (!alreadyRendered)
		{
			if (fps60) mWorld.step(0.016f, 1, 1);
			else	   mWorld.step(0.032f, 1, 1);
			
			camera.update();
			spriteBatch.setProjectionMatrix(camera.combined);
			
			spriteBatch.begin();
		
			gameMap.render(spriteBatch, camera);
		}
			
		for (int i = gameMap.units.size() - 1; i >= 0; i--)
        {
			Unit unit = gameMap.units.get(i);
			if (!unit.dead)
			{
				unit.compute(gameMap, camera);
				//we render in the gamemap renderlist
				
				for (int pi = 0; pi < gameMap.powerups.size(); pi++)
				{
					Powerup p = gameMap.powerups.get(pi);
					
					if (unit.getCollRect().overlaps(p.getCollRect()))
					{
						if (Game.PLAYAUDIO) sounds.get("ping").play();
						//TODO Add Particle Effect ?
						
						if (p.getType() == Powerup.TYPE.SMALL_HEALTH)
						{
							unit.changeHP(unit.getMaxHP()*0.25f, allAtlas);
						}
						
						p = null;
						gameMap.powerups.remove(pi);
					}
				}
				
				if (debug_mapdesign && !alreadyRendered && !unit.flying && unit.isWithinFrustum(camera))
				{
					smallFont.draw(spriteBatch, ""+(int)unit.getX()+" , "+(int)unit.getY()+" @ "+(int)unit.getRotation(), unit.getX(), unit.getY());
        			smallFont.draw(spriteBatch, ""+((int)unit.getY()+(int)unit.getHeight()), unit.getX(), unit.getY()+smallFont.getLineHeight());
        			smallFont.draw(spriteBatch, ""+unit.owner, unit.getX(), unit.getY()+(smallFont.getLineHeight()*2));
				}
			}
			else
			{
				if (!alreadyRendered)
				{
					addParticleEffect("expl02", unit.centerX, unit.centerY);//expl02
	//				particleEffect.start();
	//				particleEffect.setPosition(unit.centerX, unit.centerY);
				//	addExplo(unit.centerX, unit.centerY, 1.5f);
					quake(15,15,800);
					if (unit == ctrlUnit) ctrlUnit = null;
					if (unit.owner == Unit.OWNER.PLAYER1) player1UnitAmount--;
					else if (unit.owner == Unit.OWNER.PLAYER2) player2UnitAmount--;
					if (player1UnitAmount < 0) player1UnitAmount = 0;
					if (player2UnitAmount < 0) player2UnitAmount = 0;
					unit.targeting = false;
					unit.shooting = false;
					unit.doneShooting = false;
					unit.projs.clear();
					unit.shotPoint.clear();
					unit.shotPointSpeed.clear();
					unit.distanceToShoot = 0;
					unit = null;
	    			gameMap.units.remove(i);
	    			if (!gameOver) gameOver = checkGameOver();
				}
			}
        }
		
		if (!alreadyRendered) // in fps30 we dont want to render twice
		{
			for (int ui = 0; ui<gameMap.units.size(); ui++)
	        {
				Unit unit = gameMap.units.get(ui);
				
	            if (unit.flying) 
	            {
	    			if (unit.isWithinFrustum(camera))
	    			{
	    				unit.renderShadow(spriteBatch,38,32);
	    	            unit.render(spriteBatch);
	    	            
	    	            if (debug_mapdesign)
	    	            {
	    	            	smallFont.draw(spriteBatch, ""+(int)unit.getX()+" , "+(int)unit.getY()+" @ "+(int)unit.getRotation(), unit.getX(), unit.getY());
	    	            }
	    			}
	            }
	        }
			
			if (debug_mapdesign)
			{
				if (debugMapDesignElement != null)	debugMapDesignElement.render(spriteBatch, designColor);
				
				for (int i = gameMap.obstacles.size() - 1; i >= 0; i--)
		        {
					Element el = gameMap.obstacles.get(i);
					if (el.isWithinFrustum(camera))
					{
						smallFont.draw(spriteBatch, ""+(int)el.getX()+" , "+(int)el.getY()+" @ "+(int)el.getRotation(), el.getX(), el.getY());
						smallFont.draw(spriteBatch, ""+((int)el.getY()+(int)el.getHeight()), el.getX(), el.getY()+smallFont.getLineHeight());
					}
				}
				
				for (int i = gameMap.floors.size() - 1; i >= 0; i--)
		        {
					Element el = gameMap.floors.get(i);
					if (el.isWithinFrustum(camera))
					{
						smallFont.draw(spriteBatch, ""+(int)el.getX()+" , "+(int)el.getY()+" @ "+(int)el.getRotation(), el.getX(), el.getY());
						smallFont.draw(spriteBatch, ""+((int)el.getY()+(int)el.getHeight()), el.getX(), el.getY()+smallFont.getLineHeight());
					}
				}
				
				for (int i = gameMap.powerups.size() - 1; i >= 0; i--)
		        {
					Element el = gameMap.powerups.get(i);
					if (el.isWithinFrustum(camera))
					{
						smallFont.draw(spriteBatch, ""+(int)el.getX()+" , "+(int)el.getY()+" @ "+(int)el.getRotation(), el.getX(), el.getY());
						smallFont.draw(spriteBatch, ""+((int)el.getY()+(int)el.getHeight()), el.getX(), el.getY()+smallFont.getLineHeight());
					}
				}
			}
			
			if (ctrlUnit != null)
			{
				if (ctrlUnit.shotPointSpeed != null)
				{
					for (Vector2 v : ctrlUnit.shotPoint)
					{
						spriteBatch.draw(menuAtlas.findRegion("attack-circle"), v.x-(ctrlUnit.damageRange/4.0f), v.y-(ctrlUnit.damageRange/4.0f), (ctrlUnit.damageRange/2.0f), (ctrlUnit.damageRange/2.0f));
						//ctrlUnit.distanceToShoot >= ctrlUnit.maxShotDistance
					}
				}
				
				if (ctrlUnit.shooting && ctrlUnit.projs.size() > 0)
				{
					ctrlUnit.renderProjectile(spriteBatch);
				}
			}
			
			ParticleManager.updateAndRender(spriteBatch, delta);
			
			renderHPBars();
			
			if (ctrlUnit != null)
			{
				renderHPBar(ctrlUnit);
				
				renderSegmentSelector(ctrlUnit);
			}
			
			if (phase == PHASE.PLAYER1_CHOOSE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_CHOOSE))
			{
				TextureRegion tr = allAtlas.findRegion("unitcorner");
				
				tempColor = spriteBatch.getColor();
				spriteBatch.setColor(new Color(1,1,1,0.5f));
				
				for (int ui = 0; ui<gameMap.units.size(); ui++)
		        {
					Unit unit = gameMap.units.get(ui);
					if (phase == PHASE.PLAYER1_CHOOSE)
					{
						if (unit.owner != Unit.OWNER.PLAYER1) continue;
					}
					else
					{
						if (unit.owner != Unit.OWNER.PLAYER2) continue;
					}

					spriteBatch.draw(tr, unit.getX()-tr.getRegionWidth(), unit.getY()-tr.getRegionHeight(), 0, 0, 
							tr.getRegionWidth(), tr.getRegionHeight(), 
							1f, 1f, 0);
					
					spriteBatch.draw(tr, unit.getX()+unit.biggestSize+tr.getRegionWidth(), unit.getY()-tr.getRegionHeight(), 0, 0, 
							tr.getRegionWidth(), tr.getRegionHeight(), 
							1f, 1f, 90);
					
					spriteBatch.draw(tr, unit.getX()-tr.getRegionWidth(), unit.getY()+unit.biggestSize+tr.getRegionHeight(), 0, 0, 
							tr.getRegionWidth(), tr.getRegionHeight(), 
							1f, 1f, 270);
					
					spriteBatch.draw(tr, unit.getX()+unit.biggestSize+tr.getRegionWidth(), unit.getY()+unit.biggestSize+tr.getRegionHeight(), 0, 0, 
							tr.getRegionWidth(), tr.getRegionHeight(), 
							1f, 1f, 180);
		        }
				
				spriteBatch.setColor(tempColor);
			}
			
			spriteBatch.end();
			
			renderHUD();
		}
		
		
		if (DEVKIT && renderBox2D)
		{
			if (pathP != null)
			{
				shaper.setProjectionMatrix(camera.combined);
				shaper.begin(ShapeType.Line);
				shaper.setColor(Color.BLUE);
				
				ArrayList<Vector2> s = pathP.getSteps();
				
				if (pathP.getTotalSteps() > 1)
				{
					for (int i = 1;i<s.size();i++)
					{
						Vector2 one = s.get(i-1);
						Vector2 two = s.get(i);
						shaper.line(one.x, one.y, two.x, two.y);
					}
				}
				
				shaper.end();
				
				
				shaper.begin(ShapeType.Filled);
				
				if (pathP.getTotalSteps() > 0)
				{
					for (int i = 0;i<s.size();i++)
					{
						Vector2 po = s.get(i);
						shaper.circle(po.x, po.y, 6);
					}
					
				}
				
				shaper.end();
				
				shaper.begin(ShapeType.Line);
				for (int i = 0;i<s.size();i++)
				{
					Vector2 po = s.get(i);
					float[] verts = new float[8];
					verts[0] = po.x-(pathP.stepSize/2f);
					verts[1] = po.y-(pathP.stepSize/2f);
					
					verts[2] = po.x-(pathP.stepSize/2f)+pathP.stepSize;
					verts[3] = po.y-(pathP.stepSize/2f);
					
					verts[4] = po.x-(pathP.stepSize/2f)+pathP.stepSize;
					verts[5] = po.y-(pathP.stepSize/2f)+pathP.stepSize;
					
					verts[6] = po.x-(pathP.stepSize/2f);
					verts[7] = po.y-(pathP.stepSize/2f)+pathP.stepSize;
					
	//				shaper.rect(po.x-(pathP.stepSize/2f), po.y-(pathP.stepSize/2f), pathP.stepSize, pathP.stepSize);
					shaper.polygon(verts);
				}
				
				shaper.setColor(Color.BLACK);
				for (int i = 0;i<pathP.getPolys().size();i++)
				{
					Polygon p = pathP.getPolys().get(i);
					float[] ble = p.getVertices();
					shaper.polygon(ble);
				}
				shaper.end();
				
				shaper.begin(ShapeType.Filled);
				
				shaper.setColor(Color.BLACK);
					shaper.circle(pathP.getStartPoint().x, pathP.getStartPoint().y, 12);
					shaper.circle(pathP.getGoalPoint().x, pathP.getGoalPoint().y, 12);
				shaper.setColor(Color.ORANGE);
					shaper.circle(pathP.getStartPoint().x, pathP.getStartPoint().y, 4);
					shaper.circle(pathP.getGoalPoint().x, pathP.getGoalPoint().y, 4);
				shaper.end();
				
			}
			else
			{
				spriteBatch.begin();
					debugRenderer.render(mWorld, camera.combined.scl(PIXELS_PER_METER));
				spriteBatch.end();
			}
		}
		
	}
	
	public void renderRangeBar()
	{
		if (ctrlUnit != null)
		{
			float length = (sprActionButton.getWidth())-6;
			float height = 22*scaleAmount;
			
			float posX = Gdx.graphics.getWidth()-((sprActionButton.getWidth())+((-length+3)*actionButtonFadeIn));
			float posY = sprActionButton.getY()+(sprActionButton.getHeight())+3;
			
			
			float percent = getSmallFloatPercentageValue((int)ctrlUnit.maxShotDistance,(int)ctrlUnit.distanceToShoot);
			
			if (ctrlUnit.shotPointSpeed != null)
			{
				//TODO marker
				for (int i = 0; i<ctrlUnit.shotPoint.size();i++) {
					Primitives.fillRect(hudSpriteBatch, posX, posY, length*percent, height);
				}
			}
			
			Primitives.drawRect(hudSpriteBatch, posX, posY, length, height);
			font.setColor(Color.BLACK);
			font.draw(hudSpriteBatch, "Range", posX+4, posY+font.getLineHeight()-3);
		}
		font.setColor(Color.WHITE);
	}

	public void renderHUD()
	{
		hudSpriteBatch.begin();

		//HUD
		if (ctrlUnit != null)
		{
			if (phase == PHASE.PLAYER1_MOVE || (phase == PHASE.PLAYER2_MOVE && gameMode == GAMEMODE.LOCAL_2PLAYER))
			{	//we dont render action bar for enemy movements
				renderActionBar(ctrlUnit.getActionValue());
			}
		}

		if (movieBorderAlpha == 0.0f)
		{
			Gdx.graphics.getGL10().glEnable(GL10.GL_BLEND);
			Gdx.graphics.getGL10().glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//			if (gameMode != GAMEMODE.AI_ENEMY)
//			{
//				tempColor.set(0, 0, 0, 0.6f);
//				Primitives.drawLine(hudSpriteBatch, 0, hud_boxHeight*2, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, hud_boxHeight*2, tempColor);
//				Primitives.drawLine(hudSpriteBatch, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, hud_boxHeight*2, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, 0, tempColor);
//				Primitives.drawLine(hudSpriteBatch, Gdx.graphics.getWidth()-(hud_phaseBoxWidth*3), Gdx.graphics.getHeight()-hud_boxHeight-1, Gdx.graphics.getWidth()-(hud_phaseBoxWidth*3), Gdx.graphics.getHeight(), tempColor);
//				Primitives.drawLine(hudSpriteBatch, (hud_phaseBoxWidth*3), Gdx.graphics.getHeight()-hud_boxHeight-1, (hud_phaseBoxWidth*3), Gdx.graphics.getHeight(), tempColor);
//				Primitives.drawLine(hudSpriteBatch, 0, Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth*3, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
//				Primitives.drawLine(hudSpriteBatch, Gdx.graphics.getWidth()-(hud_phaseBoxWidth*3), Gdx.graphics.getHeight()-hud_boxHeight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
//			}
			
			tempColor.set(0, 0, 0, 0.4f);
			Primitives.fillRect(hudSpriteBatch, 0, 0, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, hud_boxHeight*2, tempColor);
			
			if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
				Primitives.fillRect(hudSpriteBatch, 0, 0+hud_boxHeight, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, hud_boxHeight, tempColor);
			else
				Primitives.fillRect(hudSpriteBatch, 0, 0, 0+5+20+font.getBounds("PLAYER 2").width+font.getBounds("Units: 00").width, hud_boxHeight, tempColor);
	
			/*
			//Player 1 Phases
			if (phase == PHASE.PLAYER1_CHOOSE)	tempColor.set(0, 0, 1.0f, 0.4f);
			else								tempColor.set(0, 0, 0, 0.4f);
			Primitives.fillRect(hudSpriteBatch, 0*hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
			
			if (phase == PHASE.PLAYER1_MOVE)	tempColor.set(0, 0, 1.0f, 0.4f);
			else								tempColor.set(0, 0, 0, 0.4f);
			Primitives.fillRect(hudSpriteBatch, 1*hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
			
			if (phase == PHASE.PLAYER1_ATTACK)	tempColor.set(0, 0, 1.0f, 0.4f);
			else								tempColor.set(0, 0, 0, 0.4f);
			Primitives.fillRect(hudSpriteBatch, 2*hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
			
			if (gameMode != GAMEMODE.AI_ENEMY)
			{
				//Player 2 Phases
				if (phase == PHASE.PLAYER2_CHOOSE)	tempColor.set(1.0f, 0, 0.0f, 0.4f);
				else								tempColor.set(0, 0, 0, 0.4f);
				Primitives.fillRect(hudSpriteBatch, Gdx.graphics.getWidth()-(3*hud_phaseBoxWidth), Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
				
				if (phase == PHASE.PLAYER2_MOVE)	tempColor.set(1.0f, 0, 0.0f, 0.4f);
				else								tempColor.set(0, 0, 0, 0.4f);
				Primitives.fillRect(hudSpriteBatch, Gdx.graphics.getWidth()-(2*hud_phaseBoxWidth), Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
				
				if (phase == PHASE.PLAYER2_ATTACK)	tempColor.set(1.0f, 0, 0.0f, 0.4f);
				else								tempColor.set(0, 0, 0, 0.4f);
				Primitives.fillRect(hudSpriteBatch, Gdx.graphics.getWidth()-(1*hud_phaseBoxWidth), Gdx.graphics.getHeight()-hud_boxHeight, hud_phaseBoxWidth, Gdx.graphics.getHeight()-hud_boxHeight, tempColor);
			}
			*/
		}
		
		if (phase == PHASE.PLAYER1_CHOOSE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_CHOOSE))
		{
			// changed getWidth()/2 to 1.5
			actionButtonFading = false;
			if (drawChooseConfirm)
			{
				font.setColor(Color.BLACK);
				font.draw(hudSpriteBatch, "Hold unit to select", (Gdx.graphics.getWidth()/2f)-(font.getBounds("Hold unit to select").width/2), hud_boxHeight*2);
			}
			else
			{
				font.setColor(Color.BLACK);
				font.draw(hudSpriteBatch, "Tap unit to check", (Gdx.graphics.getWidth()/2f)-(font.getBounds("Tap unit to check").width/2), hud_boxHeight*2);
			}
		}
		else if ( (phase == PHASE.PLAYER1_MOVE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_MOVE)) ||
				(phase == PHASE.PLAYER1_ATTACK || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_ATTACK)) )
		{
			actionButtonFading = true;
			font.setColor(Color.BLACK);
			
			font.draw(hudSpriteBatch, "Hold FIRE to shoot", (Gdx.graphics.getWidth()/2f)-(font.getBounds("Hold FIRE to shoot").width/2), (hud_boxHeight*2));
			font.draw(hudSpriteBatch, "Tap SKIP end your turn", (Gdx.graphics.getWidth()/2f)-(font.getBounds("Tap SKIP end your turn").width/2), (hud_boxHeight*2)-(hud_boxHeight*0.75f));
			
			renderRangeBar();
			
			if ((phase == PHASE.PLAYER1_MOVE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_MOVE)))
			{
				font.setColor(Color.BLACK);
				font.draw(hudSpriteBatch, "Hold to move unit", (Gdx.graphics.getWidth()/2f)-(font.getBounds("Hold to move unit").width/2), (hud_boxHeight*2)+(hud_boxHeight*0.75f));
			}
//			else if (phase == PHASE.PLAYER1_ATTACK || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_ATTACK))
//			{
//				font.setColor(Color.RED);
//				font.draw(hudSpriteBatch, "ATTACK OR GO HOME!", (Gdx.graphics.getWidth()/2f)-(font.getBounds("ATTACK OR GO HOME!").width/2), (hud_boxHeight*2)+(hud_boxHeight*0.75f));
//			}
		}
		else
		{
			actionButtonFading = false;
		}
		
		if(actionButtonFading == false && actionButtonFadeIn < 1.0f)
		{
			actionButtonFadeIn = actionButtonFadeIn+BUTTON_FADE_SPEED;
			if(actionButtonFadeIn > 1.0f)actionButtonFadeIn = 1.0f;
		}
		
		if(actionButtonFading == true && actionButtonFadeIn > 0.0f)
		{
			actionButtonFadeIn = actionButtonFadeIn-BUTTON_FADE_SPEED;
			if(actionButtonFadeIn < 0.0f)actionButtonFadeIn = 0.0f;
		}
		
		if(actionButtonFadeIn <= 1.0f)
		{
			float actionOX = (Gdx.graphics.getWidth()-(sprActionButton.getWidth()));
			sprActionButton.setX(actionOX+((sprActionButton.getWidth())*actionButtonFadeIn));

			float cancelOX = (Gdx.graphics.getWidth()-(sprCancelButton.getWidth()));
			sprCancelButton.setX(cancelOX+((sprCancelButton.getWidth())*actionButtonFadeIn));

			sprActionButton.draw(hudSpriteBatch);
			sprCancelButton.draw(hudSpriteBatch);
		}
		
		
		if (movieBorderAlpha == 0.0f)
		{
			/*
			if (phase == PHASE.PLAYER1_CHOOSE)  font.setColor(Color.WHITE);
			else								font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			font.draw(hudSpriteBatch, "CHOOSE", (hud_phaseBoxWidth/2)-(font.getBounds("CHOOSE").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("CHOOSE").height/2.0f));
			
			if (phase == PHASE.PLAYER1_MOVE)  font.setColor(Color.WHITE);
			else							  font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			font.draw(hudSpriteBatch, "MOVE", (hud_phaseBoxWidth/2)+hud_phaseBoxWidth-(font.getBounds("MOVE").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("MOVE").height/2.0f));
			
			if (phase == PHASE.PLAYER1_ATTACK)  font.setColor(Color.WHITE);
			else							    font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			font.draw(hudSpriteBatch, "ATTACK", (hud_phaseBoxWidth/2)+(hud_phaseBoxWidth*2)-(font.getBounds("ATTACK").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("ATTACK").height/2.0f));
			
			if (gameMode != GAMEMODE.AI_ENEMY)
			{
				//Player 2 Phases
				if (phase == PHASE.PLAYER2_CHOOSE)  font.setColor(Color.WHITE);
				else								font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
				font.draw(hudSpriteBatch, "CHOOSE", Gdx.graphics.getWidth()-(hud_phaseBoxWidth*2)-(hud_phaseBoxWidth/2)-(font.getBounds("CHOOSE").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("CHOOSE").height/2.0f));
				
				if (phase == PHASE.PLAYER2_MOVE)    font.setColor(Color.WHITE);
				else								font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
				font.draw(hudSpriteBatch, "MOVE", Gdx.graphics.getWidth()-hud_phaseBoxWidth-(hud_phaseBoxWidth/2)-(font.getBounds("MOVE").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("MOVE").height/2.0f));
				
				if (phase == PHASE.PLAYER2_ATTACK)    font.setColor(Color.WHITE);
				else								  font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
				font.draw(hudSpriteBatch, "ATTACK", Gdx.graphics.getWidth()-(hud_phaseBoxWidth/2)-(font.getBounds("ATTACK").width/2), (Gdx.graphics.getHeight()-(hud_boxHeight/2.0f))+(font.getBounds("ATTACK").height/2.0f));
			}
			*/
			
			
			
			if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
				font.setColor(0.4f, 0.4f, 1.0f, 1.0f); // blue
			else
				font.setColor(0.4f, 0.4f, 1.0f, 0.5f);
//			if (gameMode != GAMEMODE.AI_ENEMY) font.draw(hudSpriteBatch, "PLAYER 1", 5, hud_boxHeight*2-5); //font.getBounds("PLAYER 1").height
//			else	font.draw(hudSpriteBatch, "PLAYER 1", 5, hud_boxHeight*1-5); //font.getBounds("PLAYER 1").height
			if (gameMode != GAMEMODE.AI_ENEMY)
				font.draw(hudSpriteBatch, "PLAYER 1", 5, hud_boxHeight*2-5);
			else
				font.draw(hudSpriteBatch, "PLAYER", 5, hud_boxHeight*2-5);
			
			if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
				font.setColor(Color.WHITE);
			else
				font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
//			if (gameMode != GAMEMODE.AI_ENEMY) font.draw(hudSpriteBatch, "Units: "+player1UnitAmount, 5+20+font.getBounds("PLAYER 2").width, hud_boxHeight*2-5);
//			else	font.draw(hudSpriteBatch, "Units: "+player1UnitAmount, 5+20+font.getBounds("PLAYER 2").width, hud_boxHeight*1-5);
			font.draw(hudSpriteBatch, "Units: "+player1UnitAmount, 5+20+font.getBounds("PLAYER 2").width, hud_boxHeight*2-5);
			
			
			if (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK)
				font.setColor(1.0f, 0.45f, 0.45f, 1.0f);
			else
				font.setColor(1.0f, 0.45f, 0.45f, 0.5f);
			
			if (gameMode != GAMEMODE.AI_ENEMY)
				font.draw(hudSpriteBatch, "PLAYER 2", 5, 0+hud_boxHeight-(hud_boxHeight*0.1f)); //font.getBounds("PLAYER 1").height
			else
				font.draw(hudSpriteBatch, "ENEMY", 5, 0+hud_boxHeight-(hud_boxHeight*0.1f)); //font.getBounds("PLAYER 1").height
			
			
			if (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK)
				font.setColor(Color.WHITE);
			else
				font.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			font.draw(hudSpriteBatch, "Units: "+player2UnitAmount, 5+20+font.getBounds("PLAYER 2").width, 0+hud_boxHeight-(hud_boxHeight*0.1f));
		}
		
		if (movieBorderAlpha > 0.0f)
		{
			Gdx.gl.glEnable(GL10.GL_BLEND);
            Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			tempColor.set(0, 0, 0, movieBorderAlpha);
			Primitives.fillRect(hudSpriteBatch, 0, 0, Gdx.graphics.getWidth(), hud_boxHeight, tempColor);
			Primitives.fillRect(hudSpriteBatch, 0, Gdx.graphics.getHeight()-hud_boxHeight, Gdx.graphics.getWidth(), hud_boxHeight, tempColor);
			
			if (gameMode == GAMEMODE.AI_ENEMY && (phase == PHASE.PLAYER2_CHOOSE || phase == PHASE.PLAYER2_MOVE || phase == PHASE.PLAYER2_ATTACK))
			{
				tempColor2 = hudSpriteBatch.getColor();
				spriteBatch.setColor(tempColor);
				hudSpriteBatch.draw(menuAtlas.findRegion("enemy_phase"),
						(Gdx.graphics.getWidth()/2f)-((menuAtlas.findRegion("enemy_phase").getRegionWidth()*scaleAmount)/2f),
						0,
						menuAtlas.findRegion("enemy_phase").getRegionWidth()*scaleAmount, 
						menuAtlas.findRegion("enemy_phase").getRegionHeight()*scaleAmount);
				spriteBatch.setColor(tempColor2);
			}
		}
		
		if (phase == PHASE.PLAYER1_CHOOSE || phase == PHASE.PLAYER1_MOVE || phase == PHASE.PLAYER1_ATTACK)
		{
			hudSpriteBatch.draw(menuAtlas.findRegion("player_phase"),
					(Gdx.graphics.getWidth()/2f)-((menuAtlas.findRegion("player_phase").getRegionWidth()*scaleAmount)/2f),
					Gdx.graphics.getHeight()-(menuAtlas.findRegion("player_phase").getRegionHeight()*scaleAmount),
					menuAtlas.findRegion("player_phase").getRegionWidth()*scaleAmount, 
					menuAtlas.findRegion("player_phase").getRegionHeight()*scaleAmount);
		}

		if (SHOWFPS)
		{
			font.setColor(Color.YELLOW);
			font.draw(hudSpriteBatch, "FPS: "+Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth()-font.getBounds("FPS: 00").width, Gdx.graphics.getHeight()-hud_boxHeight-2);
		}
		
		if (debug_mapdesign)
		{
			font.setColor(Color.BLACK);
			font.draw(hudSpriteBatch, "POS: "+ getRelativeMouseX() +" , "+getRelativeMouseY(),
				Gdx.graphics.getWidth()-300, Gdx.graphics.getHeight()-hud_boxHeight-2-30);
			
			font.draw(hudSpriteBatch, "MAP DESIGN MODE",	10, Gdx.graphics.getHeight()-20);
		}
		
		
		
		font.setColor(Color.WHITE);
		
		if (gameOver || showPauseMenu)
		{
			Gdx.gl.glEnable(GL10.GL_BLEND);
            Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            
            tempColor.set(0, 0, 0, 0.5f);
            Primitives.fillRect(hudSpriteBatch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), tempColor);
		}
		
		if (showPauseMenu)
		{
			renderStats();
			
			font.draw(hudSpriteBatch, "Return to Main Menu ?", (Gdx.graphics.getWidth()/2)-(font.getBounds("Return to Main Menu ?").width/2), (Gdx.graphics.getHeight()/2)-(font.getBounds("Return to Main Menu ?").height/2));
			font.setColor(Color.WHITE);
			tempRect.set((Gdx.graphics.getWidth()*0.333f)-(font.getBounds("YES").width/2)-30, (Gdx.graphics.getHeight()/2)-(font.getBounds("YES").height/2)-80-font.getBounds("YES").height-15, font.getBounds("YES").width+60, font.getBounds("YES").height+30);
			if (tempRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) font.setColor(Color.BLACK);
			font.draw(hudSpriteBatch, "YES", (Gdx.graphics.getWidth()*0.333f)-(font.getBounds("Yes").width/2), (Gdx.graphics.getHeight()/2)-(font.getBounds("Yes").height/2)-80);
			font.setColor(Color.WHITE);
			tempRect.set((Gdx.graphics.getWidth()*0.666f)-(font.getBounds("NO").width/2)-30, (Gdx.graphics.getHeight()/2)-(font.getBounds("NO").height/2)-80-font.getBounds("NO").height-15, font.getBounds("NO").width+60, font.getBounds("NO").height+30);
			if (tempRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) font.setColor(Color.BLACK);
			font.draw(hudSpriteBatch, "NO", (Gdx.graphics.getWidth()*0.666f)-(font.getBounds("NO").width/2), (Gdx.graphics.getHeight()/2)-(font.getBounds("NO").height/2)-80);

		}
		else
		{
			if (gameOver)
			{
				renderStats();
				font.draw(hudSpriteBatch, winner, (Gdx.graphics.getWidth()/2)-(font.getBounds(winner).width/2), (Gdx.graphics.getHeight()/2)-(font.getBounds(winner).height/2));
				font.draw(hudSpriteBatch, "Tap to restart", (Gdx.graphics.getWidth()/2)-(font.getBounds("Tap to restart").width/2), (Gdx.graphics.getHeight()/2)-(font.getBounds("Tap to restart").height/2)-45);
			}
		}
		
		hudSpriteBatch.end();
	}
	
	public void runMapChoice()
	{
		hudSpriteBatch.begin();
		Primitives.fillRect(hudSpriteBatch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Color.BLACK);
		
		startbg.draw(hudSpriteBatch);
		
		if (Gdx.input.isTouched())
		{
			if (MapChoiceMenu.isMouseOnBack())
			{
				gameState = GAMESTATE.STARTMENU;
			}
			else if (MapChoiceMenu.isMouseOnField() != -1)
			{
				String mapNum = "1";
				mapNum = ""+MapChoiceMenu.isMouseOnField();
				
				clearBox2DBodies();

				SkirmishScript.loadMapScript(Gdx.files.internal("content/maps/field_"+mapNum+".ssm"), allAtlas, mWorld, mapBox2DLoaders);
				startNewGame(SkirmishScript.getGameMap());

				gameMode = gotoMode;
				gameState = GAMESTATE.INGAME;
			}
			else if (MapChoiceMenu.isMouseOnSnow() != -1)
			{
				String mapNum = "1";
				mapNum = ""+MapChoiceMenu.isMouseOnSnow();
				
				clearBox2DBodies();
				
				SkirmishScript.loadMapScript(Gdx.files.internal("content/maps/snow_"+mapNum+".ssm"), allAtlas, mWorld, mapBox2DLoaders);
				startNewGame(SkirmishScript.getGameMap());
				
				gameMode = gotoMode;
				gameState = GAMESTATE.INGAME;
			}
			else if (MapChoiceMenu.isMouseOnDesert() != -1)
			{
				String mapNum = "1";
				mapNum = ""+MapChoiceMenu.isMouseOnDesert();
				
				clearBox2DBodies();

				SkirmishScript.loadMapScript(Gdx.files.internal("content/maps/desert_"+mapNum+".ssm"), allAtlas, mWorld, mapBox2DLoaders);
				startNewGame(SkirmishScript.getGameMap());
				
				gameMode = gotoMode;
				gameState = GAMESTATE.INGAME;
			}
		}
		
		hudSpriteBatch.draw(menuTitle, Gdx.graphics.getHeight()*0.07f, Gdx.graphics.getHeight()-(menuTitle.getRegionHeight()*titleScreenScale)-(Gdx.graphics.getHeight()*0.07f), menuTitle.getRegionWidth()*titleScreenScale, menuTitle.getRegionHeight()*titleScreenScale);
		
		MapChoiceMenu.render(hudSpriteBatch);
		
		
		hudSpriteBatch.end();
	}
	
	public void runStartMenu()
	{
		if (PLAYAUDIO)
		{
			if (titleMusic == null)
			{
				titleMusic = Gdx.audio.newMusic(Gdx.files.internal("content/audio/music/title.ogg"));
				titleMusic.setLooping(true);
				titleMusic.setVolume(0.7f);
			}
			
			if (!titleMusic.isPlaying() && assetsFullyLoaded) titleMusic.play();
		}	
		hudSpriteBatch.begin();
		
		hudSpriteBatch.draw(blackPreLoadTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		if (assetsFullyLoaded)
		{
			if (DEVKIT && JUMP_INTO_MAP_DIRECTLY != null)
			{
				clearBox2DBodies();

				SkirmishScript.loadMapScript(Gdx.files.internal("content/maps/"+JUMP_INTO_MAP_DIRECTLY+".ssm"), allAtlas, mWorld, mapBox2DLoaders);
				startNewGame(SkirmishScript.getGameMap());
				JUMP_INTO_MAP_DIRECTLY = null;

				gameMode = gotoMode;
				gameState = GAMESTATE.INGAME;
			}
			
			float fps_factor = 1f;
			if (!fps60) fps_factor = 2f;
			if (clearMenuBgAlpha > 0.0f)
			{
				clearMenuBgAlpha-=0.005f*fps_factor;
				titleAlpha+=0.005f*fps_factor;
			}
			if (clearMenuBgAlpha < 0.0f)
			{
				clearMenuBgAlpha=0;
				titleAlpha=1;
			}		
			
			startbg.draw(hudSpriteBatch);
			startbgClear.draw(hudSpriteBatch, clearMenuBgAlpha);
			
			Color precolor = hudSpriteBatch.getColor();
	        float oldAlpha = precolor.a;
	        precolor.a *= titleAlpha;
	        hudSpriteBatch.setColor(precolor);
	        hudSpriteBatch.draw(menuTitle, Gdx.graphics.getHeight()*0.07f, Gdx.graphics.getHeight()-(menuTitle.getRegionHeight()*titleScreenScale)-(Gdx.graphics.getHeight()*0.07f), menuTitle.getRegionWidth()*titleScreenScale, menuTitle.getRegionHeight()*titleScreenScale);
	        precolor.a = oldAlpha;
	        hudSpriteBatch.setColor(precolor);
			
	        if(clearMenuBgAlpha <= 0)
	        {
				if (howToScreen)
				{
					font.setColor(Color.BLACK);
					font.draw(hudSpriteBatch, "Drive around shoot tanks and win :D", (Gdx.graphics.getWidth()/2.0f)-(font.getBounds("How to text").width/2.0f), Gdx.graphics.getHeight()*0.7f);
					font.draw(hudSpriteBatch, "Use two fingers to zoom", (Gdx.graphics.getWidth()/2.0f)-(font.getBounds("How to text").width/2.0f), (Gdx.graphics.getHeight()*0.7f)-(font.getLineHeight()*1.1f));
					font.draw(hudSpriteBatch, "More elaborate explanation in future releases", (Gdx.graphics.getWidth()/2.0f)-(font.getBounds("How to text").width/2.0f), (Gdx.graphics.getHeight()*0.7f)-(font.getLineHeight()*2.2f));
					font.draw(hudSpriteBatch, "Tap screen to return to main menu", (Gdx.graphics.getWidth()/2.0f)-(font.getBounds("How to text").width/2.0f), Gdx.graphics.getHeight()*0.4f);
				}
	        }
		
			font.setColor(Color.WHITE);
		}
		else
		{
			
			hudSpriteBatch.draw(blackPreLoadTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			
			float sf = (float)Gdx.graphics.getWidth() / (float)alchTempTitle.getRegionWidth();
			
			hudSpriteBatch.draw(alchTempTitle, (Gdx.graphics.getWidth()/2.0f)-(sf*(alchTempTitle.getRegionWidth())/2.0f),
											   (Gdx.graphics.getHeight()/2.0f)-(sf*(alchTempTitle.getRegionHeight())/2.0f),
											   sf*alchTempTitle.getRegionWidth(),
											   sf*alchTempTitle.getRegionHeight());
			font.setColor(Color.WHITE);
			font.draw(hudSpriteBatch, "Loading...", (Gdx.graphics.getWidth()/2.0f)-(font.getBounds("Loading...").width/2.0f), Gdx.graphics.getHeight()*0.9f);
			
			String[] qu = QuoteManager.getQuote().split("<br>");
			float offY = Gdx.graphics.getHeight()*0.20f;
			
			//Y UP here
			for (int i = 0; i<qu.length; i++)
			{
				font.draw(hudSpriteBatch, qu[i], (Gdx.graphics.getWidth()/2.0f)-(font.getBounds(qu[i]).width/2.0f), offY-(font.getLineHeight()*i));
			}
		}
		
		hudSpriteBatch.end();
		
		if (!assetsFullyLoaded)
		{
			if (menuPreLoadFrame == 1)
			{
				menuPreLoadFrame = 0;
				System.out.println("Loading all Assets");
				loadAllAssets();
			}
			else if (menuPreLoadFrame > 1)
			{
				menuPreLoadFrame--;
			}
			else
			{
				//its zero, should be done
			}
		}
	}

	@Override
	public void render()
	{
		if (DEVKIT) Gdx.graphics.getGL10().glClearColor(1, 1, 1, 1);
		else 		Gdx.graphics.getGL10().glClearColor(0, 0, 0, 0);
		
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		alreadyRendered = false;
		
		if (gameState == GAMESTATE.INGAME)
		{
			Gdx.input.setInputProcessor(gestureDetector);
			runIngame();
		}
		else if (gameState == GAMESTATE.STARTMENU)
		{
			Gdx.input.setInputProcessor(uiStage);
			runStartMenu();
			
			
			
			
			if (assetsFullyLoaded && titleAlpha == 1f)
			{
				uiStage.act();
				uiStage.draw();
			}
		}
		else if (gameState == GAMESTATE.MAPCHOICE)
		{
			Gdx.input.setInputProcessor(gestureDetector);
			runMapChoice();
		}
		
		alreadyRendered = true;
		
		// compile again if you need double speed for 30 fps
		// doesnt get much worse than this in terms of performance
		// even the gpu is used twice for everything
		if (!fps60)
		{
			if (gameState == GAMESTATE.INGAME)
			{
				runIngame();
			}
		}
		
		if (!fps60) skip.sync();
	}
	
	private boolean isMobilePlatform()
	{
		return (Gdx.app.getType() == ApplicationType.iOS || Gdx.app.getType() == ApplicationType.Android);
	}
	
	@Override
	public void dispose()
	{
		//do a KYRO savefile, reload on resume
		
		if (PLAYAUDIO)
		{
			music.dispose();
			for (Sound snd : sounds.values())
			{
				snd.dispose();
			}
		}
		if (gameMap != null && gameMap.units != null) gameMap.units.clear();
		
		uiStage.dispose();
		
		allAtlas.dispose();
		menuAtlas.dispose();
		
		Texture.clearAllTextures(Gdx.app);
		
		font.dispose();
		startbgtex.dispose();
		startbgtexClear.dispose();
		
		spriteBatch.dispose();
		hudSpriteBatch.dispose();
	}
	
	@Override
	public void resume()
	{
		System.out.println("GDX RESUME");
	}

	@Override
	public void pause()
	{
		System.out.println("GDX PAUSE");
	}
	
	private void debugReloadMap()
	{
		if (SkirmishScript.hasChanged())
		{
			clearBox2DBodies();
			gameMap.units.clear();
			gameMap.obstacles.clear();
			gameMap.floors.clear();
			gameMap = null;
			SkirmishScript.debugQuickReloadIfChanged();
			gameMap = SkirmishScript.getGameMap();
		}
		
		//TODO
	}
	
	class CameraController implements GestureListener
	{
        float velX, velY;
        float initialScale = 1;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button)
        {
            initialScale = camera.zoom;
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button)
        {
            return false;
        }

        @Override
        public boolean longPress(float x, float y)
        {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY)
        {
        	if (gameState == GAMESTATE.INGAME && !Gdx.input.isKeyPressed(Keys.W) && debugMapDesignElement == null)
        	{
        		camera.position.add(-deltaX * camera.zoom, -deltaY * camera.zoom, 0);
                keepCameraInBounds();
        	}
            return false;
        }

        @Override
        public boolean zoom (float originalDistance, float currentDistance)
        {
        	if (gameState == GAMESTATE.INGAME)
        	{
        		float ratio = originalDistance / currentDistance;
        		targetZoom = initialScale * ratio;
                if (targetZoom > (gameMap.getWidth()/camera.viewportWidth)*0.65f) targetZoom = (gameMap.getWidth()/camera.viewportWidth)*0.65f;
                if (targetZoom < MIN_ZOOM) targetZoom = MIN_ZOOM;
                keepCameraInBounds();
        	}
            return false;
        }

		@Override
		public boolean pinch(Vector2 arg0, Vector2 arg1, Vector2 arg2, Vector2 arg3)
		{
			return false;
		}

		@Override
		public boolean fling(float arg0, float arg1, int arg2)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private class MyStage extends Stage
	{
		@Override
		public boolean keyDown(int keycode)
		{
			if (keycode == Keys.ESCAPE || (Gdx.app.getType() == ApplicationType.Android && keycode == Keys.BACK))
			{
				goBack();
			}
			
			return super.keyDown(keycode);
		}
	}
	
	private class MyGestureDetector extends GestureDetector
	{
		public MyGestureDetector(GestureListener listener)
		{
			super(listener);
		}

		public MyGestureDetector(int halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay, GestureListener listener)
		{
			super(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, listener);
		}
		
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer)
		{
			if (DEVKIT && debug_mapdesign && debugMapDesignElement != null)
			{
				int ddx = (int)(Mouse.getDX()*camera.zoom);//+(int)(camera.position.x)-(int)((camera.viewportWidth*camera.zoom)/2.0f);
				int ddy = (int)(Mouse.getDY()*camera.zoom);//+(int)(camera.position.y)-(int)((camera.viewportHeight*camera.zoom)/2.0f);

				debugMapDesignElement.setPosition(debugMapDesignElement.getX()+ddx, debugMapDesignElement.getY()-ddy);
				if (debugMapDesignElement.uBody != null)
				{
					debugMapDesignElement.uBody.setTransform(debugMapDesignElement.centerX/Game.PIXELS_PER_METER, 
														 	debugMapDesignElement.centerY/Game.PIXELS_PER_METER, 
														 	(float) ((debugMapDesignElement.getRotation()/180.0f)*Math.PI));
				}
				debugMapDesignElement.compute();
			}
			
			return super.touchDragged(screenX, screenY, pointer);
		}
		
		@Override
		public boolean mouseMoved(int screenX, int screenY)
		{
			if (gameState == GAMESTATE.INGAME && DEVKIT && debug_mapdesign)
			{
				debugMapDesignElement = null;
				
				int x = getRelativeMouseX();
				int y = getRelativeMouseY();
				
				for (int i = gameMap.obstacles.size() - 1; i >= 0; i--)
		        {
					Element el = gameMap.obstacles.get(i);
					if (el.isWithinFrustum(camera))
					{
						if (el.getCollRect().contains(x, y))
						{
							debugMapDesignElement = el;
							break;
						}
					}
				}
				
				
				if (debugMapDesignElement == null)
				{	
					for (int i = gameMap.units.size() - 1; i >= 0; i--)
			        {
						Element el = gameMap.units.get(i);
						if (el.isWithinFrustum(camera))
						{
							if (el.getCollRect().contains(x, y))
							{
								debugMapDesignElement = el;
								break;
							}
						}
					}
				}
				
				if (debugMapDesignElement == null)
				{	
					for (int i = gameMap.floors.size() - 1; i >= 0; i--)
			        {
						Element el = gameMap.floors.get(i);
						if (el.isWithinFrustum(camera))
						{
							if (el.getCollRect().contains(x, y))
							{
								debugMapDesignElement = el;
								break;
							}
						}
					}
				}
				
				if (debugMapDesignElement == null)
				{	
					for (int i = gameMap.powerups.size() - 1; i >= 0; i--)
			        {
						Element el = gameMap.powerups.get(i);
						if (el.isWithinFrustum(camera))
						{
							if (el.getCollRect().contains(x, y))
							{
								debugMapDesignElement = el;
								break;
							}
						}
					}
				}
			}
			
			return super.mouseMoved(screenX, screenY);
		}
		
		@Override
		public boolean keyDown(int keycode)
		{
			if (keycode == Keys.ESCAPE || (Gdx.app.getType() == ApplicationType.Android && keycode == Keys.BACK))
			{
				goBack();
			}
			
			if (keycode == Keys.M || (Gdx.app.getType() == ApplicationType.Android && keycode == Keys.MENU))
			{
				goMenuButton();
			}
			
			if (keycode == Keys.C)
			{
				targetZoom = 1f;
			}
			
//			if (DEVKIT && debug_mapdesign && keycode == Keys.F5)
//			{
//				debugReloadMap();
//			}
			
			if (DEVKIT && debug_mapdesign)
			{
				if (keycode == Keys.S && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
				{
					SkirmishScript.writeMapScript(gameMap);
				}
				
				if ((keycode == Keys.D && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)))
				{
					if (debugMapDesignElement != null)
					{
						if (debugMapDesignElement instanceof Floor)
						{
							Floor el = (Floor)debugMapDesignElement;
							gameMap.addFloor(allAtlas.findRegion(el.getTexName()), el.getTexName(), (int)el.getX()+100, (int)el.getY()+100, el.getRotation(), false, false, el.getEffect());
						}
						else if (debugMapDesignElement instanceof Powerup)
						{
							Powerup el = (Powerup)debugMapDesignElement;
							gameMap.addPowerup(allAtlas.findRegion(el.getTexName()), el.getTexName(), (int)el.getX()+100, (int)el.getY()+100, el.getRotation(), el.getType());
						}
						else if (debugMapDesignElement instanceof Unit)
						{
							Unit el = (Unit)debugMapDesignElement;
							Unit u = new Unit(allAtlas.findRegion(el.getTexName()), el.getTexName(), (int)el.getX()+100, (int)el.getY()+100, el.owner, el.type, mWorld);
							u.rotate(el.getRotation());
							gameMap.units.add(u);
						}
						else //leaves us with element which should be obstacles
						{
							Element el = (Element)debugMapDesignElement;
							gameMap.addObstacle(allAtlas.findRegion(el.getTexName()), el.getTexName(), (int)el.getX()+100, (int)el.getY()+100, el.getRotation(), el.hFlipped, el.vFlipped, mWorld);
						}
						
						debugMapDesignElement.compute();
					}
				}
				
				if (keycode == Keys.BACKSPACE || keycode == Keys.DEL || keycode == Keys.FORWARD_DEL)
				{
					if (debugMapDesignElement != null)
					{
						if (debugMapDesignElement instanceof Floor)
						{
							gameMap.floors.remove(debugMapDesignElement);
						}
						else if (debugMapDesignElement instanceof Powerup)
						{
							gameMap.powerups.remove(debugMapDesignElement);
						}
						else if (debugMapDesignElement instanceof Unit)
						{
							gameMap.units.remove(debugMapDesignElement);
						}
						else //leaves us with element which should be obstacles
						{
							gameMap.obstacles.remove(debugMapDesignElement);
						}
						debugMapDesignElement = null;
					}
				}
			}
			
			if (DEVKIT && keycode == Keys.F3)
			{
				debug_mapdesign = !debug_mapdesign;
			}
			
			if (DEVKIT && keycode == Keys.F1)
			{
				renderBox2D = !renderBox2D;
			}
			
			if (DEVKIT && debug_mapdesign && debugMapDesignElement != null)
			{
				if (keycode == Keys.LEFT || keycode == Keys.RIGHT)
				{
					debugMapDesignElement.flip(true, false);
				}
				else if (keycode == Keys.UP || keycode == Keys.DOWN)
				{
					debugMapDesignElement.flip(false, true);
				}
			}
			
			return super.keyDown(keycode);
		}
		
		@Override
        public boolean touchUp(int x, int y, int pointer, int button)
        {
			if (gameState == GAMESTATE.STARTMENU)
			{
				if (!howToScreen)
				{

				}
			}
			else if (gameState == GAMESTATE.INGAME)
        	{
				if (!gameOver)
				{
					if (sprActionButton.getBoundingRectangle().contains(x, Gdx.graphics.getHeight()-y))
        			{
        				sprActionButton.setRegion(menuAtlas.findRegion("action-button"));
        			}
				}
				
				selectWait = 0;
				
				
        		if (!showPauseMenu)
        		{
        			if (phase == PHASE.PLAYER1_ATTACK || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_ATTACK))
        			{
        				if (timeToNextPhase == -1 && ctrlUnit != null)
        				{
        					if (!ctrlUnit.doneShooting)
        					{
        						if (ctrlUnit.targeting && !ctrlUnit.shooting)
        						{
        							shoot();
        						}
        					}
        				}
        			}
        		}
        	}

        	return super.touchUp(x, y, pointer, button);
        }
        
        @Override
        public boolean touchDown (int x, int y, int pointer, int button)
        {
//        	if (DEVKIT)
//        	{
//        		var aabb = new b2AABB();
//        	    aabb.lowerBound.Set(mouseX - 0.001, mouseY - 0.001);
//        	    aabb.upperBound.Set(mouseX + 0.001, mouseY + 0.001);
//        	    selectedBody = null;
//        	    world.QueryAABB(getBodyCB, aabb);
//        	    if(selectedBody!= null && selectedBody.GetUserData() != null)
//        	               console.log(selectedBody.GetUserData().name);
//        	    return selectedBody;
//        	}
        	
        	if (gameState == GAMESTATE.INGAME)
    		{
        		if (!gameOver)
        		{
        			if (sprActionButton.getBoundingRectangle().contains(x, Gdx.graphics.getHeight()-y))
        			{
        				sprActionButton.setRegion(menuAtlas.findRegion("action-button-down"));
        			}
        			
        			if (showPauseMenu)
        			{
        				tempRect.set((Gdx.graphics.getWidth()*0.333f)-(font.getBounds("YES").width/2)-30, (Gdx.graphics.getHeight()/2)-(font.getBounds("YES").height/2)-80-font.getBounds("YES").height-15, font.getBounds("YES").width+60, font.getBounds("YES").height+30);
        				if (tempRect.contains(x, Gdx.graphics.getHeight()-y)) 
        				{
        					//stop music or whatever
        					gameState = GAMESTATE.STARTMENU;
        					gameMap = null;
        					clearGame();
        					showPauseMenu = false;
        				}
        				tempRect.set((Gdx.graphics.getWidth()*0.666f)-(font.getBounds("NO").width/2)-30, (Gdx.graphics.getHeight()/2)-(font.getBounds("NO").height/2)-80-font.getBounds("NO").height-15, font.getBounds("NO").width+60, font.getBounds("NO").height+30);
        				if (tempRect.contains(x, Gdx.graphics.getHeight()-y))
        				{
        					showPauseMenu = false;
        					timeMeasurePoint = System.currentTimeMillis();
        				}
        			}
        			else
        			{
        				if (phase == PHASE.PLAYER1_CHOOSE || (gameMode == GAMEMODE.LOCAL_2PLAYER && phase == PHASE.PLAYER2_CHOOSE))
        				{
//        					int x = (expression) ? 1 : 2;
        					Unit.OWNER leOwner = (phase == PHASE.PLAYER1_CHOOSE) ? Unit.OWNER.PLAYER1 : Unit.OWNER.PLAYER2;
        					for (int ui = 0; ui<gameMap.units.size(); ui++)
        			        {
        						Unit unit = gameMap.units.get(ui);
        						if (unit.owner == leOwner && unit.getCollRect().contains(getRelativeMouseX(), getRelativeMouseY()))
        						{
        							if (ctrlUnit != null && ctrlUnit == unit)
        							{
        								/*
        								drawChooseConfirm = false;
        								if (selectWait == 0) selectWait = System.currentTimeMillis();
        								if (System.currentTimeMillis() - selectWait >= timeToWaitForSelect)
        								{
        									nextPhase();
        									System.out.println("next phasing");
        								}
        								else
        								{
        									float p = getPercentageValue(
        											timeToWaitForSelect,
        											System.currentTimeMillis() - selectWait);
        									System.out.println("Still waiting: "+p);
        								}
        								*/
        							}
        							else
        							{
        								chooseUnit(unit);
        							}
        							break;
        						}
        					}
        				}
        			}
        		}
        		else
    			{
    				gameState = GAMESTATE.STARTMENU;
    			}
    		}
        	
        	return super.touchDown(x, y, pointer, button);
        }
		
		@Override public boolean scrolled(int amount) // MOUSE WHEEL
		{
			if (gameState == GAMESTATE.INGAME)
			{
				if (DEVKIT && debug_mapdesign && debugMapDesignElement != null)
				{
					if (amount > 0)
					{
						if (debugMapDesignElement instanceof Unit)
							debugMapDesignElement.setRotation(debugMapDesignElement.getRotation()+10);
						else
							debugMapDesignElement.rotate(10);
					}
					else if (amount < 0)
					{
						if (debugMapDesignElement instanceof Unit)
							debugMapDesignElement.setRotation(debugMapDesignElement.getRotation()-10);
						else
							debugMapDesignElement.rotate(-10);
					}
					
					debugMapDesignElement.compute();
				}
				
				if (DEVKIT && Gdx.input.isKeyPressed(Keys.SPACE))
				{
					if ((!debug_mapdesign || debugMapDesignElement == null))
						targetZoom+=(float)(amount/5f);
				}
				else if (debugMapDesignElement == null)
				{
					if (amount > 0) // zoom out
					{
						if (targetZoom < (gameMap.getWidth()/camera.viewportWidth)*0.85f)
							targetZoom+=0.2f;
					}
					
					if (amount < 0)
					{
						if (targetZoom >= MIN_ZOOM+0.2f)
							targetZoom-=0.2f;
					}
				}
			}
			
			if (amount > 0) return true;
			else return false;
		}
		
	}
	
	private void loadAllAssets()
	{
		long b = System.nanoTime();
		
		if (PLAYAUDIO)
		{
			System.out.println("Starting to load all audio");
			b = System.nanoTime();
			
			sounds.put("choose", 				Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/choose.ogg")));
			sounds.put("copter_engine", 		Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/copter_engine.ogg")));
			sounds.put("explosion", 			Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/explosion.ogg")));
			sounds.put("explosion_big", 		Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/explosion_big.ogg")));
			sounds.put("gatling-laser", 		Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/gatling-laser.ogg")));
			sounds.put("heavy_canon", 			Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/heavy_canon.ogg")));
			sounds.put("laser1", 				Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/laser1.ogg")));
			sounds.put("light_tank_engine", 	Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/light_tank_engine.ogg")));
			sounds.put("light_tank_shot", 		Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/light_tank_shot.ogg")));
			sounds.put("missile1", 				Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/missile1.ogg")));
			sounds.put("ricochet", 				Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/ricochet.ogg")));
			sounds.put("ping", 					Gdx.audio.newSound(Gdx.files.internal("content/audio/sound/ping.ogg")));
			
			//sounds.get("sound").setVolume(arg0, arg1)

			music = Gdx.audio.newMusic(Gdx.files.internal("content/audio/music/loop1.ogg"));
			
			System.out.println("Done. Took "+((System.nanoTime()-b)/1000L/1000L)+"ms");
		}
		
		System.out.println("Starting to load atlas'");
		b = System.nanoTime();
		allAtlas  = new TextureAtlas(Gdx.files.internal("content/atlas/all.atlas"), true);
		menuAtlas = new TextureAtlas(Gdx.files.internal("content/atlas/menu.atlas"), false);
		System.out.println("Done. Took "+((System.nanoTime()-b)/1000L/1000L)+"ms");
		
		mapBox2DLoaders = new HashMap<String, OurBodyEditorLoader>();
		
		smallFont		= new BitmapFont(Gdx.files.internal("content/small_debug.fnt"), true);
		smallFont.setScale(scaleAmount);
		smallFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		smallFont.setColor(Color.WHITE);
		
		MapChoiceMenu.load(menuAtlas);
		
		mWorld = new World(new Vector2(0,0), true); // create box2d world  

		menuTitle = menuAtlas.findRegion("startmenu-skirmish"); //menu / startmenu-skirmish

		//atlas  menu/startbg
		startbg = new Sprite(menuAtlas.findRegion("startbg"));
		startbg.setOrigin(startbg.getWidth()/2.0f, startbg.getHeight()/2.0f);
		float sw = Gdx.graphics.getWidth()/startbg.getWidth();

		titleScreenScale = sw;
		startbg.setScale(titleScreenScale);
		startbg.setX((Gdx.graphics.getWidth()/2.0f)-(startbg.getWidth()/2.0f));
		startbg.setY((Gdx.graphics.getHeight()/2.0f)-(startbg.getHeight()/2.0f));
		
		System.out.println("scaleAmount: "+scaleAmount);
		
		startbgClear = new Sprite(menuAtlas.findRegion("startbg-clear"));
		startbgClear.setOrigin(startbgClear.getWidth()/2.0f, startbgClear.getHeight()/2.0f);
		startbgClear.setScale(titleScreenScale);
		startbgClear.setX((Gdx.graphics.getWidth()/2.0f)-(startbgClear.getWidth()/2.0f));
		startbgClear.setY((Gdx.graphics.getHeight()/2.0f)-(startbgClear.getHeight()/2.0f));
		
		hud_boxHeight		= (int)font.getLineHeight();
		hud_phaseBoxWidth	= (int)(Gdx.graphics.getWidth() / 8.0f);
		
		sprActionButton = new Sprite(menuAtlas.findRegion("action-button"));
		sprCancelButton = new Sprite(menuAtlas.findRegion("cancel-button"));
		sprActionButton.setSize(sprActionButton.getWidth()*scaleAmount, sprActionButton.getHeight()*scaleAmount);
		sprCancelButton.setSize(sprCancelButton.getWidth()*scaleAmount, sprCancelButton.getHeight()*scaleAmount);
		sprActionButton.setPosition(0, 0);
		sprCancelButton.setPosition(0, 0);
		sprActionButton.setY(Gdx.graphics.getHeight()-(hud_boxHeight*4f)-(sprActionButton.getHeight()));
		sprCancelButton.setY(sprActionButton.getY()-((sprCancelButton.getHeight()*2)));
		
		startbgtex = menuAtlas.findRegion("startbg").getTexture();
		startbgtex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		startbgtexClear = menuAtlas.findRegion("startbg-clear").getTexture();
		startbgtexClear.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		System.out.println("Initializing Particles");
		b = System.nanoTime();
		ParticleManager.initialize();
		System.out.println("Done. Took "+((System.nanoTime()-b)/1000L/1000L)+"ms");
		
		
		//UI
  		// Holo Dark Theme, created by Carsten Engelke
  		uiStage = new MyStage();

		
		TextureRegionDrawable singleIdle = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("content/singlePlayer_idle.png"))));
		TextureRegionDrawable singleDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("content/singlePlayer_down.png"))));
		TextureRegionDrawable twoIdle    = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("content/twoPlayer_idle.png"))));
		TextureRegionDrawable twoDown    = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("content/twoPlayer_down.png"))));
		
		ButtonStyle buttonStyle = new ButtonStyle();
		buttonStyle.up 			= singleIdle;
		buttonStyle.down 		= singleDown;
		
		ButtonStyle buttonStyle2 = new ButtonStyle();
		buttonStyle2.up 			= twoIdle;
		buttonStyle2.down 			= twoDown;
		
  		
  		buttonSingle = new Button(buttonStyle);
  		uiStage.addActor(buttonSingle);
  		
  		buttonLocal2 = new Button(buttonStyle2);
  		uiStage.addActor(buttonLocal2);
  		
  		
  		float padd = Gdx.graphics.getHeight()*0.05f;
		
		buttonSingle.setSize(Gdx.graphics.getWidth()*0.4f, Gdx.graphics.getHeight()*0.3f);
		buttonLocal2.setSize(Gdx.graphics.getWidth()*0.4f, Gdx.graphics.getHeight()*0.3f);

		buttonLocal2.setPosition(Gdx.graphics.getWidth()-buttonLocal2.getWidth()-padd, 0+padd);
		
		buttonSingle.setPosition(buttonLocal2.getX(), buttonLocal2.getY()+buttonLocal2.getHeight()+padd);
  		
//  		buttonHowTo = new TextButton("How To", uiSkin);
//  		uiStage.addActor(buttonHowTo);
//  		buttonHowTo.setWidth(buttonSingle.getWidth());
//  		buttonHowTo.setPosition(buttonSingle.getX(), buttonSingle.getY()-(Gdx.graphics.getHeight()*0.14f*2f));
//  		buttonHowTo.setHeight(buttonSingle.getHeight());
  		
//  		buttonQuit = new TextButton("Quit", uiSkin);
//  		uiStage.addActor(buttonQuit);
//  		buttonQuit.setWidth(buttonSingle.getWidth());
//  		buttonQuit.setPosition(buttonSingle.getX(), buttonSingle.getY()-(Gdx.graphics.getHeight()*0.14f*3f));
//  		buttonQuit.setHeight(buttonSingle.getHeight());
  		
  		buttonSingle.addListener(new ClickListener()
  		{
  			public void clicked(InputEvent event, float x, float y) 
  			{
  				buttonSingle.setChecked(false);
  				gotoMode = GAMEMODE.AI_ENEMY;
  				gameState = GAMESTATE.MAPCHOICE;
  			}
  		});
  		
  		buttonLocal2.addListener(new ClickListener()
  		{
  			public void clicked(InputEvent event, float x, float y) 
  			{
  				buttonLocal2.setChecked(false);
  				gotoMode = GAMEMODE.LOCAL_2PLAYER;
  				gameState = GAMESTATE.MAPCHOICE;
  			}
  		});
  		
//  		buttonHowTo.addListener(new ClickListener()
//  		{
//  			public void clicked(InputEvent event, float x, float y) 
//  			{
//  				buttonHowTo.setChecked(false);
//  				howToScreen = true;
//  			}
//  		});
//  		
//  		buttonQuit.addListener(new ClickListener()
//  		{
//  			public void clicked(InputEvent event, float x, float y) 
//  			{
//  				buttonQuit.setChecked(false);
//  				Gdx.app.exit();
//  			}
//  		});
		
		Primitives.init(allAtlas);
		
//		for (Texture t : spriteSheets.values())
//		{
//			t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
//		}
//		
//		spriteSheets.get("one").setFilter(TextureFilter.Linear, TextureFilter.Linear);

		
		assetsFullyLoaded = true;
		System.out.println("Done loading");
	}
	
	
}
