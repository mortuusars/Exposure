package io.github.mortuusars.exposure.integration.iris;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.mortuusars.exposure.Exposure;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.BlendModeStorage;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL11C;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Most of this was written by AI. I'm too stupid to understand all this graphics sh*t. I'm sorry.
 * <br>
 * Intended purpose of this code is to properly handle rendering into a separate RenderTarget.
 * And it almost works as it should - world renders, Distant Horizons LODs render as well.
 * But there are still some issues with reflections in some cases, and TAA not having enough time to stabilize - producing a noisy image.
 * And there's some flickering on the next frame that I've been unable to fix.
 * <br>
 * But it's still better than without it. Though direct capture still the best, if quality is important.
 */
public class IrisCompat {
    private static final Field PIPELINE_FIELD = lookupField(PipelineManager.class, "pipeline");
    private static final @Nullable Field BLOCK_MAPPINGS_INITIALIZED_FIELD = lookupField(IrisRenderingPipeline.class, "initializedBlockIds");
    private static final @Nullable Field LEVEL_RENDERER_PIPELINE_FIELD = lookupPipelineField(LevelRenderer.class);
    private static final @Nullable Field DEPTH_BUFFER_VERSION_FIELD = lookupField(RenderTarget.class, "iris$depthBufferVersion");
    private static final @Nullable Field COLOR_BUFFER_VERSION_FIELD = lookupField(RenderTarget.class, "iris$colorBufferVersion");

    private static WeakReference<RenderTarget> lastTarget = new WeakReference<>(null);
    private static @Nullable String activeCaptureKey;
    private static @Nullable NamespacedId activeCaptureWorldId;
    private static boolean activeCaptureCanReuseBlockMappings;
    private static boolean pendingMainBlockMappingsInitialization;

    /**
     * Starts an isolated Iris pipeline for an offscreen camera render. Iris keeps its
     * colortex/TAA history on the pipeline, so reusing the world pipeline for a feed
     * render contaminates the next on-screen frame even when CapturedRenderingState
     * is restored.
     */
    public static CaptureScope beginBackgroundCapture(String captureKey) {
        return new CaptureScope(captureKey);
    }

    public static CaptureScope beginBackgroundCapture() {
        return new CaptureScope("default");
    }

    /**
     * Called by the optional PipelineManager mixin while a background capture is active.
     */
    public static NamespacedId getCapturePipelineId(NamespacedId worldId) {
        String key = activeCaptureKey;
        if (key == null) return worldId;

        activeCaptureWorldId = worldId;
        String safeKey = key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        if (safeKey.isBlank()) safeKey = "default";
        return new NamespacedId(worldId.getNamespace(), "exposure_capture_" + safeKey + "_" + worldId.getName());
    }

    /**
     * Keep the cache entry camera-specific while asking Iris for the real world's shaders.
     */
    public static NamespacedId getPipelineFactoryId(NamespacedId pipelineKey) {
        return activeCaptureKey != null && activeCaptureWorldId != null ? activeCaptureWorldId : pipelineKey;
    }

    /**
     * A capture pipeline uses the same dimension-specific program set as the player's
     * pipeline, so its first render can reuse Iris's already-built global block map.
     * This avoids Iris calling LevelRenderer.allChanged() for the background pass.
     */
    public static boolean canReuseWorldBlockMappings() {
        return activeCaptureKey != null
              && (activeCaptureCanReuseBlockMappings || pendingMainBlockMappingsInitialization);
    }

    public static boolean isBackgroundCaptureActive() {
        return activeCaptureKey != null;
    }

    /**
     * Skip Iris's allChanged call when this capture initializes the shared block maps.
     */
    public static boolean shouldSkipCaptureChunkReload() {
        return activeCaptureKey != null && !canReuseWorldBlockMappings();
    }

    /**
     * A capture may initialize maps before the real world pipeline is constructed.
     */
    public static boolean consumePendingMainBlockMappingsInitialization() {
        if (activeCaptureKey != null || !pendingMainBlockMappingsInitialization) return false;
        pendingMainBlockMappingsInitialization = false;
        return true;
    }

    public static boolean isShaderPackActive() {
        return Iris.getCurrentPack().isPresent();
    }

