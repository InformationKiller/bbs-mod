package mchorse.bbs_mod.math.molang;

import java.util.HashMap;
import java.util.Map;

import mchorse.bbs_mod.math.Variable;

public class MolangContext
{
    private Map<String, Variable> variables = new HashMap<>();

    public Variable get(String name)
    {
        Variable var = variables.get(name);
        
        if (var == null)
        {
            var = new Variable(name, 0);
            variables.put(name, var);
        }

        return var;
    }
}
