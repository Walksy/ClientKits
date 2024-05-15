package walksy.antipearldesync.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import walksy.antipearldesync.main.ILivingEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntity {


    @Shadow
    protected double serverX;

    @Shadow
    protected double serverY;

    @Shadow
    protected double serverZ;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public double getServerX() {
        return serverX;
    }

    @Override
    public double getServerY() {
        return serverY;
    }

    @Override
    public double getServerZ() {
        return serverZ;
    }
}
