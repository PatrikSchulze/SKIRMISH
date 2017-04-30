package desktop;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

public class DoTexturePacking
{
	public static void main(String[] args)
	{
    	TexturePacker2.Settings settings = new TexturePacker2.Settings();
    	settings.maxWidth = 1024;
    	settings.maxHeight = 1024;
    	settings.filterMag = TextureFilter.Linear;
    	settings.filterMin = TextureFilter.Linear;
    	
    	TexturePacker2.process(settings, "content/raw_unpacked", "content/atlas", "all");
    	TexturePacker2.process(settings, "content/raw_menu", "content/atlas", "menu");
	}
}
