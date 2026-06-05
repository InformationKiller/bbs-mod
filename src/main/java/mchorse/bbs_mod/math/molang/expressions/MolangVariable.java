package mchorse.bbs_mod.math.molang.expressions;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.math.molang.MolangParser;

public class MolangVariable extends Variable
{
    public MolangParser context;

    public MolangVariable(MolangParser context, String name)
    {
        super(name, 0);
        this.context = context;
    }

    @Override
    public IExpression get() {
        return this.context.getVariable(this.getName());
    }

    @Override
    public boolean isNumber() {
        return this.get().isNumber();
    }

    @Override
    public void set(double value) {
        this.get().set(value);
    }

    @Override
    public void set(String value) {
        this.get().set(value);
    }

    @Override
    public double doubleValue() {
        return this.get().doubleValue();
    }

    @Override
    public boolean booleanValue() {
        return this.get().booleanValue();
    }

    @Override
    public String stringValue() {
        return this.get().stringValue();
    }
}
