package com.cleanroommc.groovyscript.compat.mods.roots;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import epicsquid.roots.init.ModRecipes;
import epicsquid.roots.recipe.AnimalHarvestFishRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@RegistryDescription
public class AnimalHarvestFish extends VirtualizedRegistry<AnimalHarvestFishRecipe> {

    @RecipeBuilderDescription(example = {
            @Example(".name('clay_fish').weight(50).output(item('minecraft:clay'))"),
            @Example(".weight(13).fish(item('minecraft:gold_ingot'))")
    })
    public static RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Override
    public void onReload() {
        removeScripted().forEach(recipe -> ModRecipes.removeAnimalHarvestFishRecipe(recipe.getRegistryName()));
        restoreFromBackup().forEach(recipe -> ModRecipes.getAnimalHarvestFishRecipes().put(recipe.getRegistryName(), recipe));
    }

    public void add(AnimalHarvestFishRecipe recipe) {
        add(recipe.getRegistryName(), recipe);
    }

    public void add(ResourceLocation name, AnimalHarvestFishRecipe recipe) {
        ModRecipes.getAnimalHarvestFishRecipes().put(name, recipe);
        addScripted(recipe);
    }

    public ResourceLocation findRecipeByOutput(ItemStack output) {
        for (Map.Entry<ResourceLocation, AnimalHarvestFishRecipe> entry : ModRecipes.getAnimalHarvestFishRecipes().entrySet()) {
            if (ItemStack.areItemsEqual(entry.getValue().getItemStack(), output)) return entry.getKey();
        }
        return null;
    }

    @MethodDescription(example = @Example("resource('roots:cod')"))
    public boolean removeByName(ResourceLocation name) {
        AnimalHarvestFishRecipe recipe = ModRecipes.getAnimalHarvestFishRecipes().get(name);
        if (recipe == null) return false;
        ModRecipes.removeAnimalHarvestFishRecipe(name);
        addBackup(recipe);
        return true;
    }

    @MethodDescription(example = @Example("item('minecraft:fish:1')"))
    public boolean removeByOutput(ItemStack output) {
        for (Map.Entry<ResourceLocation, AnimalHarvestFishRecipe> x : ModRecipes.getAnimalHarvestFishRecipes().entrySet()) {
            if (ItemStack.areItemsEqual(x.getValue().getItemStack(), output)) {
                ModRecipes.getAnimalHarvestFishRecipes().remove(x.getKey());
                addBackup(x.getValue());
                return true;
            }
        }
        return false;
    }

    @MethodDescription(example = @Example("item('minecraft:fish:2')"))
    public boolean removeByFish(ItemStack fish) {
        return removeByOutput(fish);
    }

    @MethodDescription(priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        ModRecipes.getAnimalHarvestFishRecipes().values().forEach(this::addBackup);
        ModRecipes.getAnimalHarvestFishRecipes().clear();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<Map.Entry<ResourceLocation, AnimalHarvestFishRecipe>> streamRecipes() {
        return new SimpleObjectStream<>(ModRecipes.getAnimalHarvestFishRecipes().entrySet())
                .setRemover(r -> this.removeByName(r.getKey()));
    }

    @Property(property = "name")
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<AnimalHarvestFishRecipe> {

        @Property(comp = @Comp(gt = 0))
        private int weight;

        @RecipeBuilderMethodDescription
        public RecipeBuilder weight(int weight) {
            this.weight = weight;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "output")
        public RecipeBuilder fish(ItemStack fish) {
            this.output.add(fish);
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Roots Animal Harvest Fish recipe";
        }

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_animal_harvest_fish_";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateName();
            validateItems(msg, 0, 0, 1, 1);
            validateFluids(msg);
            msg.add(weight <= 0, "weight must be a nonnegative integer greater than 0, instead it was {}", weight);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public @Nullable AnimalHarvestFishRecipe register() {
            if (!validate()) return null;
            AnimalHarvestFishRecipe recipe = new AnimalHarvestFishRecipe(super.name, output.get(0), weight);
            ModSupport.ROOTS.get().animalHarvestFish.add(super.name, recipe);
            return recipe;
        }
    }
}
