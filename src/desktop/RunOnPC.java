package desktop;

import java.awt.GraphicsEnvironment;

import main.Game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;


public class RunOnPC
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		boolean DEBUG = true;
		int dw = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		int dh = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 0;
		config.resizable = false;
		
		
		if(DEBUG)
		{
			config.width = 1024;
			config.height = 768;
		}
		else
		{
			if (dw >= 1280 && dh >= 720)
			{
				if (dw >= 1366 && dh >= 768)
				{
					if (dw >= 1600 && dh >= 900)
					{
						config.width = 1600;
						config.height = 900;
					}
					else
					{
						config.width = 1366;
						config.height = 768;
					}
				}
				else
				{
					config.width = 1280;
					config.height = 720;
				}
			}
			else
			{
				config.width = 1024;
				config.height = 576;
			}
		}
		
//		config.width 	= (int)(800*1.0f);
//		config.height 	= (int)(480*1.0f);
//		config.width 	= (int)(1280*1.0f);
//		config.height 	= (int)(800 *1.0f);
		
		config.width 	= (int)(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()*0.85f);
		config.height 	= (int)(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()*0.8f);
//		
		config.title = "SKIRMISH";
		
		config.addIcon("content/img/desktop/icon128.png", FileType.Internal);
		config.addIcon("content/img/desktop/icon64.png", FileType.Internal);
		config.addIcon("content/img/desktop/icon32.png", FileType.Internal);
		config.addIcon("content/img/desktop/icon16.png", FileType.Internal);
		
		new LwjglApplication(new Game(60f), config);
		
		
//		new LwjglApplication(new Gdx2DTest(), "Skirmish PC", 800, 480, false);
		
		/*
		//ANDROID
		getWindow().addFlags(WindowManager.LayoutParam.FLAG_KEEP_SCREEN_ON);
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
//        config.numSamples = 2; // seems reasonable, you can turn it up of course, but bench!
        initialize(new Game(), false);
//        initialize(new Game(), config);
		*/
	}
	
	

}
