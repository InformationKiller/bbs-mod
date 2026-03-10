package mchorse.bbs_mod.utils.iris;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.utils.Pair;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderCurves
{
    public static Map<String, ShaderVariable> variableMap = new HashMap<>();
    public static Set<String> prohibitedVariablesCurrent = new HashSet<>();

    private static Set<String> prohibitedVariables = new HashSet<>();
    private static Set<String> prohibitedConstIdentifiers = new HashSet<>();

    public static final String BRIGHTNESS = "brightness";
    public static final String SUN_ROTATION = "sun_rotation";
    public static final String SUN_PATH_ROTATION = "sun_path_rotation";
    public static final String WEATHER = "weather";
    public static final String CENTER_DEPTH = "center_depth";

    public static final String UNIFORM_IDENTIFIER = "bbs_";

    public static final String SUN_PATH_ROTATION_CONST = "sunPathRotation";
    public static final ShaderVariable SUN_PATH_ROTATION_VARIABLE = new ShaderVariable(SUN_PATH_ROTATION_CONST, "0.0", false);

    static
    {
        /* photon & Hysteria */
        prohibitedVariables.add("WATER_WAVE_ITERATIONS");
        prohibitedConstIdentifiers.add("get_luminance_from_exposure");
        prohibitedConstIdentifiers.add("get_exposure_from_luminance");
    }

    public static void reset()
    {
        variableMap.clear();
        prohibitedVariablesCurrent.clear();

        if (!BBSSettings.shaderCurvesEnabled.get())
        {
            return;
        }

        variableMap.put(SUN_PATH_ROTATION_CONST, SUN_PATH_ROTATION_VARIABLE);
        SUN_PATH_ROTATION_VARIABLE.defaultValue = 0.0f;
    }

    public static void finishLoading()
    {}

    public static String processSource(String source)
    {
        if (!BBSSettings.shaderCurvesEnabled.get())
        {
            return source;
        }

        Map<String, ShaderVariable> variables = parseVariables(source);
        variables.put(SUN_PATH_ROTATION_CONST, SUN_PATH_ROTATION_VARIABLE);

        if (!variables.isEmpty())
        {
            removeIrrelevantVariables(source, variables);

            source = patchSunPathRotation(source);
            source = replaceMacroReferences(source, variables);
            source = removeConstFromRelevantVariables(source);
            source = insertUniforms(source, variables);

            for (ShaderVariable value : variables.values())
            {
                variableMap.putIfAbsent(value.name, value);
            }
        }

        return source;
    }

    private static String patchSunPathRotation(String source)
    {
        Pattern constPattern = Pattern.compile("const\\s+float\\s+sunPathRotation\\s*=\\s*-?[\\d.ef]+\\s*;");

        return constPattern.matcher(source).replaceAll("");
    }

    private static void removeIrrelevantVariables(String source, Map<String, ShaderVariable> variables)
    {
        /* Remove irrelevant variables */
        List<String> filter = BBSRendering.getShadersSliderOptions();

        variables.values().removeIf((v) -> !filter.contains(v.name) && !v.name.equals(SUN_PATH_ROTATION_CONST));

        for (String prohibitedVariable : prohibitedVariables)
        {
            variables.remove(prohibitedVariable);
        }

        int index = 0;

        while ((index = source.indexOf("#", index + 1)) != -1)
        {
            int newLine = source.indexOf('\n', index);

            if (newLine >= 0)
            {
                String substr = source.substring(index, newLine);

                if (substr.startsWith("#if") || substr.startsWith("#elif"))
                {
                    variables.values().forEach((v) ->
                    {
                        if (substr.contains(v.name)) prohibitedVariablesCurrent.add(v.name);
                    });

                    variables.values().removeIf((v) -> substr.contains(v.name));
                }
                else if (substr.startsWith("#define"))
                {
                    final int WHITESPACE = 0, CHARACTERS = 1;
                    int iindex = 7;
                    int state = 0;
                    int switches = 0;

                    while (iindex < newLine - index)
                    {
                        char c = substr.charAt(iindex);

                        if (state == WHITESPACE && Character.isWhitespace(c))
                        {
                            state = CHARACTERS;
                            switches += 1;
                        }
                        else if (Character.isWhitespace(c))
                        {
                            state = WHITESPACE;
                        }

                        if (switches == 2)
                        {
                            break;
                        }

                        iindex += 1;
                    }

                    final String subsubstr = substr.substring(iindex);

                    variables.values().removeIf((v) -> subsubstr.contains(v.name));
                }
            }
        }
    }

    private static Map<String, ShaderVariable> parseVariables(String source)
    {
        Map<String, ShaderVariable> variables = new HashMap<>();
        Pattern definePattern = Pattern.compile("^\\s*(?!//)\\s*#define\\s+([\\w_]+)\\s+(-?[\\d.ef]+)\\s*//\\s*(\\[|OptionAnnotatedSource)");
        Pattern constPattern = Pattern.compile("^\\s*(?!//)\\s*const\\s+float\\s+sunPathRotation\\s*=\\s*(-?[\\d.ef]+)\\s*;");
        int index = 0;

        while ((index = source.indexOf("#define", index)) != -1)
        {
            int newLine = source.indexOf("\n", index);

            if (newLine == -1)
            {
                newLine = source.length();
            }

            int lastNewLine = source.lastIndexOf('\n', index);
            String define = source.substring(lastNewLine != -1 ? lastNewLine : index, newLine).trim();
            Matcher matcher = definePattern.matcher(define);

            if (matcher.find())
            {
                String name = matcher.group(1);
                String defaultValue = matcher.group(2);
                boolean integer = !defaultValue.contains(".");
                ShaderVariable variable = new ShaderVariable(name, defaultValue, integer);

                variables.putIfAbsent(variable.name, variable);
            }

            index = newLine;
        }

        index = 0;
        while ((index = source.indexOf("const", index)) != -1)
        {
            int newLine = source.indexOf("\n", index);

            if (newLine == -1)
            {
                newLine = source.length();
            }

            int lastNewLine = source.lastIndexOf('\n', index);
            String define = source.substring(lastNewLine != -1 ? lastNewLine : index, newLine).trim();
            Matcher matcher = constPattern.matcher(define);

            if (matcher.find())
            {
                String rotation = matcher.group(1);
                SUN_PATH_ROTATION_VARIABLE.defaultValue = Float.parseFloat(rotation);
                
                break;
            }

            index = newLine;
        }

        return variables;
    }

    private static String replaceMacroReferences(String source, Map<String, ShaderVariable> variables)
    {
        StringBuilder out = new StringBuilder(source.length());
        int length = source.length();
        int i = 0;
        boolean macro = false;

        while (i < length)
        {
            char c = source.charAt(i);

            if (c == '#') macro = true;
            if (c == '\n') macro = false;

            if (isIdentifierStart(c) && !macro)
            {
                int start = i;
                int j = i + 1;

                while (j < length && isIdentifierPart(source.charAt(j)))
                {
                    j++;
                }

                String identifier = source.substring(start, j);
                String replacement = variables.containsKey(identifier) ? UNIFORM_IDENTIFIER  + identifier : identifier;

                out.append(replacement);

                i = j;
            }
            else
            {
                out.append(c);

                i++;
            }
        }

        return out.toString();
    }

    private static boolean isIdentifierStart(char c)
    {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentifierPart(char c)
    {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static String removeConstFromRelevantVariables(String source)
    {
        Pair<String, Set<String>> pair = removeConst(source, (s) -> s.contains("bbs_"));
        Set<String> deconst = pair.b;

        source = pair.a;

        for (String constIdentifier : prohibitedConstIdentifiers)
        {
            deconst.add(constIdentifier);
        }

        while (!deconst.isEmpty())
        {
            final Set<Pattern> finalDeconst = new HashSet<>();
            for (String c : deconst)
            {
                finalDeconst.add(createVariableNamePattern(c));
            }

            pair = removeConst(source, (s) ->
            {
                for (Pattern string : finalDeconst)
                {
                    if (string.matcher(s).find()) return true;
                }

                return false;
            });
            source = pair.a;
            deconst = pair.b;
        }

        return source;
    }

    private static Pattern createVariableNamePattern(String variableName)
    {
        String quoted = Pattern.quote(variableName);
        String regex = "(?<![a-zA-Z0-9_$])" + quoted + "(?![a-zA-Z0-9_$])";

        return Pattern.compile(regex);
    }

    private static Pair<String, Set<String>> removeConst(String source, Function<String, Boolean> function)
    {
        Set<String> deconst = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int lastIndex = 0;

        while ((index = source.indexOf("const ", index + 1)) != -1)
        {
            int semicolon = source.indexOf(';', index);

            if (semicolon >= 0)
            {
                String substr = source.substring(index, semicolon);

                if (substr.indexOf('{') == -1 && function.apply(substr))
                {
                    builder.append(source, lastIndex, index);
                    builder.append(source, index + 6, semicolon);

                    int equals = substr.indexOf('=');
                    String sub = substr.substring(0, equals).trim();

                    equals = sub.lastIndexOf(' ');
                    sub = sub.substring(equals).trim();

                    deconst.add(sub);
                }
                else
                {
                    builder.append(source, lastIndex, semicolon);
                }
            }

            lastIndex = semicolon;
        }

        builder.append(source, lastIndex, source.length());

        return new Pair<>(builder.toString(), deconst);
    }

    private static String insertUniforms(String source, Map<String, ShaderVariable> variables)
    {
        int version = source.indexOf("#version");
        int nextNewLine = source.indexOf('\n', version);
        StringBuilder sb = new StringBuilder();

        for (ShaderVariable variable : variables.values())
        {
            sb.append(variable.toUniformDeclaration());
            sb.append('\n');
        }

        return source.substring(0, nextNewLine + 1) + sb + source.substring(nextNewLine + 1);
    }

    public static void addUniforms(List<CachedUniform> list)
    {
        BBSRendering.addUniforms(list, variableMap);
    }

    public static class ShaderVariable
    {
        public String name = "";
        public String uniformName = "";
        public boolean integer;
        public float defaultValue;
        public Float value;

        public ShaderVariable(String name, String defaultValue, boolean integer)
        {
            this.name = name;
            this.uniformName = UNIFORM_IDENTIFIER + name;
            this.defaultValue = Float.parseFloat(defaultValue);
            this.integer = integer;
        }

        public String toUniformDeclaration()
        {
            return "uniform " + (this.integer ? "int" : "float") + " " + this.uniformName + ";";
        }

        public float getValue()
        {
            if (this.value == null)
            {
                return this.defaultValue;
            }

            float v = this.value;

            this.value = null;

            return v;
        }
    }
}