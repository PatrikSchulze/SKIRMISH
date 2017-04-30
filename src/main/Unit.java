package main;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Unit extends Element
{
	public static enum TYPE { LIGHT_TANK, COPTER, HEAVY_TANK, BUGGY; }
	public static enum OWNER { PLAYER1, PLAYER2; }
	public OWNER owner = OWNER.PLAYER1;
	public TYPE type = null;
	
	public static final float COPTER_TURNSPEED = 1.9f;
	
	private boolean reduceProjectiles = true;
	
	PooledEffect pe = null;
	float speedX = 0.0f;
	float speedY = 0.0f;
	float rotorAngle = 0;
	float accDriveSpeed = 0.02f; // acceleration, how fast can it get to speed, in percent
	float curDriveSpeed = 0.0f; // in percent
	float maxDriveSpeed = 5.0f;
	float projectileSpeed = 30.0f;
	float distanceToShoot = 0f;
	float maxShotDistance = 400.0f;
	float damageMultipl = 1.0f;
	int damageRange = 140;
	int projShootAmount;//how many proj a unit shoots per turn
	private float maxHP = 180.0f;
	private float HP = maxHP;
	long hpChangeDisplayTime, projDelay; // in ms
	public ArrayList<Vector2> shotPoint = new ArrayList<Vector2>();
	public ArrayList<Vector2> shotPointSpeed = new ArrayList<Vector2>();
	private float actionValue = 1.0f;
	private float actionSubtraction = 0.0005f;
	boolean shooting = false; // projectile flying
	boolean targeting = false; // target marking moving
	boolean flying = false;
	boolean doneShooting = false;
	public ArrayList<Projectile> projs = null;
//	private Body uBody;
	private float activeLinearDamping = 5.0f;
	private float passiveLinearDamping = 50.0f;
	private float activeAngularDamping = 40.0f;
	private float passiveAngularDamping = 80.0f;
	float velocityMultiplier = 1000.0f;
	private static Vector2 vecTemp1 = new Vector2(0,0);
	private static Vector2 vecTemp2 = new Vector2(0,0);
	
	/*
	 * At speed:
	 * the further the finger is away, the faster you go.
	 * meaning curDriveSpeed isnt accerlated to 1.0 below a certain margin of distance
	 * something like
	 * if (distance > 200) approach until max (1.0)
	 * below that, go to relative percentage. eg: 100 goes to 0.5 max, 50 to 0.25 and so on
	 */
	
	public Unit(TextureRegion treg, String _textureName, float _x, float _y, OWNER _owner, TYPE _type, World mWorld)
	{
		super(treg, _textureName);
		bodyWorld = mWorld;
		setX(_x);
		setY(_y);
		owner = _owner;
		type = _type;
		projs = new ArrayList<Projectile>();
		if (type == TYPE.LIGHT_TANK)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.DynamicBody;
			bodyDef.position.set(_x/Game.PIXELS_PER_METER, _y/Game.PIXELS_PER_METER);
	 		uBody = mWorld.createBody(bodyDef);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(94f / ( 2.0f * Game.PIXELS_PER_METER), 64f / (2f * Game.PIXELS_PER_METER));
			uBody.setFixedRotation(false);
			uBody.createFixture(shape, 100);
			activeAngularDamping = 40.0f;
			activeLinearDamping = 5.0f;
			passiveLinearDamping = 50.0f;
			passiveAngularDamping = 80.0f;
			
			projShootAmount = 1;
			projDelay = 300;
			accDriveSpeed = 0.02f;
			maxDriveSpeed = 4.0f;
			projectileSpeed = 30.0f;
			distanceToShoot = 0f;
			maxShotDistance = 400.0f;
			damageMultipl = 1.0f;
			damageRange = 140;
			maxHP = 180.0f;
			actionSubtraction = 0.002f;
		}
		else if (type == TYPE.COPTER)
		{
			if (reduceProjectiles == false)
			{
				projShootAmount = 4;
			}
			else
			{
				projShootAmount = 1;
			}
			projDelay = 400;
			flying = true;
			damageRange = 150;
			damageMultipl = 0.6f;
			accDriveSpeed = 0.1f;
			maxDriveSpeed = 5.0f;
			maxHP = 140.0f;
			projectileSpeed = 20.0f;
			maxShotDistance = 500.0f;
			actionSubtraction = 0.005f;
		}
		else if (type == TYPE.BUGGY)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.DynamicBody;
			bodyDef.position.set(_x/Game.PIXELS_PER_METER, _y/Game.PIXELS_PER_METER);
	 		uBody = mWorld.createBody(bodyDef);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(91f / ( 2.0f * Game.PIXELS_PER_METER), 60f / (2f * Game.PIXELS_PER_METER));
			uBody.setFixedRotation(false);
			uBody.createFixture(shape, 40);
			activeAngularDamping = 40.0f;
			activeLinearDamping = 5.0f;
			passiveLinearDamping = 50.0f;
			passiveAngularDamping = 80.0f;
			
			if (reduceProjectiles == false)
			{
				projShootAmount = 7;
			}
			else
			{
				projShootAmount = 1;
			}
			projDelay = 300;
			accDriveSpeed = 0.22f;
			damageRange = 60;
			damageMultipl = 1.0f;
			maxDriveSpeed = 2.2f;
			projectileSpeed = 35.0f;
			maxShotDistance = 400.0f;
			maxHP = 150.0f;
			actionSubtraction = 0.001f;
		}
		else if (type == TYPE.HEAVY_TANK)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.DynamicBody;
			bodyDef.position.set(_x/Game.PIXELS_PER_METER, _y/Game.PIXELS_PER_METER);
	 		uBody = mWorld.createBody(bodyDef);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(119f / ( 2.0f * Game.PIXELS_PER_METER), 88f / (2f * Game.PIXELS_PER_METER));
			uBody.setFixedRotation(false);
			uBody.createFixture(shape, 400);
			activeAngularDamping = 40.0f;
			activeLinearDamping = 5.0f;
			passiveLinearDamping = 80.0f;
			passiveAngularDamping = 99.0f;
			
			projShootAmount = 1;
			projDelay = 300;
			damageRange = 200;
			damageMultipl = 2.2f;
			projectileSpeed = 45.0f;
			maxDriveSpeed = 9.0f;
			maxShotDistance = 400.0f;
			maxHP = 600.0f;
			actionSubtraction = 0.0025f;
		}
		HP = maxHP;
		super.compute();
	}
	
	public void push(float x, float y)
	{
		if(uBody != null){
			vecTemp1.set(x,y);
			vecTemp2.set(uBody.getPosition().x, uBody.getPosition().y);
			uBody.applyLinearImpulse(vecTemp1, vecTemp2, true);
		}
	}
	
	public long getHPChangeDisplayTime() { return hpChangeDisplayTime;}
	
	public float getHP() { return HP; }
	public float getMaxHP() { return maxHP; }
	
	/*
	 * Needs a TextureAtlas so that the sprite can be switched when there is damage
	 */
	public void changeHP(float delta, TextureAtlas atlas)
	{
		HP+=delta;
		hpChangeDisplayTime = System.currentTimeMillis()+3000;
		
		//damage
		if (delta < 0)
		{
			if (HP < 0) HP = 0;
			
			this.tint.g = 0;
			this.tint.b = 0;
			if (HP > 0 && HP < maxHP*0.4f && !getTexName().endsWith("-dmg"))
			{
				this.setRegion(atlas.findRegion(getTexName()+"-dmg"));
				textureName = getTexName()+"-dmg";
			}
			
			if (pe == null && HP > 0 && HP < maxHP*0.4f)
			{
				pe = ParticleManager.getNewPooledEffect("dmgsmoke");;
				pe.start();
				pe.setPosition(centerX, centerY);
			}
		}
		else if (delta > 0)
		{
			System.out.println("Adding HP: "+delta);
			if (HP > maxHP) HP = maxHP;
			
			this.tint.g = 1;
			this.tint.r = 0;
			this.tint.b = 0;
			
			if (HP >= maxHP*0.4f && getTexName().endsWith("-dmg"))
			{
				//substring minus "-dmg"
				this.setRegion(atlas.findRegion(getTexName().substring(0, getTexName().length()-4)));
				textureName = getTexName().substring(0, getTexName().length()-4);
			}
			
			if (pe != null && HP > 0 && HP >= maxHP*0.4f)
			{
				pe.dispose();
				pe = null;
			}
		}
	}
	
	public void stopMovement()
	{
		if (curDriveSpeed > 0.0f) curDriveSpeed-=accDriveSpeed;
		if (curDriveSpeed < 0.0f) curDriveSpeed=0.0f;
	}
	
	public void moveTo(float _x, float _y, GameMap gameMap, OrthographicCamera cam)
	{
		if (curDriveSpeed < 1.0f) curDriveSpeed+=accDriveSpeed;
		if (curDriveSpeed > 1.0f) curDriveSpeed=1.0f;
		
		float dx = _x-this.centerX;
		float dy = _y-this.centerY;
		
		if (actionValue > 0.0f)
		{
			double distance = SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
		    
		    if ( distance > maxDriveSpeed )
		    { 
		        // still got a way to go, so take a full step 
		    	
		    	float speedFactor = 1f;
		    	
		    	Floor.EFFECT floorCollisionType = null;
				if (!flying)
				{
					for (int fi = 0; fi < gameMap.floors.size(); fi++)
					{
						Floor f = gameMap.floors.get(fi);
						
						if (getCollRect().overlaps(f.getCollRect()))
						{
							if (f.getEffect() != Floor.EFFECT.NONE)
							{
								floorCollisionType = f.getEffect();
//								break;
							}
						}
					}
				}
				
				if (flying || floorCollisionType == null)
					speedFactor = 1f;
				else
				{
					if (floorCollisionType == Floor.EFFECT.HALFSPEED)
						speedFactor = 0.5f;
					else if (floorCollisionType == Floor.EFFECT.POINT_ONE)
						speedFactor = 0.1f;
					else if (floorCollisionType == Floor.EFFECT.POINT_TWO)
						speedFactor = 0.2f;
					else //IN CASE WE DONT KNOW WHAT TO DO. THIS SHOULDNT HAPPEN
						speedFactor = 1f;
				}	
		    	
				speedX = (float)((maxDriveSpeed*speedFactor)*dx/distance); 
		    	speedY = (float)((maxDriveSpeed*speedFactor)*dy/distance);
		    	
		    	if (HP < maxHP*0.4f)
		    	{
		    		speedX*=0.5f;
		    		speedY*=0.5f;
		    	}
		    	if(uBody != null)
	    		{
		    		vecTemp1.set(speedX*velocityMultiplier, speedY*velocityMultiplier);
					vecTemp2.set(uBody.getPosition().x, uBody.getPosition().y);
					uBody.applyLinearImpulse(vecTemp1, vecTemp2, true);
		    	}
		    }
		    
		    actionValue-=actionSubtraction;
			if (actionValue < 0.0f) actionValue = 0.0f;
			if (actionValue == 0.0f) stopMovement();
		}
	}
	
	public float getActionValue() { return actionValue; }
	public void refillAction() 	{ actionValue = 1.0f; }
	
	public void compute(GameMap map, OrthographicCamera cam)
	{
		//---------------
		//---MOVING------
		//---------------
		if(uBody != null)
		{
			uBody.setLinearDamping(passiveLinearDamping);
			uBody.setAngularDamping(passiveAngularDamping);
		}
		
		if((speedX != 0.0f || speedY != 0.0f) && type != TYPE.COPTER)
		{
			if(uBody != null)
			{
				uBody.setLinearDamping(activeLinearDamping);
				uBody.setAngularDamping(activeAngularDamping);
				
				speedX*=curDriveSpeed;
				speedY*=curDriveSpeed;
			}
			else
			{
				addX(speedX);
				this.compute();
				
				addY(speedY);
				this.compute();
				if (!flying) // flying things dont collide
				{
					boolean collidedWithObstacle = false;
					for (int oi = 0; oi < map.obstacles.size(); oi++)
					{
						Element el = map.obstacles.get(oi);
						if (getCollRect().overlaps(el.getCollRect()))
						{
							subY(speedY*1.0f);
							collidedWithObstacle = true;
							break;
						}
					}
					if (!collidedWithObstacle)
					{
						for (int ui = 0; ui < map.units.size(); ui++)
						{
							Unit un = map.units.get(ui);
							if (un == this || un.flying) continue;
							if (getCollRect().overlaps(un.getCollRect()))
							{
								subY(speedY);
							}
						}
					}
				}
			}

		}
		else if (type == TYPE.COPTER)
		{
			speedX*=curDriveSpeed;
			speedY*=curDriveSpeed;
			rotorAngle+=MathUtils.random(0.01f, 0.2f);
			if (rotorAngle > 2*MathUtils.PI) rotorAngle = (2*MathUtils.PI)+(rotorAngle-(2*MathUtils.PI));
			speedX+=(MathUtils.sin(rotorAngle)/4.0f);
			speedY+=(MathUtils.cos(rotorAngle)/4.0f);
			addX(speedX);
			addY(speedY);
		}
		//---------------
		//---SHOOTING----
		//---------------
		if(shooting || targeting)
		{
			for (int si = 0; si < shotPoint.size(); si++)
			{
				if (targeting && shotPointSpeed != null)
				{
					shotPoint.get(si).x+=shotPointSpeed.get(si).x;
					shotPoint.get(si).y+=shotPointSpeed.get(si).y;
				}
				else if (shooting && shotPointSpeed != null && projs.get(si) != null)
				{
					projs.get(si).addX(shotPointSpeed.get(si).x);
					projs.get(si).addY(shotPointSpeed.get(si).y);
					projs.get(si).compute();
				}
			}
			
			if (shotPoint.size() > 0 && shotPoint.get(0) != null)
			{
				float tdx = centerX-shotPoint.get(0).x;
				float tdy = centerY-shotPoint.get(0).y;
				distanceToShoot = (float)SquareRoot.fastSqrt((int)(tdx*tdx + tdy*tdy));
				if (distanceToShoot > maxShotDistance) distanceToShoot = maxShotDistance;
			}
		}

		if(uBody != null)
		{
			if(uBody.isAwake())
			{
				this.setPosition(uBody.getPosition().x*(float)Game.PIXELS_PER_METER-this.getWidth()/2.0f, uBody.getPosition().y*(float)Game.PIXELS_PER_METER-this.getHeight()/2.0f);
				this.setRotation((float) (uBody.getAngle()/Math.PI *180.0f));		
			}

		}
		checkUpDeath();
		if (pe != null)
		{
			pe.update(Gdx.graphics.getRawDeltaTime());
			pe.setPosition(centerX, centerY);
		}
		superCompute();
		

	}
	
	public void checkUpDeath()
	{
		if (HP <= 0)
			{
//avoid game crash (copter death)
				if (type != TYPE.COPTER)
				{
					this.clearBody();
					dead = true;
				}
				else
				{
					dead = true;
				}
			}
	}
	
	public void superCompute()
	{
		super.compute();
	}
	
	//return null if false
	//returns shotpoint vector if true
	public Vector2 projectileReachedTarget(int index)
	{
		projs.get(index).compute();
		super.compute();
		
		float dxop = projs.get(index).centerX - centerX;
		float dyop = projs.get(index).centerY - centerY;
		double distanceOwnerToProj = SquareRoot.fastSqrt((int)(dxop*dxop + dyop*dyop));
		
		float dxos = centerX - shotPoint.get(index).x;
		float dyos = centerY - shotPoint.get(index).y;
		double distanceOwnerToShotPoint = SquareRoot.fastSqrt((int)(dxos*dxos + dyos*dyos));
		
		if (distanceOwnerToProj >= distanceOwnerToShotPoint)
		{
			projs.get(index).killEffect();
			Vector2 ret = new Vector2(shotPoint.get(index).x, shotPoint.get(index).y);
			projs.remove(index);
			shotPoint.remove(index);
			shotPointSpeed.remove(index);
			if (projs.size() == 0 && shotPoint.size() == 0)
			{
				shooting = false;
				targeting = false;
				doneShooting = true;
			}
			return ret;
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public void rotate(float amount)
	{
		if(uBody != null)
		{
			uBody.setTransform(uBody.getPosition(), (float) (amount/180.0*Math.PI));
			this.setPosition(uBody.getPosition().x*(float)Game.PIXELS_PER_METER-this.getWidth()/2.0f, uBody.getPosition().y*(float)Game.PIXELS_PER_METER-this.getHeight()/2.0f);
			this.setRotation((float) (uBody.getAngle()/Math.PI *180.0f));		
		}	
		else
		{
			this.setRotation(amount);
		}
	}
	
	public void turnTo(float _x, float _y, Game.GAMEMODE mode)
	{
		float dx = this.centerX-_x;
		float dy = this.centerY-_y;
		float targetAngle = 57.2957795f*MathUtils.atan2(dy, dx);
		if (!flying || (owner == OWNER.PLAYER2 && mode == Game.GAMEMODE.AI_ENEMY))
			if(uBody == null)
			{
				setRotation(targetAngle);
			}
			else
			{
				setRotation(targetAngle);
				uBody.setTransform(uBody.getPosition(), (float) ((targetAngle/180.0f)*Math.PI));
			}
		else
		{
			while (targetAngle < 0) targetAngle+=360;
			while (targetAngle > 360) targetAngle-=360;
			
			float checkRota = getRotation();
			while (checkRota < 0) checkRota+=360;
			while (checkRota > 360) checkRota-=360;
			
			if (checkRota > targetAngle+4.0f || checkRota < targetAngle-4.0f)
			{
				float changeA = 0;
				
				float curAngle = getRotation();
				for (int i=0;i<180;i++)
				{
					curAngle++;
					if (curAngle < 0) curAngle+=360;
					if (curAngle > 360) curAngle-=360;
					if (curAngle <= targetAngle+4.0f && curAngle >= targetAngle-4.0f)
					{
						changeA = COPTER_TURNSPEED;
						break;
					}
				}
				
				curAngle = getRotation();
				for (int i=0;i<180;i++)
				{
					curAngle--;
					if (curAngle < 0) curAngle+=360;
					if (curAngle > 360) curAngle-=360;
					if (curAngle <= targetAngle+4.0f && curAngle >= targetAngle-4.0f)
					{
						changeA = -COPTER_TURNSPEED;
						break;
					}
				}
				
				setRotation(getRotation()+changeA);
			}
//			setRotation(targetAngle);
		}
	}
	
	public void setShootingDirectionPoint(int n)
	{
		shotPointSpeed.clear();
		shotPoint.clear();
		
		if (reduceProjectiles == true)
		{
			n = 1;
		}
		
		for (int i = 0; i < n; i++)
		{
			float ran = 0;
			if (reduceProjectiles == false)
			{
				if (type == TYPE.COPTER)
				{
					ran = 20.0f*(i-1.5f);
				}
				else if (type == TYPE.BUGGY)
				{
					ran = 5.0f*(i-3.5f);
				}
			}	
			Vector2 v = new Vector2(centerX, centerY);
			shotPoint.add(v);

			
			float vsdx = (v.x+(MathUtils.cosDeg(getRotation()+180+ran)*200)) - centerX;
			float vsdy = (v.y+(MathUtils.sinDeg(getRotation()+180+ran)*200)) - centerY;
			double vsdistance = SquareRoot.fastSqrt((int)(vsdx*vsdx + vsdy*vsdy));
			Vector2 vs = new Vector2((float)((projectileSpeed/2.0f)*vsdx/vsdistance), (float)((projectileSpeed/2.0f)*vsdy/vsdistance));
			shotPointSpeed.add(vs);
		}
		
		targeting = true;
	}
	
	/**
	 * Shoot projectiles.
	 * @param tex
	 */
	public void shoot(TextureRegion tex)
	{
		targeting = false;
		shooting = true;
		projs.clear();
		for (int i = 0; i < shotPoint.size(); i++)
		{
			float dx = this.centerX-shotPoint.get(i).x;
			float dy = this.centerY-shotPoint.get(i).y;
			float targetAngle = 57.2957795f*MathUtils.atan2(dy, dx);
			
			Projectile pp = new Projectile(tex, centerX, centerY, targetAngle);
			if (type == Unit.TYPE.HEAVY_TANK)
				pp.setParticleEffect("rocketfireball");//rocketfireball
			else if (type == Unit.TYPE.COPTER)
				pp.setParticleEffect("rocketsmokelong");
			else
				pp.setParticleEffect("rocketdust");//rocketfireball
			projs.add(pp);
		}
	}
	
	@Override
	public void render(SpriteBatch sb)
	{
		super.render(sb);
		if (pe != null) pe.draw(sb);
	}
	
	public void renderProjectile(SpriteBatch spriteBatch)
	{
		for (int i = projs.size() - 1; i >= 0; i--)
		{
			projs.get(i).render(spriteBatch);
		}
	}
	
	
	public void renderShadow(SpriteBatch spriteBatch, float xoffset, float yoffset)
	{
		float x = getX();
		float y = getY();
		setColor(0, 0, 0, 0.3f);
		setX(x+xoffset);
		setY(y+yoffset);
		draw(spriteBatch);
		setColor(tint);
		setX(x);
		setY(y);
	}
}
