package main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class MapChoiceMenu
{
	public static float scale;
	public static float widthOfAMapPic;
	public static float targetWidthOfAMapPic;
	
	private static Sprite sprMap1Field, sprMap1Snow, sprMap1Desert, sprMap2Field, sprMap2Snow, sprMap2Desert;
	private static Sprite sprField;
	private static Sprite sprSnow;
	private static Sprite sprDesert;
	private static Sprite sprBack;
	
	public static void load(TextureAtlas atlas)
	{
		sprField  = new Sprite(atlas.findRegion("level-choice_field"));
		sprSnow   = new Sprite(atlas.findRegion("level-choice_snow"));
		sprDesert = new Sprite(atlas.findRegion("level-choice_desert"));
		sprBack   = new Sprite(atlas.findRegion("level-choice_back"));
		
		sprMap1Field  = new Sprite(atlas.findRegion("map1"));
		sprMap1Snow   = new Sprite(atlas.findRegion("map1"));
		sprMap1Desert = new Sprite(atlas.findRegion("map1"));
		
		sprMap2Field  = new Sprite(atlas.findRegion("map2"));
		sprMap2Snow   = new Sprite(atlas.findRegion("map2"));
		sprMap2Desert = new Sprite(atlas.findRegion("map2"));
		
		widthOfAMapPic = sprField.getWidth();
		targetWidthOfAMapPic = (Gdx.graphics.getWidth()/6.0f);
		
		scale = targetWidthOfAMapPic / widthOfAMapPic;
		
		float offsetX = (targetWidthOfAMapPic-widthOfAMapPic)/2.0f;
		if (offsetX < 0) offsetX = 0;
		
		float padd = Gdx.graphics.getHeight()*0.025f;
		
		sprBack.setBounds(Gdx.graphics.getWidth()-(sprBack.getWidth()*scale), Gdx.graphics.getHeight()-(sprBack.getHeight()*scale), sprBack.getWidth()*scale, sprBack.getHeight()*scale);
		
		
		
		float sss = Gdx.graphics.getWidth()/1280f;
		
		float widthOfImagePlusMapButtons = targetWidthOfAMapPic+(sprMap1Field.getWidth()*sss);
		
		
		sprField.setSize( sprField.getWidth()*scale,  sprField.getHeight()*scale );
		sprSnow.setSize(   sprSnow.getWidth()*scale,   sprSnow.getHeight()*scale  );
		sprDesert.setSize( sprDesert.getWidth()*scale, sprDesert.getHeight()*scale);
		
		sprMap1Field.setSize(sprMap1Field.getWidth()*sss, sprMap1Field.getHeight()*sss);
		sprMap2Field.setSize(sprMap2Field.getWidth()*sss, sprMap2Field.getHeight()*sss);
		sprMap1Snow.setSize(sprMap1Snow.getWidth()*sss, sprMap1Snow.getHeight()*sss);
		sprMap2Snow.setSize(sprMap2Snow.getWidth()*sss, sprMap2Snow.getHeight()*sss);
		sprMap1Desert.setSize(sprMap1Desert.getWidth()*sss, sprMap1Desert.getHeight()*sss);
		sprMap2Desert.setSize(sprMap2Desert.getWidth()*sss, sprMap2Desert.getHeight()*sss);
		
		
		
		
		sprField.setPosition( padd+sprMap1Field.getWidth(),   padd);
		sprSnow.setPosition(  (Gdx.graphics.getWidth()*0.50f)-(widthOfImagePlusMapButtons/2f)+sprMap1Field.getWidth(),   padd);
		sprDesert.setPosition((Gdx.graphics.getWidth()-(sprDesert.getWidth())-padd),   padd);
		
		sprMap1Field.setPosition(sprField.getX()-sprMap1Field.getWidth()+3, sprField.getY()+sprField.getHeight()-(sprMap1Field.getHeight()*1.5f));
		sprMap2Field.setPosition(sprMap1Field.getX(), sprField.getY()+(sprMap1Field.getHeight()*0.5f));
		
		sprMap1Snow.setPosition(sprSnow.getX()-sprMap1Snow.getWidth()+3, sprSnow.getY()+sprSnow.getHeight()-(sprMap1Snow.getHeight()*1.5f));
		sprMap2Snow.setPosition(sprMap1Snow.getX(), sprSnow.getY()+(sprMap1Snow.getHeight()*0.5f));
		
		sprMap1Desert.setPosition(sprDesert.getX()-sprMap1Desert.getWidth()+3, sprDesert.getY()+sprDesert.getHeight()-(sprMap1Desert.getHeight()*1.5f));
		sprMap2Desert.setPosition(sprMap1Desert.getX(), sprDesert.getY()+(sprMap1Desert.getHeight()*0.5f));
		
	}
	
	public static boolean isMouseOnBack()
	{
		return sprBack.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY());
	}
	
	public static int isMouseOnField()
	{
		if (sprMap1Field.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 1;
//		if (sprMap2Field.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 2;
		
		return -1;
	}
	
	public static int isMouseOnSnow()
	{
		if (sprMap1Snow.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 1;
//		if (sprMap2Snow.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 2;
		
		return -1;
	}
	
	public static int isMouseOnDesert()
	{
		if (sprMap1Desert.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 1;
//		if (sprMap2Desert.getBoundingRectangle().contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY())) return 2;
		
		return -1;
	}
	
	public static void render(SpriteBatch sb)
	{
		
		sprMap1Field.draw(sb);
		sprMap1Snow.draw(sb);
		sprMap1Desert.draw(sb);
		
//		sprMap2Field.draw(sb);
//		sprMap2Snow.draw(sb);
//		sprMap2Desert.draw(sb);
		
		sprField.draw(sb);
		sprSnow.draw(sb);
		sprDesert.draw(sb);
		sprBack.draw(sb);
		
//		sb.draw(tregTitle, 0+((tregTitle.getRegionWidth()/8.0f)*scale), Gdx.graphics.getHeight()-(tregTitle.getRegionHeight()*scale), tregTitle.getRegionWidth()*scale, tregTitle.getRegionHeight()*scale);
	}
	
}
