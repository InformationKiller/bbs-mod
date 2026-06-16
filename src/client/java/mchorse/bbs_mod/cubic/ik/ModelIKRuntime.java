package mchorse.bbs_mod.cubic.ik;

import mchorse.bbs_mod.cubic.IModel;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.constraints.ModelConstraintsConfig.BoneConstraint;
import mchorse.bbs_mod.cubic.constraints.ModelConstraintsRuntime;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.film.replays.PerLimbService;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.BodyPart;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.forms.renderers.ModelFormRenderer;
import mchorse.bbs_mod.settings.values.base.BaseValueBasic;
import mchorse.bbs_mod.utils.pose.Pose;
import mchorse.bbs_mod.utils.pose.PoseTransform;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModelIKRuntime
{
    private ModelIKRuntime()
    {
    }

    public static void clearCache()
    {
        ModelIKCache.clear();
    }

    public static void invalidate(String modelId)
    {
        clearCache();
    }

    public static void applyWithPoseFix(ModelInstance instance, Map<String, Float> poseFixByBone)
    {
        apply(instance, null, poseFixByBone);
    }

    public static void apply(ModelInstance instance, Map<String, Vector4f> controllerTargets, Map<String, Float> poseFixByBone)
    {
        if (instance == null || instance.model == null)
        {
            return;
        }

        IModel model = instance.model;

        ModelIKCache.Compiled compiled = null;
        if (instance.form instanceof ModelForm form && form.ik.get() instanceof MapType map)
        {
            compiled = ModelIKCache.getFromData(model, map);
        }

        if (compiled == null)
        {
            return;
        }

        List<ModelIKCache.CompiledChain> chains = compiled.chains();

        if (chains == null || chains.isEmpty())
        {
            return;
        }

        Map<String, BoneConstraint> boneLimits = ModelConstraintsRuntime.getBones(instance);

        ModelIKApplier.apply(model, chains, controllerTargets, poseFixByBone, boneLimits);
    }

    public static List<String> getControllers(ModelForm form)
    {
        if (form == null)
        {
            return java.util.Collections.emptyList();
        }

        ModelInstance instance = ModelFormRenderer.getModel(form);

        if (instance == null || instance.model == null)
        {
            return java.util.Collections.emptyList();
        }

        IModel model = instance.model;

        ModelIKCache.Compiled compiled = null;
        if (form.ik.get() instanceof MapType map)
        {
            compiled = ModelIKCache.getFromData(model, map);
        }

        if (compiled == null || compiled.chains() == null || compiled.chains().isEmpty())
        {
            return java.util.Collections.emptyList();
        }

        Set<String> unique = new LinkedHashSet<>();

        for (ModelIKCache.CompiledChain chain : compiled.chains())
        {
            if (chain != null && chain.target() != null && !chain.target().isEmpty())
            {
                unique.add(chain.target());
            }
        }

        return unique.isEmpty() ? java.util.Collections.emptyList() : new ArrayList<>(unique);
    }

    public static List<String> getAllControllers(ModelForm form)
    {
        List<String> ret = new ArrayList<>();
        List<String> result = getControllers(form);

        for (String bone : result)
        {
            ret.add(PerLimbService.toPoseBoneKey(FormUtils.getPath(form), bone));
        }

        for (BodyPart part : form.parts.getAllTyped())
        {
            if (part.getForm() instanceof ModelForm bodyForm)
            {
                ret.addAll(getAllControllers(bodyForm));
            }
        }

        return ret;
    }

    public static List<String> getPoleControllers(ModelForm form)
    {
        if (form == null)
        {
            return java.util.Collections.emptyList();
        }

        ModelInstance instance = ModelFormRenderer.getModel(form);

        if (instance == null || instance.model == null)
        {
            return java.util.Collections.emptyList();
        }

        IModel model = instance.model;

        ModelIKCache.Compiled compiled = null;
        if (form.ik.get() instanceof MapType map)
        {
            compiled = ModelIKCache.getFromData(model, map);
        }

        if (compiled == null || compiled.chains() == null || compiled.chains().isEmpty())
        {
            return java.util.Collections.emptyList();
        }

        Set<String> unique = new LinkedHashSet<>();

        for (ModelIKCache.CompiledChain chain : compiled.chains())
        {
            if (chain != null && chain.target() != null && !chain.target().isEmpty() && chain.pole() && chain.chainRootToEffector().size() == 3)
            {
                unique.add(chain.poleTarget());
            }
        }

        return unique.isEmpty() ? java.util.Collections.emptyList() : new ArrayList<>(unique);
    }

    public static Map<String, PoseTransform> bakeIK(ModelInstance instance, Map<String, Vector4f> controllerTargets, Map<String, Float> poseFixByBone, List<String> from)
    {
        ModelIKCache.Compiled compiled = null;
        if (instance.form instanceof ModelForm form && form.ik.get() instanceof MapType map)
        {
            compiled = ModelIKCache.getFromData(instance.model, map);
        }

        if (compiled == null)
        {
            return null;
        }

        List<ModelIKCache.CompiledChain> chains = compiled.chains();
        Set<String> bones = new HashSet<>();

        for (ModelIKCache.CompiledChain chain : chains)
        {
            if (from.contains(chain.target()))
            {
                bones.addAll(chain.chainRootToEffector());
            }
        }

        apply(instance, controllerTargets, poseFixByBone);

        Pose pose = instance.model.createPose();

        Map<String, PoseTransform> ret = new HashMap<>();

        for (String bone : bones)
        {
            ret.put(bone, pose.get(bone));
        }

        return ret;
    }
}
