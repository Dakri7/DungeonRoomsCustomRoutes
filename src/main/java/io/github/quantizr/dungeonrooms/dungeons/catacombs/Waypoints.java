/*
 * Dungeon Rooms Mod - Secret Waypoints for Hypixel Skyblock Dungeons
 * Copyright 2021 Quantizr(_risk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.quantizr.dungeonrooms.dungeons.catacombs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.quantizr.dungeonrooms.DungeonRooms;
import io.github.quantizr.dungeonrooms.events.PacketEvent;
import io.github.quantizr.dungeonrooms.utils.MapUtils;
import io.github.quantizr.dungeonrooms.utils.Utils;
import io.github.quantizr.dungeonrooms.utils.WaypointUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Waypoints {
    public static boolean enabled = true;

    public static boolean showEntrance = true;
    public static boolean showSuperboom = true;
    public static boolean showSecrets = true;
    public static boolean showFairySouls = true;
    public static boolean showStonk = true;
    public static boolean showRoute = true;	//TODO Add to options
    public static boolean sneakToDisable = true;

    public static boolean disableWhenAllFound = true;
    public static boolean allFound = false;

    public static boolean showWaypointText = true;
    public static boolean showBoundingBox = true;
    public static boolean showBeacon = true;

    public static boolean practiceModeOn = false;

    public static int secretNum = 0;
    public static int completedSecrets = 0;

    public static Map<String, List<Boolean>> allSecretsMap = new HashMap<>();
    public static List<Boolean> secretsList = new ArrayList<>(Arrays.asList(new Boolean[10]));

    static long lastSneakTime = 0;

    Frustum frustum = new Frustum();

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!enabled) return;
        if (practiceModeOn && !DungeonRooms.keyBindings[2].isKeyDown()) return;
        String roomName = RoomDetection.roomName;
        if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
        drawSecretWaypoints(event, roomName);
        drawRoutesWaypoints(event, roomName);
    }

	private void drawSecretWaypoints(RenderWorldLastEvent event, String roomName) {
		if (DungeonRooms.waypointsJson.get(roomName) != null) {
            JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
            int arraySize = secretsArray.size();
            for(int i = 0; i < arraySize; i++) {
                JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();

                boolean display = true;
                for(int j = 1; j <= secretNum; j++) {
                    if (!secretsList.get(j-1)) {
                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                            display = false;
                            break;
                        }
                    }
                }
                if (!display) continue;

                if (disableWhenAllFound && allFound && !secretsObject.get("category").getAsString().equals("fairysoul")) continue;

                BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);
                
                
                Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
                frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
                if (!frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, 255, pos.getZ() + 1)){
                    continue;
                }


                Color color;
                float alpha = 0.4f;
                switch (secretsObject.get("category").getAsString()) {
                    case "entrance":
                        if (!showEntrance) continue;
                        color = new Color(0, 255, 0);
                        break;
                    case "superboom":
                        if (!showSuperboom) continue;
                        color = new Color(255, 0, 0);
                        break;
                    case "chest":
                        if (!showSecrets) continue;
                        color = new Color(2, 213, 250);
                        break;
                    case "item":
                        if (!showSecrets) continue;
                        color = new Color(2, 64, 250);
                        break;
                    case "bat":
                        if (!showSecrets) continue;
                        color = new Color(142, 66, 0);
                        break;
                    case "wither":
                        if (!showSecrets) continue;
                        color = new Color(30, 30, 30);
                        break;
                    case "lever":
                        if (!showSecrets) continue;
                        color = new Color(250, 217, 2);
                        break;
                    case "fairysoul":
                        if (!showFairySouls) continue;
                        color = new Color(255, 85, 255);
                        break;
                    case "stonk":
                        if (!showStonk) continue;
                        color = new Color(146, 52, 235);
                        break;
                    default:
                        color = new Color(190, 255, 252);
                }

                double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
                double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
                double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

                double x = pos.getX() - viewerX;
                double y = pos.getY() - viewerY;
                double z = pos.getZ() - viewerZ;
                double distSq = x*x + y*y + z*z;

                GlStateManager.disableDepth();
                GlStateManager.disableCull();
                if (showBoundingBox && frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
                    WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, alpha);
                }
                GlStateManager.disableTexture2D();
                if (showBeacon && distSq > 5*5) WaypointUtils.renderBeaconBeam(x, y + 1, z, color.getRGB(), 0.25f, event.partialTicks);
                if (showWaypointText) WaypointUtils.renderWaypointText(secretsObject.get("secretName").getAsString(), pos.up(2), event.partialTicks);
                GlStateManager.disableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.enableCull();
            }
        }
	}
	
	private void drawRoutesWaypoints(RenderWorldLastEvent event, String roomName) {
		if (DungeonRooms.routesJson.get(roomName) != null) {
            JsonArray routesArray = DungeonRooms.routesJson.get(roomName).getAsJsonArray();
            int arraySize = routesArray.size();
            BlockPos prevPos = null;
            for(int i = 0; i < arraySize; i++) {
                JsonObject routesObject = routesArray.get(i).getAsJsonObject();

                if (disableWhenAllFound && allFound) continue;

                BlockPos relative = new BlockPos(routesObject.get("x").getAsInt(), routesObject.get("y").getAsInt(), routesObject.get("z").getAsInt());
                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);
                
                boolean draw = true;
                Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
                frustum.setPosition(viewer.posX, viewer.posY, viewer.posZ);
                if (!frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, 255, pos.getZ() + 1)){
                    draw = false;
                }

                if(draw) {
		            float alpha = 0.2f;
		            Color color = prevPos == null ? new Color(219, 172, 52): new Color(173, 216, 230);
		
		            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
		
		            double x = pos.getX() - viewerX;
		            double y = pos.getY() - viewerY;
		            double z = pos.getZ() - viewerZ;
		
		            GlStateManager.disableDepth();
		            GlStateManager.disableCull();
		            if (showBoundingBox && frustum.isBoxInFrustum(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
		                WaypointUtils.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, alpha);
		            }
		            GlStateManager.disableTexture2D();
		            GlStateManager.disableLighting();
		            GlStateManager.enableTexture2D();
		            GlStateManager.enableDepth();
		            GlStateManager.enableCull();
                }
                if(prevPos != null) {
                    drawLine(pos, prevPos, new Color(173, 216, 230), event.partialTicks);
                }
                prevPos = pos;
            }
        }
	}
    
    //Code stolen from DungeonGuide https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide
    public static void drawLine(BlockPos pos1, BlockPos pos2, Color colour, float partialTicks) {
        Entity render = Minecraft.getMinecraft().getRenderViewEntity();
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(2);
        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        worldRenderer.pos(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5).endVertex();
        worldRenderer.pos(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        // Action Bar
        if (event.type == 2) {
            String[] actionBarSections = event.message.getUnformattedText().split(" {3,}");

            for (String section : actionBarSections) {
                if (section.contains("Secrets") && section.contains("/")) {
                    String cleanedSection = StringUtils.stripControlCodes(section);
                    String[] splitSecrets = cleanedSection.split("/");

                    completedSecrets = Integer.parseInt(splitSecrets[0].replaceAll("[^0-9]", ""));
                    int totalSecrets = Integer.parseInt(splitSecrets[1].replaceAll("[^0-9]", ""));

                    allFound = (totalSecrets == secretNum && completedSecrets == secretNum);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
	        //Debug print coords:
	        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
	        BlockPos evntPos = MapUtils.actualToRelative(event.pos, RoomDetection.roomDirection, RoomDetection.roomCorner);
	        player.addChatMessage(new ChatComponentText("Coords of clicked Block: " + evntPos.getX() + " " + evntPos.getY() + " " + evntPos.getZ()));
	        String myString = String.format("{\n"
	        		+ "      \"secretName\":\"Start\",\n"
	        		+ "      \"category\":\"route\",\n"
	        		+ "      \"x\":%d,\n"
	        		+ "      \"y\":%d,\n"
	        		+ "      \"z\":%d\n"
	        		+ "    }", evntPos.getX(), evntPos.getY(), evntPos.getZ());
	        StringSelection stringSelection = new StringSelection(myString);
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        clipboard.setContents(stringSelection, null);
        }
        
        if (disableWhenAllFound && allFound) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.world.getBlockState(event.pos).getBlock();
            
            if (block != Blocks.chest && block != Blocks.skull) return;
            String roomName = RoomDetection.roomName;
            if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
            if (DungeonRooms.waypointsJson.get(roomName) != null) {
                JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                int arraySize = secretsArray.size();
                for(int i = 0; i < arraySize; i++) {
                    JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                    if (secretsObject.get("category").getAsString().equals("chest") || secretsObject.get("category").getAsString().equals("wither")) {
                        BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                        BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                        if (pos.equals(event.pos)) {
                            for(int j = 1; j <= secretNum; j++) {
                                if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                    Waypoints.secretsList.set(j-1, false);
                                    Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                    DungeonRooms.logger.info("DungeonRooms: Detected " + secretsObject.get("category").getAsString() + " click, turning off waypoint for secret #" + j);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inCatacombs || !enabled) return;
        if (disableWhenAllFound && allFound) return;
        Minecraft mc = Minecraft.getMinecraft();

        if (event.packet instanceof S0DPacketCollectItem) {
            S0DPacketCollectItem packet = (S0DPacketCollectItem) event.packet;
            Entity entity = mc.theWorld.getEntityByID(packet.getCollectedItemEntityID());
            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                entity = mc.theWorld.getEntityByID(packet.getEntityID());
                if (entity == null) return;
                String name = item.getEntityItem().getDisplayName();
                if (name.contains("Decoy") || name.contains("Defuse Kit") || name.contains("Dungeon Chest Key") ||
                        name.contains("Healing VIII") || name.contains("Inflatable Jerry") || name.contains("Spirit Leap") ||
                        name.contains("Training Weights") || name.contains("Trap") || name.contains("Treasure Talisman")) {
                    if (!entity.getCommandSenderEntity().getName().equals(mc.thePlayer.getName())) {
                        //Do nothing if someone else picks up the item in order to follow Hypixel rules
                        return;
                    }
                    String roomName = RoomDetection.roomName;
                    if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
                    if (DungeonRooms.waypointsJson.get(roomName) != null) {
                        JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                        int arraySize = secretsArray.size();
                        for(int i = 0; i < arraySize; i++) {
                            JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                            if (secretsObject.get("category").getAsString().equals("item") || secretsObject.get("category").getAsString().equals("bat")) {
                                BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                                BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                                if (entity.getDistanceSq(pos) <= 36D) {
                                    for(int j = 1; j <= secretNum; j++) {
                                        if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                            if (!Waypoints.secretsList.get(j-1)) continue;
                                            Waypoints.secretsList.set(j-1, false);
                                            Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                            DungeonRooms.logger.info("DungeonRooms: " + entity.getCommandSenderEntity().getName() + " picked up " +  StringUtils.stripControlCodes(name) + " from a "  + secretsObject.get("category").getAsString() + " secret, turning off waypoint for secret #" + j);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //Disable waypoint within 4 blocks away on sneak
    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (!Utils.inCatacombs || !enabled || !sneakToDisable) return;
        if(Keyboard.isKeyDown(Keyboard.KEY_J))
            DungeonRooms.loadJsons();
        
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (FMLClientHandler.instance().getClient().gameSettings.keyBindSneak.isPressed()) {
            if (System.currentTimeMillis() - lastSneakTime < 500) { //check for two taps in under half a second
                String roomName = RoomDetection.roomName;
                if (roomName.equals("undefined") || DungeonRooms.roomsJson.get(roomName) == null || secretsList == null) return;
                if (DungeonRooms.waypointsJson.get(roomName) != null) {
                    JsonArray secretsArray = DungeonRooms.waypointsJson.get(roomName).getAsJsonArray();
                    int arraySize = secretsArray.size();
                    for(int i = 0; i < arraySize; i++) {
                        JsonObject secretsObject = secretsArray.get(i).getAsJsonObject();
                        if (secretsObject.get("category").getAsString().equals("chest") || secretsObject.get("category").getAsString().equals("wither")
                                || secretsObject.get("category").getAsString().equals("item") || secretsObject.get("category").getAsString().equals("bat")) {
                            BlockPos relative = new BlockPos(secretsObject.get("x").getAsInt(), secretsObject.get("y").getAsInt(), secretsObject.get("z").getAsInt());
                            BlockPos pos = MapUtils.relativeToActual(relative, RoomDetection.roomDirection, RoomDetection.roomCorner);

                            if (player.getDistanceSq(pos) <= 16D) {
                                for(int j = 1; j <= secretNum; j++) {
                                    if (secretsObject.get("secretName").getAsString().substring(0,2).replaceAll("[\\D]", "").equals(String.valueOf(j))) {
                                        if (!Waypoints.secretsList.get(j-1)) continue;
                                        Waypoints.secretsList.set(j-1, false);
                                        Waypoints.allSecretsMap.replace(roomName, Waypoints.secretsList);
                                        DungeonRooms.logger.info("DungeonRooms: Player sneaked near " + secretsObject.get("category").getAsString() + " secret, turning off waypoint for secret #" + j);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            lastSneakTime = System.currentTimeMillis();
        }
    }
}
