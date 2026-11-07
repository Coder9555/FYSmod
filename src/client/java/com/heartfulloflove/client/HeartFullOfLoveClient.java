package com.heartfulloflove.client;

import com.heartfulloflove.HeartFullOfLove;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class HeartFullOfLoveClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(HeartFullOfLove.LOVE_ENTITY, LoveEntityRenderer::new);
	}
}
