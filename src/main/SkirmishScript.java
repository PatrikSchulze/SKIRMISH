package main;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;

public class SkirmishScript
{
	private static GameMap gameMap;
	private static long lastModi;
	
	private static FileHandle reload_fh;
	private static TextureAtlas reload_atlas;
	private static World reload_world;
	private static HashMap<String, OurBodyEditorLoader> reload_mapBox2DLoaders;

	
	public static GameMap getGameMap()
	{
		return gameMap;
	}
	
	/*
	 * First line of a skirmish map script is the initilizer, it has to be first - setting size and all that
	 * 
	 * lines starting with # or which are empty will be ignored
	 * string will be trimmed and caselowered
	 */
	public static void loadMapScript(FileHandle fh, TextureAtlas atlas, World world, HashMap<String, OurBodyEditorLoader> mapBox2DLoaders)
	{
		if (!fh.exists())
		{
			System.err.println("Error: SKIRMISH script "+fh.path()+" doesn't exist.");
			return;
		}
		
		reload_fh = fh;
		reload_atlas = atlas;
		reload_world = world;
		reload_mapBox2DLoaders = mapBox2DLoaders;

		String[] lines = fh.readString().split("\r\n|\r|\n");
		gameMap = null;
		OurBodyEditorLoader loader = null;
		
		if (lines[0].startsWith("map:"))
		{
			//TODO
			//code assumes 
			
			String[] initElems 	= lines[0].substring(4).split(",");
			String mapNameRoot	= initElems[0].split("_")[0];
			int mapNum 			= Integer.parseInt(initElems[0].split("_")[1]);
			
			try{
				gameMap = new GameMap(initElems[0], atlas.findRegion(initElems[1]), Integer.parseInt(initElems[2]), Integer.parseInt(initElems[3]), world);
				gameMap.setMapNameRoot(mapNameRoot);
				gameMap.setMapNum(mapNum);
			}catch(Exception e)
			{
				System.err.println("Error at line 0");
				e.printStackTrace();
			}
			
			//BOX2D
			loader = mapBox2DLoaders.get(mapNameRoot);
			if (loader == null)
			{
				loader = new OurBodyEditorLoader(Gdx.files.internal("content/maps/box2d/" + mapNameRoot + ".json"));
				mapBox2DLoaders.put(mapNameRoot, loader);
			}
		}
		else
		{
			System.err.println("Error at line 0, init wrong");
		}
		
		for (int i=1;i<lines.length;i++)
		{
			if (lines[i].startsWith("#"))
			{
				continue;
			}
			else if (lines[i].startsWith("additive:"))
			{
				if (lines[i].equals("additive:true"))
				{
					gameMap.useAdditiveBlending = true;
				}
				else
				{
					gameMap.useAdditiveBlending = false;
				}
			}
			else if (lines[i].startsWith("obstacle:"))
			{
				String[] elem = lines[i].substring(9).split(",");
				
				gameMap.addObstacle(atlas.findRegion(elem[0]),
						Integer.parseInt(elem[1]), Integer.parseInt(elem[2]), Integer.parseInt(elem[3]),
						Boolean.parseBoolean(elem[4]), Boolean.parseBoolean(elem[5]), world, elem[0], atlas, loader, Boolean.parseBoolean(elem[6]));
			}
			else if (lines[i].startsWith("unit:"))
			{
				String[] elem = lines[i].substring(5).split(",");
				
				Unit.OWNER owner = null;
				if (elem[3].equals("P1"))
				{
					owner = Unit.OWNER.PLAYER1;
				}
				else if (elem[3].equals("P2"))
				{
					owner = Unit.OWNER.PLAYER2;
				}
				else System.err.println("Error with Unit ownership, what is : "+elem[3]);
				
				Unit.TYPE type = null;
				Unit.TYPE[] allTypes = Unit.TYPE.values();
				
				for (int t=0;t<allTypes.length;t++)
				{
					if (elem[4].equalsIgnoreCase(allTypes[t].toString()))
					{
						type = allTypes[t];
					}
				}
					
				if (type == null) System.err.println("Error with Unit TYPE, what is : "+elem[4]);
				else System.out.println("Adding "+type);
				
				Unit u = new Unit(atlas.findRegion(elem[0]), elem[0], Integer.parseInt(elem[1]), Integer.parseInt(elem[2]), owner, type, world);
				u.rotate(Integer.parseInt(elem[5]));
				gameMap.units.add(u);
			}
			else if (lines[i].startsWith("floor:"))
			{
				String[] elem = lines[i].substring(6).split(",");
				
				Floor.EFFECT eff = null;
				Floor.EFFECT[] allTypes = Floor.EFFECT.values();
				
				for (int t=0;t<allTypes.length;t++)
				{
					if (elem[6].equalsIgnoreCase(allTypes[t].toString()))
					{
						eff = allTypes[t];
					}
				}
					
				if (eff == null) System.err.println("Error with Floor EFFECT, what is : "+elem[6]);
				
				if (atlas.findRegion(elem[0]) == null)
				{
					System.err.println("TextureAtlas find '"+elem[0]+"' is null");
				}
				
				gameMap.addFloor(atlas.findRegion(elem[0]), elem[0],
						Integer.parseInt(elem[1]), Integer.parseInt(elem[2]), Integer.parseInt(elem[3]),
						Boolean.parseBoolean(elem[4]), Boolean.parseBoolean(elem[5]), eff);
				
				//floor,-130,366,90,false,true,NONE
			}
			else if (lines[i].startsWith("powerup:"))
			{
				String[] elem = lines[i].substring(8).split(",");
				
				Powerup.TYPE type = null;
				Powerup.TYPE[] allTypes = Powerup.TYPE.values();
				
				for (int t=0;t<allTypes.length;t++)
				{
					if (elem[4].equalsIgnoreCase(allTypes[t].toString()))
					{
						type = allTypes[t];
					}
				}
					
				if (type == null) System.err.println("Error with Floor EFFECT, what is : "+elem[4]);
				
				if (atlas.findRegion(elem[0]) == null)
				{
					System.err.println("TextureAtlas find '"+elem[0]+"' is null");
				}
				
				gameMap.addPowerup(atlas.findRegion(elem[0]), elem[0],
						Integer.parseInt(elem[1]), Integer.parseInt(elem[2]), Integer.parseInt(elem[3]), type);
				
				//powerup:powerup-1,1400,800,0,SMALL_HEALTH
			}
		}
		
		lastModi = fh.lastModified();
		
		
		
//		GameMap gameMap = new GameMap("desert", atlas.findRegion("texture-desert"), 4500, 3000);
//		map:desert,texture-desert,4500,3000
//		
//		
//		gameMap.addObstacle(atlas.findRegion("rock1-2"), 0-130, 235+130+1, 90,false,true, mWorld,"rock1-2", atlas, loader);
//		obstacle:rock1-2,-130,366,90,false,true
//		
//		Unit nice1 = new Unit(atlas.findRegion("light-tank-blue"),900,200, Unit.OWNER.PLAYER1, TYPE.LIGHT_TANK, mWorld);
//		nice1.rotate(205);
//		gameMap.units.add(nice1);
//		unit:light-tank-blue,900,200,P1,LIGHT_TANK,205
		
	}
	
