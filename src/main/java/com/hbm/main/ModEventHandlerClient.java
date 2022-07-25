package com.hbm.main;

import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockAshes;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.HTTPHandler;
import com.hbm.handler.HazmatRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.interfaces.IHoldableWeapon;
import com.hbm.interfaces.IItemHUD;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.gui.GUIArmorTable;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.JetpackBase;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.packet.AuxButtonPacket;
import com.hbm.packet.GunButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.HbmAnimations.Animation;
import com.hbm.render.block.ct.CTStitchReceiver;
import com.hbm.render.util.RenderAccessoryUtility;
import com.hbm.render.util.RenderOverhead;
import com.hbm.render.util.RenderScreenOverlay;
import com.hbm.render.util.SoyuzPronter;
import com.hbm.render.world.RenderNTMSkyboxChainloader;
import com.hbm.render.world.RenderNTMSkyboxImpact;
import com.hbm.sound.MovingSoundChopper;
import com.hbm.sound.MovingSoundChopperMine;
import com.hbm.sound.MovingSoundCrashing;
import com.hbm.sound.MovingSoundPlayerLoop;
import com.hbm.sound.MovingSoundXVL1456;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.tileentity.bomb.TileEntityNukeCustom.CustomNukeEntry;
import com.hbm.tileentity.bomb.TileEntityNukeCustom.EnumEntryType;
import com.hbm.tileentity.machine.TileEntityNukeFurnace;
import com.hbm.util.I18nUtil;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.LoggingUtil;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import api.hbm.item.IButtonReceiver;
import api.hbm.item.IClickReceiver;

