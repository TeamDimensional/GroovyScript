package com.cleanroommc.groovyscript.compat.mods.lightningcraft;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;
import org.jetbrains.annotations.Nullable;
import sblectric.lightningcraft.api.recipes.LightningInfusionRecipe;
import sblectric.lightningcraft.recipes.LightningInfusionRecipes;

import java.util.Collection;

@RegistryDescription
public class Infusion extends StandardListRegistry<LightningInfusionRecipe> {

    @Override
    public Collection<LightningInfusionRecipe> getRecipes() {
        return LightningInfusionRecipes.instance().getRecipeList();
    }

    @MethodDescription(example = @Example("item('minecraft:diamond')"))
    public boolean removeByOutput(IIngredient output) {
        return getRecipes().removeIf(r -> {
            if (output.test(r.getOutput())) {
                addBackup(r);
                return true;
            }
            return false;
        });
    }

    @RecipeBuilderDescription(example = {
        @Example(".input(item('minecraft:clay'), item('minecraft:gold_ingot'), item('minecraft:gold_ingot'), item('minecraft:iron_ingot'), item('minecraft:iron_ingot')).output(item('minecraft:nether_star')).le(500)"),
        @Example(".input(item('minecraft:clay'), item('minecraft:gold_ingot'), item('minecraft:potion').withNbt(['Potion': 'minecraft:leaping'])).output(item('minecraft:diamond_block')).le(200).nbtSensitive()"),
    })
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(gte = 1, lte = 5))
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<LightningInfusionRecipe> {

        @Property(comp = @Comp(gte = 0))
        private int le = -1;

        @Property
        private boolean nbtSensitive = false;

        public RecipeBuilder le(int le) {
            this.le = le;
            return this;
        }

        public RecipeBuilder cost(int le) {
            this.le = le;
            return this;
        }

        public RecipeBuilder nbtSensitive(boolean nbtSensitive) {
            this.nbtSensitive = nbtSensitive;
            return this;
        }

        public RecipeBuilder nbtSensitive() {
            this.nbtSensitive = true;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Lightningcraft Infusion Table recipe";
        }

        @Override
        protected int getMaxItemInput() {
            // recipes with more than 1 item in some slot don't get recognized
            return 1;
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 5, 1, 1);
            validateFluids(msg);
            msg.add(le < 0, "LE cost must be positive");
            for (IIngredient it : this.input) {
                msg.add(it == null || it.getMatchingStacks().length == 0, "all inputs must have a matching item");
            }
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public @Nullable LightningInfusionRecipe register() {
            if (!validate()) return null;
            Object centerItem = input.get(0).getMatchingStacks()[0];
            Object[] inputs = input.stream().skip(1).map(i -> i.getMatchingStacks()[0]).toArray();
            LightningInfusionRecipe recipe = new LightningInfusionRecipe(output.get(0), le, centerItem, inputs);
            if (nbtSensitive) recipe.setNBTSensitive();
            ModSupport.LIGHTNINGCRAFT.get().infusion.add(recipe);
            return recipe;
        }
    }

}
