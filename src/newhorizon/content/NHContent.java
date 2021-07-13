package newhorizon.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import newhorizon.NewHorizon;
import newhorizon.feature.UpgradeData;

public class NHContent extends Content{
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion;
	
	public static void initLoad(){
		new NHContent().load();
	}
	
	public static TextureRegion getArrowRegion(){return Core.atlas.find(NewHorizon.configName("jump-gate-arrow"));}
	public static TextureRegion getPointerRegion(){return Core.atlas.find(NewHorizon.configName("jump-gate-pointer"));}
	
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	public void load(){
		if(Vars.headless)return;
		arrowRegion = Core.atlas.find(NewHorizon.configName("jump-gate-arrow"));
		ammoInfo = Core.atlas.find(NewHorizon.configName("upgrade-info"));
		iconLevel = Core.atlas.find(NewHorizon.configName("level-up"));
		pointerRegion = Core.atlas.find(NewHorizon.configName("jump-gate-pointer"));
		
		for(UpgradeData data : NHUpgradeDatas.all){
			data.load();
			data.init();
		}
	}
}