import com.hbm.sound.MovingSoundPlayerLoop.EnumHbmSound;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class ModEventHandlerClient {
	
	@SubscribeEvent
	public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		/*if(event.type == ElementType.CROSSHAIRS) {
			int width = event.resolution.getScaledWidth();
			int height = event.resolution.getScaledHeight();
			Tessellator tess = Tessellator.instance;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
			GL11.glDepthMask(false);
			tess.startDrawingQuads();
			float brightness = (int)(System.currentTimeMillis() % 10000) / 10000F;
			tess.setColorRGBA_F(1F, 1F, 1F, (1 - brightness) * 0.8F);
			tess.addVertex(width, 0, 0);
			tess.addVertex(0, 0, 0);
			tess.addVertex(0, height, 0);
			tess.addVertex(width, height, 0);
			tess.draw();
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glDepthMask(true);
			return;
		}*/
		
		/// HANDLE GUN OVERLAYS ///
		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IItemHUD) {
			((IItemHUD)player.getHeldItem().getItem()).renderHUD(event, event.type, player, player.getHeldItem());
		}
		
		/// HANDLE GEIGER COUNTER HUD ///
		if(event.type == ElementType.HOTBAR) {
			
			if(!(ArmorFSB.hasFSBArmor(player) && ((ArmorFSB)player.inventory.armorInventory[2].getItem()).customGeiger)) {
				
				if(player.inventory.hasItem(ModItems.geiger_counter)) {
	
					float rads = HbmLivingProps.getRadiation(player);
					
					RenderScreenOverlay.renderRadCounter(event.resolution, rads, Minecraft.getMinecraft().ingameGUI);
				}
			}
		}

		/// DODD DIAG HOOK FOR RBMK
		if(event.type == ElementType.CROSSHAIRS) {
			Minecraft mc = Minecraft.getMinecraft();
			World world = mc.theWorld;
			MovingObjectPosition mop = mc.objectMouseOver;
			
			if(mop != null && mop.typeOfHit == mop.typeOfHit.BLOCK && world.getBlock(mop.blockX, mop.blockY, mop.blockZ) instanceof ILookOverlay) {
				((ILookOverlay) world.getBlock(mop.blockX, mop.blockY, mop.blockZ)).printHook(event, world, mop.blockX, mop.blockY, mop.blockZ);
			}
			
			/*if(mop != null && mop.typeOfHit == mop.typeOfHit.BLOCK) {
				ScaledResolution resolution = event.resolution;
				GL11.glPushMatrix();
				int pX = resolution.getScaledWidth() / 2 + 8;
				int pZ = resolution.getScaledHeight() / 2;
				mc.fontRenderer.drawString("META: " + world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ), pX, pZ - 3, 0xffff00);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glColor3f(1F, 1F, 1F);
				GL11.glPopMatrix();
				Minecraft.getMinecraft().renderEngine.bindTexture(Gui.icons);
			}*/
		}
		
		/// HANLDE ANIMATION BUSES ///
		
		for(int i = 0; i < HbmAnimations.hotbar.length; i++) {
			
			Animation animation = HbmAnimations.hotbar[i];
			
			if(animation == null)
				continue;
			
			long time = System.currentTimeMillis() - animation.startMillis;
			
			if(time > animation.animation.getDuration())
				HbmAnimations.hotbar[i] = null;
		}
			
		if(!ducked && Keyboard.isKeyDown(Keyboard.KEY_O)) {
			
			ducked = true;
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(0, 0, 0, 999, 0));
		}
		
		/// HANDLE FSB HUD ///
		ItemStack helmet = player.inventory.armorInventory[3];
		
		if(helmet != null && helmet.getItem() instanceof ArmorFSB) {
			((ArmorFSB)helmet.getItem()).handleOverlay(event, player);
		}
		
		/// HANDLE ELECTRIC FSB HUD ///
		
		if(!event.isCanceled() && event.type == event.type.ARMOR) {
			
	        int width = event.resolution.getScaledWidth();
	        int height = event.resolution.getScaledHeight();
	        int left = width / 2 - 91;
	        int top = height - GuiIngameForge.left_height - 3;
	        
			Tessellator tess = Tessellator.instance;
			
			if(ArmorFSB.hasFSBArmorIgnoreCharge(player)) {
				ArmorFSB chestplate = (ArmorFSB)player.inventory.armorInventory[2].getItem();
				boolean noHelmet = chestplate.noHelmet;
		        
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				tess.startDrawingQuads();
	
				for(int i = 0; i < (noHelmet ? 3 : 4); i++) {
					
					ItemStack stack = player.inventory.armorInventory[i];
					
					if(!(stack != null && stack.getItem() instanceof ArmorFSBPowered))
						break;
					
					float tot = 1F - (float) ((ArmorFSBPowered)stack.getItem()).getDurabilityForDisplay(stack);
					
					tess.setColorOpaque_F(0.25F, 0.25F, 0.25F);
					tess.addVertex(left - 0.5, top - 0.5, 0);
					tess.addVertex(left - 0.5, top + 1.5, 0);
					tess.addVertex(left + 81.5, top + 1.5, 0);
					tess.addVertex(left + 81.5, top - 0.5, 0);
					
					tess.setColorOpaque_F(1F - tot, tot, 0F);
					tess.addVertex(left, top, 0);
					tess.addVertex(left, top + 1, 0);
					tess.addVertex(left + 81 * tot, top + 1, 0);
					tess.addVertex(left + 81 * tot, top, 0);
					
					top -= 2.5;
				}
				
				tess.draw();
				
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
			} else if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof JetpackBase) {
				
				ItemStack stack = player.inventory.armorInventory[2];
				
				float tot = (float) ((JetpackBase)stack.getItem()).getFuel(stack) / (float) ((JetpackBase)stack.getItem()).getMaxFill(stack);
				top -= 3;
				
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				tess.startDrawingQuads();
				tess.setColorOpaque_F(0.25F, 0.25F, 0.25F);
				tess.addVertex(left - 0.5, top - 0.5, 0);
				tess.addVertex(left - 0.5, top + 4.5, 0);
				tess.addVertex(left + 81.5, top + 4.5, 0);
				tess.addVertex(left + 81.5, top - 0.5, 0);
				
				tess.setColorOpaque_F(1F - tot, tot, 0F);
				tess.addVertex(left, top, 0);
				tess.addVertex(left, top + 4, 0);
				tess.addVertex(left + 81 * tot, top + 4, 0);
				tess.addVertex(left + 81 * tot, top, 0);
				tess.draw();
				
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
			}
			
		}
		if(!event.isCanceled() && event.type == event.type.HOTBAR) {
			
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.getDashCount() > 0) {
				RenderScreenOverlay.renderDashBar(event.resolution, Minecraft.getMinecraft().ingameGUI, props);
					
			}
		}
	}
	
	public static boolean ducked = false;
	
	@SubscribeEvent
	public void preRenderEvent(RenderPlayerEvent.Pre event) {
		
		RenderPlayer renderer = event.renderer;
		AbstractClientPlayer player = (AbstractClientPlayer)event.entityPlayer;
		
		PotionEffect invis = player.getActivePotionEffect(Potion.invisibility);
		
		if(invis != null && invis.getAmplifier() > 0)
			event.setCanceled(true);

		if(player.getDisplayName().toLowerCase().equals("martmn")) {
			
			event.setCanceled(true);
			
			float pX = (float) (player.prevPosX + (player.posX - player.prevPosX) * (double)event.partialRenderTick);
			float pY = (float) (player.prevPosY + (player.posY - player.prevPosY) * (double)event.partialRenderTick);
			float pZ = (float) (player.prevPosZ + (player.posZ - player.prevPosZ) * (double)event.partialRenderTick);
			EntityPlayer me = Minecraft.getMinecraft().thePlayer;
			float mX = (float) (me.prevPosX + (me.posX - me.prevPosX) * (double)event.partialRenderTick);
			float mY = (float) (me.prevPosY + (me.posY - me.prevPosY) * (double)event.partialRenderTick);
			float mZ = (float) (me.prevPosZ + (me.posZ - me.prevPosZ) * (double)event.partialRenderTick);

			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/particle/fart.png"));
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glTranslatef(pX - mX, pY - mY + 0.75F - (float)player.getYOffset(), pZ - mZ);
			GL11.glRotatef(-me.rotationYaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(me.rotationPitch, 1.0F, 0.0F, 0.0F);
			Tessellator t = Tessellator.instance;
			t.startDrawingQuads();
			t.setBrightness(240);
			t.addVertexWithUV(-1, 1, 0, 0, 0);
			t.addVertexWithUV(1, 1, 0, 1, 0);
			t.addVertexWithUV(1, -1, 0, 1, 1);
			t.addVertexWithUV(-1, -1, 0, 0, 1);
			t.draw();
			
			GL11.glEnable(GL11.GL_LIGHTING);
			
			GL11.glPopMatrix();
		}
		
		ResourceLocation cloak = RenderAccessoryUtility.getCloakFromPlayer(player);
		
		if(cloak != null)
			player.func_152121_a(Type.CAPE, cloak);
		
		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IHoldableWeapon) {
			renderer.modelBipedMain.aimedBow = true;
			renderer.modelArmor.aimedBow = true;
			renderer.modelArmorChestplate.aimedBow = true;
		}
	}
	
	@SubscribeEvent
	public void onRenderArmorEvent(RenderPlayerEvent.SetArmorModel event) {
		
		EntityPlayer player = event.entityPlayer;
		
		for(int i = 0; i < 4; i++) {
			
			ItemStack armor = player.getCurrentArmor(i);
			
			if(armor != null && ArmorModHandler.hasMods(armor)) {
				
				for(ItemStack mod : ArmorModHandler.pryMods(armor)) {
					
					if(mod != null && mod.getItem() instanceof ItemArmorMod) {
						((ItemArmorMod)mod.getItem()).modRender(event, armor);
					}
				}
			}
			
			//because armor that isn't ItemArmor doesn't render at all
			if(armor != null && armor.getItem() instanceof JetpackBase) {
				((ItemArmorMod)armor.getItem()).modRender(event, armor);
			}
		}
		
		if(player.getCurrentArmor(2) == null && !player.isPotionActive(Potion.invisibility)) {
			if(player.getUniqueID().toString().equals(Library.SolsticeUnlimitd) || player.getDisplayName().equals("SolsticeUnlimitd"))
				RenderAccessoryUtility.renderSol(event);
			if(player.getUniqueID().toString().equals(Library.HbMinecraft) || player.getDisplayName().equals("HbMinecraft"))
				RenderAccessoryUtility.renderWings(event, 2);
			if(player.getUniqueID().toString().equals(Library.the_NCR) || player.getDisplayName().equals("the_NCR"))
				RenderAccessoryUtility.renderWings(event, 3);
		}
	}

	@SubscribeEvent
	public void clickHandler(MouseEvent event) {
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		if(player.getHeldItem() != null) {
			
			Item held = player.getHeldItem().getItem();
			
			if(held instanceof IClickReceiver) {
				IClickReceiver rec = (IClickReceiver) held;
				
				if(rec.handleMouseInput(player.getHeldItem(), player, event.button, event.buttonstate)) {
					event.setCanceled(true);
					return;
				}
			}
			
			if(held instanceof ItemGunBase) {
				
				if(event.button == 0)
					event.setCanceled(true);
				
				ItemGunBase item = (ItemGunBase)player.getHeldItem().getItem();
				
				if(event.button == 0 && !item.m1 && !item.m2) {
					item.m1 = true;
					PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 0));
					item.startActionClient(player.getHeldItem(), player.worldObj, player, true);
				}
				else if(event.button == 1 && !item.m2 && !item.m1) {
					item.m2 = true;
					PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 1));
					item.startActionClient(player.getHeldItem(), player.worldObj, player, false);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void keyEvent(KeyInputEvent event) {
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		if(player.getHeldItem() != null) {
			
			Item held = player.getHeldItem().getItem();
			
			if(held instanceof IButtonReceiver) {
				IButtonReceiver rec = (IButtonReceiver) held;
				rec.handleKeyboardInput(player.getHeldItem(), player);
			}
		}
	}

	@Spaghetti("please get this shit out of my face")
	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent17 e) {
		
		EntityPlayer player = MainRegistry.proxy.me();
		Minecraft mc = Minecraft.getMinecraft();
		
		if(player != null && mc.theWorld != null) {
			int i = MathHelper.floor_double(player.posX);
			int j = MathHelper.floor_double(player.posY);
			int k = MathHelper.floor_double(player.posZ);
			Block block = mc.theWorld.getBlock(i, j, k);
			
			if(block == ModBlocks.vacuum) {
				e.result = null;
				return;
			}
		}
		
		ResourceLocation r = e.sound.getPositionedSoundLocation();

		WorldClient wc = mc.theWorld;
		
		//Alright, alright, I give the fuck up, you've wasted my time enough with this bullshit. You win.
		//A winner is you.
		//Conglaturations.
		//Fuck you.

		if(r.toString().equals("hbm:misc.nullTau") && Library.getClosestPlayerForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2) != null)
		{
			EntityPlayer ent = Library.getClosestPlayerForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2);
			
			if(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop) == null) {
				MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundXVL1456(new ResourceLocation("hbm:weapon.tauChargeLoop2"), ent, EnumHbmSound.soundTauLoop));
				MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).setPitch(0.5F);
			} else {
				if(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).getPitch() < 1.5F)
				MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).setPitch(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).getPitch() + 0.01F);
			}
		}
		
		if(r.toString().equals("hbm:misc.nullChopper") && Library.getClosestChopperForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2) != null)
		{
			EntityHunterChopper ent = Library.getClosestChopperForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2);
			
			if(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundChopperLoop) == null) {
				MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundChopper(new ResourceLocation("hbm:entity.chopperFlyingLoop"), ent, EnumHbmSound.soundChopperLoop));
				MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundChopperLoop).setVolume(10.0F);
			}
		}
		
		if(r.toString().equals("hbm:misc.nullCrashing") && Library.getClosestChopperForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2) != null)
		{
			EntityHunterChopper ent = Library.getClosestChopperForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2);
			
			if(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundCrashingLoop) == null) {
				MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundCrashing(new ResourceLocation("hbm:entity.chopperCrashingLoop"), ent, EnumHbmSound.soundCrashingLoop));
				MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundCrashingLoop).setVolume(10.0F);
			}
		}
		
		if(r.toString().equals("hbm:misc.nullMine") && Library.getClosestMineForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2) != null)
		{
			EntityChopperMine ent = Library.getClosestMineForSound(wc, e.sound.getXPosF(), e.sound.getYPosF(), e.sound.getZPosF(), 2);
			
			if(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundMineLoop) == null) {
				MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundChopperMine(new ResourceLocation("hbm:entity.chopperMineLoop"), ent, EnumHbmSound.soundMineLoop));
				MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundMineLoop).setVolume(10.0F);
			}
		}

		for(MovingSoundPlayerLoop sounds : MovingSoundPlayerLoop.globalSoundList)
		{
			if(!sounds.init || sounds.isDonePlaying()) {
				sounds.init = true;
				sounds.setDone(false);
				mc.getSoundHandler().playSound(sounds);
			}
		}
	}
	
	@SubscribeEvent
	public void drawTooltip(ItemTooltipEvent event) {
		
		ItemStack stack = event.itemStack;
		List<String> list = event.toolTip;
		
		/// HAZMAT INFO ///
		List<HazardClass> hazInfo = ArmorRegistry.hazardClasses.get(stack.getItem());
		
		if(hazInfo != null) {
			
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				list.add(EnumChatFormatting.GOLD + I18nUtil.resolveKey("hazard.prot"));
				for(HazardClass clazz : hazInfo) {
					list.add(EnumChatFormatting.YELLOW + "  " + I18nUtil.resolveKey(clazz.lang));
				}
			} else {
				
				list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC +"Hold <" +
						EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + "LSHIFT" +
						EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "> to display protection info");
			}
		}
		
		/// CLADDING (LEGACY) ///
		double rad = HazmatRegistry.getResistance(stack);
		rad = ((int)(rad * 1000)) / 1000D;
		if(rad > 0) list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("trait.radResistance", rad));
		
		/// ARMOR MODS ///
		if(stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
			
			if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !(Minecraft.getMinecraft().currentScreen instanceof GUIArmorTable)) {
				
				list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC +"Hold <" +
						EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + "LSHIFT" +
						EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "> to display installed armor mods");
				
			} else {
				
				list.add(EnumChatFormatting.YELLOW + "Mods:");
				
				ItemStack[] mods = ArmorModHandler.pryMods(stack);
				
				for(int i = 0; i < 8; i++) {
					
					if(mods[i] != null && mods[i].getItem() instanceof ItemArmorMod) {
						
						((ItemArmorMod)mods[i].getItem()).addDesc(list, mods[i], stack);
					}
				}
			}
		}
		
		/// HAZARDS ///
		HazardSystem.addFullTooltip(stack, event.entityPlayer, list);
		
		if(event.showAdvancedItemTooltips) {
			List<String> names = ItemStackUtil.getOreDictNames(stack);
			
			if(names.size() > 0) {
				list.add(EnumChatFormatting.BLUE + "Ore Dict:");
				for(String s : names) {
					list.add(EnumChatFormatting.AQUA + " -" + s);
				}
			} else {
				list.add(EnumChatFormatting.RED + "No Ore Dict data!");
			}
		}
		
		/// NUCLEAR FURNACE FUELS ///
		int breeder = TileEntityNukeFurnace.getFuelValue(stack);
		
		if(breeder != 0) {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("trait.furnace", breeder));
		}
		
		/// CUSTOM NUKE ///
		ComparableStack comp = new ComparableStack(stack).makeSingular();
		CustomNukeEntry entry = TileEntityNukeCustom.entries.get(comp);
		
		if(entry != null) {
			
			if(!list.isEmpty())
				list.add("");
			
			if(entry.entry == EnumEntryType.ADD)
				list.add(EnumChatFormatting.GOLD + "Adds " + entry.value + " to the custom nuke stage " + entry.type);

			if(entry.entry == EnumEntryType.MULT)
				list.add(EnumChatFormatting.GOLD + "Adds multiplier " + entry.value + " to the custom nuke stage " + entry.type);
		}
	}
	
	private ResourceLocation ashes = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_ash.png");
	
	@SideOnly(Side.CLIENT)
	//@SubscribeEvent
	public void onRenderStorm(RenderHandEvent event) {
		
		if(BlockAshes.ashes == 0)
			return;
		
		GL11.glPushMatrix();
		
		Minecraft mc = Minecraft.getMinecraft();

		GL11.glRotatef((float)-mc.thePlayer.rotationYaw, 0, 1, 0);
		GL11.glRotatef((float)(mc.thePlayer.rotationPitch), 1, 0, 0);
		
		ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		
		int w = resolution.getScaledWidth();
		int h = resolution.getScaledHeight();
		double off = System.currentTimeMillis() / -10000D % 10000D;
		double aw = 25;
		
		Tessellator tessellator = Tessellator.instance;

		//int d = mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posY), MathHelper.floor_double(mc.thePlayer.posZ), 0);
		int cX = currentBrightness % 65536;
		int cY = currentBrightness / 65536;
		int lX = lastBrightness % 65536;
		int lY = lastBrightness / 65536;
		float interp = (mc.theWorld.getTotalWorldTime() % 20) * 0.05F;
		
		if(mc.theWorld.getTotalWorldTime() == 1)
			lastBrightness = currentBrightness;
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(lX + (cX - lX) * interp) / 1.0F, (float)(lY + (cY - lY) * interp) / 1.0F);

		mc.entityRenderer.enableLightmap((double)event.partialTicks);
		
		mc.getTextureManager().bindTexture(ashes);
		
		for(int i = 1; i < 3; i++) {
			
			GL11.glRotatef(-15, 0, 0, 1);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, BlockAshes.ashes / 256F * 0.98F / i);
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-w * 0.25, 	h * 0.25, 	aw, 0.0D + off * i, 1.0D);
			tessellator.addVertexWithUV(w * 0.25, 	h * 0.25, 	aw, 1.0D + off * i, 1.0D);
			tessellator.addVertexWithUV(w * 0.25, 	-h * 0.25, 	aw, 1.0D + off * i, 0.0D);
			tessellator.addVertexWithUV(-w * 0.25, 	-h * 0.25, 	aw, 0.0D + off * i, 0.0D);
			tessellator.draw();
		}

		mc.entityRenderer.disableLightmap((double)event.partialTicks);
		
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopMatrix();
	}

	public static int currentBrightness = 0;
	public static int lastBrightness = 0;
	
	@SubscribeEvent
	public void clentTick(ClientTickEvent event) {
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.gameSettings.renderDistanceChunks > 16 && GeneralConfig.enableRenderDistCheck && ! FMLClientHandler.instance().hasOptifine()) {
			mc.gameSettings.renderDistanceChunks = 16;
			LoggingUtil.errorWithHighlight("========================== WARNING ==========================");
			LoggingUtil.errorWithHighlight("Dangerous render distance detected: Values over 16 only work on 1.8+ or with Optifine installed!!");
			LoggingUtil.errorWithHighlight("Set '1.25_enableRenderDistCheck' in hbm.cfg to 'false' to disable this check.");
			LoggingUtil.errorWithHighlight("========================== WARNING ==========================");
			LoggingUtil.errorWithHighlight("If you got this error after removing Optifine: Consider deleting your option files after removing mods.");
			LoggingUtil.errorWithHighlight("If you got this error after downgrading your Minecraft version: Consider using a launcher that doesn't reuse the same folders for every game instance. MultiMC for example, it's really good and it comes with a dedicated cat button. You like cats, right? Are you using the Microsoft launcher? The one launcher that turns every version switch into a tightrope act because all the old config and options files are still here because different instances all use the same folder structure instead of different folders like a competent launcher would, because some MO-RON thought that this was an acceptable way of doing things? Really? The launcher that circumcises every crashlog into indecipherable garbage, tricking oblivious people into posting that as a \"crash report\", effectively wasting everyone's time? The launcher made by the company that thought it would be HI-LA-RI-OUS to force everyone to use Microsoft accounts, effectively breaking every other launcher until they implement their terrible auth system?");
			LoggingUtil.errorWithHighlight("========================== WARNING ==========================");
		}
		
		if(mc.theWorld == null || mc.thePlayer == null)
			return;
		
		if(event.phase == Phase.START && event.side == Side.CLIENT) {
			
			if(BlockAshes.ashes > 256) BlockAshes.ashes = 256;
			if(BlockAshes.ashes > 0) BlockAshes.ashes -= 2;
			if(BlockAshes.ashes < 0) BlockAshes.ashes = 0;
			
			if(mc.theWorld.getTotalWorldTime() % 20 == 0) {
				this.lastBrightness = this.currentBrightness;
				currentBrightness = mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posY), MathHelper.floor_double(mc.thePlayer.posZ), 0);
			}
			
			if(ArmorUtil.isWearingEmptyMask(mc.thePlayer)) {
				MainRegistry.proxy.displayTooltip(EnumChatFormatting.RED + "Your mask has no filter!", MainRegistry.proxy.ID_FILTER);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientTickLast(ClientTickEvent event) {
		
		if(event.phase == Phase.START) {
			
			World world = Minecraft.getMinecraft().theWorld;
			
			if(world != null && world.provider instanceof WorldProviderSurface) {
				
				IRenderHandler sky = world.provider.getSkyRenderer();
				
				if(ModEventHandler.dust > 0 || ModEventHandler.fire > 0) {

					if(!(sky instanceof RenderNTMSkyboxImpact)) {
						world.provider.setSkyRenderer(new RenderNTMSkyboxImpact());
					}
				} else {

					if(!(sky instanceof RenderNTMSkyboxChainloader)) {
						world.provider.setSkyRenderer(new RenderNTMSkyboxChainloader(sky));
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event) {

		GL11.glPushMatrix();

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		double dx = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
		double dy = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
		double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

		int dist = 300;
		int x = 0;
		int y = 500;
		int z = 0;

		Vec3 vec = Vec3.createVectorHelper(x - dx, y - dy, z - dz);

		if(player.worldObj.provider.dimensionId == 0 && vec.lengthVector() < dist && !HTTPHandler.capsule.isEmpty()) {

			GL11.glTranslated(vec.xCoord, vec.yCoord, vec.zCoord);

			GL11.glPushMatrix();

			RenderHelper.enableStandardItemLighting();

			GL11.glRotated(80, 0, 0, 1);
			GL11.glRotated(30, 0, 1, 0);

			double sine = Math.sin(System.currentTimeMillis() * 0.0005) * 5;
			double sin3 = Math.sin(System.currentTimeMillis() * 0.0005 + Math.PI * 0.5) * 5;
			GL11.glRotated(sine, 0, 0, 1);
			GL11.glRotated(sin3, 1, 0, 0);

			GL11.glTranslated(0, -3, 0);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 6500F, 30F);
			SoyuzPronter.prontCapsule();

			GL11.glRotated(System.currentTimeMillis() * 0.025 % 360, 0, -1, 0);

			int rand = new Random(MainRegistry.startupTime).nextInt(HTTPHandler.capsule.size());
			String msg = HTTPHandler.capsule.get(rand);

			GL11.glTranslated(0, 3.75, 0);
			GL11.glRotated(180, 1, 0, 0);

			float rot = 0F;

			// looks dumb but we'll use this technology for the cyclotron
			for(char c : msg.toCharArray()) {

				GL11.glPushMatrix();

				GL11.glRotatef(rot, 0, 1, 0);

				float width = Minecraft.getMinecraft().fontRenderer.getStringWidth(msg);
				float scale = 5 / width;

				rot -= Minecraft.getMinecraft().fontRenderer.getCharWidth(c) * scale * 50;

				GL11.glTranslated(2, 0, 0);

				GL11.glRotatef(-90, 0, 1, 0);
				GL11.glScalef(scale, scale, scale);
				GL11.glDisable(GL11.GL_CULL_FACE);
				Minecraft.getMinecraft().fontRenderer.drawString(String.valueOf(c), 0, 0, 0xff00ff);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glPopMatrix();
			}
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			RenderHelper.disableStandardItemLighting();

			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
		
		RenderOverhead.renderMarkers(event.partialTicks);

		if(ArmorFSB.hasFSBArmor(player)) {
			ItemStack plate = player.inventory.armorInventory[2];
			ArmorFSB chestplate = (ArmorFSB) plate.getItem();

			if(chestplate.thermal && HbmPlayerProps.getData(player).enableHUD)
				RenderOverhead.renderThermalSight(event.partialTicks);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void preRenderEventFirst(RenderLivingEvent.Pre event) {
		
		if(MainRegistry.proxy.isVanished(event.entity))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void preRenderEvent(RenderLivingEvent.Pre event) {
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		if(ArmorFSB.hasFSBArmor(player) && HbmPlayerProps.getData(player).enableHUD) {
			ItemStack plate = player.inventory.armorInventory[2];
			ArmorFSB chestplate = (ArmorFSB)plate.getItem();
			
			if(chestplate.vats) {
				
				int count = (int)Math.min(event.entity.getMaxHealth(), 100);
				
				int bars = (int)Math.ceil(event.entity.getHealth() * count / event.entity.getMaxHealth());
				
				String bar = EnumChatFormatting.RED + "";
				
				for(int i = 0; i < count; i++) {
					
					if(i == bars)
						bar += EnumChatFormatting.RESET + "";
					
						bar += "|";
				}
				RenderOverhead.renderTag(event.entity, event.x, event.y, event.z, event.renderer, bar, chestplate.thermal);
			}
		}
	}
	
	/*@SubscribeEvent
	public void setupFog(RenderFogEvent event) {
		event.setResult(Result.DENY);
	}
	
	@SubscribeEvent
	public void thickenFog(FogDensity event) {
		event.density = 0.05F;
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void tintFog(FogColors event) {
		event.red = 0.5F;
		event.green = 0.0F;
		event.blue = 0.0F;
	}*/

	public static IIcon particleBase;
	public static IIcon particleLeaf;

	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		
		if(event.map.getTextureType() == 0) {
			particleBase = event.map.registerIcon(RefStrings.MODID + ":particle/particle_base");
			particleLeaf = event.map.registerIcon(RefStrings.MODID + ":particle/dead_leaf");
		}
	}

	@SubscribeEvent
	public void postTextureStitch(TextureStitchEvent.Post event) {
		CTStitchReceiver.receivers.forEach(x -> x.postStitch());
	}

	private static final ResourceLocation poster = new ResourceLocation(RefStrings.MODID + ":textures/models/misc/poster.png");
	private static final ResourceLocation poster_cat = new ResourceLocation(RefStrings.MODID + ":textures/models/misc/poster_cat.png");
	
	@SubscribeEvent
	public void renderFrame(RenderItemInFrameEvent event) {
		
		if(event.item != null && event.item.getItem() == ModItems.flame_pony) {
			event.setCanceled(true);
			
			double p = 0.0625D;
			double o = p * 2.75D;
			
			GL11.glDisable(GL11.GL_LIGHTING);
			Minecraft.getMinecraft().renderEngine.bindTexture(poster);
			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			tess.addVertexWithUV(0.5, 0.5 + o, p * 0.5, 1, 0);
			tess.addVertexWithUV(-0.5, 0.5 + o, p * 0.5, 0, 0);
			tess.addVertexWithUV(-0.5, -0.5 + o, p * 0.5, 0, 1);
			tess.addVertexWithUV(0.5, -0.5 + o, p * 0.5, 1, 1);
			tess.draw();
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		
		if(event.item != null && event.item.getItem() == Items.paper) {
			event.setCanceled(true);
			
			double p = 0.0625D;
			double o = p * 2.75D;

			GL11.glDisable(GL11.GL_LIGHTING);
			Minecraft.getMinecraft().renderEngine.bindTexture(poster_cat);
			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			tess.addVertexWithUV(0.5, 0.5 + o, p * 0.5, 1, 0);
			tess.addVertexWithUV(-0.5, 0.5 + o, p * 0.5, 0, 0);
			tess.addVertexWithUV(-0.5, -0.5 + o, p * 0.5, 0, 1);
			tess.addVertexWithUV(0.5, -0.5 + o, p * 0.5, 1, 1);
			tess.draw();
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}
}
