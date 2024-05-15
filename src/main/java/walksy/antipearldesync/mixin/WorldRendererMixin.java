package walksy.antipearldesync.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.antipearldesync.main.ILivingEntity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci)
    {
        if (entity instanceof PlayerEntity p && p != MinecraftClient.getInstance().player) {
            //if (p.hurtTime != 0 && p.getRecentDamageSource().isOf(DamageTypes.FALL)) {
                ci.cancel();
                float g = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
                this.entityRenderDispatcher.render(entity, ((ILivingEntity) entity).getServerX() - cameraX, ((ILivingEntity) entity).getServerY() - cameraY, ((ILivingEntity) entity).getServerZ() - cameraZ, g, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
            //}
        }
    }
}
