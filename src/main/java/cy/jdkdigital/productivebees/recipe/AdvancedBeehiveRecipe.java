package cy.jdkdigital.productivebees.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.init.ModItemGroups;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredient;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredientFactory;
import cy.jdkdigital.productivebees.util.BeeHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class AdvancedBeehiveRecipe extends TagOutputRecipe implements IRecipe<IInventory>
{
    public static final IRecipeType<AdvancedBeehiveRecipe> ADVANCED_BEEHIVE = IRecipeType.register(ProductiveBees.MODID + ":advanced_beehive");

    public final ResourceLocation id;
    public final Lazy<BeeIngredient> ingredient;

    public AdvancedBeehiveRecipe(ResourceLocation id, Lazy<BeeIngredient> ingredient, Map<Ingredient, IntArrayNBT> itemOutput, Map<Ingredient, IntArrayNBT> tagOutput) {
        super(itemOutput, tagOutput);
        this.id = id;
        this.ingredient = ingredient;
    }

    @Override
    public String toString() {
        return "AdvancedBeehiveRecipe{" +
                "id=" + id +
                ", bee=" + ingredient.get().getBeeEntity() +
                '}';
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        if (inv instanceof BeeHelper.IdentifierInventory && ingredient.get() != null) {
            String beeName = ((BeeHelper.IdentifierInventory)inv).getIdentifier();
            return beeName.equals(ingredient.get().getBeeType().toString());
        }
        if (ingredient.get() == null) {
            ProductiveBees.LOGGER.info(id + " is null");
        }

        return false;
    }

    @Override
    public Map<ItemStack, IntArrayNBT> getRecipeOutputs() {
        Map<ItemStack, IntArrayNBT> output = super.getRecipeOutputs();

        for(Map.Entry<ItemStack, IntArrayNBT> entry: output.entrySet()) {
            if (entry.getKey().getItem().equals(ModItems.CONFIGURABLE_HONEYCOMB.get()) && ingredient.get().isConfigurable()) {
                ModItemGroups.ModItemGroup.setTag(ingredient.get().getBeeType().toString(), entry.getKey());
            }
        }

        return output;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ADVANCED_BEEHIVE.get();
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return ADVANCED_BEEHIVE;
    }

    public static class Serializer<T extends AdvancedBeehiveRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T>
    {
        final IRecipeFactory<T> factory;

        public Serializer(Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation id, JsonObject json) {
            String beeName = JSONUtils.getString(json, "ingredient");

            String beeType = JSONUtils.getString(json, "bee_type", "");
            if (!beeType.isEmpty()) {
                beeName = beeType;
            }

            Lazy<BeeIngredient> beeIngredient = Lazy.of(BeeIngredientFactory.getIngredient(beeName));

            Map<Ingredient, IntArrayNBT> itemOutputs = new HashMap<>();
            Map<Ingredient, IntArrayNBT> tagOutputs = new HashMap<>();

            JsonArray jsonArray = JSONUtils.getJsonArray(json, "results");
            jsonArray.forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                String ingredientKey = "item_produce";
                if (ProductiveBeesConfig.GENERAL.enableCombProduce.get()) {
                    ingredientKey = "comb_produce";
                }

                Ingredient produce;
                if (JSONUtils.isJsonArray(json, ingredientKey)) {
                    produce = Ingredient.deserialize(JSONUtils.getJsonArray(jsonObject, ingredientKey));
                } else {
                    produce = Ingredient.deserialize(JSONUtils.getJsonObject(jsonObject, ingredientKey));
                }

//                if (!beeType.isEmpty() && ingredientKey.equals("comb_produce")) {
//                    ItemStack stack = new ItemStack(ModItems.CONFIGURABLE_HONEYCOMB.get());
//                    ModItemGroups.ModItemGroup.setTag(beeType, stack);
//                    produce = Ingredient.fromStacks(stack);
//                }

                int min = 1;
                int max = 1;
                if (ingredientKey.equals("item_produce")) {
                    min = JSONUtils.getInt(jsonObject, "min", 1);
                    max = JSONUtils.getInt(jsonObject, "max", 1);
                }
                int outputChance = JSONUtils.getInt(jsonObject, "chance", 100);
                IntArrayNBT nbt = new IntArrayNBT(new int[]{min, max, outputChance});

                if (ingredientKey.equals("item_produce")) {
                    itemOutputs.put(produce, nbt);
                } else {
                    tagOutputs.put(produce, nbt);
                }
            });

            return this.factory.create(id, beeIngredient, itemOutputs, tagOutputs);
        }

        public T read(@Nonnull ResourceLocation id, @Nonnull PacketBuffer buffer) {
            try {
                BeeIngredient ingredient = BeeIngredient.read(buffer);
                Map<Ingredient, IntArrayNBT> itemOutput = new HashMap<>();
                IntStream.range(0, buffer.readInt()).forEach(
                    i -> itemOutput.put(Ingredient.read(buffer), new IntArrayNBT(new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()}))
                );

                Map<Ingredient, IntArrayNBT> tagOutput = new HashMap<>();
                IntStream.range(0, buffer.readInt()).forEach(
                    i -> tagOutput.put(Ingredient.read(buffer), new IntArrayNBT(new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()}))
                );

                return this.factory.create(id, Lazy.of(() -> ingredient), itemOutput, tagOutput);
            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error reading beehive produce recipe from packet. " + id, e);
                throw e;
            }
        }

        public void write(@Nonnull PacketBuffer buffer, T recipe) {
            try {
                if (recipe.ingredient.get() != null) {
                    recipe.ingredient.get().write(buffer);
                } else {
                    ProductiveBees.LOGGER.error("Bee produce recipe ingredient missing " + recipe.getId() + " - " + recipe.ingredient);
                }
                buffer.writeInt(recipe.itemOutput.size());

                recipe.itemOutput.forEach((key, value) -> {
                    key.write(buffer);
                    buffer.writeInt(value.get(0).getInt());
                    buffer.writeInt(value.get(1).getInt());
                    buffer.writeInt(value.get(2).getInt());
                });

                buffer.writeInt(recipe.tagOutput.size());

                recipe.tagOutput.forEach((key, value) -> {
                    key.write(buffer);
                    buffer.writeInt(value.get(0).getInt());
                    buffer.writeInt(value.get(1).getInt());
                    buffer.writeInt(value.get(2).getInt());
                });

            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error writing beehive produce recipe to packet. " + recipe.getId(), e);
                throw e;
            }
        }

        public interface IRecipeFactory<T extends AdvancedBeehiveRecipe>
        {
            T create(ResourceLocation id, Lazy<BeeIngredient> input, Map<Ingredient, IntArrayNBT> itemOutput, Map<Ingredient, IntArrayNBT> tagOutput);
        }
    }
}
