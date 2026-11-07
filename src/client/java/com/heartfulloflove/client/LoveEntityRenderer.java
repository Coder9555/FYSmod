package com.heartfulloflove.client;

import com.heartfulloflove.entity.LoveEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.ResourceLocation;

public class LoveEntityRenderer extends LivingEntityRenderer<LoveEntity, AvatarRenderState, PlayerModel> {

	/** Стандартная текстура Alex, она уже есть внутри Minecraft */
	private static final ResourceLocation ALEX_TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");

	public LoveEntityRenderer(EntityRendererProvider.Context context) {
		// PLAYER_SLIM + true = модель с тонкими руками (Alex). Тень радиусом 0.5.
		super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
	}

	@Override
	public AvatarRenderState createRenderState() {
		return new AvatarRenderState();
	}

	@Override
	public ResourceLocation getTextureLocation(AvatarRenderState state) {
		return ALEX_TEXTURE;
	}
}