    public static final class CaptureScope implements AutoCloseable {
        private final @Nullable String previousCaptureKey;
        private final @Nullable NamespacedId previousCaptureWorldId;
        private final boolean previousCaptureCanReuseBlockMappings;
        private final @Nullable PipelineManager pipelineManager;
        private final @Nullable WorldRenderingPipeline previousPipeline;
        private final @Nullable LevelRenderer levelRenderer;
        private final @Nullable WorldRenderingPipeline previousLevelRendererPipeline;
        private final StoredCapturedRenderingState capturedState;
        private final boolean wasRenderingLevel;
        private final boolean wasShadowRendererActive;
        private final List<FieldValue> timerState;
        private final List<FieldValue> worldRenderingSettingsState;
        private boolean closed;

        private CaptureScope(String captureKey) {
            this.previousCaptureKey = activeCaptureKey;
            this.previousCaptureWorldId = activeCaptureWorldId;
            this.previousCaptureCanReuseBlockMappings = activeCaptureCanReuseBlockMappings;
            this.capturedState = StoredCapturedRenderingState.storeFrom(CapturedRenderingState.INSTANCE);
            this.wasRenderingLevel = ImmediateState.isRenderingLevel;
            this.wasShadowRendererActive = ShadowRenderer.ACTIVE;
            this.pipelineManager = Iris.getPipelineManager();
            this.previousPipeline = pipelineManager == null ? null : pipelineManager.getPipelineNullable();
            boolean canReuseBlockMappings = isBlockMappingsInitialized(previousPipeline);
            this.levelRenderer = Minecraft.getInstance().levelRenderer;
            this.previousLevelRendererPipeline = getPipeline(levelRenderer);
            this.timerState = snapshotObjectFields(SystemTimeUniforms.COUNTER, SystemTimeUniforms.TIMER);
            this.worldRenderingSettingsState = snapshotObjectFields(WorldRenderingSettings.INSTANCE);
            releaseIrisStateLocks();
            activeCaptureKey = captureKey;
            activeCaptureWorldId = null;
            activeCaptureCanReuseBlockMappings = canReuseBlockMappings;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;

            WorldRenderingPipeline capturePipeline = pipelineManager == null ? null : pipelineManager.getPipelineNullable();
            boolean captureInitializedBlockMappings = isBlockMappingsInitialized(capturePipeline);

            activeCaptureKey = previousCaptureKey;
            activeCaptureWorldId = previousCaptureWorldId;
            activeCaptureCanReuseBlockMappings = previousCaptureCanReuseBlockMappings;
            releaseIrisStateLocks();
            if (pipelineManager != null && PIPELINE_FIELD != null) {
                try {
                    PIPELINE_FIELD.set(pipelineManager, previousPipeline);
                } catch (IllegalAccessException e) {
                    Exposure.LOGGER.warn("Failed to restore Iris world pipeline after background capture", e);
                }
            }
            setPipeline(levelRenderer, previousLevelRendererPipeline);
            restoreObjectFields(timerState);
            if (!activeCaptureCanReuseBlockMappings && captureInitializedBlockMappings) {
                // The first capture can run before Iris has selected the player's pipeline.
                // Keep the block maps it just initialized, so the real pipeline can reuse
                // them without clearing the visible world's chunk renderer mid-frame.
                WorldRenderingSettings.INSTANCE.clearReloadRequired();
                if (previousPipeline instanceof IrisRenderingPipeline) {
                    setBlockMappingsInitialized(previousPipeline);
                } else {
                    pendingMainBlockMappingsInitialization = true;
                }
            } else {
                restoreObjectFields(worldRenderingSettingsState);
            }
            capturedState.restoreTo(CapturedRenderingState.INSTANCE);
            ImmediateState.isRenderingLevel = wasRenderingLevel;
            ShadowRenderer.ACTIVE = wasShadowRendererActive;
        }
    }

