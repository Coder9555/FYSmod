package com.heartfulloflove.entity;

import com.heartfulloflove.HeartFullOfLove;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LoveEntity extends PathfinderMob {

	/** Здоровье как у Warden */
	public static final double MAX_HEALTH = 500.0;

	/** Сколько тиков моб уже прожил */
	private int lifeTicks = 0;

	public LoveEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
	}

	/** Характеристики моба */
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	/** Поведение: стоит на месте и смотрит на ближайшего игрока (до 32 блоков). */
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0F));
	}

	/** Вызывается каждый тик (20 раз в секунду). */
	@Override
	public void tick() {
		super.tick();

		// Считаем время жизни только на сервере
		if (this.level() instanceof ServerLevel) {
			this.lifeTicks++;
			if (this.lifeTicks >= HeartFullOfLove.LIFETIME_TICKS) {
				this.discard(); // исчезаем без дропа и эффектов
			}
		}
	}
}
