package cy.jdkdigital.productivebees.recipe;

import com.google.gson.JsonObject;
import cy.jdkdigital.productivebees.ProductiveBees;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class BeeConversionRecipe implements IRecipe<IInventory>
{
    public static final IRecipeType<BeeConversionRecipe> BEE_CONVERSION = IRecipeType.register(ProductiveBees.MODID + ":bee_conversion");

    public final ResourceLocation id;
    public final Lazy<BeeIngredient> source;
    public final Lazy<BeeIngredient> result;
    public final Ingredient item;

    public BeeConversionRecipe(ResourceLocation id, Lazy<BeeIngredient> ingredients, Lazy<BeeIngredient> result, Ingredient item) {
        this.id = id;
        this.source = ingredients;
        this.result = result;
        this.item = item;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        if (inv instanceof BeeHelper.IdentifierInventory && source.get() != null) {
            String beeName = ((BeeHelper.IdentifierInventory) inv).getIdentifier(0);
            String itemName = ((BeeHelper.IdentifierInventory) inv).getIdentifier(1);

            String parentName = source.get().getBeeType().toString();

            boolean matchesItem = false;
            for (ItemStack stack : this.item.getMatchingStacks()) {
                if (stack.getItem().getRegistryName().toString().equals(itemName)) {
                    matchesItem = true;
                }
            }

            return parentName.equals(beeName) && matchesItem;
        }
        ProductiveBees.LOGGER.warn("conversion recipe source is null " + this);
        return false;
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
        return ModRecipeTypes.BEE_CONVERSION.get();
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return BEE_CONVERSION;
    }

    public static class Serializer<T extends BeeConversionRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T>
    {
        final BeeConversionRecipe.Serializer.IRecipeFactory<T> factory;

        public Serializer(BeeConversionRecipe.Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Nonnull
        @Override
        public T read(ResourceLocation id, JsonObject json) {
            String source = JSONUtils.getString(json, "source");
            String result = JSONUtils.getString(json, "result");

            Lazy<BeeIngredient> sourceBee = Lazy.of(BeeIngredientFactory.getIngredient(source));
            Lazy<BeeIngredient> resultBee = Lazy.of(BeeIngredientFactory.getIngredient(result));

            Ingredient item;
            if (JSONUtils.isJsonArray(json, "ingredient")) {
                item = Ingredient.deserialize(JSONUtils.getJsonArray(json, "item"));
            }
            else {
                item = Ingredient.deserialize(JSONUtils.getJsonObject(json, "item"));
            }

            return this.factory.create(id, sourceBee, resultBee, item);
        }

        public T read(@Nonnull ResourceLocation id, @Nonnull PacketBuffer buffer) {
            try {
                BeeIngredient source = BeeIngredient.read(buffer);
                BeeIngredient result = BeeIngredient.read(buffer);
                return this.factory.create(id, Lazy.of(() -> source), Lazy.of(() -> result), Ingredient.read(buffer));
            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error reading bee conversion recipe from packet. " + id, e);
                throw e;
            }
        }

        public void write(@Nonnull PacketBuffer buffer, T recipe) {
            try {
                recipe.source.get().write(buffer);
                recipe.result.get().write(buffer);
                recipe.item.write(buffer);
            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error writing bee conversion recipe to packet. " + recipe.getId(), e);
                throw e;
            }
        }

        public interface IRecipeFactory<T extends BeeConversionRecipe>
        {
            T create(ResourceLocation id, Lazy<BeeIngredient> input, Lazy<BeeIngredient> output, Ingredient item);
        }
    }
}
