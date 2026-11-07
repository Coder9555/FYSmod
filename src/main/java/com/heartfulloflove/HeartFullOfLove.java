package com.heartfulloflove;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.state.BlockState;

import com.heartfulloflove.entity.LoveEntity;

public class HeartFullOfLove implements ModInitializer {

	public static final String MOD_ID = "heartfulloflove";

	// ===== НАСТРОЙКИ (20 тиков = 1 секунда) =====
	/** Как часто пытаемся создать entity: 1200 тиков = 60 секунд */
	public static final int SPAWN_INTERVAL_TICKS = 1200;
	/** Сколько живёт entity: 600 тиков = 30 секунд */
	public static final int LIFETIME_TICKS = 600;
	/** Расстояние от игрока в блоках: 16 блоков = 1 чанк */
	public static final int SPAWN_DISTANCE = 16;
	/** Сколько блоков воздуха должно быть над твёрдым блоком (минимум 2) */
	public static final int REQUIRED_AIR_BLOCKS = 2;

	// ===== РЕГИСТРАЦИЯ ТИПА МОБА =====
	public static final ResourceKey<EntityType<?>> LOVE_ENTITY_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "love_entity"));

	public static final EntityType<LoveEntity> LOVE_ENTITY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			LOVE_ENTITY_KEY,
			EntityType.Builder.of(LoveEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.8F)          // ширина и высота как у игрока
					.clientTrackingRange(10)
					.build(LOVE_ENTITY_KEY));

	private int tickCounter = 0;

	@Override
	public void onInitialize() {
		// Атрибуты моба (здоровье и т.д.)
		FabricDefaultAttributeRegistry.register(LOVE_ENTITY, LoveEntity.createAttributes());

		// Это событие вызывается 20 раз в секунду
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter < SPAWN_INTERVAL_TICKS) {
				return;
			}
			tickCounter = 0;

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isSpectator()) {
					continue;
				}
				if (player.level() instanceof ServerLevel level) {
					trySpawnNear(level, player);
				}
			}
		});
	}

	/** Пробуем 20 раз выбрать случайное место на расстоянии 16 блоков от игрока. */
	private static void trySpawnNear(ServerLevel level, ServerPlayer player) {
		RandomSource random = level.getRandom();

		for (int attempt = 0; attempt < 20; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			int x = Mth.floor(player.getX() + Math.cos(angle) * SPAWN_DISTANCE);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * SPAWN_DISTANCE);

			// Чанк должен быть загружен
			if (!level.hasChunkAt(new BlockPos(x, player.getBlockY(), z))) {
				continue;
			}

			BlockPos spawnPos = findSpawnPos(level, x, z, player.getBlockY());
			if (spawnPos != null) {
				LoveEntity entity = LOVE_ENTITY.spawn(level, spawnPos, EntitySpawnReason.EVENT);
				if (entity != null) {
					return; // успех
				}
			}
		}
	}

	/**
	 * Ищем в колонке (x, z) место: снизу твёрдый блок, на нём (и выше) воздух.
	 * Смотрим на 10 блоков вверх и вниз от высоты игрока.
	 * Возвращает позицию "ног" моба или null, если ничего не нашли.
	 */
	private static BlockPos findSpawnPos(ServerLevel level, int x, int z, int playerY) {
		for (int y = playerY + 10; y >= playerY - 10; y--) {
			BlockPos feet = new BlockPos(x, y, z);
			BlockPos ground = feet.below();

			// 1) Под ногами твёрдый блок (верхняя грань, на которой можно стоять)
			BlockState groundState = level.getBlockState(ground);
			if (!groundState.isFaceSturdy(level, ground, Direction.UP)) {
				continue;
			}

			// 2) Над твёрдым блоком достаточно воздуха
			boolean enoughAir = true;
			for (int i = 0; i < REQUIRED_AIR_BLOCKS; i++) {
				if (!level.getBlockState(feet.above(i)).isAir()) {
					enoughAir = false;
					break;
				}
			}
			if (enoughAir) {
				return feet;
			}
		}
		return null;
	}
}
