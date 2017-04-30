package main;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class GameMap
{
	private String name;
	private TextureRegion bgTexture;
	private String mapNameRoot;
	private int mapNum = 1;
	private int width;
	private int height;
	private int tex_size;
	public boolean useAdditiveBlending;
	public ArrayList<Element> obstacles = new ArrayList<Element>(); //collrect from unit
	public ArrayList<Floor> floors = new ArrayList<Floor>();
	public ArrayList<Powerup> powerups = new ArrayList<Powerup>();
	public ArrayList<Unit> units = new ArrayList<Unit>();
	private Vector3 calcV3 = new Vector3(0,0,1);
	
	public ArrayList<Element> renderList = new ArrayList<Element>();
	
	//width & height should be power of tex_size
	public GameMap(String _n, TextureRegion _tex, int _w, int _h, World mWorld)
	{
		name = _n;
		
		useAdditiveBlending = true; //default
		
		String[] nels = name.split("_");
		if (nels.length > 1)
		{
			mapNameRoot	= nels[0];
			mapNum 		= Integer.parseInt(nels[1]);
		}
		else
		{
			mapNameRoot = name;
		}
		
		bgTexture = _tex;
		width = _w;
		height = _h;
		tex_size = bgTexture.getRegionWidth();
		units = new ArrayList<Unit>();
		
//		createTopBox2DBorder(mWorld);
//		createBottomBox2DBorder(mWorld);
//		createLeftBox2DBorder(mWorld);
//		createRightBox2DBorder(mWorld);
		
		
	}
	
	public void createTopBox2DBorder(World mWorld)
	{
		int lex = width / Game.PIXELS_PER_METER / 2;
		int ley = 0-190;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER);
		Body uBody = mWorld.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		
		shape.setAsBox(width*2 / Game.PIXELS_PER_METER, 200 / Game.PIXELS_PER_METER);
		
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER,  0);
	}
	
	public void createBottomBox2DBorder(World mWorld)
	{
		int lex = width / Game.PIXELS_PER_METER / 2;
		int ley = height+190;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER);
		Body uBody = mWorld.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		
		shape.setAsBox(width*2 / Game.PIXELS_PER_METER, 200 / Game.PIXELS_PER_METER);
		
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER,  0);
	}
	
	public void createLeftBox2DBorder(World mWorld)
	{
		int lex = -190;
		int ley = 0;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER);
		Body uBody = mWorld.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		
		shape.setAsBox(200 / Game.PIXELS_PER_METER, height*2 / Game.PIXELS_PER_METER);
		
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER,  0);
	}

	public void createRightBox2DBorder(World mWorld)
	{
		int lex = width+180;
		int ley = 0;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER);
		Body uBody = mWorld.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		
		shape.setAsBox(200 / Game.PIXELS_PER_METER, height*2 / Game.PIXELS_PER_METER);
		
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(lex/Game.PIXELS_PER_METER, ley/Game.PIXELS_PER_METER,  0);
	}
	
	public String getMapNameRoot()
	{
		return mapNameRoot;
	}

	public void setMapNameRoot(String mapNameRoot)
	{
		this.mapNameRoot = mapNameRoot;
	}

	public int getMapNum()
	{
		return mapNum;
	}

	public void setMapNum(int mapNum)
	{
		this.mapNum = mapNum;
	}

	public String getName()
	{
		return name;
	}
	
	public void addObstacle(TextureRegion treg, String texName, int _x, int _y, float degrees, boolean _hori, boolean _vert, World mWorld)
	{
		Element el = new Element(treg, texName, _x, _y);
		el.flip(_hori, _vert);
		el.setRotation(degrees);
		el.compute();
		el.createStaticBody(mWorld);
		obstacles.add(el);
	}
	
	public void addObstacle(TextureRegion treg, int _x, int _y, float degrees, boolean _hori, boolean _vert, World mWorld, String name, TextureAtlas atlas, OurBodyEditorLoader loader, boolean extraShadow)
	{
		Element el = new Element(treg, name, _x, _y);
		
		el.flip(_hori, _vert);
		el.setExtraShadow(extraShadow);
		el.setRotation(degrees);
		el.compute();
		el.createStaticBody(mWorld,name, atlas, loader);
		obstacles.add(el);
	}
	
	public void addFloor(TextureRegion treg, String name, int _x, int _y, float degrees, boolean _hori, boolean _vert, Floor.EFFECT eff)
	{
		Floor el = new Floor(treg, name, _x, _y, eff);
		el.flip(_hori, _vert);
		el.setRotation(degrees);
		el.compute();
		floors.add(el);
	}
	
	public void addPowerup(TextureRegion treg, String name, int _x, int _y, float degrees, Powerup.TYPE type)
	{
		Powerup el = new Powerup(treg, name, _x, _y, type);
		el.setRotation(degrees);
		el.compute();
		powerups.add(el);
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	private boolean isTextureWithinFrustum(int x, int y, OrthographicCamera cam)
	{
		calcV3.x = x;
		calcV3.y = y;
		if (cam.frustum.pointInFrustum(calcV3))
		{
			return true;
		}
		
		calcV3.x = x+tex_size;
		calcV3.y = y;
		if (cam.frustum.pointInFrustum(calcV3))
		{
			return true;
		}
		
		calcV3.x = x;
		calcV3.y = y+tex_size;
		if (cam.frustum.pointInFrustum(calcV3))
		{
			return true;
		}
		
		calcV3.x = x+tex_size;
		calcV3.y = y+tex_size;
		if (cam.frustum.pointInFrustum(calcV3))
		{
			return true;
		}		
		
		return false;
	}
	
	public void render(SpriteBatch spriteBatch, OrthographicCamera cam)
	{
		//lets render the ground textue first
		for (int y = 0; y < height; y+=tex_size)
		{
			for (int x = 0; x < width; x+=tex_size)
			{
				if (isTextureWithinFrustum(x,y, cam))
				{
					spriteBatch.draw(bgTexture, x, y);
				}
			}
		}
		
		//lets render the floors
		for (int i = 0; i<floors.size(); i++)
        {
			Element el = floors.get(i);
			if (el.isWithinFrustum(cam))
				el.render(spriteBatch);
		}
		
		
		renderList.clear();

		
		//lets add all obstacles within frustum to the sort list
		for (int i = 0; i<obstacles.size(); i++)
        {
			Element el = obstacles.get(i);
			if (el.isWithinFrustum(cam))
				renderList.add(el);
		}
		
		//lets add all powerups within frustum to the sort list
		for (int i = 0; i<powerups.size(); i++)
        {
			Element el = powerups.get(i);
			if (el.isWithinFrustum(cam))
				renderList.add(el);
		}
		
		//lets add all units within frustum to the sort list
		for (int i = 0; i<units.size(); i++)
        {
			Unit el = units.get(i);
			if (el.isWithinFrustum(cam) && !el.flying) //the flying ones we render outside in the Game
				renderList.add(el);
		}
		
		//SORT by y+height of every unit
		Collections.sort(renderList);
	
		
		for (int i = renderList.size() - 1; i >= 0; i--)
		{
			Element el = renderList.get(i);
			if (el.isExtraShadow())		el.renderShadow(spriteBatch, 0, 0, -30);
			el.render(spriteBatch);
		}
		
	}
}
