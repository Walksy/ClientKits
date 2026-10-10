package walksy.clientkits.screen;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.gui.style.Theme;
import main.walksy.lib.core.gui.style.Type;
import main.walksy.lib.core.gui.WLScreen;
import main.walksy.lib.core.gui.widget.WLWidget;
import main.walksy.lib.core.gui.widget.options.control.LiquidKnob;
import main.walksy.lib.core.render.WLGraphics2D;
import main.walksy.lib.core.render.WLRenderSystem;
import main.walksy.lib.core.render.font.Fonts;
import main.walksy.lib.core.render.font.msdf.MsdfFont;
import main.walksy.lib.core.render.glass.api.GlassStyle;
import main.walksy.lib.core.render.glass.api.LiquidGlassConstants;
import main.walksy.lib.core.utils.ColorUtils;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.concurrent.atomic.AtomicLong;

public final class PopoverButton extends WLWidget {
    private static final AtomicLong FADE_KEYS = new AtomicLong(0xC1E7A5700L);

    private static final float PAD_X = 9f;
    private static final float PAD_Y = 5f;
    private static final float LABEL_LINE = 12f;
    private static final float CORNER_RADIUS = 9f;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final float LABEL_SIZE = Type.SUBHEADLINE.size();
    private static final float CAP_HEIGHT_RATIO = 0.59043f;

    private static final float HOVER_SPEED = 14f;
    private static final float PRESS_IN_SPEED = 22f;
    private static final float PRESS_OUT_SPEED = 14f;
    private static final float PRESS_GROW = 0.05f;

    private final String label;
    private final int panelLayer;
    private final Runnable onPress;
    private final long fadeKey = FADE_KEYS.incrementAndGet();
    private float uiScale = 1f;
    private float hoverAnim;
    private float pressAnim;
    private boolean armed;

    public PopoverButton(final String label, final int panelLayer, final Runnable onPress) {
        super(0, 0, 1, 1);
        this.label = label == null ? "" : label;
        this.panelLayer = panelLayer;
        this.onPress = onPress;
    }

    private GlassStyle panelStyle() {
        return LiquidKnob.bevelKnobStyle(this.panelLayer)
            .blurRadius(LiquidGlassConstants.BLUR_POPOVER)
            .tint(Theme.get().menuTint(), Theme.get().menuTintAlpha());
    }

    private static MsdfFont labelFont() {
        return Fonts.SFPRO_DISPLAY;
    }

    public GlassStyle style() {
        return this.panelStyle().fadeKey(this.fadeKey).scaled(WLScreen.uiScaleFactor());
    }

    public void layout(final int x, final int y, final float uiScale) {
        this.layout(x, y, uiScale, Math.round(widthFor(this.label, uiScale)));
    }

    public void layout(final int x, final int y, final float uiScale, final int width) {
        this.uiScale = uiScale;
        this.setX(x);
        this.setY(y);
        this.setWidth(Math.max(1, width));
        this.setHeight(Math.max(1, Math.round(heightFor(uiScale))));
    }

    public static float widthFor(final String label, final float uiScale) {
        return PAD_X * 2f * uiScale + labelFont().width(label, LABEL_SIZE * uiScale);
    }

    public static float heightFor(final float uiScale) {
        return (PAD_Y * 2f + LABEL_LINE) * uiScale;
    }

    public float cornerRadius() {
        return Math.min(CORNER_RADIUS * this.uiScale, 0.5f * Math.min(this.getWidth(), this.getHeight()));
    }

    public boolean isArmed() {
        return this.armed;
    }

    public void settleInteraction() {
        this.hoverAnim = 0f;
        this.pressAnim = 0f;
        this.armed = false;
    }

    @Override
    protected void extract(final WLGraphics2D graphics, final int mouseX, final int mouseY, final float a) {
        final float dt = WLRenderSystem.frameDeltaSeconds();
        final boolean hovered = this.isHovered() && this.active;
        this.hoverAnim = approach(this.hoverAnim, hovered ? 1f : 0f, HOVER_SPEED, dt);

        if (this.armed && !isLeftMouseDown()) {
            this.armed = false;
            if (hovered) {
                this.fire();
            }
        }
        this.pressAnim = approach(this.pressAnim, this.armed ? 1f : 0f,
            this.armed ? PRESS_IN_SPEED : PRESS_OUT_SPEED, dt);

        final float alpha = clamp(this.getAlpha(), 0f, 1f);
        if (alpha <= 0.001f) {
            return;
        }

        final float grow = PRESS_GROW * this.pressAnim;
        final float growX = this.getWidth() * grow / 2f;
        final float growY = this.getHeight() * grow / 2f;
        final float x = this.getX() - growX;
        final float y = this.getY() - growY;
        final float w = Math.max(1f, this.getWidth() + growX * 2f);
        final float h = Math.max(1f, this.getHeight() + growY * 2f);

        final GlassStyle material = this.active
            ? this.style().hoverDarken(this.hoverAnim)
            : GlassStyle.INACTIVE.scaled(WLScreen.uiScaleFactor());
        graphics.glass()
            .dimensions(x, y, w, h)
            .cornerRadius(Math.min(CORNER_RADIUS * this.uiScale * (1f + grow), 0.5f * Math.min(w, h)))
            .style(material.opacity(alpha))
            .extract();

        this.extractLabel(graphics, x, y, w, h, alpha);
    }

    public void extractLabel(final WLGraphics2D graphics, final float alpha) {
        this.extractLabel(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), alpha);
    }

    private void extractLabel(final WLGraphics2D graphics, final float x, final float y,
                              final float w, final float h, final float alpha) {
        if (this.label.isEmpty()) {
            return;
        }
        final float size = LABEL_SIZE * this.uiScale;
        final float lineHeight = LABEL_LINE * this.uiScale;
        final float lineTop = y + (h - lineHeight) / 2f;
        final float baseline = lineTop + lineHeight / 2f + size * CAP_HEIGHT_RATIO * 0.5f;
        final MsdfFont font = labelFont();
        font.extract(graphics, this.label, x + (w - font.width(this.label, size)) / 2f,
            font.topForBaseline(baseline, size), size, ColorUtils.withAlpha(LABEL_COLOR, alpha));
    }

    @Override
    public void onClick(final MouseButtonEvent event, final boolean doubleClick) {
        this.armed = true;
    }

    @Override
    public void onRelease(final MouseButtonEvent event) {
        if (!this.armed) {
            return;
        }
        this.armed = false;
        if (this.active && this.isMouseOver(event.x(), event.y())) {
            this.fire();
        }
    }

    private void fire() {
        if (this.onPress != null) {
            this.onPress.run();
        }
    }

    private static boolean isLeftMouseDown() {
        return WalksyLib.getInstance().getMouseHandler().isLeftDown();
    }

    private static float approach(final float current, final float target, final float speed, final float dt) {
        if (dt <= 0f) {
            return current;
        }
        final float k = 1f - (float) Math.exp(-speed * dt);
        return current + (target - current) * k;
    }

    private static float clamp(final float v, final float min, final float max) {
        return Math.max(min, Math.min(max, v));
    }
}
