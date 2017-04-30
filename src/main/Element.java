package main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Element extends Sprite implements Comparable
{
	boolean dead = false;
	String textureName;
	float centerX = getX()+(getWidth()/2);
	float centerY = getY()+(getHeight()/2);
	boolean hFlipped=false;
	boolean vFlipped=true;
	boolean extraShadow = false;
	float biggestSize = 0;
	Color tint = new Color(1,1,1,1);
	private Rectangle collRect = new Rectangle(0,0,0,0);
	protected Body uBody;
	private static Vector3 calcV3_1 = new Vector3(0,0,1);
	private static Vector3 calcV3_2 = new Vector3(0,0,1);
	protected World bodyWorld;
	private static Color preColor = new Color(1,1,1,1);
	private TextureRegion texreg;
	private BoundingBox box = null;

	public Element(TextureRegion treg, String _textureName, float _x, float _y)
	{
		super(treg);
		textureName = _textureName;
		setX(_x);
		setY(_y);
		biggestSize = (getWidth() > getHeight()) ? getWidth() : getHeight();
		texreg = treg; 
		box = new BoundingBox();
	}
	
	public Element(TextureRegion treg, String _textureName)
	{
		super(treg);
		textureName = _textureName;
		setX((float)Gdx.graphics.getWidth()/2.0f);
		setY((float)Gdx.graphics.getHeight()/2.0f);
		biggestSize = (getWidth() > getHeight()) ? getWidth() : getHeight();
		texreg = treg;
		box = new BoundingBox();
	}
	
	public boolean isExtraShadow()
	{
		return extraShadow;
	}

	public void setExtraShadow(boolean extraShadow)
	{
		this.extraShadow = extraShadow;
	}

	public String getTexName() { return textureName; }
	
	public void clearBody()
	{
		bodyWorld.destroyBody(uBody);
	}
	
	public void createStaticBody(World mWorld)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(this.centerX/Game.PIXELS_PER_METER, this.centerY/Game.PIXELS_PER_METER);
 		uBody = mWorld.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(this.getWidth() / ( 2 * Game.PIXELS_PER_METER), this.getHeight() / (2 * Game.PIXELS_PER_METER));
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(this.centerX/Game.PIXELS_PER_METER,this.centerY/Game.PIXELS_PER_METER,  (float) ((this.getRotation()/180.0f)*Math.PI));
		bodyWorld = mWorld;
	}
	
	public void createStaticBody(World mWorld, String name, TextureAtlas atlas, OurBodyEditorLoader loader)
	{
		// 0. Create a loader for the file saved from the editor.
	    if (!loader.exist(name)) {createStaticBody(mWorld); return; }
	    
	    centerX = getX()+(getWidth()/2);
		centerY = getY()+(getHeight()/2);
	    
	    float rads = MathUtils.degreesToRadians * (getRotation());
	    
	    // 1. Create a BodyDef, as usual.
	    BodyDef bd = new BodyDef();
	    bd.position.set(0, 0);
//	    bd.angle = rads;
	    bd.type = BodyType.StaticBody;
	 
	    // 2. Create a FixtureDef, as usual.
	    FixtureDef fd = new FixtureDef();
	    fd.density = 7000;
	    fd.friction = 0f;
	 
	    // 3. Create a Body, as usual.
	    uBody = mWorld.createBody(bd);
	    uBody.setFixedRotation(false);
	    
	    
	    // 4. Create the body fixture automatically by using the loader.
	    loader.attachFixture(uBody, name, fd, atlas.findRegion(name).getRegionWidth()/Game.PIXELS_PER_METER, vFlipped, hFlipped);
	    
	    

	    float trX = centerX/Game.PIXELS_PER_METER;
	    float trY = centerY/Game.PIXELS_PER_METER;
	    
//	    trX+=(MathUtils.sin(rads)*(atlas.findRegion(name).getRegionWidth()/Game.PIXELS_PER_METER));
//	    trY+=(MathUtils.cos(rads)*(atlas.findRegion(name).getRegionWidth()/Game.PIXELS_PER_METER));
	    
	    
//	    //yDown flip
//	    trY+=(getHeight()/Game.PIXELS_PER_METER);
//	    //tiny fix
//	    trY+= - 1.2f;
	    
//	    if (hFlipped)
//	    {
//	    	trX+=(getWidth()/Game.PIXELS_PER_METER);
//	    	trX+= - 1.2f;
//	    }
//	    
//	    if (vFlipped)
//	    {
//	    	trY+=-(getHeight()/Game.PIXELS_PER_METER);
//	 	    trY+=+ 1.2f;
//	    }
	    
//	    uBody.
	    
	    uBody.setTransform(trX, trY ,  rads);
//	    Transform transform = uBody.getTransform();
//	    
//	    uBody.
	    
		
	    bodyWorld = mWorld;
	    
	    
	    
//	    Vector2 origin = loader.getOrigin(name, atlas.findRegion(name).getRegionWidth()/Game.PIXELS_PER_METER).cpy();
//	    System.out.println("origin: "+origin);
	    
//	    Vector2 pos = uBody.getPosition().sub(origin);
//	    
//	    this.setPosition(pos.x, pos.y);
//	    System.out.println("pos to "+pos);
//	    this.setOrigin(origin.x, origin.y);
//	    this.setRotation(uBody.getAngle() * MathUtils.radiansToDegrees);
	    
	}
	
	public void createStaticBody(World mWorld, Vector2[] vertices)
	{
//		System.out.println("texX: "+this.getRegionX()+", "+this.getRegionY()+": "+this.getRegionWidth()+", "+this.getRegionHeight());
		if(vertices == null || vertices.length == 0)
		{
			createStaticBody(mWorld);
			return;
		}
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(this.centerX/Game.PIXELS_PER_METER, this.centerY/Game.PIXELS_PER_METER);
 		uBody = mWorld.createBody(bodyDef);
 		
		PolygonShape shape = new PolygonShape();
		// creating polygonshape with vertex data

		 // invert values if necessary
		if((!hFlipped && vFlipped)||(hFlipped && !vFlipped))
		{
//			System.out.println("invert");
			Vector2[] vTemp = new Vector2[vertices.length];
			int j=0;
			for(int i=vertices.length-1;i>=0;i--)
			{
				vTemp[i] = new Vector2();
				if(hFlipped)
					vTemp[i].x = -vertices[j].x;
				else 
					vTemp[i].x = vertices[j].x;
				if(vFlipped)		
					vTemp[i].y = -vertices[j].y;
				else
					vTemp[i].y = vertices[j].y;

				j++;
			}
			shape.set(vTemp);
		}
		else
		{
			if(hFlipped && vFlipped)
			{
				Vector2[] vTemp = new Vector2[vertices.length];
				for(int i=0;i<vertices.length;i++)
				{
					vTemp[i] = new Vector2();
					if(hFlipped)
						vTemp[i].x = -vertices[i].x;
					else 
						vTemp[i].x = vertices[i].x;
					if(vFlipped)		
						vTemp[i].y = -vertices[i].y;
					else
						vTemp[i].y = vertices[i].y;
				}
				shape.set(vTemp);
			}
			else
			{
				shape.set(vertices);
			}

		}
		
		uBody.setFixedRotation(false);
		uBody.createFixture(shape, 7000);
		uBody.setTransform(this.centerX/Game.PIXELS_PER_METER,this.centerY/Game.PIXELS_PER_METER,  (float) ((this.getRotation()/180.0f)*Math.PI));
		bodyWorld = mWorld;
	}
	
	public void flip(boolean hor, boolean vert)
	{
		hFlipped = hor;
		vFlipped = vert;
		super.flip(hor, vert);
	}
	
	public Rectangle getCollRect(){ return collRect; }
	
	public void compute()
	{
		centerX = getX()+(getWidth()/2);
		centerY = getY()+(getHeight()/2);
		
		float padding = 0.84f; 
		collRect.set(getX()+((getWidth()-(getWidth()*padding))/2), getY()+((getHeight()-(getHeight()*padding))/2), getWidth()*padding, getHeight()*padding);
		
		if (tint.r < 1.0f)
		{
			tint.r+=0.01f;
			if (tint.r > 1.0f) tint.r = 1.0f;
		}
		
		if (tint.g < 1.0f)
		{
			tint.g+=0.01f;
			if (tint.g > 1.0f) tint.g = 1.0f;
		}
		
		if (tint.b < 1.0f)
		{
			tint.b+=0.01f;
			if (tint.b > 1.0f) tint.b = 1.0f;
		}
		
		if (tint.a < 1.0f)
		{
			tint.a+=0.01f;
			if (tint.a > 1.0f) tint.a = 1.0f;
		}
	}
	
	public void addX(float in)
	{
		setX(getX()+in);
	}
	
	public void addY(float in)
	{
		setY(getY() + (in) );
		
	}
	
	public void subX(float in)
	{
		setX(getX() - (in) );
		
	}
	
	public void subY(float in)
	{
		setY(getY() - (in));
	}
	
	public boolean isWithinFrustum(OrthographicCamera cam)
	{
		calcV3_1.set  (centerX-(biggestSize/2f), centerY-(biggestSize/2f), 1);
		calcV3_2.set  (centerX+(biggestSize/2f), centerY+(biggestSize/2f), 1);
		
		box.set(calcV3_1, calcV3_2);
		
		return cam.frustum.boundsInFrustum(box);
	}
	
	public void render(SpriteBatch spriteBatch)
	{
		setColor(tint);
		draw(spriteBatch);
	}
	
	public void render(SpriteBatch spriteBatch, Color c)
	{
		setColor(c);
		draw(spriteBatch);
	}
	
	public void renderShadow(SpriteBatch spriteBatch, float xoffset, float yoffset, float rot)
	{
		preColor = spriteBatch.getColor();
		spriteBatch.setColor(0, 0, 0, 0.3f);
		
		spriteBatch.draw(texreg, getX()+xoffset, getY()+yoffset, 0, 0+getHeight(), getWidth(), getHeight(), 1f, 1f, rot);
		
		spriteBatch.setColor(preColor);
	}

	//Yeah be careful here. This comparator is simple for renderlist y comparasion
	@Override
	public int compareTo(Object arg)
	{
		if (arg instanceof Element)
		{
			Element in = (Element)arg;
			if (this.getY()+this.getHeight() < in.getY()+in.getHeight())
			{
				return 1;
			}
			if (this.getY()+this.getHeight() > in.getY()+in.getHeight())
			{
				return -1;
			}
			else return 0;
		}
		else	return 0;
	}

}
