package com.cleanroommc.groovyscript.compat.mods.actuallyadditions;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.recipe.TreasureChestLoot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@RegistryDescription
public class TreasureChest extends StandardListRegistry<TreasureChestLoot> {

    @RecipeBuilderDescription(example = @Example(".output(item('minecraft:clay')).weight(50).min(16).max(32)"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @Override
    public Collection<TreasureChestLoot> getRecipes() {
        return ActuallyAdditionsAPI.TREASURE_CHEST_LOOT;
    }

    public TreasureChestLoot add(ItemStack returnItem, int chance, int minAmount, int maxAmount) {
        TreasureChestLoot recipe = new TreasureChestLoot(returnItem, chance, minAmount, maxAmount);
        add(recipe);
        return recipe;
    }

    @MethodDescription(example = @Example("item('minecraft:iron_ingot')"))
    public boolean removeByOutput(ItemStack output) {
        return getRecipes().removeIf(recipe -> {
            boolean matches = ItemStack.areItemStacksEqual(recipe.returnItem, output);
            if (matches) {
                addBackup(recipe);
            }
            return matches;
        });
    }

    @Property(property = "output", comp = @Comp(eq = 1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<TreasureChestLoot> {

        @Property(comp = @Comp(gte = 0))
        private int weight;
        @Property(comp = @Comp(gte = 0, unique = "groovyscript.wiki.actuallyadditions.treasure_chest.min.required"))
        private int min;
        @Property(comp = @Comp(gte = 0, unique = "groovyscript.wiki.actuallyadditions.treasure_chest.max.required"))
        private int max;

        @RecipeBuilderMethodDescription
        public RecipeBuilder weight(int weight) {
            this.weight = weight;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder min(int min) {
            this.min = min;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder max(int max) {
            this.max = max;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Actually Additions Treasure Chest Loot recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 0, 0, 1, 1);
            validateFluids(msg);
            msg.add(weight < 0, "weight must be a non negative integer, yet it was {}", weight);
            msg.add(min < 0, "min must be a non negative integer, yet it was {}", min);
            msg.add(max < 0, "max must be a non negative integer, yet it was {}", max);
            msg.add(max < min, "max must be greater than min, yet max was {} while min was {}", max, min);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public @Nullable TreasureChestLoot register() {
            if (!validate()) return null;
            TreasureChestLoot recipe = new TreasureChestLoot(output.get(0), weight, min, max);
            ModSupport.ACTUALLY_ADDITIONS.get().treasureChest.add(recipe);
            return recipe;
        }
    }
}