	public static void writeMapScript(GameMap _gameMap)
	{
		gameMap = _gameMap;
		
		FileHandle fh = new FileHandle("content/maps/"+gameMap.getName()+".ssm");
		StringBuilder strb = new StringBuilder();
		
		strb.append("map:"+gameMap.getName()+",texture-"+gameMap.getMapNameRoot()+","+gameMap.getWidth()+","+gameMap.getHeight()+"\n");
		
		strb.append("additive:"+gameMap.useAdditiveBlending+"\n");
		
		for (Element e : gameMap.obstacles)
		{
			strb.append("obstacle:"+e.getTexName()+","+(int)e.getX()+","+(int)e.getY()+","+(int)e.getRotation()+","+e.hFlipped+","+e.vFlipped+","+e.extraShadow+"\n");
		}
		
		for (Floor e : gameMap.floors)
		{
			strb.append("floor:"+e.getTexName()+","+(int)e.getX()+","+(int)e.getY()+","+(int)e.getRotation()+","+e.hFlipped+","+e.vFlipped+","+e.getEffect()+"\n");
		}
		
		for (Powerup p : gameMap.powerups)
		{
			strb.append("powerup:"+p.getTexName()+","+(int)p.getX()+","+(int)p.getY()+","+(int)p.getRotation()+","+p.getType()+"\n");
		}
		
		for (Unit u : gameMap.units)
		{
			String owner = (u.owner == Unit.OWNER.PLAYER1) ? "P1" : "P2";
			String type = u.type.toString();
			strb.append("unit:"+u.getTexName()+","+(int)u.getX()+","+(int)u.getY()+","+owner+","+type+","+(int)u.getRotation()+"\n");
			//unit:light-tank-blue,900,200,P1,LIGHT_TANK,205
		}

		
		fh.writeString(strb.toString(), false);
		System.out.println("Written map '"+gameMap.getName()+"' to "+fh.path());
	}
	
	public static boolean hasChanged()
	{
		return (lastModi != reload_fh.lastModified());
	}
	
	public static void debugQuickReloadIfChanged()
	{
		if (hasChanged()) loadMapScript(reload_fh, reload_atlas, reload_world, reload_mapBox2DLoaders);
	}
}
