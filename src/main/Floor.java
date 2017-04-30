package main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/*
 * Floor CAN have a type.
 */
public class Floor extends Element
{
	public static enum EFFECT { NONE, HALFSPEED, POINT_ONE, POINT_TWO; }
	private EFFECT effect = null;
	
	public Floor(TextureRegion treg, String _textureName, float _x, float _y, EFFECT _eff)
	{
		super(treg, _textureName, _x, _y);
		effect = _eff;
	}

	public EFFECT getEffect() {
		return effect;
	}

}
