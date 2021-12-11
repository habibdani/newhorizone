package newhorizon.expand.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.content.NHShaders;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.util.feature.ScreenHack;
import newhorizon.util.func.DrawFunc;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHSetting;


public class EventListeners{
	public static class BossGeneratedEvent{
		public final Unit unit;
		
		public BossGeneratedEvent(Unit unit){
			this.unit = unit;
		}
	}
	
	public static final Seq<Runnable> actAfterLoad = new Seq<>();
	public static Seq<Block> banned = new Seq<>();
	
	public static Interval timer = new Interval();
	
	private static String kickWarn;
	
	private static boolean caution = false;
	
	public static final ObjectMap<Class<?>, Seq<Cons2<? extends Building, Tile>>> onTapActor = new ObjectMap<>();
	
	public static <T extends Building> void addActor(Class<T> type, Cons2<T, Tile> act){
		Seq<Cons2<? extends Building, Tile>> actions = onTapActor.get(type);
		if(actions == null){
			actions = new Seq<>();
			actions.add(act);
			onTapActor.put(type, actions);
		}else actions.add(act);
	}
	
	
	public static void load(){
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.world.worldLoaded = true;
			
			NHVars.world.afterLoad();
			
			actAfterLoad.each(Runnable::run);
			actAfterLoad.clear();
		});
		
		Events.on(EventType.ResetEvent.class, e -> {
			actAfterLoad.clear();
			NHVars.reset();
			RemoteCoreStorage.clear();
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				NHVars.reset();
				
				actAfterLoad.clear();
				RemoteCoreStorage.clear();
				
				NHVars.world.worldLoaded = false;
			}
		});
		
		if(Vars.headless)return;
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(caution){
				caution = false;
				Vars.ui.showCustomConfirm("@warning", kickWarn, "@settings", "@confirm", () -> new NHSetting.SettingDialog().show(), () -> {});
				Vars.player.con.kick(kickWarn, 1);
			}
		});

		Events.run(EventType.Trigger.update, () -> {
			ScreenHack.update();
			NHSetting.update();
		});
		
		Events.on(ScreenHack.ScreenHackEvent.class, e -> {
			ScreenHack.generate(e.time);
		});
		
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			Draw.drawRange(Layer.light + 5, 1, () -> Vars.renderer.effectBuffer.begin(Color.clear), () -> {
				Vars.renderer.effectBuffer.end();
				Vars.renderer.effectBuffer.blit(NHShaders.gravityTrapShader);
			});
			
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			if(building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper)){
				Seq<GravityTrap.TrapField> bi = NHFunc.getObjects(NHVars.world.gravityTraps);
				
				Draw.z(Layer.overlayUI + 0.1f);
				for(GravityTrap.TrapField i : bi){
					GravityTrap.GravityTrapBuild b = i.build;
					Drawf.square(b.x, b.y, b.block.size * Vars.tilesize / 2f, b.team.color);
				}
				Draw.reset();
				
				Draw.blend(Blending.additive);
				Draw.z(Layer.light + 5);
				for(GravityTrap.TrapField i : bi){
					GravityTrap.GravityTrapBuild b = i.build;
					if(!b.active())continue;
					Draw.color(DrawFunc.markColor(b));
					Fill.poly(b.x, b.y, 6, b.range());
				}
				Draw.reset();
				Draw.blend();
			}
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			if(!NHSetting.getBool("@active.override") && e.host.name.equals(NewHorizon.SERVER_AUZ_NAME)){
				caution = true;
			}
		});
		
		Events.on(EventType.UnitChangeEvent.class, e -> e.unit.apply(NHStatusEffects.invincible, 180f));
		
		Events.on(EventType.TapEvent.class, e -> {
			Building selecting = Vars.control.input.frag.config.getSelectedTile();
			if(selecting != null)for(Class<?> type : onTapActor.keys()){
				if(type == selecting.getClass()){
					for(Cons2 actor : onTapActor.get(type)){
						actor.get(selecting, e.tile);
					}
				}
			}
		});
		
//		Events.on(EventType.ClientPreConnectEvent.class, e -> {
//			server = true;
//			if(Vars.headless)return;
//		});
//
//		Events.on(EventType.StateChangeEvent.class, e -> {
//			if(server){
//				server = false;
//				for(Block c : contents){
//					c.buildVisibility = BuildVisibility.sandboxOnly;
//				}
//			}
//			if(Vars.headless)return;
//		});
		
//		Events.on(EventType.StateChangeEvent.class, e -> {
//			NHSetting.log("Event", "Server Preload Run");
//
//			if(NHWorldVars.worldLoaded){
//				NHSetting.log("Event", "Leaving World");
//				NHWorldVars.worldLoaded= false;
//			}
//		});
	}
}