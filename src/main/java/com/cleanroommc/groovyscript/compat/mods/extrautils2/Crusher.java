package com.cleanroommc.groovyscript.compat.mods.extrautils2;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.rwtema.extrautils2.api.machine.IMachineRecipe;
import com.rwtema.extrautils2.api.machine.XUMachineCrusher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RegistryDescription
public class Crusher extends VirtualizedRegistry<IMachineRecipe> {

    @Override
    public void onReload() {
        removeScripted().forEach(XUMachineCrusher.INSTANCE.recipes_registry::removeRecipe);
        restoreFromBackup().forEach(XUMachineCrusher.INSTANCE.recipes_registry::addRecipe);
    }

    public IMachineRecipe add(IMachineRecipe recipe) {
        if (recipe != null) {
            addScripted(recipe);
            XUMachineCrusher.INSTANCE.recipes_registry.addRecipe(recipe);
        }
        return recipe;
    }

    public boolean remove(IMachineRecipe recipe) {
        if (XUMachineCrusher.INSTANCE.recipes_registry.removeRecipe(recipe)) {
            addBackup(recipe);
            return true;
        }
        return false;
    }

    @MethodDescription(example = @Example("item('minecraft:blaze_rod')"))
    public boolean removeByInput(IIngredient input) {
        List<IMachineRecipe> agony = new ArrayList<>();
        for (IMachineRecipe recipe : XUMachineCrusher.INSTANCE.recipes_registry) {
            if (recipe.getJEIInputItemExamples().stream().flatMap(x -> x.getKey().get(XUMachineCrusher.INPUT).stream()).anyMatch(input)) {
                agony.add(recipe);
            }
        }
        for (IMachineRecipe recipe : agony) {
            addBackup(recipe);
            XUMachineCrusher.INSTANCE.recipes_registry.removeRecipe(recipe);
        }
        return !agony.isEmpty();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<IMachineRecipe> streamRecipes() {
        List<IMachineRecipe> list = new ArrayList<>();
        for (IMachineRecipe recipe : XUMachineCrusher.INSTANCE.recipes_registry) {
            list.add(recipe);
        }
        return new SimpleObjectStream<>(list).setRemover(this::remove);
    }

    @MethodDescription(priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        List<IMachineRecipe> agony = new ArrayList<>();
        for (IMachineRecipe recipe : XUMachineCrusher.INSTANCE.recipes_registry) {
            agony.add(recipe);
        }
        for (IMachineRecipe recipe : agony) {
            addBackup(recipe);
            XUMachineCrusher.INSTANCE.recipes_registry.removeRecipe(recipe);
        }
    }


    @RecipeBuilderDescription(example = {
            @Example(".input(item('minecraft:gold_ingot')).output(item('minecraft:clay')).energy(1000).time(5)"),
            @Example(".input(item('minecraft:blaze_rod')).output(item('minecraft:gold_ingot') * 3).output(item('minecraft:gold_ingot')).chance(0.2f).energy(1000).time(5)"),
    })
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Property(property = "input", comp = @Comp(eq = 1))
    @Property(property = "output", comp = @Comp(gte = 1, lte = 2))
    public static class RecipeBuilder extends AbstractRecipeBuilder<IMachineRecipe> {

        @Property(comp = @Comp(gte = 0))
        private int energy;
        @Property(comp = @Comp(gt = 0))
        private int time;
        @Property(comp = @Comp(gte = 0, lte = 1))
        private float chance;

        @RecipeBuilderMethodDescription
        public RecipeBuilder energy(int energy) {
            this.energy = energy;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder time(int time) {
            this.time = time;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder chance(float chance) {
            this.chance = chance;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Extra Utilities 2 Crusher recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 1, 2);
            validateFluids(msg);
            msg.add(energy < 0, () -> "energy must not be negative");
            msg.add(time <= 0, () -> "time must not be less than or equal to 0");
            msg.add(chance < 0 || chance > 1, "chance must be between 0 and 1, yet it was {}", chance);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public IMachineRecipe register() {
            if (!validate()) return null;
            com.rwtema.extrautils2.api.machine.RecipeBuilder builder = com.rwtema.extrautils2.api.machine.RecipeBuilder.newbuilder(XUMachineCrusher.INSTANCE);
            builder.setItemInput(XUMachineCrusher.INPUT, Arrays.asList(input.get(0).getMatchingStacks()), input.get(0).getAmount());
            builder.setItemOutput(XUMachineCrusher.OUTPUT, output.get(0));
            if (!IngredientHelper.isEmpty(output.getOrEmpty(1))) {
                builder.setItemOutput(XUMachineCrusher.OUTPUT_SECONDARY, output.get(1));
                builder.setProbability(XUMachineCrusher.OUTPUT_SECONDARY, chance);
            }
            builder.setEnergy(energy);
            builder.setProcessingTime(time);

            IMachineRecipe recipe = builder.build();

            ModSupport.EXTRA_UTILITIES_2.get().crusher.add(recipe);
            return recipe;
        }
    }
}
