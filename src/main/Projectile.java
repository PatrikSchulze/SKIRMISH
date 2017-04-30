package main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Whenever a Unit fires it creates Projectiles
 * @author Patrik
 *
 */
public class Projectile extends Element
{
	ParticleEffect pe = null;
	
	/*
	 * We null/omit the texturename here for projectiles... not needed.
	 */
	public Projectile(TextureRegion treg, float _x, float _y, float rot)
	{
		super(treg, null, _x, _y);
		setX(getX()-getWidth()/2);
		setY(getY()-getHeight()/2);
		setRotation(rot);
		super.compute();
	}
	
	public void setParticleEffect(String in)
	{
		pe = new ParticleEffect();
		pe.load(Gdx.files.internal("content/particles/"+in+".p"), Gdx.files.internal("content/particles"));
		pe.start();
		pe.setPosition(getX(), getY());
	}
	
	public void killEffect()
	{
		if (pe != null)
		{
//			pe.dispose();
			pe = null;
		}
	}
	
	@Override
	public void render(SpriteBatch spriteBatch)
	{
		if (pe != null) pe.draw(spriteBatch);
		super.render(spriteBatch);
	}
	
	@Override
	public void compute()
	{
		super.compute();
		if (pe != null)
		{
			pe.setPosition(centerX, centerY);
			pe.update(Gdx.graphics.getRawDeltaTime());
		}
	}
}
