package com.cleanroommc.groovyscript.compat.mods.roots;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import epicsquid.roots.init.ModRecipes;
import epicsquid.roots.recipe.FeyCraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@RegistryDescription(
        admonition = @Admonition(value = "groovyscript.wiki.roots.fey_crafter.note", type = Admonition.Type.DANGER, format = Admonition.Format.STANDARD)
)
public class FeyCrafter extends VirtualizedRegistry<Pair<ResourceLocation, FeyCraftingRecipe>> {

    @RecipeBuilderDescription(example = @Example(".name('clay_craft').input(item('minecraft:stone'),item('minecraft:stone'),item('minecraft:stone'),item('minecraft:stone'),item('minecraft:stone')) // Must be exactly 5.output(item('minecraft:clay')).xp(100)"))
    public static RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Override
    public void onReload() {
        removeScripted().forEach(pair -> ModRecipes.removeFeyCraftingRecipe(pair.getKey()));
        restoreFromBackup().forEach(pair -> ModRecipes.addFeyCraftingRecipe(pair.getKey(), pair.getValue()));
    }

    public void add(FeyCraftingRecipe recipe) {
        add(recipe.getName().contains(":") ? new ResourceLocation(recipe.getName()) : new ResourceLocation("roots", recipe.getName()), recipe);
    }

    public void add(ResourceLocation name, FeyCraftingRecipe recipe) {
        ModRecipes.addFeyCraftingRecipe(name, recipe);
        addScripted(Pair.of(name, recipe));
    }

    public ResourceLocation findRecipe(FeyCraftingRecipe recipe) {
        for (Map.Entry<ResourceLocation, FeyCraftingRecipe> entry : ModRecipes.getFeyCraftingRecipes().entrySet()) {
            if (entry.getValue().matches(recipe.getRecipe())) return entry.getKey();
        }
        return null;
    }

    public ResourceLocation findRecipeByOutput(ItemStack output) {
        for (Map.Entry<ResourceLocation, FeyCraftingRecipe> entry : ModRecipes.getFeyCraftingRecipes().entrySet()) {
            if (ItemStack.areItemsEqual(entry.getValue().getResult(), output)) return entry.getKey();
        }
        return null;
    }

    @MethodDescription(example = @Example("resource('roots:unending_bowl')"))
    public boolean removeByName(ResourceLocation name) {
        FeyCraftingRecipe recipe = ModRecipes.getFeyCraftingRecipe(name);
        if (recipe == null) return false;
        ModRecipes.removeFeyCraftingRecipe(name);
        addBackup(Pair.of(name, recipe));
        return true;
    }

    @MethodDescription(example = @Example("item('minecraft:gravel')"))
    public boolean removeByOutput(ItemStack output) {
        for (Map.Entry<ResourceLocation, FeyCraftingRecipe> x : ModRecipes.getFeyCraftingRecipes().entrySet()) {
            if (ItemStack.areItemsEqual(x.getValue().getResult(), output)) {
                ModRecipes.getFeyCraftingRecipes().remove(x.getKey());
                addBackup(Pair.of(x.getKey(), x.getValue()));
                return true;
            }
        }
        return false;
    }

    @MethodDescription(priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        ModRecipes.getFeyCraftingRecipes().forEach((key, value) -> addBackup(Pair.of(key, value)));
        ModRecipes.getFeyCraftingRecipes().clear();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<Map.Entry<ResourceLocation, FeyCraftingRecipe>> streamRecipes() {
        return new SimpleObjectStream<>(ModRecipes.getFeyCraftingRecipes().entrySet())
                .setRemover(r -> this.removeByName(r.getKey()));
    }

    @Property(property = "name")
    @Property(property = "input", comp = @Comp(eq = 5))
    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<FeyCraftingRecipe> {

        @Property(comp = @Comp(gte = 0))
        private int xp;

        @RecipeBuilderMethodDescription
        public RecipeBuilder xp(int xp) {
            this.xp = xp;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Roots Fey Crafter recipe";
        }

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_fey_crafter_";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateName();
            validateItems(msg, 5, 5, 1, 1);
            validateFluids(msg);
            msg.add(xp < 0, "xp must be a nonnegative integer, yet it was {}", xp);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public @Nullable FeyCraftingRecipe register() {
            if (!validate()) return null;
            FeyCraftingRecipe recipe = new FeyCraftingRecipe(output.get(0), xp);
            input.forEach(i -> recipe.addIngredient(i.toMcIngredient()));
            ModSupport.ROOTS.get().feyCrafter.add(super.name, recipe);
            return recipe;
        }
    }
}
