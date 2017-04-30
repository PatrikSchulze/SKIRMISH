package main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Powerup extends Element
{
	public static enum TYPE { SMALL_HEALTH; }
	private TYPE type = null;
	
	public Powerup(TextureRegion treg, String _textureName, float _x, float _y, TYPE _type)
	{
		super(treg, _textureName, _x, _y);
		type = _type;
	}

	public TYPE getType() {
		return type;
	}
}