    private static List<FieldValue> snapshotObjectFields(Object... objects) {
        List<FieldValue> values = new ArrayList<>();
        for (Object object : objects) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    values.add(new FieldValue(field, object, field.get(object)));
                } catch (IllegalAccessException | RuntimeException e) {
                    Exposure.LOGGER.warn("Failed to snapshot Iris timing state", e);
                }
            }
        }
        return values;
    }

    private static void restoreObjectFields(List<FieldValue> values) {
        for (FieldValue value : values) {
            try {
                value.field.set(value.owner, value.value);
            } catch (IllegalAccessException | RuntimeException e) {
                Exposure.LOGGER.warn("Failed to restore Iris timing state", e);
            }
        }
    }

    private static @Nullable Field lookupField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException e) {
            return null;
        }
    }

    private static @Nullable Field lookupPipelineField(Class<?> owner) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.getType() != WorldRenderingPipeline.class) continue;
            try {
                field.setAccessible(true);
                return field;
            } catch (RuntimeException e) {
                Exposure.LOGGER.warn("Failed to access Iris pipeline field on " + owner.getName(), e);
                return null;
            }
        }
        return null;
    }

    private static boolean isBlockMappingsInitialized(@Nullable WorldRenderingPipeline pipeline) {
        if (!(pipeline instanceof IrisRenderingPipeline)
              || BLOCK_MAPPINGS_INITIALIZED_FIELD == null) {
            return false;
        }
        try {
            return BLOCK_MAPPINGS_INITIALIZED_FIELD.getBoolean(pipeline);
        } catch (IllegalAccessException e) {
            Exposure.LOGGER.warn("Failed to inspect Iris block map initialization state", e);
            return false;
        }
    }

    private static void setBlockMappingsInitialized(WorldRenderingPipeline pipeline) {
        if (!(pipeline instanceof IrisRenderingPipeline) || BLOCK_MAPPINGS_INITIALIZED_FIELD == null) return;
        try {
            BLOCK_MAPPINGS_INITIALIZED_FIELD.setBoolean(pipeline, true);
        } catch (IllegalAccessException e) {
            Exposure.LOGGER.warn("Failed to mark Iris block maps initialized after background capture", e);
        }
    }

    private static @Nullable WorldRenderingPipeline getPipeline(@Nullable Object owner) {
        if (owner == null || LEVEL_RENDERER_PIPELINE_FIELD == null) return null;
        try {
            return (WorldRenderingPipeline) LEVEL_RENDERER_PIPELINE_FIELD.get(owner);
        } catch (IllegalAccessException e) {
            Exposure.LOGGER.warn("Failed to read Iris LevelRenderer pipeline", e);
            return null;
        }
    }

    private static void setPipeline(@Nullable Object owner, @Nullable WorldRenderingPipeline pipeline) {
        if (owner == null || LEVEL_RENDERER_PIPELINE_FIELD == null) return;
        try {
            LEVEL_RENDERER_PIPELINE_FIELD.set(owner, pipeline);
        } catch (IllegalAccessException e) {
            Exposure.LOGGER.warn("Failed to restore Iris LevelRenderer pipeline", e);
        }
    }

    private static void releaseIrisStateLocks() {
        BlendModeStorage.restoreBlend();
        DepthColorStorage.unlockDepthColor();
    }

    private record FieldValue(Field field, Object owner, Object value) {
    }

    public static void onRenderTargetBound(RenderTarget target) {
        if (lastTarget.get() == target) {
            return;
        }

        lastTarget = new WeakReference<>(target);
        if (DEPTH_BUFFER_VERSION_FIELD == null && COLOR_BUFFER_VERSION_FIELD == null) {
            return;
        }

        try {
            if (DEPTH_BUFFER_VERSION_FIELD != null) {
                DEPTH_BUFFER_VERSION_FIELD.setInt(target, DEPTH_BUFFER_VERSION_FIELD.getInt(target) + 1);
            }
            if (COLOR_BUFFER_VERSION_FIELD != null) {
                COLOR_BUFFER_VERSION_FIELD.setInt(target, COLOR_BUFFER_VERSION_FIELD.getInt(target) + 1);
            }
        } catch (IllegalAccessException e) {
            Exposure.LOGGER.warn("Failed to bump Iris render-target version counters", e);
        }
    }

    public static class StoredCapturedRenderingState {
        private static final Vector3d ZERO_VECTOR_3d = new Vector3d();
        private Matrix4fc gbufferModelView;
        private Matrix4fc gbufferProjection;
        private Vector3d fogColor;
        private float fogDensity;
        private float darknessLightFactor;
        private float tickDelta;
        private float realTickDelta;
        private int currentRenderedBlockEntity;
        private int currentRenderedEntity = -1;
        private int currentRenderedItem = -1;
        private float currentAlphaTest;
        private float cloudTime;

        public StoredCapturedRenderingState(Matrix4fc gbufferModelView,
                                            Matrix4fc gbufferProjection,
                                            Vector3d fogColor,
                                            float fogDensity,
                                            float darknessLightFactor,
                                            float tickDelta,
                                            float realTickDelta,
                                            int currentRenderedBlockEntity,
                                            int currentRenderedEntity,
                                            int currentRenderedItem,
                                            float currentAlphaTest,
                                            float cloudTime) {
            this.gbufferModelView = gbufferModelView;
            this.gbufferProjection = gbufferProjection;
            this.fogColor = fogColor;
            this.fogDensity = fogDensity;
            this.darknessLightFactor = darknessLightFactor;
            this.tickDelta = tickDelta;
            this.realTickDelta = realTickDelta;
            this.currentRenderedBlockEntity = currentRenderedBlockEntity;
            this.currentRenderedEntity = currentRenderedEntity;
            this.currentRenderedItem = currentRenderedItem;
            this.currentAlphaTest = currentAlphaTest;
            this.cloudTime = cloudTime;
        }

        public Matrix4fc getGbufferModelView() {
            return this.gbufferModelView;
        }

        public void setGbufferModelView(Matrix4fc gbufferModelView) {
            this.gbufferModelView = gbufferModelView;
        }

        public Matrix4fc getGbufferProjection() {
            return this.gbufferProjection;
        }

        public void setGbufferProjection(Matrix4f gbufferProjection) {
            this.gbufferProjection = new Matrix4f(gbufferProjection);
        }

        public Vector3d getFogColor() {
            return Minecraft.getInstance().level != null && this.fogColor != null ? this.fogColor : ZERO_VECTOR_3d;
        }

        public void setFogColor(float red, float green, float blue) {
            this.fogColor = new Vector3d(red, green, blue);
        }

        public float getFogDensity() {
            return this.fogDensity;
        }

        public void setFogDensity(float fogDensity) {
            this.fogDensity = fogDensity;
        }

        public float getTickDelta() {
            return this.tickDelta;
        }

        public void setTickDelta(float tickDelta) {
            this.tickDelta = tickDelta;
        }

        public float getRealTickDelta() {
            return this.realTickDelta;
        }

        public void setRealTickDelta(float tickDelta) {
            this.realTickDelta = tickDelta;
        }

        public void setCurrentBlockEntity(int entity) {
            this.currentRenderedBlockEntity = entity;
        }

        public int getCurrentRenderedBlockEntity() {
            return this.currentRenderedBlockEntity;
        }

        public void setCurrentEntity(int entity) {
            this.currentRenderedEntity = entity;
        }

        public int getCurrentRenderedEntity() {
            return this.currentRenderedEntity;
        }

        public int getCurrentRenderedItem() {
            return this.currentRenderedItem;
        }

        public void setCurrentRenderedItem(int item) {
            this.currentRenderedItem = item;
        }

        public float getCurrentAlphaTest() {
            return this.currentAlphaTest;
        }

        public void setCurrentAlphaTest(float alphaTest) {
            this.currentAlphaTest = alphaTest;
        }

        public float getDarknessLightFactor() {
            return this.darknessLightFactor;
        }

        public void setDarknessLightFactor(float factor) {
            this.darknessLightFactor = factor;
        }

        public float getCloudTime() {
            return this.cloudTime;
        }

        public void setCloudTime(float cloudTime) {
            this.cloudTime = cloudTime;
        }

        public void restoreTo(CapturedRenderingState state) {
            state.setGbufferModelView(gbufferModelView);
            state.setGbufferProjection((Matrix4f) gbufferProjection);
            state.setFogColor((float) fogColor.x, (float) fogColor.y, (float) fogColor.z);
            state.setFogDensity(fogDensity);
            state.setDarknessLightFactor(darknessLightFactor);
            state.setTickDelta(tickDelta);
            state.setRealTickDelta(realTickDelta);
            state.setCurrentBlockEntity(currentRenderedBlockEntity);
            state.setCurrentEntity(currentRenderedEntity);
            state.setCurrentRenderedItem(currentRenderedItem);
            state.setCurrentAlphaTest(currentAlphaTest);
            state.setCloudTime(cloudTime);
        }

        public static StoredCapturedRenderingState storeFrom(CapturedRenderingState state) {
            return new StoredCapturedRenderingState(
                  new Matrix4f(state.getGbufferModelView()),
                  new Matrix4f(state.getGbufferProjection()),
                  new Vector3d(state.getFogColor()),
                  state.getFogDensity(), state.getDarknessLightFactor(), state.getTickDelta(),
                  state.getRealTickDelta(), state.getCurrentRenderedBlockEntity(), state.getCurrentRenderedEntity(),
                  state.getCurrentRenderedItem(), state.getCurrentAlphaTest(), state.getCloudTime());
        }
    }

    public record RenderSystemState(Matrix4f projMatrix, Matrix4f modelViewMatrix, Matrix4f textureMatrix,
                                    Matrix4f lastSavedProj,
                                    VertexSorting vertexSorting, VertexSorting lastSavedVertexSorting,
                                    Matrix4fStack modelViewStack,
                                    int[] shaderTextures, float[] shaderColor, float glintAlpha, float shaderFogStart,
                                    float shaderFogEnd,
                                    float[] shaderFogColor, FogShape shaderFogShape, Vector3f[] lights, float shaderGameTime,
                                    float lineWidth, float[] clearColor, ShaderInstance shader) {

        public static RenderSystemState store() {
            Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
            Matrix4f model = new Matrix4f(RenderSystem.getModelViewMatrix());
            Matrix4f texture = new Matrix4f(RenderSystem.getTextureMatrix());
            Matrix4f lastSavedProj = new Matrix4f(RenderSystem.savedProjectionMatrix);
            VertexSorting lastVertexSorting = RenderSystem.getVertexSorting();
            VertexSorting lastSavedVertexSorting = RenderSystem.savedVertexSorting;
            Matrix4fStack modelViewStack = null;
            try {
                modelViewStack = (Matrix4fStack) RenderSystem.modelViewStack.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            int[] textures = RenderSystem.shaderTextures.clone();
            float[] shaderColor = RenderSystem.getShaderColor().clone();
            float glintAlpha = RenderSystem.getShaderGlintAlpha();
            float shaderFogStart = RenderSystem.getShaderFogStart();
            float shaderFogEnd = RenderSystem.getShaderFogEnd();
            float[] shaderFogColor = RenderSystem.getShaderFogColor();
            FogShape shaderFogShape = RenderSystem.getShaderFogShape();
            Vector3f[] lights = RenderSystem.shaderLightDirections.clone();
            float gameTime = RenderSystem.getShaderGameTime();
            float lineWidth = RenderSystem.getShaderLineWidth();
            float[] clearColor = new float[4];
            GL11C.glGetFloatv(GL11C.GL_COLOR_CLEAR_VALUE, clearColor);
            ShaderInstance shader = RenderSystem.getShader();
            return new RenderSystemState(proj, model, texture, lastSavedProj, lastVertexSorting, lastSavedVertexSorting,
                  modelViewStack, textures, shaderColor, glintAlpha, shaderFogStart, shaderFogEnd, shaderFogColor,
                  shaderFogShape, lights, gameTime, lineWidth, clearColor, shader);
        }

        public void restore() {
            if (modelViewMatrix != null) RenderSystem.modelViewStack.set(modelViewStack);
            RenderSystem.setProjectionMatrix(projMatrix, vertexSorting);
            RenderSystem.getModelViewMatrix().set(modelViewMatrix);
            RenderSystem.setTextureMatrix(textureMatrix);
            RenderSystem.savedProjectionMatrix = lastSavedProj;
            RenderSystem.savedVertexSorting = lastSavedVertexSorting;
            System.arraycopy(shaderTextures, 0, RenderSystem.shaderTextures, 0, shaderTextures.length);
            System.arraycopy(shaderColor, 0, RenderSystem.getShaderColor(), 0, shaderColor.length);
            RenderSystem.setShaderGlintAlpha(glintAlpha);
            RenderSystem.setShaderFogStart(shaderFogStart);
            RenderSystem.setShaderFogEnd(shaderFogEnd);
            System.arraycopy(shaderFogColor, 0, RenderSystem.getShaderFogColor(), 0, shaderFogColor.length);
            RenderSystem.setShaderFogShape(shaderFogShape);
            System.arraycopy(lights, 0, RenderSystem.shaderLightDirections, 0, lights.length);
            RenderSystem.shaderGameTime = shaderGameTime;
            RenderSystem.lineWidth(lineWidth);
            RenderSystem.clearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
            RenderSystem.setShader(() -> shader);
        }
    }
}
